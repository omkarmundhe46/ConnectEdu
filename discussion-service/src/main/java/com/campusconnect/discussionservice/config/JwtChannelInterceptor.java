package com.campusconnect.discussionservice.config;

import com.campusconnect.discussionservice.client.ClubClient;
import com.campusconnect.discussionservice.client.EventClient;
import com.campusconnect.discussionservice.dto.EventDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
//    private final EventClient eventClient;
//    private final ClubClient clubClient;

    // Pattern to extract eventId from a destination like "/app/chat.sendMessage/123"
    private static final Pattern eventIdPattern = Pattern.compile("/event/(\\d+)");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            throw new AccessDeniedException("Invalid message headers");
        }

        // We only care about the first CONNECT or SEND message
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. Get the token from the "Authorization" header
            List<String> authHeader = accessor.getNativeHeader("Authorization");
            if (authHeader == null || authHeader.isEmpty() || !authHeader.get(0).startsWith("Bearer ")) {
                log.warn("No 'Authorization' header found in STOMP CONNECT message.");
                throw new AccessDeniedException("No 'Authorization' header found in STOMP CONNECT message.");
            }

            String jwtToken = authHeader.get(0).substring(7);

            // 2. Validate the token and build the Spring Security Principal (the 'User')
            Claims claims = jwtService.extractAllClaims(jwtToken);
            String userEmail = claims.getSubject();
            Long userId = claims.get("userId", Long.class);

            if (userEmail == null || userId == null) {
                throw new AccessDeniedException("Invalid JWT token claims.");
            }

            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()) : Collections.emptyList();

            Jwt jwt = buildJwt(jwtToken, claims);

            // We do not check for event/club membership here.
            // We only authenticate.

            // 4. Set the authenticated user on the WebSocket session
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(jwt, null, authorities);
            accessor.setUser(authToken);
            log.info("User {} successfully authenticated for WebSocket session.", userEmail);
        }

        return message;
    }

    // Helper to extract eventId from "/app/chat.sendMessage/123" or "/topic/event/123"
    private Long getEventIdFromDestination(String destination) {
        if (destination == null) {
            return null;
        }
        Matcher matcher = eventIdPattern.matcher(destination);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        // Try the other pattern (for SEND messages)
        matcher = Pattern.compile("/chat\\.sendMessage/(\\d+)").matcher(destination);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    // Helper to build a Spring Security Jwt object from raw claims
    private Jwt buildJwt(String tokenValue, Claims claims) {
        Jwt.Builder jwtBuilder = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .subject(claims.getSubject())
                .issuedAt(claims.getIssuedAt().toInstant())
                .expiresAt(claims.getExpiration().toInstant());

        claims.forEach((key, value) -> {
            if (!key.equals(Claims.ISSUED_AT) && !key.equals(Claims.EXPIRATION) && !key.equals(Claims.SUBJECT)) {
                jwtBuilder.claim(key, value);
            }
        });
        return jwtBuilder.build();
    }
}
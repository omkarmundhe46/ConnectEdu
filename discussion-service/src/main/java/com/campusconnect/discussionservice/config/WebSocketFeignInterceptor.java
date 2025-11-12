package com.campusconnect.discussionservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
public class WebSocketFeignInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        // This is the standard way to get the token for HTTP requests
        if (RequestContextHolder.getRequestAttributes() != null) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String authorizationHeader = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                template.header(AUTHORIZATION_HEADER, authorizationHeader);
                return;
            }
        }

        // --- THIS IS THE FIX ---
        // This is the fallback to get the token from a WebSocket context
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            try {
                UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) authToken.getPrincipal();
                String tokenValue = jwt.getTokenValue();

                if (tokenValue != null) {
                    template.header(AUTHORIZATION_HEADER, "Bearer " + tokenValue);
                    log.info("Applying JWT token to Feign request from WebSocket context");
                }
            } catch (Exception e) {
                log.error("Could not apply JWT token from WebSocket context: {}", e.getMessage());
            }
        }
    }
}
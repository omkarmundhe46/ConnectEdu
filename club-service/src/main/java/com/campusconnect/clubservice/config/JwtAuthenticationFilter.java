package com.campusconnect.clubservice.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtString = authHeader.substring(7);
        Claims claims = jwtService.extractAllClaims(jwtString);
        String userEmail = claims.getSubject();
        log.info("JWT Filter: Processing token for user: {}", userEmail);
        log.debug("JWT Filter: Raw claims received: {}", claims);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // The token already contains the "ROLE_" prefix (e.g., "ROLE_COLLEGE_ADMIN").
            // We now create the authorities directly from the roles in the token without adding another prefix.
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                    .map(SimpleGrantedAuthority::new) // Directly use the role string from the token
                    .collect(Collectors.toList()) : Collections.emptyList();

            log.info("JWT Filter: Extracted roles from token: {}", roles);
            log.info("JWT Filter: Converted authorities for Spring Security: {}", authorities);

            Jwt.Builder jwtBuilder = Jwt.withTokenValue(jwtString)
                    .header("alg", "HS256")
                    .subject(userEmail)
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant());

            claims.forEach((key, value) -> {
                if (!key.equals(Claims.ISSUED_AT) && !key.equals(Claims.EXPIRATION) && !key.equals(Claims.SUBJECT)) {
                    jwtBuilder.claim(key, value);
                }
            });

            Jwt jwt = jwtBuilder.build();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    jwt, null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
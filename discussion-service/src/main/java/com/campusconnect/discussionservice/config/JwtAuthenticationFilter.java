package com.campusconnect.discussionservice.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // --- THIS IS THE FIX ---
            // The token already contains the "ROLE_" prefix (e.g., "ROLE_COLLEGE_ADMIN").
            // We now create the authorities directly from the roles in the token without adding another prefix.
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                    .map(SimpleGrantedAuthority::new) // Directly use the role string from the token
                    .collect(Collectors.toList()) : Collections.emptyList();

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
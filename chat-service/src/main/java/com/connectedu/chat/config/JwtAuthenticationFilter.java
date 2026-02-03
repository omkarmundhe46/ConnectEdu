package com.connectedu.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // 1. Decode Secret Key
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            Key signInKey = Keys.hmacShaKeyFor(keyBytes);

            // 2. Extract Claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signInKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            // 3. Extract User ID (Assuming it's stored in "userId" claim or Subject)
            // Adjust "userId" to match exactly what your User-Service puts in the token
            Integer userIdInt = (Integer) claims.get("userId");
            Long userId = userIdInt != null ? userIdInt.longValue() : null;

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 4. Create Authentication Object
                // We store the userId as the "Principal" so the Controller can grab it easily
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, // Principal
                        null,
                        new ArrayList<>() // Authorities (Roles) - Add if needed
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token invalid or expired
            System.err.println("JWT Verification Failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
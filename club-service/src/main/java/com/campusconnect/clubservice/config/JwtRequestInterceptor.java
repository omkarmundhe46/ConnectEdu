package com.campusconnect.clubservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JwtRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                // Forward the Bearer token to the downstream service
                requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
            }
        }
    }
}
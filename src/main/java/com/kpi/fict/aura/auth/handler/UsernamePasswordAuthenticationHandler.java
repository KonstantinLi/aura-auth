package com.kpi.fict.aura.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.fict.aura.auth.dto.AuthTokensResponse;
import com.kpi.fict.aura.auth.model.AuthenticatedUser;
import com.kpi.fict.aura.auth.service.SecurityService;
import com.kpi.fict.aura.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UsernamePasswordAuthenticationHandler extends BaseHandler {

    private final ObjectMapper objectMapper;

    public UsernamePasswordAuthenticationHandler(SecurityService securityService, UserService userService, ObjectMapper objectMapper) {
        super(userService, securityService);
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        Long userId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        AuthTokensResponse authTokensResponse = securityService.generateAuthTokens(userId);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(authTokensResponse));
    }

}
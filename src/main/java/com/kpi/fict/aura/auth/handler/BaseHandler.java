package com.kpi.fict.aura.auth.handler;


import com.kpi.fict.aura.auth.service.SecurityService;
import com.kpi.fict.aura.auth.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public abstract class BaseHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    protected final UserService userService;
    protected final SecurityService securityService;

    protected BaseHandler(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        throw new ServletException(); // should override
    }

}

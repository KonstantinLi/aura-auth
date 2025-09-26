package com.kpi.fict.aura.auth.handler;

import com.kpi.fict.aura.auth.dto.AuthenticatedUserInfo;
import com.kpi.fict.aura.auth.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClearTokensLogoutHandler implements LogoutHandler, LogoutSuccessHandler {

    private final TokenRepository tokenRepository;

    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        AuthenticatedUserInfo authenticatedUser = (AuthenticatedUserInfo) authentication.getPrincipal();
        tokenRepository.deleteByUserId(authenticatedUser.id());
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpServletResponse.SC_OK);
    }

}

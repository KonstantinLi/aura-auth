package com.kpi.fict.aura.auth.handler;

import com.kpi.fict.aura.auth.dto.AuthTokensResponse;
import com.kpi.fict.aura.auth.dto.OAuth2UserDto;
import com.kpi.fict.aura.auth.exception.ExternalUserProcessorNotFoundException;
import com.kpi.fict.aura.auth.handler.processor.ExternalUserProcessor;
import com.kpi.fict.aura.auth.mapper.UserMapper;
import com.kpi.fict.aura.auth.model.AuthenticatedUser;
import com.kpi.fict.aura.auth.service.SecurityService;
import com.kpi.fict.aura.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class OAuth2AuthenticationHandler extends BaseHandler {

    private static final String SIGN_IN_URI = "/authentication/sign-in";
    private static final String WELCOME_PAGE_URI = "/authentication/welcome";

    private final String clientHost;
    private final UserMapper userMapper;
    private final List<ExternalUserProcessor> externalUserProcessors;

    public OAuth2AuthenticationHandler(UserMapper userMapper, UserService userService,
            SecurityService securityService, List<ExternalUserProcessor> externalUserProcessors,
            @Value("${application.host}") String host, @Value("${spring.profiles.active:local}") String profile) {
        super(userService, securityService);
        this.userMapper = userMapper;
        this.externalUserProcessors = externalUserProcessors;
        this.clientHost = !profile.equals("local") ? host : host.replace("8080", "3001");
    }

    private ExternalUserProcessor defineExternalUserProcessor(OidcUser externalUser) {
        return externalUserProcessors.stream().filter(processor -> processor.canProcess(externalUser)).findAny()
                .orElseThrow(() -> new ExternalUserProcessorNotFoundException(externalUser.getIssuer().toString()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OidcUser externalUser = (OidcUser) authentication.getPrincipal();
        OAuth2UserDto externalUserDto = defineExternalUserProcessor(externalUser)
                .extractExternalUser(externalUser, authentication);
        AuthenticatedUser user = userService.findByUsername(externalUserDto.email())
                .map(userMapper::toAuthenticatedUser).orElse(null);
        if (user != null) {
            setUpResponse(user.getId(), clientHost + WELCOME_PAGE_URI, response);
        }
        try {
            Long userId = securityService.saveUser(userMapper.toExternalSignUpRequest(externalUserDto)).userId();
            setUpResponse(userId, clientHost + WELCOME_PAGE_URI, response);
        } catch (Exception ex) {
            log.debug("OAuth2 registration failed: {}", ex.getMessage(), ex);
            setUpResponse(null, clientHost + SIGN_IN_URI, response);
        }
    }

    private void setUpResponse(Long userId, String redirect, HttpServletResponse response) throws IOException {
        if (userId != null) {
            AuthTokensResponse authTokensResponse = securityService.generateAuthTokens(userId);
            response.addCookie(createCookie("accessToken", authTokensResponse.token()));
            response.addCookie(createCookie("refreshToken", authTokensResponse.refreshToken()));
            response.addCookie(createCookie("scope", authTokensResponse.details().getScope().toString()));
        }
        response.sendRedirect(redirect);
    }

    private Cookie createCookie(String name, String value) {
        var cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }

}
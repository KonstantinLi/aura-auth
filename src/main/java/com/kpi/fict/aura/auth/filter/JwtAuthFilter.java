package com.kpi.fict.aura.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.fict.aura.auth.dto.AuthenticatedUserInfo;
import com.kpi.fict.aura.auth.dto.TokenScope;
import com.kpi.fict.aura.auth.exception.ErrorResponse;
import com.kpi.fict.aura.auth.exception.TokenScopePermissionDeniedException;
import com.kpi.fict.aura.auth.service.JwtService;
import com.kpi.fict.aura.auth.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.AUTHORIZATION_HEADER_NAME;
import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.AUTHORIZATION_TOKEN_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final SecurityService securityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String authorizationHeaderValue = Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER_NAME))
                .orElseGet(() -> Optional.ofNullable(getCookie(request, "accessToken"))
                        .map(AUTHORIZATION_TOKEN_PREFIX::concat).orElse(null));

        if (jwtService.isAuthorizationHeaderValueNotAcceptable(authorizationHeaderValue)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            AuthenticatedUserInfo authenticatedUser = securityService.loadUserById(
                    Long.valueOf(jwtService.extractSubject(authorizationHeaderValue)),
                    TokenScope.valueOf(jwtService.extractClaim(authorizationHeaderValue, "scope")),
                    requestUri);
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
                    .authenticated(authenticatedUser, null, authenticatedUser.authorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            setupResponse(response, new ErrorResponse(ex.getMessage()), getErrorCode(ex));
        }
    }

    private String getCookie(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName())).findAny().map(Cookie::getValue))
                .orElse(null);
    }

    private void setupResponse(HttpServletResponse response, Object payload, int code) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }

    private Integer getErrorCode(Exception ex) {
        return ex instanceof TokenScopePermissionDeniedException ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return !path.contains("/secured/");
    }

}

package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.dto.*;
import com.kpi.fict.aura.auth.exception.EntityNotFoundException;
import com.kpi.fict.aura.auth.exception.InvalidTokenException;
import com.kpi.fict.aura.auth.mapper.SecurityMapper;
import com.kpi.fict.aura.auth.model.ConfirmToken;
import com.kpi.fict.aura.auth.model.Token;
import com.kpi.fict.aura.auth.repository.ConfirmTokenRepository;
import com.kpi.fict.aura.auth.repository.TokenRepository;
import com.kpi.fict.aura.auth.service.validator.SecurityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.AUTHORIZATION_TOKEN_PREFIX;
import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.RESET_PASSWORD_URI_TEMPLATE;

@Slf4j
@Service
public class SecurityService {

    private final Long sendCodeRate;
    private final String clientHost;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final UserService userService;
    private final SecurityMapper securityMapper;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityValidator securityValidator;
    private final ConfirmTokenRepository confirmTokenRepository;
    private final EmailNotificationService emailNotificationService;

    public SecurityService(JwtService jwtService, TotpService totpService,
            UserService userService, SecurityMapper securityMapper,
            TokenRepository tokenRepository, PasswordEncoder passwordEncoder,
            SecurityValidator securityValidator, ConfirmTokenRepository confirmTokenRepository,
            EmailNotificationService emailNotificationService, @Value("${application.host}") String host,
            @Value("${spring.profiles.active:local}") String profile, @Value("${application.security.send-code-rate}") Long sendCodeRate) {
        this.jwtService = jwtService;
        this.totpService = totpService;
        this.userService = userService;
        this.sendCodeRate = sendCodeRate;
        this.securityMapper = securityMapper;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityValidator = securityValidator;
        this.confirmTokenRepository = confirmTokenRepository;
        this.emailNotificationService = emailNotificationService;
        this.clientHost = !profile.equals("local") ? host : host.replace("8080", "3001");
    }

    @Transactional
    public AuthTokensResponse generateAuthTokens(Long userId) {
        return generateAuthTokens(userId, !userService.isUserEnabled(userId) ? TokenScope.CONFIRMATION_REQUIRED
                : (userService.isTwoFactorEnabled(userId) ? TokenScope.TWO_FACTOR_REQUIRED : TokenScope.FULL_ACCESS));
    }

    @Transactional
    public AuthTokensResponse generateAuthTokens(Long userId, TokenScope scope) {
        String token = jwtService.generateToken(userId, scope);
        String refreshToken = jwtService.generateRefreshToken(userId, scope);
        return securityMapper.toAuthTokensResponse(saveTokenData(userId, token, refreshToken, scope),
                getAuthDetails(userId, scope));
    }

    private AuthDetails getAuthDetails(Long userId, TokenScope scope) {
        if (scope == TokenScope.CONFIRMATION_REQUIRED) {
            new AuthDetails(scope, getUserConfirmationStatus(userId));
        }
        return new AuthDetails(scope);
    }

    public UserConfirmationStatusDto getUserConfirmationStatus(Long userId) {
        ConfirmToken confirmToken = confirmTokenRepository.findByCredentialsIdAndTokenType(
                userService.getUserCredentialsByUserId(userId).credentialsId(), ConfirmTokenType.SIGN_UP_CONFIRM)
                .stream().findFirst().orElse(null);
        if (confirmToken == null) {
            return new UserConfirmationStatusDto(UserConfirmationStatus.CODE_NOT_SENT);
        }
        if (confirmToken.getExpiredAt().isBefore(userService.getSystemDateTime())) {
            return new UserConfirmationStatusDto(UserConfirmationStatus.CODE_EXPIRED);
        }
        return new UserConfirmationStatusDto(UserConfirmationStatus.CODE_SENT, getResendCodeCoolDown(confirmToken));
    }

    public long getResendCodeCoolDown(ConfirmToken confirmToken) {
        return Math.max(sendCodeRate - Duration.between(confirmToken.getSentAt(),
                userService.getSystemDateTime()).getSeconds(), 0L);
    }

    private Token saveTokenData(Long userId, String accessToken, String refreshToken, TokenScope scope) {
        return tokenRepository.save(tokenRepository.findByUserIdAndScope(userId, scope)
                .map(token -> securityMapper.updateToken(token, accessToken, refreshToken))
                .orElseGet(() -> securityMapper.toToken(userId, accessToken, refreshToken, scope)));
    }

    @Transactional
    public AuthTokensResponse fromRefreshToken(String refreshToken) {
        Optional<Token> token = tokenRepository.findByRefreshToken(refreshToken);
        if (token.isEmpty() || jwtService.isTokenNotAcceptable(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String authorizationHeaderValue = AUTHORIZATION_TOKEN_PREFIX.concat(refreshToken);
        return generateAuthTokens(Long.valueOf(jwtService.extractSubject(authorizationHeaderValue)),
                TokenScope.valueOf(jwtService.extractClaim(authorizationHeaderValue, "scope")));
    }

    @Transactional
    public AuthTokensResponse saveUser(SignUpRequest signUpRequest) {
        securityValidator.validateUserIsNotRegistered(signUpRequest);
        UserSecurityDto savedUser = userService.saveUser(securityMapper.toSaveUserDto(signUpRequest, passwordEncoder));
        notifyUserConfirmation(userService.getUserCredentialsByUserId(savedUser.id()));
        return generateAuthTokens(savedUser.id());
    }

    public AuthenticatedUserInfo loadUserById(Long userId, TokenScope scope, String requestUri) throws Exception {
        UserSecurityDto userSecurityDto = userService.getUserSecurityInfoById(userId).orElseThrow(
                () -> new EntityNotFoundException("User", String.valueOf(userId)));
        securityValidator.validateTokenScopePermission(scope, requestUri);
        return securityMapper.toAuthenticatedUserInfo(userSecurityDto);
    }

    @Transactional
    public void notifyUserConfirmation(Long userId) {
        UserCredentialsDto user = userService.getUserCredentialsByUserId(userId);
        securityValidator.validateSendUserConfirmation(user, confirmTokenRepository.findByCredentialsIdAndTokenType(
                user.credentialsId(), ConfirmTokenType.SIGN_UP_CONFIRM).stream().findFirst().orElse(null));
        notifyUserConfirmation(user);
    }

    private void notifyUserConfirmation(UserCredentialsDto user) {
        if (!user.enabled()) {
            ConfirmToken confirmToken = getConfirmToken(user.credentialsId(), ConfirmTokenType.SIGN_UP_CONFIRM,
                    Duration.of(5L, ChronoUnit.MINUTES));
            Map<String, Object> notificationParams = new HashMap<>();
            notificationParams.put("username", user.username());
            notificationParams.put("code", confirmToken.getCode());
            emailNotificationService.sendMessage("confirm_registration", notificationParams,
                    "Confirm registration", new String[]{user.email()});
        }
    }

    @Transactional
    public void forgotPassword(String email) {
        userService.findByUsername(email).ifPresent(user -> {
            ConfirmToken confirmToken = getConfirmToken(user.credentialsId(), ConfirmTokenType.FORGOT_PASSWORD,
                    Duration.of(2L, ChronoUnit.HOURS));
            Map<String, Object> notificationParams = new HashMap<>();
            notificationParams.put("username", user.username());
            notificationParams.put("link", RESET_PASSWORD_URI_TEMPLATE.formatted(clientHost, confirmToken.getToken()));
            emailNotificationService.sendMessage("forgot_password", notificationParams,
                    "Reset password", new String[]{email});
        });
    }

    @Transactional
    public void resetPassword(String password, String token) {
        boolean isCode = false;
        ConfirmTokenType tokenType = ConfirmTokenType.FORGOT_PASSWORD;
        confirmTokenRepository.findByTokenAndTokenType(token, tokenType).ifPresentOrElse(
                confirmTokenAction(password, isCode),
                () -> { throw new InvalidTokenException(tokenType, isCode); });
    }

    @Transactional
    public AuthTokensResponse confirmRegistration(String code, Long currentUserId) {
        boolean isCode = true;
        ConfirmTokenType tokenType = ConfirmTokenType.SIGN_UP_CONFIRM;
        confirmTokenRepository.findByCredentialsIdAndCodeAndTokenType(userService.getUserCredentialsByUserId(
                currentUserId).credentialsId(), code, tokenType).ifPresentOrElse(
                        confirmTokenAction(null, isCode),
                        () -> { throw new InvalidTokenException(tokenType, isCode); });
        return generateAuthTokens(currentUserId, TokenScope.FULL_ACCESS);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, Long currentUserId) {
        userService.updateUserPassword(userService.getUserCredentialsByUserId(currentUserId).credentialsId(),
                passwordEncoder.encode(String.valueOf(request.newPassword())));
    }

    @Transactional
    public AuthTokensResponse verify2FA(String code, Long currentUserId) {
        securityValidator.validateVerify2FA(code, currentUserId);
        return generateAuthTokens(currentUserId, TokenScope.FULL_ACCESS);
    }

    @Transactional
    public TwoFactorSetupResponse init2FA(Long currentUserId) {
        securityValidator.validate2FADisabled(currentUserId);
        TwoFactorSetupResponse initResponse = totpService.generateSetupData(userService.getUsernameById(currentUserId));
        userService.setTwoFactorData(currentUserId, initResponse.secret(), initResponse.recoveryCodes());
        return initResponse;
    }

    @Transactional
    public void confirm2FA(String code, Long currentUserId) {
        securityValidator.validateConfirm2FA(code, currentUserId);
        userService.updateTwoFactor(currentUserId, true);
    }

    @Transactional
    public void disable2FA(Long currentUserId) {
        securityValidator.validate2FAEnabled(currentUserId);
        userService.updateTwoFactor(currentUserId, false);
    }

    @Transactional
    public List<String> generateRecoveryCodes(Long currentUserId) {
        securityValidator.validate2FAEnabled(currentUserId);
        List<String> recoveryCodes = totpService.generateRecoveryCodes();
        userService.setRecoveryCodes(currentUserId, recoveryCodes);
        return recoveryCodes;
    }

    private ConfirmToken getConfirmToken(Long credentialsId, ConfirmTokenType tokenType, Duration expiredIn) {
        LocalDateTime sentAt = userService.getSystemDateTime();
        LocalDateTime expiredAt = expiredIn != null ? sentAt.plusSeconds(expiredIn.toSeconds()) : null;
        return confirmTokenRepository.save(confirmTokenRepository.findByCredentialsIdAndTokenType(
                credentialsId, tokenType).map(token -> securityMapper.updateConfirmToken(token, token.getTokenType(), sentAt, expiredAt))
                .orElseGet(() -> securityMapper.toConfirmToken(credentialsId, tokenType, sentAt, expiredAt)));
    }

    private Consumer<ConfirmToken> confirmTokenAction(Object payload, boolean code) {
        return (confirmToken) -> {
            Long credentialsId = confirmToken.getCredentialsId();
            securityValidator.validateTokenExpired(confirmToken, code);
            switch (confirmToken.getTokenType()) {
                case SIGN_UP_CONFIRM -> userService.enableUser(credentialsId);
                case FORGOT_PASSWORD -> userService.updateUserPassword(credentialsId, passwordEncoder.encode((String) payload));
            }
            confirmTokenRepository.deleteById(confirmToken.getId());
        };
    }

}
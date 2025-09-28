package com.kpi.fict.aura.auth.service.validator;

import com.kpi.fict.aura.auth.dto.SignUpRequest;
import com.kpi.fict.aura.auth.dto.TokenScope;
import com.kpi.fict.aura.auth.dto.UserCredentialsDto;
import com.kpi.fict.aura.auth.exception.*;
import com.kpi.fict.aura.auth.model.ConfirmToken;
import com.kpi.fict.aura.auth.repository.ConfirmTokenRepository;
import com.kpi.fict.aura.auth.service.SecurityService;
import com.kpi.fict.aura.auth.service.TotpService;
import com.kpi.fict.aura.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityValidator {

    private final UserService userService;
    private final TotpService totpService;
    private final SecurityService securityService;
    private final ConfirmTokenRepository confirmTokenRepository;

    public void validateUserIsNotRegistered(SignUpRequest request) {
        if (userService.isUserRegistered(request.email())) {
            throw new UserRegisteredException();
        }
    }

    public void validateSendUserConfirmation(UserCredentialsDto user, ConfirmToken confirmToken) {
        if (user.enabled()) {
            throw new UserEnabledException();
        }
        if (confirmToken == null) return;
        long resendCodeCoolDown = securityService.getResendCodeCoolDown(confirmToken);
        if (resendCodeCoolDown > 0L) {
            throw new EmailResendCoolDownException(resendCodeCoolDown);
        }
    }

    public void validateTokenScopePermission(TokenScope scope, String requestUri) {
        if (scope != TokenScope.FULL_ACCESS && (requestUri == null || !requestUri.matches(scope.getUriExcludedPattern()))) {
            throw new TokenScopePermissionDeniedException(scope);
        }
    }

    @Transactional
    public void validateVerify2FA(String code, Long userId) {
        validate2FAEnabled(userId);
        String secret = userService.getTotpSecret(userId);
        if (secret == null) {
            throw new TwoFactorInitializationException();
        }
        if (!totpService.verifyCode(code, secret) && !userService.useRecoveryCode(userId, code)) {
            throw new TwoFactorVerificationFailedException();
        }
    }

    public void validateConfirm2FA(String code, Long userId) {
        validate2FADisabled(userId);
        String secret = userService.getTotpSecret(userId);
        if (secret == null) {
            throw new TwoFactorInitializationException();
        }
        if (!totpService.verifyCode(code, secret)) {
            throw new TwoFactorVerificationFailedException();
        }
    }

    public void validate2FADisabled(Long userId) {
        if (userService.isTwoFactorEnabled(userId)) {
            throw new TwoFactorAlreadyEnabledException();
        }
    }

    public void validate2FAEnabled(Long userId) {
        if (!userService.isTwoFactorEnabled(userId)) {
            throw new TwoFactorDisabledException();
        }
    }

    @Transactional
    public void validateTokenExpired(ConfirmToken confirmToken, boolean code) {
        LocalDateTime expiredAt = confirmToken.getExpiredAt();
        boolean isExpired = expiredAt != null && userService.getSystemDateTime().isAfter(expiredAt);
        if (isExpired) {
            confirmTokenRepository.delete(confirmToken);
            throw new TokenExpiredException(confirmToken.getTokenType(), code);
        }
    }

}

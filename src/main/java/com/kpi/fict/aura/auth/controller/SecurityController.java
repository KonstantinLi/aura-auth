package com.kpi.fict.aura.auth.controller;

import com.kpi.fict.aura.auth.dto.AuthTokensResponse;
import com.kpi.fict.aura.auth.dto.ConfirmChangePasswordRequest;
import com.kpi.fict.aura.auth.dto.SignUpRequest;
import com.kpi.fict.aura.auth.service.SecurityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping(path = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthTokensResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return securityService.saveUser(signUpRequest);
    }

    @GetMapping("/password-forgot")
    public void passwordForgot(@NotEmpty @Email @RequestParam String email) {
        securityService.forgotPassword(email);
    }

    @PostMapping("/password-forgot")
    public void passwordForgotConfirm(@Valid @RequestBody ConfirmChangePasswordRequest confirmChangePasswordRequest,
            @RequestParam String token) {
        securityService.resetPassword(confirmChangePasswordRequest.password(), token);
    }

    @GetMapping(path = "/auth/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthTokensResponse refreshToken(@RequestParam String refreshToken) {
        return securityService.fromRefreshToken(refreshToken);
    }

}
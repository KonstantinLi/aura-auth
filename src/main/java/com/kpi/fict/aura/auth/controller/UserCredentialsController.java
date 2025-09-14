package com.kpi.fict.aura.auth.controller;

import com.kpi.fict.aura.auth.dto.*;
import com.kpi.fict.aura.auth.model.DeviceStatus;
import com.kpi.fict.aura.auth.service.SecurityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/secured")
public class UserCredentialsController {

    private final SecurityService securityService;

    @GetMapping("/confirm-registration")
    public void notifyUserConfirmation(@AuthenticationPrincipal AuthenticatedUserInfo authenticatedUserInfo) {
        securityService.notifyUserConfirmation(authenticatedUserInfo.id());
    }

    @PostMapping(path = "/confirm-registration", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthTokensResponse confirmRegistration(@RequestParam String code,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUserInfo) {
        return securityService.confirmRegistration(code, authenticatedUserInfo.id());
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        securityService.resetPassword(request, authenticatedUser.id());
    }

    @PostMapping(path = "/2fa/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthTokensResponse verify2FA(@RequestParam String code,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        return securityService.verify2FA(code, authenticatedUser.id());
    }

    @PostMapping(path = "/2fa/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    public TwoFactorSetupResponse init2FA(@AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        return securityService.init2FA(authenticatedUser.id());
    }

    @PostMapping(path = "/2fa/enable/confirm")
    public void confirm2FA(@RequestParam String code, @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        securityService.confirm2FA(code, authenticatedUser.id());
    }

    @DeleteMapping("/2fa/disable")
    public void disable2FA(@AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        securityService.disable2FA(authenticatedUser.id());
    }

    @PostMapping(path = "/2fa/recovery-codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> generateRecoveryCodes(@AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        return securityService.generateRecoveryCodes(authenticatedUser.id());
    }

    @GetMapping(path = "/devices", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DeviceResponse> getDevices(@AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        return securityService.getAllDevices(authenticatedUser.id());
    }

    @GetMapping(path = "/devices/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeviceResponse getDevice(@Positive @PathVariable Long deviceId,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        return securityService.getDevice(deviceId, authenticatedUser.id());
    }

    @PutMapping("/devices/{deviceId}")
    public void updateDeviceStatus(@Positive @PathVariable Long deviceId, @RequestParam DeviceStatus status,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        securityService.updateDeviceStatus(deviceId, status, authenticatedUser.id());
    }

    @DeleteMapping("/devices/{deviceId}")
    public void deleteDevice(@Positive @PathVariable Long deviceId,
            @AuthenticationPrincipal AuthenticatedUserInfo authenticatedUser) {
        securityService.deleteDevice(deviceId, authenticatedUser.id());
    }

}
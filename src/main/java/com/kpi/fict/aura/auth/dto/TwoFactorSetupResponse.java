package com.kpi.fict.aura.auth.dto;

import java.util.List;

public record TwoFactorSetupResponse(
        String secret,
        String qrCodeUri,
        List<String> recoveryCodes) {}
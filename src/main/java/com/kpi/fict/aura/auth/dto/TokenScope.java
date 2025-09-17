package com.kpi.fict.aura.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenScope {

    FULL_ACCESS(null),
    TWO_FACTOR_REQUIRED("^/api/secured/2fa/verify"),
    CONFIRMATION_REQUIRED("^/api/secured/confirm-registration");

    private final String uriExcludedPattern;

}
package com.kpi.fict.aura.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfirmTokenType {

    SIGN_UP_CONFIRM(true),
    FORGOT_PASSWORD(false);

    private final boolean requiresCode;

}
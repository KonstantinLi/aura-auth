package com.kpi.fict.aura.auth.exception;

import com.kpi.fict.aura.auth.dto.ConfirmTokenType;

public class InvalidTokenException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Invalid %s %s";

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(ConfirmTokenType tokenType, boolean code) {
        super(MESSAGE_TEMPLATE.formatted(tokenType, code ? "code" : "token"));
    }

}

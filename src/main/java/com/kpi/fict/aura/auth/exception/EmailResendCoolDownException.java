package com.kpi.fict.aura.auth.exception;

public class EmailResendCoolDownException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Please wait %d seconds before resending verification email";

    public EmailResendCoolDownException(long seconds) {
        super(MESSAGE_TEMPLATE.formatted(seconds));
    }

}
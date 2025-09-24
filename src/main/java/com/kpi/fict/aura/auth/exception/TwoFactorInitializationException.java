package com.kpi.fict.aura.auth.exception;

public class TwoFactorInitializationException extends RuntimeException {

    public TwoFactorInitializationException() {
        super("2FA setup data is not initialized yet");
    }

}

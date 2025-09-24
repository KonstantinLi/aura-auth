package com.kpi.fict.aura.auth.exception;

public class TwoFactorVerificationFailedException extends RuntimeException {

    public TwoFactorVerificationFailedException() {
        super("2FA verification failed");
    }

}

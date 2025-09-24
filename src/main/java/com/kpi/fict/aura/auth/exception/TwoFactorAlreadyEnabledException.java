package com.kpi.fict.aura.auth.exception;

public class TwoFactorAlreadyEnabledException extends RuntimeException {

    public TwoFactorAlreadyEnabledException() {
        super("2FA is already enabled");
    }

}

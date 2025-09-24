package com.kpi.fict.aura.auth.exception;

public class TwoFactorDisabledException extends RuntimeException {

    public TwoFactorDisabledException() {
        super("2FA is disabled");
    }

}

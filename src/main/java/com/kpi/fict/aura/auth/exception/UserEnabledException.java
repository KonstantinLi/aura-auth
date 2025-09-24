package com.kpi.fict.aura.auth.exception;

public class UserEnabledException extends RuntimeException {

    public UserEnabledException() {
        super("This user is already enabled");
    }

}

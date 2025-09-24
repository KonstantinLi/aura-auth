package com.kpi.fict.aura.auth.exception;

public class UserRegisteredException extends RuntimeException {

    public UserRegisteredException() {
        super("This user is already registered");
    }

}

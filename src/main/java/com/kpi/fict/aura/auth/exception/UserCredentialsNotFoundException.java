package com.kpi.fict.aura.auth.exception;

public class UserCredentialsNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Credentials of a user '%s' not found";

    public UserCredentialsNotFoundException(String username) {
        super(MESSAGE_TEMPLATE.formatted(username));
    }
}

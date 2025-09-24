package com.kpi.fict.aura.auth.exception;

public class ExternalUserProcessorNotFoundException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "External user processor for issuer %s not found.";

    public ExternalUserProcessorNotFoundException(String issuer) {
        super(MESSAGE_TEMPLATE.formatted(issuer));
    }

}

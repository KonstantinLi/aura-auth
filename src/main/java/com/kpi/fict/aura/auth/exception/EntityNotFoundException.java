package com.kpi.fict.aura.auth.exception;

public class EntityNotFoundException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "%s with id %s not found";

    public EntityNotFoundException(String entityName, String entityId) {
        super(MESSAGE_TEMPLATE.formatted(entityName, entityId));
    }

}

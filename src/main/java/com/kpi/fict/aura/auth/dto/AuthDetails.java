package com.kpi.fict.aura.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDetails {

    private final TokenScope scope;

    private UserConfirmationStatusDto confirmationStatus;

    public AuthDetails(TokenScope scope) {
        this.scope = scope;
    }

    public AuthDetails(TokenScope scope, UserConfirmationStatusDto confirmationStatus) {
        this.scope = scope;
        this.confirmationStatus = confirmationStatus;
    }

}
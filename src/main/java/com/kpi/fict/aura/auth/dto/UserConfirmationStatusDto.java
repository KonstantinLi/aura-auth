package com.kpi.fict.aura.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserConfirmationStatusDto {

    private final UserConfirmationStatus status;

    private Long resendId;

    public UserConfirmationStatusDto(UserConfirmationStatus status) {
        this.status = status;
    }

    public UserConfirmationStatusDto(UserConfirmationStatus status, Long resendId) {
        this.status = status;
        this.resendId = resendId;
    }

}
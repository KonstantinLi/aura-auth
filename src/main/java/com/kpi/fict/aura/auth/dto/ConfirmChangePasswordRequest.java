package com.kpi.fict.aura.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kpi.fict.aura.auth.controller.validation.FirstOrder;
import com.kpi.fict.aura.auth.controller.validation.NotEmail;
import com.kpi.fict.aura.auth.controller.validation.ValidPassword;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@GroupSequence({ FirstOrder.class, ConfirmChangePasswordRequest.class })
public record ConfirmChangePasswordRequest(
        @NotNull(groups = FirstOrder.class) @ValidPassword @NotEmail
        String password,
        @NotNull(groups = FirstOrder.class)
        String confirmPassword) {

        @JsonIgnore
        @AssertTrue(message = "Password and confirmPassword must be equals.")
        public boolean isPasswordEqualsToConfirmPassword() {
                return Objects.equals(password, confirmPassword);
        }

}
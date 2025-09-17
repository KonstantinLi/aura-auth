package com.kpi.fict.aura.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kpi.fict.aura.auth.controller.validation.*;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;
import java.util.Optional;

@GroupSequence({ FirstOrder.class, SecondOrder.class, SignUpRequest.class })
public record SignUpRequest(
        @NotNull(groups = FirstOrder.class) @Pattern(regexp = NAME_RESTRICTION_REGEXP, message = NAME_RESTRICTION_MESSAGE)
        String firstName,
        @NotNull(groups = FirstOrder.class) @Pattern(regexp = NAME_RESTRICTION_REGEXP, message = NAME_RESTRICTION_MESSAGE)
        String lastName,
        @NotNull(groups = FirstOrder.class) @ValidEmail
        String email,
        @NotNull(groups = SecondOrder.class) @ValidPassword @NotEmail
        String password,
        @NotNull
        String confirmPassword,
        String token,
        SignUpMethod method) {

        private static final String NAME_RESTRICTION_REGEXP = "^[^\\d~!@#$%^&*()_+=:;?/.,<>{}|\"\\\\\\[\\]]{2,}$";
        private static final String NAME_RESTRICTION_MESSAGE = "Value must be at least 2 characters long "
                + "and should not contain digits or following characters: ~!@#$%^&*()_+=:;?/.,<>{}|\"\\[]";

        @Override
        public SignUpMethod method() {
                return Optional.ofNullable(method).orElse(SignUpMethod.INTERNAL);
        }

        @JsonIgnore
        @AssertTrue(message = "Password and confirmPassword must be equals.")
        public boolean isPasswordEqualsToConfirmPassword() {
                return Objects.equals(password, confirmPassword);
        }

}
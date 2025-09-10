package com.kpi.fict.aura.auth.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.ConstraintComposition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

@ConstraintComposition(ALL_FALSE)
@Email
@Target({ FIELD, METHOD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface NotEmail {

    String message() default "Value must not be valid email.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
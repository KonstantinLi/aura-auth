package com.kpi.fict.aura.auth.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        PasswordValidator passwordValidator = getPasswordValidator();
        RuleResult ruleResult = passwordValidator.validate(new PasswordData(value));
        if (ruleResult.isValid()) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(passwordValidator.getMessages(ruleResult).stream().findFirst()
                .orElse(context.getDefaultConstraintMessageTemplate())).addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }

    private PasswordValidator getPasswordValidator() {
        return new PasswordValidator(
                new LengthRule(12, 50),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false, false),
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false, false),
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false, false),
                new RepeatCharacterRegexRule(5,false),
                new WhitespaceRule(MatchBehavior.Contains, false));
    }

}
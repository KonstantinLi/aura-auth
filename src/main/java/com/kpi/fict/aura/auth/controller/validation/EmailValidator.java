package com.kpi.fict.aura.auth.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@([A-Z0-9.-]+)\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return buildViolation(context, "Email address is empty");
        }
        if (!EMAIL_REGEX.matcher(value).matches()) {
            return buildViolation(context, "Email format is invalid");
        }
        String domain = value.substring(value.indexOf('@') + 1);
        if (!hasMXRecord(domain)) {
            return buildViolation(context, "The domain '%s' doesn't exist".formatted(domain));
        }
        return true;
    }

    public boolean hasMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext context = new InitialDirContext(env);
            Attributes attrs = context.getAttributes(domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

}

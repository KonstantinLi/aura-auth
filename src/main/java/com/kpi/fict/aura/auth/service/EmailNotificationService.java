package com.kpi.fict.aura.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendMessage(String templateName, Map<String, Object> params, String subject, String[] emails) {
        try {
            var context = new Context();
            context.setVariables(params);
            String content = templateEngine.process(templateName, context);
            sendEmail(content, subject, emails);
        } catch (Exception ex) {
            log.error("The email was not sent. Error message: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void sendEmail(String content, String subject, String... recipients) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
        log.debug("Email sent to %s".formatted(String.join(", ", recipients)));
    }

}
package com.kpi.fict.aura.auth.handler.processor;

import com.kpi.fict.aura.auth.dto.OAuth2UserDto;
import com.kpi.fict.aura.auth.dto.SignUpMethod;
import com.kpi.fict.aura.auth.handler.processor.ExternalUserProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleExternalUserProcessor implements ExternalUserProcessor {

    private static final String GOOGLE_ISSUER_IDENTIFIER = "accounts.google.com";
    private static final String GOOGLE_AUTH_URI = "api/oauth2/authorization/google?prompt=select_account";

    @Value("${application.host}")
    private String host;

    @Override
    public boolean canProcess(OidcUser externalUser) {
        return Objects.toString(externalUser.getIssuer()).contains(GOOGLE_ISSUER_IDENTIFIER);
    }

    @Override
    public OAuth2UserDto extractExternalUser(OidcUser externalUser, Authentication authentication) {
        return new OAuth2UserDto(externalUser.getGivenName(), externalUser.getFamilyName(),
                externalUser.getEmail(), imageUrlToBytes((String) externalUser.getAttributes().get("picture")),
                SignUpMethod.GOOGLE);
    }

    private byte[] imageUrlToBytes(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        } catch (Exception ex) {
            log.error("Google fetch photo error: {}", ex.getMessage());
            return null;
        }
    }

}
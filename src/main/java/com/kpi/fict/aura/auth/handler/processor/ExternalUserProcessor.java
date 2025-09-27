package com.kpi.fict.aura.auth.handler.processor;

import com.kpi.fict.aura.auth.dto.OAuth2UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface ExternalUserProcessor {

    boolean canProcess(OidcUser externalUser);

    OAuth2UserDto extractExternalUser(OidcUser externalUser, Authentication authentication);

}
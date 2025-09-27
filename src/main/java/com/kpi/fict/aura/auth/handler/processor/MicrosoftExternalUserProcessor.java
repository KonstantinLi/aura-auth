package com.kpi.fict.aura.auth.handler.processor;

import com.kpi.fict.aura.auth.dto.OAuth2UserDto;
import com.kpi.fict.aura.auth.dto.SignUpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MicrosoftExternalUserProcessor implements ExternalUserProcessor {

    private static final String MICROSOFT_AUTH_URI = "/oauth2/authorization/azure";
    private static final String MICROSOFT_ISSUER_URL = "https://login.microsoftonline.com";
    private static final String MICROSOFT_REQUEST_PHOTO_URL = "https://graph.microsoft.com/v1.0/me/photo/$value";

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService clientService;

    @Override
    public boolean canProcess(OidcUser externalUser) {
        return Objects.toString(externalUser.getIssuer()).startsWith(MICROSOFT_ISSUER_URL);
    }

    @Override
    public OAuth2UserDto extractExternalUser(OidcUser externalUser, Authentication authentication) {
        Map<String, Object> attributes = externalUser.getAttributes();
        return new OAuth2UserDto(
                (String) attributes.getOrDefault("given_name", attributes.get("givenname")),
                (String) attributes.getOrDefault("family_name", attributes.get("familyname")),
                (String) attributes.getOrDefault("templates", attributes.get("userprincipalname")),
                fetchUserLogo(authentication, (String) attributes.get("picture")), SignUpMethod.MICROSOFT);
    }

    private byte[] fetchUserLogo(Authentication authentication, String requestPhotoUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken(authentication));
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(Optional.ofNullable(requestPhotoUrl)
                    .orElse(MICROSOFT_REQUEST_PHOTO_URL), HttpMethod.GET, requestEntity, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (Exception ex) {
            log.error("Microsoft fetch photo error: {}", ex.getMessage());
            return null;
        }
        return null;
    }

    private String getAccessToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    token.getAuthorizedClientRegistrationId(), token.getName());
            return client != null && client.getAccessToken() != null ? client.getAccessToken().getTokenValue() : null;
        }
        return null;
    }

}

package com.kpi.fict.aura.auth.mapper;

import com.kpi.fict.aura.auth.dto.*;
import com.kpi.fict.aura.auth.model.AuthenticatedUser;
import com.kpi.fict.aura.auth.model.ConfirmToken;
import com.kpi.fict.aura.auth.model.Token;
import org.mapstruct.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MappingConfig.class )
public interface SecurityMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", source = "signUpRequest.email")
    @Mapping(target = "enabled", expression = "java(signUpRequest.method() != SignUpMethod.INTERNAL")
    SaveUserDto toSaveUserDto(SignUpRequest signUpRequest, @Context PasswordEncoder passwordEncoder);

    @AfterMapping
    default void encodePassword(@MappingTarget SaveUserDto dto, SignUpRequest request, @Context PasswordEncoder passwordEncoder) {
        if (request.password() != null) {
            dto.setPassword(passwordEncoder.encode(request.password()));
        }
    }

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "privateToken", ignore = true)
    @Mapping(target = "confirmPassword", ignore = true)
    SignUpRequest toExternalSignUpRequest(OAuth2UserDto dto, String token);

    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "username", source = "templates")
    @Mapping(target = "authorities", source = "roles")
    AuthenticatedUser toAuthenticatedUser(UserSecurityDto userSecurityDto);

    Token toToken(Long userId, String token, String refreshToken, TokenScope scope);

    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Token updateToken(@MappingTarget Token target, String token, String refreshToken);

    @Mapping(target = "tokenType", source = "tokenType")
    @Mapping(target = "code", source = "tokenType", qualifiedByName = "generateCode")
    @Mapping(target = "token", source = "tokenType", qualifiedByName = "generateToken")
    ConfirmToken toConfirmToken(Long credentialsId, ConfirmTokenType tokenType,
            LocalDateTime sentAt, LocalDateTime expiredAt);

    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "credentialsId", ignore = true)
    @Mapping(target = "code", source = "tokenType", qualifiedByName = "generateCode")
    @Mapping(target = "token", source = "tokenType", qualifiedByName = "generateToken")
    ConfirmToken updateConfirmToken(@MappingTarget ConfirmToken confirmToken,
            ConfirmTokenType tokenType, LocalDateTime sentAt, LocalDateTime expiredAt);

    AuthTokensResponse toAuthTokensResponse(Token token, AuthDetails details);

    @Mapping(target = "authorities", source = "roles")
    AuthenticatedUserInfo toAuthenticatedUserInfo(UserSecurityDto userSecurityDto);

    default Collection<? extends GrantedAuthority> rolesToAuthorities(Set<RoleDto> roleDtos) {
        List<SimpleGrantedAuthority> roles = roleDtos.stream()
                .map(roleDto -> new SimpleGrantedAuthority(roleDto.name()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<SimpleGrantedAuthority> permissions = roleDtos.stream()
                .flatMap(roleDto -> roleDto.permissions().stream())
                .map(SimpleGrantedAuthority::new)
                .toList();
        roles.addAll(permissions);
        return roles;
    }

}

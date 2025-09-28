package com.kpi.fict.aura.auth.mapper;

import com.kpi.fict.aura.auth.dto.*;
import com.kpi.fict.aura.auth.model.AuthenticatedUser;
import com.kpi.fict.aura.auth.model.Credentials;
import com.kpi.fict.aura.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(config = MappingConfig.class)
public interface UserMapper {

    @Mapping(target = "username", source = "templates")
    AuthenticatedUser toAuthenticatedUser(UserSecurityDto userSecurityDto);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "templates", source = "username")
    @Mapping(target = "credentialsId", source = "id")
    @Mapping(target = "enabled", source = "user.enabled")
    @Mapping(target = "username", expression = "java(credentials.getUser().toString())")
    UserSecurityDto toUserSecurityDto(Credentials credentials);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "credentialsId", ignore = true)
    UserSecurityDto toUserSecurityDto(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "confirmPassword", ignore = true)
    SignUpRequest toExternalSignUpRequest(OAuth2UserDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "templates", source = "user.email")
    @Mapping(target = "username", expression = "java(user.toString())")
    UserCredentialsDto toUserCredentialsDto(User user, Long credentialsId);

    @Mapping(target = "username", expression = "java(user.toString())")
    UsernameResponse toUsernameResponse(User user);

    @Mapping(target = "totpSecret", ignore = true)
    @Mapping(target = "recoveryCodes", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    @Mapping(target = "registered", constant = "true")
    User toRegisteredUser(SaveUserDto saveUserDto, LocalDateTime registeredAt);

    User updateRegisteredUser(@MappingTarget User user, SaveUserDto saveUserDto, LocalDateTime registeredAt);

    @Mapping(target = "user", ignore = true)
    Credentials toCredentials(SaveUserDto saveUserDto);

    @Mapping(target = "user", ignore = true)
    Credentials updateCredentials(@MappingTarget Credentials credentials, SaveUserDto saveUserDto);

}
package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.dto.SaveUserDto;
import com.kpi.fict.aura.auth.dto.UserCredentialsDto;
import com.kpi.fict.aura.auth.dto.UserSecurityDto;
import com.kpi.fict.aura.auth.dto.UsernameResponse;
import com.kpi.fict.aura.auth.exception.EntityNotFoundException;
import com.kpi.fict.aura.auth.exception.UserCredentialsNotFoundException;
import com.kpi.fict.aura.auth.mapper.UserMapper;
import com.kpi.fict.aura.auth.model.Credentials;
import com.kpi.fict.aura.auth.model.User;
import com.kpi.fict.aura.auth.repository.CredentialsRepository;
import com.kpi.fict.aura.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;

    @Value("${server.time-zone}")
    private String systemTimeZone;

    @Transactional(readOnly = true)
    public boolean isUserRegistered(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isUserEnabled(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().isEnabled();
    }

    @Transactional(readOnly = true)
    public boolean isTwoFactorEnabled(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().isTwoFactorEnabled();
    }

    @Transactional(readOnly = true)
    public Optional<UserSecurityDto> findByUsername(String username) {
        Optional<Credentials> optionalCredentials = credentialsRepository.findWithUserAndRolesByUsername(username);
        return optionalCredentials.map(userMapper::toUserSecurityDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserSecurityDto> getUserSecurityInfoById(Long id) {
        return userRepository.findWithRolesById(id).map(userMapper::toUserSecurityDto);
    }

    @Transactional(readOnly = true)
    public UserCredentialsDto getUserCredentialsByUserId(Long userId) {
        User user = userRepository.findByIdRequired(userId);
        return userMapper.toUserCredentialsDto(user, findCredentialsByUser(user).getId());
    }

    private Credentials findCredentialsByUser(User user) {
        return credentialsRepository.findByUser(user).orElseThrow(() ->
                new UserCredentialsNotFoundException(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public UsernameResponse getUsernameById(Long userId) {
        return userMapper.toUsernameResponse(userRepository.findByIdRequired(userId));
    }

    @Transactional(readOnly = true)
    public String getTotpSecret(Long userId) {
        return userRepository.findByIdRequired(userId).getTotpSecret();
    }

    @Transactional
    public UserSecurityDto saveUser(SaveUserDto saveUserDto) {
        LocalDateTime dateTime = getSystemDateTime();
        User user = userRepository.save(userRepository.findByEmail(saveUserDto.getEmail())
                .map(unregisteredUser -> userMapper.updateRegisteredUser(unregisteredUser, saveUserDto, dateTime))
                .orElseGet(() -> userMapper.toRegisteredUser(saveUserDto, dateTime)));
        Credentials credentials = credentialsRepository.findWithUserAndRolesByUsername(saveUserDto.getUsername())
                .map(emptyCredentials -> userMapper.updateCredentials(emptyCredentials, saveUserDto))
                .orElseGet(() -> userMapper.toCredentials(saveUserDto));
        credentials.setUser(user);
        return userMapper.toUserSecurityDto(credentialsRepository.save(credentials));
    }

    @Transactional
    public void updateUserPassword(Long credentialsId, String password) {
        credentialsRepository.findById(credentialsId).ifPresent(credentials -> credentials.setPassword(password));
    }

    @Transactional
    public void enableUser(Long credentialsId) {
        findCredentialsWithUserById(credentialsId).getUser().setEnabled(true);
    }

    private Credentials findCredentialsWithUserById(Long credentialsId) {
        return credentialsRepository.findWithUserById(credentialsId).orElseThrow(() ->
                new EntityNotFoundException(credentialsRepository.getEntityName(), String.valueOf(credentialsId)));
    }

    @Transactional
    public void updateTwoFactor(Long userId, boolean twoFactorEnabled) {
        User user = userRepository.findByIdRequired(userId);
        user.setTwoFactorEnabled(twoFactorEnabled);
        if (!twoFactorEnabled) {
            user.setTotpSecret(null);
            user.setRecoveryCodes(null);
        }
    }

    public LocalDateTime getSystemDateTime() {
        return LocalDateTime.now(ZoneId.of(systemTimeZone));
    }

    @Transactional
    public void setTwoFactorData(Long userId, String totpSecret, List<String> recoveryCodes) {
        User user = userRepository.findByIdRequired(userId);
        user.setTotpSecret(totpSecret);
        user.setRecoveryCodes(String.join(";", recoveryCodes));
    }

    @Transactional
    public void setRecoveryCodes(Long userId, List<String> recoveryCodes) {
        User user = userRepository.findByIdRequired(userId);
        user.setRecoveryCodes(String.join(";", recoveryCodes));
    }

    @Transactional
    public boolean useRecoveryCode(Long userId, String recoveryCode) {
        User user = userRepository.findByIdRequired(userId);
        Set<String> recoveryCodes = new HashSet<>(Optional.ofNullable(user.getRecoveryCodes())
                .map(codes -> Arrays.asList(codes.split(";"))).orElse(Collections.emptyList()));
        if (recoveryCodes.isEmpty() || !recoveryCodes.remove(recoveryCode)) return false;
        user.setRecoveryCodes(!recoveryCodes.isEmpty() ? String.join(";", recoveryCodes) : null);
        return true;
    }

}
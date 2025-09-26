package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.mapper.SecurityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String USERNAME_NOT_FOUND_MESSAGE_TEMPLATE = "User with username '%s' not found";

    private final UserService userService;
    private final SecurityMapper securityMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByUsername(username)
                .map(securityMapper::toAuthenticatedUser)
                .orElseThrow(() -> new UsernameNotFoundException(USERNAME_NOT_FOUND_MESSAGE_TEMPLATE.formatted(username)));
    }

}
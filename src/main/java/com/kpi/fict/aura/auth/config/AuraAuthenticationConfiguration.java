package com.kpi.fict.aura.auth.config;

import com.kpi.fict.aura.auth.filter.JwtAuthFilter;
import com.kpi.fict.aura.auth.handler.ClearTokensLogoutHandler;
import com.kpi.fict.aura.auth.handler.OAuth2AuthenticationHandler;
import com.kpi.fict.aura.auth.handler.UsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EntityScan(basePackages = {AuraAuthenticationConfiguration.AUTHENTICATION_BASE_PACKAGE})
@ComponentScan(basePackages = {AuraAuthenticationConfiguration.AUTHENTICATION_BASE_PACKAGE})
@EnableJpaRepositories(basePackages = {AuraAuthenticationConfiguration.AUTHENTICATION_BASE_PACKAGE})
@ConfigurationPropertiesScan(basePackages = {AuraAuthenticationConfiguration.AUTHENTICATION_BASE_PACKAGE})
public class AuraAuthenticationConfiguration implements WebMvcConfigurer {

    public static final String AUTHORIZATION_TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHENTICATION_BASE_PACKAGE = "com.kpi.fict.aura.auth";

    public static final String RESET_PASSWORD_URI_TEMPLATE = "%sen/authentication/reset-password?token=%s";

    public static final String ALPHA_NUMERIC_STRING_CAPS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXY";

    private static final String[] PERMIT_ALL_PATHS = {
            "/sign-up",
            "/password-forgot",
            "/auth/refresh-token"
    };

    private final String host;
    private final String profile;

    public AuraAuthenticationConfiguration(
            @Value("${application.host}") String host,
            @Value("${spring.profiles.active:local}") String profile) {
        this.host = host;
        this.profile = profile;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = new ArrayList<>(List.of(host));
        if (!profile.equals("prod")) allowedOrigins.add("http://localhost:5173");
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(900);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
            ClearTokensLogoutHandler logoutHandler, OAuth2AuthenticationHandler oAuth2AuthenticationHandler,
            UserDetailsService userDetailsService, UsernamePasswordAuthenticationHandler usernamePasswordAuthenticationHandler) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .addFilterBefore(jwtAuthFilter, LogoutFilter.class)
                .authorizeHttpRequests(requestMatcherRegistry -> requestMatcherRegistry
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oAuth2LoginConfigurer -> oAuth2LoginConfigurer
                        .successHandler(oAuth2AuthenticationHandler)
                        .failureHandler(oAuth2AuthenticationHandler))
                .formLogin(formLoginConfigurer -> formLoginConfigurer
                        .loginProcessingUrl("/auth/login")
                        .successHandler(usernamePasswordAuthenticationHandler)
                        .failureHandler(usernamePasswordAuthenticationHandler))
                .userDetailsService(userDetailsService)
                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutRequestMatcher(new AndRequestMatcher(
                                new RequestHeaderRequestMatcher(AUTHORIZATION_HEADER_NAME),
                                new AntPathRequestMatcher("/secured/auth/logout", HttpMethod.POST.name())
                        ))
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(logoutHandler));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
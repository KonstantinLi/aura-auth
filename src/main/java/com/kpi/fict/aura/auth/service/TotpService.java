package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.dto.TwoFactorSetupResponse;
import com.kpi.fict.aura.auth.dto.UsernameResponse;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.IntStream;

import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.ALPHA_NUMERIC_STRING_CAPS;

@Service
public class TotpService {

    private static final String OTP_AUTH_URL_TEMPLATE = "otpauth://totp/%s?secret=%s&issuer=%s";

    private final String appName;
    private final SecureRandom random;

    public TotpService(@Value("${application.name}") String appName) {
        this.appName = appName;
        this.random = new SecureRandom();
    }

    public boolean verifyCode(String code, String secret) {
        return new Totp(secret).verify(code);
    }

    public TwoFactorSetupResponse generateSetupData(UsernameResponse user) {
        String secret = generateSecret();
        return new TwoFactorSetupResponse(secret, buildOtpAuthUrl(user.email(), secret), generateRecoveryCodes());
    }

    public List<String> generateRecoveryCodes() {
        return IntStream.range(0, 5).mapToObj(i -> generateRecoveryCode(4, 6, "-")).toList();
    }

    private String generateRecoveryCode(int groups, int groupSize, String delimiter) {
        StringBuilder sb = new StringBuilder(groups * groupSize + (groups - 1));
        for (int g = 0; g < groups; g++) {
            if (g > 0) sb.append(delimiter);
            for (int j = 0; j < groupSize; j++) {
                sb.append(ALPHA_NUMERIC_STRING_CAPS.charAt(random.nextInt(ALPHA_NUMERIC_STRING_CAPS.length())));
            }
        }
        return sb.toString();
    }

    private String generateSecret() {
        byte[] buffer = new byte[20];
        random.nextBytes(buffer);
        return new Base32().encodeToString(buffer).replace("=", "");
    }

    private String buildOtpAuthUrl(String username, String secret) {
        return OTP_AUTH_URL_TEMPLATE.formatted(username, secret, appName);
    }

}

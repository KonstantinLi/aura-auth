package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${application.security.encryption-algorithm}")
    private String algorithm;

    @Value("${application.security.encryption-secret-key}")
    private SecretKey secretKey;

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception ex) {
            throw new EncryptionException(ex.getMessage());
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception ex) {
            throw new EncryptionException(ex.getMessage());
        }
    }

}
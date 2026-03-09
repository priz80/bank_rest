package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CardUtil {

    private static final String ALGORITHM = "AES";
    private SecretKey key;

    public CardUtil(@Value("${jwt.secret}") String secret) {
        // Используем часть JWT-секрета для AES (256 бит)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] truncatedKey = new byte[32]; // 256 bit
        System.arraycopy(keyBytes, 0, truncatedKey, 0, truncatedKey.length);
        this.key = new SecretKeySpec(truncatedKey, ALGORITHM);
    }

    public String encrypt(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования номера карты", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка дешифрования номера карты", e);
        }
    }

    public String mask(String encryptedCardNumber) {
        String cardNumber = decrypt(encryptedCardNumber);
        return "**** **** **** " + cardNumber.substring(15);
    }
}
package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

/**
 * Утилита для генерации данных банковской карты.
 * Генерирует номер, CVV и срок действия.
 */
@Component
public class CardGenerator {

    private static final Random random = new Random();

    /**
     * Генерирует 16-значный номер карты
     */
    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Генерирует 3-значный CVV
     */
    public String generateCvv() {
        return String.format("%03d", random.nextInt(1000));
    }

    /**
     * Генерирует дату окончания (через 4 года, 1-е число месяца)
     */
    public LocalDate calculateExpiryDate() {
        return LocalDate.now().plusYears(4).withDayOfMonth(1);
    }
}
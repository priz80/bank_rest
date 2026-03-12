package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
public class CardGenerator {

    private static final Random random = new Random();

    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public String generateCvv() {
        return String.format("%03d", random.nextInt(1000));
    }

    public LocalDate calculateExpiryDate() {
        return LocalDate.now().plusYears(5).withDayOfMonth(1);
    }
}
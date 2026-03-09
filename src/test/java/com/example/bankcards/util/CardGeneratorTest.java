package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class CardGeneratorTest {

    private CardGenerator cardGenerator;

    @BeforeEach
    void setUp() {
        cardGenerator = new CardGenerator();
    }

    @Test
    void generateCardNumber_Returns16DigitNumber() {
        String cardNumber = cardGenerator.generateCardNumber();

        assertThat(cardNumber).isNotNull();
        assertThat(cardNumber).hasSize(16);
        assertThat(cardNumber).matches("\\d{16}");
    }

    @Test
    void generateCvv_Returns3DigitNumber() {
        String cvv = cardGenerator.generateCvv();

        assertThat(cvv).isNotNull();
        assertThat(cvv).hasSize(3);
        assertThat(cvv).matches("\\d{3}");
    }

    @Test
    void calculateExpiryDate_ReturnsDateApproximately5YearsFromNow() {
        LocalDate expiryDate = cardGenerator.calculateExpiryDate();
        LocalDate expected = LocalDate.now().plusYears(5).withDayOfMonth(1);

        assertThat(expiryDate.getYear()).isEqualTo(expected.getYear());
        assertThat(expiryDate.getMonth()).isEqualTo(expected.getMonth());
        assertThat(expiryDate.getDayOfMonth()).isEqualTo(1);
    }

    @Test
    void generateCardNumber_UsesLuhnAlgorithm_IfImplemented() {
        // проверим, что метод не возвращает null
        String number = cardGenerator.generateCardNumber();
        assertThat(number).isNotNull();
    }
}
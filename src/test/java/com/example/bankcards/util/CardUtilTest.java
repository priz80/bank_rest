package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CardUtilTest {

    @Autowired
    private CardUtil cardUtil;

    @Test
    void mask_ValidCardNumber_ReturnsMasked() {
        String masked = cardUtil.mask("1234567890123456");
        assertThat(masked).isEqualTo("1234 **** **** 3456");
    }

    @Test
    void mask_NullCardNumber_ReturnsStars() {
        String masked = cardUtil.mask(null);
        assertThat(masked).isEqualTo("****");
    }

    @Test
    void mask_ShortCardNumber_ReturnsStars() {
        String masked = cardUtil.mask("1234");
        assertThat(masked).isEqualTo("****");
    }
}
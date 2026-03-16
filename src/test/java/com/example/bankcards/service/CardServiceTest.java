// src/test/java/com/example/bankcards/service/CardServiceTest.java
package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardUtil cardUtil;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user1");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setCardHolderName("USER1");
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000.0));
        card.setUser(user);

        // ✅ Используем lenient() — чтобы избежать UnnecessaryStubbingException
        Mockito.lenient().when(cardUtil.mask(anyString())).thenReturn("**** **** **** 1111");
    }

    @Test
    void getCardById_Exists_ReturnsCardDto() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        CardDto result = cardService.getCardById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMaskedCardNumber()).isEqualTo("**** **** **** 1111");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1000.0));
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(999L))
                .isInstanceOf(CardException.class)
                .hasMessage("Карта не найдена");

        // Не использует cardUtil → но lenient() спасает
    }

    @Test
    void blockCard_ValidId_BlocksCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardDto result = cardService.blockCard(1L);

        assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(argThat(c -> c.getStatus() == CardStatus.BLOCKED));
    }
}
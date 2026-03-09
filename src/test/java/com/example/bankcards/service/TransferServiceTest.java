package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("500.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.ZERO);
        toCard.setStatus(CardStatus.ACTIVE);
        user.setCards(java.util.Arrays.asList(fromCard, toCard));
        fromCard.setUser(user);
        toCard.setUser(user);
    }

    // ✅ Тест: недостаточно средств → сообщение как в коде
    @Test
    void transfer_NotEnoughFunds_ThrowsException() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("600.00"));

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(java.util.Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transfer(request, user))
                .isInstanceOf(CardException.class)
                .hasMessage("Недостаточно средств на карте");
    }

    // ✅ Тест: нельзя переводить самому себе
    @Test
    void transfer_SameCard_ThrowsException() {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(fromCard));

        assertThatThrownBy(() -> transferService.transfer(request, user))
                .isInstanceOf(CardException.class)
                .hasMessage("Нельзя перевести деньги на ту же карту");
    }
}
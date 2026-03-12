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
import java.util.Optional;

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

        // Создаём пользователя
        user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        // Карта отправителя
        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("500.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        // Карта получателя
        toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.ZERO);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        // Убеждаемся, что обе карты привязаны к пользователю
        fromCard.setUser(user);
        toCard.setUser(user);
    }

    @Test
    void transfer_NotEnoughFunds_ThrowsException() {
        // given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("600.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // when & then
        assertThatThrownBy(() -> transferService.transfer(request, user))
                .isInstanceOf(CardException.class)
                .hasMessage("Недостаточно средств на карте");
    }

    @Test
    void transfer_SameCard_ThrowsException() {
        // given
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        // when & then
        assertThatThrownBy(() -> transferService.transfer(request, user))
                .isInstanceOf(CardException.class)
                .hasMessage("Нельзя перевести деньги на ту же карту");
    }

    @Test
    void transfer_CardNotBelongToUser_ThrowsException() {
        // given
        User otherUser = new User();
        otherUser.setId(2L);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));

        toCard.setUser(otherUser); // Карта не принадлежит user

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // when & then
        assertThatThrownBy(() -> transferService.transfer(request, user))
                .isInstanceOf(CardException.class)
                .hasMessage("Карта получателя не принадлежит пользователю");
    }

    @Test
    void transfer_ValidRequest_Success() {
        // given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // when
        transferService.transfer(request, user);

        // then
        assertThat(fromCard.getBalance()).isEqualByComparingTo("300.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("200.00");
        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
    }
}
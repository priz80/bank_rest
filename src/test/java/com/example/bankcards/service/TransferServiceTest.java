package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private Card senderCard;
    private Card receiverCard;

    @BeforeEach
    void setUp() {
        senderCard = new Card();
        senderCard.setId(1L);
        senderCard.setBalance(BigDecimal.valueOf(500.0));
        senderCard.setUser(new User());
        senderCard.getUser().setId(1L);

        receiverCard = new Card();
        receiverCard.setId(2L);
        receiverCard.setBalance(BigDecimal.valueOf(100.0));

        Mockito.lenient().when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void transfer_UserValid_TransfersMoney() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200.0));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        transferService.transfer(request, senderCard.getUser());

        assertThat(senderCard.getBalance()).isEqualTo(BigDecimal.valueOf(300.0));
        assertThat(receiverCard.getBalance()).isEqualTo(BigDecimal.valueOf(300.0));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(1000.0));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        assertThatThrownBy(() -> transferService.transfer(request, senderCard.getUser()))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Недостаточно средств на карте");
    }

    @Test
    void transfer_WrongOwner_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.0));

        User otherUser = new User();
        otherUser.setId(999L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));

        assertThatThrownBy(() -> transferService.transfer(request, otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Вы не можете переводить с чужой карты");
    }

    @Test
    void transfer_Admin_Successful() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200.0));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));

        transferService.transfer(request);

        assertThat(senderCard.getBalance()).isEqualTo(BigDecimal.valueOf(300.0));
        assertThat(receiverCard.getBalance()).isEqualTo(BigDecimal.valueOf(300.0));
    }
}
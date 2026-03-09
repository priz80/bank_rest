package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.bankcards.entity.Role;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardUtil cardUtil;
    @Mock
    private CardGenerator cardGenerator;

    @InjectMocks
    private CardService cardService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setStatus(User.Status.ACTIVE);
        user.setRole(Role.USER);
    }

    @Test
    void createCardForUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> cardService.createCardForUser(999L))
                .isInstanceOf(UserException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void createCardForUser_UserNotActive_ThrowsException() {
        user.setStatus(User.Status.BLOCKED);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> cardService.createCardForUser(1L))
                .isInstanceOf(CardException.class) // ← было UserException
                .hasMessage("Нельзя создать карту для неактивного пользователя");
    }

    @Test
    void createCardForUser_ValidUser_CreatesCard() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(cardGenerator.generateCardNumber()).thenReturn("1234567812345678");
        when(cardGenerator.generateCvv()).thenReturn("123");
        when(cardGenerator.calculateExpiryDate()).thenReturn(LocalDate.now().plusYears(5));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        CardDto dto = cardService.createCardForUser(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getCardHolderName()).isEqualTo("TESTUSER");
        assertThat(dto.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(dto.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cardRepository).save(any(Card.class));
    }
}
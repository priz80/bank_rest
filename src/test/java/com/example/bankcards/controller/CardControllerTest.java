package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // ✅ Добавлено

@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // ✅ Добавлено: ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getMyCards_ValidUser_ReturnsCardList() throws Exception {
        // Подготавливаем данные
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setRole(Role.USER);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setCardHolderName("USER1");
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000.0));

        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setMaskedCardNumber("**** **** **** 1111");
        cardDto.setCardHolderName(card.getCardHolderName());
        cardDto.setExpiryDate(java.time.YearMonth.from(card.getExpiryDate()));
        cardDto.setStatus(card.getStatus());
        cardDto.setBalance(card.getBalance());
        cardDto.setUserId(user.getId());

        Page<CardDto> page = new PageImpl<>(List.of(cardDto), PageRequest.of(0, 10), 1);

        // Мокаем зависимости JWT
        when(jwtUtil.extractUsername("mock-token")).thenReturn("user1");
        when(jwtUtil.extractRole("mock-token")).thenReturn("USER");
        when(jwtUtil.validateToken("mock-token", "user1")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("user1")).thenReturn(userDetails);

        when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

        // Выполняем запрос с заголовком
        mockMvc.perform(get("/api/cards")
                .header("Authorization", "Bearer mock-token")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.0))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(cardService).getCardsByUser(eq(user), any(Pageable.class));
    }

    @Test
    void transfer_ValidRequest_TransfersSuccessfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.0));

        // Мокаем JWT
        when(jwtUtil.extractUsername("mock-token")).thenReturn("user1");
        when(jwtUtil.extractRole("mock-token")).thenReturn("USER");
        when(jwtUtil.validateToken("mock-token", "user1")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("user1")).thenReturn(userDetails);
        doNothing().when(transferService).transfer(eq(request), eq(user));

        // Выполняем запрос
        mockMvc.perform(post("/api/cards/transfers")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Проверяем вызов
        verify(transferService).transfer(eq(request), eq(user));
    }

    @Test
    void transfer_AmountNull_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(null); // ← null

        when(jwtUtil.extractUsername("mock-token")).thenReturn("user1");
        when(jwtUtil.extractRole("mock-token")).thenReturn("USER");
        when(jwtUtil.validateToken("mock-token", "user1")).thenReturn(true);

        mockMvc.perform(post("/api/cards/transfers")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // ✅ Теперь реально 400
    }
}
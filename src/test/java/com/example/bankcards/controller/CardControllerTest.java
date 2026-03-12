package com.example.bankcards.controller;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.GenerateCardRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.example.bankcards.security.JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Пользователь USER
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole(Role.USER);
        user.setStatus(User.Status.ACTIVE);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Пользователь ADMIN
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        admin.setStatus(User.Status.ACTIVE);
        UserDetailsImpl adminDetails = new UserDetailsImpl(admin);

        // Моки UserDetailsService
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminDetails);

        // Моки JwtUtil
        when(jwtUtil.extractUsername("user-jwt-token")).thenReturn("user");
        when(jwtUtil.extractUsername("admin-jwt-token")).thenReturn("admin");

        when(jwtUtil.validateToken("user-jwt-token", userDetails)).thenReturn(true);
        when(jwtUtil.validateToken("admin-jwt-token", adminDetails)).thenReturn(true);

        when(jwtUtil.extractRole("user-jwt-token")).thenReturn(Role.USER);
        when(jwtUtil.extractRole("admin-jwt-token")).thenReturn(Role.ADMIN);

        // Инициализация токенов
        this.userToken = "Bearer user-jwt-token";
        this.adminToken = "Bearer admin-jwt-token";
    }

    @Test
    void generateCard_User_NoBody_ReturnsCard() throws Exception {
        // Подготовка
        CardDto mockDto = new CardDto();
        mockDto.setId(101L);
        mockDto.setMaskedCardNumber("**** **** **** 1234");
        mockDto.setBalance(BigDecimal.ZERO);

        when(cardService.createCardForUser(1L)).thenReturn(mockDto);

        // Выполнение и проверка
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.balance").value(0));

        verify(cardService).createCardForUser(1L);
    }

    @Test
    void generateCard_User_WithUserId_Forbidden() throws Exception {
        // Подготовка
        GenerateCardRequest request = new GenerateCardRequest();
        request.setUserId(2L); // даже если свой ID — запрещено

        // Выполнение и проверка
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCardForUser(anyLong());
    }

    @Test
    void generateCard_Admin_WithUserId_CreatesForUser() throws Exception {
        // Подготовка
        GenerateCardRequest request = new GenerateCardRequest();
        request.setUserId(1L);

        CardDto mockDto = new CardDto();
        mockDto.setId(102L);
        mockDto.setMaskedCardNumber("**** **** **** 5678");
        mockDto.setBalance(BigDecimal.valueOf(100.00));

        when(cardService.createCardForUser(1L)).thenReturn(mockDto);

        // Выполнение и проверка
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(102))
                .andExpect(jsonPath("$.balance").value(100.00));

        verify(cardService).createCardForUser(1L);
    }

    @Test
    void generateCard_Admin_NoBody_CreatesForSelf() throws Exception {
        // Подготовка
        CardDto mockDto = new CardDto();
        mockDto.setId(201L);
        mockDto.setMaskedCardNumber("**** **** **** 9999");
        mockDto.setBalance(BigDecimal.ZERO);

        when(cardService.createCardForUser(2L)).thenReturn(mockDto);

        // Выполнение и проверка
        mockMvc.perform(post("/api/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(201));

        verify(cardService).createCardForUser(2L);
    }
}
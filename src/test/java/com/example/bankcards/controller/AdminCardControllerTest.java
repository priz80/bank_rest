// src/test/java/com/example/bankcards/controller/AdminCardControllerTest.java
package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDto;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
@Import(SecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransferService transferService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllCards_ReturnsCardList() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        UserDetailsImpl userDetails = new UserDetailsImpl(admin);

        CardDto cardDto = new CardDto();
        cardDto.setId(1L);
        cardDto.setMaskedCardNumber("**** **** **** 1111");
        cardDto.setCardHolderName("ADMIN");
        cardDto.setExpiryDate(java.time.YearMonth.now().plusYears(5));
        cardDto.setStatus(CardStatus.ACTIVE);
        cardDto.setBalance(BigDecimal.valueOf(5000.0));

        var page = new PageImpl<>(List.of(cardDto), PageRequest.of(0, 10), 1);

        when(jwtUtil.extractUsername("mock-token")).thenReturn("admin");
        when(jwtUtil.extractRole("mock-token")).thenReturn("ADMIN");
        when(jwtUtil.validateToken("mock-token", "admin")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(cardService.getAllCards(any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")  // ✅ Исправлено: добавлен /
                        .header("Authorization", "Bearer mock-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1111"));
    }

    @Test
    void blockCard_ValidId_CallsService() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        UserDetailsImpl userDetails = new UserDetailsImpl(admin);

        when(jwtUtil.extractUsername("mock-token")).thenReturn("admin");
        when(jwtUtil.extractRole("mock-token")).thenReturn("ADMIN");
        when(jwtUtil.validateToken("mock-token", "admin")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        mockMvc.perform(post("/api/admin/cards/1/block")  // ✅ POST + /api/
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk());

        verify(cardService).blockCard(1L);
    }
}
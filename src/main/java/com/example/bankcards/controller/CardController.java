package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;

    public CardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getCards(Pageable pageable, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<CardDto> cards = cardService.getCardsByUser(user, pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<CardDto> createCard(@RequestParam Long userId, Authentication authentication) {
        System.out.println("✅ [DEBUG] CardController: Метод createCard вызван");
        System.out.println("✅ [DEBUG] userId = " + userId);

        if (authentication == null) {
            System.err.println("❌ Authentication is null");
            throw new RuntimeException("Аутентификация не выполнена");
        }

        System.out.println("✅ [DEBUG] Authentication exists: " + authentication.getClass().getSimpleName());
        System.out.println("✅ [DEBUG] Principal class: " + authentication.getPrincipal().getClass().getSimpleName());
        System.out.println("✅ [DEBUG] Principal value: " + authentication.getPrincipal());

        User user;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            user = userDetails.getUser(); // ✅ Получаем User из обёртки
        } catch (ClassCastException e) {
            System.err.println("❌ Ошибка приведения Principal к UserDetailsImpl: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка аутентификации", e);
        }

        System.out.println("✅ [DEBUG] Текущий пользователь: " + user.getUsername() + ", роль: " + user.getRole());

        boolean isOwner = user.getId().equals(userId);
        boolean isAdmin = Role.ADMIN.equals(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        System.out.println("✅ [DEBUG] Проверка доступа пройдена. Вызов cardService...");
        CardDto card = cardService.createCardForUser(userId);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CardDto card = cardService.getCardById(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CardDto card = cardService.blockCard(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CardDto card = cardService.activateCard(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        transferService.transfer(request, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cardService.deleteCard(id, user);
        return ResponseEntity.noContent().build();
    }
}
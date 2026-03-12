package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasRole('USER')")
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
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(userId) && !user.getRole().equals(com.example.bankcards.entity.Role.ADMIN)) {
            throw new RuntimeException("Доступ запрещён");
        }
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
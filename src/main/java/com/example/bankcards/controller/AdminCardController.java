package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Admin Cards Controller")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;
    private final TransferService transferService;

    public AdminCardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    // Получить все карты
    @GetMapping("/cards")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,asc") String sort) {

        String[] parts = sort.split(",", 2);
        String field = parts[0];
        Sort.Direction direction = "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, field));
        Page<CardDto> cards = cardService.getAllCards(pageable);

        return ResponseEntity.ok(cards);
    }

    // Создать карту для пользователя
    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@RequestParam Long userId) {
        CardDto card = cardService.createCardForUser(userId);
        return ResponseEntity.ok(card);
    }

    // Получить карту по ID
    @GetMapping("/cards/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    // Заблокировать карту
    @PostMapping("/cards/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id) {
        CardDto card = cardService.blockCard(id);
        return ResponseEntity.ok(card);
    }

    // Активировать карту
    @PostMapping("/cards/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id) {
        CardDto card = cardService.activateCard(id);
        return ResponseEntity.ok(card);
    }

    // Перевод средств (админ может переводить с любой на любую)
    @PostMapping("/cards/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        transferService.transfer(request);
        return ResponseEntity.ok().build();
    }

    // Удалить карту
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
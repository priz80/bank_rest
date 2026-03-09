package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.GenerateCardRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cards", description = "API для управления банковскими картами")
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // ✅ Основной эндпоинт: генерация новой карты (авто)
    @PostMapping
    @Operation(summary = "Создать новую карту (автогенерация номера, CVV, срока)")
    public ResponseEntity<CardDto> generateCard(
            @RequestBody(required = false) GenerateCardRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User currentUser = userDetails.getUser();
        Long targetUserId;

        if (request != null && request.getUserId() != null) {
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                throw new AccessDeniedException("Только администратор может создавать карты для других пользователей");
            }
            targetUserId = request.getUserId();
        } else {
            targetUserId = currentUser.getId();
        }

        CardDto cardDto = cardService.createCardForUser(targetUserId);
        return ResponseEntity.ok(cardDto);
    }

    /**
     * Получение своих карт (с пагинацией и сортировкой)
     */
    @GetMapping
    @Operation(summary = "Получить свои карты")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(name = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Размер страницы", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Parameter(description = "Сортировка: поле,направление", example = "id,asc") @RequestParam(name = "sort", defaultValue = "id,asc") String sort,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Sort.Order order = parseSort(sort);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(order));

        User user = userDetails.getUser();
        Page<CardDto> cards = cardService.getCardsByUser(user, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Получение карты по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить карту по ID")
    public ResponseEntity<CardDto> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        CardDto card = cardService.getCardById(id, user);
        return ResponseEntity.ok(card);
    }

    /**
     * Блокировка карты
     */
    @PostMapping("/{id}/block")
    @Operation(summary = "Заблокировать карту")
    public ResponseEntity<CardDto> blockCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        CardDto card = cardService.blockCard(id, user);
        return ResponseEntity.ok(card);
    }

    /**
     * Активация карты
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Активировать карту")
    public ResponseEntity<CardDto> activateCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        CardDto card = cardService.activateCard(id, user);
        return ResponseEntity.ok(card);
    }

    /**
     * Удаление карты
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить карту")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        cardService.deleteCard(id, user);
        return ResponseEntity.noContent().build();
    }

    // Вспомогательный метод
    private Sort.Order parseSort(String sort) {
        if (sort == null || sort.isEmpty() || !sort.contains(",")) {
            return Sort.Order.asc("id");
        }
        try {
            String[] parts = sort.split(",", 2);
            String property = parts[0].trim();
            Sort.Direction direction = "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return Sort.Order.by(property).with(direction);
        } catch (Exception e) {
            return Sort.Order.asc("id");
        }
    }
}
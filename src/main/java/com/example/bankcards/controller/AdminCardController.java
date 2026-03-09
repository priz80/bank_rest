package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
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

@Tag(name = "Admin", description = "Административные операции (только для ADMIN)")
@RestController
@RequestMapping("/api/admin")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    @Operation(summary = "Получить все карты системы")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(name = "page", defaultValue = "0") Integer page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(name = "size", defaultValue = "10") Integer size,

            @Parameter(description = "Сортировка: поле,направление", example = "id,desc")
            @RequestParam(name = "sort", defaultValue = "id,asc") String sort,

            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Доступ разрешён только администраторам");
        }

        Sort.Order order = parseSort(sort);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(order));

        Page<CardDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

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
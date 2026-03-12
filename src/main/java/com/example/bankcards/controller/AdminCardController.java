package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

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
}
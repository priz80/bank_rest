package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cadrs Controller")
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
    public ResponseEntity<Page<CardDto>> getCards(
            @ParameterObject Pageable pageable,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        Page<CardDto> cards = cardService.getCardsByUser(user, pageable);
        return ResponseEntity.ok(cards);
    }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteCard(@PathVariable Long id, Authentication authentication) {
    //     User user = getUserFromAuth(authentication);
    //     cardService.deleteCard(id, user);
    //     return ResponseEntity.noContent().build();
    // }
    

    @PostMapping
    public ResponseEntity<CardDto> createCard(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        CardDto card = cardService.createCardForUser(user.getId());
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuth(authentication);
        CardDto card = cardService.getCardById(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuth(authentication);
        CardDto card = cardService.blockCard(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id, Authentication authentication) {
        User user = getUserFromAuth(authentication);
        CardDto card = cardService.activateCard(id, user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        User user = getUserFromAuth(authentication);
        transferService.transfer(request, user);
        return ResponseEntity.ok().build();
    }

    private User getUserFromAuth(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
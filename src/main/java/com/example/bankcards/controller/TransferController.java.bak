package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        // ✅ Правильное извлечение User
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var user = userDetails.getUser(); // ← Теперь работает!

        transferService.transfer(request, user);
        return ResponseEntity.ok().build();
    }
}
package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для выполнения переводов между картами
 */
@Tag(name = "Cards", description = "API для управления банковскими картами")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(
        summary = "Перевести деньги между картами",
        description = "Выполняет перевод средств с одной карты на другую. " +
                     "Проверяет баланс, статус карт и права доступа."
    )
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        transferService.transfer(request, userDetails.getUser());
        return ResponseEntity.ok("Transfer is successful");
    }
}
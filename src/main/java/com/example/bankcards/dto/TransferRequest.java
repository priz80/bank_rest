package com.example.bankcards.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferRequest {
    @NotNull(message = "Source card ID is required")
    private Long fromCardId;

    @NotNull(message = "Target card ID is required")
    private Long toCardId;

    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}
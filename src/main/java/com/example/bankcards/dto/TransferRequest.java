// src/main/java/com/example/bankcards/dto/TransferRequest.java
package com.example.bankcards.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull(message = "Source card ID is required")
    private Long fromCardId;

    @NotNull(message = "Target card ID is required")
    private Long toCardId;

    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}
package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CreateCardRequest {

    @Schema(example = "4111111111111111", description = "16-digit card number")
    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 19, message = "Card number must be 16-19 digits")
    private String cardNumber;

    @Schema(example = "ИМЯ ФАМИЛИЯ", description = "Full name of cardholder")
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @Schema(example = "2027-12", description = "Expiry date in YYYY-MM format")
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(19|20)\\d{2}-(0[1-9]|1[0-2])$", message = "Expiry date must be in format YYYY-MM")
    private String expiryDate;

    @Schema(example = "0", description = "Initial balance")
    @DecimalMin(value = "0.0", message = "Balance must be positive")
    @DecimalMax(value = "999999.99", message = "Balance too large")
    private BigDecimal balance;
}
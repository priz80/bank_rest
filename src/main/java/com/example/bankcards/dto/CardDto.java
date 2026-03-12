package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.Data;

@Data
public class CardDto {
    private Long id;
    private String maskedCardNumber;
    private String cardHolderName;

    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth expiryDate;

    private CardStatus status;
    private BigDecimal balance;
    private Long userId;
}
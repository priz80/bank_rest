package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Запрос на генерацию новой банковской карты.
 * Номер, CVV, срок действия — генерируются автоматически.
 */
@Data
public class GenerateCardRequest {

    /**
     * ID пользователя, для которого создаётся карта.
     * Если null — создаётся для текущего пользователя.
     */
    @Schema(description = "ID пользователя. Только для ADMIN", example = "5")
    private Long userId;
}
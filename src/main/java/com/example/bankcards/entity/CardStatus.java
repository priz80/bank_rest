// src/main/java/com/example/bankcards/entity/CardStatus.java
package com.example.bankcards.entity;

public enum CardStatus {

    ACTIVE,
    BLOCKED,
    EXPIRED;

    /**
     * Проверяет, активна ли карта (не заблокирована и не просрочена)
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}
package com.example.bankcards.entity;

public enum CardStatus {

    ACTIVE,
    BLOCKED,
    EXPIRED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
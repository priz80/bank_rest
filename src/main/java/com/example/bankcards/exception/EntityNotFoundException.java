// src/main/java/com/example/bankcards/exception/EntityNotFoundException.java
package com.example.bankcards.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
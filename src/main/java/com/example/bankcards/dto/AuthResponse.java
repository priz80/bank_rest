package com.example.bankcards.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
}
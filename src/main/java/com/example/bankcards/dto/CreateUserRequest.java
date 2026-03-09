package com.example.bankcards.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private Long userId;
}
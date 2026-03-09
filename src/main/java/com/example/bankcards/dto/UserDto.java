package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User.Status;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private Role role;
     private Status status;
}
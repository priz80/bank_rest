package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Управление пользователями и картами (только ADMIN)")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; // ← теперь корректно внедрён

    // ✅ Создать пользователя
    @PostMapping("/users")
    @Operation(summary = "Создать нового пользователя")
    public ResponseEntity<UserDto> createUser(
            @RequestBody @Valid CreateUserRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = userService.createUser(request.getUsername(), encodedPassword);
        UserDto dto = userService.toDto(newUser);

        return ResponseEntity.ok(dto);
    }

    // ✅ Получить всех пользователей
    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ Получить пользователя по ID
    @GetMapping("/users/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ✅ Изменить статус пользователя
    @PutMapping("/users/{id}/status")
    @Operation(summary = "Изменить статус пользователя")
    public ResponseEntity<UserDto> updateStatus(
            @PathVariable Long id,
            @RequestParam User.Status status) {
        var updated = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(userService.toDto(updated));
    }

    // ✅ Удалить пользователя
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя (только если DELETED и нет карт)")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Пользователь успешно удалён");
    }
}
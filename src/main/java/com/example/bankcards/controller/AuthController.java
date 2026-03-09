package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Аутентификация пользователей: вход в систему")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Аутентифицирует пользователя и возвращает JWT-токен и данные пользователя"
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        try {
            logger.info("Login attempt for user: {}", request.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();

            String authority = userDetails.getAuthorities().iterator().next().getAuthority();
            String role = authority.replace("ROLE_", "");

            String token = jwtUtil.generateToken(user.getUsername(), role);
            UserDto userDto = userService.toDto(user);

            AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();

            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for user: {}", request.getUsername());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(500).build();
        }
    }
}
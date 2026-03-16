// src/test/java/com/example/bankcards/service/UserServiceTest.java
package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setStatus(User.Status.ACTIVE);
    }

    @Test
    void getUserById_Exists_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserException.class) // ✅ Исправлено
                .hasMessage("Пользователь не найден с ID: 999");
    }

    @Test
    void updateUserStatus_Valid_ChangesStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUserStatus(1L, User.Status.BLOCKED);

        assertThat(updated.getStatus()).isEqualTo(User.Status.BLOCKED);
        verify(userRepository).save(user); // ✅ Проверка вызова
    }

    @Test
    void toDto_MapsCorrectly() {
        UserDto dto = userService.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("user1");
        assertThat(dto.getRole()).isEqualTo(Role.USER);
        assertThat(dto.getStatus()).isEqualTo(User.Status.ACTIVE);
    }

    @Test
    void getAllUsers_Pageable_ReturnsPage() {
        // Используем PageImpl — настоящий класс Spring Data
        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDto> result = userService.getAllUsers(PageRequest.of(0, 10));

        verify(userRepository).findAll(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
    }
}
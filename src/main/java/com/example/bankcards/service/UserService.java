package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.User.Status;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    public UserDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь не найден с ID: " + id));
    }

    // Добавьте метод, если ещё нет:
    public User createUser(String username, String encodedPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserException("Пользователь с таким именем уже существует");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(Role.USER);
        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }

    public User updateUserStatus(Long id, Status newStatus) {
        User user = getUserEntityById(id);
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);

        if (user.getStatus() != Status.DELETED) {
            throw new UserException("Пользователь должен быть в статусе DELETED перед удалением.");
        }

        List<com.example.bankcards.entity.Card> cards = cardRepository.findByUserId(user.getId());
        if (!cards.isEmpty()) {
            throw new UserException("Нельзя удалить пользователя, у которого есть карты.");
        }

        userRepository.deleteById(id);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
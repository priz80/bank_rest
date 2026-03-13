package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import com.example.bankcards.util.CardUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardUtil cardUtil;
    private final CardGenerator cardGenerator;

    public CardService(CardRepository cardRepository,
            UserRepository userRepository,
            CardUtil cardUtil,
            CardGenerator cardGenerator) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardUtil = cardUtil;
        this.cardGenerator = cardGenerator;
    }

    public CardDto createCardForUser(Long userId) {
        System.out.println("🔧 Создание карты для пользователя ID: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Пользователь не найден" + userId));

        System.out.println("✅ Найден пользователь: " + user.getUsername() + ", статус: " + user.getStatus());

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new CardException("Нельзя создать карту для неактивного пользователя");
        }

        String cardNumber = cardGenerator.generateCardNumber();
        String cvv = cardGenerator.generateCvv();
        LocalDate expiryDate = cardGenerator.calculateExpiryDate();

        System.out.println("🔢 Сгенерирован номер карты: " + cardNumber);
        System.out.println("🔐 CVV: " + cvv);
        System.out.println("📅 Срок действия: " + expiryDate);

        Card card = new Card();
        card.setCardNumber(cardGenerator.generateCardNumber());
        card.setCvv(cardGenerator.generateCvv());
        card.setExpiryDate(cardGenerator.calculateExpiryDate());
        card.setCardHolderName(user.getUsername().toUpperCase());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        try {
            Card saved = cardRepository.save(card);
            System.out.println("💾 Карта сохранена, ID: " + saved.getId());
            return toDto(saved);
        } catch (Exception e) {
            System.err.println("❌ Ошибка при сохранении карты: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Page<CardDto> getCardsByUser(User user, Pageable pageable) {
        Page<Card> cards = Role.ADMIN.equals(user.getRole())
                ? cardRepository.findAll(pageable)
                : cardRepository.findByUser(user, pageable);
        return cards.map(this::toDto);
    }

    public CardDto getCardById(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);
        return toDto(card);
    }

    public CardDto blockCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updated = cardRepository.save(card);
        return toDto(updated);
    }

    public CardDto activateCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updated = cardRepository.save(card);
        return toDto(updated);
    }

    public void deleteCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Нельзя удалить активную карту");
        }

        cardRepository.delete(card);
    }

    public Card getCardEntityById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardException("Карта не найдена"));
    }

    public boolean isCardExpired(Card card) {
        if (card.getExpiryDate() == null) {
            return false;
        }
        return card.getExpiryDate().isBefore(LocalDate.now());
    }

    public void markAsExpired(Card card) {
        if (card.getStatus() != CardStatus.EXPIRED) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void expireOldCards() {
        List<Card> expiredCards = cardRepository.findActiveExpiredCards();
        expiredCards.forEach(this::markAsExpired);
    }

    private CardDto toDto(Card card) {
    System.out.println("🔧 Конвертация карты в DTO: ID=" + card.getId());
    System.out.println("🔢 Номер карты: " + (card.getCardNumber() != null ? "не null" : "null"));

    CardDto dto = new CardDto();
    dto.setId(card.getId());
    
    try {
        dto.setMaskedCardNumber(cardUtil.mask(card.getCardNumber()));
        System.out.println("✅ Маскировка успешна");
    } catch (Exception e) {
        System.err.println("❌ Ошибка маскировки номера карты: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }

    dto.setCardHolderName(card.getCardHolderName());
    dto.setExpiryDate(card.getExpiryDate() != null ? YearMonth.from(card.getExpiryDate()) : null);
    dto.setStatus(card.getStatus());
    dto.setBalance(card.getBalance());
    dto.setUserId(card.getUser().getId());
    return dto;
}

    public void checkAccess(Card card, User user) {
        if (!card.getUser().getId().equals(user.getId()) && !Role.ADMIN.equals(user.getRole())) {
            throw new CardException("Доступ запрещён");
        }
    }

    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toDto);
    }
}
package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import com.example.bankcards.util.CardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

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

    // ✅ Создать карту с генерацией (для админа или пользователя)
    public CardDto createCardForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Пользователь не найден"));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new CardException("Нельзя создать карту для неактивного пользователя");
        }

        Card card = new Card();
        card.setCardNumber(cardGenerator.generateCardNumber());
        card.setCvv(cardGenerator.generateCvv());
        card.setExpiryDate(cardGenerator.calculateExpiryDate());
        card.setCardHolderName(user.getUsername().toUpperCase());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    // ✅ Получение своих карт (USER) или всех (ADMIN)
    public Page<CardDto> getCardsByUser(User user, Pageable pageable) {
        Page<Card> cards = Role.ADMIN.equals(user.getRole())
                ? cardRepository.findAll(pageable)
                : cardRepository.findByUser(user, pageable);
        return cards.map(this::toDto);
    }

    // ✅ Получение карты по ID
    public CardDto getCardById(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);
        return toDto(card);
    }

    // ✅ Блокировка карты
    public CardDto blockCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updated = cardRepository.save(card);
        logger.info("Card blocked, ID={}", updated.getId());

        return toDto(updated);
    }

    // ✅ Активация карты
    public CardDto activateCard(Long id, User user) {
        logger.debug("Activating card with ID={}", id);

        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updated = cardRepository.save(card);
        logger.info("Card activated, ID={}", updated.getId());

        return toDto(updated);
    }

    // ✅ Удаление карты
    public void deleteCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Нельзя удалить активную карту");
        }

        cardRepository.delete(card);
        logger.info("Card deleted, ID={}", id);
    }

    // ✅ Получение сущности по ID
    public Card getCardEntityById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardException("Карта не найдена"));
    }

    // ✅ Проверка просроченности
    public boolean isCardExpired(Card card) {
        if (card.getExpiryDate() == null) {
            logger.warn("Card with ID={} has null expiry date", card.getId());
            return false;
        }
        return card.getExpiryDate().isBefore(LocalDate.now());
    }

    // ✅ Пометить как просроченную
    public void markAsExpired(Card card) {
        if (card.getStatus() != CardStatus.EXPIRED) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            logger.info("Card marked as expired, ID={}", card.getId());
        }
    }

    // ✅ Ежедневная проверка
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireOldCards() {
        logger.info("Running scheduled task: checking expired cards");
        List<Card> expiredCards = cardRepository.findActiveExpiredCards();
        expiredCards.forEach(this::markAsExpired);
        if (!expiredCards.isEmpty()) {
            logger.info("Marked {} cards as expired", expiredCards.size());
        }
    }

    // ✅ Преобразование в DTO
    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardUtil.mask(card.getCardNumber()));
        dto.setCardHolderName(card.getCardHolderName());

        // ✅ Исправлено: проверяем card.getExpiryDate(), а не
        // cardGenerator.calculateExpiryDate()
        dto.setExpiryDate(card.getExpiryDate() != null ? YearMonth.from(card.getExpiryDate()) : null);

        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        return dto;
    }

    // ✅ Проверка доступа
    public void checkAccess(Card card, User user) {
        if (!card.getUser().getId().equals(user.getId()) && !Role.ADMIN.equals(user.getRole())) {
            logger.warn("Access denied for user ID={} trying to access card ID={}", user.getId(), card.getId());
            throw new CardException("Доступ запрещён");
        }
    }
}
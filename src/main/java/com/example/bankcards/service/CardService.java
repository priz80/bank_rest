package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.repository.CardRepository;
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
    private final CardUtil cardUtil;

    public CardService(CardRepository cardRepository, CardUtil cardUtil) {
        this.cardRepository = cardRepository;
        this.cardUtil = cardUtil;
    }

    /**
     * Создание новой карты
     */
    public CardDto createCard(CreateCardRequest request, User user) {
        logger.debug("Creating new card for user ID={}", user.getId());

        String encryptedCardNumber = cardUtil.encrypt(request.getCardNumber());
        if (cardRepository.existsByCardNumber(encryptedCardNumber)) {
            throw new CardException("Card with this number already exists");
        }

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(request.getExpiryDate());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiry date format: use YYYY-MM", e);
        }
        LocalDate cardExpiryDate = yearMonth.atDay(1);

        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setOwnerName(request.getCardHolderName());
        card.setExpiryDate(cardExpiryDate);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        card.setUser(user);

        logger.info("Saving card for user ID={}", user.getId());
        Card saved = cardRepository.save(card);
        logger.info("Card saved successfully, ID={}", saved.getId());

        return toDto(saved);
    }

    /**
     * Получение своих карт (для USER) или всех (для ADMIN)
     */
    public Page<CardDto> getCardsByUser(User user, Pageable pageable) {
        Page<Card> cards = (!Role.ADMIN.equals(user.getRole()))
                ? cardRepository.findAll(pageable)
                : cardRepository.findByUser(user, pageable);
        return cards.map(this::toDto);
    }

    /**
     * Получение конкретной карты по ID
     */
    public CardDto getCardById(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);
        return toDto(card);
    }

    /**
     * Получение всех карт (только для ADMIN)
     */
    public Page<CardDto> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);
        return cards.map(this::toDto);
    }

    /**
     * Блокировка карты
     */
    public CardDto blockCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        card.setStatus(CardStatus.BLOCKED);
        Card updated = cardRepository.save(card);
        logger.info("Card blocked, ID={}", updated.getId());

        return toDto(updated);
    }

    /**
     * Активация карты
     */
    public CardDto activateCard(Long id, User user) {
        logger.debug("Activating card with ID={}", id);

        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (CardStatus.ACTIVE.equals(card.getStatus())) {
            throw new CardException("Card is already active");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updated = cardRepository.save(card);
        logger.info("Card activated, ID={}", updated.getId());

        return toDto(updated);
    }

    /**
     * Удаление карты
     */
    public void deleteCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Нельзя удалить активную карту");
        }

        cardRepository.delete(card);
        logger.info("Card deleted, ID={}", id);
    }

    /**
     * Поиск сущности по ID
     */
    public Card getCardEntityById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardException("Card not found"));
    }

    /**
     * Проверка, просрочена ли карта
     */
    public boolean isCardExpired(Card card) {
        if (card.getExpiryDate() == null) {
            logger.warn("Card with ID={} has null expiry date", card.getId());
            return false;
        }
        return card.getExpiryDate().isBefore(LocalDate.now());
    }

    /**
     * Пометить карту как просроченную
     */
    public void markAsExpired(Card card) {
        if (!CardStatus.EXPIRED.equals(card.getStatus())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            logger.info("Card marked as expired, ID={}", card.getId());
        }
    }

    /**
     * Фоновая задача: ежедневная проверка просроченных карт
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireOldCards() {
        logger.info("Running scheduled task: checking expired cards");

        List<Card> expiredCards = cardRepository.findActiveExpiredCards();
        expiredCards.forEach(this::markAsExpired);

        if (!expiredCards.isEmpty()) {
            logger.info("Marked {} cards as expired", expiredCards.size());
        }
    }

    /**
     * Преобразование сущности в DTO
     */
    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardUtil.mask(card.getCardNumber()));
        dto.setOwnerName(card.getOwnerName());
        dto.setExpiryDate(card.getExpiryDate() != null ? YearMonth.from(card.getExpiryDate()) : null);
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        return dto;
    }

    /**
     * Проверка доступа к карте
     */
    private void checkAccess(Card card, User user) {
        if (!card.getUser().getId().equals(user.getId()) && !Role.ADMIN.equals(user.getRole())) {
            logger.warn("Access denied for user ID={} trying to access card ID={}", user.getId(), card.getId());
            throw new CardException("Access denied");
        }

    }
}
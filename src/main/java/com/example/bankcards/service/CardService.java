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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Пользователь не найден: " + userId));

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
        return toDto(cardRepository.save(card));
    }

    public CardDto activateCard(Long id, User user) {
        Card card = getCardEntityById(id);
        checkAccess(card, user);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        return toDto(cardRepository.save(card));
    }

    public CardDto getCardById(Long id) {
        Card card = getCardEntityById(id);
        return toDto(card);
    }

    public CardDto blockCard(Long id) {
        Card card = getCardEntityById(id);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        return toDto(cardRepository.save(card));
    }

    public CardDto activateCard(Long id) {
        Card card = getCardEntityById(id);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        return toDto(cardRepository.save(card));
    }

    public void deleteCard(Long id) {
        Card card = getCardEntityById(id);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardException("Нельзя удалить активную карту");
        }

        cardRepository.delete(card);
    }

    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toDto);
    }

    public boolean isCardExpired(Card card) {
        return card.getExpiryDate() != null && card.getExpiryDate().isBefore(LocalDate.now());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void expireOldCards() {
        List<Card> expiredCards = cardRepository.findActiveExpiredCards();
        expiredCards.forEach(this::markAsExpired);
    }

    private void markAsExpired(Card card) {
        if (card.getStatus() != CardStatus.EXPIRED) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
    }

    private Card getCardEntityById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardException("Карта не найдена"));
    }

    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardUtil.mask(card.getCardNumber()));
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate() != null ? YearMonth.from(card.getExpiryDate()) : null);
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        return dto;
    }

    private void checkAccess(Card card, User user) {
        if (!card.getUser().getId().equals(user.getId()) && !Role.ADMIN.equals(user.getRole())) {
            throw new CardException("Доступ запрещён");
        }
    }
}
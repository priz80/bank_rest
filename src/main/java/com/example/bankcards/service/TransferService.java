// src/main/java/com/example/bankcards/service/TransferService.java
package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final CardRepository cardRepository;

    public TransferService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Выполняет перевод средств между двумя картами
     */
    public void transfer(TransferRequest request, User user) {
        Long fromCardId = request.getFromCardId();
        Long toCardId = request.getToCardId();
        BigDecimal amount = request.getAmount();

        logger.info("User ID={} initiating transfer: {} from card ID={} to card ID={}",
                user.getId(), amount, fromCardId, toCardId);

        // Проверка: карта-источник существует
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardException("Исходящая карта не найдена"));

        // Проверка: карта-назначение существует
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardException("Целевая карта не найдена"));

        // Проверка доступа к исходящей карте
        if (!fromCard.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            logger.warn("Access denied: user ID={} tried to access card ID={}", user.getId(), fromCardId);
            throw new CardException("Нет доступа к исходящей карте");
        }

        // Проверка статуса карты-источника
        if (!fromCard.getStatus().isActive()) {
            logger.warn("Source card is not active: status={}", fromCard.getStatus());
            throw new CardException("Исходящая карта недействительна (статус: " + fromCard.getStatus() + ")");
        }

        // Проверка статуса целевой карты
        if (!toCard.getStatus().isActive()) {
            logger.warn("Target card is not active: status={}", toCard.getStatus());
            throw new CardException("Целевая карта недействительна (статус: " + toCard.getStatus() + ")");
        }

        // Проверка баланса
        if (fromCard.getBalance().compareTo(amount) < 0) {
            logger.warn("Insufficient funds: balance={}, requested={}", fromCard.getBalance(), amount);
            throw new CardException("Недостаточно средств на карте");
        }

        // Выполнение перевода
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        logger.info("Transfer completed successfully: {} transferred from card {} to card {}", amount, fromCardId, toCardId);
    }
}
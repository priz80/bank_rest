// src/main/java/com/example/bankcards/service/TransferService.java
package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class TransferService {

    @Autowired
    private CardRepository cardRepository;

    /**
     * Перевод от имени пользователя.
     * Проверяет, что отправитель — владелец карты.
     */
    public void transfer(TransferRequest request, User user) {
        Card senderCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new IllegalArgumentException("Карта отправителя не найдена"));

        // Проверка: пользователь — владелец карты
        if (!senderCard.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Вы не можете переводить с чужой карты");
        }

        performTransfer(senderCard, request.getToCardId(), request.getAmount());
    }

    /**
     * Перевод от имени администратора.
     * Никаких проверок владельца — админ может переводить с любой карты.
     */
    public void transfer(TransferRequest request) {
        Card senderCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new IllegalArgumentException("Карта отправителя не найдена"));

        performTransfer(senderCard, request.getToCardId(), request.getAmount());
    }

    /**
     * Выполняет сам перевод: списание и зачисление.
     */
    private void performTransfer(Card senderCard, Long toCardId, BigDecimal amount) {
        Card receiverCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта получателя не найдена"));

        if (senderCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте");
        }

        senderCard.setBalance(senderCard.getBalance().subtract(amount));
        receiverCard.setBalance(receiverCard.getBalance().add(amount));

        cardRepository.save(senderCard);
        cardRepository.save(receiverCard);
    }
}
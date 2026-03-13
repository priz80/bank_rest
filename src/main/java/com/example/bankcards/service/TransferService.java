package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.exception.TransferException;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Transactional
public class TransferService {

    private final CardRepository cardRepository;

    public TransferService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public void transfer(TransferRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new TransferException("Исходящая карта не найдена"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new TransferException("Входящая карта не найдена"));

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransferException("Недостаточно средств на карте отправителя");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public void transfer(TransferRequest request, User user) {
        if (request == null) {
            throw new CardException("Запрос на перевод не может быть пустым");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardException("Сумма перевода должна быть больше нуля");
        }

        if (request.getFromCardId() == null) {
            throw new CardException("ID карты отправителя не может быть пустым");
        }
        if (request.getToCardId() == null) {
            throw new CardException("ID карты получателя не может быть пустым");
        }

        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new CardException("Нельзя перевести деньги на ту же карту");
        }

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardException("Карта отправителя не найдена"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardException("Карта получателя не найдена"));

        if (!Objects.equals(fromCard.getUser().getId(), user.getId()) ||
            !Objects.equals(toCard.getUser().getId(), user.getId())) {
            throw new CardException("Обе карты должны принадлежать текущему пользователю");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardException("Карта отправителя не активна");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardException("Карта получателя не активна");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new CardException("Недостаточно средств на карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
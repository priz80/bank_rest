package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUser(User user);

    Page<Card> findByUser(User user, Pageable pageable);

    Optional<Card> findByIdAndUser(Long id, User user);

    Optional<Card> findByCardNumber(String cardNumber);

    Boolean existsByCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c WHERE c.status != 'EXPIRED' AND c.expiryDate < CURRENT_DATE")
    List<Card> findActiveExpiredCards();

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(Long userId);
}
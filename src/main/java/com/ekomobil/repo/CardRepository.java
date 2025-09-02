package com.ekomobil.repo;

import com.ekomobil.domain.entity.Card;
import com.ekomobil.domain.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findAllByUser(User user);
    boolean existsByCardNumber(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> lockById(@Param("id") Long id);
}

package com.ekomobil.repo;

import com.ekomobil.domain.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    List<CardTransaction> findByCard_IdOrderByIdDesc(Long cardId);
}

package com.ekomobil.service;

import com.ekomobil.domain.dto.card.*;
import com.ekomobil.domain.entity.Card;
import com.ekomobil.domain.entity.CardTransaction;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.CardRepository;
import com.ekomobil.repo.CardTransactionRepository;
import com.ekomobil.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.text.Normalizer;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private static final int CARD_NUMBER_MAX = 64;

    private final CardRepository cardRepo;
    private final CardTransactionRepository txRepo;
    private final UserRepository userRepo;


    private CardDto toDto(Card c) {
        return CardDto.builder()
                .id(c.getId())
                .cardNumber(c.getCardNumber())
                .alias(c.getAlias())
                .currency(c.getCurrency())
                .balanceCents(c.getBalanceCents())
                .status(c.getStatus().name())
                .build();
    }

    private TransactionDto toTxDto(CardTransaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .type(t.getType().name())
                .amountCents(t.getAmountCents())
                .balanceAfter(t.getBalanceAfter())
                .note(t.getNote())
                .createdAt(t.getCreatedAt().toString())
                .build();
    }


    private static String normalizeCard(String raw) {
        if (raw == null) return null;
        String n = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        return n.replaceAll("^[\\p{Z}\\s]+|[\\p{Z}\\s]+$", "");
    }

    private User loadUserOr404(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
    }

    private Card loadCardOr404(Long cardId) {
        return cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kart bulunamadı"));
    }

    private Card lockCardOr404(Long cardId) {
        return cardRepo.lockById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kart bulunamadı"));
    }

    private void assertOwner(Long userId, Card c) {
        if (!c.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Yetkisiz erişim");
        }
    }

    private void assertActive(Card c) {
        if (c.getStatus() != Card.CardStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kart aktif değil");
        }
    }


    public List<CardDto> myCards(Long userId) {
        User u = loadUserOr404(userId);
        return cardRepo.findAllByUser(u).stream().map(this::toDto).toList();
    }

    public CardDto getCard(Long userId, Long cardId) {
        Card c = loadCardOr404(cardId);
        assertOwner(userId, c);
        return toDto(c);
    }

    public List<TransactionDto> history(Long userId, Long cardId) {
        Card c = loadCardOr404(cardId);
        assertOwner(userId, c);
        return txRepo.findByCard_IdOrderByIdDesc(cardId).stream().map(this::toTxDto).toList();
    }

    @Transactional
    public CardDto createCard(Long userId, CreateCardRequest req) {
        String normalized = normalizeCard(req.getCardNumber());
        if (normalized == null || normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kart numarası zorunlu");
        }
        if (normalized.length() > CARD_NUMBER_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Kart numarası en fazla " + CARD_NUMBER_MAX + " karakter olabilir");
        }

        User u = loadUserOr404(userId);

        Card c = Card.builder()
                .user(u)
                .cardNumber(normalized)
                .alias(req.getAlias())
                .balanceCents(0L)
                .currency("TRY")
                .status(Card.CardStatus.ACTIVE)
                .build();

        try {
            return toDto(cardRepo.saveAndFlush(c));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            String code = findSqlState(ex);
            String msg  = rootMessage(ex);
            if ("23505".equals(code)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kart numarası kullanımda");
            }
            if ("23502".equals(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zorunlu alan eksik: " + msg);
            }
            if ("23503".equals(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz ilişki/FK: " + msg);
            }
            if ("22001".equals(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alan uzunluğu aşıldı: " + msg);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veri bütünlüğü hatası: " + msg);
        }
    }

    private String findSqlState(Throwable t) {
        while (t != null) {
            if (t instanceof java.sql.SQLException se) {
                return se.getSQLState();
            }
            t = t.getCause();
        }
        return null;
    }
    private String rootMessage(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage();
    }
    
    @Transactional
    public CardDto topUp(Long userId, Long cardId, TopUpRequest req) {
        if (req.getAmountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutar geçersiz");
        }

        Card c = lockCardOr404(cardId);
        assertOwner(userId, c);
        assertActive(c);

        c.setBalanceCents(Math.addExact(c.getBalanceCents(), req.getAmountCents()));
        cardRepo.save(c);

        txRepo.save(CardTransaction.builder()
                .card(c)
                .type(CardTransaction.TxType.TOPUP)
                .amountCents(req.getAmountCents())
                .balanceAfter(c.getBalanceCents())
                .note(req.getNote())
                .build());

        return toDto(c);
    }

    @Transactional
    public CardDto charge(Long userId, Long cardId, ChargeRequest req) {
        if (req.getAmountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutar geçersiz");
        }

        Card c = lockCardOr404(cardId);
        assertOwner(userId, c);
        assertActive(c);

        if (c.getBalanceCents() < req.getAmountCents()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yetersiz bakiye");
        }

        c.setBalanceCents(c.getBalanceCents() - req.getAmountCents());
        cardRepo.save(c);

        txRepo.save(CardTransaction.builder()
                .card(c)
                .type(CardTransaction.TxType.CHARGE)
                .amountCents(req.getAmountCents())
                .balanceAfter(c.getBalanceCents())
                .note(req.getNote())
                .build());

        return toDto(c);
    }

    @Transactional
    public void transfer(Long userId, Long fromCardId, TransferRequest req) {
        if (req.getAmountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutar geçersiz");
        }

        Card from = lockCardOr404(fromCardId);
        assertOwner(userId, from);
        assertActive(from);

        if (from.getCardNumber().equals(req.getToCardNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aynı karta transfer yapılamaz");
        }

        Card to = cardRepo.findByCardNumber(normalizeCard(req.getToCardNumber()))
                .flatMap(c -> cardRepo.lockById(c.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hedef kart bulunamadı"));
        assertActive(to);

        if (from.getBalanceCents() < req.getAmountCents()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yetersiz bakiye");
        }

        from.setBalanceCents(from.getBalanceCents() - req.getAmountCents());
        to.setBalanceCents(Math.addExact(to.getBalanceCents(), req.getAmountCents()));
        cardRepo.save(from);
        cardRepo.save(to);

        txRepo.save(CardTransaction.builder()
                .card(from)
                .type(CardTransaction.TxType.TRANSFER_OUT)
                .amountCents(req.getAmountCents())
                .balanceAfter(from.getBalanceCents())
                .note(req.getNote())
                .build());

        txRepo.save(CardTransaction.builder()
                .card(to)
                .type(CardTransaction.TxType.TRANSFER_IN)
                .amountCents(req.getAmountCents())
                .balanceAfter(to.getBalanceCents())
                .note("from:" + from.getCardNumber())
                .build());
    }

    @Transactional
    public CardDto block(Long userId, Long cardId) {
        Card c = lockCardOr404(cardId);
        assertOwner(userId, c);
        c.setStatus(Card.CardStatus.BLOCKED);
        return toDto(cardRepo.save(c));
    }
}

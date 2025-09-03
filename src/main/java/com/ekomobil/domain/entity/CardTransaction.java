package com.ekomobil.domain.entity;

import com.ekomobil.domain.entity.Card;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "card_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private TxType type;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "note")
    private String note;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum TxType { TOPUP, CHARGE, TRANSFER_OUT, TRANSFER_IN, REFUND }
}

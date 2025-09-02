package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_number", nullable = false, unique = true, length = 64)
    private String cardNumber;

    @Column(name = "alias", length = 64)
    private String alias;

    @Builder.Default
    @Column(name = "balance_cents", nullable = false)
    private long balanceCents = 0L;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "TRY";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 16)
    private CardStatus status = CardStatus.ACTIVE;

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum CardStatus { ACTIVE, BLOCKED }
}

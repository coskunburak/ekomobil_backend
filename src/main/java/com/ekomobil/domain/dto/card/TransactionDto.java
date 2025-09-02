package com.ekomobil.domain.dto.card;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TransactionDto {
    private Long id;
    private String type;
    private long amountCents;
    private long balanceAfter;
    private String note;
    private String createdAt;
}

package com.ekomobil.domain.dto.card;


import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardDto
{
    private long id;
    private String cardNumber;
    private String alias;
    private String currency;
    private long balanceCents;
    private String status;
}

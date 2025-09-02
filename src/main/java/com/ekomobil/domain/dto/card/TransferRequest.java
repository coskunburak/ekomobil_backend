package com.ekomobil.domain.dto.card;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransferRequest
{
    private String toCardNumber;
    private long amountCents;
    private String note;
}

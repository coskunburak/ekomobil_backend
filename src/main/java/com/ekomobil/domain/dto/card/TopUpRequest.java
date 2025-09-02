package com.ekomobil.domain.dto.card;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TopUpRequest
{
    private long AmountCents;
    private String note;
}

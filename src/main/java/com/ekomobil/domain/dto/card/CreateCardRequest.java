package com.ekomobil.domain.dto.card;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateCardRequest
{
    private String cardNumber;
    private String alias;
}

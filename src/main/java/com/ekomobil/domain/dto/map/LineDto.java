package com.ekomobil.domain.dto.map;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class LineDto
{
    Long id;
    String code;
    String name;
    String color;
}

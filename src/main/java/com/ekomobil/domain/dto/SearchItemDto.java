package com.ekomobil.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchItemDto {
    String type;
    Long   id;
    String name;
    String shortName;
    String code;
}
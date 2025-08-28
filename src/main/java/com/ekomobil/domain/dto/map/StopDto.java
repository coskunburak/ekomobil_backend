package com.ekomobil.domain.dto.map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value @Builder @Getter @Setter
public class StopDto
{
    Long id;
    String name;
    Double lat;
    Double lon;
    Integer orderNo;
}

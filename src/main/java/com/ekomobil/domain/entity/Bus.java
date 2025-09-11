package com.ekomobil.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity @Table(name="buses")
@Setter @Getter
public class Bus
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false,unique = true, length = 20)
    private String plate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="route_id")
    private Route route;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at",nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

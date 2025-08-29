package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
public class Route
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false,length = 120)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at",nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(length = 16)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String polyline;
}

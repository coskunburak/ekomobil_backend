package com.ekomobil.domain.entity;

import jakarta.persistence.*;

@Entity @Table(name = "stop")
public class Stop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private Double lat;
    @Column(nullable = false) private Double lon;
    // getters/setters
}

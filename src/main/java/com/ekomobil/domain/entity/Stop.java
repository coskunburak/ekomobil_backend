package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "stops")
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(nullable=false)
    private Double lat;

    @Column(nullable=false)
    private Double lon;

    @Column(length=500)
    private String description;

}

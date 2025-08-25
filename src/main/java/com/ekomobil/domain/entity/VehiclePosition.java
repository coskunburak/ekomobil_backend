package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity @Table(name="vehicle_position")
@Getter @Setter
public class VehiclePosition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="bus_id")
    private Bus bus;

    @Column(nullable=false) private double lat;
    @Column(nullable=false) private double lon;
    private Double speed;   // km/h
    private Double heading; // 0..360

    @Column(nullable=false)
    private OffsetDateTime ts;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    // getters/setters
}

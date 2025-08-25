package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "vehicle_position")
public class VehiclePosition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name="bus_id", nullable=false) private Long busId; // basit tutuyoruz
    @Column(nullable=false) private OffsetDateTime ts;
    @Column(nullable=false) private Double lat;
    @Column(nullable=false) private Double lon;
    private Double speed; private Double heading;
    // getters/setters
}

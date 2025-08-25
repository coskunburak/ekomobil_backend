package com.ekomobil.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

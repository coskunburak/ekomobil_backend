package com.ekomobil.domain.entity;


import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity  @Table(name ="device_keys")
public class DeviceKey
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name= "bus_id")
    private Bus bus;

    @Column(name = "api_key", nullable = false,unique = true,length = 64)
    private String apiKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_used_at")
    private OffsetDateTime lastusedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OffsetDateTime getLastusedAt() {
        return lastusedAt;
    }

    public void setLastusedAt(OffsetDateTime lastusedAt) {
        this.lastusedAt = lastusedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

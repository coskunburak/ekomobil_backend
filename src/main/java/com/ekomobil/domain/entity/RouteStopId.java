package com.ekomobil.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RouteStopId implements Serializable {

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "stop_id")
    private Long stopId;

    public RouteStopId() {}
    public RouteStopId(Long routeId, Long stopId) {
        this.routeId = routeId;
        this.stopId = stopId;
    }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteStopId that)) return false;
        return Objects.equals(routeId, that.routeId) &&
                Objects.equals(stopId, that.stopId);
    }
    @Override public int hashCode() {
        return Objects.hash(routeId, stopId);
    }
}

package com.ekomobil.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "route_stop")
public class RouteStop {

    @EmbeddedId
    private RouteStopId id;

    // route_id'yi id.routeId ile paylaşır
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("routeId")
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // stop_id'yi id.stopId ile paylaşır
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stopId")
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "dwell_seconds", nullable = false)
    private int dwellSeconds = 0;

    public RouteStopId getId() { return id; }
    public void setId(RouteStopId id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public int getDwellSeconds() { return dwellSeconds; }
    public void setDwellSeconds(int dwellSeconds) { this.dwellSeconds = dwellSeconds; }
}

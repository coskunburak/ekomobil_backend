package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "route_stop")
public class RouteStop {

    @EmbeddedId
    private RouteStopId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("routeId")
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stopId")
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "dwell_seconds", nullable = false)
    private int dwellSeconds = 0;

    @Column(name = "order_no")
    private Integer orderNo;

}

package com.ekomobil.repo;

import com.ekomobil.domain.entity.RouteStop;
import com.ekomobil.domain.entity.RouteStopId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, RouteStopId> {

    List<RouteStop> findByRoute_IdOrderByOrderIndexAsc(Long routeId);

}

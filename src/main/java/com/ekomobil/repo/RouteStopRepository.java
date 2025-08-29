package com.ekomobil.repo;

import com.ekomobil.domain.entity.RouteStop;
import com.ekomobil.domain.entity.Stop;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    @Query("""
      select rs from RouteStop rs
      join fetch rs.stop s
      where rs.route.id = :routeId
      order by rs.orderNo asc
    """)
    List<RouteStop> findByRouteIdOrderByOrderNo(@Param("routeId") Long routeId);
    @Query("select s from Stop s")
    List<Stop> findAllLite();

    @Query("""
   select r.code from RouteStop rs
   join rs.route r
   where rs.stop.id = :stopId
   order by r.code
""")
    List<String> findServedRouteCodesByStop(@Param("stopId") Long stopId);
}

package com.ekomobil.repo;

import com.ekomobil.domain.entity.Stop;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long>
{
    @Query("""
     select s from Stop s
     where abs(s.lat - :lat) <= :deg and abs(s.lon - :lon) <= :deg
  """)
    List<Stop> findNearby(@Param("lat") double lat, @Param("lon") double lon, @Param("deg") double deg);
}

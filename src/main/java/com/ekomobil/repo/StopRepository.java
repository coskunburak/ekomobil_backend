package com.ekomobil.repo;

import com.ekomobil.domain.entity.Stop;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {

    @Query("""
        select s from Stop s
        where abs(s.lat - :lat) <= :deg and abs(s.lon - :lon) <= :deg
    """)
    List<Stop> findNearby(@Param("lat") double lat,
                          @Param("lon") double lon,
                          @Param("deg") double deg);

    @Query("""
        select s from Stop s
        where lower(coalesce(s.name, '')) like lower(concat('%', :q, '%'))
           or lower(coalesce(s.description, '')) like lower(concat('%', :q, '%'))
    """)
    List<Stop> search(@Param("q") String q, Pageable pageable);

}

/*
buna bir bakacağım
@GetMapping("/nearby-stops")
public List<StopDto> nearby(@RequestParam double lat,
                            @RequestParam double lon,
                            @RequestParam(defaultValue="0.01") double deg) {
    return stopRepository.findNearby(lat, lon, deg)
            .stream().map(s -> new StopDto(s.getId(), s.getName(), s.getLat(), s.getLon(), s.getDescription()))
            .toList();
}

 */
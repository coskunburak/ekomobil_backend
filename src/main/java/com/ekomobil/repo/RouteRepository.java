package com.ekomobil.repo;

import com.ekomobil.domain.entity.Route;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCode(String code);

    @Query("""
       select r from Route r
       where lower(coalesce(r.name,'')) like lower(concat('%', :q, '%'))
          or lower(coalesce(r.code,'')) like lower(concat('%', :q, '%'))
    """)
    List<Route> search(@Param("q") String q, Pageable pageable);
}

package com.ekomobil.repo;

import com.ekomobil.domain.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCode(String code);
}


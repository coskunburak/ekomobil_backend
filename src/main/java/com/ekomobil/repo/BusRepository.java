package com.ekomobil.repo;

import com.ekomobil.domain.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long>
{
    Optional<Bus> findByCode(String code);
    Boolean existsByPlate(String plate);
}

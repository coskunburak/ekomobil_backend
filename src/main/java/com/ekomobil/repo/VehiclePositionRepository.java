package com.ekomobil.repo;

import com.ekomobil.domain.entity.VehiclePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiclePositionRepository extends JpaRepository<VehiclePosition, Long>
{
    Optional<VehiclePosition> findTopByBus_IdOrderByTsDesc(Long busId);
    //List<VehiclePosition> findByBusIdAndTsBetweenOrderByTsAsc(Long busId, OffsetDateTime from, OffsetDateTime to);
}

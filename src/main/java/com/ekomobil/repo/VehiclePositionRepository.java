package com.ekomobil.repo;

import com.ekomobil.domain.entity.VehiclePosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiclePositionRepository extends JpaRepository<VehiclePosition, Long>
{
}

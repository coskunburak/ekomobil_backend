package com.ekomobil.repo;

import com.ekomobil.domain.entity.DeviceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceKeyRepository extends JpaRepository<DeviceKey, Long>
{
    Optional<DeviceKey> findByApiKeyAndEnabledTrue(String apiKey);
}

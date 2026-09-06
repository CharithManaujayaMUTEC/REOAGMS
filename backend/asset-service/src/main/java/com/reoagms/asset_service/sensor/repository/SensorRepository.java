package com.reoagms.asset_service.sensor.repository;

import com.reoagms.asset_service.sensor.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID>, JpaSpecificationExecutor<Sensor> {

    List<Sensor> findByAssetId(UUID assetId);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

}

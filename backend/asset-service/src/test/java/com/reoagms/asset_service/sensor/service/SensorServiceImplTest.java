package com.reoagms.asset_service.sensor.service;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.common.exception.InvalidRequestException;
import com.reoagms.asset_service.common.exception.ResourceNotFoundException;
import com.reoagms.asset_service.sensor.dto.SensorRequest;
import com.reoagms.asset_service.sensor.dto.SensorResponse;
import com.reoagms.asset_service.sensor.mapper.SensorMapper;
import com.reoagms.asset_service.sensor.model.Sensor;
import com.reoagms.asset_service.sensor.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorServiceImplTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private AssetRepository assetRepository;

    private SensorServiceImpl sensorService;

    private final SensorMapper mapper = new SensorMapper();

    @BeforeEach
    void setUp() {
        sensorService = new SensorServiceImpl(sensorRepository, assetRepository, mapper);
    }

    private Asset asset(UUID id) {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setName("Inverter A1");
        return asset;
    }

    private SensorRequest validRequest(UUID assetId) {
        SensorRequest request = new SensorRequest();
        request.setName("Temp Sensor 1");
        request.setType(SensorType.TEMPERATURE);
        request.setStatus(SensorStatus.ACTIVE);
        request.setUnit("C");
        request.setMinThreshold(0.0);
        request.setMaxThreshold(85.0);
        request.setAssetId(assetId);
        return request;
    }

    @Test
    void createsSensorWhenAssetExistsAndThresholdsValid() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset(assetId)));
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(inv -> inv.getArgument(0));

        SensorResponse response = sensorService.create(validRequest(assetId));

        assertThat(response.getName()).isEqualTo("Temp Sensor 1");
        assertThat(response.getAssetId()).isEqualTo(assetId);
    }

    @Test
    void createThrowsWhenAssetMissing() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensorService.create(validRequest(assetId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThrowsWhenMinThresholdGreaterThanMax() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset(assetId)));

        SensorRequest request = validRequest(assetId);
        request.setMinThreshold(100.0);
        request.setMaxThreshold(10.0);

        assertThatThrownBy(() -> sensorService.create(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("minThreshold");

        verify(sensorRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenSerialNumberAlreadyUsed() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset(assetId)));
        when(sensorRepository.existsBySerialNumberIgnoreCase("SN-123")).thenReturn(true);

        SensorRequest request = validRequest(assetId);
        request.setSerialNumber("SN-123");

        assertThatThrownBy(() -> sensorService.create(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("serial number");
    }

    @Test
    void deleteThrowsWhenSensorMissing() {
        UUID id = UUID.randomUUID();
        when(sensorRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> sensorService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sensorRepository, never()).deleteById(any());
    }

    @Test
    void getByIdThrowsWhenSensorMissing() {
        UUID id = UUID.randomUUID();
        when(sensorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensorService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}

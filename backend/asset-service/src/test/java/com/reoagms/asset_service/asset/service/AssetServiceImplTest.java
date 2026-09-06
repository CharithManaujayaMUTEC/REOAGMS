package com.reoagms.asset_service.asset.service;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.asset.mapper.AssetMapper;
import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import com.reoagms.asset_service.common.exception.ResourceNotFoundException;
import com.reoagms.asset_service.facility.model.Facility;
import com.reoagms.asset_service.facility.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private FacilityRepository facilityRepository;

    private AssetServiceImpl assetService;

    private final AssetMapper mapper = new AssetMapper();

    @BeforeEach
    void setUp() {
        assetService = new AssetServiceImpl(assetRepository, facilityRepository, mapper);
    }

    private Facility facility(UUID id) {
        Facility facility = new Facility();
        facility.setId(id);
        facility.setName("Solar Farm 1");
        return facility;
    }

    private AssetRequest validRequest(UUID facilityId) {
        AssetRequest request = new AssetRequest();
        request.setName("Inverter A1");
        request.setType(AssetType.INVERTER);
        request.setStatus(AssetStatus.ACTIVE);
        request.setManufacturer("SunPower");
        request.setFacilityId(facilityId);
        return request;
    }

    @Test
    void createsAssetWhenFacilityExists() {
        UUID facilityId = UUID.randomUUID();
        Facility facility = facility(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(assetRepository.save(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));

        AssetResponse response = assetService.create(validRequest(facilityId));

        assertThat(response.getName()).isEqualTo("Inverter A1");
        assertThat(response.getFacilityId()).isEqualTo(facilityId);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void createThrowsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.create(validRequest(facilityId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facility not found");

        verify(assetRepository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenAssetMissing() {
        UUID id = UUID.randomUUID();
        when(assetRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asset not found");
    }

    @Test
    void getAllMapsEveryEntityToAResponse() {
        UUID facilityId = UUID.randomUUID();
        Asset asset = Asset.builder()
                .name("Panel 1")
                .type(AssetType.SOLAR_PANEL)
                .status(AssetStatus.ACTIVE)
                .facility(facility(facilityId))
                .build();

        when(assetRepository.findAll()).thenReturn(List.of(asset));

        List<AssetResponse> results = assetService.getAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Panel 1");
    }

    @Test
    void deleteThrowsWhenAssetDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(assetRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> assetService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(assetRepository, never()).deleteById(any());
    }

    @Test
    void deleteRemovesAssetWhenItExists() {
        UUID id = UUID.randomUUID();
        when(assetRepository.existsById(id)).thenReturn(true);

        assetService.delete(id);

        verify(assetRepository).deleteById(id);
    }

    @Test
    void updateThrowsWhenNewFacilityMissing() {
        UUID id = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        Asset existing = Asset.builder().name("Old name").facility(facility(UUID.randomUUID())).build();

        when(assetRepository.findById(id)).thenReturn(Optional.of(existing));
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.update(id, validRequest(facilityId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facility not found");
    }

}

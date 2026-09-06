package com.reoagms.maintenance_service;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.dto.WorkOrderResponse;
import com.reoagms.maintenance_service.mapper.WorkOrderMapper;
import com.reoagms.maintenance_service.model.WorkOrder;
import com.reoagms.maintenance_service.repository.WorkOrderRepository;
import com.reoagms.maintenance_service.service.WorkOrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderRepository repository;

    private final WorkOrderMapper mapper = new WorkOrderMapper();

    @InjectMocks
    private WorkOrderServiceImpl service;

    private WorkOrder existing;

    @BeforeEach
    void setUp() {
        service = new WorkOrderServiceImpl(repository, mapper);

        existing = WorkOrder.builder()
                .assetId(UUID.randomUUID())
                .title("Inspect inverter")
                .maintenanceType(MaintenanceType.PREVENTIVE)
                .status(WorkOrderStatus.CREATED)
                .build();
    }

    @Test
    void create_savesAndReturnsCreatedStatus() {
        WorkOrderRequest request = new WorkOrderRequest();
        request.setAssetId(UUID.randomUUID());
        request.setTitle("Inspect inverter");
        request.setMaintenanceType(MaintenanceType.PREVENTIVE);

        when(repository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderResponse response = service.create(request);

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.CREATED);
        assertThat(response.getTitle()).isEqualTo("Inspect inverter");
    }

    @Test
    void start_throwsWhenNotAssignedYet() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.start(id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getById_throwsEntityNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

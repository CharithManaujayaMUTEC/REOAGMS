package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.dto.MaintenanceReportResponse;
import com.reoagms.maintenance_service.mapper.WorkOrderMapper;
import com.reoagms.maintenance_service.model.WorkOrder;
import com.reoagms.maintenance_service.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceReportServiceImpl implements MaintenanceReportService {

    private final WorkOrderRepository repository;
    private final WorkOrderMapper mapper;

    @Override
    public MaintenanceReportResponse getSummary() {
        List<WorkOrder> all = repository.findAll();

        List<WorkOrder> overdue = repository.findByScheduledAtBeforeAndStatusNot(
                LocalDateTime.now(), WorkOrderStatus.COMPLETED)
                .stream()
                .filter(w -> w.getStatus() != WorkOrderStatus.CANCELLED)
                .toList();

        Map<java.util.UUID, Long> byAsset = all.stream()
                .collect(Collectors.groupingBy(WorkOrder::getAssetId, Collectors.counting()));

        Map<java.util.UUID, Long> byTechnician = all.stream()
                .filter(w -> w.getTechnicianId() != null)
                .collect(Collectors.groupingBy(WorkOrder::getTechnicianId, Collectors.counting()));

        return MaintenanceReportResponse.builder()
                .totalWorkOrders(all.size())
                .completedWorkOrders(all.stream().filter(w -> w.getStatus() == WorkOrderStatus.COMPLETED).count())
                .overdueWorkOrders(overdue.size())
                .preventiveCount(all.stream().filter(w -> w.getMaintenanceType() == MaintenanceType.PREVENTIVE).count())
                .correctiveCount(all.stream().filter(w -> w.getMaintenanceType() == MaintenanceType.CORRECTIVE).count())
                .workOrdersByAsset(byAsset)
                .workOrdersByTechnician(byTechnician)
                .overdueList(overdue.stream().map(mapper::toResponse).toList())
                .build();
    }
}

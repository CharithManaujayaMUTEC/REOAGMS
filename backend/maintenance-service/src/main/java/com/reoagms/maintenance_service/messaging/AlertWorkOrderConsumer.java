package com.reoagms.maintenance_service.messaging;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.repository.WorkOrderRepository;
import com.reoagms.maintenance_service.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

/** FR-11: opens a corrective work order whenever a HIGH or CRITICAL alert is raised. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertWorkOrderConsumer {
    private static final Set<String> ACTIONABLE = Set.of("HIGH", "CRITICAL");

    private final WorkOrderService workOrderService;
    private final WorkOrderRepository workOrderRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MaintenanceMessagingConfig.QUEUE)
    public void onAlertOpened(String payload) {
        AlertEvent event = objectMapper.readValue(payload, AlertEvent.class);
        if (event.alertId() == null || event.assetId() == null || !ACTIONABLE.contains(event.severity())) {
            return;
        }
        String marker = "[alert:" + event.alertId() + "]";
        if (workOrderRepository.existsByDescriptionContaining(marker)) {
            return; // redelivery of an event we have already handled
        }
        // An asset already being repaired does not need a second corrective order for the same fault.
        if (workOrderRepository.existsByAssetIdAndMaintenanceTypeAndStatusIn(event.assetId(), MaintenanceType.CORRECTIVE,
                List.of(WorkOrderStatus.CREATED, WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS))) {
            return;
        }
        WorkOrderRequest request = new WorkOrderRequest();
        request.setAssetId(event.assetId());
        request.setTitle(event.severity() + " alert: " + event.metricType() + " threshold breached");
        request.setDescription(event.message() + " (value=" + event.value() + ", threshold=" + event.threshold() + ") " + marker);
        request.setMaintenanceType(MaintenanceType.CORRECTIVE);
        request.setScheduledAt(event.occurredAt());
        workOrderService.create(request);
        log.info("Created corrective work order for alert {}", event.alertId());
    }
}

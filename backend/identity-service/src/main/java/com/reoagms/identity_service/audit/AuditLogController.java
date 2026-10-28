package com.reoagms.identity_service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditService auditService;

    public record AuditLogResponse(UUID id, String actor, String action, String entityType, String details,
                                   LocalDateTime createdAt) {
        static AuditLogResponse from(AuditLog log) {
            return new AuditLogResponse(log.getId(), log.getActor(), log.getAction(), log.getEntityType(),
                    log.getDetails(), log.getCreatedAt());
        }
    }

    @GetMapping
    public List<AuditLogResponse> latest() {
        return auditService.latest().stream().map(AuditLogResponse::from).toList();
    }
}

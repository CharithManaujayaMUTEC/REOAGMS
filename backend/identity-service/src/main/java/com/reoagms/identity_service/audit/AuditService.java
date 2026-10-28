package com.reoagms.identity_service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    /** Runs in its own transaction so auditing works from read-only callers and survives their rollback. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String entityType, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setDetails(details);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> latest() {
        return repository.findTop200ByOrderByCreatedAtDesc();
    }
}

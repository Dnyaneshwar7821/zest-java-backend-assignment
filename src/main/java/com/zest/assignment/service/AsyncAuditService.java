package com.zest.assignment.service;

import com.zest.assignment.entity.AuditLog;
import com.zest.assignment.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AsyncAuditService {

    private static final Logger log = LoggerFactory.getLogger(AsyncAuditService.class);

    private final AuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    public void logAudit(String action, String entityName, Long entityId, String performedBy, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .performedBy(performedBy != null ? performedBy : "SYSTEM")
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Async Audit Log written: Action={}, Entity={}, ID={}, User={}", action, entityName, entityId, performedBy);
        } catch (Exception e) {
            log.error("Failed to write async audit log for entity {} with id {}: {}", entityName, entityId, e.getMessage());
        }
    }
}

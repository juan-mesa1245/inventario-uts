package com.uts.inventario.service;

import com.uts.inventario.entity.AuditLog;
import com.uts.inventario.entity.User;
import com.uts.inventario.enums.AuditAction;
import com.uts.inventario.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, AuditAction action, String entityType, Long entityId,
                    String entityDescription, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .username(user != null ? user.getUsername() : "system")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityDescription(entityDescription)
                    .ipAddress(ipAddress)
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWithChanges(User user, AuditAction action, String entityType, Long entityId,
                               String entityDescription, String oldValues, String newValues,
                               String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .username(user != null ? user.getUsername() : "system")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityDescription(entityDescription)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress(ipAddress)
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error al registrar auditoría con cambios: {}", e.getMessage());
        }
    }
}

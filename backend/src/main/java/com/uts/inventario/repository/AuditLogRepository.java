package com.uts.inventario.repository;

import com.uts.inventario.entity.AuditLog;
import com.uts.inventario.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT l FROM AuditLog l
        WHERE (:userId IS NULL OR l.user.id = :userId)
        AND (:action IS NULL OR l.action = :action)
        AND (:entityType IS NULL OR l.entityType = :entityType)
        AND (:from IS NULL OR l.timestamp >= :from)
        AND (:to IS NULL OR l.timestamp <= :to)
        ORDER BY l.timestamp DESC
        """)
    Page<AuditLog> searchLogs(
        @Param("userId") Long userId,
        @Param("action") AuditAction action,
        @Param("entityType") String entityType,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );
}

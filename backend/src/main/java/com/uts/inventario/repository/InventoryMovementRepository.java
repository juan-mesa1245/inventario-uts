package com.uts.inventario.repository;

import com.uts.inventario.entity.InventoryMovement;
import com.uts.inventario.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByAssetId(Long assetId, Pageable pageable);

    @Query("""
        SELECT m FROM InventoryMovement m
        LEFT JOIN FETCH m.fromArea
        LEFT JOIN FETCH m.toArea
        LEFT JOIN FETCH m.fromUser
        LEFT JOIN FETCH m.toUser
        LEFT JOIN FETCH m.createdBy
        WHERE m.asset.id = :assetId
        ORDER BY m.movementDate DESC
        """)
    List<InventoryMovement> findByAssetIdOrderByMovementDateDesc(@Param("assetId") Long assetId);

    @Query("""
        SELECT m FROM InventoryMovement m
        WHERE (:assetId IS NULL OR m.asset.id = :assetId)
        AND (:movementType IS NULL OR m.movementType = :movementType)
        AND (:from IS NULL OR m.movementDate >= :from)
        AND (:to IS NULL OR m.movementDate <= :to)
        ORDER BY m.movementDate DESC
        """)
    Page<InventoryMovement> searchMovements(
        @Param("assetId") Long assetId,
        @Param("movementType") MovementType movementType,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query("SELECT m FROM InventoryMovement m ORDER BY m.movementDate DESC")
    List<InventoryMovement> findRecentMovements(Pageable pageable);
}

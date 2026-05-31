package com.uts.inventario.repository;

import com.uts.inventario.entity.Asset;
import com.uts.inventario.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySerialNumber(String serialNumber);

    Optional<Asset> findByCodigo(String codigo);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByAreaId(Long areaId, Pageable pageable);

    Page<Asset> findByAssignedUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT a FROM Asset a
        WHERE (CAST(:search AS String) IS NULL
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (CAST(:#{#status?.name()} AS String) IS NULL OR a.status = :status)
        AND (:areaId IS NULL OR a.area.id = :areaId)
        AND (:typeId IS NULL OR a.assetType.id = :typeId)
        """)
    Page<Asset> searchAssets(
        @Param("search") String search,
        @Param("status") AssetStatus status,
        @Param("areaId") Long areaId,
        @Param("typeId") Long typeId,
        Pageable pageable
    );

    long countByStatus(AssetStatus status);

    @Query("SELECT a.assetType.name as type, COUNT(a) as count FROM Asset a GROUP BY a.assetType.name")
    List<Object[]> countByAssetType();

    @Query("SELECT a.area.name as area, COUNT(a) as count FROM Asset a WHERE a.area IS NOT NULL GROUP BY a.area.name")
    List<Object[]> countByArea();

    List<Asset> findByAreaIdAndStatus(Long areaId, AssetStatus status);
}

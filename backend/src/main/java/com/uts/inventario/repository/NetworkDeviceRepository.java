package com.uts.inventario.repository;

import com.uts.inventario.entity.NetworkDevice;
import com.uts.inventario.enums.NetworkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {

    Optional<NetworkDevice> findByAssetId(Long assetId);

    Optional<NetworkDevice> findByIpAddress(String ipAddress);

    Optional<NetworkDevice> findByMacAddress(String macAddress);

    boolean existsByIpAddress(String ipAddress);

    boolean existsByMacAddress(String macAddress);

    List<NetworkDevice> findByNetworkStatus(NetworkStatus status);

    long countByNetworkStatus(NetworkStatus status);

    @Query("""
        SELECT nd FROM NetworkDevice nd
        JOIN nd.asset a
        WHERE (:search IS NULL
            OR LOWER(nd.ipAddress) LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(nd.hostname) LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(nd.macAddress) LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(a.name) LIKE LOWER(CONCAT('%',:search,'%')))
        AND (:status IS NULL OR nd.networkStatus = :status)
        """)
    Page<NetworkDevice> searchDevices(
        @Param("search") String search,
        @Param("status") NetworkStatus status,
        Pageable pageable
    );
}

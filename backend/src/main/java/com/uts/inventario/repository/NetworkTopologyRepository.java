package com.uts.inventario.repository;

import com.uts.inventario.entity.NetworkTopology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkTopologyRepository extends JpaRepository<NetworkTopology, Long> {

    @Query("SELECT t FROM NetworkTopology t WHERE t.sourceDevice.id = :deviceId OR t.targetDevice.id = :deviceId")
    List<NetworkTopology> findByDeviceId(@Param("deviceId") Long deviceId);

    List<NetworkTopology> findByIsActiveTrue();

    boolean existsBySourceDeviceIdAndTargetDeviceId(Long sourceId, Long targetId);
}

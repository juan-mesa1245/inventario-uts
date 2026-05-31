package com.uts.inventario.service;

import com.uts.inventario.dto.request.NetworkDeviceRequest;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.entity.Asset;
import com.uts.inventario.entity.NetworkDevice;
import com.uts.inventario.entity.NetworkTopology;
import com.uts.inventario.enums.AuditAction;
import com.uts.inventario.enums.NetworkStatus;
import com.uts.inventario.exception.BusinessException;
import com.uts.inventario.exception.ResourceNotFoundException;
import com.uts.inventario.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NetworkService {

    private final NetworkDeviceRepository networkDeviceRepository;
    private final NetworkTopologyRepository networkTopologyRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public PageResponse<NetworkDevice> findAllDevices(String search, NetworkStatus status, Pageable pageable) {
        Page<NetworkDevice> page = networkDeviceRepository.searchDevices(search, status, pageable);
        return PageResponse.from(page);
    }

    public NetworkDevice findDeviceById(Long id) {
        return networkDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo de red no encontrado con id: " + id));
    }

    public NetworkDevice findDeviceByAssetId(Long assetId) {
        return networkDeviceRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay configuración de red para el activo: " + assetId));
    }

    @Transactional
    public NetworkDevice createOrUpdateDevice(NetworkDeviceRequest request, String ipAddress) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado: " + request.getAssetId()));

        NetworkDevice device = networkDeviceRepository.findByAssetId(request.getAssetId())
                .orElse(new NetworkDevice());

        validateNetworkFields(request, device.getId());
        mapRequestToDevice(request, device, asset);

        NetworkDevice savedDevice = networkDeviceRepository.save(device);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user ->
            auditService.log(user, AuditAction.CREATE, "NetworkDevice", savedDevice.getId(),
                    "IP: " + savedDevice.getIpAddress(), ipAddress)
        );

        return savedDevice;
    }

    @Transactional
    public void deleteDevice(Long id, String ipAddress) {
        NetworkDevice device = networkDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo de red no encontrado: " + id));
        networkDeviceRepository.delete(device);
    }

    @Transactional
    public NetworkDevice updateStatus(Long id, NetworkStatus status) {
        NetworkDevice device = networkDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo de red no encontrado: " + id));
        device.setNetworkStatus(status);
        return networkDeviceRepository.save(device);
    }

    public List<NetworkTopology> getTopology() {
        return networkTopologyRepository.findByIsActiveTrue();
    }

    public List<NetworkTopology> getTopologyByDevice(Long deviceId) {
        return networkTopologyRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public NetworkTopology createTopologyLink(Long sourceId, Long targetId,
                                               String connectionType, String bandwidth) {
        if (networkTopologyRepository.existsBySourceDeviceIdAndTargetDeviceId(sourceId, targetId)) {
            throw new BusinessException("Ya existe una conexión entre estos dispositivos");
        }

        NetworkDevice source = networkDeviceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo origen no encontrado"));
        NetworkDevice target = networkDeviceRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo destino no encontrado"));

        NetworkTopology topology = NetworkTopology.builder()
                .sourceDevice(source)
                .targetDevice(target)
                .connectionType(com.uts.inventario.enums.ConnectionType.valueOf(connectionType))
                .bandwidth(bandwidth)
                .isActive(true)
                .build();

        return networkTopologyRepository.save(topology);
    }

    @Transactional
    public void deleteTopologyLink(Long id) {
        networkTopologyRepository.deleteById(id);
    }

    public Map<String, Object> getNetworkStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", networkDeviceRepository.count());
        stats.put("online", networkDeviceRepository.countByNetworkStatus(NetworkStatus.ONLINE));
        stats.put("offline", networkDeviceRepository.countByNetworkStatus(NetworkStatus.OFFLINE));
        stats.put("unknown", networkDeviceRepository.countByNetworkStatus(NetworkStatus.UNKNOWN));
        return stats;
    }

    private void validateNetworkFields(NetworkDeviceRequest request, Long excludeId) {
        if (request.getIpAddress() != null && !request.getIpAddress().isBlank()) {
            networkDeviceRepository.findByIpAddress(request.getIpAddress()).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new BusinessException("La dirección IP ya está asignada: " + request.getIpAddress());
                }
            });
        }
        if (request.getMacAddress() != null && !request.getMacAddress().isBlank()) {
            networkDeviceRepository.findByMacAddress(request.getMacAddress()).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new BusinessException("La dirección MAC ya está registrada: " + request.getMacAddress());
                }
            });
        }
    }

    private void mapRequestToDevice(NetworkDeviceRequest request, NetworkDevice device, Asset asset) {
        device.setAsset(asset);
        device.setIpAddress(request.getIpAddress());
        device.setMacAddress(request.getMacAddress() != null ?
                request.getMacAddress().toUpperCase() : null);
        device.setHostname(request.getHostname());
        device.setSubnetMask(request.getSubnetMask());
        device.setGateway(request.getGateway());
        device.setDnsPrimary(request.getDnsPrimary());
        device.setDnsSecondary(request.getDnsSecondary());
        device.setVlanId(request.getVlanId());
        device.setPortNumber(request.getPortNumber());
        device.setIsDhcp(request.getIsDhcp() != null ? request.getIsDhcp() : false);
        device.setNetworkStatus(request.getNetworkStatus() != null ?
                request.getNetworkStatus() : NetworkStatus.UNKNOWN);
        device.setLocationDetail(request.getLocationDetail());
        device.setFirmwareVersion(request.getFirmwareVersion());
    }
}

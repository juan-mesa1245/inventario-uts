package com.uts.inventario.service;

import com.uts.inventario.dto.request.AssetRequest;
import com.uts.inventario.dto.response.AssetResponse;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.entity.*;
import com.uts.inventario.enums.AssetStatus;
import com.uts.inventario.enums.AuditAction;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public PageResponse<AssetResponse> findAll(String search, AssetStatus status,
                                                Long areaId, Long typeId, Pageable pageable) {
        Page<Asset> page = assetRepository.searchAssets(search, status, areaId, typeId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public AssetResponse findById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));
        return toResponse(asset);
    }

    public AssetResponse findByCodigo(String codigo) {
        Asset asset = assetRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no válido"));
        return toResponse(asset);
    }

    @Transactional
    public AssetResponse create(AssetRequest request, String ipAddress) {
        validateUniqueFields(request, null);

        Asset asset = new Asset();
        mapRequestToEntity(request, asset);
        asset.setCreatedBy(getCurrentUser());

        asset = assetRepository.save(asset);

        auditService.log(getCurrentUser(), AuditAction.CREATE, "Asset", asset.getId(),
                asset.getName(), ipAddress);

        return toResponse(asset);
    }

    @Transactional
    public AssetResponse update(Long id, AssetRequest request, String ipAddress) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));

        validateUniqueFields(request, id);
        String oldValues = toJsonSummary(asset);

        mapRequestToEntity(request, asset);
        asset = assetRepository.save(asset);

        auditService.logWithChanges(getCurrentUser(), AuditAction.UPDATE, "Asset", asset.getId(),
                asset.getName(), oldValues, toJsonSummary(asset), ipAddress);

        return toResponse(asset);
    }

    @Transactional
    public void delete(Long id, String ipAddress) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));

        auditService.log(getCurrentUser(), AuditAction.DELETE, "Asset", id, asset.getName(), ipAddress);
        assetRepository.delete(asset);
    }

    @Transactional
    public AssetResponse changeStatus(Long id, AssetStatus newStatus, String ipAddress) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));

        AssetStatus oldStatus = asset.getStatus();
        asset.setStatus(newStatus);
        asset = assetRepository.save(asset);

        auditService.logWithChanges(getCurrentUser(), AuditAction.UPDATE, "Asset", id,
                asset.getName(), "status:" + oldStatus, "status:" + newStatus, ipAddress);

        return toResponse(asset);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", assetRepository.count());
        stats.put("active", assetRepository.countByStatus(AssetStatus.ACTIVE));
        stats.put("maintenance", assetRepository.countByStatus(AssetStatus.MAINTENANCE));
        stats.put("retired", assetRepository.countByStatus(AssetStatus.RETIRED));
        stats.put("lost", assetRepository.countByStatus(AssetStatus.LOST));
        stats.put("byType", assetRepository.countByAssetType());
        stats.put("byArea", assetRepository.countByArea());
        return stats;
    }

    private void validateUniqueFields(AssetRequest request, Long excludeId) {
        if (request.getSerialNumber() != null) {
            assetRepository.findBySerialNumber(request.getSerialNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new BusinessException("Ya existe un activo con el número de serie: " + request.getSerialNumber());
                }
            });
        }
        if (request.getCodigo() != null) {
            assetRepository.findByCodigo(request.getCodigo()).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new BusinessException("Ya existe un activo con el código: " + request.getCodigo());
                }
            });
        }
    }

    private void mapRequestToEntity(AssetRequest request, Asset asset) {
        asset.setName(request.getName());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setCodigo(request.getCodigo());
        asset.setStatus(request.getStatus() != null ? request.getStatus() : AssetStatus.ACTIVE);
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchasePrice(request.getPurchasePrice());
        asset.setWarrantyExpiry(request.getWarrantyExpiry());
        asset.setSpecifications(request.getSpecifications());
        asset.setNotes(request.getNotes());

        if (request.getAssetTypeId() != null) {
            AssetType type = assetTypeRepository.findById(request.getAssetTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de activo no encontrado"));
            asset.setAssetType(type);
        }

        if (request.getAreaId() != null) {
            Area area = areaRepository.findById(request.getAreaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada"));
            asset.setArea(area);
        }

        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            asset.setAssignedUser(user);
        } else {
            asset.setAssignedUser(null);
        }
    }

    private AssetResponse toResponse(Asset asset) {
        AssetResponse.AssetResponseBuilder builder = AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .brand(asset.getBrand())
                .model(asset.getModel())
                .serialNumber(asset.getSerialNumber())
                .codigo(asset.getCodigo())
                .status(asset.getStatus())
                .statusLabel(asset.getStatus().getLabel())
                .purchaseDate(asset.getPurchaseDate())
                .purchasePrice(asset.getPurchasePrice())
                .warrantyExpiry(asset.getWarrantyExpiry())
                .specifications(asset.getSpecifications())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt());

        if (asset.getAssetType() != null) {
            builder.assetTypeId(asset.getAssetType().getId())
                    .assetTypeName(asset.getAssetType().getName())
                    .assetTypeCategory(asset.getAssetType().getCategory());
        }

        if (asset.getArea() != null) {
            builder.areaId(asset.getArea().getId())
                    .areaName(asset.getArea().getName())
                    .areaLocation(asset.getArea().getLocation());
        }

        if (asset.getAssignedUser() != null) {
            builder.assignedUserId(asset.getAssignedUser().getId())
                    .assignedUserName(asset.getAssignedUser().getFullName())
                    .assignedUserEmail(asset.getAssignedUser().getEmail());
        }

        if (asset.getNetworkDevice() != null) {
            NetworkDevice nd = asset.getNetworkDevice();
            builder.hasNetworkDevice(true)
                    .ipAddress(nd.getIpAddress())
                    .macAddress(nd.getMacAddress())
                    .networkStatus(nd.getNetworkStatus().name());
        } else {
            builder.hasNetworkDevice(false);
        }

        return builder.build();
    }

    private String toJsonSummary(Asset asset) {
        return String.format("{name:'%s',status:'%s',area:'%s'}",
                asset.getName(), asset.getStatus(),
                asset.getArea() != null ? asset.getArea().getName() : "N/A");
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}

package com.uts.inventario.service;

import com.uts.inventario.dto.request.InventoryMovementRequest;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.entity.*;
import com.uts.inventario.enums.AssetStatus;
import com.uts.inventario.enums.AuditAction;
import com.uts.inventario.enums.MovementType;
import com.uts.inventario.exception.ResourceNotFoundException;
import com.uts.inventario.entity.AssetType;
import com.uts.inventario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final AssetRepository assetRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final AuditService auditService;

    public PageResponse<InventoryMovement> findAll(Long assetId, MovementType type,
                                                    LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<InventoryMovement> page = movementRepository.searchMovements(assetId, type, from, to, pageable);
        return PageResponse.from(page);
    }

    public List<InventoryMovement> findByAsset(Long assetId) {
        return movementRepository.findByAssetIdOrderByMovementDateDesc(assetId);
    }

    public List<InventoryMovement> findRecent(int limit) {
        return movementRepository.findRecentMovements(PageRequest.of(0, limit));
    }

    public InventoryMovement findById(Long id) {
        return movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    @Transactional
    public InventoryMovement registerMovement(InventoryMovementRequest request, String ipAddress) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado: " + request.getAssetId()));

        User currentUser = getCurrentUser();

        InventoryMovement movement = InventoryMovement.builder()
                .asset(asset)
                .movementType(request.getMovementType())
                .movementDate(request.getMovementDate() != null ?
                        request.getMovementDate() : LocalDateTime.now())
                .reason(request.getReason())
                .notes(request.getNotes())
                .referenceNumber(request.getReferenceNumber())
                .createdBy(currentUser)
                .build();

        if (request.getAssetTypeId() != null) {
            AssetType assetType = assetTypeRepository.findById(request.getAssetTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de activo no encontrado"));
            asset.setAssetType(assetType);
        }

        if (request.getFromAreaId() != null) {
            movement.setFromArea(areaRepository.findById(request.getFromAreaId()).orElse(null));
        }
        if (request.getToAreaId() != null) {
            Area toArea = areaRepository.findById(request.getToAreaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área destino no encontrada"));
            movement.setToArea(toArea);
            asset.setArea(toArea);
        }
        if (request.getFromUserId() != null) {
            movement.setFromUser(userRepository.findById(request.getFromUserId()).orElse(null));
        }
        if (request.getToUserId() != null) {
            User toUser = userRepository.findById(request.getToUserId()).orElse(null);
            movement.setToUser(toUser);
            asset.setAssignedUser(toUser);
        }

        updateAssetStatusOnMovement(asset, request.getMovementType());
        assetRepository.save(asset);

        movement = movementRepository.save(movement);

        auditService.log(currentUser, AuditAction.CREATE, "InventoryMovement", movement.getId(),
                request.getMovementType().getLabel() + " - " + asset.getName(), ipAddress);

        return movement;
    }

    private void updateAssetStatusOnMovement(Asset asset, MovementType movementType) {
        switch (movementType) {
            case MAINTENANCE_IN -> asset.setStatus(AssetStatus.MAINTENANCE);
            case MAINTENANCE_OUT, RETURN -> asset.setStatus(AssetStatus.ACTIVE);
            case ENTRY -> asset.setStatus(AssetStatus.ACTIVE);
            default -> { }
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}

package com.uts.inventario.dto.request;

import com.uts.inventario.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryMovementRequest {

    @NotNull(message = "El activo es obligatorio")
    private Long assetId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType movementType;

    private LocalDateTime movementDate;

    private Long assetTypeId;

    private Long fromAreaId;

    private Long toAreaId;

    private Long fromUserId;

    private Long toUserId;

    private String reason;

    private String notes;

    private String referenceNumber;
}

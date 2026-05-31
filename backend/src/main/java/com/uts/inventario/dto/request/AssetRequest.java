package com.uts.inventario.dto.request;

import com.uts.inventario.enums.AssetStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetRequest {

    @NotBlank(message = "El nombre del activo es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 50)
    private String brand;

    @Size(max = 100)
    private String model;

    @Size(max = 100)
    private String serialNumber;

    @Size(max = 50)
    private String codigo;

    private AssetStatus status;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal purchasePrice;

    private LocalDate warrantyExpiry;

    private String specifications;

    private String notes;

    private Long assetTypeId;

    private Long areaId;

    private Long assignedUserId;
}

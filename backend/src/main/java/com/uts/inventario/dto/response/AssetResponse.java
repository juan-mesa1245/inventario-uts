package com.uts.inventario.dto.response;

import com.uts.inventario.enums.AssetStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AssetResponse {

    private Long id;
    private String name;
    private String brand;
    private String model;
    private String serialNumber;
    private String codigo;
    private AssetStatus status;
    private String statusLabel;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate warrantyExpiry;
    private String specifications;
    private String notes;

    private Long assetTypeId;
    private String assetTypeName;
    private String assetTypeCategory;

    private Long areaId;
    private String areaName;
    private String areaLocation;

    private Long assignedUserId;
    private String assignedUserName;
    private String assignedUserEmail;

    private Boolean hasNetworkDevice;
    private String ipAddress;
    private String macAddress;
    private String networkStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

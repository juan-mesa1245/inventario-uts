package com.uts.inventario.controller;

import com.uts.inventario.dto.request.InventoryMovementRequest;
import com.uts.inventario.dto.response.ApiResponse;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.entity.InventoryMovement;
import com.uts.inventario.enums.MovementType;
import com.uts.inventario.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión de movimientos de inventario")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/movements")
    @Operation(summary = "Listar movimientos de inventario con filtros")
    public ResponseEntity<ApiResponse<PageResponse<InventoryMovement>>> findAll(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("movementDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.findAll(assetId, type, from, to, pageable)));
    }

    @GetMapping("/movements/{id}")
    @Operation(summary = "Obtener movimiento por ID")
    public ResponseEntity<ApiResponse<InventoryMovement>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findById(id)));
    }

    @GetMapping("/movements/asset/{assetId}")
    @Operation(summary = "Historial de movimientos de un activo")
    public ResponseEntity<ApiResponse<List<InventoryMovement>>> findByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findByAsset(assetId)));
    }

    @GetMapping("/movements/recent")
    @Operation(summary = "Últimos movimientos registrados")
    public ResponseEntity<ApiResponse<List<InventoryMovement>>> findRecent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findRecent(limit)));
    }

    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Registrar movimiento de inventario")
    public ResponseEntity<ApiResponse<InventoryMovement>> registerMovement(
            @Valid @RequestBody InventoryMovementRequest request,
            HttpServletRequest httpRequest) {

        InventoryMovement movement = inventoryService.registerMovement(request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movimiento registrado exitosamente", movement));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

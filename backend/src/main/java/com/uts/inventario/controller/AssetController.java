package com.uts.inventario.controller;

import com.uts.inventario.dto.request.AssetRequest;
import com.uts.inventario.dto.response.ApiResponse;
import com.uts.inventario.dto.response.AssetResponse;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.enums.AssetStatus;
import com.uts.inventario.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Tag(name = "Activos Tecnológicos", description = "Gestión de activos TI")
@SecurityRequirement(name = "bearerAuth")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @Operation(summary = "Listar activos con filtros y paginación")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(ApiResponse.success(
                assetService.findAll(search, status, areaId, typeId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener activo por ID")
    public ResponseEntity<ApiResponse<AssetResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findById(id)));
    }

    @GetMapping("/code/{codigo}")
    @Operation(summary = "Obtener activo por código")
    public ResponseEntity<ApiResponse<AssetResponse>> findByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findByCodigo(codigo)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Registrar nuevo activo")
    public ResponseEntity<ApiResponse<AssetResponse>> create(
            @Valid @RequestBody AssetRequest request,
            HttpServletRequest httpRequest) {

        AssetResponse response = assetService.create(request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activo registrado exitosamente", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Actualizar activo existente")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request,
            HttpServletRequest httpRequest) {

        AssetResponse response = assetService.update(id, request, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Activo actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar activo (solo ADMIN)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        assetService.delete(id, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Activo eliminado exitosamente", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Cambiar estado de un activo")
    public ResponseEntity<ApiResponse<AssetResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam AssetStatus status,
            HttpServletRequest httpRequest) {

        AssetResponse response = assetService.changeStatus(id, status, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Estado actualizado exitosamente", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas del dashboard de activos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(assetService.getDashboardStats()));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

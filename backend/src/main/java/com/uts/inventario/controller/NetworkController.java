package com.uts.inventario.controller;

import com.uts.inventario.dto.request.NetworkDeviceRequest;
import com.uts.inventario.dto.response.ApiResponse;
import com.uts.inventario.dto.response.PageResponse;
import com.uts.inventario.entity.NetworkDevice;
import com.uts.inventario.entity.NetworkTopology;
import com.uts.inventario.enums.NetworkStatus;
import com.uts.inventario.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/network")
@RequiredArgsConstructor
@Tag(name = "Redes y Comunicaciones", description = "Gestión de dispositivos de red y topología")
@SecurityRequirement(name = "bearerAuth")
public class NetworkController {

    private final NetworkService networkService;

    @GetMapping("/devices")
    @Operation(summary = "Listar dispositivos de red con filtros")
    public ResponseEntity<ApiResponse<PageResponse<NetworkDevice>>> findAllDevices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) NetworkStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                networkService.findAllDevices(search, status, pageable)));
    }

    @GetMapping("/devices/{id}")
    @Operation(summary = "Obtener dispositivo de red por ID")
    public ResponseEntity<ApiResponse<NetworkDevice>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(networkService.findDeviceById(id)));
    }

    @GetMapping("/devices/by-asset/{assetId}")
    @Operation(summary = "Obtener configuración de red de un activo")
    public ResponseEntity<ApiResponse<NetworkDevice>> findByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(ApiResponse.success(networkService.findDeviceByAssetId(assetId)));
    }

    @PostMapping("/devices")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Registrar o actualizar dispositivo de red")
    public ResponseEntity<ApiResponse<NetworkDevice>> createOrUpdate(
            @Valid @RequestBody NetworkDeviceRequest request,
            HttpServletRequest httpRequest) {

        NetworkDevice device = networkService.createOrUpdateDevice(request, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Configuración de red guardada", device));
    }

    @DeleteMapping("/devices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Eliminar configuración de red")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        networkService.deleteDevice(id, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Dispositivo eliminado", null));
    }

    @PatchMapping("/devices/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Actualizar estado de un dispositivo de red")
    public ResponseEntity<ApiResponse<NetworkDevice>> updateStatus(
            @PathVariable Long id,
            @RequestParam NetworkStatus status) {

        return ResponseEntity.ok(ApiResponse.success(networkService.updateStatus(id, status)));
    }

    @GetMapping("/topology")
    @Operation(summary = "Obtener topología de red completa")
    public ResponseEntity<ApiResponse<List<NetworkTopology>>> getTopology() {
        return ResponseEntity.ok(ApiResponse.success(networkService.getTopology()));
    }

    @GetMapping("/topology/device/{deviceId}")
    @Operation(summary = "Obtener conexiones de un dispositivo específico")
    public ResponseEntity<ApiResponse<List<NetworkTopology>>> getDeviceTopology(
            @PathVariable Long deviceId) {
        return ResponseEntity.ok(ApiResponse.success(networkService.getTopologyByDevice(deviceId)));
    }

    @PostMapping("/topology")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Crear enlace entre dos dispositivos de red")
    public ResponseEntity<ApiResponse<NetworkTopology>> createLink(
            @RequestParam Long sourceId,
            @RequestParam Long targetId,
            @RequestParam String connectionType,
            @RequestParam(required = false) String bandwidth) {

        NetworkTopology topology = networkService.createTopologyLink(sourceId, targetId, connectionType, bandwidth);
        return ResponseEntity.ok(ApiResponse.success("Enlace creado exitosamente", topology));
    }

    @DeleteMapping("/topology/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Eliminar enlace de topología")
    public ResponseEntity<ApiResponse<Void>> deleteLink(@PathVariable Long id) {
        networkService.deleteTopologyLink(id);
        return ResponseEntity.ok(ApiResponse.success("Enlace eliminado", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de red")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(networkService.getNetworkStats()));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

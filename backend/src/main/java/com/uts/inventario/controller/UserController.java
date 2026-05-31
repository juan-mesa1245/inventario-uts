package com.uts.inventario.controller;

import com.uts.inventario.dto.response.ApiResponse;
import com.uts.inventario.entity.Area;
import com.uts.inventario.entity.AssetType;
import com.uts.inventario.entity.User;
import com.uts.inventario.exception.ResourceNotFoundException;
import com.uts.inventario.repository.AreaRepository;
import com.uts.inventario.repository.AssetTypeRepository;
import com.uts.inventario.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catálogos", description = "Endpoints de catálogos: usuarios, áreas y tipos de activos")
public class UserController {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final AssetTypeRepository assetTypeRepository;

    // ---- Usuarios ----

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Listar usuarios activos")
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAllActive()));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/desactivar usuario")
    public ResponseEntity<ApiResponse<String>> toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        String state = user.getIsActive() ? "activado" : "desactivado";
        return ResponseEntity.ok(ApiResponse.success("Usuario " + state, null));
    }

    // ---- Áreas ----

    @GetMapping("/areas")
    @Operation(summary = "Listar áreas activas")
    public ResponseEntity<ApiResponse<List<Area>>> getAreas() {
        return ResponseEntity.ok(ApiResponse.success(areaRepository.findByIsActiveTrueOrderByName()));
    }

    @GetMapping("/areas/{id}")
    @Operation(summary = "Obtener área por ID")
    public ResponseEntity<ApiResponse<Area>> getArea(@PathVariable Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada: " + id));
        return ResponseEntity.ok(ApiResponse.success(area));
    }

    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva área")
    public ResponseEntity<ApiResponse<Area>> createArea(@RequestBody Area area) {
        area.setIsActive(true);
        return ResponseEntity.ok(ApiResponse.success("Área creada", areaRepository.save(area)));
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar área")
    public ResponseEntity<ApiResponse<Area>> updateArea(@PathVariable Long id, @RequestBody Area request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada: " + id));
        area.setName(request.getName());
        area.setDescription(request.getDescription());
        area.setLocation(request.getLocation());
        area.setFloor(request.getFloor());
        area.setBuilding(request.getBuilding());
        return ResponseEntity.ok(ApiResponse.success("Área actualizada", areaRepository.save(area)));
    }

    // ---- Tipos de activos ----

    @GetMapping("/asset-types")
    @Operation(summary = "Listar tipos de activos")
    public ResponseEntity<ApiResponse<List<AssetType>>> getAssetTypes() {
        return ResponseEntity.ok(ApiResponse.success(assetTypeRepository.findByOrderByName()));
    }

    @GetMapping("/asset-types/category/{category}")
    @Operation(summary = "Listar tipos de activos por categoría")
    public ResponseEntity<ApiResponse<List<AssetType>>> getAssetTypesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                assetTypeRepository.findByCategoryOrderByName(category.toUpperCase())));
    }

    @PostMapping("/asset-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear tipo de activo")
    public ResponseEntity<ApiResponse<AssetType>> createAssetType(@RequestBody AssetType assetType) {
        return ResponseEntity.ok(ApiResponse.success("Tipo creado", assetTypeRepository.save(assetType)));
    }
}

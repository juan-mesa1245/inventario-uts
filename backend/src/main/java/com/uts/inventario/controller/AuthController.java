package com.uts.inventario.controller;

import com.uts.inventario.dto.request.LoginRequest;
import com.uts.inventario.dto.request.RegisterRequest;
import com.uts.inventario.dto.response.ApiResponse;
import com.uts.inventario.dto.response.AuthResponse;
import com.uts.inventario.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Sesión iniciada exitosamente", response));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar nuevo usuario (solo ADMIN)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario autenticado")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            jakarta.servlet.http.HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return ResponseEntity.ok(ApiResponse.success(username));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

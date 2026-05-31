package com.uts.inventario.service;

import com.uts.inventario.dto.request.LoginRequest;
import com.uts.inventario.dto.request.RegisterRequest;
import com.uts.inventario.dto.response.AuthResponse;
import com.uts.inventario.entity.Role;
import com.uts.inventario.entity.User;
import com.uts.inventario.enums.AuditAction;
import com.uts.inventario.enums.RoleName;
import com.uts.inventario.exception.BusinessException;
import com.uts.inventario.repository.RoleRepository;
import com.uts.inventario.repository.UserRepository;
import com.uts.inventario.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(user);

        auditService.log(user, AuditAction.LOGIN, "User", user.getId(), user.getUsername(), ipAddress);

        return buildAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario ya está en uso: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El correo electrónico ya está registrado: " + request.getEmail());
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .documentNumber(request.getDocumentNumber())
                .roles(roles)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user);

        return buildAuthResponse(user, token);
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of(roleRepository.findByName(RoleName.ROLE_USUARIO)
                    .orElseThrow(() -> new BusinessException("Rol por defecto no encontrado")));
        }

        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BusinessException("Rol no encontrado: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .build();
    }
}

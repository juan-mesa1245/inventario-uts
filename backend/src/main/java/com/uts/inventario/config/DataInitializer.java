package com.uts.inventario.config;

import com.uts.inventario.entity.Role;
import com.uts.inventario.entity.User;
import com.uts.inventario.enums.RoleName;
import com.uts.inventario.repository.RoleRepository;
import com.uts.inventario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {
            // Crear roles si no existen (por si Flyway no los insertó)
            ensureRole(RoleName.ROLE_ADMIN, "Administrador con acceso total al sistema");
            ensureRole(RoleName.ROLE_TECNICO, "Técnico de soporte");
            ensureRole(RoleName.ROLE_USUARIO, "Usuario estándar");

            // Actualizar contraseña del admin (siempre re-codifica al arrancar)
            userRepository.findByUsername("admin").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("Admin@123"));
                    admin.setIsActive(true);
                    userRepository.save(admin);
                    log.info("✓ Contraseña del admin actualizada correctamente");
                },
                () -> {
                    // Si no existe el usuario admin, crearlo
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow();
                    User admin = User.builder()
                            .username("admin")
                            .email("admin@uts.edu.co")
                            .password(passwordEncoder.encode("Admin@123"))
                            .fullName("Administrador del Sistema")
                            .phone("6076543210")
                            .roles(Set.of(adminRole))
                            .isActive(true)
                            .build();
                    userRepository.save(admin);
                    log.info("✓ Usuario admin creado: admin / Admin@123");
                }
            );

            // Actualizar contraseña del técnico de prueba
            userRepository.findByUsername("tecnico1").ifPresent(tecnico -> {
                tecnico.setPassword(passwordEncoder.encode("Tecnico@123"));
                userRepository.save(tecnico);
                log.info("✓ Contraseña del técnico actualizada correctamente");
            });
        };
    }

    private void ensureRole(RoleName name, String description) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder().name(name).description(description).build();
            return roleRepository.save(role);
        });
    }
}

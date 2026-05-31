package com.uts.inventario;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(
    info = @Info(
        title = "Sistema de Inventario y Control de Activos Tecnológicos - UTS",
        version = "1.0.0",
        description = "API REST para la gestión integral de la infraestructura TI de las " +
                      "Unidades Tecnológicas de Santander. Módulos: Activos, Inventario, " +
                      "Redes & Comunicaciones, Usuarios y Auditoría.",
        contact = @Contact(
            name = "Unidades Tecnológicas de Santander",
            email = "sistemasinformacion@uts.edu.co"
        )
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}

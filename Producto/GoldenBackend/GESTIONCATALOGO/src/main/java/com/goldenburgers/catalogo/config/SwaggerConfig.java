package com.goldenburgers.catalogo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI con autenticación JWT Bearer
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestión de Catálogo - Golden Burgers")
                        .version("1.0")
                        .description("API para gestión completa del catálogo de productos y categorías.\n\n" +
                                "**AUTENTICACIÓN:**\n" +
                                "1. Obtén el token desde: `POST /auth/login` del API Gateway (puerto 8080)\n" +
                                "2. Click en 'Authorize' y pega el token (sin 'Bearer')\n" +
                                "3. Los endpoints públicos (GET productos/categorías) no requieren token\n" +
                                "4. Crear/Editar/Eliminar requieren roles específicos (ver cada endpoint)"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido del API Gateway")));
    }
}

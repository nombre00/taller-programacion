package com.GoldenBurger.gestionContacto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para GESTIONCONTACTO
 * Permite probar endpoints protegidos con el token JWT interno
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Golden Burgers - Gestión de Contacto API")
                        .description("Microservicio para gestión de mensajes de contacto y comunicación con clientes")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Golden Burgers Team")
                                .email("contact@goldenburgers.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT interno generado por el API Gateway. " +
                                        "Para obtenerlo, haz login en el API Gateway (POST /api/auth/login) " +
                                        "y copia el 'internalToken' aquí (sin 'Bearer').")));
    }
}

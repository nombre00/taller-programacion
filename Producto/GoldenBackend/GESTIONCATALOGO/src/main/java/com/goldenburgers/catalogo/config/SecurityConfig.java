package com.goldenburgers.catalogo.config;

import com.goldenburgers.catalogo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad con JWT
 * - Valida tokens JWT generados por el API Gateway
 * - Endpoints públicos: GET de productos y categorías, /health, Swagger
 * - Endpoints protegidos: POST, PUT, DELETE requieren autenticación y roles
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize en los controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // CORS: Solo permitir peticiones desde el API Gateway
            .cors(cors -> cors.disable()) // Gateway maneja CORS
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ===== RUTAS PÚBLICAS (sin autenticación) =====
                // Health check
                .requestMatchers("/health", "/api/catalogo/health").permitAll()
                // Actuator
                .requestMatchers("/actuator/**").permitAll()
                // Swagger UI y documentación
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                // GET de productos (listar, buscar, ver detalle) - PÚBLICO
                .requestMatchers(
                        "/api/catalogo/productos",           // GET todos disponibles
                        "/api/catalogo/productos/todos",     // GET todos (incluye no disponibles)
                        "/api/catalogo/productos/*",         // GET por ID
                        "/api/catalogo/productos/categoria/*", // GET por categoría
                        "/api/catalogo/productos/buscar"     // GET buscar por nombre
                ).permitAll()
                // GET de categorías - PÚBLICO
                .requestMatchers(
                        "/api/catalogo/categorias",
                        "/api/catalogo/categorias/*"
                ).permitAll()
                
                // ===== RUTAS PROTEGIDAS =====
                // Resto de operaciones requieren autenticación
                // Los permisos específicos se controlan con @PreAuthorize en el controller
                .anyRequest().authenticated()
            )
            // Agregar filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

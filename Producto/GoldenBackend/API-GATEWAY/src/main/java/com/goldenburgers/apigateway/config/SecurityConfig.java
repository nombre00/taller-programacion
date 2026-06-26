package com.goldenburgers.apigateway.config;

import com.goldenburgers.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de Spring Security para el API Gateway
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ===== RUTAS PÚBLICAS (sin autenticación) =====

                        // Sistema y documentación
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // GESTIÓN USUARIO - Endpoints públicos
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/firebase/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/exists/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll() // Registro de clientes
                        .requestMatchers(HttpMethod.GET, "/api/roles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ciudades/**").permitAll()

                        // GESTIÓN CATÁLOGO - Endpoints públicos (GET solamente)
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**").permitAll()

                        // GESTIÓN CONTACTO - Formulario de contacto público
                        .requestMatchers(HttpMethod.POST, "/api/mensajes").permitAll()

                        // GESTIÓN PEDIDO - Webhook de Mercado Pago
                        .requestMatchers(HttpMethod.POST, "/api/pagos/webhook").permitAll()

                        // ===== TODAS LAS DEMÁS RUTAS REQUIEREN AUTENTICACIÓN =====
                        .anyRequest().authenticated()
                )
                // Agregar el filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (ajustar según tu frontend)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",      // Vite local
                "http://localhost:3000",      // React local alternativo
                "http://localhost:4200",      // Angular local
                "http://127.0.0.1:5173",
                "*"                           // En desarrollo, permitir todo (CAMBIAR EN PRODUCCIÓN)
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(false); // Cambiar a true si necesitas cookies
        
        // Tiempo de caché para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

package com.goldenburgers.apigateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;


@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos
        // DESARROLLO: localhost
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://localhost:5174");

        // PRODUCCIÓN: S3 Bucket
        // Reemplazar con la URL real de tu bucket S3
        config.addAllowedOrigin("http://golden-burgers-s3.s3-website-us-east-1.amazonaws.com/");
        config.addAllowedOrigin("http://golden-burgers-s3.s3-website-us-east-1.amazonaws.com");
        config.addAllowedOrigin("https://golden-burgers-frontend.vercel.app");

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Headers expuestos (para que el frontend pueda leerlos)
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));

        // Permitir envío de credenciales (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Cache de la configuración CORS (1 hora)
        config.setMaxAge(3600L);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
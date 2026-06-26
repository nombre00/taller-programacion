package com.goldenburgers.apigateway.controller;

import com.goldenburgers.apigateway.dto.LoginRequest;
import com.goldenburgers.apigateway.dto.LoginResponse;
import com.goldenburgers.apigateway.dto.RefreshTokenRequest;
import com.goldenburgers.apigateway.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación
 * Endpoints públicos para login y refresh de tokens
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permitir CORS para autenticación
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * POST /api/auth/login
     * Autentica con token de Firebase y retorna token JWT interno
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Intento de login con token de Firebase");
            
            LoginResponse response = authenticationService.authenticateWithFirebase(
                    request.getFirebaseToken()
            );
            
            log.info("Login exitoso para usuario: {}", response.getUser().getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Error de autenticación: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/refresh
     * Refresca el token JWT interno
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.info("Intento de refresh de token");
            
            LoginResponse response = authenticationService.refreshToken(request.getToken());
            
            log.info("Token refrescado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al refrescar token: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Error al refrescar token: " + e.getMessage()));
        }
    }

    /**
     * GET /api/auth/health
     * Verificar que el servicio de autenticación está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running");
    }

    /**
     * Clase interna para respuestas de error
     */
    private record ErrorResponse(String message) {}
}

package com.goldenburgers.apigateway.service;

import com.goldenburgers.apigateway.dto.LoginResponse;
import com.goldenburgers.apigateway.dto.UserDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;

/**
 * Servicio de autenticación que valida tokens de Firebase
 * y genera tokens JWT internos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;

    @Value("${microservices.gestion-usuario.url}")
    private String gestionUsuarioUrl;

    /**
     * Autentica al usuario con el token de Firebase y genera token interno
     */
    public LoginResponse authenticateWithFirebase(String firebaseToken) {
        try {
            // 1. Validar el token de Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            log.info("Token de Firebase validado para usuario: {}", email);

            // 2. Consultar el rol del usuario desde GESTIONUSUARIO
            UserDTO userInfo = getUserInfoFromMicroservice(uid);

            // 3. Generar token JWT interno con los claims
            String internalToken = jwtService.generateToken(
                    uid,
                    email,
                    userInfo.getRolId(),
                    userInfo.getRolNombre()
            );

            log.info("Token interno generado para usuario: {} con rol: {}", email, userInfo.getRolNombre());

            // 4. Construir y retornar la respuesta
            return LoginResponse.builder()
                    .internalToken(internalToken)
                    .user(UserDTO.builder()
                            .uid(uid)
                            .email(email)
                            .nombre(userInfo.getNombre())
                            .rolId(userInfo.getRolId())
                            .rolNombre(userInfo.getRolNombre())
                            .build())
                    .expiresIn(jwtService.getExpirationTime())
                    .build();

        } catch (FirebaseAuthException e) {
            log.error("Error al validar token de Firebase: {}", e.getMessage());
            throw new RuntimeException("Token de Firebase inválido", e);
        } catch (Exception e) {
            log.error("Error en autenticación: {}", e.getMessage());
            throw new RuntimeException("Error en el proceso de autenticación", e);
        }
    }

    /**
     * Consulta la información del usuario desde el microservicio GESTIONUSUARIO
     */
    private UserDTO getUserInfoFromMicroservice(String uid) {
        try {
            log.info("Consultando información del usuario desde GESTIONUSUARIO: {}", uid);

            WebClient webClient = webClientBuilder.baseUrl(gestionUsuarioUrl).build();

            // Hacer request al microservicio para obtener el rol del usuario
            // Ajusta esta URL según tu endpoint en GESTIONUSUARIO
            UserDTO userInfo = webClient.get()
                    .uri("/api/usuarios/firebase/{uid}", uid)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block(); // Bloquear para obtener el resultado (síncrono)

            if (userInfo == null) {
                throw new RuntimeException("Usuario no encontrado en el sistema");
            }

            log.info("Información del usuario obtenida: {} - Rol: {}", userInfo.getEmail(), userInfo.getRolNombre());
            return userInfo;

        } catch (Exception e) {
            log.error("Error al consultar información del usuario: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener la información del usuario", e);
        }
    }

    /**
     * Refresca el token JWT interno
     */
    public LoginResponse refreshToken(String expiredToken) {
        try {
            // Extraer información del token expirado (sin validar expiración)
            String uid = jwtService.extractUid(expiredToken);
            String email = jwtService.extractEmail(expiredToken);
            Integer rolId = jwtService.extractRolId(expiredToken);
            String rolNombre = jwtService.extractRolNombre(expiredToken);

            // Generar nuevo token
            String newToken = jwtService.generateToken(uid, email, rolId, rolNombre);

            log.info("Token refrescado para usuario: {}", email);

            return LoginResponse.builder()
                    .internalToken(newToken)
                    .user(UserDTO.builder()
                            .uid(uid)
                            .email(email)
                            .rolId(rolId)
                            .rolNombre(rolNombre)
                            .build())
                    .expiresIn(jwtService.getExpirationTime())
                    .build();

        } catch (Exception e) {
            log.error("Error al refrescar token: {}", e.getMessage());
            throw new RuntimeException("No se pudo refrescar el token", e);
        }
    }
}

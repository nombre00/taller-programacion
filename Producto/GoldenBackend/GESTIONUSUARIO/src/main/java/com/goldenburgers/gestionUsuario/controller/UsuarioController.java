package com.goldenburgers.gestionUsuario.controller;

import com.goldenburgers.gestionUsuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operaciones de Usuario
 * Necesario para que el API Gateway pueda consultar información de usuarios por Firebase UID
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    /**
     * Endpoint para que el API Gateway obtenga información de usuario por Firebase UID
     * Este endpoint es llamado durante el proceso de login para obtener el rol del usuario
     * 
     * @param uid Firebase UID del usuario
     * @return UsuarioDTO con email, rol ID y nombre del rol
     */
    @GetMapping("/firebase/{uid}")
    public ResponseEntity<?> getUserByFirebaseUid(@PathVariable String uid) {
        log.debug("Buscando usuario por Firebase UID: {}", uid);
        
        return usuarioRepository.findById(uid)
                .map(usuario -> {
                    // Crear un Map simple con los datos necesarios
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("firebaseUid", usuario.getIdUsuario());
                    response.put("email", usuario.getEmail());
                    response.put("rolId", usuario.getRol().getIdRol().intValue());
                    response.put("rolNombre", usuario.getRol().getNombreRol());
                    
                    log.debug("Usuario encontrado: {} con rol: {}", usuario.getEmail(), usuario.getRol().getNombreRol());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("Usuario no encontrado con Firebase UID: {}", uid);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Endpoint para verificar si un email ya está registrado
     * @param email Email a verificar
     * @return true si existe, false si no existe
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = usuarioRepository.existsByEmail(email);
        log.debug("Email {} existe: {}", email, exists);
        return ResponseEntity.ok(exists);
    }

    /**
     * Endpoint para verificar si un Firebase UID ya está registrado
     * @param uid Firebase UID a verificar
     * @return true si existe, false si no existe
     */
    @GetMapping("/exists/uid/{uid}")
    public ResponseEntity<Boolean> existsByUid(@PathVariable String uid) {
        boolean exists = usuarioRepository.existsById(uid);
        log.debug("Firebase UID {} existe: {}", uid, exists);
        return ResponseEntity.ok(exists);
    }
}

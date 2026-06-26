package com.goldenburgers.catalogo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio para validar tokens JWT generados por el API Gateway
 * Este microservicio NO genera tokens, solo los valida
 */
@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Extrae el email (subject) del token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae los roles del token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        logger.info("===== DEBUG JWT ROLES =====");
        logger.info("Claims completos: {}", claims);
        
        // Primero intentar obtener como lista (roles)
        Object rolesObj = claims.get("roles");
        logger.info("roles (como lista): {}", rolesObj);
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        
        // Si no es lista, intentar obtener como string único (rolNombre)
        String rolNombre = (String) claims.get("rolNombre");
        logger.info("rolNombre (como string): {}", rolNombre);
        if (rolNombre != null) {
            List<String> result = List.of(rolNombre.toUpperCase()); // Admin -> ADMIN
            logger.info("Roles finales: {}", result);
            return result;
        }
        
        logger.warn("SIN ROLES ENCONTRADOS");
        return List.of(); // Sin roles
    }

    /**
     * Extrae el UID de Firebase del token
     */
    public String extractFirebaseUid(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("firebaseUid");
    }

    /**
     * Convierte los roles a authorities de Spring Security
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        List<String> roles = extractRoles(token);
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * Valida que el token sea válido (firma correcta y no expirado)
     */
    public boolean isTokenValid(String token) {
        try {
            boolean isValid = !isTokenExpired(token);
            logger.info("Token válido: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token y valida la firma
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave de firma a partir del secret configurado
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

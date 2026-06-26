package com.goldenburgers.catalogo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta todas las peticiones HTTP y valida el token JWT
 * Si el token es válido, autentica al usuario en el contexto de Spring Security
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");
        
        // Si no hay header o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer el token (quitar "Bearer " del inicio)
            final String jwt = authHeader.substring(7);
            
            logger.info("===== JWT FILTER: Procesando token =====");
            
            // Validar el token
            if (jwtService.isTokenValid(jwt)) {
                logger.info("Token válido, extrayendo información...");
                // Extraer información del usuario del token
                String email = jwtService.extractEmail(jwt);
                String firebaseUid = jwtService.extractFirebaseUid(jwt);
                List<String> roles = jwtService.extractRoles(jwt);
                
                logger.info("Email: " + email + ", FirebaseUid: " + firebaseUid + ", Roles: " + roles);

                // Crear UserDetails con la información del token
                CustomUserDetails userDetails = new CustomUserDetails(email, firebaseUid, roles);

                logger.info("UserDetails creado - Authorities: " + userDetails.getAuthorities());

                // Crear el objeto de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Agregar detalles de la petición HTTP
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si hay error al procesar el token, continuar sin autenticar
            // El SecurityConfig se encargará de rechazar la petición si requiere autenticación
            logger.error("Error al procesar JWT: " + e.getMessage());
        }

        // Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}

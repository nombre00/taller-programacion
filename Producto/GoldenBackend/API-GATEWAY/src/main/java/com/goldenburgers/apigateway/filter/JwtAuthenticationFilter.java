package com.goldenburgers.apigateway.filter;

import com.goldenburgers.apigateway.security.CustomUserDetails;
import com.goldenburgers.apigateway.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para validar tokens JWT internos en todas las requests
 * Este filtro se ejecuta DESPUÉS del login, en las requests subsecuentes
 */
/* @Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // log para ver que pasa con los token de autentificacion.
        log.info("JwtAuthenticationFilter ejecutado - Path: {}, Method: {}", 
         request.getRequestURI(), request.getMethod());

        // ===== RUTAS QUE NO REQUIEREN AUTENTICACIÓN =====
        // String path = request.getServletPath();   version vieja
        String path = request.getRequestURI();     // version nueva para pruebas
        String method = request.getMethod();

        // Sistema y documentación
        if (path.startsWith("/api/auth/") ||
            path.startsWith("/actuator/") ||
            path.startsWith("/health") ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // GESTIÓN USUARIO - Endpoints públicos
        if ((path.startsWith("/api/usuarios/firebase/") && method.equals("GET")) ||
            (path.startsWith("/api/usuarios/exists/") && method.equals("GET")) ||
            (path.equals("/api/clientes") && method.equals("POST")) ||
            (path.startsWith("/api/roles") && method.equals("GET")) ||
            (path.startsWith("/api/ciudades") && method.equals("GET"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // GESTIÓN CATÁLOGO - Endpoints públicos (GET solamente)
        if (path.startsWith("/api/catalogo") && method.equals("GET")) {
            filterChain.doFilter(request, response);
            return;
        }

        // GESTIÓN CONTACTO - Formulario de contacto público
        if (path.equals("/api/mensajes") && method.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // GESTIÓN PEDIDO - Webhook de Mercado Pago
        if (path.equals("/api/pagos/webhook") && method.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token del header Authorization
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Request sin token JWT interno a: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            
            log.debug("Validando JWT token...");
            
            // Extraer información del token
            final String uid = jwtService.extractUid(jwt);
            final String email = jwtService.extractEmail(jwt);
            final Integer rolId = jwtService.extractRolId(jwt);
            final String rolNombre = jwtService.extractRolNombre(jwt);

            log.debug("Token extraído - UID: {}, Email: {}, Rol: {}", uid, email, rolNombre);

            // Si el usuario no está autenticado aún
            if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                log.debug("Validando token para UID: {}", uid);
                
                // Validar el token
                if (jwtService.isTokenValid(jwt, uid)) {
                    
                    log.info("Token válido para usuario: {}", email);
                    
                    // Crear CustomUserDetails con la información del token
                    CustomUserDetails userDetails = new CustomUserDetails(
                            uid,
                            email,
                            rolId.longValue(),  // Convertir Integer a Long
                            rolNombre
                    );

                    // Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Usuario autenticado: {} con rol: {}", email, rolNombre);
                } else {
                    log.warn("Token inválido o expirado para UID: {}", uid);
                }
            }
        } catch (Exception e) {
            log.error("Error al validar token JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
} */







@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Este método es el más importante: define qué rutas NO deben pasar por el filtro
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // log
        log.info("shouldNotFilter llamado para path: {}", path);   // <--- Log fuerte
        if (path.contains("/api/auth/login")) {
            log.info("=== RUTA DE LOGIN DETECTADA - SALTANDO FILTRO ===");
            return true;
        }
        // fin log

        // Rutas que NO requieren autenticación
        return path.startsWith("/api/auth/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               
               // Gestión Usuario - públicos
               (path.startsWith("/api/usuarios/firebase/") && method.equals("GET")) ||
               (path.startsWith("/api/usuarios/exists/") && method.equals("GET")) ||
               (path.equals("/api/clientes") && method.equals("POST")) ||
               (path.startsWith("/api/roles") && method.equals("GET")) ||
               (path.startsWith("/api/ciudades") && method.equals("GET")) ||
               
               // Catálogo público
               (path.startsWith("/api/catalogo") && method.equals("GET")) ||
               
               // Contacto público
               (path.equals("/api/mensajes") && method.equals("POST")) ||
               
               // Webhook Mercado Pago
               (path.equals("/api/pagos/webhook") && method.equals("POST"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("JwtAuthenticationFilter procesando ruta autenticada: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Request sin token JWT a: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String uid = jwtService.extractUid(jwt);

            if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt, uid)) {
                    // ... (el resto del código de crear UserDetails y autenticación se mantiene igual)
                    final String email = jwtService.extractEmail(jwt);
                    final Integer rolId = jwtService.extractRolId(jwt);
                    final String rolNombre = jwtService.extractRolNombre(jwt);

                    CustomUserDetails userDetails = new CustomUserDetails(uid, email, rolId.longValue(), rolNombre);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Usuario autenticado correctamente: {}", email);
                } else {
                    log.warn("Token inválido para UID: {}", uid);
                }
            }
        } catch (Exception e) {
            log.error("Error procesando token JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
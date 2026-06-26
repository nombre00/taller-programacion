package com.example.Microservicio_Gestion_Venta.filter;

import com.example.Microservicio_Gestion_Venta.security.CustomUserDetails;
import com.example.Microservicio_Gestion_Venta.service.JwtService;
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
 * Filtro para validar tokens JWT internos del API Gateway
 * Este filtro valida el header X-Internal-Token enviado por el API Gateway
 */
@Component
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

        // Rutas públicas que no requieren autenticación
        String path = request.getServletPath();
        if (path.startsWith("/actuator/") || 
            path.startsWith("/health") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token del header X-Internal-Token (enviado por el API Gateway)
        // o del header Authorization (enviado por Swagger UI / Postman)
        String internalToken = request.getHeader("X-Internal-Token");
        
        // Si no viene en X-Internal-Token, intentar con Authorization Bearer
        if (internalToken == null || internalToken.isEmpty()) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                internalToken = authHeader.substring(7);
            }
        }
        
        if (internalToken == null || internalToken.isEmpty()) {
            log.warn("Request sin token JWT interno a: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token interno requerido\"}");
            return;
        }

        try {
            // Extraer información del token
            final String uid = jwtService.extractUid(internalToken);
            final String email = jwtService.extractEmail(internalToken);
            final Integer rolId = jwtService.extractRolId(internalToken);
            final String rolNombre = jwtService.extractRolNombre(internalToken);

            // Si el usuario no está autenticado aún
            if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validar el token
                if (jwtService.isTokenValid(internalToken)) {
                    
                    // Crear CustomUserDetails con la información del token
                    CustomUserDetails userDetails = new CustomUserDetails(
                            uid,
                            email,
                            rolId.longValue(),
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
                    
                    log.debug("Usuario autenticado desde API Gateway: {} con rol: {}", email, rolNombre);
                }
            }
        } catch (Exception e) {
            log.error("Error al validar token JWT interno: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token inválido\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

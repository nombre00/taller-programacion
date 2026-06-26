package com.goldenburgers.gestionturnos.filter;


import com.goldenburgers.gestionturnos.security.CustomUserDetails;
import com.goldenburgers.gestionturnos.service.JwtService;
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

        String path = request.getServletPath();

        if (path.startsWith("/actuator/") ||
            path.startsWith("/health") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        String internalToken = request.getHeader("X-Internal-Token");

        if (internalToken == null || internalToken.isEmpty()) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                internalToken = authHeader.substring(7);
            }
        }

        if (internalToken == null || internalToken.isEmpty()) {
            log.warn("Request sin token JWT interno a: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token interno requerido\"}");
            return;
        }

        try {
            final String uid = jwtService.extractUid(internalToken);
            final String email = jwtService.extractEmail(internalToken);
            final Integer rolId = jwtService.extractRolId(internalToken);
            final String rolNombre = jwtService.extractRolNombre(internalToken);

            if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(internalToken)) {

                    CustomUserDetails userDetails = new CustomUserDetails(
                            uid,
                            email,
                            rolId.longValue(),
                            rolNombre
                    );

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuario autenticado: {} con rol: {}", email, rolNombre);
                }
            }
        } catch (Exception e) {
            log.error("Error al validar token JWT interno: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token inválido\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

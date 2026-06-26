package com.goldenburgers.apigateway.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Detalles del usuario autenticado con Firebase
 * Contiene el UID de Firebase, email, y rol del usuario
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final String firebaseUid;      // UID de Firebase
    private final String email;            // Email del usuario
    private final Long rolId;              // ID del rol (1=ADMIN, 2=TRABAJADOR, 3=CLIENTE)
    private final String rolNombre;        // Nombre del rol

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapear el ID del rol a authorities de Spring Security
        String roleName = switch (rolId.intValue()) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_TRABAJADOR";
            case 3 -> "ROLE_CLIENTE";
            default -> "ROLE_USER";
        };

        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        // No se usa password, Firebase maneja la autenticación
        return null;
    }

    @Override
    public String getUsername() {
        // Usamos el email como username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

package com.goldenburgers.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para retornar el token JWT interno y datos del usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String internalToken;
    private UserDTO user;
    private long expiresIn; // Tiempo de expiración en milisegundos
}

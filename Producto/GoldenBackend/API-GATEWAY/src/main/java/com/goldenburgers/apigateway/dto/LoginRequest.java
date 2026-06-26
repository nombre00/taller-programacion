package com.goldenburgers.apigateway.dto;

import lombok.Data;

/**
 * DTO para recibir el token de Firebase del cliente
 */
@Data
public class LoginRequest {
    private String firebaseToken;
}

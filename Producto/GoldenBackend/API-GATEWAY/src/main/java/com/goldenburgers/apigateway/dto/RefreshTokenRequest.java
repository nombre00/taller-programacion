package com.goldenburgers.apigateway.dto;

import lombok.Data;

/**
 * DTO para solicitar refresh de token
 */
@Data
public class RefreshTokenRequest {
    private String token;
}

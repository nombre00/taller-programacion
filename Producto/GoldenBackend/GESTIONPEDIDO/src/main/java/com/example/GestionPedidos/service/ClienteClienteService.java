package com.example.GestionPedidos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class ClienteClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteClienteService.class);

    @Value("${services.gestionusuario.url}")
    private String gestionUsuarioUrl;

    private final RestTemplate restTemplate;

    public ClienteClienteService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String obtenerTelefonoCliente(Long idCliente, String token) {
        try {
            String url = gestionUsuarioUrl + "/api/clientes/" + idCliente;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object telefono = response.getBody().get("telefono");
                return telefono != null ? telefono.toString() : null;
            }

            return null;

        } catch (Exception e) {
            logger.error("Error al obtener teléfono del cliente {}: {}", idCliente, e.getMessage());
            return null;
        }
    }
}
package com.example.Microservicio_Gestion_Venta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


// Gestiona la comunicación con el servicio GESTIONCATALOGO.


@Service
public class CatalogoClienteService {

    private final RestTemplate restTemplate;

    @Value("${gestioncatalogo.url:http://localhost:8082}")
    private String gestionCatalogoUrl;

    public CatalogoClienteService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> descontarPorVenta(Map<String, Object> requestBody, String token) {
        String url = gestionCatalogoUrl + "/api/materias-primas/descontar-por-venta";
        System.out.println("Consumiendo API CATALOGO: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            System.out.println("✅ Stock descontado correctamente");
            return response.getBody();
        }

        throw new RuntimeException("No se pudo descontar stock en GESTIONCATALOGO");
    }
}
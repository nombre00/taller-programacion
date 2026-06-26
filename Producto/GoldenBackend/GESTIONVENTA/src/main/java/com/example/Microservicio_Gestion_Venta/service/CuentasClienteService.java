package com.example.Microservicio_Gestion_Venta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


// Gestiona la comunicación con el servicio GESTIONCUENTAS.


@Service
public class CuentasClienteService {

    private final RestTemplate restTemplate;

    @Value("${gestioncuentas.url:http://localhost:8087}")
    private String gestionCuentasUrl;

    public CuentasClienteService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> registrarIngreso(Map<String, Object> requestBody, String token) {
        String url = gestionCuentasUrl + "/api/ingresos";
        System.out.println("Consumiendo API CUENTAS: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            System.out.println("✅ Ingreso registrado en GESTIONCUENTAS");
            return response.getBody();
        }

        throw new RuntimeException("No se pudo registrar ingreso en GESTIONCUENTAS");
    }
}
package com.example.GestionPedidos.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class VentaClienteService {

    private final RestTemplate restTemplate;

    @Value("${gestionventa.url:http://localhost:8082}")
    private String gestionVentaUrl;

    public VentaClienteService() {
        this.restTemplate = new RestTemplate();
    }

    public void crearVentaDesdePedido(Long idPedido, String token) {
        try {
            String url = gestionVentaUrl + "/api/ventas/desde-pedido/" + idPedido;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(null, headers);

            restTemplate.postForObject(url, request, String.class);

            System.out.println("Venta creada para pedido: " + idPedido);
        } catch (Exception e) {
            System.err.println("Error al crear venta: " + e.getMessage());
            throw new RuntimeException("Error al comunicarse con GestionVenta", e);
        }
    }
}

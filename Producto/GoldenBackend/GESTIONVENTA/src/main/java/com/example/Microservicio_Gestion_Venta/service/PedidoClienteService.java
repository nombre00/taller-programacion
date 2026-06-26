package com.example.Microservicio_Gestion_Venta.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import java.util.Map;

@Service
public class PedidoClienteService {

    private final RestTemplate restTemplate;

    @Value("${gestionpedido.url:http://localhost:8083}")
    private String gestionPedidoUrl;

    public PedidoClienteService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Obtener pedido completo desde GestionPedidos
     * @param idPedido ID del pedido
     * @param token X-Internal-Token para autenticación interna
     * @return Map con los datos completos del pedido
     */

     //Anotación para evitar warnings de tipos genéricos
    @SuppressWarnings({"unchecked", "rawtypes"})

    //map porque es un objeto con varios datos (pedido, detalles, etc)
    public Map<String, Object> obtenerPedidoCompleto(Long idPedido, String token) {
        String url = gestionPedidoUrl + "/api/pedidos/" + idPedido;
        System.out.println("Consumiendo API: " + url);

        // Preparar headers con token
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        // Hacer GET con token
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            System.out.println("✅ Datos recibidos del pedido: " + idPedido);
            return response.getBody();
        }

        throw new RuntimeException("No se pudo obtener el pedido: " + idPedido);
    }
}

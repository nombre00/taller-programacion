package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.IngresarCompraRequestDTO;
import com.goldenburgers.gestioncuentas.dto.StockOperacionResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CatalogoClienteService {

    private final RestTemplate restTemplate;

    @Value("${services.gestioncatalogo.url:http://localhost:8082}")
    private String gestionCatalogoUrl;

    public CatalogoClienteService() {
        this.restTemplate = new RestTemplate();
    }

    //@SuppressWarnings({"unchecked", "rawtypes"})
    public StockOperacionResponseDTO ingresarPorCompra(IngresarCompraRequestDTO request, String token) {
        String url = gestionCatalogoUrl + "/api/materias-primas/ingresar-por-compra";
        log.info("Consumiendo API: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<IngresarCompraRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<StockOperacionResponseDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            StockOperacionResponseDTO.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("Respuesta recibida de GESTIONCATALOGO: exitoso={}", response.getBody().getExitoso());
            return response.getBody();
        }

        throw new RuntimeException("No se pudo contactar a GESTIONCATALOGO");
    }
}
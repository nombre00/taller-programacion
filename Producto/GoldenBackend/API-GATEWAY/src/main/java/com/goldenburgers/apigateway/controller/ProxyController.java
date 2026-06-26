package com.goldenburgers.apigateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Enumeration;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final RestTemplate restTemplate;

    @Value("${microservices.gestion-usuario.url}")
    private String gestionUsuarioUrl;

    @Value("${microservices.gestion-venta.url}")
    private String gestionVentaUrl;

    @Value("${microservices.gestion-pedido.url}")
    private String gestionPedidoUrl;

    @Value("${microservices.gestion-catalogo.url}")
    private String gestionCatalogoUrl;

    @Value("${microservices.gestion-contacto.url}")
    private String gestionContactoUrl;

    //Gestion Usuario:

    @RequestMapping(value = "/api/usuarios/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyUsuarios(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionUsuarioUrl);
        }

    @RequestMapping(value = "/api/clientes/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyClientes(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionUsuarioUrl);
        }

    @RequestMapping(value = "/api/trabajadores/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyTrabajadores(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionUsuarioUrl);
        }

    @RequestMapping(value = "/api/roles/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyRoles(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionUsuarioUrl);
        }

    @RequestMapping(value = "/api/ciudades/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyCiudades(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionUsuarioUrl);
        }

    //Gestión Catalogo:

    @RequestMapping(value = "/api/catalogo/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
        public ResponseEntity<String> proxyCatalogo(HttpServletRequest request, @RequestBody(required = false) String body) {
            // Detectar si es multipart/form-data
            String contentType = request.getContentType();
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                log.info("🔄 Detectada petición multipart/form-data, usando proxy especializado");
                return forwardMultipartRequest(request, gestionCatalogoUrl);
            }
            return forwardRequest(request, body, gestionCatalogoUrl);
        }

    //Gestion contacto:

    @RequestMapping(value = "/api/mensajes/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyMensaje(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionContactoUrl);
        }

    //Gestion pedido:

    @RequestMapping(value = "/api/pagos/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyPago(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionPedidoUrl);
        }

    @RequestMapping(value = "/api/pedidos/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyPedido(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionPedidoUrl);
        }

    //Gestion venta:

    @RequestMapping(value = "/api/boletas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyBoleta(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionVentaUrl);
        }

    @RequestMapping(value = "/api/dashboard/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyDashboard(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionVentaUrl);
        }

    @RequestMapping(value = "/api/devoluciones/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyDevolucion(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionVentaUrl);
        }

    @RequestMapping(value = "/api/ventas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
        public ResponseEntity<String> proxyVenta(HttpServletRequest request, @RequestBody(required = false) String body) {
            return forwardRequest(request, body, gestionVentaUrl);
        }

    // Proxy para Swagger UI de GESTIONUSUARIO (sin autenticación)
    @RequestMapping(value = "/swagger-ui/**", method = RequestMethod.GET)
    public ResponseEntity<String> proxySwaggerUi(HttpServletRequest request) {
        return forwardRequestWithoutAuth(request, gestionUsuarioUrl);
    }

    @RequestMapping(value = "/v3/api-docs/**", method = RequestMethod.GET)
    public ResponseEntity<String> proxyApiDocs(HttpServletRequest request) {
        return forwardRequestWithoutAuth(request, gestionUsuarioUrl);
    }

    @RequestMapping(value = "/swagger-ui.html", method = RequestMethod.GET)
    public ResponseEntity<String> proxySwaggerHtml(HttpServletRequest request) {
        return forwardRequestWithoutAuth(request, gestionUsuarioUrl);
    }
/* 
    private ResponseEntity<String> forwardRequest(HttpServletRequest request, String body, String microserviceUrl) {
        try {
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String targetUrl = microserviceUrl + path;
            
            if (queryString != null && !queryString.isEmpty()) {
                targetUrl += "?" + queryString;
            }

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpHeaders headers = new HttpHeaders();
            
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") && 
                    !headerName.equalsIgnoreCase("content-length") &&
                    !headerName.equalsIgnoreCase("transfer-encoding")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }
            
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                headers.set("X-Internal-Token", token);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(targetUrl), method, entity, String.class);

            return response;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
*/
  private ResponseEntity<String> forwardRequest(HttpServletRequest request, String body, String microserviceUrl) {
      try {
          String path = request.getRequestURI();
          String queryString = request.getQueryString();
          String targetUrl = microserviceUrl + path;

          if (queryString != null && !queryString.isEmpty()) {
              targetUrl += "?" + queryString;
          }

          HttpMethod method = HttpMethod.valueOf(request.getMethod());
          HttpHeaders headers = new HttpHeaders();

          // Copiar todos los headers
          Enumeration<String> headerNames = request.getHeaderNames();
          while (headerNames.hasMoreElements()) {
              String headerName = headerNames.nextElement();
              if (!headerName.equalsIgnoreCase("host") &&
                  !headerName.equalsIgnoreCase("content-length") &&
                  !headerName.equalsIgnoreCase("transfer-encoding")) {
                  headers.add(headerName, request.getHeader(headerName));
              }
          }

          // IMPORTANTE: Asegurar que el header Authorization esté presente
          String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
          if (authHeader != null && authHeader.startsWith("Bearer ")) {
              String token = authHeader.substring(7);
              // Mantener AMBOS headers
              headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);  // ← AGREGAR ESTA LÍNEA
              headers.set("X-Internal-Token", token);

              log.debug("✅ Reenviando token al microservicio: Bearer {}...", token.substring(0, Math.min(20, token.length())));
          } else {
              log.warn("⚠️ No se encontró header Authorization en la petición");
          }

          HttpEntity<String> entity = new HttpEntity<>(body, headers);
          ResponseEntity<String> response = restTemplate.exchange(URI.create(targetUrl), method, entity, String.class);

          return response;

      } catch (Exception e) {
          log.error("❌ Error al reenviar request: {}", e.getMessage(), e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body("{\"error\": \"" + e.getMessage() + "\"}");
      }
  }
    /**
     * Reenvía requests de Swagger sin agregar token de autenticación
     * (Swagger UI debe ser público para poder cargar la documentación)
     */
    private ResponseEntity<String> forwardRequestWithoutAuth(HttpServletRequest request, String microserviceUrl) {
        try {
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String targetUrl = microserviceUrl + path;

            if (queryString != null && !queryString.isEmpty()) {
                targetUrl += "?" + queryString;
            }

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpHeaders headers = new HttpHeaders();

            // Copiar solo headers básicos (sin Authorization)
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") &&
                    !headerName.equalsIgnoreCase("content-length") &&
                    !headerName.equalsIgnoreCase("transfer-encoding") &&
                    !headerName.equalsIgnoreCase("authorization")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(targetUrl), method, entity, String.class);

            return response;

        } catch (Exception e) {
            log.error("Error al reenviar request de Swagger: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Reenvía peticiones multipart/form-data sin procesar el body
     * (Importante para uploads de archivos)
     */
    private ResponseEntity<String> forwardMultipartRequest(HttpServletRequest request, String microserviceUrl) {
        try {
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String targetUrl = microserviceUrl + path;

            if (queryString != null && !queryString.isEmpty()) {
                targetUrl += "?" + queryString;
            }

            org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest =
                (org.springframework.web.multipart.MultipartHttpServletRequest) request;

            org.springframework.util.MultiValueMap<String, org.springframework.core.io.Resource> multipartBody =
                new org.springframework.util.LinkedMultiValueMap<>();

            multipartRequest.getFileMap().forEach((name, file) -> {
                try {
                    multipartBody.add(name, new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    });
                    log.info("📎 Archivo agregado: {} ({} bytes)", name, file.getSize());
                } catch (Exception e) {
                    log.error("❌ Error al leer archivo: {}", e.getMessage());
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                log.debug("✅ Token agregado a petición multipart");
            }

            HttpEntity<org.springframework.util.MultiValueMap<String, org.springframework.core.io.Resource>> entity =
                new HttpEntity<>(multipartBody, headers);

            log.info("📤 Reenviando multipart a: {}", targetUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                URI.create(targetUrl), HttpMethod.POST, entity, String.class);

            log.info("✅ Respuesta: {}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("❌ Error multipart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

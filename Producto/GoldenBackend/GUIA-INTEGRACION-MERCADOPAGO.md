# Guía Completa: Integración MercadoPago en GESTIONPEDIDO

## Índice
1. [Registro en MercadoPago Developers](#1-registro-en-mercadopago-developers)
2. [Configuración del Backend (GESTIONPEDIDO)](#2-configuración-del-backend)
3. [Modificación de Base de Datos](#3-modificación-de-base-de-datos)
4. [Implementación en Backend](#4-implementación-en-backend)
5. [Integración en Frontend](#5-integración-en-frontend)
6. [Pruebas con Tarjetas de Test](#6-pruebas-con-tarjetas-de-test)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Registro en MercadoPago Developers

### Paso 1.1: Crear cuenta de desarrollador

1. **Ir a**: https://www.mercadopago.cl/developers/ (o .com.ar según tu país)

2. **Registrarse**:
   - Opción A: Usar cuenta de MercadoPago existente
   - Opción B: Crear nueva cuenta (email + contraseña)

3. **Verificar email**: Recibirás un correo de confirmación

### Paso 1.2: Crear aplicación de prueba

1. **Acceder al Panel de Desarrolladores**:
   - https://www.mercadopago.cl/developers/panel

2. **Crear aplicación**:
   ```
   Panel → "Tus aplicaciones" → "Crear aplicación"
   ```

3. **Completar datos**:
   - **Nombre de la aplicación**: Golden Burgers
   - **Tipo**: Pagos online
   - **Descripción**: Sistema de pedidos para restaurante
   - **Modelo de integración**: Checkout Pro

4. **Guardar aplicación**

### Paso 1.3: Obtener credenciales de TEST

1. **En tu aplicación**, ir a:
   ```
   Credenciales → Credenciales de prueba
   ```

2. **Copiar credenciales**:
   ```
   Access Token de prueba: TEST-1234567890123456-112010-abcdef1234567890abcdef1234567890-123456789
   Public Key de prueba: TEST-abc12345-6789-0123-4567-890abcdef123
   ```

   ⚠️ **IMPORTANTE**: Copiar el **Access Token de prueba** (comienza con `TEST-`)

3. **Guardar en lugar seguro**: Las necesitarás para configurar el backend

---

## 2. Configuración del Backend

### Paso 2.1: Agregar dependencia de MercadoPago

**Archivo**: `GESTIONPEDIDO/GestionPedidos/pom.xml`

```xml
<dependencies>
    <!-- Dependencias existentes... -->
    
    <!-- MercadoPago SDK -->
    <dependency>
        <groupId>com.mercadopago</groupId>
        <artifactId>sdk-java</artifactId>
        <version>2.1.26</version>
    </dependency>
</dependencies>
```

**Ejecutar** para descargar dependencia:
```bash
cd GESTIONPEDIDO/GestionPedidos
./mvnw clean install
```

### Paso 2.2: Configurar credenciales

**Archivo**: `GESTIONPEDIDO/GestionPedidos/src/main/resources/application.properties`

Agregar al final del archivo:

```properties
# ==========================================
# MERCADOPAGO CONFIGURATION
# ==========================================
# Credenciales de TEST (Sandbox)
mercadopago.access.token=${MERCADOPAGO_ACCESS_TOKEN:TEST-1234567890123456-112010-abcdef1234567890abcdef1234567890-123456789}
mercadopago.public.key=${MERCADOPAGO_PUBLIC_KEY:TEST-abc12345-6789-0123-4567-890abcdef123}

# URLs de retorno (ajustar según tu frontend)
mercadopago.success.url=http://localhost:3000/pago/exitoso
mercadopago.failure.url=http://localhost:3000/pago/fallido
mercadopago.pending.url=http://localhost:3000/pago/pendiente

# URL de webhook (debe ser accesible desde internet en producción)
# Para desarrollo local, usar ngrok o similar
mercadopago.notification.url=http://localhost:8083/api/pagos/webhook/mercadopago

# Ambiente (test o production)
mercadopago.environment=test
```

⚠️ **REEMPLAZAR**: 
- `TEST-1234567890123456-...` con tu Access Token real
- `TEST-abc12345-...` con tu Public Key real
- URLs del frontend según tu configuración

---

## 3. Modificación de Base de Datos

### Paso 3.1: Agregar columnas a tabla PEDIDO

**Ejecutar en Oracle SQL Developer o SQL*Plus**:

```sql
-- Agregar columnas relacionadas con el pago
ALTER TABLE pedido ADD estado_pago VARCHAR2(20) DEFAULT 'PENDIENTE';
ALTER TABLE pedido ADD mercadopago_preference_id VARCHAR2(100);
ALTER TABLE pedido ADD mercadopago_payment_id VARCHAR2(100);
ALTER TABLE pedido ADD mercadopago_status VARCHAR2(50);
ALTER TABLE pedido ADD mercadopago_status_detail VARCHAR2(100);
ALTER TABLE pedido ADD mercadopago_payment_type VARCHAR2(50);
ALTER TABLE pedido ADD mercadopago_payment_method VARCHAR2(50);
ALTER TABLE pedido ADD fecha_pago TIMESTAMP;
ALTER TABLE pedido ADD monto_pagado NUMBER(10,2);

-- Agregar comentarios para documentación
COMMENT ON COLUMN pedido.estado_pago IS 'Estado del pago: PENDIENTE, APROBADO, RECHAZADO, CANCELADO';
COMMENT ON COLUMN pedido.mercadopago_preference_id IS 'ID de preferencia de pago en MercadoPago';
COMMENT ON COLUMN pedido.mercadopago_payment_id IS 'ID del pago confirmado en MercadoPago';
COMMENT ON COLUMN pedido.mercadopago_status IS 'Estado devuelto por MercadoPago: approved, rejected, pending, etc.';
```

### Paso 3.2: Verificar columnas agregadas

```sql
SELECT column_name, data_type, data_length 
FROM user_tab_columns 
WHERE table_name = 'PEDIDO' 
  AND column_name LIKE '%PAGO%' OR column_name LIKE '%MERCADOPAGO%'
ORDER BY column_name;
```

---

## 4. Implementación en Backend

### Paso 4.1: Actualizar modelo Pedido.java

**Archivo**: `GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/model/Pedido.java`

Agregar los nuevos campos al final de la clase (antes del último `}`):

```java
// ==========================================
// CAMPOS DE MERCADOPAGO
// ==========================================

@Column(name = "estado_pago", length = 20)
private String estadoPago = "PENDIENTE"; // PENDIENTE, APROBADO, RECHAZADO, CANCELADO

@Column(name = "mercadopago_preference_id", length = 100)
private String mercadopagoPreferenceId;

@Column(name = "mercadopago_payment_id", length = 100)
private String mercadopagoPaymentId;

@Column(name = "mercadopago_status", length = 50)
private String mercadopagoStatus;

@Column(name = "mercadopago_status_detail", length = 100)
private String mercadopagoStatusDetail;

@Column(name = "mercadopago_payment_type", length = 50)
private String mercadopagoPaymentType;

@Column(name = "mercadopago_payment_method", length = 50)
private String mercadopagoPaymentMethod;

@Column(name = "fecha_pago")
private Timestamp fechaPago;

@Column(name = "monto_pagado", precision = 10, scale = 2)
private Double montoPagado;
```

### Paso 4.2: Crear DTOs para MercadoPago

**Crear carpeta**: `GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/dto/mercadopago/`

#### IniciarPagoRequest.java

```java
package com.example.GestionPedidos.dto.mercadopago;

import lombok.Data;

@Data
public class IniciarPagoRequest {
    private Long idPedido;
    private String emailCliente;
}
```

#### IniciarPagoResponse.java

```java
package com.example.GestionPedidos.dto.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarPagoResponse {
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private Long idPedido;
    private Double monto;
    private String mensaje;
}
```

#### WebhookMercadoPagoRequest.java

```java
package com.example.GestionPedidos.dto.mercadopago;

import lombok.Data;

@Data
public class WebhookMercadoPagoRequest {
    private Long id; // payment_id
    private String topic; // "payment" o "merchant_order"
}
```

### Paso 4.3: Crear servicio MercadoPagoService

**Archivo**: `GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/service/MercadoPagoService.java`

```java
package com.example.GestionPedidos.service;

import com.example.GestionPedidos.dto.mercadopago.IniciarPagoResponse;
import com.example.GestionPedidos.model.Pedido;
import com.example.GestionPedidos.repository.PedidoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.success.url}")
    private String successUrl;

    @Value("${mercadopago.failure.url}")
    private String failureUrl;

    @Value("${mercadopago.pending.url}")
    private String pendingUrl;

    @Value("${mercadopago.notification.url}")
    private String notificationUrl;

    /**
     * Crear preferencia de pago en MercadoPago
     */
    @Transactional
    public IniciarPagoResponse crearPreferenciaPago(Long idPedido, String emailCliente) {
        try {
            // Configurar SDK con access token
            MercadoPagoConfig.setAccessToken(accessToken);

            // Obtener pedido
            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));

            // Validar que el pedido no esté ya pagado
            if ("APROBADO".equals(pedido.getEstadoPago())) {
                throw new RuntimeException("El pedido ya fue pagado");
            }

            // Crear item del pedido
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id("PEDIDO-" + idPedido)
                    .title("Pedido Golden Burgers #" + idPedido)
                    .description("Pedido de comida - Golden Burgers")
                    .categoryId("food")
                    .quantity(1)
                    .currencyId("CLP") // Cambiar según país: CLP, ARS, BRL, MXN, etc.
                    .unitPrice(new BigDecimal(pedido.getMontoTotal()))
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl + "?pedido_id=" + idPedido)
                    .failure(failureUrl + "?pedido_id=" + idPedido)
                    .pending(pendingUrl + "?pedido_id=" + idPedido)
                    .build();

            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved") // Retorna automáticamente si se aprueba
                    .externalReference("PEDIDO-" + idPedido) // Referencia para identificar el pedido
                    .notificationUrl(notificationUrl) // URL para webhooks
                    .statementDescriptor("GOLDEN BURGERS") // Aparece en el resumen de tarjeta
                    .build();

            // Crear en MercadoPago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Guardar preference_id en el pedido
            pedido.setMercadopagoPreferenceId(preference.getId());
            pedido.setEstadoPago("PENDIENTE");
            pedidoRepository.save(pedido);

            // Retornar respuesta
            return IniciarPagoResponse.builder()
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint()) // URL de producción
                    .sandboxInitPoint(preference.getSandboxInitPoint()) // URL de prueba
                    .idPedido(idPedido)
                    .monto(pedido.getMontoTotal())
                    .mensaje("Redirigir al usuario a initPoint o sandboxInitPoint")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error creando preferencia de pago en MercadoPago", e);
        }
    }

    /**
     * Procesar notificación de pago (Webhook)
     */
    @Transactional
    public void procesarNotificacionPago(Long paymentId) {
        try {
            // Configurar SDK
            MercadoPagoConfig.setAccessToken(accessToken);

            // Obtener información del pago desde MercadoPago
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);

            // Extraer external_reference (PEDIDO-123)
            String externalReference = payment.getExternalReference();
            if (externalReference == null || !externalReference.startsWith("PEDIDO-")) {
                throw new RuntimeException("External reference inválida: " + externalReference);
            }

            Long idPedido = Long.parseLong(externalReference.replace("PEDIDO-", ""));

            // Buscar pedido
            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));

            // Actualizar datos del pago
            pedido.setMercadopagoPaymentId(payment.getId().toString());
            pedido.setMercadopagoStatus(payment.getStatus());
            pedido.setMercadopagoStatusDetail(payment.getStatusDetail());
            pedido.setMercadopagoPaymentType(payment.getPaymentTypeId());
            pedido.setMercadopagoPaymentMethod(payment.getPaymentMethodId());

            // Actualizar según el estado
            switch (payment.getStatus()) {
                case "approved":
                    pedido.setEstadoPago("APROBADO");
                    pedido.setMontoPagado(payment.getTransactionAmount().doubleValue());
                    
                    // Convertir OffsetDateTime a Timestamp
                    OffsetDateTime dateApproved = payment.getDateApproved();
                    if (dateApproved != null) {
                        pedido.setFechaPago(Timestamp.from(dateApproved.toInstant()));
                    }
                    
                    // Aquí puedes agregar lógica adicional:
                    // - Notificar a GESTIONVENTA para crear venta
                    // - Notificar a GESTIONCATALOGO para descontar stock
                    // - Enviar email de confirmación al cliente
                    break;

                case "rejected":
                    pedido.setEstadoPago("RECHAZADO");
                    break;

                case "pending":
                case "in_process":
                    pedido.setEstadoPago("PENDIENTE");
                    break;

                case "cancelled":
                    pedido.setEstadoPago("CANCELADO");
                    break;

                default:
                    pedido.setEstadoPago("DESCONOCIDO");
                    break;
            }

            pedidoRepository.save(pedido);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando notificación de pago", e);
        }
    }

    /**
     * Obtener estado del pago de un pedido
     */
    public String obtenerEstadoPago(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));
        return pedido.getEstadoPago();
    }
}
```

### Paso 4.4: Crear controlador PagoController

**Archivo**: `GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/controller/PagoController.java`

```java
package com.example.GestionPedidos.controller;

import com.example.GestionPedidos.dto.mercadopago.IniciarPagoRequest;
import com.example.GestionPedidos.dto.mercadopago.IniciarPagoResponse;
import com.example.GestionPedidos.service.MercadoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*") // Ajustar según tu frontend
public class PagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    /**
     * Iniciar proceso de pago con MercadoPago
     * 
     * POST /api/pagos/mercadopago/iniciar
     * Body: { "idPedido": 123, "emailCliente": "cliente@example.com" }
     */
    @PostMapping("/mercadopago/iniciar")
    public ResponseEntity<IniciarPagoResponse> iniciarPago(@RequestBody IniciarPagoRequest request) {
        try {
            IniciarPagoResponse response = mercadoPagoService.crearPreferenciaPago(
                    request.getIdPedido(),
                    request.getEmailCliente()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(IniciarPagoResponse.builder()
                            .mensaje("Error: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Webhook: MercadoPago notifica aquí cuando hay cambios en el pago
     * 
     * POST /api/pagos/webhook/mercadopago?id=123456&topic=payment
     * 
     * IMPORTANTE: Este endpoint NO debe tener autenticación JWT
     * porque MercadoPago lo llama directamente
     */
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<String> webhookMercadoPago(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String topic
    ) {
        try {
            // MercadoPago envía notificaciones de tipo "payment" o "merchant_order"
            if ("payment".equals(topic) && id != null) {
                mercadoPagoService.procesarNotificacionPago(id);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            // Siempre retornar 200 OK aunque haya error
            // MercadoPago reintenta si recibe error
            System.err.println("Error en webhook: " + e.getMessage());
            return ResponseEntity.ok("ERROR");
        }
    }

    /**
     * Consultar estado del pago de un pedido
     * 
     * GET /api/pagos/estado/{idPedido}
     */
    @GetMapping("/estado/{idPedido}")
    public ResponseEntity<Map<String, String>> consultarEstado(@PathVariable Long idPedido) {
        try {
            String estado = mercadoPagoService.obtenerEstadoPago(idPedido);
            return ResponseEntity.ok(Map.of("estado_pago", estado));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
```

### Paso 4.5: Permitir webhook sin autenticación JWT

**Archivo**: `GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/config/SecurityConfig.java`

Modificar el método `securityFilterChain` para permitir el webhook:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Endpoints públicos (sin autenticación)
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/health").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            
            // WEBHOOK DE MERCADOPAGO (público)
            .requestMatchers("/api/pagos/webhook/mercadopago").permitAll()
            
            // Todos los demás requieren autenticación
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### Paso 4.6: Compilar y verificar

```bash
cd GESTIONPEDIDO/GestionPedidos
./mvnw clean package -DskipTests
```

Si compila sin errores, estás listo para probar.

---

## 5. Integración en Frontend

### Paso 5.1: Crear servicio de pago (React)

**Archivo**: `src/services/pagoService.js`

```javascript
import axios from 'axios';

const API_URL = 'http://localhost:8083/api/pagos';

// Obtener token JWT del localStorage
const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return { Authorization: `Bearer ${token}` };
};

/**
 * Iniciar proceso de pago con MercadoPago
 * @param {number} idPedido - ID del pedido a pagar
 * @param {string} emailCliente - Email del cliente
 * @returns {Promise} - Respuesta con URL de pago
 */
export const iniciarPagoMercadoPago = async (idPedido, emailCliente) => {
  try {
    const response = await axios.post(
      `${API_URL}/mercadopago/iniciar`,
      {
        idPedido,
        emailCliente
      },
      {
        headers: getAuthHeader()
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error iniciando pago:', error);
    throw error;
  }
};

/**
 * Consultar estado del pago de un pedido
 * @param {number} idPedido - ID del pedido
 * @returns {Promise} - Estado del pago
 */
export const consultarEstadoPago = async (idPedido) => {
  try {
    const response = await axios.get(
      `${API_URL}/estado/${idPedido}`,
      {
        headers: getAuthHeader()
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error consultando estado:', error);
    throw error;
  }
};
```

### Paso 5.2: Componente de Confirmación de Pedido

**Archivo**: `src/components/ConfirmacionPedido.jsx`

```javascript
import React, { useState } from 'react';
import { iniciarPagoMercadoPago } from '../services/pagoService';

const ConfirmacionPedido = ({ pedido, emailCliente }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handlePagar = async () => {
    try {
      setLoading(true);
      setError(null);

      // Llamar al backend para iniciar pago
      const response = await iniciarPagoMercadoPago(pedido.idPedido, emailCliente);

      // Redirigir a MercadoPago
      // En producción usar: response.initPoint
      // En pruebas usar: response.sandboxInitPoint
      const urlPago = response.sandboxInitPoint || response.initPoint;

      if (urlPago) {
        // Redirigir al usuario a MercadoPago
        window.location.href = urlPago;
      } else {
        setError('No se pudo obtener URL de pago');
      }

    } catch (err) {
      setError('Error al procesar el pago: ' + (err.response?.data?.mensaje || err.message));
      setLoading(false);
    }
  };

  return (
    <div className="confirmacion-pedido">
      <h2>Resumen del Pedido #{pedido.idPedido}</h2>
      
      <div className="resumen">
        <p>Subtotal: ${pedido.montoSubtotal?.toLocaleString('es-CL')}</p>
        <p>Envío: ${pedido.montoEnvio?.toLocaleString('es-CL')}</p>
        <h3>Total: ${pedido.montoTotal?.toLocaleString('es-CL')}</h3>
      </div>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      <button
        onClick={handlePagar}
        disabled={loading}
        className="btn btn-primary btn-lg"
      >
        {loading ? 'Procesando...' : 'Pagar con MercadoPago'}
      </button>
    </div>
  );
};

export default ConfirmacionPedido;
```

### Paso 5.3: Páginas de resultado de pago

#### PagoExitoso.jsx

**Archivo**: `src/pages/PagoExitoso.jsx`

```javascript
import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { consultarEstadoPago } from '../services/pagoService';

const PagoExitoso = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [pedido, setPedido] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const idPedido = searchParams.get('pedido_id');
    const collectionId = searchParams.get('collection_id');
    const paymentId = searchParams.get('payment_id');

    if (idPedido) {
      verificarPago(idPedido);
    }
  }, []);

  const verificarPago = async (idPedido) => {
    try {
      const response = await consultarEstadoPago(idPedido);
      setPedido({ idPedido, estadoPago: response.estado_pago });
      setLoading(false);
    } catch (error) {
      console.error('Error verificando pago:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center p-5">Verificando pago...</div>;
  }

  return (
    <div className="container mt-5">
      <div className="card text-center">
        <div className="card-body">
          <div className="mb-4">
            <svg 
              width="80" 
              height="80" 
              fill="green" 
              viewBox="0 0 16 16"
            >
              <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
            </svg>
          </div>

          <h1 className="text-success">¡Pago Exitoso!</h1>
          <p className="lead">Tu pago ha sido procesado correctamente</p>
          
          {pedido && (
            <div className="mt-4">
              <p><strong>Pedido:</strong> #{pedido.idPedido}</p>
              <p><strong>Estado:</strong> {pedido.estadoPago}</p>
            </div>
          )}

          <div className="mt-4">
            <button 
              className="btn btn-primary me-2"
              onClick={() => navigate('/mis-pedidos')}
            >
              Ver mis pedidos
            </button>
            <button 
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              Volver al inicio
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PagoExitoso;
```

#### PagoFallido.jsx

**Archivo**: `src/pages/PagoFallido.jsx`

```javascript
import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

const PagoFallido = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const idPedido = searchParams.get('pedido_id');

  return (
    <div className="container mt-5">
      <div className="card text-center">
        <div className="card-body">
          <div className="mb-4">
            <svg 
              width="80" 
              height="80" 
              fill="red" 
              viewBox="0 0 16 16"
            >
              <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.354 4.646a.5.5 0 1 0-.708.708L7.293 8l-2.647 2.646a.5.5 0 0 0 .708.708L8 8.707l2.646 2.647a.5.5 0 0 0 .708-.708L8.707 8l2.647-2.646a.5.5 0 0 0-.708-.708L8 7.293 5.354 4.646z"/>
            </svg>
          </div>

          <h1 className="text-danger">Pago Rechazado</h1>
          <p className="lead">No se pudo procesar tu pago</p>
          
          {idPedido && (
            <p className="text-muted">Pedido #{idPedido}</p>
          )}

          <div className="mt-4">
            <p>Por favor, intenta nuevamente o utiliza otro método de pago</p>
          </div>

          <div className="mt-4">
            <button 
              className="btn btn-primary me-2"
              onClick={() => navigate(`/pedido/${idPedido}`)}
            >
              Reintentar pago
            </button>
            <button 
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              Volver al inicio
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PagoFallido;
```

#### PagoPendiente.jsx

**Archivo**: `src/pages/PagoPendiente.jsx`

```javascript
import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

const PagoPendiente = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const idPedido = searchParams.get('pedido_id');

  return (
    <div className="container mt-5">
      <div className="card text-center">
        <div className="card-body">
          <div className="mb-4">
            <svg 
              width="80" 
              height="80" 
              fill="orange" 
              viewBox="0 0 16 16"
            >
              <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
            </svg>
          </div>

          <h1 className="text-warning">Pago Pendiente</h1>
          <p className="lead">Tu pago está siendo procesado</p>
          
          {idPedido && (
            <p className="text-muted">Pedido #{idPedido}</p>
          )}

          <div className="mt-4">
            <p>Recibirás una notificación cuando se confirme el pago</p>
            <p className="text-muted">
              Esto puede tardar unos minutos
            </p>
          </div>

          <div className="mt-4">
            <button 
              className="btn btn-primary me-2"
              onClick={() => navigate('/mis-pedidos')}
            >
              Ver mis pedidos
            </button>
            <button 
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              Volver al inicio
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PagoPendiente;
```

### Paso 5.4: Configurar rutas en App.js

**Archivo**: `src/App.js`

```javascript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import PagoExitoso from './pages/PagoExitoso';
import PagoFallido from './pages/PagoFallido';
import PagoPendiente from './pages/PagoPendiente';
// ... otros imports

function App() {
  return (
    <Router>
      <Routes>
        {/* ... otras rutas ... */}
        
        {/* Rutas de resultado de pago */}
        <Route path="/pago/exitoso" element={<PagoExitoso />} />
        <Route path="/pago/fallido" element={<PagoFallido />} />
        <Route path="/pago/pendiente" element={<PagoPendiente />} />
      </Routes>
    </Router>
  );
}

export default App;
```

### Paso 5.5: Flujo completo en el frontend

```javascript
// Ejemplo de flujo completo desde el carrito hasta el pago

// 1. Usuario completa el pedido
const crearPedido = async (datosCarrito) => {
  const response = await axios.post('/api/pedidos', {
    productos: datosCarrito,
    direccionEntrega: direccion,
    // ... otros datos
  });
  
  return response.data; // { idPedido: 123, montoTotal: 15990, ... }
};

// 2. Después de crear el pedido, mostrar botón de pago
const pedido = await crearPedido(carrito);

// 3. Usuario hace clic en "Pagar"
const iniciarPago = async () => {
  const response = await iniciarPagoMercadoPago(
    pedido.idPedido, 
    'cliente@example.com'
  );
  
  // 4. Redirigir a MercadoPago
  window.location.href = response.sandboxInitPoint;
};

// 5. MercadoPago procesa el pago y redirige de vuelta
// - Éxito: /pago/exitoso?pedido_id=123&payment_id=456
// - Fallo: /pago/fallido?pedido_id=123
// - Pendiente: /pago/pendiente?pedido_id=123

// 6. Backend recibe webhook y actualiza el pedido automáticamente
```

---

## 6. Pruebas con Tarjetas de Test

### Tarjetas de Prueba para Chile (CLP)

Una vez que tengas todo configurado, usa estas tarjetas para probar:

#### ✅ Pago Aprobado

| Tarjeta | Número | CVV | Fecha | Resultado |
|---------|--------|-----|-------|-----------|
| **Visa** | `4509 9535 6623 3704` | 123 | 11/25 | ✅ Aprobado |
| **Mastercard** | `5031 7557 3453 0604` | 123 | 11/25 | ✅ Aprobado |
| **American Express** | `3711 803032 57522` | 1234 | 11/25 | ✅ Aprobado |

#### ❌ Pago Rechazado

| Tarjeta | Número | Resultado |
|---------|--------|-----------|
| **Visa** | `4074 0943 6766 7283` | ❌ Fondos insuficientes |
| **Mastercard** | `5031 4332 1540 6351` | ❌ Rechazado por otro motivo |

#### ⏳ Pago Pendiente

| Tarjeta | Número | Resultado |
|---------|--------|-----------|
| **Visa** | `4013 5406 8274 6260` | ⏳ Pendiente |

### Datos del titular (usar cualquiera)

```
Nombre: APRO (o cualquier nombre)
DNI/RUT: 12345678-9
Email: test_user_123@testuser.com
```

### Probar diferentes escenarios

```javascript
// 1. Pago exitoso
// Usar tarjeta: 4509 9535 6623 3704
// Resultado: MercadoPago redirige a /pago/exitoso
// Backend recibe webhook con status: "approved"

// 2. Pago rechazado
// Usar tarjeta: 4074 0943 6766 7283
// Resultado: MercadoPago redirige a /pago/fallido
// Backend recibe webhook con status: "rejected"

// 3. Pago pendiente
// Usar tarjeta: 4013 5406 8274 6260
// Resultado: MercadoPago redirige a /pago/pendiente
// Backend recibe webhook con status: "pending"
```

---

## 7. Troubleshooting

### Problema 1: Error "Access token inválido"

**Síntoma**:
```
Error: invalid_client
```

**Solución**:
- Verificar que copiaste el **Access Token de PRUEBA** (comienza con `TEST-`)
- Verificar que está en `application.properties` correctamente
- Reiniciar el microservicio GESTIONPEDIDO

### Problema 2: Webhook no se ejecuta

**Síntoma**: 
- El pago se procesa en MercadoPago
- Pero el pedido no se actualiza en la base de datos

**Causa**: 
MercadoPago no puede acceder a `http://localhost:8083`

**Solución para desarrollo local**:

1. **Usar ngrok** para exponer tu localhost:
   ```bash
   # Instalar ngrok: https://ngrok.com/
   ngrok http 8083
   ```

2. **Copiar URL pública** que ngrok te da:
   ```
   Forwarding: https://abc123.ngrok.io -> http://localhost:8083
   ```

3. **Actualizar application.properties**:
   ```properties
   mercadopago.notification.url=https://abc123.ngrok.io/api/pagos/webhook/mercadopago
   ```

4. **Reiniciar GESTIONPEDIDO**

5. **Probar nuevamente**: Ahora MercadoPago podrá enviar notificaciones

### Problema 3: Error CORS en frontend

**Síntoma**:
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solución**:

Verificar `@CrossOrigin` en `PagoController.java`:

```java
@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "http://localhost:3000") // Cambiar según tu frontend
public class PagoController {
    // ...
}
```

### Problema 4: Preferencia no se crea

**Síntoma**:
```
Error creando preferencia de pago en MercadoPago
```

**Revisar**:

1. **Logs del backend**:
   ```bash
   tail -f /opt/golden-burgers/logs/gestion-pedido.log
   ```

2. **Verificar que el pedido existe**:
   ```sql
   SELECT * FROM pedido WHERE id_pedido = 123;
   ```

3. **Verificar monto**:
   - MercadoPago requiere monto > 0
   - Verificar que `monto_total` no sea NULL

### Problema 5: Redirección infinita

**Síntoma**: 
Después de pagar, la página sigue redirigiendo

**Solución**:

Verificar que las URLs de retorno sean diferentes:
```properties
mercadopago.success.url=http://localhost:3000/pago/exitoso
mercadopago.failure.url=http://localhost:3000/pago/fallido
mercadopago.pending.url=http://localhost:3000/pago/pendiente
```

Y que las rutas estén configuradas en React Router.

### Problema 6: JWT no funciona en webhook

**Síntoma**:
```
401 Unauthorized en /api/pagos/webhook/mercadopago
```

**Solución**:

El webhook DEBE estar permitido sin JWT en `SecurityConfig.java`:

```java
.requestMatchers("/api/pagos/webhook/mercadopago").permitAll()
```

---

## 📋 Checklist Final de Implementación

### Backend
- [ ] Agregar dependencia de MercadoPago en `pom.xml`
- [ ] Configurar credenciales en `application.properties`
- [ ] Ejecutar script SQL para agregar columnas a tabla `pedido`
- [ ] Actualizar modelo `Pedido.java` con nuevos campos
- [ ] Crear DTOs en carpeta `dto/mercadopago/`
- [ ] Crear `MercadoPagoService.java`
- [ ] Crear `PagoController.java`
- [ ] Configurar webhook sin JWT en `SecurityConfig.java`
- [ ] Compilar: `./mvnw clean package`
- [ ] Iniciar microservicio: `java -jar target/*.jar`

### Frontend
- [ ] Crear `pagoService.js` con funciones de API
- [ ] Crear componente `ConfirmacionPedido.jsx`
- [ ] Crear página `PagoExitoso.jsx`
- [ ] Crear página `PagoFallido.jsx`
- [ ] Crear página `PagoPendiente.jsx`
- [ ] Configurar rutas en `App.js`
- [ ] Actualizar URLs según tu configuración
- [ ] Probar flujo completo

### Pruebas
- [ ] Registrarse en MercadoPago Developers
- [ ] Obtener credenciales de TEST
- [ ] Probar pago exitoso (tarjeta 4509 9535 6623 3704)
- [ ] Probar pago rechazado (tarjeta 4074 0943 6766 7283)
- [ ] Probar pago pendiente (tarjeta 4013 5406 8274 6260)
- [ ] Verificar que webhook actualiza la BD
- [ ] Verificar redirección correcta según resultado

### Opcional (Desarrollo local)
- [ ] Instalar ngrok para webhooks: https://ngrok.com/
- [ ] Configurar ngrok: `ngrok http 8083`
- [ ] Actualizar `notification.url` con URL de ngrok

---

## 🎯 Resumen del Flujo Completo

```
1. FRONTEND: Usuario completa pedido
   └─> POST /api/pedidos
   └─> Response: { idPedido: 123, montoTotal: 15990 }

2. FRONTEND: Usuario hace clic en "Pagar"
   └─> POST /api/pagos/mercadopago/iniciar
       Body: { idPedido: 123, emailCliente: "user@test.com" }
   └─> Response: { 
         preferenceId: "123-abc",
         sandboxInitPoint: "https://sandbox.mercadopago.cl/checkout/..."
       }

3. FRONTEND: Redirige a MercadoPago
   └─> window.location.href = sandboxInitPoint

4. USUARIO: Ingresa datos de tarjeta en MercadoPago
   └─> Tarjeta: 4509 9535 6623 3704

5. MERCADOPAGO: Procesa pago y redirige
   └─> https://tudominio.com/pago/exitoso?pedido_id=123&payment_id=456

6. MERCADOPAGO: Envía webhook a tu backend
   └─> POST /api/pagos/webhook/mercadopago?id=456&topic=payment

7. BACKEND: Procesa webhook
   └─> Consulta a MercadoPago: GET /v1/payments/456
   └─> Actualiza pedido en BD:
       - estado_pago = 'APROBADO'
       - mercadopago_payment_id = '456'
       - fecha_pago = NOW()

8. FRONTEND: Muestra confirmación
   └─> Página "¡Pago Exitoso!"
   └─> Pedido #123 confirmado
```

---

## 📞 Recursos Adicionales

- **Documentación oficial**: https://www.mercadopago.com/developers/es/docs
- **SDK Java**: https://github.com/mercadopago/sdk-java
- **Panel de desarrolladores**: https://www.mercadopago.cl/developers/panel
- **Tarjetas de prueba**: https://www.mercadopago.com/developers/es/docs/checkout-pro/additional-content/test-cards
- **Simulador de webhooks**: https://www.mercadopago.cl/developers/panel/webhooks

---

## ✅ Resultado Final

Al completar esta guía tendrás:

- ✅ Integración completa de MercadoPago en GESTIONPEDIDO
- ✅ Frontend con flujo de pago funcional
- ✅ Webhooks para actualización automática de pedidos
- ✅ Manejo de todos los estados de pago (aprobado, rechazado, pendiente)
- ✅ Sistema de pruebas con tarjetas de test
- ✅ Sin costo (usando sandbox de MercadoPago)

¡Listo para procesar pagos en Golden Burgers! 🍔💳

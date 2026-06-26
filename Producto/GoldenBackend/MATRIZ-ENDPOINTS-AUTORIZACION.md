# 🔐 Matriz de Endpoints y Niveles de Autorización

**Proyecto:** Golden Burgers - Backend Microservicios  
**Fecha:** 9 de noviembre de 2025  
**Arquitectura:** API Gateway (Firebase Auth) → JWT → Microservicios

---

## 📋 Roles del Sistema

| Rol | Descripción | Permisos Generales |
|-----|-------------|-------------------|
| `ADMIN` | Administrador del sistema | Acceso total a todos los endpoints |
| `TRABAJADOR` | Empleado de Golden Burgers | Lectura/escritura, sin eliminación de datos críticos |
| `CLIENTE` | Usuario registrado/comprador | Solo puede ver y gestionar sus propios datos |

---

## 🎯 Microservicio: GESTIONPEDIDO (Puerto 8083)

**Base Path:** `/api/pedidos`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/pedidos` | Listar todos los pedidos | `ADMIN`, `TRABAJADOR` | Vista completa de órdenes |
| GET | `/api/pedidos/{id}` | Obtener pedido por ID | `ADMIN`, `TRABAJADOR`, `CLIENTE`* | *Cliente solo si es dueño |
| GET | `/api/pedidos/cliente/{idCliente}` | Listar pedidos de un cliente | `ADMIN`, `TRABAJADOR`, `CLIENTE`* | ✅ **Endpoint clave** - *Cliente solo sus propios pedidos |
| POST | `/api/pedidos` | Crear nuevo pedido | `ADMIN`, `TRABAJADOR`, `CLIENTE` | Cualquier usuario autenticado puede hacer pedidos |
| DELETE | `/api/pedidos/{id}` | Cancelar/eliminar pedido | `ADMIN`, `CLIENTE`* | *Cliente solo puede cancelar sus propios pedidos |

### 📝 Implementación @PreAuthorize en PedidoController

```java
// GET /api/pedidos
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// GET /api/pedidos/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
// Validación adicional en servicio: verificar que cliente solo acceda a sus pedidos

// GET /api/pedidos/cliente/{idCliente}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
// Validación adicional: verificar que idCliente == usuario autenticado (si es CLIENTE)

// POST /api/pedidos
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")

// DELETE /api/pedidos/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
// Validación adicional: verificar propiedad si es CLIENTE
```

---

## 💰 Microservicio: GESTIONVENTA (Puerto 8082)

### VentaController
**Base Path:** `/api/ventas`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/ventas` | Listar todas las ventas | `ADMIN`, `TRABAJADOR` | Solo personal interno |
| GET | `/api/ventas/{id}` | Obtener venta por ID | `ADMIN`, `TRABAJADOR` | Solo personal interno |
| POST | `/api/ventas/desde-pedido/{id}` | Crear venta desde pedido | `ADMIN`, `TRABAJADOR` | Conversión pedido → venta |
| PUT | `/api/ventas/{id}` | Actualizar venta | `ADMIN`, `TRABAJADOR` | Modificar transacción |
| DELETE | `/api/ventas/{id}` | Eliminar venta | `ADMIN` | Solo administrador |

```java
// GET /api/ventas
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// GET /api/ventas/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// POST /api/ventas/desde-pedido/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// PUT /api/ventas/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// DELETE /api/ventas/{id}
@PreAuthorize("hasRole('ADMIN')")
```

---

### BoletaController
**Base Path:** `/api/boletas`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/boletas` | Listar todas las boletas | `ADMIN`, `TRABAJADOR` | Gestión de comprobantes |
| GET | `/api/boletas/{id}` | Obtener boleta por ID | `ADMIN`, `TRABAJADOR` | Ver detalle boleta |
| POST | `/api/boletas` | Crear boleta | `ADMIN`, `TRABAJADOR` | Generar comprobante |
| PUT | `/api/boletas/{id}` | Actualizar boleta | `ADMIN`, `TRABAJADOR` | Modificar comprobante |
| DELETE | `/api/boletas/{id}` | Eliminar boleta | `ADMIN` | Solo administrador |

```java
// GET /api/boletas
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// GET /api/boletas/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// POST /api/boletas
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// PUT /api/boletas/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// DELETE /api/boletas/{id}
@PreAuthorize("hasRole('ADMIN')")
```

---

### DashboardController
**Base Path:** `/api/dashboard`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/dashboard/resumen-ventas` | Resumen general de ventas | `ADMIN`, `TRABAJADOR` | KPIs generales |
| GET | `/api/dashboard/ventas-hoy` | Ventas del día actual | `ADMIN`, `TRABAJADOR` | Métricas diarias |
| GET | `/api/dashboard/ventas-mes-actual` | Ventas del mes actual | `ADMIN`, `TRABAJADOR` | Métricas mensuales |
| GET | `/api/dashboard/ventas-anio-actual` | Ventas del año actual | `ADMIN`, `TRABAJADOR` | Métricas anuales |
| GET | `/api/dashboard/ventas-por-mes` | Ventas agrupadas por mes | `ADMIN`, `TRABAJADOR` | Análisis temporal |
| GET | `/api/dashboard/kpis` | Indicadores clave de rendimiento | `ADMIN`, `TRABAJADOR` | Business intelligence |
| GET | `/api/dashboard/ventas-categoria` | Ventas por categoría producto | `ADMIN`, `TRABAJADOR` | Análisis de productos |
| GET | `/api/dashboard/ventas-ciudad` | Ventas por ciudad | `ADMIN`, `TRABAJADOR` | Análisis geográfico |
| GET | `/api/dashboard/total-ventas` | Total de ventas | `ADMIN`, `TRABAJADOR` | Suma total |

```java
// Todos los endpoints del Dashboard
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
```

**Nota:** Dashboard es exclusivo para análisis interno del negocio. Clientes NO tienen acceso.

---

### DevolucionController
**Base Path:** `/api/devoluciones`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/devoluciones` | Listar devoluciones | `ADMIN`, `TRABAJADOR` | Gestión de reembolsos |
| GET | `/api/devoluciones/{id}` | Obtener devolución por ID | `ADMIN`, `TRABAJADOR` | Detalle devolución |
| POST | `/api/devoluciones` | Crear devolución | `ADMIN`, `TRABAJADOR` | Procesar reembolso |
| PUT | `/api/devoluciones/{id}` | Actualizar devolución | `ADMIN`, `TRABAJADOR` | Modificar estado |
| DELETE | `/api/devoluciones/{id}` | Eliminar devolución | `ADMIN` | Solo administrador |

```java
// GET /api/devoluciones
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// GET /api/devoluciones/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// POST /api/devoluciones
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// PUT /api/devoluciones/{id}
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")

// DELETE /api/devoluciones/{id}
@PreAuthorize("hasRole('ADMIN')")
```

---

## 📧 Microservicio: GESTIONCONTACTO (Puerto 8085)

**Base Path:** `/api/mensajes`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/mensajes` | Listar todos los mensajes | `ADMIN` | Solo administrador ve todos |
| GET | `/api/mensajes/{id}` | Obtener mensaje por ID | `ADMIN` | Ver detalle específico |
| POST | `/api/mensajes` | Enviar mensaje de contacto | **PÚBLICO** | ⚠️ **Sin autenticación** - formulario contacto |
| PUT | `/api/mensajes/{id}` | Actualizar mensaje | `ADMIN` | Marcar como leído/respondido |
| DELETE | `/api/mensajes/{id}` | Eliminar mensaje | `ADMIN` | Limpiar bandeja |

```java
// GET /api/mensajes
@PreAuthorize("hasRole('ADMIN')")

// GET /api/mensajes/{id}
@PreAuthorize("hasRole('ADMIN')")

// POST /api/mensajes
// Sin @PreAuthorize - debe configurarse como ruta pública en SecurityConfig

// PUT /api/mensajes/{id}
@PreAuthorize("hasRole('ADMIN')")

// DELETE /api/mensajes/{id}
@PreAuthorize("hasRole('ADMIN')")
```

**Configuración adicional en SecurityConfig de GESTIONCONTACTO:**
```java
.requestMatchers(HttpMethod.POST, "/api/mensajes").permitAll()
```

---

## 🔑 Microservicio: GESTIONUSUARIO (Puerto 8081)

**Base Path:** `/api/usuarios`

| Método | Endpoint | Descripción | Autorización | Notas |
|--------|----------|-------------|--------------|-------|
| GET | `/api/usuarios` | Listar todos los usuarios | `ADMIN` | Gestión de usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID | `ADMIN`, `CLIENTE`* | *Cliente solo su propio perfil |
| GET | `/api/usuarios/email/{email}` | Buscar por email | `ADMIN` | Búsqueda administrativa |
| POST | `/api/usuarios` | Crear usuario | **PÚBLICO** | Registro de nuevos usuarios |
| PUT | `/api/usuarios/{id}` | Actualizar usuario | `ADMIN`, `CLIENTE`* | *Cliente solo su propio perfil |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario | `ADMIN` | Solo administrador |

```java
// Ya implementado en GESTIONUSUARIO (ver UserController.java)
```

---

## 🌐 API Gateway (Puerto 8080)

**Rutas Públicas (sin autenticación):**
- `POST /auth/login` - Login con Firebase + retorna JWT
- `POST /auth/refresh` - Renovar token JWT
- `GET /health` - Health check
- `GET /actuator/**` - Métricas (considerar restringir en producción)

**Rutas Protegidas:**
- Todas las demás rutas requieren JWT válido
- El Gateway valida token y reenvía request a microservicios

---

## 📊 Resumen por Rol

### 👑 ADMIN (Administrador)
- **Acceso total:** Todos los endpoints de todos los microservicios
- **Permisos especiales:** DELETE en ventas, boletas, devoluciones
- **Gestión:** Usuarios, configuración, análisis completo

### 👔 TRABAJADOR (Empleado)
- **GESTIONPEDIDO:** Ver/crear/modificar pedidos
- **GESTIONVENTA:** Ver/crear/modificar ventas, boletas, devoluciones (sin DELETE)
- **Dashboard:** Acceso completo a métricas
- **GESTIONCONTACTO:** Ver/responder mensajes
- **GESTIONUSUARIO:** Sin acceso

### 👤 CLIENTE (Usuario registrado)
- **GESTIONPEDIDO:** Ver solo SUS pedidos, crear pedidos, cancelar SUS pedidos
- **GESTIONVENTA:** Sin acceso directo (recibe comprobantes vía Webpay)
- **Dashboard:** Sin acceso
- **GESTIONCONTACTO:** Enviar mensajes (sin ver historial)
- **GESTIONUSUARIO:** Ver/editar solo SU propio perfil

### 🌍 PÚBLICO (Sin autenticación)
- **POST /api/mensajes** - Enviar mensaje de contacto
- **POST /api/usuarios** - Registrarse
- **POST /auth/login** - Iniciar sesión

---

## ⚠️ Validaciones Adicionales Necesarias

### En PedidoController:
```java
// Validar que CLIENTE solo acceda a sus propios pedidos
@GetMapping("/cliente/{idCliente}")
public ResponseEntity<List<Pedido>> listarPedidosPorCliente(@PathVariable Long idCliente) {
    // Obtener usuario autenticado desde SecurityContext
    String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
    String rolUsuario = // extraer rol del JWT
    
    // Si es CLIENTE, validar que idCliente coincida con su ID
    if (rolUsuario.equals("CLIENTE")) {
        Long idUsuarioAutenticado = // obtener ID desde JWT o servicio
        if (!idUsuarioAutenticado.equals(idCliente)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    // Proceder si es ADMIN/TRABAJADOR o si es CLIENTE accediendo a sus propios datos
    return ResponseEntity.ok(pedidoService.listarPedidosPorCliente(idCliente));
}
```

---

## 🔄 Flujo de Autenticación

```
1. Usuario → Firebase Login → API Gateway
2. API Gateway valida Firebase → consulta rol en GESTIONUSUARIO
3. API Gateway genera JWT con claims: {email, rol, idUsuario}
4. Frontend almacena JWT
5. Frontend envía JWT en header: Authorization: Bearer <token>
6. API Gateway valida JWT → reenvía a microservicio con JWT
7. Microservicio valida JWT → extrae rol → verifica @PreAuthorize
8. Si autorizado → ejecuta endpoint
9. Si no autorizado → 403 Forbidden
```

---

## 🎯 Endpoints Totales por Microservicio

| Microservicio | Total Endpoints | Públicos | ADMIN | TRABAJADOR | CLIENTE |
|---------------|-----------------|----------|-------|------------|---------|
| GESTIONPEDIDO | 5 | 0 | 5 | 4 | 3 |
| GESTIONVENTA | 19 | 0 | 19 | 18 | 0 |
| GESTIONCONTACTO | 5 | 1 | 5 | 0 | 0 |
| GESTIONUSUARIO | 6 | 1 | 6 | 0 | 2 |
| **TOTAL** | **35** | **2** | **35** | **22** | **5** |

---

## 📝 Notas Importantes

1. **Seguridad en Cascada:** El API Gateway ya valida JWT, pero los microservicios también validan por seguridad en profundidad.

2. **CORS:** Configurado solo en API Gateway para permitir frontend (localhost:8080 → localhost:3000/4200).

3. **Sesiones:** STATELESS en todos los microservicios (no se guardan sesiones).

4. **Tokens:** Validez 24 horas, renovables con `/auth/refresh`.

5. **Roles en JWT:** Incluidos en claims para evitar consultas adicionales a GESTIONUSUARIO.

6. **Webpay/Transbank:** Integración futura en GESTIONVENTA (POST /api/ventas/webpay/crear, POST /api/ventas/webpay/confirmar).

7. **Respuestas Mensajes:** Se responden por correo electrónico (no requiere endpoint de historial para cliente).

---

## 🚀 Siguiente Paso: Implementación

Aplicar las anotaciones `@PreAuthorize` según esta matriz en:
1. ✅ PedidoController (GESTIONPEDIDO)
2. ✅ VentaController, BoletaController, DashboardController, DevolucionController (GESTIONVENTA)
3. ✅ MensajeContactoController (GESTIONCONTACTO) + configurar POST como público

**Comando de validación:**
```bash
mvn clean compile -DskipTests
```

---

**Última actualización:** 9 de noviembre de 2025  
**Responsable:** Equipo Golden Burgers Backend

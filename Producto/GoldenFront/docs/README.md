# Documentación del Proyecto Golden Burgers

Bienvenido a la documentación completa del proyecto Golden Burgers. Esta carpeta contiene documentación detallada sobre todos los flujos, endpoints y arquitectura del sistema.

---

## Índice de Documentos

### 1. [Flujo de Autenticación](01-FLUJO-AUTENTICACION.md)
**Descripción:** Documentación completa del sistema de autenticación híbrido (Firebase + JWT interno).

**Contenido:**
- Flujo completo de registro de usuarios
- Flujo completo de inicio de sesión
- Generación y validación de tokens JWT
- Manejo de sesiones en frontend y backend
- Autorización por roles
- Rutas públicas vs protegidas
- Configuración de seguridad

**Ideal para:**
- Entender cómo funciona el login y registro
- Comprender el sistema de tokens
- Configurar roles y permisos
- Resolver problemas de autenticación

---

### 2. [Endpoints del Frontend](02-FRONTEND-ENDPOINTS-CLIENTE.md)
**Descripción:** Catálogo completo de todos los endpoints llamados desde las páginas del frontend.

**Contenido:**
- Endpoints de autenticación
- Endpoints de gestión de usuarios y clientes
- Endpoints de catálogo de productos
- Endpoints de gestión de pedidos
- Endpoints de gestión de pagos
- Endpoints de gestión de ventas y boletas
- Endpoints de dashboard
- Endpoints de contacto
- Resumen de permisos por rol

**Ideal para:**
- Saber qué endpoints están disponibles
- Entender qué datos se envían y reciben
- Ver ejemplos de request y response
- Conocer los permisos necesarios para cada endpoint

---

### 3. [Arquitectura del Backend](03-BACKEND-MICROSERVICIOS.md)
**Descripción:** Documentación completa de la arquitectura de microservicios del backend.

**Contenido:**
- Arquitectura general del sistema
- API Gateway: componentes y responsabilidades
- Microservicio GESTIONUSUARIO: modelos, DTOs, controladores
- Microservicio GESTIONCATALOGO: catálogo y Firebase Storage
- Microservicio GESTIONPEDIDO: pedidos y pagos con Mercado Pago
- Microservicio GESTIONVENTA: ventas, boletas y dashboard
- Microservicio GESTIONCONTACTO: mensajes de contacto
- Esquema de base de datos Oracle
- Comunicación entre microservicios

**Ideal para:**
- Entender la arquitectura del backend
- Conocer los modelos de datos
- Ver qué microservicio maneja cada funcionalidad
- Comprender la comunicación entre servicios

---

### 4. [Flujo Completo de Endpoints](04-FLUJO-COMPLETO-ENDPOINTS.md)
**Descripción:** Documentación de los flujos completos de negocio desde frontend hasta backend.

**Contenido:**
- Flujo de registro e inicio de sesión
- Flujo de navegación y catálogo
- Flujo de compra (crear pedido)
- Flujo de pago con Mercado Pago
- Flujo de gestión de perfil
- Flujo de gestión de productos (Admin)
- Flujo de gestión de pedidos (Admin)
- Flujo de dashboard y reportes (Admin)
- Flujo de contacto
- Diagramas de comunicación

**Ideal para:**
- Entender el flujo completo de una funcionalidad
- Ver cómo se comunican frontend y backend
- Comprender el ciclo de vida de un pedido
- Entender la integración con Mercado Pago

---

## Navegación Rápida por Requisitos Funcionales

### Para Clientes

**Registro y Login:**
- [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md) - Sección "Flujo de Registro e Inicio de Sesión"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 1

**Ver Productos:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Catálogo de Productos"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 2

**Hacer Pedido:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Gestión de Pedidos"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 3

**Pagar con Mercado Pago:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Gestión de Pagos"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 4

**Mi Perfil:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Gestión de Usuarios y Clientes"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 5

**Contacto:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Contacto"
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 9

### Para Administradores

**Gestión de Productos:**
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 6
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONCATALOGO"

**Gestión de Pedidos:**
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 7
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONPEDIDO"

**Dashboard y Reportes:**
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 8
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Dashboard (Admin)"

**Gestión de Clientes:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Gestión de Usuarios y Clientes"

**Gestión de Ventas:**
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Gestión de Ventas"
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONVENTA"

---

## Búsqueda por Tecnología

### Firebase
- [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md) - Autenticación con Firebase
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "FirebaseStorageService"

### JWT
- [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md) - Generación y validación de JWT
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "JwtService" y "JwtAuthenticationFilter"

### Mercado Pago
- [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 4
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONPEDIDO"

### React
- [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Todos los servicios del frontend

### Spring Boot
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - Todos los microservicios

### Oracle Database
- [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Base de Datos Oracle"

---

## Búsqueda por Archivo

### Frontend

**Páginas (src/pages):**
- `Login.jsx` → [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md)
- `CatalogoPag.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 2
- `CheckOut.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 3 y 4
- `MiPerfilPag.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 5
- `gestionProductos.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 6
- `gestionPedidos.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 7
- `dashboard.jsx` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 8

**Servicios (src/services):**
- `usuariosService.js` → [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Gestión de Usuarios"
- `catalogoService.js` → [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Catálogo de Productos"
- `pedidosService.js` → [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Gestión de Pedidos"
- `pagoService.js` → [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Gestión de Pagos"
- `dashboardService.js` → [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Dashboard"

### Backend

**API Gateway:**
- `AuthController.java` → [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md)
- `ProxyController.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "API Gateway"
- `JwtAuthenticationFilter.java` → [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md)

**GESTIONUSUARIO:**
- `ClienteController.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONUSUARIO"
- `Usuario.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Modelos de GESTIONUSUARIO"

**GESTIONCATALOGO:**
- `CatalogoController.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONCATALOGO"
- `Producto.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Modelos de GESTIONCATALOGO"

**GESTIONPEDIDO:**
- `PedidoController.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONPEDIDO"
- `PagoController.java` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 4

**GESTIONVENTA:**
- `VentaController.java` → [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Microservicio: GESTIONVENTA"
- `DashboardController.java` → [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 8

---

## Casos de Uso Comunes

### Problema: "¿Cómo funciona el login?"
**Solución:** Lee [01-FLUJO-AUTENTICACION.md](01-FLUJO-AUTENTICACION.md) - Sección "Inicio de Sesión"

### Problema: "¿Qué endpoints necesito para mostrar productos?"
**Solución:** Lee [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - Sección "Catálogo de Productos"

### Problema: "¿Cómo se crea un pedido?"
**Solución:** Lee [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 3

### Problema: "¿Cómo integro Mercado Pago?"
**Solución:** Lee [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 4

### Problema: "¿Qué modelo de datos usa Pedido?"
**Solución:** Lee [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Modelos de GESTIONPEDIDO"

### Problema: "¿Cómo se comunican los microservicios?"
**Solución:** Lee [03-BACKEND-MICROSERVICIOS.md](03-BACKEND-MICROSERVICIOS.md) - "Comunicación Entre Microservicios"

### Problema: "¿Qué rol necesito para eliminar productos?"
**Solución:** Lee [02-FRONTEND-ENDPOINTS-CLIENTE.md](02-FRONTEND-ENDPOINTS-CLIENTE.md) - "Resumen de Permisos por Rol"

### Problema: "¿Cómo subo imágenes a Firebase?"
**Solución:** Lee [04-FLUJO-COMPLETO-ENDPOINTS.md](04-FLUJO-COMPLETO-ENDPOINTS.md) - Sección 6 (Subir Imagen de Producto)

---

## Estructura de Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| Frontend | 5173 (dev) | http://localhost:5173 |
| API Gateway | 8080 | http://161.153.219.128:8080 |
| GESTIONUSUARIO | 8081 | http://localhost:8081 |
| GESTIONCATALOGO | 8082 | http://localhost:8082 |
| GESTIONPEDIDO | 8083 | http://localhost:8083 |
| GESTIONVENTA | 8084 | http://localhost:8084 |
| GESTIONCONTACTO | 8085 | http://localhost:8085 |

---

## Roles del Sistema

| ID | Nombre | Permisos |
|----|--------|----------|
| 1 | Cliente | Acceso a funcionalidades de cliente (ver productos, hacer pedidos, ver su perfil) |
| 2 | Trabajador | Acceso a funcionalidades de administración limitadas (ver pedidos, gestionar productos) |
| 3 | Admin | Acceso completo a todas las funcionalidades del sistema |

---

## Contribuciones

Esta documentación fue generada automáticamente analizando el código fuente del proyecto Golden Burgers.

**Fecha de creación:** 2025-01-27
**Versión:** 1.0

---

## Licencia

Este proyecto es desarrollado para fines académicos en DUOC UC.

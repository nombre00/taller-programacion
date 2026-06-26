# Flujo Completo de Endpoints - Golden Burgers

## Resumen General

Este documento presenta el flujo completo de endpoints de la aplicación Golden Burgers, mostrando cómo los requisitos funcionales se implementan a través de los diferentes microservicios. Cada sección describe un flujo de negocio completo desde el frontend hasta el backend.

---

## Índice de Flujos

1. [Flujo de Registro e Inicio de Sesión](#1-flujo-de-registro-e-inicio-de-sesión)
2. [Flujo de Navegación y Catálogo](#2-flujo-de-navegación-y-catálogo)
3. [Flujo de Compra (Pedido)](#3-flujo-de-compra-pedido)
4. [Flujo de Pago con Mercado Pago](#4-flujo-de-pago-con-mercado-pago)
5. [Flujo de Gestión de Perfil](#5-flujo-de-gestión-de-perfil)
6. [Flujo de Gestión de Productos (Admin)](#6-flujo-de-gestión-de-productos-admin)
7. [Flujo de Gestión de Pedidos (Admin)](#7-flujo-de-gestión-de-pedidos-admin)
8. [Flujo de Dashboard y Reportes (Admin)](#8-flujo-de-dashboard-y-reportes-admin)
9. [Flujo de Contacto](#9-flujo-de-contacto)

---

## 1. Flujo de Registro e Inicio de Sesión

### Registro de Nuevo Cliente

**Pasos del Usuario:**
1. Usuario accede a la página de Login
2. Hace clic en "Regístrate"
3. Ingresa nombre, email y contraseña
4. Hace clic en "Registrarse"

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: Login.jsx                                         │
│ handleRegisterSubmit()                                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 1. createUserWithEmailAndPassword(email, password)
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FIREBASE AUTHENTICATION                                     │
│ - Crea cuenta con email/password                           │
│ - Retorna: firebaseUid                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 2. POST /api/clientes
           │    Body: { idUsuario: firebaseUid, nombreCliente, email }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Proxy → GESTIONUSUARIO                                    │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController.registerCliente()        │
│                                                             │
│ 1. Verificar que firebaseUid no exista                     │
│ 2. Verificar que email no exista                           │
│ 3. Buscar rol "Cliente" (id=1)                             │
│ 4. Crear Usuario en tabla USUARIO                          │
│ 5. Crear Cliente en tabla CLIENTE                          │
│ 6. Retornar ClienteDTO                                     │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/Login.jsx:125-185` (Función handleRegisterSubmit)
- `src/services/usuariosService.js:18-40` (registrarCliente)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/controller/ProxyController.java:43-46` (Proxy clientes)
- `backGoldenBurgers/GESTIONUSUARIO/controller/ClienteController.java:109-114` (registerCliente)
- `backGoldenBurgers/GESTIONUSUARIO/service/ClienteService.java` (registerCliente)
- `backGoldenBurgers/GESTIONUSUARIO/model/Usuario.java` (Modelo Usuario)
- `backGoldenBurgers/GESTIONUSUARIO/model/Cliente.java` (Modelo Cliente)

**DTOs:**
- `RegistrarCliente` (Input)
- `ClienteDTO` (Output)

---

### Inicio de Sesión

**Pasos del Usuario:**
1. Usuario ingresa email y contraseña
2. Hace clic en "Iniciar Sesión"

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: Login.jsx                                         │
│ handleLoginSubmit()                                         │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 1. signInWithEmailAndPassword(email, password)
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FIREBASE AUTHENTICATION                                     │
│ - Valida credenciales                                       │
│ - Retorna: firebaseToken, firebaseUid                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 2. POST /api/auth/login
           │    Body: { firebaseToken }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY: AuthController.login()                        │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthenticationService.authenticateWithFirebase()           │
│                                                             │
│ 1. Validar token de Firebase (FirebaseAuth.verifyIdToken) │
│ 2. Extraer uid y email del token                           │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 3. GET /api/usuarios/firebase/{uid}
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: UsuarioController.getUserByFirebaseUid()   │
│                                                             │
│ - Buscar usuario por uid en tabla USUARIO                  │
│ - Retornar: { firebaseUid, email, rolId, rolNombre }      │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthenticationService                                       │
│                                                             │
│ 4. Generar JWT interno con:                                │
│    - subject: uid                                           │
│    - claims: email, rolId, rolNombre                       │
│    - expiración: 24 horas                                  │
│                                                             │
│ 5. Construir LoginResponse                                 │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Retorna: { internalToken, user, expiresIn }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: Login.jsx                                         │
│                                                             │
│ - Guardar en localStorage:                                 │
│   * authToken                                              │
│   * user                                                   │
│   * userId                                                 │
│   * userRole                                               │
│                                                             │
│ - Redirigir según rol:                                     │
│   * Admin/Trabajador → /admin/dashboard                    │
│   * Cliente → /inicio                                      │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/Login.jsx:40-122` (handleLoginSubmit)
- `src/config/api.js` (Configuración Axios)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/controller/AuthController.java:31-49` (login)
- `backGoldenBurgers/API-GATEWAY/service/AuthenticationService.java:33-75` (authenticateWithFirebase)
- `backGoldenBurgers/API-GATEWAY/service/JwtService.java:46-59` (generateToken)
- `backGoldenBurgers/GESTIONUSUARIO/controller/UsuarioController.java:28-48` (getUserByFirebaseUid)

**DTOs:**
- `LoginRequest` (Input)
- `LoginResponse` (Output)
- `UserDTO`

---

## 2. Flujo de Navegación y Catálogo

### Ver Productos Disponibles

**Pasos del Usuario:**
1. Usuario accede a la página de Catálogo
2. Ve la lista de productos disponibles

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CatalogoPag.jsx                                   │
│ useEffect() → obtenerProductosDisponibles()                 │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/catalogo/productos
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY (Sin validar JWT - Ruta pública)               │
│ - Proxy → GESTIONCATALOGO                                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCATALOGO: CatalogoController.obtenerProductos()     │
│                                                             │
│ - Llamar CatalogoService.obtenerDisponibles()              │
│ - Query SQL: SELECT * FROM PRODUCTO WHERE disponible = 1   │
│ - Mapear a ProductoDTO                                     │
│ - Retornar List<ProductoDTO>                               │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/CatalogoPag.jsx` (Página de catálogo)
- `src/services/catalogoService.js:18-36` (obtenerProductosDisponibles)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/controller/ProxyController.java:65-74` (Proxy catalogo)
- `backGoldenBurgers/GESTIONCATALOGO/controller/CatalogoController.java:38-42` (obtenerProductos)
- `backGoldenBurgers/GESTIONCATALOGO/service/CatalogoService.java` (obtenerDisponibles)
- `backGoldenBurgers/GESTIONCATALOGO/model/Producto.java` (Modelo Producto)

**DTOs:**
- `ProductoDTO` (Output)

---

### Filtrar por Categoría

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CatalogoPag.jsx                                   │
│ handleCategoriaChange() → obtenerProductosPorCategoria()    │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/catalogo/productos/categoria/1
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCATALOGO: CatalogoController                         │
│ .obtenerPorCategoria(idCategoria)                           │
│                                                             │
│ - Query SQL:                                                │
│   SELECT * FROM PRODUCTO                                    │
│   WHERE id_categoria = :idCategoria AND disponible = 1     │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/services/catalogoService.js:56-65` (obtenerProductosPorCategoria)

**Backend:**
- `backGoldenBurgers/GESTIONCATALOGO/controller/CatalogoController.java:57-62` (obtenerPorCategoria)

---

### Buscar Productos

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CatalogoPag.jsx                                   │
│ handleSearch() → buscarProductosPorNombre(searchTerm)       │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/catalogo/productos/buscar?nombre=hamburguesa
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCATALOGO: CatalogoController.buscarPorNombre()      │
│                                                             │
│ - Query SQL:                                                │
│   SELECT * FROM PRODUCTO                                    │
│   WHERE LOWER(nombre_producto) LIKE '%hamburguesa%'        │
│   AND disponible = 1                                       │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/services/catalogoService.js:70-81` (buscarProductosPorNombre)

**Backend:**
- `backGoldenBurgers/GESTIONCATALOGO/controller/CatalogoController.java:64-69` (buscarPorNombre)

---

## 3. Flujo de Compra (Pedido)

### Agregar Productos al Carrito

**Nota:** El carrito se maneja en el estado local del frontend (useState en React)

**Archivos Involucrados:**
- `src/pages/client/CatalogoPag.jsx` (Estado del carrito)
- `src/components/CartContext.jsx` (Context API para carrito global)

---

### Crear Pedido (Checkout)

**Pasos del Usuario:**
1. Usuario hace clic en "Ir a pagar"
2. Selecciona dirección de entrega
3. Selecciona método de pago
4. Confirma el pedido

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CheckOut.jsx                                      │
│ handleConfirmarPedido()                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 1. Obtener datos del cliente por Firebase UID
           │    GET /api/clientes/usuario/{firebaseUid}
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController                           │
│ .getClienteByUsuarioId(firebaseUid)                         │
│                                                             │
│ - Retornar: ClienteDTO con idCliente                       │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 2. Obtener direcciones del cliente
           │    GET /api/clientes/{idCliente}/direcciones
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController                           │
│ .getDireccionesByCliente(idCliente)                         │
│                                                             │
│ - Retornar: List<DireccionClienteDTO>                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 3. Usuario selecciona dirección y confirma
           │    POST /api/pedidos/completo
           │    Body: PedidoDTO con detalles
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Proxy → GESTIONPEDIDO                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PedidoController.crearPedido()              │
│                                                             │
│ 1. Crear registro en tabla PEDIDO                          │
│    - Estado: 1 (Pendiente de Pago)                         │
│    - Calcular montos                                       │
│                                                             │
│ 2. Crear registros en tabla DETALLE_PEDIDO                 │
│    - Por cada producto en el carrito                       │
│    - Guardar: idProducto, cantidad, precioUnitario         │
│                                                             │
│ 3. Retornar Pedido completo con detalles                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Retorna: Pedido con idPedido = 127
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CheckOut.jsx                                      │
│                                                             │
│ - Guardar idPedido en estado                               │
│ - Redirigir a página de pago                               │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/CheckOut.jsx` (Página de checkout)
- `src/services/usuariosService.js:108-116` (obtenerClientePorUid)
- `src/services/usuariosService.js:246-254` (obtenerDireccionesPorCliente)
- `src/services/pedidosService.js:44-52` (crearPedido)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/controller/ProxyController.java:90-93` (Proxy pedidos)
- `backGoldenBurgers/GESTIONPEDIDO/controller/PedidoController.java:89-120` (crearPedido)
- `backGoldenBurgers/GESTIONPEDIDO/service/PedidoService.java` (crearPedidoConDetalles)
- `backGoldenBurgers/GESTIONPEDIDO/model/Pedido.java` (Modelo Pedido)
- `backGoldenBurgers/GESTIONPEDIDO/model/DetallePedido.java` (Modelo DetallePedido)

**DTOs:**
- `PedidoDTO` (Input/Output)
- `DetallePedidoDTO`

---

## 4. Flujo de Pago con Mercado Pago

### Crear Preferencia de Pago

**Pasos del Usuario:**
1. Usuario hace clic en "Pagar con Mercado Pago"

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CheckOut.jsx                                      │
│ handlePagarConMercadoPago()                                 │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ POST /api/pagos/crear-preferencia
           │ Body: {
           │   idPedido: 127,
           │   montoPago: 17000,
           │   descripcion: "Pedido #127",
           │   emailPagador: "cliente@gmail.com",
           │   nombrePagador: "Juan Pérez"
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Proxy → GESTIONPEDIDO                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PagoController.crearPreferenciaPago()       │
│                                                             │
│ - Llamar PagoService.crearPreferenciaPago()                │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ PagoService                                                 │
│                                                             │
│ 1. Crear PreferenceRequest de Mercado Pago                 │
│    - title, quantity, unit_price                           │
│    - back_urls (success, failure, pending)                 │
│    - auto_return: "approved"                               │
│    - notification_url: webhook                             │
│                                                             │
│ 2. Llamar API de Mercado Pago                              │
│    POST https://api.mercadopago.com/checkout/preferences   │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Mercado Pago retorna:
           │ {
           │   id: "123456-abc-123",
           │   init_point: "https://www.mercadopago.cl/checkout/v1/redirect?pref_id=..."
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ PagoService                                                 │
│                                                             │
│ 3. Crear registro en tabla PAGO                            │
│    - idPedido: 127                                         │
│    - montoPago: 17000                                      │
│    - estadoPago: 1 (Pendiente)                             │
│    - idPreferenciaMpos: "123456-abc-123"                   │
│    - respuestaMercadoPago: URL de pago                     │
│                                                             │
│ 4. Retornar Pago con URL                                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Retorna: {
           │   idPago: 123,
           │   urlPago: "https://www.mercadopago.cl/checkout/..."
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: CheckOut.jsx                                      │
│                                                             │
│ - Redirigir al usuario a la URL de Mercado Pago           │
│   window.location.href = urlPago                           │
└─────────────────────────────────────────────────────────────┘
           │
           │ Usuario completa el pago en Mercado Pago
           ▼
┌─────────────────────────────────────────────────────────────┐
│ MERCADO PAGO                                                │
│                                                             │
│ 1. Usuario ingresa datos de pago                           │
│ 2. Mercado Pago procesa el pago                            │
│ 3. Redirige según resultado:                               │
│    - Aprobado → /pago-exitoso                              │
│    - Rechazado → /pago-fallido                             │
│    - Pendiente → /pago-pendiente                           │
│                                                             │
│ 4. Envía notificación al webhook (POST /api/pagos/webhook) │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/CheckOut.jsx` (handlePagarConMercadoPago)
- `src/services/pagoService.js:18-44` (crearPreferenciaPago)
- `src/pages/client/PagoExitoPage.jsx` (Página de éxito)

**Backend:**
- `backGoldenBurgers/GESTIONPEDIDO/controller/PagoController.java:43-70` (crearPreferenciaPago)
- `backGoldenBurgers/GESTIONPEDIDO/service/PagoService.java` (crearPreferenciaPago)
- `backGoldenBurgers/GESTIONPEDIDO/model/Pago.java` (Modelo Pago)

**DTOs:**
- `CrearPreferenciaRequest` (Input)

---

### Webhook de Mercado Pago

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ MERCADO PAGO                                                │
│ POST /api/pagos/webhook?type=payment&id=123                │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ (Sin JWT - Endpoint público)
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY (Ruta pública permitida)                       │
│ - Proxy → GESTIONPEDIDO                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PagoController.webhookMercadoPago()         │
│                                                             │
│ - Llamar PagoService.procesarWebhook(tipo, id, idPago)     │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ PagoService                                                 │
│                                                             │
│ 1. Llamar API de Mercado Pago para obtener info del pago   │
│    GET https://api.mercadopago.com/v1/payments/{id}        │
│                                                             │
│ 2. Buscar Pago en BD por idPagoMpos                        │
│                                                             │
│ 3. Actualizar estado según status de MP:                   │
│    - approved → estadoPago = 2 (Aprobado)                  │
│    - rejected → estadoPago = 3 (Rechazado)                 │
│    - pending → estadoPago = 1 (Pendiente)                  │
│                                                             │
│ 4. Si aprobado, actualizar pedido a "Pagado"               │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Backend:**
- `backGoldenBurgers/API-GATEWAY/filter/JwtAuthenticationFilter.java:75-78` (Permitir webhook)
- `backGoldenBurgers/GESTIONPEDIDO/controller/PagoController.java:178-192` (webhookMercadoPago)
- `backGoldenBurgers/GESTIONPEDIDO/service/PagoService.java` (procesarWebhook)

---

### Procesar Pedido Pagado y Crear Venta

**Pasos del Usuario:**
1. Usuario ve "Pago exitoso"
2. Sistema procesa automáticamente

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: PagoExitoPage.jsx                                 │
│ useEffect() → actualizarPedidoAPagado(idPedido)             │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ PUT /api/pedidos/procesar/127
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Proxy → GESTIONPEDIDO                                     │
│ - Pasar token en header X-Internal-Token                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PedidoController                             │
│ .actualizarEstadoPedidoPagado(idPedido, token)             │
│                                                             │
│ 1. Cambiar estado del pedido a "Pagado" (id=2)             │
│    UPDATE PEDIDO SET id_estado_pedido = 2                  │
│    WHERE id_pedido = 127                                   │
│                                                             │
│ 2. Llamar VentaClienteService                              │
│    .crearVentaDesdePedido(idPedido, token)                 │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ POST /api/ventas/desde-pedido/127
           │ Headers: X-Internal-Token
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONVENTA: VentaController                               │
│ .crearVentaDesdePedido(id, token)                           │
│                                                             │
│ - Llamar VentaService                                      │
│   .crearVentaDesdePedidoconBoleta(id, token)               │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ VentaService                                                │
│                                                             │
│ 1. Llamar a GESTIONPEDIDO para obtener pedido              │
│    GET /api/pedidos/127 (con token en header)              │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Retorna: PedidoDTO completo
           ▼
┌─────────────────────────────────────────────────────────────┐
│ VentaService                                                │
│                                                             │
│ 2. Crear registro en tabla VENTA                           │
│    INSERT INTO VENTA (id_pedido, monto_total_venta,        │
│                       fecha_venta)                          │
│    VALUES (127, 17000, NOW())                              │
│                                                             │
│ 3. Crear registro en tabla BOLETA                          │
│    INSERT INTO BOLETA (id_venta, numero_boleta,            │
│                        fecha_emision, monto_total,          │
│                        nombre_cliente)                      │
│    VALUES (venta.idVenta, generarNumeroBoleta(),           │
│            NOW(), 17000, "Juan Pérez")                     │
│                                                             │
│ 4. Retornar Venta completa                                 │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: PagoExitoPage.jsx                                 │
│                                                             │
│ - Mostrar mensaje de éxito                                 │
│ - Mostrar botón "Ver mi pedido"                            │
│ - Limpiar carrito                                          │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/PagoExitoPage.jsx`
- `src/services/pedidosService.js:81-93` (actualizarPedidoAPagado)

**Backend:**
- `backGoldenBurgers/GESTIONPEDIDO/controller/PedidoController.java:171-188` (actualizarEstadoPedidoPagado)
- `backGoldenBurgers/GESTIONPEDIDO/service/VentaClienteService.java` (crearVentaDesdePedido)
- `backGoldenBurgers/GESTIONVENTA/controller/VentaController.java:88-102` (crearVentaDesdePedido)
- `backGoldenBurgers/GESTIONVENTA/service/VentaService.java` (crearVentaDesdePedidoconBoleta)
- `backGoldenBurgers/GESTIONVENTA/model/Venta.java` (Modelo Venta)
- `backGoldenBurgers/GESTIONVENTA/model/Boleta.java` (Modelo Boleta)

---

## 5. Flujo de Gestión de Perfil

### Ver Mi Perfil

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: MiPerfilPag.jsx                                   │
│ useEffect() → cargarDatosPerfil()                           │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 1. Obtener firebaseUid de localStorage
           │    GET /api/clientes/usuario/{firebaseUid}
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController                           │
│ .getClienteByUsuarioId(firebaseUid)                         │
│                                                             │
│ - SELECT * FROM CLIENTE c                                   │
│   JOIN USUARIO u ON c.id_usuario = u.id_usuario            │
│   WHERE u.id_usuario = :firebaseUid                        │
│                                                             │
│ - Retornar: ClienteDTO completo                            │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 2. Obtener direcciones del cliente
           │    GET /api/clientes/{idCliente}/direcciones
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController                           │
│ .getDireccionesByCliente(idCliente)                         │
│                                                             │
│ - SELECT * FROM DIRECCION_CLIENTE dc                        │
│   JOIN CIUDAD c ON dc.id_ciudad = c.id_ciudad              │
│   WHERE dc.id_cliente = :idCliente                         │
│                                                             │
│ - Retornar: List<DireccionClienteDTO>                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ 3. Obtener pedidos del cliente
           │    GET /api/pedidos/cliente/{idCliente}
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PedidoController                             │
│ .listarPedidosPorCliente(idCliente)                         │
│                                                             │
│ - SELECT * FROM PEDIDO                                      │
│   WHERE id_cliente = :idCliente                            │
│   ORDER BY fecha_pedido DESC                               │
│                                                             │
│ - Retornar: List<Pedido>                                   │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/MiPerfilPag.jsx`
- `src/services/usuariosService.js:108-116` (obtenerClientePorUid)
- `src/services/usuariosService.js:246-254` (obtenerDireccionesPorCliente)
- `src/services/pedidosService.js:31-39` (getPedidosPorCliente)

**Backend:**
- `backGoldenBurgers/GESTIONUSUARIO/controller/ClienteController.java:264-271` (getClienteByUsuarioId)
- `backGoldenBurgers/GESTIONUSUARIO/controller/ClienteController.java:622-629` (getDireccionesByCliente)
- `backGoldenBurgers/GESTIONPEDIDO/controller/PedidoController.java:73-81` (listarPedidosPorCliente)

---

### Actualizar Perfil

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: MiPerfilPag.jsx                                   │
│ handleActualizarPerfil()                                    │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ PUT /api/clientes/perfil
           │ Body: {
           │   nombreCliente: "Juan Carlos Pérez",
           │   email: "nuevo@email.com",
           │   telefonoCliente: "912345678"
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Extraer firebaseUid del token                            │
│ - Proxy → GESTIONUSUARIO                                    │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONUSUARIO: ClienteController.actualizarPerfil()       │
│                                                             │
│ 1. Extraer firebaseUid del CustomUserDetails               │
│    (Spring Security)                                        │
│                                                             │
│ 2. Buscar cliente por firebaseUid                          │
│                                                             │
│ 3. Validar que nuevo email no esté en uso                  │
│                                                             │
│ 4. Actualizar registros:                                   │
│    UPDATE USUARIO SET email = :email                       │
│    WHERE id_usuario = :firebaseUid                         │
│                                                             │
│    UPDATE CLIENTE SET nombre_cliente = :nombre,            │
│                       telefono_cliente = :telefono         │
│    WHERE id_usuario = :firebaseUid                         │
│                                                             │
│ 5. Retornar ClienteDTO actualizado                         │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/services/usuariosService.js:167-175` (actualizarPerfilCliente)

**Backend:**
- `backGoldenBurgers/GESTIONUSUARIO/controller/ClienteController.java:432-448` (actualizarPerfil)
- `backGoldenBurgers/GESTIONUSUARIO/service/ClienteService.java` (actualizarPerfil)

**DTOs:**
- `ActualizarPerfilCliente` (Input)
- `ClienteDTO` (Output)

---

## 6. Flujo de Gestión de Productos (Admin)

### Crear Nuevo Producto

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionProductos.jsx (Admin)                     │
│ handleCrearProducto()                                       │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ POST /api/catalogo/productos
           │ Body: {
           │   idCategoria: 1,
           │   nombreProducto: "Hamburguesa Deluxe",
           │   descripcion: "...",
           │   precioBase: 7500,
           │   disponible: 1
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Verificar rol ADMIN en token                             │
│ - Proxy → GESTIONCATALOGO                                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCATALOGO: CatalogoController.crearProducto()        │
│ @PreAuthorize("hasRole('ADMIN')")                           │
│                                                             │
│ 1. Validar ProductoDTO con @Valid                          │
│                                                             │
│ 2. Llamar CatalogoService.crearProducto()                  │
│                                                             │
│ 3. INSERT INTO PRODUCTO (id_categoria, nombre_producto,    │
│                          descripcion, precio_base,          │
│                          disponible)                        │
│    VALUES (:idCategoria, :nombreProducto, ...)             │
│                                                             │
│ 4. Retornar ProductoDTO con idProducto generado            │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Retorna: ProductoDTO con idProducto = 25
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionProductos.jsx                             │
│                                                             │
│ - Mostrar mensaje de éxito                                 │
│ - Abrir formulario de subida de imagen                     │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/admin/gestionProductos.jsx`
- `src/services/productosService.js:84-95` (crearProducto)

**Backend:**
- `backGoldenBurgers/GESTIONCATALOGO/controller/CatalogoController.java:76-85` (crearProducto)
- `backGoldenBurgers/GESTIONCATALOGO/service/CatalogoService.java` (crearProducto)

**DTOs:**
- `ProductoDTO` (Input/Output)

---

### Subir Imagen de Producto

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionProductos.jsx                             │
│ handleSubirImagen(idProducto, file)                         │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ POST /api/catalogo/productos/25/imagen
           │ Content-Type: multipart/form-data
           │ Body: FormData con archivo
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Detectar multipart/form-data                             │
│ - Usar forwardMultipartRequest()                           │
│ - Mantener boundary del Content-Type                       │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCATALOGO: CatalogoController                         │
│ .subirImagenProducto(id, file)                              │
│                                                             │
│ 1. Validar que file no esté vacío                          │
│ 2. Validar que sea imagen (contentType image/*)            │
│ 3. Llamar CatalogoService.subirImagenProducto()            │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ CatalogoService                                             │
│                                                             │
│ 1. Buscar producto en BD                                   │
│                                                             │
│ 2. Llamar FirebaseStorageService.subirImagen()             │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FirebaseStorageService                                      │
│                                                             │
│ 1. Generar nombre único para archivo                       │
│    formato: "productos/{idProducto}_{timestamp}.jpg"       │
│                                                             │
│ 2. Subir archivo a Firebase Storage                        │
│    usando Firebase Admin SDK                               │
│                                                             │
│ 3. Generar URL pública de descarga                         │
│                                                             │
│ 4. Retornar URL                                            │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ URL: "https://firebasestorage.googleapis.com/..."
           ▼
┌─────────────────────────────────────────────────────────────┐
│ CatalogoService                                             │
│                                                             │
│ 3. Actualizar producto con URL de imagen                   │
│    UPDATE PRODUCTO SET imagen_url = :url                   │
│    WHERE id_producto = :idProducto                         │
│                                                             │
│ 4. Retornar ProductoDTO actualizado                        │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Response: {
           │   imageUrl: "https://firebasestorage...",
           │   mensaje: "imagen subida..."
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionProductos.jsx                             │
│                                                             │
│ - Mostrar preview de imagen                                │
│ - Actualizar tabla de productos                            │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/services/productosService.js:139-189` (subirImagenProducto)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/controller/ProxyController.java:266-315` (forwardMultipartRequest)
- `backGoldenBurgers/GESTIONCATALOGO/controller/CatalogoController.java:126-169` (subirImagenProducto)
- `backGoldenBurgers/GESTIONCATALOGO/service/FirebaseStorageService.java` (subirImagen)

---

## 7. Flujo de Gestión de Pedidos (Admin)

### Ver Todos los Pedidos

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionPedidos.jsx (Admin)                       │
│ useEffect() → getPedidos()                                  │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/pedidos
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Verificar rol ADMIN o TRABAJADOR                         │
│ - Proxy → GESTIONPEDIDO                                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PedidoController.listarPedidos()            │
│ @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")          │
│                                                             │
│ - SELECT p.*, ep.nombre_estado, mp.nombre_metodo,          │
│          te.nombre_tipo                                     │
│   FROM PEDIDO p                                             │
│   JOIN ESTADO_PEDIDO ep ON p.id_estado_pedido = ep.id      │
│   JOIN METODO_PAGO mp ON p.id_metodo_pago = mp.id          │
│   JOIN TIPO_ENTREGA te ON p.id_tipo_entrega = te.id        │
│   ORDER BY p.fecha_pedido DESC                             │
│                                                             │
│ - Retornar: List<Pedido>                                   │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/admin/gestionPedidos.jsx`
- `src/services/pedidosService.js:9-17` (getPedidos)

**Backend:**
- `backGoldenBurgers/GESTIONPEDIDO/controller/PedidoController.java:41-48` (listarPedidos)

---

### Cambiar Estado de Pedido

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionPedidos.jsx                               │
│ handleCambiarEstado(idPedido, nuevoEstado)                  │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ PUT /api/pedidos/cambiar-estado/127/estado/3
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONPEDIDO: PedidoController.cambiarEstadoPedido()      │
│                                                             │
│ - UPDATE PEDIDO SET id_estado_pedido = :idEstado           │
│   WHERE id_pedido = :idPedido                              │
│                                                             │
│ - Retornar Pedido actualizado                              │
└─────────────────────────────────────────────────────────────┘
```

**Estados de Pedido:**
1. Pendiente de Pago
2. Pagado
3. En Preparación
4. En Camino
5. Entregado
6. Cancelado

**Archivos Involucrados:**

**Frontend:**
- `src/services/pedidosService.js:68-77` (actualizarEstadoPedido)

**Backend:**
- `backGoldenBurgers/GESTIONPEDIDO/controller/PedidoController.java:151-156` (cambiarEstadoPedido)

---

## 8. Flujo de Dashboard y Reportes (Admin)

### Ver Resumen de Ventas

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: dashboard.jsx (Admin)                            │
│ useEffect() → getResumenVentas()                            │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/dashboard/resumen-ventas
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Verificar rol ADMIN o TRABAJADOR                         │
│ - Proxy → GESTIONVENTA                                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONVENTA: DashboardController.getResumenVentas()       │
│                                                             │
│ 1. Ventas de hoy:                                          │
│    SELECT COUNT(*), SUM(monto_total_venta)                 │
│    FROM VENTA                                               │
│    WHERE TRUNC(fecha_venta) = TRUNC(SYSDATE)               │
│                                                             │
│ 2. Ventas del mes:                                         │
│    SELECT COUNT(*), SUM(monto_total_venta)                 │
│    FROM VENTA                                               │
│    WHERE EXTRACT(MONTH FROM fecha_venta) = EXTRACT(MONTH...) │
│                                                             │
│ 3. Ventas del año:                                         │
│    SELECT COUNT(*), SUM(monto_total_venta)                 │
│    FROM VENTA                                               │
│    WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR...) │
│                                                             │
│ 4. Construir y retornar ResumenVentasDTO                   │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/admin/dashboard.jsx`
- `src/services/dashboardService.js:10-18` (getResumenVentas)

**Backend:**
- `backGoldenBurgers/GESTIONVENTA/controller/DashboardController.java` (getResumenVentas)

---

### Ver Ventas por Mes (Gráfico)

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: dashboard.jsx                                     │
│ useEffect() → getVentasPorMes()                             │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/dashboard/ventas-por-mes
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONVENTA: DashboardController.getVentasPorMes()        │
│                                                             │
│ - SELECT TO_CHAR(fecha_venta, 'YYYY-MM') as mes,           │
│          COUNT(*) as total_ventas,                          │
│          SUM(monto_total_venta) as monto_total             │
│   FROM VENTA                                                │
│   WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR...)  │
│   GROUP BY TO_CHAR(fecha_venta, 'YYYY-MM')                 │
│   ORDER BY mes DESC                                        │
│                                                             │
│ - Retornar: List<VentasPorMesDTO>                          │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/services/dashboardService.js:54-62` (getVentasPorMes)

**Backend:**
- `backGoldenBurgers/GESTIONVENTA/controller/DashboardController.java` (getVentasPorMes)

---

## 9. Flujo de Contacto

### Enviar Mensaje de Contacto

**Pasos del Usuario:**
1. Usuario accede a la página de Contacto (sin login)
2. Completa formulario: nombre, email, teléfono, mensaje
3. Hace clic en "Enviar"

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: ContactoPag.jsx                                   │
│ handleEnviarMensaje()                                       │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ POST /api/mensajes
           │ Body: {
           │   nombre: "Juan Pérez",
           │   email: "juan@gmail.com",
           │   telefono: "987654321",
           │   mensaje: "Consulta sobre horarios..."
           │ }
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY (Sin validar JWT - Ruta pública)               │
│ - Proxy → GESTIONCONTACTO                                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCONTACTO: MensajeContactoController                  │
│ .enviarMensaje(mensajeDTO)                                  │
│                                                             │
│ 1. Validar datos con @Valid                                │
│                                                             │
│ 2. INSERT INTO MENSAJE_CONTACTO                            │
│    (nombre, email, telefono, mensaje, fecha_envio, estado) │
│    VALUES (:nombre, :email, :telefono, :mensaje,           │
│            SYSDATE, 0)  -- 0 = No leído                    │
│                                                             │
│ 3. Retornar MensajeContactoDTO                             │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: ContactoPag.jsx                                   │
│                                                             │
│ - Mostrar mensaje de éxito                                 │
│ - Limpiar formulario                                       │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/client/ContactoPag.jsx`
- `src/services/contactoService.js:12-20` (enviarMensajeContacto)

**Backend:**
- `backGoldenBurgers/API-GATEWAY/filter/JwtAuthenticationFilter.java:69-72` (Permitir POST mensajes)
- `backGoldenBurgers/GESTIONCONTACTO/controller/MensajeContactoController.java` (enviarMensaje)

---

### Ver Mensajes (Admin)

**Flujo Técnico:**

```
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND: gestionContacto.jsx (Admin)                      │
│ useEffect() → listarMensajesContacto()                      │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ GET /api/mensajes
           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ - Validar JWT                                               │
│ - Verificar rol ADMIN o TRABAJADOR                         │
│ - Proxy → GESTIONCONTACTO                                   │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ GESTIONCONTACTO: MensajeContactoController                  │
│ .listarMensajes()                                           │
│                                                             │
│ - SELECT * FROM MENSAJE_CONTACTO                            │
│   ORDER BY fecha_envio DESC                                │
│                                                             │
│ - Retornar: List<MensajeContactoDTO>                       │
└─────────────────────────────────────────────────────────────┘
```

**Archivos Involucrados:**

**Frontend:**
- `src/pages/admin/gestionContacto.jsx`
- `src/services/contactoService.js:28-36` (listarMensajesContacto)

**Backend:**
- `backGoldenBurgers/GESTIONCONTACTO/controller/MensajeContactoController.java` (listarMensajes)

---

## Resumen de Tecnologías Utilizadas

### Frontend
- **React** - Framework UI
- **React Router** - Navegación
- **Axios** - Cliente HTTP
- **Firebase Authentication** - Autenticación de usuarios
- **Context API** - Estado global (carrito)
- **Bootstrap / CSS** - Estilos

### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot** - Framework principal
- **Spring Security** - Seguridad y autorización
- **Spring Data JPA** - ORM para base de datos
- **JWT (jsonwebtoken)** - Tokens de autenticación
- **Firebase Admin SDK** - Validación de tokens Firebase
- **RestTemplate / WebClient** - Comunicación entre microservicios
- **Mercado Pago SDK** - Integración de pagos

### Infraestructura
- **Oracle Database** - Base de datos principal
- **Firebase Storage** - Almacenamiento de imágenes
- **Firebase Authentication** - Gestión de usuarios
- **Mercado Pago** - Procesamiento de pagos

---

## Diagramas de Arquitectura

### Diagrama de Comunicación General

```
┌──────────────────┐
│     CLIENTE      │
│   (React SPA)    │
└────────┬─────────┘
         │
         │ HTTPS
         │
         ▼
┌─────────────────────────────────────────────┐
│          API GATEWAY                        │
│         (Puerto 8080)                       │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter            │   │
│  │  - Valida JWT                       │   │
│  │  - Extrae claims                    │   │
│  │  - Establece SecurityContext        │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │  ProxyController                    │   │
│  │  - Redirige a microservicios        │   │
│  │  - Pasa token en headers            │   │
│  └─────────────────────────────────────┘   │
└──────┬──────┬──────┬──────┬──────┬─────────┘
       │      │      │      │      │
       ▼      ▼      ▼      ▼      ▼
    ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
    │ MS │ │ MS │ │ MS │ │ MS │ │ MS │
    │ 1  │ │ 2  │ │ 3  │ │ 4  │ │ 5  │
    └────┘ └────┘ └────┘ └────┘ └────┘
```

---

**Documentación creada:** 2025-01-27
**Versión:** 1.0

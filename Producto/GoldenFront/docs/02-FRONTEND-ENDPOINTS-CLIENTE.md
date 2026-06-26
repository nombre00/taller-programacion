# Endpoints Llamados desde el Frontend - Golden Burgers

## Resumen General

Este documento detalla todos los endpoints de la API que son llamados desde las páginas del frontend (cliente) de la aplicación Golden Burgers.

**Base URL:** `http://161.153.219.128:8080/api`

---

## Tabla de Contenidos

1. [Autenticación](#autenticación)
2. [Gestión de Usuarios y Clientes](#gestión-de-usuarios-y-clientes)
3. [Catálogo de Productos](#catálogo-de-productos)
4. [Gestión de Pedidos](#gestión-de-pedidos)
5. [Gestión de Pagos](#gestión-de-pagos)
6. [Gestión de Ventas](#gestión-de-ventas)
7. [Gestión de Boletas](#gestión-de-boletas)
8. [Dashboard (Admin)](#dashboard-admin)
9. [Contacto](#contacto)

---

## Autenticación

### POST /api/auth/login
**Descripción:** Autenticación de usuario con token de Firebase
**Archivo:** `src/pages/client/Login.jsx:54-56`
**Acceso:** Público

**Request:**
```javascript
await api.post("/auth/login", {
  firebaseToken: firebaseToken
});
```

**Request Body:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**Response:**
```json
{
  "internalToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "uid": "abc123xyz",
    "email": "cliente@gmail.com",
    "nombre": "Juan Pérez",
    "rolId": 1,
    "rolNombre": "Cliente"
  },
  "expiresIn": 86400000
}
```

---

## Gestión de Usuarios y Clientes

### POST /api/clientes
**Descripción:** Registrar un nuevo cliente
**Archivo:** `src/services/usuariosService.js:18-40`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const registrarCliente = async (clienteData) => {
  const response = await api.post("/clientes", clienteData);
  return response.data;
};
```

**Request Body:**
```json
{
  "idUsuario": "firebaseUID123",
  "nombreCliente": "Juan Pérez",
  "email": "juan@gmail.com",
  "telefonoCliente": "987654321"
}
```

**Llamado desde:**
- `src/pages/client/Login.jsx:153-157` (Registro)
- `src/pages/admin/nuevoCliente.jsx` (Admin crea cliente)

---

### POST /api/clientes/admin
**Descripción:** Registrar un nuevo cliente (Solo Admin)
**Archivo:** `src/services/usuariosService.js:47-69`
**Acceso:** Solo ADMIN

**Función del servicio:**
```javascript
export const registrarClientePorAdmin = async (clienteData) => {
  const response = await api.post("/clientes/admin", clienteData);
  return response.data;
};
```

**Llamado desde:**
- `src/pages/admin/nuevoCliente.jsx` (Formulario de admin)

---

### GET /api/clientes
**Descripción:** Listar todos los clientes
**Archivo:** `src/services/usuariosService.js:76-84`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const obtenerTodosClientes = async () => {
  const response = await api.get("/clientes");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/admin/gestionClientes.jsx` (Tabla de clientes)

---

### GET /api/clientes/:id
**Descripción:** Obtener cliente por ID
**Archivo:** `src/services/usuariosService.js:91-99`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const obtenerClientePorId = async (idCliente) => {
  const response = await api.get(`/clientes/${idCliente}`);
  return response.data;
};
```

---

### GET /api/clientes/usuario/:idUsuario
**Descripción:** Obtener cliente por Firebase UID
**Archivo:** `src/services/usuariosService.js:108-116`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const obtenerClientePorUid = async (firebaseUid) => {
  const response = await api.get(`/clientes/usuario/${firebaseUid}`);
  return response.data;
};
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Cargar datos del perfil)
- `src/pages/client/CheckOut.jsx` (Obtener datos del cliente)

---

### GET /api/clientes/email/:email
**Descripción:** Obtener cliente por email
**Archivo:** `src/services/usuariosService.js:123-131`
**Acceso:** Requiere token JWT

---

### PUT /api/clientes/:id
**Descripción:** Actualizar datos de un cliente (nombre y teléfono)
**Archivo:** `src/services/usuariosService.js:142-155`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const actualizarCliente = async (id, nombreCliente, telefonoCliente = null) => {
  const params = new URLSearchParams();
  params.append("nombreCliente", nombreCliente);
  if (telefonoCliente) {
    params.append("telefonoCliente", telefonoCliente);
  }
  const response = await api.put(`/clientes/${id}`, null, { params });
  return response.data;
};
```

**Llamado desde:**
- `src/pages/admin/gestionClientes.jsx` (Editar cliente)

---

### PUT /api/clientes/perfil
**Descripción:** Actualizar perfil del cliente autenticado
**Archivo:** `src/services/usuariosService.js:167-175`
**Acceso:** Requiere token JWT (usuario autenticado)

**Función del servicio:**
```javascript
export const actualizarPerfilCliente = async (perfilData) => {
  const response = await api.put("/clientes/perfil", perfilData);
  return response.data;
};
```

**Request Body:**
```json
{
  "nombreCliente": "Juan Carlos Pérez",
  "email": "nuevo@email.com",
  "telefonoCliente": "912345678"
}
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Formulario de perfil)

---

### DELETE /api/clientes/:id
**Descripción:** Eliminar cliente
**Archivo:** `src/services/usuariosService.js:208-216`
**Acceso:** Solo ADMIN

**Función del servicio:**
```javascript
export const eliminarCliente = async (id) => {
  const response = await api.delete(`/clientes/${id}`);
  return response.data;
};
```

**Llamado desde:**
- `src/pages/admin/gestionClientes.jsx` (Botón eliminar)

---

### POST /api/clientes/direcciones
**Descripción:** Crear nueva dirección de entrega
**Archivo:** `src/services/usuariosService.js:229-237`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const crearDireccion = async (direccionData) => {
  const response = await api.post("/clientes/direcciones", direccionData);
  return response.data;
};
```

**Request Body:**
```json
{
  "idCliente": 5,
  "idCiudad": 1,
  "direccion": "Av. Libertador 123, Depto 5B",
  "alias": "Casa"
}
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Agregar dirección)

---

### GET /api/clientes/:idCliente/direcciones
**Descripción:** Obtener direcciones de un cliente
**Archivo:** `src/services/usuariosService.js:246-254`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const obtenerDireccionesPorCliente = async (idCliente) => {
  const response = await api.get(`/clientes/${idCliente}/direcciones`);
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Listar direcciones)
- `src/pages/client/CheckOut.jsx` (Seleccionar dirección de entrega)

---

### PUT /api/clientes/direcciones/:idDireccion
**Descripción:** Actualizar dirección existente
**Archivo:** `src/services/usuariosService.js:264-272`
**Acceso:** Requiere token JWT

---

### DELETE /api/clientes/direcciones/:idDireccion
**Descripción:** Eliminar dirección
**Archivo:** `src/services/usuariosService.js:281-289`
**Acceso:** Requiere token JWT

---

### GET /api/ciudades
**Descripción:** Listar todas las ciudades disponibles
**Archivo:** `src/services/usuariosService.js:473-481`
**Acceso:** Público

**Función del servicio:**
```javascript
export const obtenerTodasCiudades = async () => {
  const response = await api.get("/ciudades");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Response:**
```json
[
  {
    "idCiudad": 1,
    "nombreCiudad": "Valparaíso"
  },
  {
    "idCiudad": 2,
    "nombreCiudad": "Viña del Mar"
  }
]
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Selector de ciudad)
- `src/pages/client/CheckOut.jsx` (Formulario de dirección)

---

## Catálogo de Productos

### GET /api/catalogo/productos
**Descripción:** Obtener todos los productos disponibles
**Archivo:** `src/services/catalogoService.js:18-36`
**Acceso:** Público

**Función del servicio:**
```javascript
export const obtenerProductosDisponibles = async () => {
  const response = await api.get("/catalogo/productos");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Response:**
```json
[
  {
    "idProducto": 1,
    "idCategoria": 1,
    "nombreProducto": "Hamburguesa Clásica",
    "descripcion": "Hamburguesa con carne de res, lechuga y tomate",
    "precioBase": 5500,
    "imagenUrl": "https://firebasestorage.googleapis.com/...",
    "disponible": 1
  }
]
```

**Llamado desde:**
- `src/pages/client/CatalogoPag.jsx` (Mostrar productos)
- `src/pages/client/InicioPag.jsx` (Productos destacados)

---

### GET /api/catalogo/productos/todos
**Descripción:** Obtener TODOS los productos (incluidos no disponibles)
**Archivo:** `src/services/productosService.js:18-30`
**Acceso:** Requiere token JWT

**Función del servicio:**
```javascript
export const obtenerTodosProductos = async () => {
  const response = await api.get("/catalogo/productos/todos");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/admin/gestionProductos.jsx` (Tabla de productos en admin)

---

### GET /api/catalogo/productos/:id
**Descripción:** Obtener producto por ID
**Archivo:** `src/services/catalogoService.js:41-50`
**Acceso:** Público

---

### GET /api/catalogo/productos/categoria/:idCategoria
**Descripción:** Obtener productos por categoría
**Archivo:** `src/services/catalogoService.js:56-65`
**Acceso:** Público

**Función del servicio:**
```javascript
export const obtenerProductosPorCategoria = async (idCategoria) => {
  const response = await api.get(`/catalogo/productos/categoria/${idCategoria}`);
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/client/CatalogoPag.jsx` (Filtrar por categoría)

---

### GET /api/catalogo/productos/buscar?nombre=:nombre
**Descripción:** Buscar productos por nombre
**Archivo:** `src/services/catalogoService.js:70-81`
**Acceso:** Público

**Función del servicio:**
```javascript
export const buscarProductosPorNombre = async (nombre) => {
  const response = await api.get(`/catalogo/productos/buscar`, {
    params: { nombre }
  });
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/client/CatalogoPag.jsx` (Búsqueda)

---

### GET /api/catalogo/categorias
**Descripción:** Obtener todas las categorías
**Archivo:** `src/services/catalogoService.js:88-97`
**Acceso:** Público

**Función del servicio:**
```javascript
export const obtenerCategorias = async () => {
  const response = await api.get("/catalogo/categorias");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Response:**
```json
[
  {
    "idCategoria": 1,
    "nombreCategoria": "Hamburguesas"
  },
  {
    "idCategoria": 2,
    "nombreCategoria": "Bebidas"
  }
]
```

**Llamado desde:**
- `src/pages/client/CatalogoPag.jsx` (Filtros de categoría)
- `src/pages/admin/gestionProductos.jsx` (Formulario de productos)

---

### POST /api/catalogo/productos
**Descripción:** Crear nuevo producto (ADMIN)
**Archivo:** `src/services/productosService.js:84-95`
**Acceso:** Solo ADMIN

**Función del servicio:**
```javascript
export const crearProducto = async (productoData) => {
  const response = await api.post("/catalogo/productos", productoData);
  return response.data;
};
```

**Request Body:**
```json
{
  "idCategoria": 1,
  "nombreProducto": "Hamburguesa Deluxe",
  "descripcion": "Hamburguesa premium con carne angus",
  "precioBase": 7500,
  "disponible": 1
}
```

**Llamado desde:**
- `src/pages/admin/gestionProductos.jsx` (Formulario crear producto)

---

### PUT /api/catalogo/productos/:id
**Descripción:** Actualizar producto existente
**Archivo:** `src/services/productosService.js:102-113`
**Acceso:** ADMIN, TRABAJADOR

---

### DELETE /api/catalogo/productos/:id
**Descripción:** Eliminar producto
**Archivo:** `src/services/productosService.js:120-129`
**Acceso:** Solo ADMIN

---

### POST /api/catalogo/productos/:id/imagen
**Descripción:** Subir imagen del producto a Firebase
**Archivo:** `src/services/productosService.js:139-189`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const subirImagenProducto = async (idProducto, file) => {
  const formData = new FormData();
  formData.append('imagen', file);

  const token = localStorage.getItem('authToken');
  const url = `/api/catalogo/productos/${idProducto}/imagen`;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: formData
  });

  const result = await response.json();
  return result;
};
```

**Response:**
```json
{
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "mensaje": "imagen subida a firebase y guardada en base de datos"
}
```

**Llamado desde:**
- `src/pages/admin/gestionProductos.jsx` (Subir imagen)

---

### PATCH /api/catalogo/productos/:id/disponibilidad?disponible=:boolean
**Descripción:** Cambiar disponibilidad del producto
**Archivo:** `src/services/productosService.js:198-213`
**Acceso:** ADMIN, TRABAJADOR

---

## Gestión de Pedidos

### GET /api/pedidos
**Descripción:** Listar todos los pedidos
**Archivo:** `src/services/pedidosService.js:9-17`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const getPedidos = async () => {
  const response = await api.get("/pedidos");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/admin/gestionPedidos.jsx` (Tabla de pedidos)

---

### GET /api/pedidos/:id
**Descripción:** Obtener pedido por ID
**Archivo:** `src/services/pedidosService.js:20-28`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

### GET /api/pedidos/cliente/:clienteId
**Descripción:** Obtener pedidos de un cliente específico
**Archivo:** `src/services/pedidosService.js:31-39`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

**Función del servicio:**
```javascript
export const getPedidosPorCliente = async (clienteId) => {
  const response = await api.get(`/pedidos/cliente/${clienteId}`);
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/client/MiPerfilPag.jsx` (Historial de pedidos)

---

### POST /api/pedidos/completo
**Descripción:** Crear nuevo pedido con detalles
**Archivo:** `src/services/pedidosService.js:44-52`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

**Función del servicio:**
```javascript
export const crearPedido = async (pedido) => {
  const response = await api.post("/pedidos/completo", pedido);
  return response.data;
};
```

**Request Body:**
```json
{
  "idCliente": 5,
  "idEstadoPedido": 1,
  "idMetodoPago": 2,
  "idTipoEntrega": 1,
  "idDireccionEntrega": 3,
  "montoSubtotal": 15000,
  "montoEnvio": 2000,
  "montoTotal": 17000,
  "fechaHoraPedido": "2025-01-27T14:30:00",
  "notasCliente": "Sin cebolla, por favor",
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2,
      "precioUnitario": 5500,
      "subtotalLinea": 11000
    },
    {
      "idProducto": 5,
      "cantidad": 1,
      "precioUnitario": 4000,
      "subtotalLinea": 4000
    }
  ]
}
```

**Response:**
```json
{
  "idPedido": 127,
  "idCliente": 5,
  "idEstadoPedido": 1,
  "montoTotal": 17000,
  "fechaPedido": "2025-01-27T14:30:00",
  "detalles": [...]
}
```

**Llamado desde:**
- `src/pages/client/CheckOut.jsx` (Confirmar pedido)

---

### PUT /api/pedidos/cambiar-estado/:idPedido/estado/:idEstado
**Descripción:** Actualizar estado de pedido (sin realizar venta)
**Archivo:** `src/services/pedidosService.js:68-77`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

**Función del servicio:**
```javascript
export const actualizarEstadoPedido = async (idPedido, idEstado) => {
  const response = await api.put(`/pedidos/cambiar-estado/${idPedido}/estado/${idEstado}`);
  return response.data;
};
```

**Estados de Pedido:**
| ID | Estado            |
|----|-------------------|
| 1  | Pendiente de Pago |
| 2  | Pagado            |
| 3  | En Preparación    |
| 4  | En Camino         |
| 5  | Entregado         |
| 6  | Cancelado         |

**Llamado desde:**
- `src/pages/admin/gestionPedidos.jsx` (Cambiar estado)

---

### PUT /api/pedidos/procesar/:idPedido
**Descripción:** Actualizar pedido a PAGADO y generar venta automáticamente
**Archivo:** `src/services/pedidosService.js:81-93`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

**Función del servicio:**
```javascript
export const actualizarPedidoAPagado = async (idPedido) => {
  const response = await api.put(`/pedidos/procesar/${idPedido}`);
  return response.data;
};
```

**Llamado desde:**
- `src/pages/client/PagoExitoPage.jsx` (Después de pago exitoso en Mercado Pago)

---

### DELETE /api/pedidos/:id
**Descripción:** Eliminar/Cancelar pedido
**Archivo:** `src/services/pedidosService.js:55-63`
**Acceso:** ADMIN, CLIENTE

---

### GET /api/pedidos/detalles/cliente/:idCliente
**Descripción:** Obtener detalles de pedidos de un cliente
**Archivo:** `src/services/pedidosService.js:107-115`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

### PUT /api/pedidos/:idPedido/actualizar
**Descripción:** Actualizar un pedido existente
**Archivo:** `src/services/pedidosService.js:125-141`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

## Gestión de Pagos

### POST /api/pagos/crear-preferencia
**Descripción:** Crear preferencia de pago en Mercado Pago
**Archivo:** `src/services/pagoService.js:18-44`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

**Función del servicio:**
```javascript
export const crearPreferenciaPago = async (datosPago) => {
  const response = await api.post('/pagos/crear-preferencia', datosPago);
  return response.data;
};
```

**Request Body:**
```json
{
  "idPedido": 127,
  "montoPago": 17000,
  "descripcion": "Pedido #127 - Golden Burgers",
  "emailPagador": "cliente@gmail.com",
  "nombrePagador": "Juan Pérez"
}
```

**Response:**
```json
{
  "idPago": 123,
  "idPedido": 127,
  "montoPago": 17000,
  "estadoPago": 1,
  "idPreferenciaMpos": "123456-abc-123",
  "urlPago": "https://www.mercadopago.cl/checkout/v1/redirect?pref_id=123456-abc-123"
}
```

**Llamado desde:**
- `src/pages/client/CheckOut.jsx` (Ir a pagar)

---

### GET /api/pagos
**Descripción:** Obtener todos los pagos
**Archivo:** `src/services/pagoService.js:81-89`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/pagos/:idPago
**Descripción:** Obtener pago por ID
**Archivo:** `src/services/pagoService.js:95-103`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

### GET /api/pagos/pedido/:idPedido
**Descripción:** Obtener pagos de un pedido
**Archivo:** `src/services/pagoService.js:109-117`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

### PUT /api/pagos/:id/estado
**Descripción:** Actualizar estado de un pago
**Archivo:** `src/services/pagoService.js:56-75`
**Acceso:** ADMIN, TRABAJADOR

---

### DELETE /api/pagos/:id
**Descripción:** Cancelar pago
**Archivo:** `src/services/pagoService.js:123-136`
**Acceso:** Solo ADMIN

---

## Gestión de Ventas

### GET /api/ventas
**Descripción:** Obtener todas las ventas
**Archivo:** `src/services/ventaService.js:9-17`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const getVentas = async () => {
  const response = await api.get("/ventas");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/admin/gestionVenta.jsx` (Tabla de ventas)

---

### GET /api/ventas/:id
**Descripción:** Obtener venta por ID
**Archivo:** `src/services/ventaService.js:20-28`
**Acceso:** ADMIN, TRABAJADOR

---

### PUT /api/ventas/:id
**Descripción:** Actualizar venta
**Archivo:** `src/services/ventaService.js:31-39`
**Acceso:** ADMIN, TRABAJADOR

---

### DELETE /api/ventas/:id
**Descripción:** Eliminar venta
**Archivo:** `src/services/ventaService.js:42-50`
**Acceso:** Solo ADMIN

---

## Gestión de Boletas

### GET /api/boletas
**Descripción:** Listar todas las boletas
**Archivo:** `src/services/boletaService.js:6-14`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const getBoletas = async () => {
  const response = await api.get("/boletas");
  return response.data;
};
```

**Llamado desde:**
- `src/pages/admin/gestionVenta.jsx` (Ver boletas)

---

### GET /api/boletas/:id
**Descripción:** Obtener boleta por ID
**Archivo:** `src/services/boletaService.js:19-27`
**Acceso:** ADMIN, TRABAJADOR, CLIENTE

---

### POST /api/boletas
**Descripción:** Crear nueva boleta
**Archivo:** `src/services/boletaService.js:32-40`
**Acceso:** ADMIN, TRABAJADOR

---

### PUT /api/boletas/:id
**Descripción:** Actualizar boleta
**Archivo:** `src/services/boletaService.js:44-52`
**Acceso:** ADMIN, TRABAJADOR

---

### DELETE /api/boletas/:id
**Descripción:** Eliminar boleta
**Archivo:** `src/services/boletaService.js:56-64`
**Acceso:** ADMIN

---

## Dashboard (Admin)

### GET /api/dashboard/resumen-ventas
**Descripción:** Resumen completo de ventas (hoy, mes, año)
**Archivo:** `src/services/dashboardService.js:10-18`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const getResumenVentas = async () => {
  const response = await api.get("/dashboard/resumen-ventas");
  return response.data;
};
```

**Response:**
```json
{
  "ventasHoy": {
    "totalVentas": 15,
    "montoTotal": 250000
  },
  "ventasMes": {
    "totalVentas": 320,
    "montoTotal": 5800000
  },
  "ventasAnio": {
    "totalVentas": 3500,
    "montoTotal": 62000000
  }
}
```

**Llamado desde:**
- `src/pages/admin/dashboard.jsx` (KPIs principales)

---

### GET /api/dashboard/ventas-hoy
**Descripción:** Ventas del día actual
**Archivo:** `src/services/dashboardService.js:21-29`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/dashboard/ventas-mes-actual
**Descripción:** Ventas del mes actual
**Archivo:** `src/services/dashboardService.js:32-40`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/dashboard/ventas-anio-actual
**Descripción:** Ventas del año actual
**Archivo:** `src/services/dashboardService.js:43-51`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/dashboard/ventas-por-mes
**Descripción:** Ventas agrupadas por mes (para gráficos)
**Archivo:** `src/services/dashboardService.js:54-62`
**Acceso:** ADMIN, TRABAJADOR

**Response:**
```json
[
  {
    "mes": "2025-01",
    "totalVentas": 320,
    "montoTotal": 5800000
  },
  {
    "mes": "2024-12",
    "totalVentas": 450,
    "montoTotal": 8200000
  }
]
```

**Llamado desde:**
- `src/pages/admin/dashboard.jsx` (Gráfico de ventas por mes)

---

### GET /api/dashboard/kpis?periodo=:periodo
**Descripción:** KPIs según periodo (hoy, mes, anio)
**Archivo:** `src/services/dashboardService.js:67-77`
**Acceso:** ADMIN, TRABAJADOR

**Parámetros:**
- `periodo`: "hoy" | "mes" | "anio"

---

### GET /api/dashboard/ventas-categoria?periodo=:periodo
**Descripción:** Ventas por categoría según periodo
**Archivo:** `src/services/dashboardService.js:80-90`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/dashboard/ventas-ciudad?periodo=:periodo
**Descripción:** Ventas por ciudad según periodo
**Archivo:** `src/services/dashboardService.js:93-103`
**Acceso:** ADMIN, TRABAJADOR

---

## Contacto

### POST /api/mensajes
**Descripción:** Enviar mensaje de contacto desde formulario público
**Archivo:** `src/services/contactoService.js:12-20`
**Acceso:** Público (sin autenticación)

**Función del servicio:**
```javascript
export const enviarMensajeContacto = async (mensaje) => {
  const response = await api.post("/mensajes", mensaje);
  return response.data;
};
```

**Request Body:**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@gmail.com",
  "telefono": "987654321",
  "mensaje": "Consulta sobre horarios de atención"
}
```

**Llamado desde:**
- `src/pages/client/ContactoPag.jsx` (Formulario de contacto)

---

### GET /api/mensajes
**Descripción:** Listar todos los mensajes de contacto (ADMIN)
**Archivo:** `src/services/contactoService.js:28-36`
**Acceso:** ADMIN, TRABAJADOR

**Función del servicio:**
```javascript
export const listarMensajesContacto = async () => {
  const response = await api.get("/mensajes");
  return Array.isArray(response.data) ? response.data : [];
};
```

**Llamado desde:**
- `src/pages/admin/gestionContacto.jsx` (Tabla de mensajes)

---

### GET /api/mensajes/:id
**Descripción:** Obtener mensaje por ID
**Archivo:** `src/services/contactoService.js:43-51`
**Acceso:** ADMIN, TRABAJADOR

---

### GET /api/mensajes/estado/:estado
**Descripción:** Filtrar mensajes por estado (0=no leído, 1=leído, 2=respondido)
**Archivo:** `src/services/contactoService.js:59-67`
**Acceso:** ADMIN, TRABAJADOR

---

### PUT /api/mensajes/:idMensaje/estado/:nuevoEstado
**Descripción:** Cambiar estado de mensaje
**Archivo:** `src/services/contactoService.js:74-82`
**Acceso:** ADMIN, TRABAJADOR

---

### DELETE /api/mensajes/:idMensaje
**Descripción:** Eliminar mensaje de contacto
**Archivo:** `src/services/contactoService.js:89-97`
**Acceso:** ADMIN

---

## Resumen de Permisos por Rol

### Endpoints Públicos (Sin autenticación)
- `POST /api/auth/login`
- `GET /api/catalogo/productos`
- `GET /api/catalogo/productos/:id`
- `GET /api/catalogo/productos/categoria/:idCategoria`
- `GET /api/catalogo/productos/buscar`
- `GET /api/catalogo/categorias`
- `GET /api/ciudades`
- `GET /api/roles`
- `POST /api/mensajes`

### CLIENTE (Rol ID: 1)
- Todos los endpoints públicos
- Gestión de su propio perfil
- Crear y ver sus propias direcciones
- Crear y ver sus propios pedidos
- Crear pagos y ver sus propios pagos
- Ver sus propias boletas

### TRABAJADOR (Rol ID: 2)
- Todos los endpoints de CLIENTE
- Ver todos los clientes
- Ver todos los pedidos
- Ver todas las ventas
- Ver dashboard
- Gestionar productos (crear, editar, cambiar disponibilidad)
- Ver mensajes de contacto

### ADMIN (Rol ID: 3)
- Todos los permisos de TRABAJADOR
- Crear y eliminar clientes
- Crear trabajadores
- Eliminar productos
- Eliminar ventas
- Cancelar pagos
- Eliminar boletas
- Eliminar mensajes

---

## Configuración del Cliente API

**Archivo:** `src/config/api.js`

**Base URL:**
```javascript
const baseURL = "http://161.153.219.128:8080/api";
```

**Headers por defecto:**
```javascript
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {token}" // Agregado automáticamente por interceptor
}
```

**Timeout:** 30 segundos

---

## Notas Importantes

1. **Autenticación:** La mayoría de los endpoints requieren un token JWT válido en el header `Authorization: Bearer {token}`

2. **FormData:** Para subir imágenes, se usa `FormData` en lugar de JSON y NO se especifica el `Content-Type` (se configura automáticamente)

3. **Manejo de Errores:** El interceptor de Axios maneja automáticamente:
   - Error 401: Redirige al login
   - Error 403: Muestra alerta de permisos insuficientes
   - Error 500: Muestra alerta de error del servidor

4. **Arrays Vacíos:** Los servicios devuelven arrays vacíos `[]` en caso de error para evitar romper el frontend

5. **Mercado Pago:** El flujo de pago redirige al usuario a Mercado Pago y luego retorna a páginas de éxito/error/pendiente

---

**Documentación creada:** 2025-01-27
**Versión:** 1.0

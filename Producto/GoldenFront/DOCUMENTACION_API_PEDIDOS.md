# Documentación: Consumo de API Backend en Gestión de Pedidos

## Índice
1. [Arquitectura General](#arquitectura-general)
2. [Configuración de API](#configuración-de-api)
3. [Servicios (Services)](#servicios-services)
4. [Implementación en gestionPedidos.jsx](#implementación-en-gestionpedidosjsx)
5. [Flujo Completo de Creación de Pedido](#flujo-completo-de-creación-de-pedido)
6. [Endpoints Utilizados](#endpoints-utilizados)

---

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                         │
├─────────────────────────────────────────────────────────────┤
│  gestionPedidos.jsx                                         │
│  └── Llama a ──┐                                            │
│                 │                                            │
│  pedidosService.js                                          │
│  usuariosService.js                                         │
│  productosService.js                                        │
│  └── Usan ────┐                                             │
│                │                                            │
│  api.js (Axios configurado)                                │
│  └── Conecta con ──┐                                        │
└────────────────────┼────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  BACKEND (Spring Boot)                      │
├─────────────────────────────────────────────────────────────┤
│  http://localhost:8080/api                                  │
│  ├── /pedidos                                               │
│  ├── /clientes                                              │
│  ├── /productos                                             │
│  ├── /catalogo                                              │
│  └── /ventas                                                │
└─────────────────────────────────────────────────────────────┘
```

---

## Configuración de API

### 📁 `src/config/api.js`

Este archivo configura la instancia de Axios con:
- URL base del backend
- Interceptores para agregar el token JWT automáticamente
- Manejo centralizado de errores

```javascript
import axios from "axios";

// Configuración base de la API
const api = axios.create({
  baseURL: "http://localhost:8080/api",  // URL del backend
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor para agregar token JWT en cada petición
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
```

**¿Qué hace?**
- Centraliza la URL del backend (solo se cambia aquí si el backend cambia de puerto)
- Agrega automáticamente el token JWT desde localStorage
- Todas las peticiones llevan `Authorization: Bearer {token}`

---

## Servicios (Services)

### 📁 `src/services/pedidosService.js`

Contiene todas las funciones para comunicarse con los endpoints de pedidos.

#### **1. Listar todos los pedidos**

```javascript
export const getPedidos = async () => {
  try {
    const response = await api.get("/pedidos");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener pedidos:", error);
    throw error;
  }
};
```

**Backend:** `GET http://localhost:8080/api/pedidos`

**Respuesta esperada:**
```json
[
  {
    "ID_PEDIDO": 1,
    "ID_CLIENTE": 5,
    "ID_ESTADO_PEDIDO": 1,
    "ID_METODO_PAGO": 2,
    "ID_TIPO_ENTREGA": 1,
    "MONTO_SUBTOTAL": 15000.00,
    "MONTO_ENVIO": 2500.00,
    "MONTO_TOTAL": 17500.00,
    "NOTA_CLIENTE": "Sin cebolla",
    "detalles": [
      {
        "NOMBRE_PRODUCTO": "Hamburguesa Clásica",
        "CANTIDAD": 2,
        "PRECIO_UNITARIO": 7500.00,
        "SUBTOTAL_LINEA": 15000.00
      }
    ]
  }
]
```

---

#### **2. Crear pedido completo**

```javascript
export const crearPedido = async (pedido) => {
  try {
    const response = await api.post("/pedidos/completo", pedido);
    return response.data;
  } catch (error) {
    console.error("Error al crear pedido:", error);
    throw error;
  }
};
```

**Backend:** `POST http://localhost:8080/api/pedidos/completo`

**Body enviado:**
```json
{
  "idCliente": 5,
  "idEstadoPedido": 1,
  "idMetodoPago": 2,
  "idTipoEntrega": 1,
  "idDireccionEntrega": 3,
  "montoSubtotal": 15000.00,
  "montoEnvio": 2500.00,
  "montoTotal": 17500.00,
  "notaCliente": "Sin cebolla",
  "detalles": [
    {
      "idProducto": 10,
      "cantidad": 2,
      "precioUnitario": 7500.00,
      "subtotalLinea": 15000.00
    }
  ]
}
```

**Respuesta del backend:**
```json
{
  "idPedido": 141,
  "idCliente": 5,
  "idEstadoPedido": 1,
  "montoTotal": 17500.00,
  "fechaPedido": "2025-11-19T15:30:00"
}
```

---

#### **3. Eliminar pedido**

```javascript
export const eliminarPedido = async (id) => {
  try {
    const response = await api.delete(`/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar pedido ${id}:`, error);
    throw error;
  }
};
```

**Backend:** `DELETE http://localhost:8080/api/pedidos/{id}`

---

#### **4. Marcar pedido como pagado**

```javascript
export const actualizarPedidoAPagado = async (idPedido) => {
  try {
    const response = await api.put(`/pedidos/procesar/${idPedido}`);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar pedido a pagado ${idPedido}:`, error);
    throw error;
  }
};
```

**Backend:** `PUT http://localhost:8080/api/pedidos/procesar/{idPedido}`

**¿Qué hace el backend?**
1. Cambia el estado del pedido a "Pagado"
2. Crea una venta asociada
3. Genera una boleta electrónica con número SII

---

### 📁 `src/services/usuariosService.js`

#### **Obtener todos los clientes**

```javascript
export const obtenerTodosClientes = async () => {
  try {
    const response = await api.get("/clientes");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener clientes:", error);
    throw error;
  }
};
```

**Backend:** `GET http://localhost:8080/api/clientes`

---

#### **Obtener direcciones de un cliente**

```javascript
export const obtenerDireccionesPorCliente = async (idCliente) => {
  try {
    const response = await api.get(`/clientes/${idCliente}/direcciones`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener direcciones del cliente ${idCliente}:`, error);
    throw error;
  }
};
```

**Backend:** `GET http://localhost:8080/api/clientes/{idCliente}/direcciones`

**Respuesta esperada:**
```json
[
  {
    "idDireccion": 1,
    "direccion": "Av. Libertad 123",
    "alias": "Casa",
    "idCiudad": 2
  },
  {
    "idDireccion": 2,
    "direccion": "Calle Principal 456",
    "alias": "Trabajo",
    "idCiudad": 2
  }
]
```

---

### 📁 `src/services/productosService.js`

#### **Obtener productos disponibles (público)**

```javascript
export const obtenerProductosDisponibles = async () => {
  try {
    const response = await api.get("/catalogo/productos");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener productos:", error);
    throw error;
  }
};
```

**Backend:** `GET http://localhost:8080/api/catalogo/productos`

**¿Por qué `/catalogo`?**
- No requiere autenticación
- Accesible para clientes no registrados
- Muestra solo productos disponibles

---

## Implementación en gestionPedidos.jsx

### 🔄 Flujo de carga inicial

```javascript
// 1. Importar servicios
import * as pedidosService from '../../services/pedidosService';
import * as usuariosService from '../../services/usuariosService';
import * as productosService from '../../services/productosService';

// 2. Estados para almacenar datos
const [pedidos, setPedidos] = useState([]);
const [clientes, setClientes] = useState([]);
const [productos, setProductos] = useState([]);

// 3. Cargar datos al montar el componente
useEffect(() => {
  inicializarDatos();
}, []);

const inicializarDatos = async () => {
  setLoading(true);
  try {
    // Cargar pedidos
    const pedidosData = await pedidosService.getPedidos();
    const pedidosOrdenados = pedidosData.sort((a, b) => 
      (a.ID_PEDIDO || a.idPedido) - (b.ID_PEDIDO || b.idPedido)
    );
    setPedidos(pedidosOrdenados);

    // Cargar clientes
    const clientesData = await usuariosService.obtenerTodosClientes();
    setClientes(clientesData);

    // Cargar productos
    const productosData = await productosService.obtenerProductosDisponibles();
    setProductos(productosData);

  } catch (err) {
    console.error('Error inicializando datos:', err);
    setError(err.message);
  } finally {
    setLoading(false);
  }
};
```

---

### 📦 Cargar direcciones cuando se selecciona un cliente

```javascript
// useEffect que escucha cambios en idCliente
useEffect(() => {
  if (idCliente) {
    cargarDireccionesCliente(idCliente);
  } else {
    setDireccionesCliente([]);
    setIdDireccion('');
  }
}, [idCliente]);

const cargarDireccionesCliente = async (id) => {
  try {
    const response = await usuariosService.obtenerDireccionesPorCliente(id);
    setDireccionesCliente(Array.isArray(response) ? response : []);
  } catch (err) {
    console.error('Error cargando direcciones:', err);
    setDireccionesCliente([]);
  }
};
```

**Flujo:**
1. Usuario selecciona un cliente en el combobox
2. `idCliente` cambia
3. useEffect detecta el cambio
4. Llama a la API: `GET /clientes/{idCliente}/direcciones`
5. Actualiza el combobox de direcciones

---

## Flujo Completo de Creación de Pedido

### 📝 Paso a paso

```javascript
const handleCrearPedido = async (e) => {
  e.preventDefault();

  // 1️⃣ VALIDAR datos del formulario
  if (!idCliente || productosCarrito.length === 0 || !idMetodoPago) {
    alert('Complete todos los campos requeridos');
    return;
  }

  // 2️⃣ CALCULAR montos
  const subtotal = calcularSubtotalCarrito();
  const envio = parseFloat(montoEnvio) || 0;
  const total = subtotal + envio;

  // 3️⃣ CONSTRUIR objeto del pedido
  const nuevoPedido = {
    idCliente: parseInt(idCliente),
    idEstadoPedido: parseInt(idEstadoPedido),
    idMetodoPago: parseInt(idMetodoPago),
    idTipoEntrega: parseInt(idTipoEntrega),
    idDireccionEntrega: idDireccion ? parseInt(idDireccion) : null,
    montoSubtotal: subtotal,
    montoEnvio: envio,
    montoTotal: total,
    notaCliente: notaCliente,
    detalles: productosCarrito.map(p => ({
      idProducto: parseInt(p.idProducto),
      cantidad: p.cantidad,
      precioUnitario: p.precioUnitario,
      subtotalLinea: p.subtotal
    }))
  };

  // 4️⃣ ENVIAR al backend
  setLoading(true);
  try {
    console.log('📤 Enviando pedido:', nuevoPedido);
    
    // POST /api/pedidos/completo
    const response = await pedidosService.crearPedido(nuevoPedido);
    
    console.log('📥 Respuesta del backend:', response);

    // 5️⃣ RECARGAR lista de pedidos
    const pedidosActualizados = await pedidosService.getPedidos();
    const pedidosOrdenados = pedidosActualizados.sort((a, b) => 
      (a.ID_PEDIDO || a.idPedido) - (b.ID_PEDIDO || b.idPedido)
    );
    setPedidos(pedidosOrdenados);

    alert('Pedido creado exitosamente');

    // 6️⃣ LIMPIAR formulario
    setIdCliente('');
    setIdProducto('');
    setCantidad('1');
    setIdEstadoPedido('1');
    setIdMetodoPago('');
    setIdTipoEntrega('');
    setIdDireccion('');
    setMontoEnvio('0');
    setNotaCliente('');
    setProductosCarrito([]);

  } catch (err) {
    console.error('❌ Error al crear pedido:', err);
    alert('Error: ' + err.message);
  } finally {
    setLoading(false);
  }
};
```

---

### 🔄 Marcar pedido como pagado

```javascript
const handleMarcarComoPagado = async (idPedido) => {
  if (!window.confirm('¿Confirmar que el pedido ha sido pagado?')) {
    return;
  }

  setLoading(true);
  try {
    console.log(`💳 Procesando pago del pedido ${idPedido}...`);
    
    // PUT /api/pedidos/procesar/{idPedido}
    await pedidosService.actualizarPedidoAPagado(idPedido);
    
    console.log('✅ Pedido marcado como pagado');

    // Recargar pedidos para ver el cambio de estado
    const pedidosActualizados = await pedidosService.getPedidos();
    setPedidos(pedidosActualizados);

    alert('Pedido marcado como pagado. Se ha generado la venta y boleta.');

  } catch (err) {
    console.error('❌ Error al procesar pago:', err);
    alert('Error: ' + err.message);
  } finally {
    setLoading(false);
  }
};
```

**¿Qué hace el backend al procesar el pedido?**
1. Cambia `ID_ESTADO_PEDIDO` de 1 (Pendiente) a 2 (Pagado)
2. Llama al microservicio de Ventas
3. Crea registro en tabla `VENTA`
4. Genera boleta con número SII
5. Calcula IVA (19%)
6. Devuelve confirmación

---

### 🗑️ Eliminar pedido

```javascript
const handleEliminarPedido = async (id) => {
  if (!window.confirm('¿Eliminar este pedido?')) {
    return;
  }

  setLoading(true);
  try {
    // DELETE /api/pedidos/{id}
    await pedidosService.eliminarPedido(id);
    
    // Actualizar lista eliminando el pedido
    setPedidos(pedidos.filter(p => 
      (p.ID_PEDIDO || p.idPedido) !== id
    ));

    alert('Pedido eliminado');

  } catch (err) {
    alert('Error al eliminar: ' + err.message);
  } finally {
    setLoading(false);
  }
};
```

---

## Endpoints Utilizados

### 🔹 Pedidos

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| GET | `/api/pedidos` | Listar todos los pedidos | ✅ Requerida |
| GET | `/api/pedidos/{id}` | Obtener pedido por ID | ✅ Requerida |
| POST | `/api/pedidos/completo` | Crear pedido con detalles | ✅ Requerida |
| PUT | `/api/pedidos/procesar/{id}` | Marcar como pagado y generar venta | ✅ Requerida |
| DELETE | `/api/pedidos/{id}` | Eliminar pedido | ✅ Requerida |

### 🔹 Clientes

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| GET | `/api/clientes` | Listar todos los clientes | ✅ Requerida |
| GET | `/api/clientes/{id}/direcciones` | Obtener direcciones de un cliente | ✅ Requerida |

### 🔹 Productos

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| GET | `/api/catalogo/productos` | Listar productos disponibles | ❌ Pública |

### 🔹 Ventas

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| GET | `/api/ventas` | Listar todas las ventas | ✅ Requerida |
| GET | `/api/ventas/{id}` | Obtener venta por ID | ✅ Requerida |
| DELETE | `/api/ventas/{id}` | Eliminar venta y boleta | ✅ Requerida |

---

## 🔐 Autenticación

### Cómo funciona el token JWT

1. **Login:** Usuario inicia sesión
   ```javascript
   POST /api/auth/login
   Body: { username: "admin", password: "123456" }
   ```

2. **Backend responde con token:**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "usuario": { "id": 1, "nombre": "Admin" }
   }
   ```

3. **Frontend guarda el token:**
   ```javascript
   localStorage.setItem("token", response.data.token);
   ```

4. **Axios lo agrega automáticamente:**
   ```javascript
   // En api.js - Interceptor
   api.interceptors.request.use((config) => {
     const token = localStorage.getItem("token");
     if (token) {
       config.headers.Authorization = `Bearer ${token}`;
     }
     return config;
   });
   ```

5. **Todas las peticiones llevan el token:**
   ```
   GET /api/pedidos
   Headers: {
     "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

---

## 📊 Diagrama de Flujo: Crear Pedido

```
┌────────────────────────────────────────────────────────────┐
│  USUARIO llena formulario en gestionPedidos.jsx           │
│  - Selecciona cliente                                      │
│  - Agrega productos al carrito                             │
│  - Elige método de pago, tipo de entrega, dirección       │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  Click en "Crear Pedido"                                   │
│  └── handleCrearPedido()                                   │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  FRONTEND valida datos y construye objeto                 │
│  {                                                          │
│    idCliente: 5,                                           │
│    idEstadoPedido: 1,                                      │
│    montoTotal: 17500,                                      │
│    detalles: [...]                                         │
│  }                                                          │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  pedidosService.crearPedido(nuevoPedido)                  │
│  └── api.post("/pedidos/completo", nuevoPedido)          │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  BACKEND recibe petición                                   │
│  POST /api/pedidos/completo                                │
│  Headers: { Authorization: "Bearer token..." }            │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  BACKEND procesa:                                          │
│  1. Valida token JWT                                       │
│  2. Crea registro en PEDIDO                                │
│  3. Crea registros en DETALLEPEDIDO                        │
│  4. Devuelve pedido creado                                 │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  FRONTEND recibe respuesta                                 │
│  { idPedido: 141, ... }                                    │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  FRONTEND recarga lista de pedidos                         │
│  └── pedidosService.getPedidos()                          │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│  Tabla se actualiza mostrando el nuevo pedido             │
│  ✅ Alert: "Pedido creado exitosamente"                   │
└────────────────────────────────────────────────────────────┘
```

---

## 🐛 Manejo de Errores

### En los servicios

```javascript
export const crearPedido = async (pedido) => {
  try {
    const response = await api.post("/pedidos/completo", pedido);
    return response.data;
  } catch (error) {
    // Log detallado para debugging
    console.error("Error al crear pedido:", error);
    console.error("Status:", error.response?.status);
    console.error("Data:", error.response?.data);
    
    // Re-lanza el error para que el componente lo maneje
    throw error;
  }
};
```

### En el componente

```javascript
try {
  await pedidosService.crearPedido(nuevoPedido);
  alert('✅ Pedido creado');
} catch (err) {
  // Mostrar error al usuario
  console.error('❌ Error:', err);
  
  // Mensaje personalizado según el tipo de error
  if (err.response?.status === 401) {
    alert('Sesión expirada. Por favor inicia sesión nuevamente.');
  } else if (err.response?.status === 400) {
    alert('Datos inválidos: ' + (err.response?.data?.message || err.message));
  } else {
    alert('Error al crear pedido: ' + err.message);
  }
}
```

---

## 📝 Resumen

### ✅ Ventajas de esta arquitectura

1. **Separación de responsabilidades**
   - `api.js`: Configuración centralizada
   - `*Service.js`: Lógica de comunicación con backend
   - `*.jsx`: Lógica de UI y presentación

2. **Reutilización**
   - Los servicios se pueden usar desde cualquier componente
   - Ejemplo: `pedidosService.getPedidos()` se usa en gestionPedidos y dashboard

3. **Mantenibilidad**
   - Si cambia la URL del backend, solo se modifica `api.js`
   - Si cambia un endpoint, solo se modifica el service correspondiente

4. **Debugging**
   - Console.logs claros en cada paso
   - Errores centralizados y manejados

5. **Seguridad**
   - Token JWT automático en todas las peticiones
   - Backend valida permisos en cada endpoint

---

## 🚀 Próximos Pasos

1. **Implementar paginación** en la tabla de pedidos
2. **Agregar filtros** por estado, fecha, cliente
3. **Optimizar recarga** usando websockets para actualizaciones en tiempo real
4. **Cachear datos** de productos y clientes (no cambian frecuentemente)
5. **Agregar loading states** más específicos por sección

---

**Documento generado:** 19 de Noviembre 2025  
**Proyecto:** Golden Burgers - Frontend React  
**Backend:** Spring Boot + Oracle Database

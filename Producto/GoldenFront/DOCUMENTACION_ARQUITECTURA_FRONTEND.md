# 📚 Documentación: Arquitectura y Flujo de Comunicación Frontend

## 🎯 Objetivo del Documento
Explicar paso a paso cómo funciona la comunicación entre el frontend React y el backend de microservicios, desde la configuración inicial hasta el manejo de datos en componentes.

---

## 📋 Índice
1. [Arquitectura General](#1-arquitectura-general)
2. [Configuración de Vite](#2-configuración-de-vite)
3. [Axios: Cliente HTTP](#3-axios-cliente-http)
4. [api.js: Configuración Central](#4-apijs-configuración-central)
5. [Services: Capa de Servicios](#5-services-capa-de-servicios)
6. [fieldMapper.js: Transformación de Datos](#6-fieldmapperjs-transformación-de-datos)
7. [Componentes: Consumo de Datos](#7-componentes-consumo-de-datos)
8. [Flujo Completo: Ejemplo Real](#8-flujo-completo-ejemplo-real)
9. [Manejo de Errores](#9-manejo-de-errores)
10. [Mejores Prácticas](#10-mejores-prácticas)

---

## 1. Arquitectura General

### 🏗️ Estructura de Capas

```
┌─────────────────────────────────────────────────────────┐
│                    COMPONENTES REACT                     │
│         (gestionPedidos.jsx, Login.jsx, etc.)          │
│  - Maneja estado local (useState)                       │
│  - Renderiza UI                                         │
│  - Llama a services para obtener datos                  │
└────────────────────┬────────────────────────────────────┘
                     │ import * as pedidosService
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   CAPA DE SERVICIOS                      │
│     (pedidosService.js, usuariosService.js, etc.)       │
│  - Define funciones para cada endpoint                  │
│  - Usa axios (api.js) para hacer peticiones HTTP        │
│  - Retorna promesas con datos o errores                 │
└────────────────────┬────────────────────────────────────┘
                     │ import api from "../config/api"
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   CONFIGURACIÓN AXIOS                    │
│                      (api.js)                           │
│  - Crea instancia de axios con baseURL                  │
│  - Interceptor de requests: agrega Authorization        │
│  - Interceptor de responses: maneja errores globales    │
│  - Timeout, headers por defecto                         │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Request
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    BACKEND (Microservicios)              │
│  - API Gateway (Puerto 8080)                            │
│    ├── GESTIONUSUARIO (8081) - Clientes, Trabajadores  │
│    ├── GESTIONVENTA (8082) - Ventas                    │
│    ├── GESTIONPEDIDO (8083) - Pedidos                  │
│    ├── GESTIONCATALOGO (8084) - Productos              │
│    └── GESTIONCONTACTO (8085) - Contacto               │
│  - Base de datos Oracle (campos UPPERCASE_SNAKE_CASE)  │
└────────────────────┬────────────────────────────────────┘
                     │ JSON Response
                     ▼
┌─────────────────────────────────────────────────────────┐
│               TRANSFORMACIÓN DE DATOS                    │
│                  (fieldMapper.js)                       │
│  - Convierte campos Oracle → JavaScript                 │
│  - NOMBRE_CLIENTE → nombreCliente                       │
│  - Mantiene compatibilidad con múltiples formatos       │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Configuración de Vite

### 📁 Archivo: `vite.config.js`

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,        // Puerto del servidor de desarrollo
    open: true,        // Abre el navegador automáticamente
    proxy: {           // ⚠️ Opcional: Proxy para evitar CORS en desarrollo
      '/api': {
        target: 'http://161.153.219.128:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  build: {
    outDir: 'dist',    // Carpeta de salida para producción
    sourcemap: true    // Genera source maps para debugging
  }
})
```

### 🎯 Propósito de Vite:
1. **Servidor de desarrollo rápido** con Hot Module Replacement (HMR)
2. **Bundling optimizado** para producción
3. **Soporte nativo** para JSX, CSS, assets
4. **Proxy opcional** para evitar problemas de CORS durante desarrollo

### 📌 Variables de Entorno:

**Archivo: `.env.development`**
```bash
VITE_API_BASE_URL=http://161.153.219.128:8080/api
VITE_ENVIRONMENT=development
```

**Archivo: `.env.production`**
```bash
VITE_API_BASE_URL=https://api.production.com
VITE_ENVIRONMENT=production
```

**Uso en código:**
```javascript
const API_URL = import.meta.env.VITE_API_BASE_URL;
console.log('Ambiente:', import.meta.env.VITE_ENVIRONMENT);
```

---

## 3. Axios: Cliente HTTP

### 📦 Instalación:
```bash
npm install axios
```

### 🔧 ¿Qué es Axios?
Axios es una librería JavaScript que facilita hacer peticiones HTTP (GET, POST, PUT, DELETE) con funcionalidades avanzadas:

- ✅ Basado en Promesas (async/await)
- ✅ Interceptores de request/response
- ✅ Manejo automático de JSON
- ✅ Timeout configurable
- ✅ Cancelación de peticiones
- ✅ Transformación de datos automática

### 📊 Métodos HTTP y Cuándo Usarlos:

| Método | Propósito | Ejemplo |
|--------|-----------|---------|
| **GET** | Obtener datos (lectura) | Listar pedidos, obtener cliente por ID |
| **POST** | Crear nuevos recursos | Crear pedido, registrar usuario |
| **PUT** | Actualizar recurso completo | Actualizar estado de pedido |
| **PATCH** | Actualizar recurso parcial | Cambiar solo el email de un cliente |
| **DELETE** | Eliminar recurso | Eliminar pedido, borrar producto |

---

## 4. api.js: Configuración Central

### 📁 Archivo: `src/config/api.js`

```javascript
import axios from 'axios';

// 1️⃣ CONFIGURACIÓN BASE
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
                     'http://161.153.219.128:8080/api';

console.log('Ambiente:', import.meta.env.MODE);
console.log('API Base URL:', API_BASE_URL);

// 2️⃣ CREAR INSTANCIA DE AXIOS
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,  // 30 segundos máximo por petición
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// 3️⃣ INTERCEPTOR DE REQUESTS (Antes de enviar)
api.interceptors.request.use(
  (config) => {
    // Obtener token de autenticación
    const token = localStorage.getItem('authToken');
    
    if (token) {
      // Agregar token a TODAS las peticiones automáticamente
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Log para debugging
    console.log(`${config.method.toUpperCase()} ${config.url}`);
    
    return config;
  },
  (error) => {
    console.error('Error en request:', error);
    return Promise.reject(error);
  }
);

// 4️⃣ INTERCEPTOR DE RESPONSES (Después de recibir)
api.interceptors.response.use(
  (response) => {
    // Log exitoso
    console.log(`${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    // Manejo de errores global
    if (error.response) {
      // El servidor respondió con un código de error
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          console.error('❌ Petición incorrecta:', data.message);
          break;
        case 401:
          console.error('🔒 No autorizado - Token inválido o expirado');
          // Redirigir al login
          localStorage.removeItem('authToken');
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;
        case 403:
          console.error('🚫 Acceso prohibido');
          break;
        case 404:
          console.error('🔍 Recurso no encontrado');
          break;
        case 500:
          console.error('⚠️ Error del servidor:', data.message);
          break;
        default:
          console.error(`Error ${status}:`, data.message);
      }
    } else if (error.request) {
      // La petición se envió pero no hubo respuesta
      console.error('❌ Sin respuesta del servidor (timeout o red)');
    } else {
      // Error al configurar la petición
      console.error('❌ Error de configuración:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

### 🎯 Propósitos de api.js:

1. **Centralización**: Un solo lugar para configurar axios
2. **Reutilización**: Todos los services usan la misma configuración
3. **Autenticación automática**: Agrega token a todas las peticiones
4. **Manejo de errores global**: Captura errores comunes (401, 403, 500)
5. **Logging consistente**: Todas las peticiones se loguean igual
6. **Timeout global**: Evita peticiones colgadas infinitamente

---

## 5. Services: Capa de Servicios

### 🏛️ Arquitectura de Services

Los **services** son módulos que encapsulan la lógica de comunicación con cada microservicio del backend.

### 📁 Estructura de Carpeta:

```
src/
└── services/
    ├── pedidosService.js       (GESTIONPEDIDO - Puerto 8083)
    ├── usuariosService.js      (GESTIONUSUARIO - Puerto 8081)
    ├── productosService.js     (GESTIONCATALOGO - Puerto 8084)
    ├── ventasService.js        (GESTIONVENTA - Puerto 8082)
    └── contactoService.js      (GESTIONCONTACTO - Puerto 8085)
```

### 📄 Ejemplo Completo: `pedidosService.js`

```javascript
import api from "../config/api";

// ============================================
// CRUD BÁSICO DE PEDIDOS
// ============================================

/**
 * 🔹 GET /api/pedidos
 * Obtiene todos los pedidos del sistema
 * Requiere: Token JWT válido
 * Roles permitidos: ADMIN, TRABAJADOR
 */
export const getPedidos = async () => {
  try {
    const response = await api.get("/pedidos");
    
    // Validar que la respuesta sea un array
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener pedidos:", error);
    throw error; // Re-lanzar el error para que el componente lo maneje
  }
};

/**
 * 🔹 GET /api/pedidos/{id}
 * Obtiene un pedido específico por su ID
 * @param {number} id - ID del pedido
 */
export const getPedidoPorId = async (id) => {
  try {
    const response = await api.get(`/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener pedido ${id}:`, error);
    throw error;
  }
};

/**
 * 🔹 GET /api/pedidos/cliente/{clienteId}
 * Obtiene todos los pedidos de un cliente específico
 * @param {number} clienteId - ID del cliente
 */
export const getPedidosPorCliente = async (clienteId) => {
  try {
    const response = await api.get(`/pedidos/cliente/${clienteId}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener pedidos del cliente ${clienteId}:`, error);
    throw error;
  }
};

/**
 * 🔹 POST /api/pedidos/completo
 * Crea un nuevo pedido con sus detalles
 * @param {Object} pedido - Objeto con datos del pedido
 * @returns {Object} Pedido creado con su ID asignado
 */
export const crearPedido = async (pedido) => {
  try {
    // El backend espera este formato:
    // {
    //   idCliente: 1,
    //   idEstadoPedido: 1,
    //   idMetodoPago: 1,
    //   idTipoEntrega: 1,
    //   idDireccionEntrega: 2,
    //   montoSubtotal: 15000,
    //   montoEnvio: 2000,
    //   montoTotal: 17000,
    //   notaCliente: "Sin cebolla",
    //   detalles: [
    //     { idProducto: 1, cantidad: 2, precioUnitario: 7500, subtotalLinea: 15000 }
    //   ]
    // }
    
    const response = await api.post("/pedidos/completo", pedido);
    return response.data;
  } catch (error) {
    console.error("Error al crear pedido:", error);
    throw error;
  }
};

/**
 * 🔹 PUT /api/pedidos/cambiar-estado/{idPedido}/estado/{idEstado}
 * Actualiza el estado de un pedido (sin crear venta)
 * @param {number} idPedido - ID del pedido
 * @param {number} idEstado - Nuevo ID de estado (1=Pendiente, 2=Pagado, etc.)
 */
export const actualizarEstadoPedido = async (idPedido, idEstado) => {
  try {
    const response = await api.put(
      `/pedidos/cambiar-estado/${idPedido}/estado/${idEstado}`
    );
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar estado del pedido ${idPedido}:`, error);
    throw error;
  }
};

/**
 * 🔹 PUT /api/pedidos/procesar/{idPedido}
 * Marca el pedido como pagado y crea la venta automáticamente
 * @param {number} idPedido - ID del pedido a procesar
 * @returns {Object} Pedido actualizado con estado "Pagado"
 * 
 * ⚠️ IMPORTANTE:
 * - Cambia estado del pedido a "Pagado" (ID 2)
 * - Crea automáticamente un registro en la tabla VENTA
 * - Requiere que el pedido esté en estado "Pendiente de Pago" (ID 1)
 */
export const actualizarPedidoAPagado = async (idPedido) => {
  try {
    // NOTA: El backend requería X-Internal-Token pero causaba CORS
    // Ahora solo usa Authorization: Bearer (incluido automáticamente por api.js)
    const response = await api.put(`/pedidos/procesar/${idPedido}`);
    
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar pedido a pagado ${idPedido}:`, error);
    console.error('Response:', error.response?.data);
    throw error;
  }
};

/**
 * 🔹 DELETE /api/pedidos/{id}
 * Elimina un pedido del sistema
 * @param {number} id - ID del pedido a eliminar
 */
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

### 🎯 Características de un Good Service:

1. **Importa `api` de config**: Reutiliza configuración central
2. **Funciones asíncronas**: Usa `async/await` para manejo de promesas
3. **Try-Catch**: Captura errores específicos del servicio
4. **Validación de datos**: Verifica que response.data sea del tipo esperado
5. **Documentación JSDoc**: Comenta cada función con parámetros y retorno
6. **Re-throw de errores**: Permite que componentes manejen errores específicos
7. **Logging específico**: Mensajes de error descriptivos

---

## 6. fieldMapper.js: Transformación de Datos

### 🔄 Problema que Resuelve

El backend retorna datos directamente desde **Oracle Database**, donde las columnas siguen la convención `UPPERCASE_SNAKE_CASE`:

```sql
-- Tabla CLIENTE en Oracle
CREATE TABLE CLIENTE (
  ID_CLIENTE NUMBER PRIMARY KEY,
  NOMBRE_CLIENTE VARCHAR2(100),
  TELEFONO_CLIENTE VARCHAR2(15),
  EMAIL_CLIENTE VARCHAR2(100)
);
```

El backend retorna JSON sin transformar:

```json
{
  "ID_CLIENTE": 1,
  "NOMBRE_CLIENTE": "Juan Pérez",
  "TELEFONO_CLIENTE": "912345678",
  "EMAIL_CLIENTE": "juan@example.com"
}
```

Pero en JavaScript/React usamos `camelCase`:

```javascript
{
  idCliente: 1,
  nombreCliente: "Juan Pérez",
  telefonoCliente: "912345678",
  emailCliente: "juan@example.com"
}
```

### 📁 Archivo: `src/utils/fieldMapper.js`

```javascript
/**
 * 🔧 FUNCIÓN PRINCIPAL: getField()
 * Intenta obtener el valor de un campo probando múltiples variaciones de nombre
 */
export const getField = (obj, fieldName, defaultValue = null) => {
  if (!obj) return defaultValue;

  // 1. Probar nombre original (camelCase): nombreCliente
  if (obj[fieldName] !== undefined) return obj[fieldName];

  // 2. Convertir a UPPERCASE_SNAKE_CASE: NOMBRE_CLIENTE
  const upperSnakeCase = fieldName
    .replace(/([A-Z])/g, '_$1')  // Inserta _ antes de mayúsculas
    .toUpperCase()               // Convierte todo a mayúsculas
    .replace(/^_/, '');          // Elimina _ inicial si existe
  
  if (obj[upperSnakeCase] !== undefined) return obj[upperSnakeCase];

  // 3. Probar snake_case minúsculas: nombre_cliente
  const lowerSnakeCase = fieldName
    .replace(/([A-Z])/g, '_$1')
    .toLowerCase()
    .replace(/^_/, '');
  
  if (obj[lowerSnakeCase] !== undefined) return obj[lowerSnakeCase];

  // 4. Si ninguna variación existe, retornar valor por defecto
  return defaultValue;
};

/**
 * 🧑 MAPEO DE CLIENTE
 * Transforma objeto de cliente para compatibilidad múltiple
 */
export const mapCliente = (cliente) => {
  if (!cliente) return null;
  
  return {
    // Campos transformados (acceso fácil)
    id: getField(cliente, 'idCliente') || getField(cliente, 'id'),
    idCliente: getField(cliente, 'idCliente') || getField(cliente, 'id'),
    nombre: getField(cliente, 'nombreCliente') || getField(cliente, 'nombre'),
    telefono: getField(cliente, 'telefonoCliente') || getField(cliente, 'telefono'),
    email: getField(cliente, 'emailCliente') || getField(cliente, 'email'),
    
    // IMPORTANTE: Mantener campos originales por compatibilidad
    // Esto permite acceder tanto a cliente.nombre como a cliente.NOMBRE_CLIENTE
    ...cliente
  };
};

/**
 * 📦 MAPEO DE PEDIDO
 * Incluye mapeo recursivo de detalles
 */
export const mapPedido = (pedido) => {
  if (!pedido) return null;
  
  return {
    id: getField(pedido, 'idPedido') || getField(pedido, 'id'),
    idPedido: getField(pedido, 'idPedido') || getField(pedido, 'id'),
    idCliente: getField(pedido, 'idCliente'),
    idEstadoPedido: getField(pedido, 'idEstadoPedido'),
    idMetodoPago: getField(pedido, 'idMetodoPago'),
    idTipoEntrega: getField(pedido, 'idTipoEntrega'),
    montoSubtotal: getField(pedido, 'montoSubtotal', 0),
    montoEnvio: getField(pedido, 'montoEnvio', 0),
    montoTotal: getField(pedido, 'montoTotal', 0),
    notaCliente: getField(pedido, 'notaCliente'),
    
    // Mapear array de detalles recursivamente
    detalles: pedido.detalles?.map(mapDetallePedido) || [],
    
    ...pedido
  };
};

/**
 * 🛒 MAPEO DE DETALLE DE PEDIDO
 */
export const mapDetallePedido = (detalle) => {
  if (!detalle) return null;
  
  return {
    idDetalle: getField(detalle, 'idDetalle') || getField(detalle, 'id'),
    idProducto: getField(detalle, 'idProducto'),
    cantidad: getField(detalle, 'cantidad', 0),
    precioUnitario: getField(detalle, 'precioUnitario', 0),
    subtotalLinea: getField(detalle, 'subtotalLinea', 0),
    nombreProducto: getField(detalle, 'nombreProducto'),
    
    // Si el detalle incluye el objeto producto completo, mapearlo
    producto: detalle.producto ? mapProducto(detalle.producto) : null,
    
    ...detalle
  };
};

/**
 * 🍔 MAPEO DE PRODUCTO
 */
export const mapProducto = (producto) => {
  if (!producto) return null;
  
  return {
    idProducto: getField(producto, 'idProducto') || getField(producto, 'id'),
    nombreProducto: getField(producto, 'nombreProducto') || getField(producto, 'nombre'),
    precio: getField(producto, 'precioBase') || getField(producto, 'precio', 0),
    descripcion: getField(producto, 'descripcion'),
    imagenUrl: getField(producto, 'imagenUrl'),
    disponible: getField(producto, 'disponible', true),
    
    ...producto
  };
};

/**
 * 📋 MAPEO DE ARRAYS
 * Aplica función de mapeo a cada elemento del array
 */
export const mapArray = (array, mapFn) => {
  if (!Array.isArray(array)) return [];
  return array.map(mapFn);
};
```

### 🎯 Beneficios del fieldMapper:

1. **Compatibilidad total**: Funciona con UPPERCASE, camelCase, snake_case
2. **Sin errores**: Si un campo no existe, retorna `null` en lugar de `undefined`
3. **Mantenibilidad**: Un solo lugar para ajustar transformaciones
4. **Reutilizable**: Funciona para todos los endpoints que usen Oracle
5. **Preserva datos originales**: El spread `...objeto` mantiene campos originales

---

## 7. Componentes: Consumo de Datos

### 📄 Ejemplo: `gestionPedidos.jsx`

```javascript
import React, { useState, useEffect } from 'react';
import * as pedidosService from '../../services/pedidosService';
import * as usuariosService from '../../services/usuariosService';
import * as productosService from '../../services/productosService';
import { mapCliente, mapPedido, mapProducto, mapArray } from '../../utils/fieldMapper';

function GestionPedidos() {
  // ============================================
  // 1️⃣ ESTADOS DEL COMPONENTE
  // ============================================
  
  // Estados para datos del backend
  const [pedidos, setPedidos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  
  // Estados para el formulario
  const [idCliente, setIdCliente] = useState('');
  const [idProducto, setIdProducto] = useState('');
  const [cantidad, setCantidad] = useState('1');
  
  // Estados de UI
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // ============================================
  // 2️⃣ CARGAR DATOS AL MONTAR EL COMPONENTE
  // ============================================
  
  useEffect(() => {
    inicializarDatos();
  }, []); // Array vacío = solo se ejecuta una vez al montar

  const inicializarDatos = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // ✅ CARGAR PEDIDOS
      const pedidosData = await pedidosService.getPedidos();
      const pedidosMapeados = mapArray(pedidosData, mapPedido);
      setPedidos(pedidosMapeados);
      console.log('✅ Pedidos cargados:', pedidosData.length);
      
      // ✅ CARGAR CLIENTES
      const clientesData = await usuariosService.obtenerTodosClientes();
      const clientesMapeados = mapArray(clientesData, mapCliente);
      setClientes(clientesMapeados);
      console.log('✅ Clientes cargados:', clientesData.length);
      
      // ✅ CARGAR PRODUCTOS
      try {
        const productosData = await productosService.obtenerProductosDisponibles();
        const productosMapeados = mapArray(productosData, mapProducto);
        setProductos(productosMapeados);
        console.log('✅ Productos cargados:', productosData.length);
      } catch (err) {
        // Fallback a productos mock si el backend da 403
        console.warn('⚠️ Error 403, usando productos MOCK');
        setProductos([
          { idProducto: 1, nombreProducto: 'Hamburguesa Clásica', precio: 8000 },
          { idProducto: 2, nombreProducto: 'Papas Fritas', precio: 3000 }
        ]);
      }
      
    } catch (err) {
      setError(err.message);
      console.error('Error cargando datos:', err);
    } finally {
      setLoading(false);
    }
  };

  // ============================================
  // 3️⃣ FUNCIÓN PARA CREAR PEDIDO
  // ============================================
  
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validación
    if (!idCliente || !idProducto) {
      alert('Por favor, completa todos los campos');
      return;
    }

    // Calcular montos
    const producto = productos.find(p => p.idProducto == idProducto);
    const subtotal = producto.precio * parseInt(cantidad);
    const envio = 2000; // Fijo o calculado
    const total = subtotal + envio;

    // Construir objeto para el backend
    const nuevoPedido = {
      idCliente: parseInt(idCliente),
      idEstadoPedido: 1, // Pendiente de Pago
      idMetodoPago: 1,   // Webpay
      idTipoEntrega: 1,  // Delivery
      montoSubtotal: subtotal,
      montoEnvio: envio,
      montoTotal: total,
      detalles: [
        {
          idProducto: parseInt(idProducto),
          cantidad: parseInt(cantidad),
          precioUnitario: producto.precio,
          subtotalLinea: subtotal
        }
      ]
    };

    setLoading(true);
    try {
      // ✅ LLAMAR AL SERVICE
      const pedidoCreado = await pedidosService.crearPedido(nuevoPedido);
      
      // ✅ ACTUALIZAR ESTADO LOCAL
      setPedidos([...pedidos, pedidoCreado]);
      
      alert('✅ Pedido creado exitosamente');
      
      // ✅ LIMPIAR FORMULARIO
      setIdCliente('');
      setIdProducto('');
      setCantidad('1');
      
    } catch (err) {
      alert('❌ Error al crear pedido: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // ============================================
  // 4️⃣ FUNCIÓN PARA MARCAR COMO PAGADO
  // ============================================
  
  const handleMarcarComoPagado = async (idPedido) => {
    if (!window.confirm('¿Confirmar pago?')) return;

    setLoading(true);
    try {
      // ✅ LLAMAR AL ENDPOINT QUE PROCESA PAGO Y CREA VENTA
      await pedidosService.actualizarPedidoAPagado(idPedido);
      
      // ✅ ACTUALIZAR ESTADO LOCAL (cambiar a estado "Pagado" = 2)
      setPedidos(pedidos.map(p => 
        p.idPedido === idPedido 
          ? { ...p, idEstadoPedido: 2 }
          : p
      ));
      
      alert('✅ Pedido marcado como pagado');
      
    } catch (err) {
      alert('❌ Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // ============================================
  // 5️⃣ HELPER PARA OBTENER NOMBRES
  // ============================================
  
  const getNombreCliente = (idCliente) => {
    const cliente = clientes.find(c => c.idCliente === idCliente);
    return cliente?.nombre || 'N/A';
  };

  const getNombreEstadoPedido = (idEstado) => {
    const estados = {
      1: 'Pendiente de Pago',
      2: 'Pagado',
      3: 'En Preparación',
      4: 'En Camino',
      5: 'Entregado',
      6: 'Cancelado'
    };
    return estados[idEstado] || 'Desconocido';
  };

  // ============================================
  // 6️⃣ RENDERIZADO
  // ============================================
  
  return (
    <div>
      <h1>Gestión de Pedidos</h1>

      {/* Mostrar errores */}
      {error && <div className="error">{error}</div>}

      {/* Formulario de creación */}
      <form onSubmit={handleSubmit}>
        <select value={idCliente} onChange={(e) => setIdCliente(e.target.value)}>
          <option value="">Seleccione Cliente</option>
          {clientes.map(cliente => (
            <option key={cliente.idCliente} value={cliente.idCliente}>
              {cliente.nombre}
            </option>
          ))}
        </select>

        <select value={idProducto} onChange={(e) => setIdProducto(e.target.value)}>
          <option value="">Seleccione Producto</option>
          {productos.map(producto => (
            <option key={producto.idProducto} value={producto.idProducto}>
              {producto.nombreProducto} - ${producto.precio}
            </option>
          ))}
        </select>

        <input 
          type="number" 
          value={cantidad} 
          onChange={(e) => setCantidad(e.target.value)}
          min="1"
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Creando...' : 'Crear Pedido'}
        </button>
      </form>

      {/* Tabla de pedidos */}
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Cliente</th>
            <th>Estado</th>
            <th>Total</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {pedidos.map(pedido => (
            <tr key={pedido.idPedido}>
              <td>{pedido.idPedido}</td>
              <td>{getNombreCliente(pedido.idCliente)}</td>
              <td>{getNombreEstadoPedido(pedido.idEstadoPedido)}</td>
              <td>${pedido.montoTotal}</td>
              <td>
                {/* Botón solo visible si está pendiente */}
                {pedido.idEstadoPedido === 1 && (
                  <button onClick={() => handleMarcarComoPagado(pedido.idPedido)}>
                    💳 Marcar Pagado
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default GestionPedidos;
```

---

## 8. Flujo Completo: Ejemplo Real

### 🔄 Flujo: Usuario marca un pedido como pagado

```
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣ USUARIO HACE CLIC EN BOTÓN "MARCAR COMO PAGADO"              │
│    Componente: gestionPedidos.jsx                               │
│    Función: handleMarcarComoPagado(idPedido = 15)               │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2️⃣ LLAMADA AL SERVICE                                            │
│    pedidosService.actualizarPedidoAPagado(15)                   │
│    Archivo: src/services/pedidosService.js                      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3️⃣ SERVICE USA INSTANCIA DE AXIOS                                │
│    api.put("/pedidos/procesar/15")                              │
│    Archivo: src/config/api.js                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4️⃣ INTERCEPTOR DE REQUEST (Antes de enviar)                     │
│    - Agrega header: Authorization: Bearer eyJhbG...             │
│    - Log: "PUT /pedidos/procesar/15"                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5️⃣ PETICIÓN HTTP AL BACKEND                                     │
│    PUT http://161.153.219.128:8080/api/pedidos/procesar/15     │
│    Headers:                                                     │
│      - Authorization: Bearer eyJhbGc...                         │
│      - Content-Type: application/json                           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6️⃣ API GATEWAY (Puerto 8080)                                    │
│    - Valida token JWT                                           │
│    - Verifica rol (ADMIN o TRABAJADOR)                          │
│    - Enruta a microservicio GESTIONPEDIDO (Puerto 8083)        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7️⃣ MICROSERVICIO GESTIONPEDIDO                                  │
│    Controlador: @PutMapping("/procesar/{idPedido}")            │
│    Lógica:                                                      │
│      1. Busca pedido con ID 15                                  │
│      2. Valida que esté en estado "Pendiente" (ID 1)           │
│      3. Actualiza ID_ESTADO_PEDIDO = 2 (Pagado)                │
│      4. Llama a ventaClienteService.crearVentaDesdePedido()    │
│      5. Crea registro en tabla VENTA                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 8️⃣ BASE DE DATOS ORACLE                                         │
│    UPDATE PEDIDO                                                │
│    SET ID_ESTADO_PEDIDO = 2                                     │
│    WHERE ID_PEDIDO = 15;                                        │
│                                                                 │
│    INSERT INTO VENTA (ID_PEDIDO, FECHA_VENTA, MONTO_VENTA)     │
│    VALUES (15, SYSDATE, 17000);                                 │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 9️⃣ RESPUESTA DEL BACKEND                                        │
│    Status: 200 OK                                               │
│    Body: {                                                      │
│      "ID_PEDIDO": 15,                                           │
│      "ID_ESTADO_PEDIDO": 2,                                     │
│      "MONTO_TOTAL": 17000,                                      │
│      ...                                                        │
│    }                                                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 🔟 INTERCEPTOR DE RESPONSE (Después de recibir)                 │
│    - Log: "200 /pedidos/procesar/15"                           │
│    - Retorna response.data al service                           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣1️⃣ SERVICE RETORNA DATOS AL COMPONENTE                          │
│    return response.data;                                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣2️⃣ COMPONENTE ACTUALIZA ESTADO LOCAL                           │
│    setPedidos(pedidos.map(p =>                                  │
│      p.idPedido === 15                                          │
│        ? { ...p, idEstadoPedido: 2 }  ← Cambia estado a Pagado │
│        : p                                                      │
│    ));                                                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣3️⃣ REACT RE-RENDERIZA COMPONENTE                               │
│    - Pedido #15 ahora muestra estado "Pagado"                  │
│    - Botón "💳 Marcar Pagado" desaparece (condición: estado=1) │
│    - Usuario ve cambio instantáneo en la UI                    │
└─────────────────────────────────────────────────────────────────┘
```

### ⏱️ Tiempo Total: ~500ms - 2 segundos
- 50ms: JavaScript local (validación, construcción de objeto)
- 100ms: Petición HTTP (depende de latencia de red)
- 200ms: Backend (validación, lógica, BD)
- 50ms: Respuesta HTTP
- 10ms: Actualización de estado React
- 16ms: Re-renderizado React (60fps)

---

## 9. Manejo de Errores

### 🚨 Tipos de Errores y Cómo Manejarlos

#### A) **Errores de Red (Sin Conexión)**

```javascript
// En api.js - Interceptor de Response
if (error.request) {
  // La petición se envió pero no hubo respuesta
  console.error('❌ Sin respuesta del servidor (timeout o red)');
  
  // Mostrar mensaje al usuario
  alert('No se pudo conectar al servidor. Verifica tu conexión a internet.');
}
```

#### B) **Errores de Autenticación (401)**

```javascript
case 401:
  console.error('🔒 No autorizado - Token inválido o expirado');
  
  // Limpiar sesión
  localStorage.removeItem('authToken');
  localStorage.removeItem('user');
  
  // Redirigir al login
  window.location.href = '/login';
  break;
```

#### C) **Errores de Permisos (403)**

```javascript
case 403:
  console.error('🚫 Acceso prohibido');
  alert('No tienes permisos para realizar esta acción');
  break;
```

#### D) **Errores de Validación (400)**

```javascript
case 400:
  console.error('❌ Petición incorrecta:', data.message);
  // Mostrar mensaje específico del backend
  alert(`Error de validación: ${data.message}`);
  break;
```

#### E) **Errores del Servidor (500)**

```javascript
case 500:
  console.error('⚠️ Error del servidor:', data.message);
  alert('Ocurrió un error en el servidor. Intenta nuevamente más tarde.');
  break;
```

### 🎯 Estrategia de Manejo en Componentes

```javascript
const [error, setError] = useState(null);
const [loading, setLoading] = useState(false);

const cargarDatos = async () => {
  setLoading(true);
  setError(null); // Limpiar errores previos
  
  try {
    const data = await pedidosService.getPedidos();
    setPedidos(data);
  } catch (err) {
    // Capturar error y mostrarlo en UI
    setError(err.response?.data?.message || err.message || 'Error desconocido');
  } finally {
    // Siempre ejecutar (éxito o error)
    setLoading(false);
  }
};

// En el JSX
{error && (
  <div className="alert alert-danger">
    ⚠️ {error}
  </div>
)}

{loading && <div className="spinner">Cargando...</div>}
```

---

## 10. Mejores Prácticas

### ✅ DO (Hacer):

1. **Usar async/await** en lugar de `.then()` para mejor legibilidad
2. **Validar datos** antes de enviarlos al backend
3. **Manejar loading states** para feedback visual
4. **Capturar errores específicos** en cada componente
5. **Usar try-catch-finally** para limpieza consistente
6. **Loguear requests** en desarrollo para debugging
7. **Centralizar configuración** de axios en un solo archivo
8. **Transformar datos** con fieldMapper para compatibilidad
9. **Reutilizar funciones** de services en múltiples componentes
10. **Comentar endpoints** con JSDoc para documentación

### ❌ DON'T (Evitar):

1. ❌ **Hardcodear tokens** en el código (usar localStorage)
2. ❌ **Ignorar errores** sin manejo (siempre usar try-catch)
3. ❌ **Peticiones síncronas** que bloqueen UI
4. ❌ **Múltiples instancias de axios** sin configuración centralizada
5. ❌ **Exponer credenciales** en frontend (usar backend proxy)
6. ❌ **No validar tipos** de datos recibidos del backend
7. ❌ **Olvidar loading states** (mala UX)
8. ❌ **No limpiar estados** después de crear/actualizar recursos
9. ❌ **Peticiones duplicadas** (usar debouncing si es necesario)
10. ❌ **Headers personalizados innecesarios** que causen CORS

---

## 📌 Resumen de Arquitectura

```
COMPONENTE
    ↓ llama a
SERVICE (pedidosService.js)
    ↓ usa
API.JS (instancia axios configurada)
    ↓ envía HTTP request
BACKEND (microservicios)
    ↓ consulta
BASE DE DATOS ORACLE
    ↓ retorna datos en UPPERCASE_SNAKE_CASE
BACKEND (responde JSON)
    ↓ recibe
API.JS (interceptor de response)
    ↓ retorna a
SERVICE
    ↓ aplica
FIELDMAPPER (transforma a camelCase)
    ↓ retorna a
COMPONENTE
    ↓ actualiza
ESTADO (useState)
    ↓ dispara
RE-RENDER (React actualiza UI)
```

---

## 🎓 Conceptos Clave Aprendidos

### 1. **Separación de Responsabilidades**
- **Componentes**: Solo UI y lógica de presentación
- **Services**: Solo comunicación con API
- **Utils**: Solo transformación de datos
- **Config**: Solo configuración

### 2. **Flujo de Datos Unidireccional**
```
Backend → Service → Componente → Estado → UI
```

### 3. **Gestión de Estado Asíncrono**
```javascript
const [data, setData] = useState([]);      // Datos
const [loading, setLoading] = useState(false); // Estado de carga
const [error, setError] = useState(null);      // Errores
```

### 4. **Interceptores de Axios**
- **Request**: Modificar peticiones antes de enviarlas
- **Response**: Procesar respuestas antes de devolverlas

### 5. **Transformación de Datos**
- Oracle usa `UPPERCASE_SNAKE_CASE`
- JavaScript usa `camelCase`
- fieldMapper convierte automáticamente

---

## 🚀 Próximos Pasos

### Para Mejorar la Arquitectura:

1. **Implementar React Context** para compartir estado global
2. **Usar React Query** para cache y sincronización de datos
3. **Agregar TypeScript** para type safety
4. **Implementar tests unitarios** para services
5. **Agregar paginación** para listas grandes
6. **Implementar retry logic** para peticiones fallidas
7. **Agregar WebSocket** para actualizaciones en tiempo real
8. **Optimizar re-renders** con React.memo y useMemo

---

## 📞 Contacto y Soporte

Si tienes dudas sobre esta arquitectura, revisa:

1. **Logs de consola**: Todas las peticiones se loguean
2. **Network tab** del navegador: Ver requests/responses
3. **React DevTools**: Inspeccionar estado de componentes
4. **Este documento**: Referencia completa del flujo

---

**Última actualización**: 19 de Noviembre, 2025
**Autor**: Implementación de arquitectura frontend React + Axios
**Versión**: 1.0.0

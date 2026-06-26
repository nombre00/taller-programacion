# Documentación de Endpoints - Golden Burgers

Esta documentación contiene todos los endpoints disponibles en los microservicios de Golden Burgers para la integración con el frontend.

## Tabla de Contenidos
- [1. Microservicio Gestión Usuario (Puerto 8081)](#1-microservicio-gestión-usuario-puerto-8081)
  - [1.1 Usuarios](#11-usuarios)
  - [1.2 Clientes](#12-clientes)
  - [1.3 Trabajadores](#13-trabajadores)
  - [1.4 Ciudades](#14-ciudades)
  - [1.5 Roles](#15-roles)
- [2. Microservicio Gestión Catálogo (Puerto 8084)](#2-microservicio-gestión-catálogo-puerto-8084)
  - [2.1 Productos](#21-productos)
  - [2.2 Categorías](#22-categorías)
- [3. Microservicio Gestión Pedidos (Puerto 8083)](#3-microservicio-gestión-pedidos-puerto-8083)
  - [3.1 Pedidos](#31-pedidos)
  - [3.2 Pagos](#32-pagos)
- [4. Microservicio Gestión Ventas (Puerto 8082)](#4-microservicio-gestión-ventas-puerto-8082)
  - [4.1 Ventas](#41-ventas)
  - [4.2 Boletas](#42-boletas)
  - [4.3 Devoluciones](#43-devoluciones)
  - [4.4 Dashboard](#44-dashboard)
- [5. Microservicio Gestión Contacto (Puerto 8085)](#5-microservicio-gestión-contacto-puerto-8085)
  - [5.1 Mensajes de Contacto](#51-mensajes-de-contacto)

---

## 1. Microservicio Gestión Usuario (Puerto 8081)

Base URL: `http://localhost:8081/api`

### 1.1 Usuarios

#### 1.1.1 Obtener usuario por Firebase UID
```
GET /usuarios/firebase/{uid}
URL Completa: http://localhost:8081/api/usuarios/firebase/{uid}
```
**Descripción:** Obtiene información de usuario por Firebase UID (usado por API Gateway durante login)

**Parámetros de ruta:**
- `uid` (String): Firebase UID del usuario

**Autorización:** Interno (API Gateway)

**Respuesta exitosa (200):**
```json
{
  "firebaseUid": "abc123...",
  "email": "usuario@email.com",
  "rolId": 1,
  "rolNombre": "Cliente"
}
```

**Respuesta no encontrado (404):** Usuario no existe

---

#### 1.1.2 Verificar si email existe
```
GET /usuarios/exists/email/{email}
URL Completa: http://localhost:8081/api/usuarios/exists/email/{email}
```
**Descripción:** Verifica si un email ya está registrado en el sistema

**Parámetros de ruta:**
- `email` (String): Email a verificar

**Autorización:** Público

**Respuesta exitosa (200):**
```json
true
```

---

#### 1.1.3 Verificar si Firebase UID existe
```
GET /usuarios/exists/uid/{uid}
URL Completa: http://localhost:8081/api/usuarios/exists/uid/{uid}
```
**Descripción:** Verifica si un Firebase UID ya está registrado

**Parámetros de ruta:**
- `uid` (String): Firebase UID a verificar

**Autorización:** Público

**Respuesta exitosa (200):**
```json
false
```

---

### 1.2 Clientes

#### 1.2.1 Registrar nuevo cliente
```
POST /clientes
URL Completa: http://localhost:8081/api/clientes
```
**Descripción:** Registra un nuevo cliente en el sistema

**Autorización:** Requiere Bearer Token

**Body (JSON):**
```json
{
  "idUsuario": "firebase-uid-123",
  "email": "cliente@email.com",
  "nombreCliente": "Juan Pérez",
  "telefonoCliente": "987654321"
}
```

**Respuesta exitosa (201):**
```json
{
  "idCliente": 1,
  "idUsuario": "firebase-uid-123",
  "email": "cliente@email.com",
  "nombreCliente": "Juan Pérez",
  "telefonoCliente": "987654321",
  "rolNombre": "Cliente"
}
```

---

#### 1.2.2 Listar todos los clientes
```
GET /clientes
URL Completa: http://localhost:8081/api/clientes
```
**Descripción:** Obtiene lista de todos los clientes

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):** Array de clientes (ver 1.2.1)

---

#### 1.2.3 Obtener cliente por ID
```
GET /clientes/{id}
URL Completa: http://localhost:8081/api/clientes/{id}
```
**Descripción:** Obtiene un cliente específico por ID

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `id` (Long): ID del cliente

**Respuesta exitosa (200):** Ver 1.2.1

**Respuesta no encontrado (404):** Cliente no existe

---

#### 1.2.4 Obtener cliente por Firebase UID
```
GET /clientes/usuario/{idUsuario}
URL Completa: http://localhost:8081/api/clientes/usuario/{idUsuario}
```
**Descripción:** Busca cliente por Firebase UID

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `idUsuario` (String): Firebase UID

**Respuesta exitosa (200):** Ver 1.2.1

---

#### 1.2.5 Obtener cliente por email
```
GET /clientes/email/{email}
URL Completa: http://localhost:8081/api/clientes/email/{email}
```
**Descripción:** Busca cliente por email

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `email` (String): Email del cliente

**Respuesta exitosa (200):** Ver 1.2.1

---

#### 1.2.6 Actualizar cliente
```
PUT /clientes/{id}?nombreCliente={nombre}&telefonoCliente={telefono}
URL Completa: http://localhost:8081/api/clientes/{id}
```
**Descripción:** Actualiza nombre y teléfono de un cliente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del cliente

**Parámetros de consulta:**
- `nombreCliente` (String): Nuevo nombre
- `telefonoCliente` (String, opcional): Nuevo teléfono (9 dígitos)

**Respuesta exitosa (200):** Cliente actualizado

---

#### 1.2.7 Actualizar perfil del cliente autenticado
```
PUT /clientes/perfil
URL Completa: http://localhost:8081/api/clientes/perfil
```
**Descripción:** Permite al cliente actualizar su propio perfil

**Autorización:** Requiere Bearer Token (el Firebase UID se extrae del token)

**Body (JSON):**
```json
{
  "nombreCliente": "Juan Carlos Pérez",
  "email": "nuevo@email.com",
  "telefonoCliente": "912345678"
}
```

**Respuesta exitosa (200):** Cliente actualizado

---

#### 1.2.8 Actualizar email de cliente (Admin)
```
PUT /clientes/{id}/email
URL Completa: http://localhost:8081/api/clientes/{id}/email
```
**Descripción:** Permite a Admin modificar email de un cliente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del cliente

**Body (JSON):**
```json
{
  "nuevoEmail": "nuevo@email.com"
}
```

**Respuesta exitosa (200):** Cliente con email actualizado

---

#### 1.2.9 Eliminar cliente
```
DELETE /clientes/{id}
URL Completa: http://localhost:8081/api/clientes/{id}
```
**Descripción:** Elimina un cliente del sistema

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del cliente

**Respuesta exitosa (204):** Cliente eliminado

---

#### 1.2.10 Agregar dirección a cliente
```
POST /clientes/direcciones
URL Completa: http://localhost:8081/api/clientes/direcciones
```
**Descripción:** Agrega una nueva dirección de entrega a un cliente

**Autorización:** Requiere Bearer Token

**Body (JSON):**
```json
{
  "idCliente": 1,
  "idCiudad": 1,
  "direccion": "Av. Principal 123, Depto 45",
  "alias": "Casa"
}
```

**Respuesta exitosa (201):**
```json
{
  "idDireccion": 1,
  "idCliente": 1,
  "idCiudad": 1,
  "nombreCiudad": "Santiago",
  "direccion": "Av. Principal 123, Depto 45",
  "alias": "Casa"
}
```

---

#### 1.2.11 Listar direcciones de un cliente
```
GET /clientes/{idCliente}/direcciones
URL Completa: http://localhost:8081/api/clientes/{idCliente}/direcciones
```
**Descripción:** Obtiene todas las direcciones de un cliente

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `idCliente` (Long): ID del cliente

**Respuesta exitosa (200):** Array de direcciones (ver 1.2.10)

---

#### 1.2.12 Actualizar dirección
```
PUT /clientes/direcciones/{idDireccion}
URL Completa: http://localhost:8081/api/clientes/direcciones/{idDireccion}
```
**Descripción:** Actualiza una dirección existente

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `idDireccion` (Long): ID de la dirección

**Body (JSON):**
```json
{
  "idCiudad": 2,
  "direccion": "Nueva dirección 456",
  "alias": "Trabajo"
}
```

**Respuesta exitosa (200):** Dirección actualizada

---

#### 1.2.13 Eliminar dirección
```
DELETE /clientes/direcciones/{idDireccion}
URL Completa: http://localhost:8081/api/clientes/direcciones/{idDireccion}
```
**Descripción:** Elimina una dirección específica

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `idDireccion` (Long): ID de la dirección

**Respuesta exitosa (204):** Dirección eliminada

---

### 1.3 Trabajadores

#### 1.3.1 Registrar nuevo trabajador
```
POST /trabajadores
URL Completa: http://localhost:8081/api/trabajadores
```
**Descripción:** Registra un nuevo trabajador (solo Admin)

**Autorización:** ADMIN

**Body (JSON):**
```json
{
  "idUsuario": "firebase-uid-456",
  "email": "trabajador@goldenburgers.com",
  "nombreTrabajador": "María González",
  "rutTrabajador": "12.345.678-9"
}
```

**Respuesta exitosa (201):**
```json
{
  "idTrabajador": 1,
  "idUsuario": "firebase-uid-456",
  "email": "trabajador@goldenburgers.com",
  "nombreTrabajador": "María González",
  "rutTrabajador": "12.345.678-9",
  "rolNombre": "Trabajador"
}
```

---

#### 1.3.2 Listar todos los trabajadores
```
GET /trabajadores
URL Completa: http://localhost:8081/api/trabajadores
```
**Descripción:** Obtiene lista de todos los trabajadores

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):** Array de trabajadores (ver 1.3.1)

---

#### 1.3.3 Obtener trabajador por ID
```
GET /trabajadores/{id}
URL Completa: http://localhost:8081/api/trabajadores/{id}
```
**Descripción:** Obtiene un trabajador específico

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del trabajador

**Respuesta exitosa (200):** Ver 1.3.1

---

#### 1.3.4 Obtener trabajador por Firebase UID
```
GET /trabajadores/usuario/{idUsuario}
URL Completa: http://localhost:8081/api/trabajadores/usuario/{idUsuario}
```
**Descripción:** Busca trabajador por Firebase UID

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `idUsuario` (String): Firebase UID

**Respuesta exitosa (200):** Ver 1.3.1

---

#### 1.3.5 Obtener trabajador por RUT
```
GET /trabajadores/rut/{rut}
URL Completa: http://localhost:8081/api/trabajadores/rut/{rut}
```
**Descripción:** Busca trabajador por RUT chileno

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `rut` (String): RUT del trabajador (formato: 12.345.678-9)

**Respuesta exitosa (200):** Ver 1.3.1

---

#### 1.3.6 Actualizar trabajador
```
PUT /trabajadores/{id}?nombreTrabajador={nombre}&rutTrabajador={rut}
URL Completa: http://localhost:8081/api/trabajadores/{id}
```
**Descripción:** Actualiza nombre y RUT de un trabajador

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del trabajador

**Parámetros de consulta:**
- `nombreTrabajador` (String): Nuevo nombre
- `rutTrabajador` (String): Nuevo RUT

**Respuesta exitosa (200):** Trabajador actualizado

---

#### 1.3.7 Actualizar email de trabajador
```
PUT /trabajadores/{id}/email
URL Completa: http://localhost:8081/api/trabajadores/{id}/email
```
**Descripción:** Actualiza el email de un trabajador

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del trabajador

**Body (JSON):**
```json
{
  "nuevoEmail": "nuevo@goldenburgers.com"
}
```

**Respuesta exitosa (200):** Trabajador con email actualizado

---

#### 1.3.8 Cambiar rol de trabajador
```
PUT /trabajadores/{id}/rol
URL Completa: http://localhost:8081/api/trabajadores/{id}/rol
```
**Descripción:** Cambia el rol entre Trabajador (2) y Admin (3)

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del trabajador

**Body (JSON):**
```json
{
  "idRol": 3
}
```

**Respuesta exitosa (200):** Trabajador con rol actualizado

---

#### 1.3.9 Eliminar trabajador
```
DELETE /trabajadores/{id}
URL Completa: http://localhost:8081/api/trabajadores/{id}
```
**Descripción:** Elimina un trabajador del sistema

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del trabajador

**Respuesta exitosa (204):** Trabajador eliminado

---

### 1.4 Ciudades

#### 1.4.1 Listar todas las ciudades
```
GET /ciudades
URL Completa: http://localhost:8081/api/ciudades
```
**Descripción:** Obtiene lista de todas las ciudades disponibles

**Autorización:** Público

**Respuesta exitosa (200):**
```json
[
  {
    "idCiudad": 1,
    "nombreCiudad": "Santiago"
  },
  {
    "idCiudad": 2,
    "nombreCiudad": "Valparaíso"
  }
]
```

---

#### 1.4.2 Obtener ciudad por ID
```
GET /ciudades/{id}
URL Completa: http://localhost:8081/api/ciudades/{id}
```
**Descripción:** Obtiene una ciudad específica

**Autorización:** Público

**Parámetros de ruta:**
- `id` (Long): ID de la ciudad

**Respuesta exitosa (200):** Ver 1.4.1

---

#### 1.4.3 Obtener ciudad por nombre
```
GET /ciudades/nombre/{nombre}
URL Completa: http://localhost:8081/api/ciudades/nombre/{nombre}
```
**Descripción:** Busca ciudad por nombre

**Autorización:** Público

**Parámetros de ruta:**
- `nombre` (String): Nombre de la ciudad

**Respuesta exitosa (200):** Ver 1.4.1

---

### 1.5 Roles

#### 1.5.1 Listar todos los roles
```
GET /roles
URL Completa: http://localhost:8081/api/roles
```
**Descripción:** Obtiene lista de todos los roles del sistema

**Autorización:** Público

**Respuesta exitosa (200):**
```json
[
  {
    "idRol": 1,
    "nombreRol": "Cliente"
  },
  {
    "idRol": 2,
    "nombreRol": "Trabajador"
  },
  {
    "idRol": 3,
    "nombreRol": "Admin"
  }
]
```

---

#### 1.5.2 Obtener rol por ID
```
GET /roles/{id}
URL Completa: http://localhost:8081/api/roles/{id}
```
**Descripción:** Obtiene un rol específico

**Autorización:** Público

**Parámetros de ruta:**
- `id` (Long): ID del rol

**Respuesta exitosa (200):** Ver 1.5.1

---

#### 1.5.3 Obtener rol por nombre
```
GET /roles/nombre/{nombre}
URL Completa: http://localhost:8081/api/roles/nombre/{nombre}
```
**Descripción:** Busca rol por nombre

**Autorización:** Público

**Parámetros de ruta:**
- `nombre` (String): Nombre del rol

**Respuesta exitosa (200):** Ver 1.5.1

---

## 2. Microservicio Gestión Catálogo (Puerto 8084)

Base URL: `http://localhost:8084/api/catalogo`

### 2.1 Productos

#### 2.1.1 Obtener todos los productos disponibles
```
GET /productos
URL Completa: http://localhost:8084/api/catalogo/productos
```
**Descripción:** Obtiene lista de productos disponibles (disponible = true)

**Autorización:** Público

**Respuesta exitosa (200):**
```json
[
  {
    "id": 1,
    "nombre": "Hamburguesa Clásica",
    "descripcion": "Hamburguesa con carne, lechuga, tomate",
    "precio": 5990,
    "imagen": "https://...",
    "disponible": true,
    "idCategoria": 1
  }
]
```

---

#### 2.1.2 Obtener todos los productos (incluidos no disponibles)
```
GET /productos/todos
URL Completa: http://localhost:8084/api/catalogo/productos/todos
```
**Descripción:** Obtiene todos los productos sin filtrar por disponibilidad

**Autorización:** Público

**Respuesta exitosa (200):** Igual que 1.1.1

---

#### 2.1.3 Obtener producto por ID
```
GET /productos/{id}
URL Completa: http://localhost:8084/api/catalogo/productos/{id}
```
**Descripción:** Obtiene un producto específico por su ID

**Parámetros de ruta:**
- `id` (Long): ID del producto

**Autorización:** Público

**Respuesta exitosa (200):**
```json
{
  "id": 1,
  "nombre": "Hamburguesa Clásica",
  "descripcion": "Hamburguesa con carne, lechuga, tomate",
  "precio": 5990,
  "imagen": "https://...",
  "disponible": true,
  "idCategoria": 1
}
```

---

#### 2.1.4 Obtener productos por categoría
```
GET /productos/categoria/{idCategoria}
URL Completa: http://localhost:8084/api/catalogo/productos/categoria/{idCategoria}
```
**Descripción:** Obtiene todos los productos de una categoría específica

**Parámetros de ruta:**
- `idCategoria` (Long): ID de la categoría

**Autorización:** Público

**Respuesta exitosa (200):** Array de productos (ver 2.1.1)

---

#### 2.1.5 Buscar productos por nombre
```
GET /productos/buscar?nombre={nombre}
URL Completa: http://localhost:8084/api/catalogo/productos/buscar?nombre={nombre}
```
**Descripción:** Busca productos que contengan el nombre especificado

**Parámetros de consulta:**
- `nombre` (String): Nombre o parte del nombre a buscar

**Autorización:** Público

**Respuesta exitosa (200):** Array de productos (ver 2.1.1)

---

#### 2.1.6 Crear producto
```
POST /productos
URL Completa: http://localhost:8084/api/catalogo/productos
```
**Descripción:** Crea un nuevo producto

**Autorización:** ADMIN

**Body (JSON):**
```json
{
  "nombre": "Hamburguesa BBQ",
  "descripcion": "Hamburguesa con salsa BBQ",
  "precio": 6990,
  "disponible": true,
  "idCategoria": 1
}
```

**Respuesta exitosa (201):** Producto creado (ver 2.1.3)

---

#### 2.1.7 Actualizar producto
```
PUT /productos/{id}
URL Completa: http://localhost:8084/api/catalogo/productos/{id}
```
**Descripción:** Actualiza un producto existente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del producto

**Body (JSON):** Ver 1.1.6

**Respuesta exitosa (200):** Producto actualizado

---

#### 2.1.8 Eliminar producto
```
DELETE /productos/{id}
URL Completa: http://localhost:8084/api/catalogo/productos/{id}
```
**Descripción:** Elimina un producto

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del producto

**Respuesta exitosa (204):** Sin contenido

---

#### 2.1.9 Cambiar disponibilidad del producto
```
PATCH /productos/{id}/disponibilidad?disponible={true|false}
URL Completa: http://localhost:8084/api/catalogo/productos/{id}/disponibilidad?disponible=true
```
**Descripción:** Cambia el estado de disponibilidad de un producto

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del producto

**Parámetros de consulta:**
- `disponible` (Boolean): true o false

**Respuesta exitosa (200):** Sin contenido

---

#### 2.1.10 Subir imagen del producto
```
POST /productos/{id}/imagen
URL Completa: http://localhost:8084/api/catalogo/productos/{id}/imagen
Content-Type: multipart/form-data
```
**Descripción:** Sube una imagen a Firebase Storage y la asocia al producto

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID del producto

**Form Data:**
- `imagen` (File): Archivo de imagen (jpg, png, webp, etc.)

**Respuesta exitosa (200):**
```json
{
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "mensaje": "imagen subida a firebase y guardada en base de datos"
}
```

---

### 1.2 Categorías

#### 2.2.1 Obtener todas las categorías
```
GET /categorias
URL Completa: http://localhost:8084/api/catalogo/categorias
```
**Descripción:** Obtiene lista de todas las categorías

**Autorización:** Público

**Respuesta exitosa (200):**
```json
[
  {
    "id": 1,
    "nombre": "Hamburguesas",
    "descripcion": "Hamburguesas artesanales"
  }
]
```

---

#### 2.2.2 Obtener categoría por ID
```
GET /categorias/{id}
URL Completa: http://localhost:8084/api/catalogo/categorias/{id}
```
**Descripción:** Obtiene una categoría específica

**Autorización:** Público

**Parámetros de ruta:**
- `id` (Long): ID de la categoría

**Respuesta exitosa (200):** Ver 1.2.1

---

#### 2.2.3 Crear categoría
```
POST /categorias
URL Completa: http://localhost:8084/api/catalogo/categorias
```
**Descripción:** Crea una nueva categoría

**Autorización:** ADMIN

**Body (JSON):**
```json
{
  "nombre": "Bebidas",
  "descripcion": "Bebidas frías y calientes"
}
```

**Respuesta exitosa (201):** Categoría creada

---

#### 2.2.4 Actualizar categoría
```
PUT /categorias/{id}
URL Completa: http://localhost:8084/api/catalogo/categorias/{id}
```
**Descripción:** Actualiza una categoría existente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la categoría

**Body (JSON):** Ver 1.2.3

**Respuesta exitosa (200):** Categoría actualizada

---

#### 2.2.5 Eliminar categoría
```
DELETE /categorias/{id}
URL Completa: http://localhost:8084/api/catalogo/categorias/{id}
```
**Descripción:** Elimina una categoría

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID de la categoría

**Respuesta exitosa (204):** Sin contenido

---

#### 2.2.6 Health Check
```
GET /health
URL Completa: http://localhost:8084/api/catalogo/health
```
**Descripción:** Verifica que el servicio esté funcionando

**Autorización:** Público

**Respuesta exitosa (200):**
```
"Servicio de Catálogo funcionando correctamente"
```

---

## 3. Microservicio Gestión Pedidos (Puerto 8083)

Base URL: `http://localhost:8083/api`

### 3.1 Pedidos

#### 3.1.1 Listar todos los pedidos
```
GET /pedidos
URL Completa: http://localhost:8083/api/pedidos
```
**Descripción:** Obtiene lista de todos los pedidos

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
[
  {
    "idPedido": 1,
    "idCliente": 1,
    "idEstadoPedido": 1,
    "idMetodoPago": 1,
    "idTipoEntrega": 1,
    "idDireccionEntrega": 1,
    "montoSubtotal": 15990,
    "montoEnvio": 2000,
    "montoTotal": 17990,
    "fechaPedido": "2025-01-15T10:30:00",
    "notaCliente": "Sin cebolla"
  }
]
```

**Respuesta sin contenido (204):** No hay pedidos registrados

---

#### 3.1.2 Obtener pedido por ID
```
GET /pedidos/{id}
URL Completa: http://localhost:8083/api/pedidos/{id}
```
**Descripción:** Obtiene un pedido específico

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Parámetros de ruta:**
- `id` (Long): ID del pedido

**Respuesta exitosa (200):** Ver 2.1.1

**Respuesta no encontrado (404):** Pedido no existe

---

#### 3.1.3 Listar pedidos por cliente
```
GET /pedidos/cliente/{idCliente}
URL Completa: http://localhost:8083/api/pedidos/cliente/{idCliente}
```
**Descripción:** Obtiene todos los pedidos de un cliente

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Parámetros de ruta:**
- `idCliente` (Long): ID del cliente

**Respuesta exitosa (200):** Array de pedidos (ver 3.1.1)

**Respuesta sin contenido (204):** Cliente no tiene pedidos

---

#### 3.1.4 Crear pedido completo
```
POST /pedidos/completo
URL Completa: http://localhost:8083/api/pedidos/completo
```
**Descripción:** Crea un nuevo pedido con sus detalles

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Body (JSON):**
```json
{
  "idCliente": 1,
  "idEstadoPedido": 1,
  "idMetodoPago": 1,
  "idTipoEntrega": 1,
  "idDireccionEntrega": 1,
  "montoSubtotal": 15990,
  "montoEnvio": 2000,
  "montoTotal": 17990,
  "fechaHoraPedido": "2025-01-15T10:30:00",
  "notasCliente": "Sin cebolla",
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2,
      "precioUnitario": 5990,
      "subtotalLinea": 11980
    },
    {
      "idProducto": 3,
      "cantidad": 1,
      "precioUnitario": 4010,
      "subtotalLinea": 4010
    }
  ]
}
```

**Respuesta exitosa (201):** Pedido creado completo con detalles

---

#### 3.1.5 Cancelar/Eliminar pedido
```
DELETE /pedidos/{id}
URL Completa: http://localhost:8083/api/pedidos/{id}
```
**Descripción:** Elimina un pedido

**Autorización:** ADMIN, CLIENTE

**Parámetros de ruta:**
- `id` (Long): ID del pedido

**Respuesta exitosa (204):** Pedido eliminado

**Respuesta no encontrado (404):** Pedido no existe

---

#### 3.1.6 Cambiar estado del pedido
```
PUT /pedidos/cambiar-estado/{idPedido}/estado/{idEstado}
URL Completa: http://localhost:8083/api/pedidos/cambiar-estado/{idPedido}/estado/{idEstado}
```
**Descripción:** Cambia el estado de un pedido (Pendiente de Pago, Recibido, En preparación, En camino, Entregado, Cancelado)

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Parámetros de ruta:**
- `idPedido` (Long): ID del pedido
- `idEstado` (Long): ID del nuevo estado

**Estados disponibles:**
- 1: Pendiente de Pago
- 2: Pagado/Recibido
- 3: En preparación
- 4: En camino
- 5: Entregado
- 6: Cancelado

**Respuesta exitosa (200):** Pedido con estado actualizado

---

#### 3.1.7 Procesar pedido pagado (Genera venta automática)
```
PUT /pedidos/procesar/{idPedido}
URL Completa: http://localhost:8083/api/pedidos/procesar/{idPedido}
```
**Descripción:** Actualiza pedido a PAGADO y genera venta con boleta automáticamente

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Headers requeridos:**
- `X-Internal-Token` (String): Token interno para comunicación entre microservicios

**Parámetros de ruta:**
- `idPedido` (Long): ID del pedido

**Respuesta exitosa (200):** Pedido actualizado a pagado y venta creada

---

#### 3.1.8 Obtener detalles de pedidos por cliente
```
GET /pedidos/detalles/cliente/{idCliente}
URL Completa: http://localhost:8083/api/pedidos/detalles/cliente/{idCliente}
```
**Descripción:** Obtiene detalles de productos de pedidos de un cliente

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Parámetros de ruta:**
- `idCliente` (Long): ID del cliente

**Respuesta exitosa (200):**
```json
[
  {
    "idDetalle": 1,
    "idPedido": 1,
    "idProducto": 1,
    "cantidad": 2,
    "precioUnitario": 5990,
    "subtotalLinea": 11980
  }
]
```

**Respuesta sin contenido (204):** No hay detalles

---

### 3.2 Pagos

#### 3.2.1 Crear preferencia de pago
```
POST /pagos/crear-preferencia
URL Completa: http://localhost:8083/api/pagos/crear-preferencia
```
**Descripción:** Crea una preferencia de pago en Mercado Pago y retorna URL de pago

**Autorización:** Requiere Bearer Token

**Body (JSON):**
```json
{
  "idPedido": 1,
  "montoPago": 17990,
  "descripcion": "Pago pedido #1 - Golden Burgers",
  "email": "cliente@email.com"
}
```

**Respuesta exitosa (201):**
```json
{
  "idPago": 1,
  "idPedido": 1,
  "montoPago": 17990,
  "estadoPago": "PENDIENTE",
  "idPreferenciaMpos": "1234567890",
  "urlPago": "https://www.mercadopago.com/mla/checkout/start?pref_id=..."
}
```

---

#### 3.2.2 Listar todos los pagos
```
GET /pagos
URL Completa: http://localhost:8083/api/pagos
```
**Descripción:** Obtiene lista de todos los pagos

**Autorización:** Requiere Bearer Token

**Respuesta exitosa (200):**
```json
[
  {
    "idPago": 1,
    "idPedido": 1,
    "montoPago": 17990,
    "estadoPago": "PAGADO",
    "idPreferenciaMpos": "1234567890",
    "idPagoMpos": "9876543210",
    "respuestaMercadoPago": "{...}",
    "fechaPago": "2025-01-15T10:35:00"
  }
]
```

---

#### 3.2.3 Obtener pago por ID
```
GET /pagos/{id}
URL Completa: http://localhost:8083/api/pagos/{id}
```
**Descripción:** Obtiene un pago específico

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `id` (Long): ID del pago

**Respuesta exitosa (200):** Ver 2.2.2

**Respuesta no encontrado (404):** Pago no existe

---

#### 3.2.4 Obtener pagos por pedido
```
GET /pagos/pedido/{idPedido}
URL Completa: http://localhost:8083/api/pagos/pedido/{idPedido}
```
**Descripción:** Obtiene todos los pagos de un pedido

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `idPedido` (Long): ID del pedido

**Respuesta exitosa (200):** Array de pagos (ver 3.2.2)

---

#### 3.2.5 Actualizar estado del pago
```
PUT /pagos/{id}/estado?estado={estado}&idPagoMpos={idPagoMpos}&respuestaMp={respuesta}
URL Completa: http://localhost:8083/api/pagos/{id}/estado
```
**Descripción:** Actualiza el estado de un pago

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `id` (Long): ID del pago

**Parámetros de consulta:**
- `estado` (String): PENDIENTE, PAGADO, RECHAZADO, CANCELADO
- `idPagoMpos` (String, opcional): ID del pago en Mercado Pago
- `respuestaMp` (String, opcional): Respuesta JSON de Mercado Pago

**Respuesta exitosa (200):** Pago actualizado

---

#### 3.2.6 Cancelar pago
```
DELETE /pagos/{id}
URL Completa: http://localhost:8083/api/pagos/{id}
```
**Descripción:** Cancela un pago (cambia estado a CANCELADO)

**Autorización:** Requiere Bearer Token

**Parámetros de ruta:**
- `id` (Long): ID del pago

**Respuesta exitosa (200):**
```json
{
  "mensaje": "Pago cancelado exitosamente"
}
```

---

#### 3.2.7 Webhook Mercado Pago
```
POST /pagos/webhook?type={type}&id={id}&data.id={dataId}
URL Completa: http://localhost:8083/api/pagos/webhook
```
**Descripción:** Recibe notificaciones de Mercado Pago sobre cambios en pagos (Endpoint público sin autenticación)

**Autorización:** Público (para Mercado Pago)

**Parámetros de consulta:**
- `type` (String, opcional): Tipo de notificación
- `id` (Long, opcional): ID del recurso
- `data.id` (String, opcional): ID del pago en Mercado Pago

**Respuesta exitosa (200):** Sin contenido

---

## 4. Microservicio Gestión Ventas (Puerto 8082)

Base URL: `http://localhost:8082/api`

### 4.1 Ventas

#### 4.1.1 Listar todas las ventas
```
GET /ventas
URL Completa: http://localhost:8082/api/ventas
```
**Descripción:** Obtiene lista de todas las ventas

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
[
  {
    "id_venta": 1,
    "id_pedido": 1,
    "total_venta": 17990,
    "fecha_venta": "2025-01-15T10:35:00"
  }
]
```

**Respuesta sin contenido (204):** No hay ventas

---

#### 4.1.2 Crear venta desde pedido
```
POST /ventas/desde-pedido/{id}
URL Completa: http://localhost:8082/api/ventas/desde-pedido/{id}
```
**Descripción:** Crea una venta a partir de un pedido y genera boleta automáticamente

**Autorización:** ADMIN, TRABAJADOR, CLIENTE

**Headers requeridos:**
- `X-Internal-Token` (String): Token interno para comunicación entre microservicios

**Parámetros de ruta:**
- `id` (Long): ID del pedido

**Respuesta exitosa (201):** Venta creada con boleta

**Respuesta error (400):**
```json
{
  "error": "Mensaje de error específico"
}
```

---

#### 4.1.3 Buscar venta por ID
```
GET /ventas/{id}
URL Completa: http://localhost:8082/api/ventas/{id}
```
**Descripción:** Obtiene una venta específica

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la venta

**Respuesta exitosa (200):** Ver 3.1.1

**Respuesta no encontrado (404):** Venta no existe

---

#### 4.1.4 Actualizar venta
```
PUT /ventas/{id}
URL Completa: http://localhost:8082/api/ventas/{id}
```
**Descripción:** Actualiza una venta existente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la venta

**Body (JSON):**
```json
{
  "id_pedido": 1,
  "total_venta": 17990,
  "fecha_venta": "2025-01-15T10:35:00"
}
```

**Respuesta exitosa (200):** Venta actualizada

**Respuesta no encontrado (404):** Venta no existe

---

#### 4.1.5 Eliminar venta
```
DELETE /ventas/{id}
URL Completa: http://localhost:8082/api/ventas/{id}
```
**Descripción:** Elimina una venta

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID de la venta

**Respuesta exitosa (204):** Venta eliminada

**Respuesta no encontrado (404):** Venta no existe

---

### 4.2 Boletas

#### 4.2.1 Listar todas las boletas
```
GET /boletas
URL Completa: http://localhost:8082/api/boletas
```
**Descripción:** Obtiene lista de todas las boletas

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
[
  {
    "id_boleta": 1,
    "id_venta": 1,
    "numero_sii": "12345678",
    "url_documento": "https://...",
    "iva": 2873,
    "fecha_emision": "2025-01-15T10:35:00"
  }
]
```

**Respuesta sin contenido (204):** No hay boletas

---

#### 4.2.2 Obtener boleta por ID
```
GET /boletas/{id}
URL Completa: http://localhost:8082/api/boletas/{id}
```
**Descripción:** Obtiene una boleta específica

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la boleta

**Respuesta exitosa (200):** Ver 3.2.1

**Respuesta no encontrado (404):** Boleta no existe

---

#### 4.2.3 Crear boleta
```
POST /boletas
URL Completa: http://localhost:8082/api/boletas
```
**Descripción:** Crea una nueva boleta

**Autorización:** ADMIN, TRABAJADOR

**Body (JSON):**
```json
{
  "id_venta": 1,
  "numero_sii": "12345678",
  "url_documento": "https://...",
  "iva": 2873
}
```

**Respuesta exitosa (201):** Boleta creada

**Respuesta error (400):**
```json
{
  "error": "Error al crear boleta: ..."
}
```

---

#### 4.2.4 Actualizar boleta
```
PUT /boletas/{id}
URL Completa: http://localhost:8082/api/boletas/{id}
```
**Descripción:** Actualiza una boleta existente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la boleta

**Body (JSON):** Ver 3.2.3

**Respuesta exitosa (200):** Boleta actualizada

**Respuesta no encontrado (404):** Boleta no existe

---

#### 4.2.5 Eliminar boleta
```
DELETE /boletas/{id}
URL Completa: http://localhost:8082/api/boletas/{id}
```
**Descripción:** Elimina una boleta

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID de la boleta

**Respuesta exitosa (204):** Boleta eliminada

**Respuesta no encontrado (404):**
```json
{
  "error": "Boleta no encontrada con ID: {id}"
}
```

---

### 4.3 Devoluciones

#### 4.3.1 Listar todas las devoluciones
```
GET /devoluciones
URL Completa: http://localhost:8082/api/devoluciones
```
**Descripción:** Obtiene lista de todas las devoluciones

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
[
  {
    "id_devolucion": 1,
    "id_venta": 1,
    "montoDevuelto": 17990,
    "motivo": "Producto defectuoso",
    "fechaDevolucion": "2025-01-16T14:00:00"
  }
]
```

**Respuesta sin contenido (204):** No hay devoluciones

---

#### 4.3.2 Obtener devolución por ID
```
GET /devoluciones/{id}
URL Completa: http://localhost:8082/api/devoluciones/{id}
```
**Descripción:** Obtiene una devolución específica

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la devolución

**Respuesta exitosa (200):** Ver 3.3.1

**Respuesta no encontrado (404):** Devolución no existe

---

#### 4.3.3 Crear devolución
```
POST /devoluciones
URL Completa: http://localhost:8082/api/devoluciones
```
**Descripción:** Registra una nueva devolución

**Autorización:** ADMIN, TRABAJADOR

**Body (JSON):**
```json
{
  "id_venta": 1,
  "montoDevuelto": 17990,
  "motivo": "Producto defectuoso",
  "fechaDevolucion": "2025-01-16T14:00:00"
}
```

**Respuesta exitosa (201):** Devolución creada

**Respuesta error (400):**
```json
{
  "error": "Error al crear devolución: ..."
}
```

---

#### 4.3.4 Actualizar devolución
```
PUT /devoluciones/{id}
URL Completa: http://localhost:8082/api/devoluciones/{id}
```
**Descripción:** Actualiza una devolución existente

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de ruta:**
- `id` (Long): ID de la devolución

**Body (JSON):** Ver 3.3.3

**Respuesta exitosa (200):** Devolución actualizada

**Respuesta no encontrado (404):** Devolución no existe

---

#### 4.3.5 Eliminar devolución
```
DELETE /devoluciones/{id}
URL Completa: http://localhost:8082/api/devoluciones/{id}
```
**Descripción:** Elimina una devolución

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID de la devolución

**Respuesta exitosa (204):** Devolución eliminada

**Respuesta no encontrado (404):**
```json
{
  "error": "Devolución no encontrada con ID: {id}"
}
```

---

### 4.4 Dashboard

#### 4.4.1 Obtener resumen completo de ventas
```
GET /dashboard/resumen-ventas
URL Completa: http://localhost:8082/api/dashboard/resumen-ventas
```
**Descripción:** Retorna resumen con ventas de hoy, mes actual y año actual

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
{
  "ventasHoy": {
    "totalVentas": 5,
    "montoTotal": 89950
  },
  "ventasMes": {
    "totalVentas": 42,
    "montoTotal": 755860
  },
  "ventasAnio": {
    "totalVentas": 156,
    "montoTotal": 2807340
  }
}
```

---

#### 4.4.2 Obtener ventas del día actual
```
GET /dashboard/ventas-hoy
URL Completa: http://localhost:8082/api/dashboard/ventas-hoy
```
**Descripción:** Retorna las ventas del día de hoy

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
{
  "totalVentas": 5,
  "montoTotal": 89950,
  "fecha": "2025-01-15"
}
```

---

#### 4.4.3 Obtener ventas del mes actual
```
GET /dashboard/ventas-mes-actual
URL Completa: http://localhost:8082/api/dashboard/ventas-mes-actual
```
**Descripción:** Retorna las ventas del mes en curso

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
{
  "totalVentas": 42,
  "montoTotal": 755860,
  "mes": 1,
  "anio": 2025
}
```

---

#### 4.4.4 Obtener ventas del año actual
```
GET /dashboard/ventas-anio-actual
URL Completa: http://localhost:8082/api/dashboard/ventas-anio-actual
```
**Descripción:** Retorna las ventas del año en curso

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
{
  "totalVentas": 156,
  "montoTotal": 2807340,
  "anio": 2025
}
```

---

#### 4.4.5 Obtener ventas por mes del año actual
```
GET /dashboard/ventas-por-mes
URL Completa: http://localhost:8082/api/dashboard/ventas-por-mes
```
**Descripción:** Retorna ventas agrupadas por mes (para gráficos estadísticos)

**Autorización:** ADMIN, TRABAJADOR

**Respuesta exitosa (200):**
```json
[
  {
    "mes": 1,
    "nombreMes": "Enero",
    "totalVentas": 42,
    "montoTotal": 755860
  },
  {
    "mes": 2,
    "nombreMes": "Febrero",
    "totalVentas": 38,
    "montoTotal": 684020
  }
]
```

---

#### 4.4.6 Obtener KPIs del dashboard
```
GET /dashboard/kpis?periodo={periodo}
URL Completa: http://localhost:8082/api/dashboard/kpis?periodo=mes
```
**Descripción:** Retorna indicadores clave de rendimiento según periodo

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de consulta:**
- `periodo` (String): 'hoy', 'mes', 'anio'

**Respuesta exitosa (200):**
```json
{
  "totalVentas": 42,
  "montoTotal": 755860,
  "promedioVenta": 17996,
  "ventaMasAlta": 45000,
  "ventaMasBaja": 8990
}
```

---

#### 4.4.7 Obtener ventas por categoría
```
GET /dashboard/ventas-categoria?periodo={periodo}
URL Completa: http://localhost:8082/api/dashboard/ventas-categoria?periodo=mes
```
**Descripción:** Retorna ventas agrupadas por categoría de producto

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de consulta:**
- `periodo` (String): 'hoy', 'mes', 'anio'

**Respuesta exitosa (200):**
```json
[
  {
    "categoria": "Hamburguesas",
    "totalVentas": 28,
    "montoTotal": 503440
  },
  {
    "categoria": "Bebidas",
    "totalVentas": 35,
    "montoTotal": 140350
  }
]
```

---

#### 4.4.8 Obtener ventas por ciudad
```
GET /dashboard/ventas-ciudad?periodo={periodo}
URL Completa: http://localhost:8082/api/dashboard/ventas-ciudad?periodo=mes
```
**Descripción:** Retorna ventas agrupadas por ciudad

**Autorización:** ADMIN, TRABAJADOR

**Parámetros de consulta:**
- `periodo` (String): 'hoy', 'mes', 'anio'

**Respuesta exitosa (200):**
```json
[
  {
    "ciudad": "Santiago",
    "totalVentas": 28,
    "montoTotal": 503440
  },
  {
    "ciudad": "Valparaíso",
    "totalVentas": 14,
    "montoTotal": 252420
  }
]
```

---

## Notas Importantes

### Autenticación
- La mayoría de los endpoints requieren autenticación mediante JWT Bearer Token
- El token debe enviarse en el header: `Authorization: Bearer {token}`
- Algunos endpoints son públicos (marcados como "Público")
- La comunicación entre microservicios usa un token interno: `X-Internal-Token`

### Roles de Usuario
- **ADMIN**: Acceso completo a todos los endpoints
- **TRABAJADOR**: Acceso a gestión de productos, pedidos, ventas y dashboard
- **CLIENTE**: Acceso limitado a sus propios pedidos y creación de nuevos pedidos

### Códigos de Estado HTTP
- **200 OK**: Operación exitosa
- **201 Created**: Recurso creado exitosamente
- **204 No Content**: Operación exitosa sin contenido en respuesta
- **400 Bad Request**: Error en los datos enviados
- **404 Not Found**: Recurso no encontrado
- **401 Unauthorized**: No autenticado
- **403 Forbidden**: No autorizado (sin permisos)

### CORS
Todos los microservicios tienen CORS habilitado con `@CrossOrigin(origins = "*")` (configurar según necesidades de producción)

### Swagger/OpenAPI
Cada microservicio tiene documentación Swagger disponible en:
- Gestión Usuario: http://localhost:8081/swagger-ui.html
- Gestión Ventas: http://localhost:8082/swagger-ui.html
- Gestión Pedidos: http://localhost:8083/swagger-ui.html
- Gestión Catálogo: http://localhost:8084/swagger-ui.html
- Gestión Contacto: http://localhost:8085/swagger-ui.html

## 5. Microservicio Gestión Contacto (Puerto 8085)

Base URL: `http://localhost:8085/api`

### 5.1 Mensajes de Contacto

#### 5.1.1 Enviar mensaje de contacto
```
POST /mensajes
URL Completa: http://localhost:8085/api/mensajes
```
**Descripción:** Guarda un nuevo mensaje enviado desde el formulario público de contacto

**Autorización:** Público (no requiere autenticación)

**Body (JSON):**
```json
{
  "nombreCliente": "Juan Pérez",
  "email": "juan@email.com",
  "telefono": "912345678",
  "mensaje": "Me gustaría obtener más información sobre...",
  "estado": 0
}
```

**Respuesta exitosa (200):**
```json
{
  "idMensaje": 1,
  "nombreCliente": "Juan Pérez",
  "email": "juan@email.com",
  "telefono": "912345678",
  "mensaje": "Me gustaría obtener más información sobre...",
  "estado": 0,
  "fechaCreacion": "2025-01-15T14:30:00"
}
```

**Estados de mensaje:**
- 0: No leído
- 1: Leído
- 2: Respondido

---

#### 5.1.2 Listar todos los mensajes
```
GET /mensajes
URL Completa: http://localhost:8085/api/mensajes
```
**Descripción:** Obtiene lista de todos los mensajes de contacto

**Autorización:** ADMIN

**Respuesta exitosa (200):** Array de mensajes (ver 5.1.1)

---

#### 5.1.3 Obtener mensaje por ID
```
GET /mensajes/{id}
URL Completa: http://localhost:8085/api/mensajes/{id}
```
**Descripción:** Obtiene un mensaje específico por ID

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del mensaje

**Respuesta exitosa (200):** Ver 5.1.1

---

#### 5.1.4 Listar mensajes por estado
```
GET /mensajes/estado/{estado}
URL Completa: http://localhost:8085/api/mensajes/estado/{estado}
```
**Descripción:** Filtra mensajes por estado (0=No leído, 1=Leído, 2=Respondido)

**Autorización:** ADMIN

**Parámetros de ruta:**
- `estado` (Integer): Estado del mensaje (0, 1 o 2)

**Respuesta exitosa (200):** Array de mensajes filtrados

---

#### 5.1.5 Actualizar mensaje completo
```
PUT /mensajes/{id}
URL Completa: http://localhost:8085/api/mensajes/{id}
```
**Descripción:** Actualiza un mensaje existente

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del mensaje

**Body (JSON):** Ver 5.1.1

**Respuesta exitosa (200):** Mensaje actualizado

---

#### 5.1.6 Actualizar solo estado del mensaje
```
PUT /mensajes/{id}/estado/{estado}
URL Completa: http://localhost:8085/api/mensajes/{id}/estado/{estado}
```
**Descripción:** Actualiza solo el estado del mensaje

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del mensaje
- `estado` (Integer): Nuevo estado (0, 1 o 2)

**Respuesta exitosa (200):** Mensaje con estado actualizado

---

#### 5.1.7 Eliminar mensaje
```
DELETE /mensajes/{id}
URL Completa: http://localhost:8085/api/mensajes/{id}
```
**Descripción:** Elimina un mensaje de contacto

**Autorización:** ADMIN

**Parámetros de ruta:**
- `id` (Long): ID del mensaje

**Respuesta exitosa (204):** Mensaje eliminado

---

#### 5.1.8 Test de conexión
```
GET /testConexion
URL Completa: http://localhost:8085/api/testConexion
```
**Descripción:** Verifica la conexión a la base de datos

**Autorización:** Público

**Respuesta exitosa (200):**
```
"Conexión exitosa a la base de datos: Oracle Database 19c (Usuario: GOLDENBURGERSDB, URL: ...)"
```

---

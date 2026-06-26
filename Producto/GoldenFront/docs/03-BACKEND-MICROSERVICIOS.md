# Arquitectura Backend - Golden Burgers

## Resumen General

La arquitectura del backend de Golden Burgers está basada en microservicios independientes que se comunican a través de un API Gateway centralizado. Todos los servicios están implementados en Java con Spring Boot.

---

## Tabla de Contenidos

1. [Arquitectura General](#arquitectura-general)
2. [API Gateway](#api-gateway)
3. [Microservicio: GESTIONUSUARIO](#microservicio-gestionusuario)
4. [Microservicio: GESTIONCATALOGO](#microservicio-gestioncatalogo)
5. [Microservicio: GESTIONPEDIDO](#microservicio-gestionpedido)
6. [Microservicio: GESTIONVENTA](#microservicio-gestionventa)
7. [Microservicio: GESTIONCONTACTO](#microservicio-gestioncontacto)
8. [Base de Datos Oracle](#base-de-datos-oracle)
9. [Comunicación Entre Microservicios](#comunicación-entre-microservicios)

---

## Arquitectura General

```
┌──────────────┐
│   CLIENTE    │
│  (Browser)   │
└──────┬───────┘
       │
       │ HTTP/REST
       ▼
┌─────────────────────────────────────────┐
│          API GATEWAY                    │
│         (Puerto 8080)                   │
│                                         │
│  - Autenticación JWT                    │
│  - Proxy HTTP                           │
│  - Validación de tokens                 │
│  - Gestión de CORS                      │
└────┬────┬────┬────┬────┬────────────────┘
     │    │    │    │    │
     │    │    │    │    │
     │    │    │    │    └──────────┐
     │    │    │    │               │
     ▼    ▼    ▼    ▼               ▼
┌────────┐ ┌────────┐ ┌─────────┐ ┌──────────┐ ┌─────────┐
│GESTIÓN │ │GESTIÓN │ │GESTIÓN  │ │ GESTIÓN  │ │GESTIÓN  │
│USUARIO │ │CATÁLOGO│ │ PEDIDO  │ │  VENTA   │ │CONTACTO │
│(8081)  │ │(8082)  │ │ (8083)  │ │  (8084)  │ │ (8085)  │
└────┬───┘ └────┬───┘ └────┬────┘ └────┬─────┘ └────┬────┘
     │          │           │           │            │
     └──────────┴───────────┴───────────┴────────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │  ORACLE DATABASE    │
              │                     │
              │  - Usuarios         │
              │  - Clientes         │
              │  - Productos        │
              │  - Pedidos          │
              │  - Ventas           │
              │  - Mensajes         │
              └─────────────────────┘
```

---

## API Gateway

**Puerto:** 8080
**Ruta Base:** `/api`

### Responsabilidades

1. **Punto de entrada único** para todas las peticiones del frontend
2. **Autenticación** con Firebase y generación de tokens JWT internos
3. **Validación de tokens JWT** en todas las peticiones subsecuentes
4. **Proxy HTTP** que redirige peticiones a los microservicios correspondientes
5. **Gestión de CORS** para permitir peticiones desde el frontend

---

### Componentes Principales

#### AuthController
**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/controller/AuthController.java`

**Endpoints:**
- `POST /api/auth/login` - Autenticación con Firebase
- `POST /api/auth/refresh` - Refrescar token JWT
- `GET /api/auth/health` - Health check

#### ProxyController
**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/controller/ProxyController.java`

**Rutas Proxiadas:**

| Ruta Frontend | Microservicio Destino | Puerto |
|---------------|----------------------|--------|
| `/api/usuarios/**` | GESTIONUSUARIO | 8081 |
| `/api/clientes/**` | GESTIONUSUARIO | 8081 |
| `/api/trabajadores/**` | GESTIONUSUARIO | 8081 |
| `/api/roles/**` | GESTIONUSUARIO | 8081 |
| `/api/ciudades/**` | GESTIONUSUARIO | 8081 |
| `/api/catalogo/**` | GESTIONCATALOGO | 8082 |
| `/api/mensajes/**` | GESTIONCONTACTO | 8085 |
| `/api/pagos/**` | GESTIONPEDIDO | 8083 |
| `/api/pedidos/**` | GESTIONPEDIDO | 8083 |
| `/api/boletas/**` | GESTIONVENTA | 8084 |
| `/api/dashboard/**` | GESTIONVENTA | 8084 |
| `/api/devoluciones/**` | GESTIONVENTA | 8084 |
| `/api/ventas/**` | GESTIONVENTA | 8084 |

#### JwtAuthenticationFilter
**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/filter/JwtAuthenticationFilter.java`

**Función:** Intercepta todas las peticiones HTTP y valida el token JWT

**Rutas Públicas (Sin autenticación):**
```java
// Sistema
/api/auth/**
/actuator/**
/health
/swagger-ui/**

// GESTIÓN USUARIO
GET /api/usuarios/firebase/{uid}
POST /api/clientes
GET /api/roles
GET /api/ciudades

// GESTIÓN CATÁLOGO
GET /api/catalogo/**

// GESTIÓN CONTACTO
POST /api/mensajes

// GESTIÓN PEDIDO
POST /api/pagos/webhook
```

#### JwtService
**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/service/JwtService.java`

**Funciones:**
- `generateToken()` - Generar token JWT con claims
- `extractUid()` - Extraer Firebase UID del token
- `extractEmail()` - Extraer email del token
- `extractRolId()` - Extraer ID del rol
- `extractRolNombre()` - Extraer nombre del rol
- `isTokenValid()` - Validar token

#### AuthenticationService
**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/service/AuthenticationService.java`

**Funciones:**
- `authenticateWithFirebase()` - Autenticar con token de Firebase
- `getUserInfoFromMicroservice()` - Consultar rol del usuario en GESTIONUSUARIO
- `refreshToken()` - Refrescar token JWT

---

### DTOs del API Gateway

#### LoginRequest
```java
public class LoginRequest {
    private String firebaseToken;
}
```

#### LoginResponse
```java
public class LoginResponse {
    private String internalToken;
    private UserDTO user;
    private long expiresIn;
}
```

#### UserDTO
```java
public class UserDTO {
    private String uid;
    private String email;
    private String nombre;
    private Integer rolId;
    private String rolNombre;
}
```

---

## Microservicio: GESTIONUSUARIO

**Puerto:** 8081
**Ruta Base:** `/api`
**Paquete:** `com.goldenburgers.gestionUsuario`

### Responsabilidades

1. Gestión de usuarios del sistema
2. Gestión de clientes y sus datos personales
3. Gestión de direcciones de entrega de clientes
4. Gestión de trabajadores
5. Gestión de roles y ciudades

---

### Modelos (Entidades JPA)

#### Usuario
**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/model/Usuario.java`

**Tabla:** `USUARIO`

```java
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @Column(name = "ID_USUARIO", length = 255)
    private String idUsuario; // Firebase UID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROL", nullable = false)
    private Rol rol;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDate fechaCreacion;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Cliente cliente;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Trabajador trabajador;
}
```

#### Cliente
**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/model/Cliente.java`

**Tabla:** `CLIENTE`

```java
@Entity
@Table(name = "CLIENTE")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long idCliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "NOMBRE_CLIENTE", nullable = false, length = 255)
    private String nombreCliente;

    @Column(name = "TELEFONO_CLIENTE", length = 20)
    private String telefonoCliente;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<DireccionCliente> direcciones = new ArrayList<>();
}
```

#### DireccionCliente
**Tabla:** `DIRECCION_CLIENTE`

```java
@Entity
@Table(name = "DIRECCION_CLIENTE")
public class DireccionCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDireccion;

    @ManyToOne
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "ID_CIUDAD", nullable = false)
    private Ciudad ciudad;

    @Column(name = "DIRECCION", nullable = false, length = 500)
    private String direccion;

    @Column(name = "ALIAS", length = 50)
    private String alias;
}
```

#### Trabajador
**Tabla:** `TRABAJADOR`

```java
@Entity
@Table(name = "TRABAJADOR")
public class Trabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTrabajador;

    @OneToOne
    @JoinColumn(name = "ID_USUARIO", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "NOMBRE_TRABAJADOR", nullable = false)
    private String nombreTrabajador;

    @Column(name = "RUT_TRABAJADOR", nullable = false, unique = true)
    private String rutTrabajador;
}
```

#### Rol
**Tabla:** `ROL`

```java
@Entity
@Table(name = "ROL")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROL")
    private Long idRol;

    @Column(name = "NOMBRE_ROL", nullable = false, unique = true)
    private String nombreRol; // "Cliente", "Trabajador", "Admin"
}
```

#### Ciudad
**Tabla:** `CIUDAD`

```java
@Entity
@Table(name = "CIUDAD")
public class Ciudad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCiudad;

    @Column(name = "NOMBRE_CIUDAD", nullable = false)
    private String nombreCiudad;
}
```

---

### DTOs de GESTIONUSUARIO

#### RegistrarCliente
```java
public class RegistrarCliente {
    @NotBlank
    private String idUsuario; // Firebase UID

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nombreCliente;

    @Pattern(regexp = "\\d{9}")
    private String telefonoCliente;
}
```

#### ClienteDTO
```java
public class ClienteDTO {
    private Long idCliente;
    private String idUsuario;
    private String email;
    private String nombreCliente;
    private String telefonoCliente;
    private Integer rolId;
    private String rolNombre;
}
```

#### ActualizarPerfilCliente
```java
public class ActualizarPerfilCliente {
    @NotBlank
    private String nombreCliente;

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "\\d{9}")
    private String telefonoCliente;
}
```

#### CrearDireccionCliente
```java
public class CrearDireccionCliente {
    @NotNull
    private Long idCliente;

    @NotNull
    private Long idCiudad;

    @NotBlank
    private String direccion;

    private String alias;
}
```

#### DireccionClienteDTO
```java
public class DireccionClienteDTO {
    private Long idDireccion;
    private Long idCliente;
    private Long idCiudad;
    private String nombreCiudad;
    private String direccion;
    private String alias;
}
```

#### RegistrarTrabajador
```java
public class RegistrarTrabajador {
    @NotBlank
    private String idUsuario; // Firebase UID

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nombreTrabajador;

    @NotBlank
    private String rutTrabajador;
}
```

#### TrabajadorDTO
```java
public class TrabajadorDTO {
    private Long idTrabajador;
    private String idUsuario;
    private String email;
    private String nombreTrabajador;
    private String rutTrabajador;
    private Integer rolId;
    private String rolNombre;
}
```

---

### Controladores de GESTIONUSUARIO

#### ClienteController
**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/controller/ClienteController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| POST | `/api/clientes` | Registrar cliente | Cualquiera |
| POST | `/api/clientes/admin` | Registrar cliente (Admin) | ADMIN |
| GET | `/api/clientes` | Listar todos los clientes | ADMIN, TRABAJADOR |
| GET | `/api/clientes/{id}` | Obtener cliente por ID | Cualquiera |
| GET | `/api/clientes/usuario/{idUsuario}` | Obtener por Firebase UID | Cualquiera |
| GET | `/api/clientes/email/{email}` | Obtener por email | Cualquiera |
| PUT | `/api/clientes/{id}` | Actualizar cliente | ADMIN, TRABAJADOR |
| PUT | `/api/clientes/perfil` | Actualizar perfil propio | Cualquiera |
| PUT | `/api/clientes/{id}/email` | Actualizar email | ADMIN, TRABAJADOR |
| DELETE | `/api/clientes/{id}` | Eliminar cliente | ADMIN |
| POST | `/api/clientes/direcciones` | Crear dirección | Cualquiera |
| GET | `/api/clientes/{idCliente}/direcciones` | Listar direcciones | Cualquiera |
| PUT | `/api/clientes/direcciones/{id}` | Actualizar dirección | Cualquiera |
| DELETE | `/api/clientes/direcciones/{id}` | Eliminar dirección | Cualquiera |

#### UsuarioController
**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/controller/UsuarioController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/usuarios/firebase/{uid}` | Obtener usuario por Firebase UID | Público |
| GET | `/api/usuarios/exists/email/{email}` | Verificar si email existe | Público |
| GET | `/api/usuarios/exists/uid/{uid}` | Verificar si UID existe | Público |

#### TrabajadorController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| POST | `/api/trabajadores` | Registrar trabajador | ADMIN |
| GET | `/api/trabajadores` | Listar trabajadores | ADMIN, TRABAJADOR |
| GET | `/api/trabajadores/{id}` | Obtener por ID | ADMIN, TRABAJADOR |
| GET | `/api/trabajadores/usuario/{uid}` | Obtener por Firebase UID | ADMIN, TRABAJADOR |
| GET | `/api/trabajadores/rut/{rut}` | Obtener por RUT | ADMIN, TRABAJADOR |
| PUT | `/api/trabajadores/{id}` | Actualizar trabajador | ADMIN |
| PUT | `/api/trabajadores/{id}/email` | Actualizar email | ADMIN |
| PUT | `/api/trabajadores/{id}/rol` | Cambiar rol | ADMIN |
| DELETE | `/api/trabajadores/{id}` | Eliminar trabajador | ADMIN |

#### RolController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/roles` | Listar todos los roles | Público |
| GET | `/api/roles/{id}` | Obtener rol por ID | Público |
| GET | `/api/roles/nombre/{nombre}` | Obtener rol por nombre | Público |

#### CiudadController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/ciudades` | Listar todas las ciudades | Público |
| GET | `/api/ciudades/{id}` | Obtener ciudad por ID | Público |
| GET | `/api/ciudades/nombre/{nombre}` | Obtener ciudad por nombre | Público |

---

## Microservicio: GESTIONCATALOGO

**Puerto:** 8082
**Ruta Base:** `/api/catalogo`
**Paquete:** `com.goldenburgers.catalogo`

### Responsabilidades

1. Gestión del catálogo de productos
2. Gestión de categorías de productos
3. Subida de imágenes a Firebase Storage
4. Consultas públicas de productos disponibles

---

### Modelos de GESTIONCATALOGO

#### Producto
**Archivo:** `backGoldenBurgers/GESTIONCATALOGO/gestion-catalogo-main/src/main/java/com/goldenburgers/catalogo/model/Producto.java`

**Tabla:** `PRODUCTO`

```java
@Entity
@Table(name = "Producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

    @Column(name = "nombre_producto", nullable = false, length = 255)
    private String nombreProducto;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "imagen_url", length = 1000)
    private String imagenUrl;

    @Column(name = "disponible", nullable = false)
    private Integer disponible; // 1 = Sí, 0 = No
}
```

#### CategoriaProducto
**Tabla:** `CATEGORIA_PRODUCTO`

```java
@Entity
@Table(name = "CategoriaProducto")
public class CategoriaProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(name = "nombre_categoria", nullable = false)
    private String nombreCategoria;
}
```

---

### DTOs de GESTIONCATALOGO

#### ProductoDTO
```java
public class ProductoDTO {
    private Long idProducto;
    private Long idCategoria;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precioBase;
    private String imagen; // URL de Firebase Storage
    private Integer disponible;
}
```

#### CategoriaDTO
```java
public class CategoriaDTO {
    private Long idCategoria;
    private String nombreCategoria;
}
```

---

### Controlador de GESTIONCATALOGO

#### CatalogoController
**Archivo:** `backGoldenBurgers/GESTIONCATALOGO/gestion-catalogo-main/src/main/java/com/goldenburgers/catalogo/controller/CatalogoController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/catalogo/productos` | Obtener productos disponibles | Público |
| GET | `/api/catalogo/productos/todos` | Obtener todos los productos | Cualquiera |
| GET | `/api/catalogo/productos/{id}` | Obtener producto por ID | Público |
| GET | `/api/catalogo/productos/categoria/{id}` | Obtener por categoría | Público |
| GET | `/api/catalogo/productos/buscar` | Buscar por nombre | Público |
| POST | `/api/catalogo/productos` | Crear producto | ADMIN |
| PUT | `/api/catalogo/productos/{id}` | Actualizar producto | ADMIN, TRABAJADOR |
| DELETE | `/api/catalogo/productos/{id}` | Eliminar producto | ADMIN |
| POST | `/api/catalogo/productos/{id}/imagen` | Subir imagen | ADMIN, TRABAJADOR |
| PATCH | `/api/catalogo/productos/{id}/disponibilidad` | Cambiar disponibilidad | ADMIN, TRABAJADOR |
| GET | `/api/catalogo/categorias` | Listar categorías | Público |
| GET | `/api/catalogo/categorias/{id}` | Obtener categoría por ID | Público |
| POST | `/api/catalogo/categorias` | Crear categoría | ADMIN |
| PUT | `/api/catalogo/categorias/{id}` | Actualizar categoría | ADMIN, TRABAJADOR |
| DELETE | `/api/catalogo/categorias/{id}` | Eliminar categoría | ADMIN |

---

### Servicios de GESTIONCATALOGO

#### FirebaseStorageService
**Función:** Subir imágenes a Firebase Storage

```java
public String subirImagen(MultipartFile file, String carpeta) {
    // 1. Generar nombre único para el archivo
    // 2. Subir archivo a Firebase Storage
    // 3. Generar URL pública
    // 4. Retornar URL
}
```

---

## Microservicio: GESTIONPEDIDO

**Puerto:** 8083
**Ruta Base:** `/api`
**Paquete:** `com.example.GestionPedidos`

### Responsabilidades

1. Gestión de pedidos de clientes
2. Gestión de detalles de pedidos
3. Gestión de pagos con Mercado Pago
4. Estados de pedidos
5. Métodos de pago y tipos de entrega

---

### Modelos de GESTIONPEDIDO

#### Pedido
**Archivo:** `backGoldenBurgers/GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/model/Pedido.java`

**Tabla:** `PEDIDO`

```java
@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_estado_pedido", nullable = false)
    private Long idEstadoPedido;

    @Column(name = "id_metodo_pago", nullable = false)
    private Long idMetodoPago;

    @Column(name = "id_tipo_entrega", nullable = false)
    private Long idTipoEntrega;

    @Column(name = "id_direccion_cliente")
    private Long idDireccionEntrega;

    @Column(name = "monto_subtotal", nullable = false)
    private Double montoSubtotal;

    @Column(name = "monto_envio", nullable = false)
    private Double montoEnvio;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(name = "fecha_pedido")
    private Timestamp fechaPedido;

    @Column(name = "nota_cliente")
    private String notaCliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> detalles = new ArrayList<>();
}
```

#### DetallePedido
**Tabla:** `DETALLE_PEDIDO`

```java
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "subtotal_linea", nullable = false)
    private Double subtotalLinea;
}
```

#### Pago
**Tabla:** `PAGO`

```java
@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "monto_pago", nullable = false)
    private Double montoPago;

    @Column(name = "fecha_pago")
    private Timestamp fechaPago;

    @Column(name = "estado_pago", nullable = false)
    private Integer estadoPago; // 1=Pendiente, 2=Aprobado, 3=Rechazado

    @Column(name = "id_preferencia_mpos")
    private String idPreferenciaMpos;

    @Column(name = "id_pago_mpos")
    private String idPagoMpos;

    @Column(name = "respuesta_mercado_pago", length = 2000)
    private String respuestaMercadoPago;
}
```

#### EstadoPedido
**Tabla:** `ESTADO_PEDIDO`

```java
@Entity
@Table(name = "estado_pedido")
public class EstadoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoPedido;

    @Column(nullable = false)
    private String nombreEstado;
    // 1 = "Pendiente de Pago"
    // 2 = "Pagado"
    // 3 = "En Preparación"
    // 4 = "En Camino"
    // 5 = "Entregado"
    // 6 = "Cancelado"
}
```

#### MetodoPago
**Tabla:** `METODO_PAGO`

```java
@Entity
@Table(name = "metodo_pago")
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMetodoPago;

    @Column(nullable = false)
    private String nombreMetodo;
    // 1 = "Efectivo"
    // 2 = "Mercado Pago"
    // 3 = "Transferencia"
}
```

#### TipoEntrega
**Tabla:** `TIPO_ENTREGA`

```java
@Entity
@Table(name = "tipo_entrega")
public class TipoEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoEntrega;

    @Column(nullable = false)
    private String nombreTipo;
    // 1 = "Domicilio"
    // 2 = "Retiro en tienda"
}
```

---

### DTOs de GESTIONPEDIDO

#### PedidoDTO
```java
public class PedidoDTO {
    private Long idPedido;
    private Long idCliente;
    private Long idEstadoPedido;
    private Long idMetodoPago;
    private Long idTipoEntrega;
    private Long idDireccionEntrega;
    private Double montoSubtotal;
    private Double montoEnvio;
    private Double montoTotal;
    private Timestamp fechaHoraPedido;
    private String notasCliente;
    private List<DetallePedidoDTO> detalles;
}
```

#### DetallePedidoDTO
```java
public class DetallePedidoDTO {
    private Long idDetalle;
    private Long idPedido;
    private Long idProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotalLinea;
}
```

#### CrearPreferenciaRequest
```java
public class CrearPreferenciaRequest {
    @NotNull
    private Long idPedido;

    @NotNull
    private Double montoPago;

    @NotBlank
    private String descripcion;

    @NotBlank
    @Email
    private String emailPagador;

    @NotBlank
    private String nombrePagador;
}
```

---

### Controladores de GESTIONPEDIDO

#### PedidoController
**Archivo:** `backGoldenBurgers/GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/controller/PedidoController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/pedidos` | Listar todos los pedidos | ADMIN, TRABAJADOR |
| GET | `/api/pedidos/{id}` | Obtener pedido por ID | ADMIN, TRABAJADOR, CLIENTE |
| GET | `/api/pedidos/cliente/{idCliente}` | Obtener pedidos de cliente | ADMIN, TRABAJADOR, CLIENTE |
| POST | `/api/pedidos/completo` | Crear pedido completo | ADMIN, TRABAJADOR, CLIENTE |
| DELETE | `/api/pedidos/{id}` | Cancelar pedido | ADMIN, CLIENTE |
| PUT | `/api/pedidos/cambiar-estado/{idPedido}/estado/{idEstado}` | Cambiar estado | ADMIN, TRABAJADOR, CLIENTE |
| PUT | `/api/pedidos/procesar/{idPedido}` | Procesar pago y crear venta | ADMIN, TRABAJADOR, CLIENTE |
| GET | `/api/pedidos/detalles/cliente/{idCliente}` | Obtener detalles por cliente | ADMIN, TRABAJADOR, CLIENTE |

#### PagoController
**Archivo:** `backGoldenBurgers/GESTIONPEDIDO/GestionPedidos/src/main/java/com/example/GestionPedidos/controller/PagoController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| POST | `/api/pagos/crear-preferencia` | Crear preferencia Mercado Pago | ADMIN, TRABAJADOR, CLIENTE |
| GET | `/api/pagos` | Listar todos los pagos | ADMIN, TRABAJADOR |
| GET | `/api/pagos/{id}` | Obtener pago por ID | ADMIN, TRABAJADOR, CLIENTE |
| GET | `/api/pagos/pedido/{idPedido}` | Obtener pagos de pedido | ADMIN, TRABAJADOR, CLIENTE |
| PUT | `/api/pagos/{id}/estado` | Actualizar estado de pago | ADMIN, TRABAJADOR |
| DELETE | `/api/pagos/{id}` | Cancelar pago | ADMIN |
| POST | `/api/pagos/webhook` | Webhook de Mercado Pago | Público |

---

### Servicios de GESTIONPEDIDO

#### PagoService
**Función:** Integración con Mercado Pago

```java
public Pago crearPreferenciaPago(CrearPreferenciaRequest request) {
    // 1. Crear preferencia en Mercado Pago
    // 2. Guardar pago en BD con estado "Pendiente"
    // 3. Retornar Pago con URL de pago
}
```

#### VentaClienteService
**Función:** Comunicación con GESTIONVENTA

```java
public void crearVentaDesdePedido(Long idPedido, String token) {
    // 1. Obtener pedido
    // 2. Llamar a GESTIONVENTA para crear venta
    // 3. Pasar token JWT en header X-Internal-Token
}
```

---

## Microservicio: GESTIONVENTA

**Puerto:** 8084
**Ruta Base:** `/api`
**Paquete:** `com.example.Microservicio_Gestion_Venta`

### Responsabilidades

1. Gestión de ventas realizadas
2. Generación automática de ventas desde pedidos pagados
3. Gestión de boletas
4. Dashboard de administración (KPIs, estadísticas)
5. Gestión de devoluciones

---

### Modelos de GESTIONVENTA

#### Venta
**Archivo:** `backGoldenBurgers/GESTIONVENTA/Microservicio-Gestion-Venta/src/main/java/com/example/Microservicio_Gestion_Venta/model/Venta.java`

**Tabla:** `VENTA`

```java
@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_venta;

    @Column(name = "id_pedido", nullable = false)
    private Long id_pedido;

    @Column(name = "monto_total_venta", nullable = false)
    private Double total_venta;

    @Column(nullable = false)
    private Timestamp fecha_venta;
}
```

#### Boleta
**Tabla:** `BOLETA`

```java
@Entity
@Table(name = "boleta")
public class Boleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBoleta;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "numero_boleta", nullable = false, unique = true)
    private String numeroBoleta;

    @Column(name = "fecha_emision", nullable = false)
    private Timestamp fechaEmision;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(name = "rut_cliente")
    private String rutCliente;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;
}
```

#### Devolucion
**Tabla:** `DEVOLUCION`

```java
@Entity
@Table(name = "devolucion")
public class Devolucion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDevolucion;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "motivo_devolucion", nullable = false)
    private String motivoDevolucion;

    @Column(name = "monto_devolucion", nullable = false)
    private Double montoDevolucion;

    @Column(name = "fecha_devolucion", nullable = false)
    private Timestamp fechaDevolucion;
}
```

---

### Controladores de GESTIONVENTA

#### VentaController
**Archivo:** `backGoldenBurgers/GESTIONVENTA/Microservicio-Gestion-Venta/src/main/java/com/example/Microservicio_Gestion_Venta/controller/VentaController.java`

**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/ventas` | Listar todas las ventas | ADMIN, TRABAJADOR |
| GET | `/api/ventas/{id}` | Obtener venta por ID | ADMIN, TRABAJADOR |
| POST | `/api/ventas/desde-pedido/{id}` | Crear venta desde pedido | ADMIN, TRABAJADOR, CLIENTE |
| PUT | `/api/ventas/{id}` | Actualizar venta | ADMIN, TRABAJADOR |
| DELETE | `/api/ventas/{id}` | Eliminar venta | ADMIN |

#### BoletaController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/boletas` | Listar todas las boletas | ADMIN, TRABAJADOR |
| GET | `/api/boletas/{id}` | Obtener boleta por ID | ADMIN, TRABAJADOR, CLIENTE |
| POST | `/api/boletas` | Crear boleta | ADMIN, TRABAJADOR |
| PUT | `/api/boletas/{id}` | Actualizar boleta | ADMIN, TRABAJADOR |
| DELETE | `/api/boletas/{id}` | Eliminar boleta | ADMIN |

#### DashboardController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| GET | `/api/dashboard/resumen-ventas` | Resumen completo | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-hoy` | Ventas de hoy | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-mes-actual` | Ventas del mes | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-anio-actual` | Ventas del año | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-por-mes` | Ventas agrupadas por mes | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/kpis` | KPIs según periodo | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-categoria` | Ventas por categoría | ADMIN, TRABAJADOR |
| GET | `/api/dashboard/ventas-ciudad` | Ventas por ciudad | ADMIN, TRABAJADOR |

---

### Servicios de GESTIONVENTA

#### VentaService
```java
public Venta crearVentaDesdePedidoconBoleta(Long idPedido, String token) {
    // 1. Consultar pedido a GESTIONPEDIDO
    // 2. Crear venta
    // 3. Crear boleta automáticamente
    // 4. Retornar venta
}
```

#### PedidoClienteService
**Función:** Comunicación con GESTIONPEDIDO

```java
public PedidoDTO obtenerPedidoPorId(Long idPedido, String token) {
    // Hacer petición HTTP a GESTIONPEDIDO
    // Pasar token JWT en header
}
```

---

## Microservicio: GESTIONCONTACTO

**Puerto:** 8085
**Ruta Base:** `/api/mensajes`
**Paquete:** `com.GoldenBurger.gestionContacto`

### Responsabilidades

1. Gestión de mensajes de contacto desde formulario público
2. Estados de mensajes (no leído, leído, respondido)
3. Panel de administración de mensajes

---

### Modelo de GESTIONCONTACTO

#### MensajeContacto
**Tabla:** `MENSAJE_CONTACTO`

```java
@Entity
@Table(name = "mensaje_contacto")
public class MensajeContacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    @Column
    private String telefono;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private Timestamp fechaEnvio;

    @Column(name = "estado", nullable = false)
    private Integer estado; // 0=No leído, 1=Leído, 2=Respondido
}
```

---

### Controlador de GESTIONCONTACTO

#### MensajeContactoController
**Endpoints:**

| Método | Ruta | Descripción | Roles |
|--------|------|-------------|-------|
| POST | `/api/mensajes` | Enviar mensaje | Público |
| GET | `/api/mensajes` | Listar mensajes | ADMIN, TRABAJADOR |
| GET | `/api/mensajes/{id}` | Obtener mensaje por ID | ADMIN, TRABAJADOR |
| GET | `/api/mensajes/estado/{estado}` | Filtrar por estado | ADMIN, TRABAJADOR |
| PUT | `/api/mensajes/{id}/estado/{estado}` | Cambiar estado | ADMIN, TRABAJADOR |
| DELETE | `/api/mensajes/{id}` | Eliminar mensaje | ADMIN |

---

## Base de Datos Oracle

### Esquema General

```
USUARIO (id_usuario PK, id_rol FK, email, fecha_creacion)
  ├─ CLIENTE (id_cliente PK, id_usuario FK, nombre_cliente, telefono_cliente)
  │   └─ DIRECCION_CLIENTE (id_direccion PK, id_cliente FK, id_ciudad FK, direccion, alias)
  └─ TRABAJADOR (id_trabajador PK, id_usuario FK, nombre_trabajador, rut_trabajador)

ROL (id_rol PK, nombre_rol)

CIUDAD (id_ciudad PK, nombre_ciudad)

CATEGORIA_PRODUCTO (id_categoria PK, nombre_categoria)
  └─ PRODUCTO (id_producto PK, id_categoria FK, nombre_producto, descripcion, precio_base, imagen_url, disponible)

PEDIDO (id_pedido PK, id_cliente FK, id_estado_pedido FK, id_metodo_pago FK, id_tipo_entrega FK, id_direccion_cliente FK, monto_subtotal, monto_envio, monto_total, fecha_pedido, nota_cliente)
  ├─ DETALLE_PEDIDO (id_detalle PK, id_pedido FK, id_producto FK, cantidad, precio_unitario, subtotal_linea)
  └─ PAGO (id_pago PK, id_pedido FK, monto_pago, fecha_pago, estado_pago, id_preferencia_mpos, id_pago_mpos, respuesta_mercado_pago)

ESTADO_PEDIDO (id_estado_pedido PK, nombre_estado)

METODO_PAGO (id_metodo_pago PK, nombre_metodo)

TIPO_ENTREGA (id_tipo_entrega PK, nombre_tipo)

VENTA (id_venta PK, id_pedido FK, monto_total_venta, fecha_venta)
  └─ BOLETA (id_boleta PK, id_venta FK, numero_boleta, fecha_emision, monto_total, rut_cliente, nombre_cliente)

DEVOLUCION (id_devolucion PK, id_venta FK, motivo_devolucion, monto_devolucion, fecha_devolucion)

MENSAJE_CONTACTO (id_mensaje PK, nombre, email, telefono, mensaje, fecha_envio, estado)
```

---

## Comunicación Entre Microservicios

### Flujo de Comunicación

1. **Frontend → API Gateway**
   - El frontend SIEMPRE llama al API Gateway (puerto 8080)
   - Nunca llama directamente a los microservicios

2. **API Gateway → Microservicios**
   - El API Gateway redirige las peticiones al microservicio correspondiente
   - Pasa el token JWT en headers `Authorization` y `X-Internal-Token`

3. **Microservicio → Microservicio**
   - Los microservicios pueden comunicarse entre sí usando `RestTemplate` o `WebClient`
   - Deben pasar el token JWT recibido en el header `X-Internal-Token`

### Ejemplo: Crear Venta desde Pedido

```
┌──────────────┐
│   FRONTEND   │
└──────┬───────┘
       │ PUT /api/pedidos/procesar/127
       ▼
┌────────────────────────┐
│    API GATEWAY         │
│  (Valida JWT)          │
└────────┬───────────────┘
         │ PUT /api/pedidos/procesar/127
         │ Headers: Authorization, X-Internal-Token
         ▼
┌────────────────────────┐
│  GESTIONPEDIDO         │
│  (8083)                │
│                        │
│  1. Cambiar estado a   │
│     "Pagado"           │
│                        │
│  2. Llamar a           │
│     GESTIONVENTA       │
└────────┬───────────────┘
         │ POST /api/ventas/desde-pedido/127
         │ Headers: X-Internal-Token
         ▼
┌────────────────────────┐
│  GESTIONVENTA          │
│  (8084)                │
│                        │
│  1. Consultar pedido   ├─────┐
│     a GESTIONPEDIDO    │     │
│                        │◄────┘
│  2. Crear venta        │
│                        │
│  3. Crear boleta       │
└────────────────────────┘
```

---

## Configuración de Seguridad en Microservicios

Cada microservicio tiene su propio filtro JWT y configuración de seguridad:

```java
// JwtAuthenticationFilter en cada microservicio
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {
        // 1. Extraer token del header Authorization
        // 2. Validar token JWT
        // 3. Extraer claims (uid, email, rol)
        // 4. Crear CustomUserDetails
        // 5. Establecer autenticación en SecurityContext
        // 6. Continuar con la petición
    }
}
```

---

## Puertos de Microservicios

| Servicio | Puerto | URL |
|----------|--------|-----|
| API Gateway | 8080 | http://161.153.219.128:8080 |
| GESTIONUSUARIO | 8081 | http://localhost:8081 |
| GESTIONCATALOGO | 8082 | http://localhost:8082 |
| GESTIONPEDIDO | 8083 | http://localhost:8083 |
| GESTIONVENTA | 8084 | http://localhost:8084 |
| GESTIONCONTACTO | 8085 | http://localhost:8085 |

---

**Documentación creada:** 2025-01-27
**Versión:** 1.0

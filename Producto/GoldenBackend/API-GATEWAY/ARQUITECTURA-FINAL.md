# 🏗️ ARQUITECTURA FINAL - Golden Burgers API Gateway

## 📊 Diagrama de Flujo Completo

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND (React/Vue/Angular)                   │
│                                                                          │
│  1. Login con Firebase Authentication (email/password)                  │
│  2. Recibe idToken de Firebase                                          │
│  3. POST /api/auth/login con { firebaseToken: idToken }                 │
│  4. Recibe internalToken (JWT 24h)                                      │
│  5. Guarda internalToken en localStorage                                │
│  6. Usa internalToken en header: Authorization: Bearer {token}          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓ HTTP
┌─────────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Puerto 8080)                           │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  SecurityConfig                                             │        │
│  │  - Rutas públicas: /api/auth/**, /actuator/**             │        │
│  │  - Rutas protegidas: /api/** (requieren JWT)              │        │
│  └────────────────────────────────────────────────────────────┘        │
│                           ↓                                              │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  JwtAuthenticationFilter                                    │        │
│  │  - Extrae token de header Authorization                    │        │
│  │  - Valida JWT con JwtService                               │        │
│  │  - Crea CustomUserDetails con claims del token             │        │
│  │  - Establece SecurityContext                               │        │
│  └────────────────────────────────────────────────────────────┘        │
│                           ↓                                              │
│  ┌──────────────────────┐      ┌──────────────────────────┐            │
│  │  AuthController      │      │  ProxyController         │            │
│  │                      │      │                          │            │
│  │  /api/auth/login ────┼──┐   │  /api/usuarios/**   ────┼──┐         │
│  │  /api/auth/refresh   │  │   │  /api/clientes/**       │  │         │
│  │  /api/auth/health    │  │   │  /api/trabajadores/**   │  │         │
│  └──────────────────────┘  │   │  /api/roles/**          │  │         │
│                            │   │  /api/ciudades/**       │  │         │
│  ┌──────────────────────┐  │   └──────────────────────────┘  │         │
│  │ AuthenticationService│◄─┘                                  │         │
│  │                      │                                     │         │
│  │  1. Valida Firebase  │      ┌──────────────────────────┐  │         │
│  │  2. Consulta rol     │      │  RestTemplate (Síncrono) │◄─┘         │
│  │  3. Genera JWT       │      │                          │            │
│  └──────────────────────┘      │  - Copia headers         │            │
│            ↓                    │  - Agrega X-Internal-Token            │
│  ┌──────────────────────┐      │  - Hace request HTTP     │            │
│  │  JwtService          │      └──────────────────────────┘            │
│  │                      │                     ↓                         │
│  │  - generateToken()   │                                               │
│  │  - validateToken()   │                                               │
│  │  - extractClaims()   │                                               │
│  └──────────────────────┘                                               │
│            ↑                                                             │
│  ┌──────────────────────┐                                               │
│  │  FirebaseConfig      │                                               │
│  │  - Firebase Admin SDK│                                               │
│  │  - Credenciales      │                                               │
│  └──────────────────────┘                                               │
└─────────────────────────────────────────────────────────────────────────┘
                    ↓ HTTP (X-Internal-Token header)
┌─────────────────────────────────────────────────────────────────────────┐
│                     GESTIONUSUARIO (Puerto 8081)                         │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  SecurityConfig                                             │        │
│  │  - Rutas públicas: /actuator/**, /api/usuarios/firebase/** │        │
│  │  - Rutas protegidas: /api/** (requieren X-Internal-Token)  │        │
│  └────────────────────────────────────────────────────────────┘        │
│                           ↓                                              │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  JwtAuthenticationFilter                                    │        │
│  │  - Extrae token de header X-Internal-Token                 │        │
│  │  - Valida JWT con JwtService (misma clave que Gateway)     │        │
│  │  - Crea CustomUserDetails                                  │        │
│  │  - Establece SecurityContext                               │        │
│  └────────────────────────────────────────────────────────────┘        │
│                           ↓                                              │
│  ┌──────────────────────────────────────────────────────────┐          │
│  │  Controllers                                              │          │
│  │  - UsuarioController: /api/usuarios/**                    │          │
│  │  - ClienteController: /api/clientes/**                    │          │
│  │  - TrabajadorController: /api/trabajadores/**             │          │
│  │  - RolController: /api/roles/**                           │          │
│  │  - CiudadController: /api/ciudades/**                     │          │
│  └──────────────────────────────────────────────────────────┘          │
│                           ↓                                              │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  Oracle Autonomous Database                                 │        │
│  │  - Tablas: Usuario, Cliente, Trabajador, Rol, Ciudad       │        │
│  │  - Wallet: basedatosfbo_medium                             │        │
│  └────────────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Flujo de Seguridad Detallado

### 1️⃣ **Login Inicial**
```
Usuario → Firebase Auth → idToken
idToken → API Gateway /api/auth/login
API Gateway → Firebase Admin SDK (verifica idToken) ✅
API Gateway → GESTIONUSUARIO /api/usuarios/firebase/{uid} (SIN TOKEN)
GESTIONUSUARIO → { rolId, email, rolNombre }
API Gateway → Genera JWT con claims { uid, email, rolId, rolNombre }
API Gateway → Usuario { internalToken, user, expiresIn }
```

### 2️⃣ **Request Protegida**
```
Usuario → API Gateway /api/roles
Header: Authorization: Bearer {internalToken}
↓
JwtAuthenticationFilter → Valida JWT ✅
JwtAuthenticationFilter → SecurityContext.setAuthentication()
↓
ProxyController → Intercepta request
ProxyController → Extrae token de Authorization header
ProxyController → Agrega header X-Internal-Token: {token}
ProxyController → RestTemplate.exchange()
↓
GESTIONUSUARIO /api/roles
Header: X-Internal-Token: {token}
↓
JwtAuthenticationFilter → Valida JWT ✅
JwtAuthenticationFilter → SecurityContext.setAuthentication()
↓
RolController → Procesa request
RolController → [ { idRol, nombreRol }, ... ]
↓
ProxyController → Recibe respuesta
ProxyController → Reenvía al usuario
```

---

## 🔑 Tokens y Claims

### Firebase idToken (1 hora)
```json
{
  "iss": "https://securetoken.google.com/goldenburgers-60680",
  "aud": "goldenburgers-60680",
  "auth_time": 1762636786,
  "user_id": "cvULYnuoH2ZROSc7rarPf16vFik2",
  "email": "fabian.basaes@gmail.com",
  "email_verified": false
}
```

### JWT Interno (24 horas)
```json
{
  "sub": "cvULYnuoH2ZROSc7rarPf16vFik2",
  "email": "fabian.basaes@gmail.com",
  "rolId": 1,
  "rolNombre": "Admin",
  "iat": 1762638975,
  "exp": 1762725375
}
```

---

## 📦 Dependencias Clave

### API Gateway
```xml
<!-- Firebase Admin SDK -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.4.2</version>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Spring WebFlux para WebClient -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### GESTIONUSUARIO
```xml
<!-- Solo JWT, NO Firebase -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Oracle JDBC -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
</dependency>
```

---

## 🎯 Ventajas de esta Arquitectura

✅ **Centralización**: Firebase solo en API Gateway
✅ **Seguridad**: JWT interno con expiración configurable
✅ **Escalabilidad**: Fácil agregar nuevos microservicios
✅ **Mantenibilidad**: Lógica de auth en un solo lugar
✅ **Performance**: RestTemplate síncrono mantiene contexto
✅ **Flexibilidad**: Puedes cambiar Firebase por otro sistema sin tocar microservicios

---

**Fecha:** 8 de noviembre de 2025
**Versión:** 1.0.0 (Probada y funcionando)

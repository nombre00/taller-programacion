# Flujo de Autenticación - Golden Burgers

## Resumen General

El sistema de autenticación de Golden Burgers utiliza una arquitectura híbrida que combina:
1. **Firebase Authentication** para la autenticación de usuarios
2. **JWT interno** generado por el API Gateway para autorización en microservicios

---

## Flujo Completo de Login

### 1. Usuario Ingresa Credenciales (Frontend)

**Archivo:** `src/pages/client/Login.jsx:40-122`

```javascript
const handleLoginSubmit = async (e) => {
  // PASO 1: Autenticar con Firebase
  const userCredential = await signInWithEmailAndPassword(auth, loginEmail, loginPassword);
  const firebaseToken = await userCredential.user.getIdToken();
  const firebaseUid = userCredential.user.uid;

  // PASO 2: Enviar token Firebase al API Gateway
  const response = await api.post("/auth/login", {
    firebaseToken: firebaseToken
  });

  // PASO 3: Guardar respuesta del backend
  const token = response.data.internalToken;
  const userData = response.data.user;

  // PASO 4: Almacenar en localStorage
  localStorage.setItem("authToken", token);
  localStorage.setItem("user", JSON.stringify(userData));
  localStorage.setItem("userId", firebaseUid);
  localStorage.setItem("userRole", userData.rolNombre);

  // PASO 5: Redirigir según rol
  if (userData.rolNombre === "Admin" || userData.rolNombre === "Trabajador") {
    navigate("/admin/dashboard");
  } else {
    navigate("/inicio");
  }
}
```

**Datos enviados al backend:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

---

### 2. API Gateway Recibe el Token de Firebase (Backend)

#### 2.1 AuthController recibe la petición

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/controller/AuthController.java:31-49`

```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    log.info("Intento de login con token de Firebase");

    LoginResponse response = authenticationService.authenticateWithFirebase(
            request.getFirebaseToken()
    );

    log.info("Login exitoso para usuario: {}", response.getUser().getEmail());
    return ResponseEntity.ok(response);
}
```

**DTO LoginRequest:**
```java
// LoginRequest.java
public class LoginRequest {
    private String firebaseToken;
}
```

---

#### 2.2 AuthenticationService valida el token

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/service/AuthenticationService.java:33-75`

```java
public LoginResponse authenticateWithFirebase(String firebaseToken) {
    // 1. Validar el token de Firebase
    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
    String uid = decodedToken.getUid();
    String email = decodedToken.getEmail();

    log.info("Token de Firebase validado para usuario: {}", email);

    // 2. Consultar el rol del usuario desde GESTIONUSUARIO
    UserDTO userInfo = getUserInfoFromMicroservice(uid);

    // 3. Generar token JWT interno con los claims
    String internalToken = jwtService.generateToken(
            uid,
            email,
            userInfo.getRolId(),
            userInfo.getRolNombre()
    );

    // 4. Construir y retornar la respuesta
    return LoginResponse.builder()
            .internalToken(internalToken)
            .user(UserDTO.builder()
                    .uid(uid)
                    .email(email)
                    .nombre(userInfo.getNombre())
                    .rolId(userInfo.getRolId())
                    .rolNombre(userInfo.getRolNombre())
                    .build())
            .expiresIn(jwtService.getExpirationTime())
            .build();
}
```

---

#### 2.3 Consulta al Microservicio GESTIONUSUARIO

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/service/AuthenticationService.java:80-105`

**Endpoint llamado:**
```
GET http://localhost:8081/api/usuarios/firebase/{uid}
```

**Microservicio GESTIONUSUARIO responde:**

**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/controller/UsuarioController.java:28-48`

```java
@GetMapping("/firebase/{uid}")
public ResponseEntity<?> getUserByFirebaseUid(@PathVariable String uid) {
    return usuarioRepository.findById(uid)
            .map(usuario -> {
                Map<String, Object> response = new HashMap<>();
                response.put("firebaseUid", usuario.getIdUsuario());
                response.put("email", usuario.getEmail());
                response.put("rolId", usuario.getRol().getIdRol().intValue());
                response.put("rolNombre", usuario.getRol().getNombreRol());

                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

**Respuesta del microservicio:**
```json
{
  "firebaseUid": "abc123xyz",
  "email": "cliente@gmail.com",
  "rolId": 1,
  "rolNombre": "Cliente"
}
```

**Modelo de datos:**

**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/model/Usuario.java`

```java
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @Column(name = "ID_USUARIO", length = 255)
    private String idUsuario; // UID de Firebase

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROL", nullable = false)
    private Rol rol;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDate fechaCreacion;
}
```

---

#### 2.4 Generación del Token JWT Interno

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/service/JwtService.java:46-59`

```java
public String generateToken(String uid, String email, Integer rolId, String rolNombre) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", email);
    claims.put("rolId", rolId);
    claims.put("rolNombre", rolNombre);
    return generateToken(claims, uid);
}

private String buildToken(Map<String, Object> extraClaims, String uid, long expiration) {
    return Jwts.builder()
            .claims(extraClaims)
            .subject(uid)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
}
```

**Estructura del JWT generado:**
```json
{
  "sub": "abc123xyz",           // Firebase UID
  "email": "cliente@gmail.com",
  "rolId": 1,
  "rolNombre": "Cliente",
  "iat": 1702345678,            // Timestamp de emisión
  "exp": 1702432078             // Timestamp de expiración
}
```

---

#### 2.5 Respuesta del API Gateway al Frontend

**DTO LoginResponse:**

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/dto/LoginResponse.java`

```java
@Data
@Builder
public class LoginResponse {
    private String internalToken;
    private UserDTO user;
    private long expiresIn;
}
```

**Respuesta JSON:**
```json
{
  "internalToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

## Flujo de Autenticación en Peticiones Subsecuentes

### 1. Interceptor de Axios Agrega el Token (Frontend)

**Archivo:** `src/config/api.js:31-57`

```javascript
api.interceptors.request.use(
  (config) => {
    // Obtener token del localStorage
    const token = localStorage.getItem("authToken");

    if (token) {
      // Agregar token en el header Authorization
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  }
);
```

**Ejemplo de petición HTTP:**
```http
GET /api/clientes/123 HTTP/1.1
Host: 161.153.219.128:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

---

### 2. API Gateway Valida el Token JWT (Backend)

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/filter/JwtAuthenticationFilter.java:32-144`

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) {

    // Verificar si la ruta es pública (no requiere autenticación)
    String path = request.getServletPath();
    if (path.startsWith("/api/auth/") ||
        path.startsWith("/api/catalogo") && method.equals("GET")) {
        filterChain.doFilter(request, response);
        return;
    }

    // Extraer el token del header Authorization
    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    final String jwt = authHeader.substring(7);

    // Extraer información del token
    final String uid = jwtService.extractUid(jwt);
    final String email = jwtService.extractEmail(jwt);
    final Integer rolId = jwtService.extractRolId(jwt);
    final String rolNombre = jwtService.extractRolNombre(jwt);

    // Validar el token
    if (jwtService.isTokenValid(jwt, uid)) {
        // Crear CustomUserDetails con la información del token
        CustomUserDetails userDetails = new CustomUserDetails(
                uid, email, rolId.longValue(), rolNombre
        );

        // Crear el objeto de autenticación
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );

        // Establecer la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
}
```

---

### 3. API Gateway Reenvía la Petición al Microservicio

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/controller/ProxyController.java:173-220`

```java
private ResponseEntity<String> forwardRequest(
        HttpServletRequest request,
        String body,
        String microserviceUrl) {

    // Construir URL del microservicio
    String path = request.getRequestURI();
    String targetUrl = microserviceUrl + path;

    // Copiar headers
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if (!headerName.equalsIgnoreCase("host") &&
            !headerName.equalsIgnoreCase("content-length")) {
            headers.add(headerName, request.getHeader(headerName));
        }
    }

    // Agregar token en headers
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.set("X-Internal-Token", token);
    }

    // Reenviar petición
    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.exchange(
        URI.create(targetUrl),
        method,
        entity,
        String.class
    );

    return response;
}
```

**Ejemplo de petición reenviada:**
```http
GET /api/clientes/123 HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Internal-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

---

### 4. Microservicio Valida el Token JWT

Cada microservicio tiene su propio `JwtAuthenticationFilter` que valida el token JWT recibido.

**Ejemplo en GESTIONUSUARIO:**

**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/filter/JwtAuthenticationFilter.java`

El filtro extrae el token y valida:
- Firma del token
- Fecha de expiración
- Claims (uid, email, rol)

Una vez validado, Spring Security permite o deniega el acceso según los permisos del rol.

---

## Flujo de Registro de Nuevo Usuario

### 1. Usuario Se Registra (Frontend)

**Archivo:** `src/pages/client/Login.jsx:125-185`

```javascript
const handleRegisterSubmit = async (e) => {
  // PASO 1: Crear usuario en Firebase Authentication
  const userCredential = await createUserWithEmailAndPassword(
    auth,
    registerEmail,
    registerPassword
  );
  const firebaseUid = userCredential.user.uid;

  // PASO 2: Registrar cliente en el backend
  await usuariosService.registrarCliente({
    idUsuario: firebaseUid,
    nombreCliente: registerName,
    email: registerEmail
  });

  alert("¡Registro exitoso! Ahora puedes iniciar sesión.");
}
```

---

### 2. Backend Registra el Cliente

**Endpoint:** `POST /api/clientes`

**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/controller/ClienteController.java:109-114`

```java
@PostMapping
public ResponseEntity<ClienteDTO> registerCliente(
        @Valid @RequestBody RegistrarCliente request) {
    ClienteDTO cliente = clienteService.registerCliente(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
}
```

**DTO RegistrarCliente:**
```java
public class RegistrarCliente {
    private String idUsuario;      // Firebase UID
    private String email;
    private String nombreCliente;
    private String telefonoCliente;
}
```

El servicio:
1. Crea un registro en la tabla `USUARIO` con rol "Cliente"
2. Crea un registro en la tabla `CLIENTE` asociado al usuario

---

## Autorización por Roles

### Roles del Sistema

| ID Rol | Nombre     | Descripción                          |
|--------|------------|--------------------------------------|
| 1      | Cliente    | Usuario normal del sistema           |
| 2      | Trabajador | Empleado con permisos administrativos|
| 3      | Admin      | Administrador con todos los permisos |

---

### Control de Acceso en Endpoints

Los microservicios usan la anotación `@PreAuthorize` de Spring Security:

```java
// Solo ADMIN puede acceder
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/clientes/{id}")
public ResponseEntity<Void> deleteCliente(@PathVariable Long id) { ... }

// ADMIN y TRABAJADOR pueden acceder
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
@GetMapping("/clientes")
public ResponseEntity<List<ClienteDTO>> getAllClientes() { ... }

// Cualquier usuario autenticado puede acceder
@GetMapping("/clientes/{id}")
public ResponseEntity<ClienteDTO> getClienteById(@PathVariable Long id) { ... }
```

---

## Rutas Públicas (Sin Autenticación)

El `JwtAuthenticationFilter` permite el acceso sin token a:

### API Gateway
```java
// Sistema y documentación
path.startsWith("/api/auth/")
path.startsWith("/actuator/")
path.startsWith("/health")
path.startsWith("/swagger-ui/")

// GESTIÓN USUARIO - Endpoints públicos
path.startsWith("/api/usuarios/firebase/") && method.equals("GET")
path.equals("/api/clientes") && method.equals("POST")
path.startsWith("/api/roles") && method.equals("GET")
path.startsWith("/api/ciudades") && method.equals("GET")

// GESTIÓN CATÁLOGO - Consultas públicas
path.startsWith("/api/catalogo") && method.equals("GET")

// GESTIÓN CONTACTO - Formulario público
path.equals("/api/mensajes") && method.equals("POST")

// GESTIÓN PEDIDO - Webhook de Mercado Pago
path.equals("/api/pagos/webhook") && method.equals("POST")
```

---

## Manejo de Sesiones en el Frontend

### Almacenamiento Local

**Archivo:** `src/pages/client/Login.jsx:78-84`

```javascript
localStorage.setItem("authToken", token);
localStorage.setItem("user", JSON.stringify(userData));
localStorage.setItem("userName", userData.email);
localStorage.setItem("userId", firebaseUid);
localStorage.setItem("userRole", userData.rolNombre);
localStorage.setItem("isLoggedIn", "true");
```

### Cierre de Sesión

El usuario debe:
1. Limpiar el `localStorage`
2. Cerrar sesión en Firebase (opcional)
3. Redirigir al login

```javascript
const handleLogout = () => {
  localStorage.clear();
  navigate("/login");
};
```

---

## Manejo de Errores de Autenticación

### En el Frontend

**Archivo:** `src/config/api.js:68-114`

```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;

      switch (status) {
        case 401:
          // Token inválido o expirado
          localStorage.removeItem("authToken");
          localStorage.removeItem("user");
          if (!window.location.pathname.includes("/login")) {
            window.location.href = "/login";
          }
          break;

        case 403:
          alert("No tienes permisos para realizar esta acción");
          break;
      }
    }
    return Promise.reject(error);
  }
);
```

---

## Diagrama de Flujo Completo

```
┌─────────────┐
│   CLIENTE   │
│  (Browser)  │
└──────┬──────┘
       │
       │ 1. Login (email, password)
       ▼
┌─────────────────┐
│    FIREBASE     │
│ Authentication  │
└────────┬────────┘
         │
         │ 2. Firebase Token
         ▼
┌─────────────────────────┐
│     API GATEWAY         │
│  (Puerto 8080)          │
│                         │
│ AuthController          │
│ └─ /auth/login          │
└────────┬────────────────┘
         │
         │ 3. Consultar rol
         ▼
┌─────────────────────────┐
│  MICROSERVICIO          │
│  GESTIONUSUARIO         │
│  (Puerto 8081)          │
│                         │
│ GET /api/usuarios/      │
│     firebase/{uid}      │
└────────┬────────────────┘
         │
         │ 4. Datos del usuario (email, rol)
         ▼
┌─────────────────────────┐
│     API GATEWAY         │
│                         │
│ JwtService              │
│ └─ Generar JWT interno  │
└────────┬────────────────┘
         │
         │ 5. LoginResponse (internalToken + user)
         ▼
┌─────────────┐
│   CLIENTE   │
│             │
│ localStorage│
│ - authToken │
│ - user      │
│ - userId    │
│ - userRole  │
└─────────────┘
```

---

## Configuración de Seguridad

### Secreto JWT

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/resources/application.properties`

```properties
jwt.secret=TU_CLAVE_SECRETA_BASE64_AQUI
jwt.expiration=86400000  # 24 horas en milisegundos
```

### Firebase Configuration

**Archivo:** `src/pages/client/Login.jsx:13-21`

```javascript
const firebaseConfig = {
  apiKey: "AIzaSyD_2NIG34JLQ3fPr2SRzwr3PRTb9IedILY",
  authDomain: "goldenburgers-60680.firebaseapp.com",
  projectId: "goldenburgers-60680",
  storageBucket: "goldenburgers-60680.firebasestorage.app",
  messagingSenderId: "200007088077",
  appId: "1:200007088077:web:b0578771a57f0ecb733684"
};
```

---

## Consideraciones de Seguridad

1. **Token JWT Interno**
   - El token JWT interno NO debe ser compartido públicamente
   - Expira después de 24 horas
   - Contiene información sensible del usuario (uid, email, rol)

2. **Firebase Token**
   - Solo se usa para el login inicial
   - No se almacena en el frontend
   - Se valida en el backend usando Firebase Admin SDK

3. **HTTPS**
   - En producción, todas las comunicaciones deben ser sobre HTTPS
   - Actualmente usa HTTP (desarrollo)

4. **CORS**
   - El API Gateway permite CORS desde cualquier origen (`origins = "*"`)
   - En producción, restringir a dominios específicos

---

## Próximos Pasos Recomendados

1. **Refresh Token**
   - Implementar un mecanismo para refrescar el token antes de que expire
   - Endpoint: `POST /api/auth/refresh`

2. **Logout**
   - Implementar logout en Firebase y backend
   - Invalidar el token JWT en el servidor

3. **Recuperación de Contraseña**
   - Usar Firebase para enviar emails de recuperación
   - Endpoint: Firebase Authentication

4. **Verificación de Email**
   - Requerir verificación de email antes de permitir login
   - Usar Firebase Authentication

---

**Documentación creada:** 2025-01-27
**Versión:** 1.0

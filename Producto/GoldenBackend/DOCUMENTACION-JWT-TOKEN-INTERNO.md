# 🔐 Documentación: Generador de Token Interno JWT

## 📖 ¿Qué es un Token JWT?

Un **JSON Web Token (JWT)** es un estándar abierto (RFC 7519) que define un formato compacto y seguro para transmitir información entre partes como un objeto JSON. Este token está firmado digitalmente, lo que garantiza su integridad y autenticidad.

En nuestro sistema **Golden Burgers**, usamos JWT para:
- Autenticar usuarios entre microservicios
- Transmitir información del usuario (email, rol) sin consultar la base de datos
- Controlar acceso a endpoints protegidos mediante roles

---

## 🏗️ Estructura de un JWT

Un JWT consta de **tres partes** separadas por puntos (`.`):

```
HEADER.PAYLOAD.SIGNATURE
```

### Ejemplo real de nuestro sistema:
```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xJZCI6MSwiZW1haWwiOiJmYWJpYW4uYmFzYWVzQGdtYWlsLmNvbSIsInJvbE5vbWJyZSI6IkFkbWluIiwic3ViIjoiY3ZVTFludW9IMlpST1NjN3JhclBmMTZ2RmlrMiIsImlhdCI6MTc2MjczNTI4MiwiZXhwIjoxNzYyODIxNjgyfQ.ZXLOwNZbJvJ7Ohjk55OhHqi9ZkoTNx_EvBkKbr4_d80
```

### Decodificado:

#### 1️⃣ **HEADER** (Cabecera)
```json
{
  "alg": "HS256",  // Algoritmo de firma: HMAC-SHA256
  "typ": "JWT"     // Tipo de token: JWT
}
```

#### 2️⃣ **PAYLOAD** (Datos del usuario)
```json
{
  "email": "fabian.basaes@gmail.com",
  "rolId": 1,
  "rolNombre": "Admin",
  "sub": "cvULYnuoH2ZROSc7rarPf16vFik2",  // Firebase UID
  "iat": 1762735282,  // Issued At: fecha de emisión (timestamp)
  "exp": 1762821682   // Expiration: fecha de expiración (24h después)
}
```

#### 3️⃣ **SIGNATURE** (Firma digital)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

---

## 🔧 Implementación en Golden Burgers

### 📍 Ubicación del código

**Microservicio:** `GESTIONUSUARIO`  
**Archivo:** `src/main/java/com/goldenburgers/gestionUsuario/service/JwtService.java`

---

## 🎯 Método Principal: `generateToken()`

### Código completo:

```java
public String generateToken(String firebaseUid, String email, Integer rolId, String rolNombre) {
    // 1. Crear un mapa de claims (datos personalizados)
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", email);           // Email del usuario
    claims.put("rolId", rolId);           // ID del rol (1=Admin, 2=Trabajador, 3=Cliente)
    claims.put("rolNombre", rolNombre);   // Nombre del rol
    
    // 2. Construir y retornar el JWT
    return Jwts.builder()
            .claims(claims)                    // Agregar datos personalizados
            .subject(firebaseUid)              // Identificador único (Firebase UID)
            .issuedAt(new Date(System.currentTimeMillis()))  // Fecha de emisión
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Expira en 24h
            .signWith(getSignInKey())          // Firmar con clave secreta
            .compact();                        // Generar string final
}
```

---

## 📋 Explicación paso a paso

### **Paso 1: Crear Claims (Datos personalizados)**

```java
Map<String, Object> claims = new HashMap<>();
claims.put("email", email);
claims.put("rolId", rolId);
claims.put("rolNombre", rolNombre);
```

**¿Qué son los Claims?**
- Son pares clave-valor que se guardan **dentro del token**
- Permiten transmitir información del usuario sin consultar la BD
- Se codifican en Base64 y son **públicamente legibles** (por eso no guardamos contraseñas)

**Claims en nuestro sistema:**
| Claim | Ejemplo | Descripción |
|-------|---------|-------------|
| `email` | `fabian.basaes@gmail.com` | Correo del usuario |
| `rolId` | `1` | ID del rol en base de datos |
| `rolNombre` | `Admin` | Nombre del rol para autorización |

---

### **Paso 2: Definir el Subject (Identificador único)**

```java
.subject(firebaseUid)
```

- El `subject` es el **identificador principal del token**
- Usamos el **Firebase UID** porque:
  - ✅ Es único e inmutable
  - ✅ Vincula usuarios entre Firebase Auth y nuestra BD
  - ✅ Permite autenticación multi-plataforma
- Se guarda en el claim estándar `"sub"`

---

### **Paso 3: Fecha de emisión (iat)**

```java
.issuedAt(new Date(System.currentTimeMillis()))
```

- `System.currentTimeMillis()` → Timestamp actual en milisegundos
- `new Date(...)` → Convierte a objeto Date de Java
- Se guarda en el claim estándar `"iat"` (Issued At)

**Utilidad:**
- Registro de cuándo se creó el token
- Auditoría y logs de seguridad
- Detectar tokens muy antiguos

---

### **Paso 4: Fecha de expiración (exp)**

```java
.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
```

**Cálculo de 24 horas:**
```
System.currentTimeMillis()  // Ahora en milisegundos
+ 1000                      // × 1000 = 1 segundo
  * 60                      // × 60   = 1 minuto
  * 60                      // × 60   = 1 hora
  * 24                      // × 24   = 24 horas
```

- Se guarda en el claim estándar `"exp"` (Expiration)
- Después de 24 horas, el token **se invalida automáticamente**
- Al validar, se verifica: `exp > fecha_actual`

**Beneficios de la expiración:**
- 🔒 Limita el daño si un token es robado
- 🔄 Obliga a renovar credenciales periódicamente
- 🛡️ Reduce superficie de ataque

---

### **Paso 5: Firmar el token**

```java
.signWith(getSignInKey())
```

**¿Qué es firmar un token?**
- Es crear una **firma digital** usando el algoritmo HMAC-SHA256
- La firma garantiza:
  - ✅ **Integridad:** Detecta modificaciones del token
  - ✅ **Autenticidad:** Solo quien tiene el secret puede crear tokens válidos
  - ✅ **No repudio:** Sabemos que fue generado por nuestro sistema

**Proceso de firmado:**
1. Toma header + payload
2. Los codifica en Base64URL
3. Calcula HASH usando HMAC-SHA256 con el `jwt.secret`
4. Agrega el HASH como tercera parte del JWT

---

### **Paso 6: Generar token final**

```java
.compact();
```

- Ensambla las tres partes: `header.payload.signature`
- Codifica todo en Base64URL
- Retorna el string completo del JWT

---

## 🔑 Método auxiliar: `getSignInKey()`

```java
private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

### **¿Qué hace este método?**

1. **Decodifica el secret desde Base64:**
   ```java
   byte[] keyBytes = Decoders.BASE64.decode(secretKey);
   ```
   
   - El `secretKey` está en `application.properties` como string Base64:
     ```properties
     jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
     ```
   - Base64 permite guardar bytes binarios como texto
   - **Decoders.BASE64.decode()** convierte de vuelta a bytes

2. **Crea la clave HMAC-SHA256:**
   ```java
   return Keys.hmacShaKeyFor(keyBytes);
   ```
   
   - Genera una clave criptográfica a partir de los bytes
   - Esta clave se usa para **firmar** y **validar** tokens
   - Requiere mínimo 256 bits (32 bytes)

---

## 🔄 Flujo completo: Generación → Uso → Validación

### **1️⃣ Usuario se autentica con Firebase**

```java
// En AuthController de GESTIONUSUARIO
@PostMapping("/firebase/login")
public ResponseEntity<?> loginWithFirebase(@RequestBody Map<String, String> request) {
    String firebaseToken = request.get("firebaseToken");
    
    // Validar token de Firebase
    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
    String firebaseUid = decodedToken.getUid();
    
    // Buscar usuario en BD por Firebase UID
    Usuario usuario = usuarioService.obtenerPorFirebaseUid(firebaseUid);
    
    // Generar JWT interno
    String jwtToken = jwtService.generateToken(
        firebaseUid,
        usuario.getEmail(),
        usuario.getRol().getIdRol(),
        usuario.getRol().getNombreRol()
    );
    
    // Retornar token al frontend
    return ResponseEntity.ok(Map.of("token", jwtToken, "usuario", usuario));
}
```

---

### **2️⃣ Frontend guarda y usa el token**

```javascript
// Guardar token en localStorage
const response = await fetch('/api/usuarios/firebase/login', {
    method: 'POST',
    body: JSON.stringify({ firebaseToken })
});

const { token, usuario } = await response.json();
localStorage.setItem('token', token);

// Usar token en peticiones
fetch('http://localhost:8084/api/catalogo/categorias', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`,  // ← Token aquí
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({ nombre: 'Hamburguesas' })
});
```

---

### **3️⃣ Microservicio valida el token**

```java
// En JwtAuthenticationFilter de GESTIONCATALOGO
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    // 1. Extraer token del header
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String jwt = authHeader.substring(7);
        
        // 2. Validar firma y expiración
        if (jwtService.isTokenValid(jwt)) {
            // 3. Extraer datos del token
            String email = jwtService.extractEmail(jwt);
            String firebaseUid = jwtService.extractFirebaseUid(jwt);
            List<String> roles = jwtService.extractRoles(jwt);  // ["ADMIN"]
            
            // 4. Crear autenticación de Spring Security
            CustomUserDetails userDetails = new CustomUserDetails(email, firebaseUid, roles);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 5. Establecer contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    
    filterChain.doFilter(request, response);
}
```

---

### **4️⃣ Spring Security valida el rol**

```java
// En CatalogoController
@PostMapping("/categorias")
@PreAuthorize("hasRole('ADMIN')")  // ← Verifica que el usuario tenga rol ADMIN
public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO categoriaDTO) {
    // Este código solo se ejecuta si el usuario tiene rol ADMIN
    CategoriaDTO nuevaCategoria = catalogoService.crearCategoria(categoriaDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
}
```

**¿Cómo funciona `@PreAuthorize`?**
1. Spring Security lee las authorities del usuario autenticado
2. Verifica que exista la authority `ROLE_ADMIN`
3. Si existe → ejecuta el método
4. Si no existe → lanza `AccessDeniedException` (403 Forbidden)

---

## 🔐 Seguridad del Token

### **¿Por qué es seguro un JWT?**

| Característica | Explicación |
|----------------|-------------|
| **Firmado digitalmente** | Solo quien tiene el `jwt.secret` puede crear tokens válidos |
| **Detección de manipulación** | Si alguien modifica el token, la firma no coincide → rechazado |
| **Expiración automática** | Tokens antiguos se invalidan después de 24 horas |
| **Stateless (sin estado)** | No requiere consultar BD en cada petición |
| **No se puede falsificar** | Sin el secret, no puedes crear un token válido |

---

### **⚠️ Consideraciones de seguridad**

#### ❌ **NO hacer:**
```properties
# NO subir el secret a Git
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

#### ✅ **SÍ hacer:**
```properties
# En application.properties
jwt.secret=${JWT_SECRET}

# En .env (no subir a Git)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# En producción (variable de entorno del servidor)
export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

#### 🔒 **Buenas prácticas:**
- ✅ Usar secrets de mínimo 256 bits (32 caracteres)
- ✅ Rotar el secret periódicamente en producción
- ✅ Todos los microservicios deben usar el MISMO secret
- ✅ Nunca guardar información sensible en el payload (contraseñas, tarjetas)
- ✅ Usar HTTPS en producción para proteger el token en tránsito
- ✅ Implementar refresh tokens para sesiones largas

---

## 📊 Diagrama del flujo completo

```
┌─────────────────────────────────────────────────────────────────┐
│                   1. AUTENTICACIÓN                              │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
    Usuario → Firebase Auth → GESTIONUSUARIO
                              │
                              ├─ Valida Firebase Token
                              ├─ Busca usuario en BD
                              ├─ Genera JWT interno
                              │
                              ▼
                    ┌──────────────────────┐
                    │   JWT Token          │
                    │  ┌────────────────┐  │
                    │  │ Header         │  │
                    │  │ Payload        │  │
                    │  │ Signature      │  │
                    │  └────────────────┘  │
                    └──────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   2. ALMACENAMIENTO                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    Frontend (localStorage)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   3. USO DEL TOKEN                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
    POST /api/catalogo/categorias
    Headers: { Authorization: "Bearer <token>" }
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   4. VALIDACIÓN                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              JwtAuthenticationFilter
                              │
                              ├─ Extrae token del header
                              ├─ Valida firma con secret
                              ├─ Verifica expiración
                              ├─ Extrae claims (email, rol)
                              ├─ Crea CustomUserDetails
                              ├─ Establece SecurityContext
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   5. AUTORIZACIÓN                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              @PreAuthorize("hasRole('ADMIN')")
                              │
                              ├─ ✅ Si tiene rol → ejecuta método
                              ├─ ❌ Si no tiene → 403 Forbidden
                              │
                              ▼
                    Respuesta al cliente
```

---

## 🛠️ Dependencias necesarias

### En `pom.xml`:

```xml
<!-- JWT - JSON Web Token -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 📝 Configuración en application.properties

```properties
# JWT Secret (debe ser el mismo en todos los microservicios)
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Expiración del token (24 horas en milisegundos)
jwt.expiration=86400000
```

---

## 🧪 Ejemplo de uso completo

### Generar token:
```java
String token = jwtService.generateToken(
    "cvULYnuoH2ZROSc7rarPf16vFik2",  // Firebase UID
    "fabian.basaes@gmail.com",        // Email
    1,                                 // Rol ID
    "Admin"                            // Rol Nombre
);

// Resultado:
// eyJhbGciOiJIUzI1NiJ9.eyJyb2xJZCI6MSwiZW1haWwiOiJmYWJpYW4uYmFzYWVzQGdtYWlsLmNvbSIsInJvbE5vbWJyZSI6IkFkbWluIiwic3ViIjoiY3ZVTFludW9IMlpST1NjN3JhclBmMTZ2RmlrMiIsImlhdCI6MTc2MjczNTI4MiwiZXhwIjoxNzYyODIxNjgyfQ.ZXLOwNZbJvJ7Ohjk55OhHqi9ZkoTNx_EvBkKbr4_d80
```

### Validar y extraer datos:
```java
String token = "eyJhbGci...";

// Validar
boolean isValid = jwtService.isTokenValid(token);  // true

// Extraer datos
String email = jwtService.extractEmail(token);           // "fabian.basaes@gmail.com"
String uid = jwtService.extractFirebaseUid(token);       // "cvULYnuoH2..."
List<String> roles = jwtService.extractRoles(token);     // ["ADMIN"]
```

---

## 🔍 Depuración de tokens

### Herramienta online:
Puedes decodificar tokens JWT en: **https://jwt.io**

Pega tu token y verás:
- Header decodificado
- Payload decodificado
- Validación de firma (si proporcionas el secret)

### Ejemplo de debugging en código:

```java
// Agregar logs para debugging
logger.info("===== GENERANDO TOKEN =====");
logger.info("Firebase UID: {}", firebaseUid);
logger.info("Email: {}", email);
logger.info("Rol: {} (ID: {})", rolNombre, rolId);

String token = generateToken(firebaseUid, email, rolId, rolNombre);

logger.info("Token generado: {}", token);
logger.info("Expira en: 24 horas");
```

---

## ✅ Checklist de implementación

- [ ] Agregar dependencias JWT en `pom.xml`
- [ ] Configurar `jwt.secret` en `application.properties`
- [ ] Crear clase `JwtService` con métodos de generación y validación
- [ ] Crear `JwtAuthenticationFilter` para interceptar peticiones
- [ ] Configurar `SecurityConfig` con endpoints públicos y protegidos
- [ ] Implementar `CustomUserDetails` para Spring Security
- [ ] Agregar `@PreAuthorize` en controllers con roles específicos
- [ ] Configurar Swagger con autenticación Bearer
- [ ] Verificar que todos los microservicios usen el mismo secret
- [ ] Probar autenticación end-to-end

---

## 📚 Referencias

- **JWT RFC 7519:** https://tools.ietf.org/html/rfc7519
- **JJWT Library:** https://github.com/jwtk/jjwt
- **Spring Security:** https://docs.spring.io/spring-security/reference/
- **JWT Debugger:** https://jwt.io

---

**Fecha de creación:** 9 de noviembre de 2025  
**Proyecto:** Golden Burgers - Sistema de Gestión de Pedidos  
**Autor:** Equipo de Desarrollo Golden Burgers

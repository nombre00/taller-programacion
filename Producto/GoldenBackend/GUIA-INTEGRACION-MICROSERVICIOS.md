# Guía de Integración de Microservicios con API Gateway

## 📋 Índice
- [Introducción](#introducción)
- [Arquitectura General](#arquitectura-general)
- [Configuración del API Gateway](#configuración-del-api-gateway)
- [Configuración de Microservicios](#configuración-de-microservicios)
- [Problemas Comunes y Soluciones](#problemas-comunes-y-soluciones)
- [Checklist para Nuevos Microservicios](#checklist-para-nuevos-microservicios)

---

## Introducción

Este documento describe el proceso completo de integración de microservicios con el API Gateway de Golden Burgers, incluyendo todos los problemas encontrados y sus soluciones.

**Versión**: 1.0  
**Fecha**: Noviembre 2025  
**Stack**: Spring Boot 3.3.5, Java 21, Firebase Auth, JWT

---

## Arquitectura General

### Flujo de Autenticación

```
Usuario → Firebase Auth → API Gateway → Microservicio
         (token Firebase)  (valida + genera JWT interno)  (valida JWT interno)
```

### Componentes Principales

1. **API Gateway** (Puerto 8080)
   - Validación de tokens Firebase
   - Generación de tokens JWT internos
   - Proxy a microservicios
   - Sin Swagger UI (solo proxy)

2. **Microservicios** (Puertos 8081+)
   - Validación de tokens JWT internos
   - Lógica de negocio
   - Swagger UI habilitado

---

## Configuración del API Gateway

### 1. Dependencias en pom.xml

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

<!-- ⚠️ IMPORTANTE: NO incluir SpringDoc en API Gateway -->
<!-- Esto causa conflictos con ProxyController que hace proxy a Swagger de microservicios -->
```

### 2. application.properties

```properties
# Configuración del servidor
server.port=8080
spring.application.name=api-gateway

# JWT Configuration
jwt.secret=TU_SECRET_KEY_MUY_LARGA_Y_SEGURA_AQUI_MINIMO_256_BITS
jwt.expiration=86400000

# Microservices URLs
microservice.gestionusuario.url=http://localhost:8081

# Logging
logging.level.com.goldenburgers=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 3. Estructura de Clases Principales

#### 3.1 DTOs (com.goldenburgers.apigateway.dto)
- `LoginRequest.java` - Email y password para login
- `LoginResponse.java` - Token interno y datos del usuario
- `UserDTO.java` - Información del usuario autenticado
- `RefreshTokenRequest.java` - Para renovar tokens

#### 3.2 Services (com.goldenburgers.apigateway.service)

**JwtService.java** - Generación y validación de tokens JWT internos
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    // Generar token con claims personalizados
    public String generateToken(String uid, String email, Long rolId, String rolNombre);
    
    // Extraer información del token
    public String extractUid(String token);
    public String extractEmail(String token);
    public Integer extractRolId(String token);
    public String extractRolNombre(String token);
    
    // Validar token
    public boolean isTokenValid(String token);
}
```

**AuthenticationService.java** - Validación de Firebase y consulta de roles
```java
@Service
public class AuthenticationService {
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${microservice.gestionusuario.url}")
    private String gestionUsuarioUrl;
    
    // Validar token de Firebase
    public FirebaseToken verifyFirebaseToken(String firebaseToken);
    
    // Obtener usuario desde microservicio
    public UserDTO getUserByFirebaseUid(String uid);
}
```

#### 3.3 Controllers (com.goldenburgers.apigateway.controller)

**AuthController.java** - Endpoints de autenticación
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request);
}
```

**ProxyController.java** - Proxy a microservicios
```java
@RestController
public class ProxyController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private JwtService jwtService;
    
    // ⚠️ CRÍTICO: Usar RestTemplate, NO WebClient
    // WebClient es reactivo y pierde el contexto de Spring Security
    
    @GetMapping("/api/trabajadores/**")
    @GetMapping("/api/clientes/**")
    @GetMapping("/api/usuarios/**")
    // etc...
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        // Extraer JWT del Authorization header
        // Validar token
        // Reenviar a microservicio con X-Internal-Token
    }
    
    // Rutas de Swagger sin autenticación
    @GetMapping({"/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"})
    public ResponseEntity<String> proxySwagger(HttpServletRequest request) {
        // Reenviar sin token
    }
}
```

#### 3.4 Filters (com.goldenburgers.apigateway.filter)

**JwtAuthenticationFilter.java** - Validación de JWT en requests
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) {
        // Rutas públicas: /api/auth/**, /swagger-ui/**, /v3/api-docs/**
        // Extraer token de Authorization header
        // Validar token
        // Establecer SecurityContext
    }
}
```

#### 3.5 Security (com.goldenburgers.apigateway.security)

**CustomUserDetails.java** - Detalles del usuario autenticado
```java
public class CustomUserDetails implements UserDetails {
    private final String uid;
    private final String email;
    private final Long rolId;
    private final String rolNombre;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ⚠️ IMPORTANTE: Convertir a mayúsculas
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase())
        );
    }
}
```

**SecurityConfig.java** - Configuración de Spring Security
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
    }
}
```

#### 3.6 Config (com.goldenburgers.apigateway.config)

**FirebaseConfig.java** - Inicialización de Firebase
```java
@Configuration
public class FirebaseConfig {
    
    @PostConstruct
    public void initialize() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(
            "src/main/resources/firebase-credentials.json"
        );
        
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
            
        FirebaseApp.initializeApp(options);
    }
}
```

**RestTemplateConfig.java** - Bean de RestTemplate
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

## Configuración de Microservicios

### 1. Dependencias en pom.xml

```xml
<!-- JWT para validación -->
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

<!-- Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

### 2. application.properties

```properties
# Configuración del servidor
server.port=8081
spring.application.name=gestion-usuario

# JWT Configuration (MISMO SECRET que API Gateway)
jwt.secret=TU_SECRET_KEY_MUY_LARGA_Y_SEGURA_AQUI_MINIMO_256_BITS

# Logging
logging.level.com.goldenburgers=DEBUG
```

### 3. Estructura de Clases Principales

#### 3.1 Services (com.goldenburgers.gestionUsuario.service)

**JwtService.java** - Validación de tokens JWT internos
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    
    // Mismos métodos que en API Gateway
    // extractUid, extractEmail, extractRolId, extractRolNombre, isTokenValid
}
```

#### 3.2 Filters (com.goldenburgers.gestionUsuario.filter)

**JwtAuthenticationFilter.java** - Validación de JWT interno
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) {
        // Rutas públicas
        String path = request.getServletPath();
        if (path.startsWith("/actuator/") || 
            path.startsWith("/health") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.equals("/swagger-ui.html") ||
            path.startsWith("/api/usuarios/firebase/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // ⚠️ IMPORTANTE: Aceptar token desde dos headers
        String internalToken = request.getHeader("X-Internal-Token");
        
        // Si no viene en X-Internal-Token, buscar en Authorization Bearer
        if (internalToken == null || internalToken.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                internalToken = authHeader.substring(7);
            }
        }
        
        if (internalToken == null || internalToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token interno requerido\"}");
            return;
        }
        
        // Validar token y establecer SecurityContext
    }
}
```

#### 3.3 Security (com.goldenburgers.gestionUsuario.security)

**CustomUserDetails.java** - Detalles del usuario
```java
public class CustomUserDetails implements UserDetails {
    private final String uid;
    private final String email;
    private final Long rolId;
    private final String rolNombre;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ⚠️ CRÍTICO: toUpperCase() para coincidir con @PreAuthorize
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase())
        );
    }
}
```

**SecurityConfig.java** - Configuración de seguridad
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuarios/firebase/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**", "/health").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
    }
}
```

#### 3.4 Config (com.goldenburgers.gestionUsuario.config)

**SwaggerConfig.java** - Configuración de Swagger UI
```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        // ⚠️ CRÍTICO: El nombre debe coincidir con @SecurityRequirement en controladores
        final String securitySchemeName = "bearer-jwt";
        
        return new OpenAPI()
            .info(new Info()
                .title("Golden Burgers - Gestión de Usuario API")
                .description("Microservicio para gestión de usuarios")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement()
                .addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token JWT interno generado por el API Gateway. " +
                        "Para obtenerlo, haz login en el API Gateway (POST /api/auth/login) " +
                        "y copia el 'internalToken' aquí (sin 'Bearer').")));
    }
}
```

#### 3.5 Controllers

**Ejemplo: TrabajadorController.java**
```java
@RestController
@RequestMapping("/api/trabajadores")
@Tag(name = "Trabajadores", description = "API para gestión de trabajadores")
@SecurityRequirement(name = "bearer-jwt") // ⚠️ Debe coincidir con SwaggerConfig
public class TrabajadorController {
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<List<TrabajadorDTO>> getAllTrabajadores() {
        // Lógica
    }
}
```

**Endpoint público para API Gateway**
```java
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    // Endpoint público para que API Gateway obtenga datos del usuario
    @GetMapping("/firebase/{uid}")
    public ResponseEntity<UserDTO> getUserByFirebaseUid(@PathVariable String uid) {
        // No requiere autenticación (usado por API Gateway)
    }
}
```

---

## Problemas Comunes y Soluciones

### Problema 1: Error 403 en Endpoints Protegidos

**Síntoma**: Endpoints protegidos retornan 403 Forbidden a pesar de tener token válido.

**Causa**: ProxyController usando `WebClient` (reactivo) que pierde el contexto de Spring Security.

**Solución**:
```java
// ❌ INCORRECTO - WebClient (reactivo)
private WebClient webClient;

Mono<ResponseEntity<String>> response = webClient.get()
    .uri(targetUrl)
    .retrieve()
    .toEntity(String.class);

// ✅ CORRECTO - RestTemplate (síncrono)
private RestTemplate restTemplate;

ResponseEntity<String> response = restTemplate.exchange(
    targetUrl,
    HttpMethod.GET,
    requestEntity,
    String.class
);
```

### Problema 2: Swagger UI No Envía Token

**Síntoma**: Error 401 `{"error": "Token interno requerido"}` al ejecutar endpoints en Swagger UI.

**Causa**: Nombre del security scheme en `SwaggerConfig` no coincide con `@SecurityRequirement` en controladores.

**Solución**:
```java
// SwaggerConfig.java
final String securitySchemeName = "bearer-jwt"; // ⚠️ Debe coincidir

// TrabajadorController.java
@SecurityRequirement(name = "bearer-jwt") // ⚠️ Mismo nombre
```

### Problema 3: Roles No Funcionan con @PreAuthorize

**Síntoma**: `@PreAuthorize("hasRole('ADMIN')")` no funciona aunque el usuario sea Admin.

**Causa**: `rolNombre` en base de datos es "Admin" pero Spring Security espera "ADMIN" (mayúsculas).

**Solución**:
```java
// CustomUserDetails.java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    // ✅ Convertir a mayúsculas
    return Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase())
    );
}
```

### Problema 4: Conflicto de Rutas Swagger en API Gateway

**Síntoma**: Swagger UI no funciona cuando se agrega ProxyController con rutas `/swagger-ui/**`.

**Causa**: API Gateway tiene dependencia de SpringDoc que crea sus propias rutas de Swagger.

**Solución**:
```xml
<!-- pom.xml del API Gateway -->
<!-- ⚠️ COMENTAR o ELIMINAR SpringDoc -->
<!--
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
-->
```

### Problema 5: Swagger UI No Recibe Token desde API Gateway

**Síntoma**: Swagger funciona en puerto directo (8081) pero no a través de API Gateway (8080).

**Causa**: JwtAuthenticationFilter solo acepta header `X-Internal-Token` pero Swagger UI envía `Authorization: Bearer`.

**Solución**:
```java
// JwtAuthenticationFilter.java en microservicio
String internalToken = request.getHeader("X-Internal-Token");

// Aceptar también desde Authorization Bearer
if (internalToken == null || internalToken.isEmpty()) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        internalToken = authHeader.substring(7);
    }
}
```

---

## Checklist para Nuevos Microservicios

Usa este checklist al agregar un nuevo microservicio al sistema:

### ✅ Configuración Inicial

- [ ] Crear proyecto Spring Boot 3.3.5 con Java 21
- [ ] Agregar dependencias: JWT, SpringDoc, Spring Security, Spring Web
- [ ] Configurar `application.properties` con puerto único
- [ ] Copiar `jwt.secret` EXACTO desde API Gateway

### ✅ Clases de Seguridad

- [ ] Crear `JwtService.java` (copiar desde GESTIONUSUARIO)
- [ ] Crear `JwtAuthenticationFilter.java` con soporte para:
  - Header `X-Internal-Token`
  - Header `Authorization: Bearer`
  - Rutas públicas: `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**`
- [ ] Crear `CustomUserDetails.java` con `rolNombre.toUpperCase()`
- [ ] Crear `SecurityConfig.java` con rutas públicas

### ✅ Swagger Configuration

- [ ] Crear `SwaggerConfig.java`
- [ ] Usar nombre `"bearer-jwt"` para security scheme
- [ ] Configurar como `SecurityScheme.Type.HTTP` con `scheme("bearer")`
- [ ] Agregar `@SecurityRequirement(name = "bearer-jwt")` en controladores

### ✅ Controllers

- [ ] Agregar `@SecurityRequirement(name = "bearer-jwt")` a nivel de clase
- [ ] Usar `@PreAuthorize` con roles en MAYÚSCULAS: `"hasRole('ADMIN')"`
- [ ] Crear endpoint público `/api/{resource}/firebase/{uid}` si el API Gateway lo necesita

### ✅ API Gateway Integration

- [ ] Agregar URL del microservicio en `application.properties` del API Gateway
- [ ] Agregar rutas proxy en `ProxyController.java`:
  ```java
  @GetMapping("/api/nuevo-recurso/**")
  public ResponseEntity<String> proxyNuevoRecurso(HttpServletRequest request)
  ```
- [ ] Agregar rutas Swagger proxy:
  ```java
  @GetMapping("/nuevo-servicio/swagger-ui/**")
  public ResponseEntity<String> proxySwaggerNuevoServicio(HttpServletRequest request)
  ```

### ✅ Testing

- [ ] Verificar health check: `curl http://localhost:{PORT}/actuator/health`
- [ ] Verificar Swagger directo: `http://localhost:{PORT}/swagger-ui.html`
- [ ] Verificar Swagger via Gateway: `http://localhost:8080/swagger-ui.html`
- [ ] Probar login: `POST http://localhost:8080/api/auth/login`
- [ ] Probar endpoints protegidos con token JWT
- [ ] Verificar roles con diferentes usuarios (Admin, Trabajador, Cliente)

### ✅ Documentación

- [ ] Actualizar `RESUMEN-COMPLETO.md` con nuevo microservicio
- [ ] Documentar endpoints en Swagger con `@Operation` y `@ApiResponse`
- [ ] Actualizar diagramas de arquitectura si es necesario

---

## Comandos Útiles

### Iniciar Servicios

```bash
# API Gateway
cd API-GATEWAY
./mvnw clean package -DskipTests
nohup java -jar target/api-gateway-0.0.1-SNAPSHOT.jar > /tmp/api-gateway.log 2>&1 &

# GESTIONUSUARIO
cd GESTIONUSUARIO
./mvnw clean package -DskipTests
nohup java -jar target/gestionUsuario-0.0.1-SNAPSHOT.jar > /tmp/gestionusuario.log 2>&1 &
```

### Verificar Servicios

```bash
# Verificar puertos
lsof -ti:8080  # API Gateway
lsof -ti:8081  # GESTIONUSUARIO

# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Ver logs
tail -f /tmp/api-gateway.log
tail -f /tmp/gestionusuario.log
```

### Detener Servicios

```bash
kill $(lsof -ti:8080)  # API Gateway
kill $(lsof -ti:8081)  # GESTIONUSUARIO
```

### Testing con curl

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "fabian.basaes@gmail.com",
    "firebaseToken": "TU_FIREBASE_TOKEN_AQUI"
  }'

# Endpoint protegido (reemplazar TOKEN con internalToken del login)
curl -X GET http://localhost:8080/api/trabajadores \
  -H "Authorization: Bearer TOKEN"

# Endpoint directo a microservicio con X-Internal-Token
curl -X GET http://localhost:8081/api/trabajadores \
  -H "X-Internal-Token: TOKEN"
```

---

## Notas Importantes

### Firebase Configuration

- Archivo `firebase-credentials.json` debe estar en `src/main/resources/` del API Gateway
- Obtener desde Firebase Console → Project Settings → Service Accounts → Generate new private key

### JWT Secret

- Usar la misma clave en API Gateway y TODOS los microservicios
- Mínimo 256 bits de longitud
- Mantener secreta (usar variables de entorno en producción)

### Puertos

- API Gateway: 8080
- GESTIONUSUARIO: 8081
- GESTIONCATALOGO: 8082 (futuro)
- GESTIONPEDIDO: 8083 (futuro)
- GESTIONVENTA: 8084 (futuro)
- GESTIONCONTACTO: 8085 (futuro)

### Roles en Base de Datos

- `Admin` → `ROLE_ADMIN`
- `Trabajador` → `ROLE_TRABAJADOR`
- `Cliente` → `ROLE_CLIENTE`

**IMPORTANTE**: Spring Security agrega prefijo "ROLE_" automáticamente y espera mayúsculas.

---

## Contacto y Soporte

Para dudas o problemas con la integración, revisar:
1. Este documento (GUIA-INTEGRACION-MICROSERVICIOS.md)
2. RESUMEN-COMPLETO.md (arquitectura general)
3. CAMBIOS-FINALES.md (cambios recientes)
4. Logs de los servicios en `/tmp/`

---

**Versión del documento**: 1.0  
**Última actualización**: Noviembre 2025  
**Autor**: Equipo Golden Burgers

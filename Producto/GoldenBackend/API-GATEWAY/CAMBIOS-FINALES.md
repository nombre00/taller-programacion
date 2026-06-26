# 🔄 CAMBIOS FINALES - Solución Problema de Dos Solicitudes

## 🐛 Problema Identificado

Al probar los endpoints protegidos, el API Gateway respondía con **403 Forbidden** a pesar de que:
- El token JWT era válido ✅
- El usuario estaba autenticado correctamente ✅
- Los logs mostraban "Usuario autenticado" ✅

**Causa raíz:** El `ProxyController` usaba `WebClient` reactivo (`Mono<ResponseEntity>`), lo que provocaba que Spring Security **perdiera el contexto de autenticación** al cambiar de hilo.

---

## ✅ Solución Implementada

### 1. Cambio de WebClient a RestTemplate

**Archivo eliminado:**
- ❌ `WebClientConfig.java` - Ya no se necesita

**Archivo agregado:**
- ✅ `RestTemplateConfig.java` - Bean de RestTemplate

**Código antes (problemático):**
```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final WebClient.Builder webClientBuilder;

    @RequestMapping(value = "/api/usuarios/**", ...)
    public Mono<ResponseEntity<Object>> proxyUsuarios(...) {
        return forwardRequest(...).block(); // ❌ Pierde contexto
    }

    private Mono<ResponseEntity<Object>> forwardRequest(...) {
        WebClient webClient = webClientBuilder.baseUrl(microserviceUrl).build();
        return webClient.method(method).uri(targetPath)...
    }
}
```

**Código después (funcional):**
```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final RestTemplate restTemplate; // ✅ Síncrono

    @RequestMapping(value = "/api/usuarios/**", ...)
    public ResponseEntity<String> proxyUsuarios(...) {
        return forwardRequest(...); // ✅ Mantiene contexto
    }

    private ResponseEntity<String> forwardRequest(...) {
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(URI.create(targetUrl), method, entity, String.class);
    }
}
```

---

## 🔍 ¿Por qué funcionó?

### Problema con WebClient (Reactivo):
```
Request inicial → Hilo A → JwtAuthenticationFilter ✅ autentica
                ↓
Mono.block() → Hilo B → ProxyController ❌ sin contexto
                ↓
Spring Security rechaza con 403
```

### Solución con RestTemplate (Síncrono):
```
Request inicial → Hilo A → JwtAuthenticationFilter ✅ autentica
                ↓
                Hilo A → ProxyController ✅ mantiene contexto
                ↓
Request exitosa 200 OK
```

---

## 📝 Cambios en GESTIONUSUARIO

Para que el API Gateway pueda consultar el rol durante el login, se hicieron estos cambios:

### SecurityConfig.java
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/**").permitAll()
    .requestMatchers("/api/usuarios/firebase/**").permitAll() // ✅ PÚBLICO
    .anyRequest().authenticated()
)
```

### JwtAuthenticationFilter.java
```java
String path = request.getServletPath();
if (path.startsWith("/actuator/") || 
    path.startsWith("/health") ||
    path.startsWith("/api/usuarios/firebase/")) { // ✅ No requiere token
    filterChain.doFilter(request, response);
    return;
}
```

---

## 🧪 Resultados de Testing

### ✅ Endpoints Probados con Postman:

1. **Login con Firebase** ✅
   ```
   POST /api/auth/login
   → Response: { internalToken, user { uid, email, rolId, rolNombre }, expiresIn }
   ```

2. **Lista de Roles** ✅
   ```
   GET /api/roles
   Authorization: Bearer {token}
   → Response: [{ idRol: 1, nombreRol: "Admin" }, ...]
   ```

3. **Usuario por UID** ✅
   ```
   GET /api/usuarios/firebase/{uid}
   Authorization: Bearer {token}
   → Response: { rolId, firebaseUid, email, rolNombre }
   ```

---

## 📦 Archivos Finales del API Gateway

### Usados ✅
1. DTOs: `LoginRequest`, `LoginResponse`, `UserDTO`, `RefreshTokenRequest`
2. Servicios: `JwtService`, `AuthenticationService`
3. Filtros: `JwtAuthenticationFilter`
4. Controladores: `AuthController`, `ProxyController`
5. Configuración: `SecurityConfig`, `FirebaseConfig`, `RestTemplateConfig`
6. Seguridad: `CustomUserDetails`
7. Propiedades: `application.properties`, `application-prod.properties`
8. Recursos: `firebase-credentials.json`

### Eliminados ❌
- `WebClientConfig.java` - No se usa porque cambiamos a RestTemplate

### AuthenticationService - Nota
- Todavía usa `WebClient.Builder` para consultar GESTIONUSUARIO durante login
- Funciona correctamente porque usa `.block()` en un contexto síncrono
- Podría cambiarse a RestTemplate en el futuro si se desea unificar todo

---

## 🎯 Conclusión

El cambio de **WebClient reactivo a RestTemplate síncrono** resolvió completamente el problema de contexto de seguridad. Ahora:

✅ La autenticación funciona correctamente
✅ El proxy reenvía requests sin perder el contexto
✅ Los microservicios reciben el header `X-Internal-Token`
✅ Todo el flujo end-to-end está probado y funcionando

**Fecha:** 8 de noviembre de 2025
**Estado:** ✅ COMPLETADO Y FUNCIONANDO

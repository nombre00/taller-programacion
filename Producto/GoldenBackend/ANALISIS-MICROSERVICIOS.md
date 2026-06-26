# 📊 Análisis de Microservicios - Estado Actual vs Requerido

## 🎯 Comparación con GESTIONUSUARIO (Referencia)

GESTIONUSUARIO está **100% integrado** con API Gateway:
- ✅ JWT Authentication (sin Firebase local)
- ✅ JwtAuthenticationFilter
- ✅ CustomUserDetails
- ✅ SecurityConfig
- ✅ Swagger con Bearer Auth
- ✅ Acepta tokens desde `X-Internal-Token` y `Authorization: Bearer`
- ✅ Oracle Cloud DB con Wallet
- ✅ Puerto 8081

---

## 📋 ANÁLISIS DETALLADO

### 1️⃣ GESTIONCONTACTO (Puerto: No configurado)

#### ✅ **Lo que tiene:**
- Spring Boot 3.4.1, Java 21
- Oracle Cloud DB con Wallet configurado
- SpringDoc OpenAPI 2.3.0 (Swagger)
- Spring Actuator
- JPA + Validation

#### ❌ **Lo que le falta:**
1. **Puerto** - No configurado en application.properties
2. **JWT** - No tiene dependencias JWT
3. **Spring Security** - No configurado
4. **Filtros de autenticación** - Ninguno
5. **JwtService** - No existe
6. **CustomUserDetails** - No existe
7. **SecurityConfig** - No existe
8. **SwaggerConfig** - No configurado para Bearer Auth
9. **Protección de endpoints** - No hay @PreAuthorize
10. **Firebase Admin SDK** - No tiene (CORRECTO, no debería tenerlo)

#### ⚠️ **Problemas adicionales:**
- `application.properties` casi vacío (solo tiene nombre de app)
- Falta configuración de BD completa
- Controllers sin seguridad

#### 🔧 **Puertos sugeridos:**
- **8085** (para mantener consistencia)

---

### 2️⃣ GESTIONPEDIDO (Puerto: 8083)

#### ✅ **Lo que tiene:**
- Spring Boot 3.5.7, Java 21
- Puerto configurado: 8083 ✅
- Oracle Cloud DB con Wallet
- SpringDoc OpenAPI 2.3.0 (Swagger)
- JWT dependencies (jjwt 0.12.3) ✅
- Lombok

#### ❌ **Lo que le falta (CRÍTICO):**
1. **Firebase Admin SDK** - LO TIENE PERO NO DEBERÍA ❌
   - `firebase-admin` version 9.2.0
   - Debe ser **ELIMINADO** (solo el API Gateway debe tenerlo)
2. **Spring Security** - No configurado
3. **JwtService** - No existe (a pesar de tener las dependencias)
4. **JwtAuthenticationFilter** - No existe
5. **CustomUserDetails** - No existe
6. **SecurityConfig** - No existe
7. **SwaggerConfig** - No configurado para Bearer Auth
8. **Protección de endpoints** - No hay @PreAuthorize

#### ⚠️ **Problemas adicionales:**
- Tiene dependencias JWT pero no las usa
- Tiene Firebase pero no debería (arquitectura incorrecta)
- URL hardcodeada en application.properties (debe usar variables)
- Credenciales expuestas

#### 🔧 **Acción inmediata:**
- **ELIMINAR** Firebase Admin SDK del pom.xml
- Implementar JWT validation (no Firebase)

---

### 3️⃣ GESTIONVENTA (Puerto: 8082)

#### ✅ **Lo que tiene:**
- Spring Boot 3.5.7, Java 21
- Puerto configurado: 8082 ✅
- Oracle Cloud DB con Wallet
- SpringDoc OpenAPI 2.3.0 (Swagger)
- JWT dependencies (jjwt 0.12.3) ✅
- Lombok

#### ❌ **Lo que le falta (CRÍTICO):**
1. **Firebase Admin SDK** - LO TIENE PERO NO DEBERÍA ❌
   - `firebase-admin` version 9.2.0
   - Debe ser **ELIMINADO** (solo el API Gateway debe tenerlo)
2. **Spring Security** - No configurado
3. **JwtService** - No existe (a pesar de tener las dependencias)
4. **JwtAuthenticationFilter** - No existe
5. **CustomUserDetails** - No existe
6. **SecurityConfig** - No existe
7. **SwaggerConfig** - No configurado para Bearer Auth
8. **Protección de endpoints** - No hay @PreAuthorize
9. **Controllers** - 4 controllers sin seguridad

#### ⚠️ **Problemas adicionales:**
- Tiene dependencias JWT pero no las usa
- Tiene Firebase pero no debería (arquitectura incorrecta)
- URL hardcodeada en application.properties
- Credenciales expuestas

#### 🔧 **Acción inmediata:**
- **ELIMINAR** Firebase Admin SDK del pom.xml
- Implementar JWT validation (no Firebase)

---

## 📊 TABLA COMPARATIVA

| Característica | GESTIONUSUARIO | GESTIONCONTACTO | GESTIONPEDIDO | GESTIONVENTA |
|---|:---:|:---:|:---:|:---:|
| **Puerto configurado** | ✅ 8081 | ❌ | ✅ 8083 | ✅ 8082 |
| **Oracle DB + Wallet** | ✅ | ⚠️ Parcial | ✅ | ✅ |
| **Swagger** | ✅ | ✅ | ✅ | ✅ |
| **JWT Dependencies** | ✅ | ❌ | ✅ | ✅ |
| **Spring Security** | ✅ | ❌ | ❌ | ❌ |
| **JwtService** | ✅ | ❌ | ❌ | ❌ |
| **JwtAuthenticationFilter** | ✅ | ❌ | ❌ | ❌ |
| **CustomUserDetails** | ✅ | ❌ | ❌ | ❌ |
| **SecurityConfig** | ✅ | ❌ | ❌ | ❌ |
| **SwaggerConfig** | ✅ | ❌ | ❌ | ❌ |
| **@PreAuthorize** | ✅ | ❌ | ❌ | ❌ |
| **Firebase Admin SDK** | ❌ (correcto) | ❌ (correcto) | ⚠️ **SÍ** (incorrecto) | ⚠️ **SÍ** (incorrecto) |
| **Estado** | ✅ Completo | ❌ Básico | ⚠️ Parcial | ⚠️ Parcial |

---

## 🚨 PROBLEMAS CRÍTICOS ENCONTRADOS

### 1. **Firebase en microservicios (GESTIONPEDIDO, GESTIONVENTA)**
**Problema:** Tienen Firebase Admin SDK en sus pom.xml

**Por qué es malo:**
- Violación de la arquitectura de API Gateway
- Cada microservicio tendría que validar tokens de Firebase directamente
- Duplicación de lógica de autenticación
- Múltiples puntos de falla
- Firebase solo debe estar en API Gateway

**Solución:**
```xml
<!-- ELIMINAR de pom.xml de GESTIONPEDIDO y GESTIONVENTA -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

### 2. **Dependencias JWT sin implementación**
**Problema:** GESTIONPEDIDO y GESTIONVENTA tienen JWT dependencies pero no las usan

**Solución:** Implementar JwtService, JwtAuthenticationFilter, etc. (copiar de GESTIONUSUARIO)

### 3. **Sin protección de endpoints**
**Problema:** Todos los endpoints son públicos

**Riesgo:**
- Cualquiera puede crear, modificar o eliminar pedidos/ventas
- No hay control de acceso por roles
- Datos sensibles expuestos

---

## 📝 PLAN DE ACCIÓN POR MICROSERVICIO

### 🟥 **GESTIONCONTACTO - Prioridad ALTA**

**Estado:** Básico, requiere configuración completa

**Tareas:**
1. ✅ Configurar puerto en `application.properties` (sugerir 8085)
2. ✅ Completar configuración de Oracle DB
3. ❌ Agregar dependencias JWT al pom.xml
4. ❌ Crear JwtService (copiar de GESTIONUSUARIO)
5. ❌ Crear JwtAuthenticationFilter (copiar de GESTIONUSUARIO)
6. ❌ Crear CustomUserDetails (copiar de GESTIONUSUARIO)
7. ❌ Crear SecurityConfig
8. ❌ Crear SwaggerConfig con Bearer Auth
9. ❌ Agregar @PreAuthorize a controllers
10. ✅ Agregar rutas al ProxyController del API Gateway

**Tiempo estimado:** 2-3 horas

---

### 🟨 **GESTIONPEDIDO - Prioridad MEDIA**

**Estado:** Parcial, tiene JWT pero sin implementar

**Tareas:**
1. ⚠️ **ELIMINAR** Firebase Admin SDK del pom.xml
2. ❌ Crear JwtService (copiar de GESTIONUSUARIO)
3. ❌ Crear JwtAuthenticationFilter (copiar de GESTIONUSUARIO)
4. ❌ Crear CustomUserDetails (copiar de GESTIONUSUARIO)
5. ❌ Crear SecurityConfig
6. ❌ Crear SwaggerConfig con Bearer Auth
7. ❌ Agregar @PreAuthorize a PedidoController
8. ✅ Actualizar application.properties (credenciales como variables)
9. ✅ Agregar rutas al ProxyController del API Gateway

**Tiempo estimado:** 2 horas (ya tiene JWT dependencies)

---

### 🟨 **GESTIONVENTA - Prioridad MEDIA**

**Estado:** Parcial, tiene JWT pero sin implementar

**Tareas:**
1. ⚠️ **ELIMINAR** Firebase Admin SDK del pom.xml
2. ❌ Crear JwtService (copiar de GESTIONUSUARIO)
3. ❌ Crear JwtAuthenticationFilter (copiar de GESTIONUSUARIO)
4. ❌ Crear CustomUserDetails (copiar de GESTIONUSUARIO)
5. ❌ Crear SecurityConfig
6. ❌ Crear SwaggerConfig con Bearer Auth
7. ❌ Agregar @PreAuthorize a todos los controllers (4 controllers)
8. ✅ Actualizar application.properties (credenciales como variables)
9. ✅ Agregar rutas al ProxyController del API Gateway

**Tiempo estimado:** 2-3 horas (4 controllers)

---

## 🎯 ORDEN RECOMENDADO DE INTEGRACIÓN

### Fase 1: Limpieza (URGENTE)
1. **GESTIONPEDIDO** - Eliminar Firebase
2. **GESTIONVENTA** - Eliminar Firebase

### Fase 2: Configuración Base
3. **GESTIONCONTACTO** - Configurar puerto y DB
4. **GESTIONPEDIDO** - Configurar variables de entorno
5. **GESTIONVENTA** - Configurar variables de entorno

### Fase 3: Implementación JWT
6. **GESTIONCONTACTO** - Implementar toda la seguridad (más trabajo)
7. **GESTIONPEDIDO** - Implementar seguridad (ya tiene deps)
8. **GESTIONVENTA** - Implementar seguridad (ya tiene deps)

### Fase 4: Integración API Gateway
9. Agregar URLs de microservicios al API Gateway
10. Agregar rutas proxy en ProxyController
11. Probar cada microservicio individualmente
12. Probar flujo completo end-to-end

---

## 📚 ARCHIVOS A COPIAR DE GESTIONUSUARIO

Para cada microservicio, copiar y adaptar estos archivos:

### 1. **JwtService.java**
```
GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/service/JwtService.java
→ Copiar a cada microservicio (mismo código, mismo jwt.secret)
```

### 2. **JwtAuthenticationFilter.java**
```
GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/filter/JwtAuthenticationFilter.java
→ Copiar y ajustar rutas públicas
```

### 3. **CustomUserDetails.java**
```
GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/security/CustomUserDetails.java
→ Copiar sin cambios
```

### 4. **SecurityConfig.java**
```
GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/config/SecurityConfig.java
→ Copiar y ajustar rutas públicas
```

### 5. **SwaggerConfig.java**
```
GESTIONUSUARIO/src/main/java/com/goldenburgers/gestionUsuario/config/SwaggerConfig.java
→ Copiar y ajustar info (título, descripción)
```

### 6. **application.properties** (fragmento JWT)
```properties
# JWT Configuration (MISMO SECRET que API Gateway)
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

---

## 🔧 CHECKLIST RÁPIDO

Para integrar cada microservicio, usar este checklist de `GUIA-INTEGRACION-MICROSERVICIOS.md`:

- [ ] Puerto configurado y único
- [ ] Oracle DB configurado con Wallet
- [ ] JWT dependencies en pom.xml
- [ ] JwtService creado
- [ ] JwtAuthenticationFilter creado
- [ ] CustomUserDetails creado
- [ ] SecurityConfig creado
- [ ] SwaggerConfig con Bearer Auth
- [ ] @PreAuthorize en controllers
- [ ] @SecurityRequirement(name = "bearer-jwt") en controllers
- [ ] jwt.secret configurado (mismo que API Gateway)
- [ ] Firebase ELIMINADO (si existe)
- [ ] URL agregada al API Gateway
- [ ] Rutas proxy agregadas al ProxyController
- [ ] Health check funciona
- [ ] Swagger UI accesible
- [ ] Endpoints protegidos funcionan con JWT

---

## 📊 RESUMEN EJECUTIVO

| Microservicio | Estado | Trabajo Requerido | Tiempo Estimado | Prioridad |
|---|---|---|---|---|
| GESTIONUSUARIO | ✅ **Completo** | Ninguno | - | - |
| GESTIONCONTACTO | 🔴 **Básico** | Alto | 2-3 horas | ALTA |
| GESTIONPEDIDO | 🟡 **Parcial** | Medio | 2 horas | MEDIA |
| GESTIONVENTA | 🟡 **Parcial** | Medio | 2-3 horas | MEDIA |

**Total estimado:** 6-8 horas de trabajo para integrar los 3 microservicios.

---

## 🚀 PRÓXIMOS PASOS

1. **Revisar este análisis** con el equipo
2. **Decidir orden** de integración (recomendado arriba)
3. **Asignar responsables** para cada microservicio
4. **Usar** `GUIA-INTEGRACION-MICROSERVICIOS.md` como referencia
5. **Seguir** el checklist paso a paso
6. **Probar** cada microservicio antes de continuar
7. **Documentar** cualquier desviación o problema

---

**Documento creado:** Noviembre 2025  
**Basado en:** GESTIONUSUARIO (referencia completa)  
**Herramientas:** Spring Boot 3.x, JWT, Oracle Cloud, Swagger

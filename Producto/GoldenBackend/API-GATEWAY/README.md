# API Gateway - Golden Burgers 

## ✅ Estado del Proyecto

**PROYECTO COMPLETO Y COMPILANDO CORRECTAMENTE**

### Archivos Creados:

#### DTOs (Data Transfer Objects)
- ✅ `LoginRequest.java` - Request para autenticación
- ✅ `LoginResponse.java` - Response con token y datos de usuario
- ✅ `UserDTO.java` - Información del usuario
- ✅ `RefreshTokenRequest.java` - Request para refresh de token

#### Servicios
- ✅ `JwtService.java` - Generación y validación de tokens JWT internos
- ✅ `AuthenticationService.java` - Lógica de autenticación con Firebase

#### Filtros de Seguridad
- ✅ `JwtAuthenticationFilter.java` - Validación de tokens JWT en requests

#### Controladores
- ✅ `AuthController.java` - Endpoints de autenticación (`/api/auth/login`, `/api/auth/refresh`)
- ✅ `ProxyController.java` - Proxy para reenviar requests a microservicios

#### Configuración
- ✅ `SecurityConfig.java` - Configuración de Spring Security
- ✅ `WebClientConfig.java` - Configuración de WebClient
- ✅ `FirebaseConfig.java` - Configuración de Firebase Admin SDK
- ✅ `CustomUserDetails.java` - Detalles del usuario autenticado

#### Properties
- ✅ `application.properties` - Configuración de desarrollo
- ✅ `application-prod.properties` - Configuración de producción

---

## 🎯 Arquitectura del API Gateway

### Flujo de Autenticación

```
1. Cliente (React) → Login con Firebase → Obtiene firebaseToken
2. Cliente → POST /api/auth/login con firebaseToken
3. API Gateway valida el token con Firebase
4. API Gateway consulta el rol del usuario a GESTIONUSUARIO
5. API Gateway genera un token JWT interno
6. Cliente recibe token JWT interno + datos del usuario
7. Cliente usa token JWT interno para todas las demás requests
```

### Flujo de Proxy

```
1. Cliente → Request con token JWT interno → API Gateway
2. API Gateway valida el token JWT
3. API Gateway reenvía la request al microservicio correspondiente
4. Microservicio procesa y responde
5. API Gateway reenvía la respuesta al cliente
```

---

## 📋 Configuración Necesaria

### 1. Firebase Setup (OBLIGATORIO)

Necesitas obtener las credenciales de Firebase:

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Configuración del proyecto** > **Cuentas de servicio**
4. Clic en **"Generar nueva clave privada"**
5. Descarga el archivo JSON

**Instalar las credenciales:**

```bash
# Copiar el archivo descargado como:
cp ~/Downloads/firebase-credentials-XXXXX.json \
   src/main/resources/firebase-credentials.json
```

⚠️ **IMPORTANTE**: El archivo `firebase-credentials.json` NO se subirá a Git (está en `.gitignore`)

### 2. Configurar JWT Secret

Para desarrollo local, puedes usar el secret por defecto. Para producción:

```bash
# Generar una clave segura
openssl rand -base64 32

# Usar como variable de entorno
export JWT_SECRET="tu_clave_generada"
```

### 3. URLs de Microservicios

Asegúrate de que estos microservicios estén ejecutándose en sus puertos:

- **GESTIONUSUARIO**: http://localhost:8081
- **GESTIONPRODUCTO**: http://localhost:8082
- **GESTIONVENTA**: http://localhost:8083
- **GESTIONCATALOGO**: http://localhost:8084
- **GESTIONCONTACTO**: http://localhost:8085

---

## 🚀 Ejecución

### Desarrollo Local

```bash
# 1. Compilar
mvn clean compile

# 2. Ejecutar
mvn spring-boot:run

# O ejecutar el JAR
mvn package
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

El API Gateway estará disponible en: **http://localhost:8080**

### Producción (Oracle Cloud VM)

```bash
# 1. Compilar con perfil de producción
mvn clean package -Pprod -DskipTests

# 2. Subir a la VM
scp target/api-gateway-0.0.1-SNAPSHOT.jar opc@TU_IP_VM:/home/opc/api-gateway/

# 3. Subir credenciales de Firebase
scp src/main/resources/firebase-credentials.json opc@TU_IP_VM:/home/opc/

# 4. Ejecutar en la VM
ssh opc@TU_IP_VM
export JWT_SECRET="tu_clave_secreta"
java -Xmx6g -Dspring.profiles.active=prod \
     -jar /home/opc/api-gateway/api-gateway-0.0.1-SNAPSHOT.jar
```

---

## 📡 Endpoints del API Gateway

### Autenticación (Públicos)

#### POST /api/auth/login
Autentica con Firebase y retorna token JWT interno.

**Request:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE3..."
}
```

**Response:**
```json
{
  "internalToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "uid": "firebase_uid_123",
    "email": "usuario@example.com",
    "nombre": "Juan Pérez",
    "rolId": 3,
    "rolNombre": "CLIENTE"
  },
  "expiresIn": 86400000
}
```

#### POST /api/auth/refresh
Refresca el token JWT interno.

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Rutas Proxy (Protegidas - Requieren token JWT)

Todas las demás rutas son reenviadas a los microservicios:

- `/api/usuarios/**` → GESTIONUSUARIO:8081
- `/api/clientes/**` → GESTIONUSUARIO:8081
- `/api/trabajadores/**` → GESTIONUSUARIO:8081
- `/api/productos/**` → GESTIONPRODUCTO:8082
- `/api/ventas/**` → GESTIONVENTA:8083
- `/api/catalogo/**` → GESTIONCATALOGO:8084
- `/api/contacto/**` → GESTIONCONTACTO:8085

**Header requerido:**
```
Authorization: Bearer <tu_token_jwt_interno>
```

---

## 🧪 Probar con Postman

### 1. Login

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "firebaseToken": "TU_FIREBASE_TOKEN_AQUI"
}
```

### 2. Usar el token interno

```
GET http://localhost:8080/api/usuarios/1
Authorization: Bearer <internal_token_recibido>
```

---

## 🔧 Próximos Pasos

### Para GESTIONUSUARIO y otros microservicios:

1. **Crear endpoint para consultar usuario por UID de Firebase:**
   ```java
   @GetMapping("/api/usuarios/firebase/{uid}")
   public UserDTO getUserByFirebaseUid(@PathVariable String uid)
   ```

2. **Agregar dependencias JWT** al `pom.xml`

3. **Crear `JwtAuthenticationFilter`** para validar tokens JWT internos

4. **Modificar `SecurityConfig`** para usar el nuevo filtro

5. **Eliminar Firebase** de los microservicios (solo en API Gateway)

---

## ⚙️ Configuración para Producción (Oracle Cloud)

### Preparar la VM

1. **Abrir puerto 8080 en Security List:**
   - Oracle Cloud Console → Networking → Security Lists
   - Agregar Ingress Rule: TCP, puerto 8080, source 0.0.0.0/0

2. **Configurar firewall en la VM:**
   ```bash
   sudo firewall-cmd --permanent --add-port=8080/tcp
   sudo firewall-cmd --reload
   ```

3. **Crear carpetas:**
   ```bash
   mkdir -p /home/opc/api-gateway
   mkdir -p /home/opc/logs
   ```

4. **Subir archivos:**
   ```bash
   # Desde tu máquina local
   scp target/api-gateway-0.0.1-SNAPSHOT.jar opc@IP_VM:/home/opc/api-gateway/
   scp firebase-credentials.json opc@IP_VM:/home/opc/
   ```

5. **Crear script de inicio:**
   ```bash
   nano /home/opc/start-gateway.sh
   ```

   ```bash
   #!/bin/bash
   export JWT_SECRET="tu_clave_secreta_super_segura"
   
   cd /home/opc/api-gateway
   nohup java -Xms2g -Xmx6g \
     -Dspring.profiles.active=prod \
     -jar api-gateway-0.0.1-SNAPSHOT.jar \
     > /home/opc/logs/gateway.log 2>&1 &
   
   echo "API Gateway iniciado. PID: $!"
   ```

   ```bash
   chmod +x /home/opc/start-gateway.sh
   ```

6. **Ejecutar:**
   ```bash
   ./start-gateway.sh
   
   # Ver logs
   tail -f /home/opc/logs/gateway.log
   ```

---

## 🐛 Troubleshooting

### Error: "Firebase credentials not found"
- Verifica que el archivo `firebase-credentials.json` exista en `src/main/resources/`
- En producción, verifica la ruta `/home/opc/firebase-credentials.json`

### Error: "Cannot connect to microservice"
- Verifica que el microservicio esté ejecutándose en el puerto correcto
- Revisa los logs del microservicio
- Prueba hacer curl directo: `curl http://localhost:8081/actuator/health`

### Error: "JWT parsing error"
- Verifica que estás usando el mismo `jwt.secret` en todos los servicios
- Genera una nueva clave con: `openssl rand -base64 32`

### Error de CORS
- Verifica que el origen de tu frontend esté en `SecurityConfig.corsConfigurationSource()`
- En desarrollo, está configurado para permitir todo (`*`)

---

## 📚 Documentación

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## 🔐 Seguridad

### En Desarrollo:
- CORS permite todos los orígenes (`*`)
- JWT secret tiene valor por defecto

### En Producción (CAMBIAR):
1. Actualizar orígenes CORS en `SecurityConfig`
2. Usar variable de entorno para `JWT_SECRET`
3. Cambiar `allowCredentials` a `true` si usas cookies
4. Configurar HTTPS
5. Limitar endpoints de Actuator

---

## 📝 Notas Importantes

- El API Gateway NO guarda el password de Firebase
- Los tokens JWT internos expiran en 24 horas (configurable)
- Cada request proxy agrega el header `X-Internal-Token` para los microservicios
- Los microservicios NO deben aceptar requests directas (solo desde API Gateway)

---

## 👨‍💻 Equipo de Desarrollo

Golden Burgers - Proyecto Final
DUOC UC - Cuarto Semestre Fullstack
Noviembre 2025
# Archivos para Compartir con el Equipo

## 🚨 Archivos NO están en Git (por seguridad)

Estos archivos **NO** se subieron a Git porque contienen información sensible (contraseñas, credenciales, etc.). Tu compañero los necesita para que el proyecto funcione en su máquina.

---

## 📦 Archivos que DEBES compartir

### 1. Oracle Wallet (Credenciales de Base de Datos)

**¿Qué es?** Archivos que permiten conectarse a la base de datos Oracle Autonomous Database.

**Ubicaciones en tu proyecto:**
```
wallet/                           ← Wallet principal (raíz)
└── cwallet.sso
└── ewallet.p12
└── ewallet.pem
└── keystore.jks
└── ojdbc.properties
└── README
└── sqlnet.ora
└── tnsnames.ora
└── truststore.jks

GESTIONUSUARIO/wallet/            ← Wallet específico de GESTIONUSUARIO
└── (mismos archivos)
```

**¿Cómo compartir?**

Opción A - **Comprimir y enviar por Drive/OneDrive** (RECOMENDADO):
```bash
# Crear ZIP con password
zip -r -e wallet-goldenburgersdb.zip wallet/ GESTIONUSUARIO/wallet/

# Te pedirá una contraseña: usa algo como "goldenburgers2025"
# Compartir el ZIP por Google Drive + enviar la contraseña por WhatsApp
```

Opción B - **USB o compartir en persona**:
```bash
# Copiar a USB
cp -r wallet/ /Volumes/USB/
cp -r GESTIONUSUARIO/wallet/ /Volumes/USB/
```

**¿Dónde debe colocarlo tu compañero?**
```
Su proyecto/
├── wallet/                    ← Copiar aquí (raíz del proyecto)
│   ├── cwallet.sso
│   ├── ewallet.p12
│   └── ... (todos los archivos)
│
└── GESTIONUSUARIO/
    └── wallet/                ← Copiar aquí también
        ├── cwallet.sso
        └── ... (todos los archivos)
```

---

### 2. Firebase Credentials (Autenticación y Storage)

**¿Qué es?** Archivo JSON con credenciales para Firebase (autenticación y almacenamiento de imágenes).

**Ubicaciones en tu proyecto:**
```
API-GATEWAY/src/main/resources/firebase-credentials.json
GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/firebase-credentials.json
```

**¿Cómo compartir?**

⚠️ **IMPORTANTE**: Este archivo es **MUY SENSIBLE**. NO compartirlo por chat público.

```bash
# Opción 1: Comprimir con password
zip -e firebase-credentials.zip \
  API-GATEWAY/src/main/resources/firebase-credentials.json \
  GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/firebase-credentials.json

# Opción 2: Enviar por email encriptado
```

**¿Dónde debe colocarlo tu compañero?**
```
API-GATEWAY/src/main/resources/
└── firebase-credentials.json         ← Copiar aquí

GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/
└── firebase-credentials.json         ← Copiar aquí
```

---

### 3. Application Properties (Configuración de cada microservicio)

**¿Qué es?** Archivos de configuración con:
- URLs de base de datos
- Contraseñas
- JWT Secret
- Configuración de puertos

**Ubicaciones en tu proyecto:**
```
API-GATEWAY/src/main/resources/application.properties
GESTIONUSUARIO/src/main/resources/application.properties
GESTIONVENTA/Microservicio-Gestion-Venta/src/main/resources/application.properties
GESTIONPEDIDO/GestionPedidos/src/main/resources/application.properties
GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/application.properties
GESTIONCONTACTO/src/main/resources/application.properties
```

**¿Cómo compartir?**

**OPCIÓN RECOMENDADA**: Crear versión "template" sin datos sensibles expuestos

Ejecuta este comando para crear templates:

```bash
# Ir a la raíz del proyecto
cd /Users/Basaes/Desktop/Desktop/Progra/DUOC/CuartoSemestre/Fullstack/proyectofinal/backGoldenBurgers

# Crear templates automáticamente
for file in $(find . -name "application.properties" -not -path "*/target/*"); do
  cp "$file" "${file}.template"
done
```

Luego **editar cada `.template`** y reemplazar valores sensibles:

```properties
# ANTES (application.properties - NO compartir así)
spring.datasource.password=goldenBurgers.01
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# DESPUÉS (application.properties.template - SÍ compartir)
spring.datasource.password=${DB_PASSWORD:REEMPLAZAR_CON_PASSWORD}
jwt.secret=${JWT_SECRET:REEMPLAZAR_CON_SECRET}
```

**¿Dónde debe colocarlo tu compañero?**

Tu compañero debe:
1. Copiar cada `.template` 
2. Renombrar quitando `.template`
3. Reemplazar valores placeholders con los reales

```bash
# Ejemplo en su proyecto:
cp API-GATEWAY/src/main/resources/application.properties.template \
   API-GATEWAY/src/main/resources/application.properties

# Editar y poner valores reales
nano API-GATEWAY/src/main/resources/application.properties
```

---

## 📋 Datos Sensibles que Compartir (Por Canal Seguro)

Compartir estos valores **por WhatsApp, Signal o en persona**. NO por email o Slack.

### 🔐 Credenciales de Base de Datos

```
URL: jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=./wallet
Username: GOLDENBURGERSDB
Password: goldenBurgers.01
```

### 🔑 JWT Secret (Todos los microservicios)

```
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### 🔥 Firebase (Si tiene acceso a Firebase Console)

**Opción A**: Tu compañero descarga el archivo desde Firebase Console
1. Ir a: https://console.firebase.google.com/
2. Proyecto Golden Burgers
3. Configuración del proyecto → Cuentas de servicio
4. Generar nueva clave privada
5. Guardar como `firebase-credentials.json`

**Opción B**: Compartir tu archivo (menos seguro)

---

## 📦 Paquete Completo para Compartir

### Paso 1: Crear paquete comprimido

```bash
# En tu máquina, crear carpeta temporal
mkdir ~/golden-burgers-setup
cd ~/golden-burgers-setup

# Copiar archivos necesarios
mkdir -p wallet
mkdir -p firebase

# Copiar wallets
cp -r /Users/Basaes/Desktop/Desktop/Progra/DUOC/CuartoSemestre/Fullstack/proyectofinal/backGoldenBurgers/wallet/* wallet/

# Copiar firebase credentials
cp /Users/Basaes/Desktop/Desktop/Progra/DUOC/CuartoSemestre/Fullstack/proyectofinal/backGoldenBurgers/API-GATEWAY/src/main/resources/firebase-credentials.json firebase/

# Crear archivo con instrucciones
cat > INSTRUCCIONES.txt << 'EOF'
CONFIGURACIÓN PROYECTO GOLDEN BURGERS
=====================================

1. WALLET (Base de Datos Oracle)
   - Copiar carpeta "wallet/" a la raíz del proyecto
   - Copiar carpeta "wallet/" a GESTIONUSUARIO/wallet/

2. FIREBASE CREDENTIALS
   - Copiar "firebase-credentials.json" a:
     * API-GATEWAY/src/main/resources/
     * GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/

3. APPLICATION.PROPERTIES
   - Crear archivo application.properties en cada microservicio
   - Usar los templates como base
   - Agregar estos valores:

   Database Password: goldenBurgers.01
   Database Username: GOLDENBURGERSDB
   JWT Secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

4. COMPILAR
   cd API-GATEWAY && ./mvnw clean package -DskipTests
   cd GESTIONUSUARIO && ./mvnw clean package -DskipTests
   (repetir para cada microservicio)

5. EJECUTAR
   Usar el script start-services.sh en la raíz del proyecto

¿PROBLEMAS?
- Verificar que wallet/ está en la ubicación correcta
- Verificar que firebase-credentials.json existe
- Verificar que application.properties tiene todos los valores
EOF

# Comprimir todo con password
zip -r -e golden-burgers-setup.zip wallet/ firebase/ INSTRUCCIONES.txt

# Te pedirá password: usa "goldenburgers2025"
```

### Paso 2: Compartir el archivo

1. **Subir a Google Drive**:
   - Subir `golden-burgers-setup.zip`
   - Compartir link solo con tu compañero

2. **Enviar password por WhatsApp**:
   ```
   Password del ZIP: goldenburgers2025
   ```

---

## 📝 Instrucciones para tu Compañero

### Configuración Inicial (Primera vez)

```bash
# 1. Clonar el repositorio
git clone https://github.com/Fblink88/backGoldenBurgers.git
cd backGoldenBurgers

# 2. Descomprimir el archivo que te compartí
unzip ~/Downloads/golden-burgers-setup.zip -d ~/golden-burgers-config

# 3. Copiar wallet a la raíz del proyecto
cp -r ~/golden-burgers-config/wallet ./

# 4. Copiar wallet a GESTIONUSUARIO
cp -r ~/golden-burgers-config/wallet GESTIONUSUARIO/

# 5. Copiar firebase credentials
cp ~/golden-burgers-config/firebase/firebase-credentials.json \
   API-GATEWAY/src/main/resources/

cp ~/golden-burgers-config/firebase/firebase-credentials.json \
   GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/

# 6. Crear application.properties en cada microservicio
# (Ver sección siguiente)
```

### Crear Application Properties

Para cada microservicio, crear `application.properties`:

#### API-GATEWAY/src/main/resources/application.properties

```properties
server.port=8080
spring.application.name=api-gateway

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=./wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Firebase
firebase.credentials.path=src/main/resources/firebase-credentials.json
```

#### GESTIONUSUARIO/src/main/resources/application.properties

```properties
server.port=8081
spring.application.name=gestionUsuario

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=./wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

#### GESTIONVENTA/Microservicio-Gestion-Venta/src/main/resources/application.properties

```properties
server.port=8082
spring.application.name=gestion-venta

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=../../wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

#### GESTIONPEDIDO/GestionPedidos/src/main/resources/application.properties

```properties
server.port=8083
spring.application.name=gestion-pedido

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=../../wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

#### GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/application.properties

```properties
server.port=8084
spring.application.name=gestion-catalogo

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=../../wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Firebase
firebase.credentials.path=src/main/resources/firebase-credentials.json
```

#### GESTIONCONTACTO/src/main/resources/application.properties

```properties
server.port=8085
spring.application.name=gestion-contacto

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=../wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### Verificar que todo funciona

```bash
# Compilar todos los microservicios
./mvnw clean package -DskipTests

# Si hay errores, verificar:
# 1. Que wallet/ existe en la raíz
# 2. Que firebase-credentials.json existe
# 3. Que application.properties tienen todos los valores
```

---

## 🔒 Seguridad - IMPORTANTE

### ⚠️ NO COMPARTIR por:
- ❌ Email sin encriptar
- ❌ Slack público
- ❌ WhatsApp Web (puede quedar en historial)
- ❌ GitHub (ya está en .gitignore)
- ❌ Compartir pantalla en videollamada grabada

### ✅ SÍ COMPARTIR por:
- ✅ Google Drive con link privado + password por separado
- ✅ OneDrive con password
- ✅ USB en persona
- ✅ Airdrop (Mac a Mac)
- ✅ WhatsApp móvil (mensaje temporal)

### 🗑️ Después de que tu compañero configure:
```bash
# Eliminar archivos temporales
rm -rf ~/golden-burgers-setup
rm ~/golden-burgers-setup.zip
```

---

## 📞 Soporte para tu Compañero

Si tu compañero tiene problemas, revisar:

1. **Error de conexión a BD**:
   ```
   ORA-12154: TNS:could not resolve the connect identifier
   ```
   → Verificar que `wallet/` está en la ubicación correcta

2. **Error de Firebase**:
   ```
   FileNotFoundException: firebase-credentials.json
   ```
   → Verificar que existe en `src/main/resources/`

3. **Error de JWT**:
   ```
   JWT signature does not match
   ```
   → Verificar que `jwt.secret` es idéntico en todos los microservicios

4. **Puerto en uso**:
   ```
   Port 8080 is already in use
   ```
   → Cambiar puerto en `application.properties` o matar proceso existente

---

## 📋 Checklist Final para tu Compañero

- [ ] Clonar repositorio desde GitHub
- [ ] Descomprimir archivo compartido
- [ ] Copiar `wallet/` a raíz del proyecto
- [ ] Copiar `wallet/` a `GESTIONUSUARIO/wallet/`
- [ ] Copiar `firebase-credentials.json` a API-GATEWAY
- [ ] Copiar `firebase-credentials.json` a GESTIONCATALOGO
- [ ] Crear `application.properties` en los 6 microservicios
- [ ] Verificar que todos tienen el mismo `jwt.secret`
- [ ] Compilar: `./mvnw clean package -DskipTests`
- [ ] Ejecutar: `./start-services.sh`
- [ ] Probar: `curl http://localhost:8080/actuator/health`

---

## 🎯 Resumen Rápido

**Archivos que TU compañero necesita**:

1. `wallet/` (carpeta completa) → 2 ubicaciones
2. `firebase-credentials.json` → 2 ubicaciones  
3. 6 archivos `application.properties` (con datos sensibles)

**Cómo compartir**:
- Crear ZIP con password
- Subir a Google Drive
- Enviar password por WhatsApp
- Tu compañero sigue las instrucciones

**Tiempo estimado**: 15-20 minutos para configurar todo.

# Guía Completa de Despliegue en VM Oracle Cloud

## Índice
1. [Preparación Local (Antes de crear VM)](#1-preparación-local)
2. [Creación y Configuración de VM](#2-creación-de-vm)
3. [Despliegue en VM](#3-despliegue-en-vm)
4. [Seguridad y Producción](#4-seguridad-y-producción)
5. [Gestión de Servicios](#5-gestión-de-servicios)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Preparación Local (Antes de crear VM)

### 1.1 Crear Perfiles de Producción

Crear `application-prod.properties` en cada microservicio:

#### API-GATEWAY/src/main/resources/application-prod.properties
```properties
server.port=8080
spring.application.name=api-gateway

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Firebase
firebase.credentials.path=/opt/golden-burgers/config/firebase-credentials.json

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers=INFO
logging.file.name=/opt/golden-burgers/logs/api-gateway.log
```

#### GESTIONUSUARIO/src/main/resources/application-prod.properties
```properties
server.port=8081
spring.application.name=gestionUsuario

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Firebase
firebase.credentials.path=/opt/golden-burgers/config/firebase-credentials.json

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/golden-burgers/logs/gestion-usuario.log
```

#### GESTIONVENTA/Microservicio-Gestion-Venta/src/main/resources/application-prod.properties
```properties
server.port=8082
spring.application.name=gestion-venta

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/golden-burgers/logs/gestion-venta.log
```

#### GESTIONPEDIDO/GestionPedidos/src/main/resources/application-prod.properties
```properties
server.port=8083
spring.application.name=gestion-pedido

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/golden-burgers/logs/gestion-pedido.log
```

#### GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/application-prod.properties
```properties
server.port=8084
spring.application.name=gestion-catalogo

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Firebase
firebase.credentials.path=/opt/golden-burgers/config/firebase-credentials.json

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/golden-burgers/logs/gestion-catalogo.log
```

#### GESTIONCONTACTO/src/main/resources/application-prod.properties
```properties
server.port=8085
spring.application.name=gestion-contacto

# JWT Configuration
jwt.secret=${JWT_SECRET}

# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@goldenBurgers_high?TNS_ADMIN=/opt/oracle/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=${DB_PASSWORD:goldenBurgers.01}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Logging
logging.level.root=INFO
logging.file.name=/opt/golden-burgers/logs/gestion-contacto.log
```

### 1.2 Compilar JARs

Ejecutar en cada directorio de microservicio:

```bash
# API Gateway
cd API-GATEWAY
./mvnw clean package -DskipTests
# JAR generado en: target/api-gateway-0.0.1-SNAPSHOT.jar

# Gestión Usuario
cd GESTIONUSUARIO
./mvnw clean package -DskipTests
# JAR generado en: target/gestionUsuario-0.0.1-SNAPSHOT.jar

# Gestión Venta
cd GESTIONVENTA/Microservicio-Gestion-Venta
./mvnw clean package -DskipTests
# JAR generado en: target/gestion-venta-0.0.1-SNAPSHOT.jar

# Gestión Pedido
cd GESTIONPEDIDO/GestionPedidos
./mvnw clean package -DskipTests
# JAR generado en: target/gestion-pedido-0.0.1-SNAPSHOT.jar

# Gestión Catálogo
cd GESTIONCATALOGO/gestion-catalogo-main
./mvnw clean package -DskipTests
# JAR generado en: target/gestion-catalogo-0.0.1-SNAPSHOT.jar

# Gestión Contacto
cd GESTIONCONTACTO
./mvnw clean package -DskipTests
# JAR generado en: target/gestion-contacto-0.0.1-SNAPSHOT.jar
```

### 1.3 Preparar Archivos de Configuración

Crear carpeta local con archivos a subir:

```bash
mkdir -p ~/vm-deployment/jars
mkdir -p ~/vm-deployment/config
mkdir -p ~/vm-deployment/wallet
mkdir -p ~/vm-deployment/scripts
```

Copiar JARs:
```bash
cp API-GATEWAY/target/api-gateway-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
cp GESTIONUSUARIO/target/gestionUsuario-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
cp GESTIONVENTA/Microservicio-Gestion-Venta/target/gestion-venta-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
cp GESTIONPEDIDO/GestionPedidos/target/gestion-pedido-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
cp GESTIONCATALOGO/gestion-catalogo-main/target/gestion-catalogo-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
cp GESTIONCONTACTO/target/gestion-contacto-0.0.1-SNAPSHOT.jar ~/vm-deployment/jars/
```

Copiar configuraciones:
```bash
cp wallet/* ~/vm-deployment/wallet/
cp API-GATEWAY/src/main/resources/firebase-credentials.json ~/vm-deployment/config/
```

---

## 2. Creación de VM

### 2.1 Crear Instancia en Oracle Cloud

1. **Acceder a Oracle Cloud Console**: https://cloud.oracle.com/
2. **Compute → Instances → Create Instance**
3. **Configuración recomendada**:
   - **Name**: golden-burgers-backend
   - **Image**: Oracle Linux 8
   - **Shape**: VM.Standard.E2.1.Micro (Always Free) o superior
   - **Networking**: Crear nueva VCN o usar existente
   - **SSH Keys**: Subir tu clave pública SSH

4. **Configurar Reglas de Firewall**:
   - Ingress Rules para los puertos:
     - 8080 (API Gateway)
     - 8081 (Gestión Usuario)
     - 8082 (Gestión Venta)
     - 8083 (Gestión Pedido)
     - 8084 (Gestión Catálogo)
     - 8085 (Gestión Contacto)
     - 22 (SSH)

### 2.2 Configurar Firewall en VM

Conectar a la VM:
```bash
ssh opc@<VM_PUBLIC_IP>
```

Configurar firewall:
```bash
# Abrir puertos necesarios
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --permanent --add-port=8083/tcp
sudo firewall-cmd --permanent --add-port=8084/tcp
sudo firewall-cmd --permanent --add-port=8085/tcp
sudo firewall-cmd --reload

# Verificar
sudo firewall-cmd --list-ports
```

### 2.3 Instalar Software Necesario

```bash
# Actualizar sistema
sudo dnf update -y

# Instalar Java 21
sudo dnf install -y java-21-openjdk java-21-openjdk-devel

# Verificar instalación
java -version

# Instalar Oracle Instant Client (necesario para Oracle Wallet)
sudo dnf install -y oracle-instantclient-basic oracle-instantclient-sqlplus

# Crear estructura de directorios
sudo mkdir -p /opt/golden-burgers/{jars,config,logs,scripts}
sudo mkdir -p /opt/oracle/wallet

# Dar permisos al usuario
sudo chown -R opc:opc /opt/golden-burgers
sudo chown -R opc:opc /opt/oracle/wallet
```

---

## 3. Despliegue en VM

### 3.1 Subir Archivos a VM

Desde tu máquina local:

```bash
# Subir JARs
scp ~/vm-deployment/jars/*.jar opc@<VM_IP>:/opt/golden-burgers/jars/

# Subir Oracle Wallet
scp -r ~/vm-deployment/wallet/* opc@<VM_IP>:/opt/oracle/wallet/

# Subir firebase-credentials.json
scp ~/vm-deployment/config/firebase-credentials.json opc@<VM_IP>:/opt/golden-burgers/config/
```

### 3.2 Configurar Variables de Entorno

Crear archivo de variables de entorno en la VM:

```bash
ssh opc@<VM_IP>

# Crear archivo .env
cat > /opt/golden-burgers/config/.env << 'EOF'
# JWT Secret (GENERAR NUEVO PARA PRODUCCIÓN)
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Database Password
export DB_PASSWORD=goldenBurgers.01

# Oracle Wallet
export TNS_ADMIN=/opt/oracle/wallet
EOF

# Proteger el archivo
chmod 600 /opt/golden-burgers/config/.env
```

### 3.3 Crear Scripts de Gestión

#### Script para iniciar servicios: `/opt/golden-burgers/scripts/start-all.sh`

```bash
cat > /opt/golden-burgers/scripts/start-all.sh << 'EOF'
#!/bin/bash

# Cargar variables de entorno
source /opt/golden-burgers/config/.env

JARS_DIR=/opt/golden-burgers/jars
LOGS_DIR=/opt/golden-burgers/logs
PIDS_DIR=/opt/golden-burgers/pids

mkdir -p $PIDS_DIR

echo "Iniciando microservicios Golden Burgers..."

# API Gateway (Puerto 8080)
echo "Iniciando API Gateway..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/api-gateway-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/api-gateway.log 2>&1 &
echo $! > $PIDS_DIR/api-gateway.pid
sleep 5

# Gestión Usuario (Puerto 8081)
echo "Iniciando Gestión Usuario..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/gestionUsuario-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/gestion-usuario.log 2>&1 &
echo $! > $PIDS_DIR/gestion-usuario.pid
sleep 5

# Gestión Venta (Puerto 8082)
echo "Iniciando Gestión Venta..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/gestion-venta-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/gestion-venta.log 2>&1 &
echo $! > $PIDS_DIR/gestion-venta.pid
sleep 5

# Gestión Pedido (Puerto 8083)
echo "Iniciando Gestión Pedido..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/gestion-pedido-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/gestion-pedido.log 2>&1 &
echo $! > $PIDS_DIR/gestion-pedido.pid
sleep 5

# Gestión Catálogo (Puerto 8084)
echo "Iniciando Gestión Catálogo..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/gestion-catalogo-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/gestion-catalogo.log 2>&1 &
echo $! > $PIDS_DIR/gestion-catalogo.pid
sleep 5

# Gestión Contacto (Puerto 8085)
echo "Iniciando Gestión Contacto..."
nohup java -jar -Dspring.profiles.active=prod \
  -DJWT_SECRET=$JWT_SECRET \
  -DDB_PASSWORD=$DB_PASSWORD \
  $JARS_DIR/gestion-contacto-0.0.1-SNAPSHOT.jar \
  > $LOGS_DIR/gestion-contacto.log 2>&1 &
echo $! > $PIDS_DIR/gestion-contacto.pid

echo ""
echo "Todos los microservicios iniciados."
echo "Verificar logs en: $LOGS_DIR"
echo "PIDs guardados en: $PIDS_DIR"
EOF

chmod +x /opt/golden-burgers/scripts/start-all.sh
```

#### Script para detener servicios: `/opt/golden-burgers/scripts/stop-all.sh`

```bash
cat > /opt/golden-burgers/scripts/stop-all.sh << 'EOF'
#!/bin/bash

PIDS_DIR=/opt/golden-burgers/pids

echo "Deteniendo microservicios Golden Burgers..."

for pidfile in $PIDS_DIR/*.pid; do
  if [ -f "$pidfile" ]; then
    PID=$(cat $pidfile)
    SERVICE=$(basename $pidfile .pid)
    echo "Deteniendo $SERVICE (PID: $PID)..."
    kill $PID 2>/dev/null
    rm $pidfile
  fi
done

echo "Todos los servicios detenidos."
EOF

chmod +x /opt/golden-burgers/scripts/stop-all.sh
```

#### Script para verificar estado: `/opt/golden-burgers/scripts/status.sh`

```bash
cat > /opt/golden-burgers/scripts/status.sh << 'EOF'
#!/bin/bash

echo "Estado de Microservicios Golden Burgers"
echo "========================================"

check_service() {
  SERVICE=$1
  PORT=$2
  
  if curl -s http://localhost:$PORT/actuator/health > /dev/null 2>&1; then
    echo "✓ $SERVICE (Puerto $PORT): RUNNING"
  else
    echo "✗ $SERVICE (Puerto $PORT): DOWN"
  fi
}

check_service "API Gateway" 8080
check_service "Gestión Usuario" 8081
check_service "Gestión Venta" 8082
check_service "Gestión Pedido" 8083
check_service "Gestión Catálogo" 8084
check_service "Gestión Contacto" 8085
EOF

chmod +x /opt/golden-burgers/scripts/status.sh
```

### 3.4 Iniciar Servicios

```bash
# Iniciar todos los servicios
/opt/golden-burgers/scripts/start-all.sh

# Esperar 30 segundos para que inicien
sleep 30

# Verificar estado
/opt/golden-burgers/scripts/status.sh

# Ver logs si hay problemas
tail -f /opt/golden-burgers/logs/*.log
```

---

## 4. Seguridad y Producción

### 4.1 Generar Nuevo JWT Secret para Producción

**IMPORTANTE**: Generar un nuevo secret para producción diferente al de desarrollo.

```bash
# En la VM
NEW_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "Nuevo JWT Secret: $NEW_SECRET"

# Actualizar .env
sed -i "s/^export JWT_SECRET=.*/export JWT_SECRET=$NEW_SECRET/" /opt/golden-burgers/config/.env
```

### 4.2 Cambiar Password de Base de Datos (Opcional)

Si decides cambiar el password:

1. **En Oracle Cloud Console**: Cambiar password de la base de datos
2. **Actualizar .env**:
```bash
sed -i "s/^export DB_PASSWORD=.*/export DB_PASSWORD=TU_NUEVO_PASSWORD/" /opt/golden-burgers/config/.env
```

### 4.3 Proteger Archivos Sensibles

```bash
# Proteger archivos de configuración
chmod 600 /opt/golden-burgers/config/.env
chmod 600 /opt/golden-burgers/config/firebase-credentials.json
chmod 600 /opt/oracle/wallet/*

# Solo el usuario opc puede leer
chown opc:opc /opt/golden-burgers/config/*
chown opc:opc /opt/oracle/wallet/*
```

### 4.4 Configurar Servicio Systemd (Opcional)

Para que los servicios inicien automáticamente al reiniciar la VM:

```bash
sudo cat > /etc/systemd/system/golden-burgers.service << 'EOF'
[Unit]
Description=Golden Burgers Microservices
After=network.target

[Service]
Type=forking
User=opc
ExecStart=/opt/golden-burgers/scripts/start-all.sh
ExecStop=/opt/golden-burgers/scripts/stop-all.sh
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

# Habilitar servicio
sudo systemctl daemon-reload
sudo systemctl enable golden-burgers
sudo systemctl start golden-burgers
```

---

## 5. Gestión de Servicios

### 5.1 Comandos Útiles

```bash
# Iniciar todos los servicios
/opt/golden-burgers/scripts/start-all.sh

# Detener todos los servicios
/opt/golden-burgers/scripts/stop-all.sh

# Ver estado
/opt/golden-burgers/scripts/status.sh

# Reiniciar todos
/opt/golden-burgers/scripts/stop-all.sh
sleep 5
/opt/golden-burgers/scripts/start-all.sh

# Ver logs en tiempo real
tail -f /opt/golden-burgers/logs/api-gateway.log
tail -f /opt/golden-burgers/logs/gestion-catalogo.log

# Ver todos los logs
tail -f /opt/golden-burgers/logs/*.log
```

### 5.2 Monitoreo

```bash
# Ver procesos Java
ps aux | grep java

# Ver puertos en uso
ss -tulpn | grep java

# Verificar conectividad a base de datos
sqlplus GOLDENBURGERSDB/goldenBurgers.01@goldenBurgers_high
```

### 5.3 Actualizar Aplicación

```bash
# 1. Detener servicios
/opt/golden-burgers/scripts/stop-all.sh

# 2. Backup de JARs actuales
cp -r /opt/golden-burgers/jars /opt/golden-burgers/jars.backup.$(date +%Y%m%d)

# 3. Subir nuevos JARs desde local
scp ~/vm-deployment/jars/*.jar opc@<VM_IP>:/opt/golden-burgers/jars/

# 4. Iniciar servicios
/opt/golden-burgers/scripts/start-all.sh
```

---

## 6. Troubleshooting

### 6.1 Servicios no Inician

**Problema**: Servicio no se inicia o se detiene inmediatamente.

**Solución**:
```bash
# Ver logs completos
cat /opt/golden-burgers/logs/api-gateway.log

# Buscar errores comunes
grep -i error /opt/golden-burgers/logs/*.log
grep -i exception /opt/golden-burgers/logs/*.log

# Verificar que Java está instalado
java -version

# Verificar que el JAR existe
ls -lh /opt/golden-burgers/jars/
```

### 6.2 Error de Conexión a Base de Datos

**Síntomas**: `ORA-12154: TNS:could not resolve the connect identifier`

**Solución**:
```bash
# Verificar que el wallet existe
ls -la /opt/oracle/wallet/

# Verificar variable TNS_ADMIN
echo $TNS_ADMIN

# Probar conexión directa
sqlplus GOLDENBURGERSDB/goldenBurgers.01@goldenBurgers_high

# Verificar tnsnames.ora
cat /opt/oracle/wallet/tnsnames.ora
```

### 6.3 JWT Token No Válido

**Síntomas**: Error "JWT signature does not match"

**Causa**: JWT_SECRET diferente entre microservicios.

**Solución**:
```bash
# Verificar que todos usan el mismo secret
grep JWT_SECRET /opt/golden-burgers/config/.env

# Reiniciar servicios después de cambiar
/opt/golden-burgers/scripts/stop-all.sh
/opt/golden-burgers/scripts/start-all.sh
```

### 6.4 Puerto en Uso

**Síntomas**: `Address already in use`

**Solución**:
```bash
# Ver qué proceso usa el puerto
sudo lsof -i :8080

# Matar proceso
kill -9 <PID>

# O usar el script de stop
/opt/golden-burgers/scripts/stop-all.sh
```

### 6.5 Firebase Credentials No Encontrado

**Síntomas**: `FileNotFoundException: firebase-credentials.json`

**Solución**:
```bash
# Verificar que existe
ls -la /opt/golden-burgers/config/firebase-credentials.json

# Verificar permisos
chmod 644 /opt/golden-burgers/config/firebase-credentials.json

# Verificar que application-prod.properties tiene la ruta correcta
grep firebase /opt/golden-burgers/jars/*.jar
```

---

## Checklist de Despliegue

### Antes de Crear VM
- [ ] Crear `application-prod.properties` para los 6 microservicios
- [ ] Compilar JARs con `./mvnw clean package -DskipTests`
- [ ] Copiar JARs a `~/vm-deployment/jars/`
- [ ] Copiar Oracle wallet a `~/vm-deployment/wallet/`
- [ ] Copiar `firebase-credentials.json` a `~/vm-deployment/config/`

### Crear VM
- [ ] Crear instancia Oracle Linux 8 en Oracle Cloud
- [ ] Configurar reglas de firewall para puertos 8080-8085
- [ ] Guardar clave SSH privada
- [ ] Anotar IP pública de la VM

### Configurar VM
- [ ] Conectar por SSH a la VM
- [ ] Actualizar sistema: `sudo dnf update -y`
- [ ] Instalar Java 21
- [ ] Instalar Oracle Instant Client
- [ ] Crear estructura de directorios `/opt/golden-burgers/`
- [ ] Crear `/opt/oracle/wallet/`
- [ ] Configurar firewall interno

### Desplegar Aplicación
- [ ] Subir JARs a `/opt/golden-burgers/jars/`
- [ ] Subir wallet a `/opt/oracle/wallet/`
- [ ] Subir `firebase-credentials.json` a `/opt/golden-burgers/config/`
- [ ] Crear archivo `.env` con variables de entorno
- [ ] Crear scripts: `start-all.sh`, `stop-all.sh`, `status.sh`
- [ ] Dar permisos de ejecución: `chmod +x /opt/golden-burgers/scripts/*.sh`

### Seguridad
- [ ] Generar nuevo JWT_SECRET para producción
- [ ] Actualizar `.env` con nuevo secret
- [ ] (Opcional) Cambiar password de base de datos
- [ ] Proteger archivos: `chmod 600 .env firebase-credentials.json`
- [ ] Proteger wallet: `chmod 600 /opt/oracle/wallet/*`

### Iniciar Servicios
- [ ] Ejecutar: `/opt/golden-burgers/scripts/start-all.sh`
- [ ] Esperar 30 segundos
- [ ] Verificar estado: `/opt/golden-burgers/scripts/status.sh`
- [ ] Revisar logs: `tail -f /opt/golden-burgers/logs/*.log`

### Pruebas
- [ ] Probar endpoint público: `curl http://<VM_IP>:8080/actuator/health`
- [ ] Probar login desde frontend
- [ ] Probar endpoints protegidos con JWT
- [ ] Verificar conexión a base de datos
- [ ] Verificar carga de imágenes (Firebase)

### Opcional - Producción
- [ ] Configurar servicio systemd para auto-inicio
- [ ] Configurar rotación de logs
- [ ] Configurar monitoreo (Prometheus/Grafana)
- [ ] Configurar HTTPS con certificado SSL
- [ ] Configurar dominio personalizado

---

## Información de Contacto de Producción

**URL Base API Gateway**: `http://<VM_PUBLIC_IP>:8080`

**Microservicios**:
- API Gateway: `http://<VM_PUBLIC_IP>:8080`
- Gestión Usuario: `http://<VM_PUBLIC_IP>:8081`
- Gestión Venta: `http://<VM_PUBLIC_IP>:8082`
- Gestión Pedido: `http://<VM_PUBLIC_IP>:8083`
- Gestión Catálogo: `http://<VM_PUBLIC_IP>:8084`
- Gestión Contacto: `http://<VM_PUBLIC_IP>:8085`

**Logs**: `/opt/golden-burgers/logs/`

**Configuración**: `/opt/golden-burgers/config/`

**Scripts**: `/opt/golden-burgers/scripts/`

---

## Notas Finales

1. **Oracle Wallet es portátil**: Los archivos del wallet se pueden copiar entre máquinas. Solo asegúrate de usar la ruta `/opt/oracle/wallet` en producción.

2. **JWT Secret**: El secret actual es de desarrollo. **DEBES** generar uno nuevo para producción por seguridad.

3. **Logs**: Los logs se acumulan. Considera configurar rotación de logs con `logrotate`.

4. **Memoria**: Cada microservicio Java consume ~200-300MB de RAM. Con 6 microservicios necesitas al menos 2GB de RAM libre.

5. **Respaldo**: Mantén respaldos de:
   - JARs compilados
   - Oracle wallet
   - firebase-credentials.json
   - Archivo .env (¡con cuidado!)

6. **Base de Datos Free Tier**: Oracle Autonomous Database Free Tier tiene límites de conexiones. HikariCP está configurado con máximo 2 conexiones por microservicio (total 12 conexiones).

7. **Actualizaciones**: Para actualizar, detén servicios, reemplaza JARs, e inicia nuevamente. Considera mantener versiones anteriores como respaldo.

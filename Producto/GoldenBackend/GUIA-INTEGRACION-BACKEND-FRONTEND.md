# Guía de Integración Backend-Frontend Golden Burgers

> **Objetivo:** Conectar el frontend React con el backend de microservicios Spring Boot, desplegando el backend en VM Oracle Cloud y el frontend en AWS S3.

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Configuración del Backend](#configuración-del-backend)
4. [Configuración del Frontend](#configuración-del-frontend)
5. [Deployment en VM Oracle](#deployment-en-vm-oracle)
6. [Deployment en AWS S3](#deployment-en-aws-s3)
7. [Testing y Verificación](#testing-y-verificación)
8. [Troubleshooting](#troubleshooting)
9. [Checklist Final](#checklist-final)

---

## Resumen Ejecutivo

### Análisis del Ejemplo del Profesor

**Backend (hospital-vm):**
- Spring Boot 3.5.6 + Java 17
- MySQL en localhost:3306
- API REST en `/api/v1/paciente`
- CORS comentado (no habilitado)
- Puerto 8080

**Frontend (hospital-vm-ui):**
- React 19 + Vite 7
- Puerto 5173
- **Proxy en Vite** configurado para `/api/v1` → `http://localhost:8080`
- Axios para peticiones HTTP
- NO usa Firebase

**Conexión:**
El profesor usa un **proxy de Vite** para evitar problemas de CORS en desarrollo. El frontend hace peticiones a `/api/v1` y Vite las redirige automáticamente a `localhost:8080`.

### Tu Proyecto Actual

**Backend:**
- Microservicios Spring Boot + API Gateway
- Base de datos Oracle (no MySQL)
- Firebase Authentication
- JWT para tokens internos
- Wallet Oracle ubicado localmente

**Frontend:**
- React 19 + Vite 7
- **LocalStorage para TODO** (productos, usuarios, pedidos)
- NO conectado al backend
- Firebase Storage para imágenes

**Problema Principal:**
Tu frontend trabaja completamente desconectado del backend, usando solo datos en localStorage.

---

## Arquitectura del Proyecto

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                              │
│            React App (AWS S3 Static Hosting)                 │
│                 http://tu-bucket.s3...                       │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP Requests
                      │ /api/v1/*
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    VM ORACLE CLOUD                           │
│              ubuntu@161.153.219.128                          │
│                                                              │
│  ┌──────────────────────────────────────────────┐           │
│  │         API GATEWAY (Puerto 8080)            │           │
│  │     /api/v1/* (CORS Habilitado)              │           │
│  └──────────────┬───────────────────────────────┘           │
│                 │                                            │
│    ┌────────────┼────────────┬────────────┬────────────┐    │
│    ▼            ▼            ▼            ▼            ▼    │
│  ┌─────┐   ┌─────┐     ┌─────┐     ┌─────┐     ┌─────┐    │
│  │8081 │   │8082 │     │8083 │     │8084 │     │8085 │    │
│  │User │   │Venta│     │Pedid│     │Catal│     │Cont │    │
│  └──┬──┘   └──┬──┘     └──┬──┘     └──┬──┘     └──┬──┘    │
│     └─────────┴───────────┴───────────┴───────────┘        │
│                           │                                 │
│                           ▼                                 │
│              ┌─────────────────────────┐                    │
│              │    Oracle Wallet        │                    │
│              │ /home/ubuntu/golden...  │                    │
│              └──────────┬──────────────┘                    │
└─────────────────────────┼───────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │  Oracle Database      │
              │  (Oracle Cloud)       │
              └───────────────────────┘
```

### Puertos Utilizados

| Servicio           | Puerto | URL                        |
|--------------------|--------|----------------------------|
| API Gateway        | 8080   | http://161.153.219.128:8080|
| Gestión Usuario    | 8081   | http://localhost:8081      |
| Gestión Venta      | 8082   | http://localhost:8082      |
| Gestión Pedido     | 8083   | http://localhost:8083      |
| Gestión Catálogo   | 8084   | http://localhost:8084      |
| Gestión Contacto   | 8085   | http://localhost:8085      |

---

## Configuración del Backend

### 1. Actualizar Configuración de Base de Datos

#### 1.1 Gestión Usuario

**Archivo:** `backGoldenBurgers/GESTIONUSUARIO/src/main/resources/application.properties`

```properties
spring.application.name=gestionUsuario
server.port=8081

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000

# Database Configuration - ACTUALIZADO PARA VM
spring.datasource.url=jdbc:oracle:thin:@basedatosfbo_medium?TNS_ADMIN=/home/ubuntu/goldenburgers/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones optimizado para Oracle Free Tier
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers.gestionUsuario=DEBUG
logging.level.org.springframework.security=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

#### 1.2 Gestión Catálogo

**Archivo:** `backGoldenBurgers/GESTIONCATALOGO/gestion-catalogo-main/src/main/resources/application.properties`

```properties
spring.application.name=gestionCatalogo
server.port=8084

# Database Configuration - ACTUALIZADO PARA VM
spring.datasource.url=jdbc:oracle:thin:@basedatosfbo_medium?TNS_ADMIN=/home/ubuntu/goldenburgers/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers.gestionCatalogo=DEBUG
```

#### 1.3 Gestión Contacto

**Archivo:** `backGoldenBurgers/GESTIONCONTACTO/src/main/resources/application.properties`

```properties
spring.application.name=gestionContacto
server.port=8085

# Database Configuration - ACTUALIZADO PARA VM
spring.datasource.url=jdbc:oracle:thin:@basedatosfbo_medium?TNS_ADMIN=/home/ubuntu/goldenburgers/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers.gestionContacto=DEBUG
```

#### 1.4 Gestión Pedido

**Archivo:** `backGoldenBurgers/GESTIONPEDIDO/GestionPedidos/src/main/resources/application.properties`

```properties
spring.application.name=gestionPedido
server.port=8083

# Database Configuration - ACTUALIZADO PARA VM
spring.datasource.url=jdbc:oracle:thin:@basedatosfbo_medium?TNS_ADMIN=/home/ubuntu/goldenburgers/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers.gestionPedido=DEBUG
```

#### 1.5 Gestión Venta

**Archivo:** `backGoldenBurgers/GESTIONVENTA/Microservicio-Gestion-Venta/src/main/resources/application.properties`

```properties
spring.application.name=gestionVenta
server.port=8082

# Database Configuration - ACTUALIZADO PARA VM
spring.datasource.url=jdbc:oracle:thin:@basedatosfbo_medium?TNS_ADMIN=/home/ubuntu/goldenburgers/wallet
spring.datasource.username=GOLDENBURGERSDB
spring.datasource.password=goldenBurgers.01
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.goldenburgers.gestionVenta=DEBUG
```

### 2. Configurar CORS en API Gateway

#### 2.1 Crear Configuración de CORS

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/java/com/goldenburgers/apigateway/config/CorsConfig.java`

```java
package com.goldenburgers.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos
        // DESARROLLO: localhost
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");

        // PRODUCCIÓN: S3 Bucket
        // TODO: Reemplazar con la URL real de tu bucket S3
        config.addAllowedOrigin("http://tu-bucket-goldenburgers.s3-website-us-east-1.amazonaws.com");
        config.addAllowedOrigin("https://tu-bucket-goldenburgers.s3.amazonaws.com");

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Headers expuestos (para que el frontend pueda leerlos)
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));

        // Permitir envío de credenciales (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Cache de la configuración CORS (1 hora)
        config.setMaxAge(3600L);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
```

#### 2.2 Actualizar application.properties del API Gateway

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/resources/application.properties`

Verificar que tenga:

```properties
# ====================================================================
# APLICACION - API GATEWAY GOLDEN BURGERS
# ====================================================================
spring.application.name=api-gateway

# Puerto del servidor
server.port=8080

# ====================================================================
# FIREBASE - AUTENTICACION
# ====================================================================
firebase.credentials.path=classpath:firebase-credentials.json

# ====================================================================
# JWT - TOKENS INTERNOS
# ====================================================================
# IMPORTANTE: Cambiar en producción por una clave segura de al menos 256 bits
# Genera una clave segura con: openssl rand -base64 32
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000

# ====================================================================
# URLS DE MICROSERVICIOS (localhost porque están en la misma VM)
# ====================================================================
microservices.gestion-usuario.url=http://localhost:8081
microservices.gestion-venta.url=http://localhost:8082
microservices.gestion-pedido.url=http://localhost:8083
microservices.gestion-catalogo.url=http://localhost:8084
microservices.gestion-contacto.url=http://localhost:8085

# ====================================================================
# LOGGING
# ====================================================================
logging.level.root=INFO
logging.level.com.goldenburgers.apigateway=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG

# ====================================================================
# ACTUATOR - MONITOREO
# ====================================================================
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# ====================================================================
# CONFIGURACION ADICIONAL
# ====================================================================
# Desactivar banner de Spring Boot (opcional)
spring.main.banner-mode=off

# Formato de fecha/hora
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.time-zone=America/Santiago
```

#### 2.3 Crear perfil de PRODUCCIÓN

**Archivo:** `backGoldenBurgers/API-GATEWAY/src/main/resources/application-prod.properties`

```properties
# ====================================================================
# PERFIL DE PRODUCCIÓN - VM ORACLE
# ====================================================================

# Puerto del servidor (mismo)
server.port=8080

# URLs de microservicios (localhost porque están en la misma VM)
microservices.gestion-usuario.url=http://localhost:8081
microservices.gestion-venta.url=http://localhost:8082
microservices.gestion-pedido.url=http://localhost:8083
microservices.gestion-catalogo.url=http://localhost:8084
microservices.gestion-contacto.url=http://localhost:8085

# Logging más restrictivo en producción
logging.level.root=WARN
logging.level.com.goldenburgers.apigateway=INFO
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web=INFO

# Deshabilitar detalles de salud en producción
management.endpoint.health.show-details=when-authorized
```

### 3. Scripts de Deployment

#### 3.1 Script de Compilación Local

**Archivo:** `backGoldenBurgers/build-all.sh`

```bash
#!/bin/bash

# Script para compilar todos los microservicios
# Ejecutar desde la carpeta backGoldenBurgers

echo "🔨 Compilando todos los microservicios de Golden Burgers..."
echo ""

# Función para compilar un proyecto Maven
compilar_proyecto() {
    local proyecto=$1
    local nombre=$2

    echo "📦 Compilando $nombre..."
    cd "$proyecto" || exit 1

    if mvn clean package -DskipTests; then
        echo "✅ $nombre compilado exitosamente"
    else
        echo "❌ Error al compilar $nombre"
        exit 1
    fi

    cd - > /dev/null || exit 1
    echo ""
}

# Compilar cada microservicio
compilar_proyecto "API-GATEWAY" "API Gateway"
compilar_proyecto "GESTIONUSUARIO" "Gestión Usuario"
compilar_proyecto "GESTIONVENTA/Microservicio-Gestion-Venta" "Gestión Venta"
compilar_proyecto "GESTIONPEDIDO/GestionPedidos" "Gestión Pedido"
compilar_proyecto "GESTIONCATALOGO/gestion-catalogo-main" "Gestión Catálogo"
compilar_proyecto "GESTIONCONTACTO" "Gestión Contacto"

echo ""
echo "✅ Todos los microservicios compilados exitosamente!"
echo ""
echo "📋 Los archivos .jar están en las carpetas target/ de cada proyecto"
echo ""
```

Dar permisos de ejecución:

```bash
chmod +x backGoldenBurgers/build-all.sh
```

#### 3.2 Script de Deployment en VM

**Archivo:** `backGoldenBurgers/deploy-vm.sh`

```bash
#!/bin/bash

# Script para deployment en VM Oracle Cloud
# Este script se ejecuta DENTRO de la VM
# Prerequisito: Los archivos .jar ya deben estar en la VM

set -e  # Salir si hay error

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Desplegando Golden Burgers Backend en VM Oracle${NC}"
echo ""

# Directorio base
BASE_DIR="/home/ubuntu/goldenburgers"
cd "$BASE_DIR" || exit 1

# Crear directorio de logs si no existe
mkdir -p logs

# Función para detener un servicio
detener_servicio() {
    local nombre=$1
    local puerto=$2

    echo -e "${YELLOW}⏹️  Deteniendo $nombre...${NC}"

    # Buscar proceso en el puerto y matarlo
    PID=$(lsof -ti:$puerto 2>/dev/null || true)
    if [ ! -z "$PID" ]; then
        kill -9 $PID
        echo -e "${GREEN}   ✓ $nombre detenido (PID: $PID)${NC}"
    else
        echo -e "   ℹ️  $nombre no estaba corriendo"
    fi
}

# Función para iniciar un servicio
iniciar_servicio() {
    local jar=$1
    local nombre=$2
    local log=$3
    local profile=$4

    echo -e "${YELLOW}▶️  Iniciando $nombre...${NC}"

    if [ ! -f "$jar" ]; then
        echo -e "${RED}   ❌ ERROR: No se encuentra $jar${NC}"
        exit 1
    fi

    if [ ! -z "$profile" ]; then
        nohup java -jar -Dspring.profiles.active=$profile "$jar" > "logs/$log" 2>&1 &
    else
        nohup java -jar "$jar" > "logs/$log" 2>&1 &
    fi

    echo -e "${GREEN}   ✓ $nombre iniciado (PID: $!)${NC}"
}

# 1. DETENER SERVICIOS EXISTENTES
echo ""
echo -e "${YELLOW}=====================================${NC}"
echo -e "${YELLOW}  DETENIENDO SERVICIOS EXISTENTES   ${NC}"
echo -e "${YELLOW}=====================================${NC}"
echo ""

detener_servicio "API Gateway" 8080
detener_servicio "Gestión Usuario" 8081
detener_servicio "Gestión Venta" 8082
detener_servicio "Gestión Pedido" 8083
detener_servicio "Gestión Catálogo" 8084
detener_servicio "Gestión Contacto" 8085

sleep 2

# 2. INICIAR MICROSERVICIOS
echo ""
echo -e "${YELLOW}=====================================${NC}"
echo -e "${YELLOW}     INICIANDO MICROSERVICIOS       ${NC}"
echo -e "${YELLOW}=====================================${NC}"
echo ""

# Primero los microservicios (necesitan tiempo para iniciar antes del gateway)
iniciar_servicio "GESTIONUSUARIO/target/gestionUsuario-0.0.1-SNAPSHOT.jar" "Gestión Usuario" "usuario.log" ""
sleep 2

iniciar_servicio "GESTIONVENTA/Microservicio-Gestion-Venta/target/Microservicio-Gestion-Venta-0.0.1-SNAPSHOT.jar" "Gestión Venta" "venta.log" ""
sleep 2

iniciar_servicio "GESTIONPEDIDO/GestionPedidos/target/GestionPedidos-0.0.1-SNAPSHOT.jar" "Gestión Pedido" "pedido.log" ""
sleep 2

iniciar_servicio "GESTIONCATALOGO/gestion-catalogo-main/target/gestion-catalogo-main-0.0.1-SNAPSHOT.jar" "Gestión Catálogo" "catalogo.log" ""
sleep 2

iniciar_servicio "GESTIONCONTACTO/target/gestionContacto-0.0.1-SNAPSHOT.jar" "Gestión Contacto" "contacto.log" ""
sleep 2

# Finalmente el API Gateway (con perfil prod)
echo ""
echo -e "${YELLOW}Esperando que los microservicios estén listos...${NC}"
sleep 5

iniciar_servicio "API-GATEWAY/target/api-gateway-0.0.1-SNAPSHOT.jar" "API Gateway" "gateway.log" "prod"

# 3. VERIFICACIÓN
echo ""
echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}         VERIFICACIÓN                ${NC}"
echo -e "${GREEN}=====================================${NC}"
echo ""

sleep 5

echo "Verificando servicios..."
echo ""

ps aux | grep java | grep -v grep || echo -e "${RED}No se encontraron procesos Java${NC}"

echo ""
echo -e "${GREEN}✅ Deployment completado!${NC}"
echo ""
echo -e "📋 Revisa los logs en: ${YELLOW}$BASE_DIR/logs/${NC}"
echo ""
echo "Para ver los logs en tiempo real:"
echo "  tail -f logs/gateway.log"
echo "  tail -f logs/usuario.log"
echo ""
echo "Para verificar que los servicios estén corriendo:"
echo "  curl http://localhost:8080/actuator/health"
echo ""
```

Dar permisos:

```bash
chmod +x backGoldenBurgers/deploy-vm.sh
```

### 4. Verificar Firebase Credentials

Asegúrate de que `firebase-credentials.json` esté en:

```
backGoldenBurgers/API-GATEWAY/src/main/resources/firebase-credentials.json
```

Este archivo también debe subirse a la VM en la misma ubicación dentro del .jar.

---

## Configuración del Frontend

### 1. Crear Configuración de API

#### 1.1 Archivo de Configuración Base

**Archivo:** `backGoldenBurgers/appBurguer-React/src/config/api.js`

```javascript
import axios from "axios";

// Configuración de URLs según el ambiente
const API_URLS = {
  // En desarrollo, usa el proxy de Vite
  development: "/api/v1",

  // En producción, apunta directamente a la VM
  production: "http://161.153.219.128:8080/api/v1"
};

// Detectar el ambiente actual
const isDevelopment = import.meta.env.DEV;
const baseURL = isDevelopment ? API_URLS.development : API_URLS.production;

console.log(`🌍 Ambiente: ${isDevelopment ? 'Desarrollo' : 'Producción'}`);
console.log(`🔗 API Base URL: ${baseURL}`);

// Crear instancia de Axios con configuración base
const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json"
  },
  timeout: 30000, // 30 segundos
  withCredentials: false // Cambiar a true si usas cookies
});

// Interceptor para agregar el token JWT en cada request
api.interceptors.request.use(
  (config) => {
    // Obtener token del localStorage
    const token = localStorage.getItem("authToken");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Log en desarrollo
    if (isDevelopment) {
      console.log(`📤 ${config.method.toUpperCase()} ${config.url}`);
    }

    return config;
  },
  (error) => {
    console.error("❌ Error en request:", error);
    return Promise.reject(error);
  }
);

// Interceptor para manejo de respuestas y errores
api.interceptors.response.use(
  (response) => {
    // Log en desarrollo
    if (isDevelopment) {
      console.log(`📥 ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error) => {
    // Manejo de errores comunes
    if (error.response) {
      // El servidor respondió con un código de error
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // Token inválido o expirado
          console.error("🔒 No autorizado - Token inválido");
          localStorage.removeItem("authToken");
          localStorage.removeItem("user");
          // Redirigir al login
          if (!window.location.pathname.includes("/login")) {
            window.location.href = "/login";
          }
          break;

        case 403:
          console.error("🚫 Acceso prohibido");
          alert("No tienes permisos para realizar esta acción");
          break;

        case 404:
          console.error("🔍 Recurso no encontrado");
          break;

        case 500:
          console.error("💥 Error del servidor");
          alert("Error en el servidor. Por favor, intenta más tarde.");
          break;

        default:
          console.error(`❌ Error ${status}:`, data);
      }
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta
      console.error("📡 No se pudo conectar con el servidor");
      alert("No se pudo conectar con el servidor. Verifica tu conexión.");
    } else {
      // Algo pasó al configurar la petición
      console.error("⚠️ Error:", error.message);
    }

    return Promise.reject(error);
  }
);

export default api;
```

### 2. Crear Servicios por Módulo

#### 2.1 Servicio de Productos

**Archivo:** `backGoldenBurgers/appBurguer-React/src/services/productosService.js`

```javascript
import api from "../config/api";

/**
 * Servicio para gestión de productos
 * Endpoints del microservicio de Catálogo
 */

// Obtener todos los productos
export const getProductos = async () => {
  try {
    const response = await api.get("/catalogo/productos");

    // Manejar tanto arrays simples como respuestas paginadas
    const data = Array.isArray(response.data)
      ? response.data
      : (response.data?.content ?? []);

    return data;
  } catch (error) {
    console.error("Error al obtener productos:", error);
    throw error;
  }
};

// Obtener un producto por ID
export const getProductoPorId = async (id) => {
  try {
    const response = await api.get(`/catalogo/productos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener producto ${id}:`, error);
    throw error;
  }
};

// Crear nuevo producto
export const crearProducto = async (producto) => {
  try {
    const response = await api.post("/catalogo/productos", producto);
    return response.data;
  } catch (error) {
    console.error("Error al crear producto:", error);
    throw error;
  }
};

// Actualizar producto existente
export const actualizarProducto = async (id, producto) => {
  try {
    const response = await api.put(`/catalogo/productos/${id}`, producto);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar producto ${id}:`, error);
    throw error;
  }
};

// Eliminar producto
export const eliminarProducto = async (id) => {
  try {
    const response = await api.delete(`/catalogo/productos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar producto ${id}:`, error);
    throw error;
  }
};

// Obtener productos por categoría
export const getProductosPorCategoria = async (categoria) => {
  try {
    const response = await api.get(`/catalogo/productos/categoria/${categoria}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener productos de categoría ${categoria}:`, error);
    throw error;
  }
};
```

#### 2.2 Servicio de Usuarios

**Archivo:** `backGoldenBurgers/appBurguer-React/src/services/usuariosService.js`

```javascript
import api from "../config/api";

/**
 * Servicio para gestión de usuarios y autenticación
 * Endpoints del microservicio de Usuario
 */

// Login
export const login = async (email, password) => {
  try {
    const response = await api.post("/usuario/login", {
      email,
      password
    });

    // Guardar token si viene en la respuesta
    if (response.data.token) {
      localStorage.setItem("authToken", response.data.token);
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }

    return response.data;
  } catch (error) {
    console.error("Error en login:", error);
    throw error;
  }
};

// Logout
export const logout = () => {
  localStorage.removeItem("authToken");
  localStorage.removeItem("user");
  window.location.href = "/login";
};

// Obtener usuario actual
export const getCurrentUser = () => {
  const userStr = localStorage.getItem("user");
  return userStr ? JSON.parse(userStr) : null;
};

// Registrar nuevo usuario
export const registrarUsuario = async (usuario) => {
  try {
    const response = await api.post("/usuario/registro", usuario);
    return response.data;
  } catch (error) {
    console.error("Error al registrar usuario:", error);
    throw error;
  }
};

// Obtener todos los usuarios (solo admin)
export const getUsuarios = async () => {
  try {
    const response = await api.get("/usuario/usuarios");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener usuarios:", error);
    throw error;
  }
};

// Obtener usuario por ID
export const getUsuarioPorId = async (id) => {
  try {
    const response = await api.get(`/usuario/usuarios/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener usuario ${id}:`, error);
    throw error;
  }
};

// Actualizar usuario
export const actualizarUsuario = async (id, usuario) => {
  try {
    const response = await api.put(`/usuario/usuarios/${id}`, usuario);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar usuario ${id}:`, error);
    throw error;
  }
};

// Eliminar usuario
export const eliminarUsuario = async (id) => {
  try {
    const response = await api.delete(`/usuario/usuarios/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar usuario ${id}:`, error);
    throw error;
  }
};
```

#### 2.3 Servicio de Pedidos

**Archivo:** `backGoldenBurgers/appBurguer-React/src/services/pedidosService.js`

```javascript
import api from "../config/api";

/**
 * Servicio para gestión de pedidos
 * Endpoints del microservicio de Pedidos
 */

// Obtener todos los pedidos
export const getPedidos = async () => {
  try {
    const response = await api.get("/pedido/pedidos");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener pedidos:", error);
    throw error;
  }
};

// Obtener pedido por ID
export const getPedidoPorId = async (id) => {
  try {
    const response = await api.get(`/pedido/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener pedido ${id}:`, error);
    throw error;
  }
};

// Crear nuevo pedido
export const crearPedido = async (pedido) => {
  try {
    const response = await api.post("/pedido/pedidos", pedido);
    return response.data;
  } catch (error) {
    console.error("Error al crear pedido:", error);
    throw error;
  }
};

// Actualizar estado de pedido
export const actualizarEstadoPedido = async (id, estado) => {
  try {
    const response = await api.put(`/pedido/pedidos/${id}/estado`, { estado });
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar estado del pedido ${id}:`, error);
    throw error;
  }
};

// Eliminar pedido
export const eliminarPedido = async (id) => {
  try {
    const response = await api.delete(`/pedido/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar pedido ${id}:`, error);
    throw error;
  }
};

// Obtener pedidos por usuario
export const getPedidosPorUsuario = async (usuarioId) => {
  try {
    const response = await api.get(`/pedido/pedidos/usuario/${usuarioId}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener pedidos del usuario ${usuarioId}:`, error);
    throw error;
  }
};
```

#### 2.4 Servicio de Contacto

**Archivo:** `backGoldenBurgers/appBurguer-React/src/services/contactoService.js`

```javascript
import api from "../config/api";

/**
 * Servicio para gestión de contactos y mensajes
 * Endpoints del microservicio de Contacto
 */

// Enviar mensaje de contacto
export const enviarMensaje = async (mensaje) => {
  try {
    const response = await api.post("/contacto/mensajes", mensaje);
    return response.data;
  } catch (error) {
    console.error("Error al enviar mensaje:", error);
    throw error;
  }
};

// Obtener todos los mensajes (admin)
export const getMensajes = async () => {
  try {
    const response = await api.get("/contacto/mensajes");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener mensajes:", error);
    throw error;
  }
};

// Marcar mensaje como leído
export const marcarComoLeido = async (id) => {
  try {
    const response = await api.put(`/contacto/mensajes/${id}/leido`);
    return response.data;
  } catch (error) {
    console.error(`Error al marcar mensaje ${id} como leído:`, error);
    throw error;
  }
};
```

### 3. Actualizar vite.config.js

**Archivo:** `backGoldenBurgers/appBurguer-React/vite.config.js`

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: "./",

  // Configuración del servidor de desarrollo
  server: {
    port: 5173,

    // PROXY - Redirige /api/v1/* al backend
    // Similar al ejemplo del profesor
    proxy: {
      '/api/v1': {
        // En desarrollo local: backend en localhost
        target: 'http://localhost:8080',

        // Para testing contra la VM, comentar línea anterior y descomentar:
        // target: 'http://161.153.219.128:8080',

        changeOrigin: true,
        secure: false,

        // Log de las peticiones (útil para debug)
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Sending Request:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response:', proxyRes.statusCode, req.url);
          });
        },
      },
    },
  },

  // Configuración de testing
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./tests/setup.js",
    coverage: {
      provider: "v8",
      reporter: ["text", "html"],
    },
  },
});
```

### 4. Variables de Entorno

#### 4.1 Desarrollo

**Archivo:** `backGoldenBurgers/appBurguer-React/.env.development`

```env
# Ambiente de desarrollo
VITE_APP_ENV=development
VITE_API_URL=/api/v1
VITE_ENABLE_LOGS=true

# Firebase (si lo usas para storage)
VITE_FIREBASE_API_KEY=tu-api-key
VITE_FIREBASE_AUTH_DOMAIN=tu-proyecto.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=tu-proyecto
VITE_FIREBASE_STORAGE_BUCKET=tu-proyecto.appspot.com
```

#### 4.2 Producción

**Archivo:** `backGoldenBurgers/appBurguer-React/.env.production`

```env
# Ambiente de producción
VITE_APP_ENV=production
VITE_API_URL=http://161.153.219.128:8080/api/v1
VITE_ENABLE_LOGS=false

# Firebase
VITE_FIREBASE_API_KEY=tu-api-key
VITE_FIREBASE_AUTH_DOMAIN=tu-proyecto.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=tu-proyecto
VITE_FIREBASE_STORAGE_BUCKET=tu-proyecto.appspot.com
```

#### 4.3 Actualizar .gitignore

**Archivo:** `backGoldenBurgers/appBurguer-React/.gitignore`

Agregar:

```gitignore
# Variables de entorno
.env
.env.local
.env.*.local

# Build
dist
dist-ssr
*.local

# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
.DS_Store
```

### 5. Ejemplo de Componente Actualizado

#### 5.1 Gestión de Productos

**Archivo:** `backGoldenBurgers/appBurguer-React/src/pages/admin/gestionProductos.jsx`

```javascript
import { useState, useEffect } from "react";
import { getProductos, eliminarProducto } from "../../services/productosService";

export default function GestionProductos() {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Cargar productos desde el backend
  const cargarProductos = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getProductos();
      setProductos(data);

    } catch (err) {
      console.error("Error al cargar productos:", err);
      setError(
        err.response?.status === 404
          ? "No se encontraron productos"
          : "Error al conectar con el servidor. Verifica que el backend esté corriendo."
      );
    } finally {
      setLoading(false);
    }
  };

  // Cargar al montar el componente
  useEffect(() => {
    cargarProductos();
  }, []);

  // Eliminar producto
  const handleEliminar = async (id) => {
    if (!confirm("¿Estás seguro de eliminar este producto?")) {
      return;
    }

    try {
      await eliminarProducto(id);

      // Recargar la lista
      await cargarProductos();

      alert("Producto eliminado exitosamente");
    } catch (err) {
      console.error("Error al eliminar producto:", err);
      alert("Error al eliminar el producto");
    }
  };

  // Renderizado
  if (loading) {
    return (
      <div className="text-center p-5">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
        <p className="mt-2">Cargando productos...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="alert alert-danger m-3" role="alert">
        <h4 className="alert-heading">Error</h4>
        <p>{error}</p>
        <hr />
        <button
          className="btn btn-outline-danger"
          onClick={cargarProductos}
        >
          Reintentar
        </button>
      </div>
    );
  }

  if (productos.length === 0) {
    return (
      <div className="alert alert-info m-3" role="alert">
        <h4 className="alert-heading">Sin productos</h4>
        <p>No hay productos registrados en el sistema.</p>
      </div>
    );
  }

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Gestión de Productos</h1>
        <button
          className="btn btn-primary"
          onClick={() => window.location.href = "/admin/nuevo-producto"}
        >
          + Nuevo Producto
        </button>
      </div>

      <div className="table-responsive">
        <table className="table table-striped table-hover">
          <thead className="table-dark">
            <tr>
              <th>ID</th>
              <th>Imagen</th>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Precio</th>
              <th>Stock</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {productos.map((producto) => (
              <tr key={producto.id}>
                <td>{producto.id}</td>
                <td>
                  <img
                    src={producto.imagen || producto.imagenSrc}
                    alt={producto.nombre}
                    style={{ width: "50px", height: "50px", objectFit: "cover" }}
                  />
                </td>
                <td>{producto.nombre || producto.nombre_producto}</td>
                <td>{producto.categoria || producto.categoria_producto}</td>
                <td>${(producto.precio || producto.precio_producto).toLocaleString()}</td>
                <td>{producto.stock || producto.stock_producto}</td>
                <td>
                  <button
                    className="btn btn-sm btn-warning me-2"
                    onClick={() => window.location.href = `/admin/editar-producto/${producto.id}`}
                  >
                    Editar
                  </button>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={() => handleEliminar(producto.id)}
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

---

## Deployment en VM Oracle

### 1. Preparación de la VM

#### 1.1 Conectarse a la VM

```bash
ssh ubuntu@161.153.219.128
```

#### 1.2 Instalar Java 21 (si no está instalado)

```bash
# Actualizar paquetes
sudo apt update

# Instalar Java 21
sudo apt install openjdk-21-jdk -y

# Verificar instalación
java -version
```

#### 1.3 Instalar Maven (para compilación en VM si es necesario)

```bash
sudo apt install maven -y
mvn -version
```

#### 1.4 Crear estructura de directorios

```bash
# Crear directorio base
mkdir -p /home/ubuntu/goldenburgers/{logs,API-GATEWAY,GESTIONUSUARIO,GESTIONCATALOGO,GESTIONCONTACTO,GESTIONPEDIDO,GESTIONVENTA}

# Verificar que el wallet esté en su lugar
ls -la /home/ubuntu/goldenburgers/wallet/
```

### 2. Configurar Firewall

#### 2.1 Oracle Cloud Security List

En la consola de Oracle Cloud:

1. Ir a **Networking** > **Virtual Cloud Networks**
2. Seleccionar tu VCN
3. Ir a **Security Lists**
4. Agregar regla **Ingress**:
   - Source CIDR: `0.0.0.0/0`
   - IP Protocol: `TCP`
   - Destination Port Range: `8080`

#### 2.2 Firewall de Ubuntu (iptables)

```bash
# Permitir puerto 8080
sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT

# Guardar reglas
sudo netfilter-persistent save

# Verificar
sudo iptables -L -n | grep 8080
```

### 3. Subir Archivos a la VM

#### 3.1 Desde tu máquina local

**Opción A: Usar SCP**

```bash
# Compilar primero (desde tu máquina local)
cd /Users/Basaes/Desktop/Desktop/Progra/DUOC/CuartoSemestre/Fullstack/proyectofinal/backGoldenBurgers
./build-all.sh

# Subir API Gateway
scp API-GATEWAY/target/api-gateway-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/API-GATEWAY/

# Subir Gestión Usuario
scp GESTIONUSUARIO/target/gestionUsuario-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/GESTIONUSUARIO/

# Subir Gestión Venta
scp GESTIONVENTA/Microservicio-Gestion-Venta/target/Microservicio-Gestion-Venta-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/GESTIONVENTA/

# Subir Gestión Pedido
scp GESTIONPEDIDO/GestionPedidos/target/GestionPedidos-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/GESTIONPEDIDO/

# Subir Gestión Catálogo
scp GESTIONCATALOGO/gestion-catalogo-main/target/gestion-catalogo-main-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/GESTIONCATALOGO/

# Subir Gestión Contacto
scp GESTIONCONTACTO/target/gestionContacto-0.0.1-SNAPSHOT.jar ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/GESTIONCONTACTO/

# Subir script de deployment
scp deploy-vm.sh ubuntu@161.153.219.128:/home/ubuntu/goldenburgers/
```

**Opción B: Crear un script automatizado**

**Archivo:** `backGoldenBurgers/upload-to-vm.sh`

```bash
#!/bin/bash

VM_USER="ubuntu"
VM_HOST="161.153.219.128"
VM_BASE="/home/ubuntu/goldenburgers"

echo "📤 Subiendo archivos a la VM Oracle..."

# Función para subir un jar
upload_jar() {
    local_path=$1
    remote_dir=$2

    echo "  Subiendo $local_path..."
    scp "$local_path" "${VM_USER}@${VM_HOST}:${VM_BASE}/${remote_dir}/"
}

# Subir todos los JARs
upload_jar "API-GATEWAY/target/api-gateway-0.0.1-SNAPSHOT.jar" "API-GATEWAY"
upload_jar "GESTIONUSUARIO/target/gestionUsuario-0.0.1-SNAPSHOT.jar" "GESTIONUSUARIO"
upload_jar "GESTIONVENTA/Microservicio-Gestion-Venta/target/Microservicio-Gestion-Venta-0.0.1-SNAPSHOT.jar" "GESTIONVENTA"
upload_jar "GESTIONPEDIDO/GestionPedidos/target/GestionPedidos-0.0.1-SNAPSHOT.jar" "GESTIONPEDIDO"
upload_jar "GESTIONCATALOGO/gestion-catalogo-main/target/gestion-catalogo-main-0.0.1-SNAPSHOT.jar" "GESTIONCATALOGO"
upload_jar "GESTIONCONTACTO/target/gestionContacto-0.0.1-SNAPSHOT.jar" "GESTIONCONTACTO"

# Subir script de deployment
echo "  Subiendo script de deployment..."
scp deploy-vm.sh "${VM_USER}@${VM_HOST}:${VM_BASE}/"

echo "✅ Archivos subidos exitosamente!"
```

```bash
chmod +x backGoldenBurgers/upload-to-vm.sh
```

### 4. Ejecutar Deployment

```bash
# Desde la VM
ssh ubuntu@161.153.219.128
cd /home/ubuntu/goldenburgers
chmod +x deploy-vm.sh
./deploy-vm.sh
```

### 5. Verificar Deployment

```bash
# Ver procesos Java corriendo
ps aux | grep java

# Ver logs
tail -f logs/gateway.log
tail -f logs/usuario.log

# Probar endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/catalogo/productos

# Desde tu máquina local
curl http://161.153.219.128:8080/actuator/health
```

### 6. Crear Servicio Systemd (Opcional pero Recomendado)

Para que los servicios se inicien automáticamente al reiniciar la VM:

**Archivo:** `/etc/systemd/system/goldenburgers.service`

```ini
[Unit]
Description=Golden Burgers Backend Services
After=network.target

[Service]
Type=forking
User=ubuntu
WorkingDirectory=/home/ubuntu/goldenburgers
ExecStart=/home/ubuntu/goldenburgers/deploy-vm.sh
ExecStop=/usr/bin/pkill -f "goldenburgers"
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Habilitar:

```bash
sudo systemctl enable goldenburgers
sudo systemctl start goldenburgers
sudo systemctl status goldenburgers
```

---

## Deployment en AWS S3

### 1. Crear y Configurar Bucket S3

#### 1.1 Crear Bucket

```bash
# Instalar AWS CLI si no lo tienes
# brew install awscli  # macOS
# sudo apt install awscli  # Linux

# Configurar credenciales
aws configure

# Crear bucket (nombre debe ser único globalmente)
aws s3 mb s3://goldenburgers-frontend --region us-east-1
```

#### 1.2 Habilitar Static Website Hosting

```bash
# Configurar como sitio web estático
aws s3 website s3://goldenburgers-frontend --index-document index.html --error-document index.html
```

#### 1.3 Configurar Bucket Policy (público)

Crear archivo `bucket-policy.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::goldenburgers-frontend/*"
    }
  ]
}
```

Aplicar:

```bash
aws s3api put-bucket-policy --bucket goldenburgers-frontend --policy file://bucket-policy.json
```

#### 1.4 Configurar CORS en S3

Crear archivo `cors-config.json`:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": []
  }
]
```

Aplicar:

```bash
aws s3api put-bucket-cors --bucket goldenburgers-frontend --cors-configuration file://cors-config.json
```

### 2. Build y Deploy del Frontend

#### 2.1 Script de Deployment

**Archivo:** `backGoldenBurgers/appBurguer-React/deploy-s3.sh`

```bash
#!/bin/bash

# Configuración
BUCKET_NAME="goldenburgers-frontend"
REGION="us-east-1"

echo "🏗️  Building frontend para producción..."
npm run build

if [ $? -ne 0 ]; then
    echo "❌ Error en el build"
    exit 1
fi

echo ""
echo "📦 Desplegando a S3..."
aws s3 sync dist/ s3://$BUCKET_NAME --delete --region $REGION

if [ $? -ne 0 ]; then
    echo "❌ Error al subir a S3"
    exit 1
fi

echo ""
echo "🗂️  Configurando tipos MIME..."
aws s3 cp s3://$BUCKET_NAME s3://$BUCKET_NAME \
    --recursive \
    --exclude "*" \
    --include "*.html" \
    --content-type "text/html" \
    --metadata-directive REPLACE

aws s3 cp s3://$BUCKET_NAME s3://$BUCKET_NAME \
    --recursive \
    --exclude "*" \
    --include "*.css" \
    --content-type "text/css" \
    --metadata-directive REPLACE

aws s3 cp s3://$BUCKET_NAME s3://$BUCKET_NAME \
    --recursive \
    --exclude "*" \
    --include "*.js" \
    --content-type "application/javascript" \
    --metadata-directive REPLACE

echo ""
echo "✅ Deployment completado!"
echo ""
echo "🌐 Tu aplicación está disponible en:"
echo "   http://$BUCKET_NAME.s3-website-$REGION.amazonaws.com"
echo ""
```

```bash
chmod +x backGoldenBurgers/appBurguer-React/deploy-s3.sh
```

#### 2.2 Ejecutar Deployment

```bash
cd backGoldenBurgers/appBurguer-React
./deploy-s3.sh
```

### 3. Actualizar CORS en Backend

Una vez que tengas la URL de S3, actualiza `CorsConfig.java`:

```java
// En producción
config.addAllowedOrigin("http://goldenburgers-frontend.s3-website-us-east-1.amazonaws.com");
```

Recompilar y redesplegar el backend.

### 4. (Opcional) Configurar CloudFront

Para HTTPS y mejor performance:

```bash
# Crear distribución CloudFront
aws cloudfront create-distribution \
    --origin-domain-name goldenburgers-frontend.s3.amazonaws.com \
    --default-root-object index.html
```

---

## Testing y Verificación

### 1. Testing Local

#### 1.1 Backend Local

```bash
# Terminal 1: API Gateway
cd backGoldenBurgers/API-GATEWAY
mvn spring-boot:run

# Terminal 2: Gestión Usuario
cd backGoldenBurgers/GESTIONUSUARIO
mvn spring-boot:run

# Terminal 3: Gestión Catálogo
cd backGoldenBurgers/GESTIONCATALOGO/gestion-catalogo-main
mvn spring-boot:run

# ... etc para otros microservicios
```

#### 1.2 Frontend Local

```bash
cd backGoldenBurgers/appBurguer-React
npm run dev
```

Abrir: http://localhost:5173

#### 1.3 Verificar Conexión

En la consola del navegador deberías ver:

```
🌍 Ambiente: Desarrollo
🔗 API Base URL: /api/v1
📤 GET /catalogo/productos
Sending Request: GET /api/v1/catalogo/productos
📥 200 /api/v1/catalogo/productos
```

### 2. Testing en VM

```bash
# SSH a la VM
ssh ubuntu@161.153.219.128

# Verificar servicios
ps aux | grep java

# Test de endpoints
curl http://localhost:8080/actuator/health

# Test desde fuera de la VM (tu máquina)
curl http://161.153.219.128:8080/actuator/health
curl http://161.153.219.128:8080/api/v1/catalogo/productos
```

### 3. Testing Frontend en S3

Abrir en navegador:
```
http://goldenburgers-frontend.s3-website-us-east-1.amazonaws.com
```

Verificar en consola del navegador que se conecte a la VM.

---

## Troubleshooting

### Problemas Comunes

#### 1. CORS Error

**Síntoma:**
```
Access to XMLHttpRequest at 'http://161.153.219.128:8080/api/v1/...'
from origin 'http://...' has been blocked by CORS policy
```

**Solución:**
- Verificar que `CorsConfig.java` esté creado
- Verificar que la URL del frontend esté en `allowedOrigins`
- Recompilar y redesplegar el backend

#### 2. Connection Refused

**Síntoma:**
```
Error: connect ECONNREFUSED 161.153.219.128:8080
```

**Solución:**
- Verificar que el API Gateway esté corriendo: `ps aux | grep api-gateway`
- Verificar firewall: `sudo iptables -L -n | grep 8080`
- Verificar Security List en Oracle Cloud

#### 3. 404 Not Found en Endpoints

**Síntoma:**
```
404 Not Found: /api/v1/catalogo/productos
```

**Solución:**
- Verificar que el microservicio correspondiente esté corriendo
- Verificar la configuración de rutas en el API Gateway
- Ver logs: `tail -f /home/ubuntu/goldenburgers/logs/gateway.log`

#### 4. Error de Base de Datos

**Síntoma:**
```
java.sql.SQLException: ORA-xxxxx
```

**Solución:**
- Verificar que el wallet esté en `/home/ubuntu/goldenburgers/wallet/`
- Verificar permisos: `chmod 600 /home/ubuntu/goldenburgers/wallet/*`
- Verificar credenciales en `application.properties`

#### 5. Token Inválido

**Síntoma:**
```
401 Unauthorized
```

**Solución:**
- Verificar que el token esté en localStorage
- Verificar que `jwt.secret` sea el mismo en todos los microservicios
- Hacer logout y login de nuevo

### Comandos Útiles

```bash
# Ver logs en tiempo real
tail -f /home/ubuntu/goldenburgers/logs/gateway.log

# Ver todos los procesos Java
ps aux | grep java

# Matar todos los procesos Java
pkill -f java

# Ver uso de puertos
sudo lsof -i :8080
sudo netstat -tulpn | grep LISTEN

# Reiniciar servicios
cd /home/ubuntu/goldenburgers
./deploy-vm.sh

# Verificar conectividad
ping 161.153.219.128
telnet 161.153.219.128 8080
curl -v http://161.153.219.128:8080/actuator/health
```

---

## Checklist Final

### Backend

- [ ] Todos los `application.properties` actualizados con ruta del wallet VM
- [ ] `CorsConfig.java` creado en API Gateway
- [ ] `firebase-credentials.json` en resources del API Gateway
- [ ] Todos los microservicios compilados sin errores
- [ ] Archivos `.jar` subidos a la VM
- [ ] Wallet Oracle en `/home/ubuntu/goldenburgers/wallet/`
- [ ] Firewall configurado (puerto 8080 abierto)
- [ ] Servicios corriendo en la VM
- [ ] Endpoints respondiendo correctamente

### Frontend

- [ ] Carpeta `src/config/` creada con `api.js`
- [ ] Carpeta `src/services/` creada con todos los servicios
- [ ] `vite.config.js` actualizado con proxy
- [ ] Archivos `.env.development` y `.env.production` creados
- [ ] Componentes actualizados para usar servicios
- [ ] Build de producción exitoso
- [ ] Bucket S3 creado y configurado
- [ ] Frontend desplegado en S3
- [ ] CORS actualizado en backend con URL de S3
- [ ] Aplicación accesible desde S3

### Testing

- [ ] Backend local funciona
- [ ] Frontend local se conecta al backend
- [ ] Backend en VM responde
- [ ] Frontend en S3 se conecta al backend en VM
- [ ] Login funciona
- [ ] CRUD de productos funciona
- [ ] CRUD de usuarios funciona
- [ ] CRUD de pedidos funciona

---

## Resumen de URLs

### Desarrollo Local

- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Gestión Usuario: http://localhost:8081
- Gestión Venta: http://localhost:8082
- Gestión Pedido: http://localhost:8083
- Gestión Catálogo: http://localhost:8084
- Gestión Contacto: http://localhost:8085

### Producción

- Frontend: http://goldenburgers-frontend.s3-website-us-east-1.amazonaws.com
- Backend API: http://161.153.219.128:8080
- Health Check: http://161.153.219.128:8080/actuator/health

---

## Próximos Pasos

1. **Migración Gradual:** Comienza migrando un módulo a la vez (ej: productos)
2. **Testing Exhaustivo:** Prueba cada funcionalidad antes de pasar a la siguiente
3. **Monitoreo:** Configura logs y monitoreo en producción
4. **SSL/HTTPS:** Considera usar CloudFront para HTTPS
5. **CI/CD:** Automatiza el deployment con GitHub Actions o similar
6. **Backup:** Implementa backups regulares de la base de datos

---

**Autor:** Claude Code
**Fecha:** 2025-01-18
**Versión:** 1.0

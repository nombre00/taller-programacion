#!/bin/bash

# ====================================================================
# SCRIPT DE GESTION DE MICROSERVICIOS - GOLDEN BURGERS
# ====================================================================
# Uso: 
#   ./manage-services.sh start   - Iniciar todos los microservicios
#   ./manage-services.sh stop    - Detener todos los microservicios
#   ./manage-services.sh restart - Reiniciar todos
#   ./manage-services.sh status  - Ver estado
# ====================================================================

# Rutas de los microservicios
PROJECT_ROOT="/C:/Users/CAMILO/Desktop/BACKEDNFINALGB/backGoldenBurgers"

# ====================================================================
# FUNCIONES
# ====================================================================

# Función para obtener el nombre del servicio por puerto
get_service_name() {
    case $1 in
        8080) echo "API-GATEWAY" ;;
        8081) echo "GESTIONUSUARIO" ;;
        8082) echo "GESTIONVENTA" ;;
        8083) echo "GESTIONPEDIDO" ;;
        8084) echo "GESTIONCATALOGO" ;;
        8085) echo "GESTIONCONTACTO" ;;
    esac
}

# Función para detener todos los microservicios
stop_services() {
    echo "🛑 Deteniendo todos los microservicios..."
    echo ""
    
    for port in 8080 8081 8082 8083 8084 8085; do
        service=$(get_service_name $port)
        pid=$(lsof -ti :$port 2>/dev/null)
        
        if [ -n "$pid" ]; then
            kill -9 $pid 2>/dev/null
            echo "✅ $service (puerto $port) - DETENIDO (PID: $pid)"
        else
            echo "⚪ $service (puerto $port) - Ya estaba detenido"
        fi
    done
    
    echo ""
    echo "✅ Todos los microservicios han sido detenidos"
}

# Función para mostrar el estado
status_services() {
    echo "📊 ESTADO DE MICROSERVICIOS"
    echo "=========================================="
    echo ""
    
    running=0
    stopped=0
    
    for port in 8080 8081 8082 8083 8084 8085; do
        service=$(get_service_name $port)
        pid=$(lsof -ti :$port 2>/dev/null)
        
        if [ -n "$pid" ]; then
            echo "✅ Puerto $port - $service (PID: $pid) - CORRIENDO"
            running=$((running + 1))
        else
            echo "❌ Puerto $port - $service - DETENIDO"
            stopped=$((stopped + 1))
        fi
    done
    
    echo ""
    echo "=========================================="
    echo "Total: $running corriendo, $stopped detenidos"
}

# Función para iniciar todos los microservicios
start_services() {
    echo "🚀 Iniciando todos los microservicios..."
    echo ""
    echo "⏳ Esto tomará unos segundos..."
    echo ""
    
    # 1. API Gateway (PRIMERO - MUY IMPORTANTE)
    echo "Iniciando API-GATEWAY (8080)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/API-GATEWAY && ./mvnw spring-boot:run\""
    sleep 3
    
    # 2. Gestión de Usuarios
    echo "Iniciando GESTIONUSUARIO (8081)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/GESTIONUSUARIO && ./mvnw spring-boot:run\""
    sleep 2
    
    # 3. Gestión de Ventas
    echo "Iniciando GESTIONVENTA (8082)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/GESTIONVENTA/Microservicio-Gestion-Venta && ./mvnw spring-boot:run\""
    sleep 2
    
    # 4. Gestión de Pedidos
    echo "Iniciando GESTIONPEDIDO (8083)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/GESTIONPEDIDO/GestionPedidos && ./mvnw spring-boot:run\""
    sleep 2
    
    # 5. Gestión de Catálogo
    echo "Iniciando GESTIONCATALOGO (8084)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/GESTIONCATALOGO/gestion-catalogo-main && ./mvnw spring-boot:run\""
    sleep 2
    
    # 6. Gestión de Contacto
    echo "Iniciando GESTIONCONTACTO (8085)..."
    osascript -e "tell app \"Terminal\" to do script \"cd $PROJECT_ROOT/GESTIONCONTACTO && ./mvnw spring-boot:run\""
    
    echo ""
    echo "✅ Comandos de inicio enviados a nuevas terminales"
    echo "⏳ Espera 30-60 segundos a que todos arranquen completamente"
    echo ""
    echo "💡 Verifica el estado con: ./manage-services.sh status"
}

# Función para reiniciar
restart_services() {
    stop_services
    echo ""
    echo "⏳ Esperando 3 segundos antes de reiniciar..."
    sleep 3
    echo ""
    start_services
}

# ====================================================================
# MENU PRINCIPAL
# ====================================================================

case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        status_services
        ;;
    *)
        echo "======================================================================"
        echo "  SCRIPT DE GESTIÓN DE MICROSERVICIOS - GOLDEN BURGERS"
        echo "======================================================================"
        echo ""
        echo "Uso: $0 {start|stop|restart|status}"
        echo ""
        echo "  start   - Iniciar todos los microservicios en nuevas terminales"
        echo "  stop    - Detener todos los microservicios"
        echo "  restart - Reiniciar todos los microservicios"
        echo "  status  - Ver el estado de todos los microservicios"
        echo ""
        echo "Ejemplo: $0 start"
        echo "======================================================================"
        exit 1
        ;;
esac

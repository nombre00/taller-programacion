# ====================================================================
# SCRIPT DE GESTION DE MICROSERVICIOS - WINDOWS POWERSHELL
# ====================================================================
$PROJECT_ROOT = "C:\Users\CAMILO\Desktop\BACKEDNFINALGB\backGoldenBurgers"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   GESTOR DE MICROSERVICIOS - WINDOWS" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Funcion para ver estado
function Get-ServicesStatus {
    Write-Host "`n ESTADO DE MICROSERVICIOS" -ForegroundColor Yellow
    Write-Host "------------------------------------------"
    
    $services = @(
        @{Name = "API-GATEWAY"; Port = 8080},
        @{Name = "GESTIONUSUARIO"; Port = 8081},
        @{Name = "GESTIONVENTA"; Port = 8082},
        @{Name = "GESTIONPEDIDO"; Port = 8083},
        @{Name = "GESTIONCATALOGO"; Port = 8084},
        @{Name = "GESTIONCONTACTO"; Port = 8085}
    )
    
    $running = 0
    foreach ($service in $services) {
        $process = Get-NetTCPConnection -LocalPort $service.Port -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host " $($service.Name) (puerto $($service.Port)) - CORRIENDO" -ForegroundColor Green
            $running++
        } else {
            Write-Host " $($service.Name) (puerto $($service.Port)) - DETENIDO" -ForegroundColor Red
        }
    }
    Write-Host "------------------------------------------"
    Write-Host " Total: $running/6 microservicios ejecutandose" -ForegroundColor Cyan
}

# Funcion para detener servicios
function Stop-Services {
    Write-Host "`n Deteniendo todos los microservicios..." -ForegroundColor Red
    
    $ports = @(8080, 8081, 8082, 8083, 8084, 8085)
    $stoppedCount = 0
    
    foreach ($port in $ports) {
        try {
            $processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
            
            if ($processes) {
                foreach ($processId in $processes) {
                    try {
                        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                        Write-Host " Puerto $port - DETENIDO (PID: $processId)" -ForegroundColor Green
                        $stoppedCount++
                    } catch {
                        Write-Host " Puerto $port - Error al detener: $($_.Exception.Message)" -ForegroundColor Yellow
                    }
                }
            } else {
                Write-Host " Puerto $port - Ya esta detenido" -ForegroundColor Gray
            }
        } catch {
            Write-Host " Error verificando puerto $port : $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`n Operacion completada - $stoppedCount servicios detenidos" -ForegroundColor Green
}

# Funcion para iniciar servicios
function Start-Services {
    Write-Host "`n Iniciando microservicios..." -ForegroundColor Green
    
    # Verificar rutas
    $services = @(
        @{Name = "API-GATEWAY"; Path = "$PROJECT_ROOT\API-GATEWAY"; Port = 8080},
        @{Name = "GESTIONUSUARIO"; Path = "$PROJECT_ROOT\GESTIONUSUARIO"; Port = 8081},
        @{Name = "GESTIONVENTA"; Path = "$PROJECT_ROOT\GESTIONVENTA\Microservicio-Gestion-Venta"; Port = 8082},
        @{Name = "GESTIONPEDIDO"; Path = "$PROJECT_ROOT\GESTIONPEDIDO\GestionPedidos"; Port = 8083},
        @{Name = "GESTIONCATALOGO"; Path = "$PROJECT_ROOT\GESTIONCATALOGO\gestion-catalogo-main"; Port = 8084},
        @{Name = "GESTIONCONTACTO"; Path = "$PROJECT_ROOT\GESTIONCONTACTO"; Port = 8085}
    )
    
    Write-Host "`n Verificando rutas..." -ForegroundColor Cyan
    foreach ($service in $services) {
        if (-not (Test-Path $service.Path)) {
            Write-Host " No se encuentra: $($service.Path)" -ForegroundColor Red
            return
        }
        Write-Host " $($service.Name) - Ruta correcta" -ForegroundColor Green
    }

    Write-Host "`n Iniciando microservicios..." -ForegroundColor Cyan
    foreach ($service in $services) {
        Write-Host "  Iniciando $($service.Name)..." -ForegroundColor White
        
        # Comando para ejecutar en nueva ventana
        $command = "cd '$($service.Path)'; .\mvnw.cmd spring-boot:run"
        
        # Iniciar en nueva ventana de PowerShell
        Start-Process PowerShell -ArgumentList "-NoExit", "-Command", $command
        
        # Esperar entre servicios
        Start-Sleep -Seconds 8
    }
    
    Write-Host "`n Microservicios iniciandose..." -ForegroundColor Green
    Write-Host " Espera 30 segundos y verifica el estado" -ForegroundColor Yellow
    Write-Host " Usa: .\manage-services.ps1 status" -ForegroundColor Cyan
}

# Funcion para reiniciar
function Restart-Services {
    Stop-Services
    Write-Host "`n Esperando 3 segundos..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3
    Start-Services
}

# Menu principal
if ($args.Count -eq 0) {
    Write-Host "`nSelecciona una opcion:" -ForegroundColor White
    Write-Host "1. Iniciar todos los microservicios" -ForegroundColor Gray
    Write-Host "2. Detener todos los microservicios" -ForegroundColor Gray
    Write-Host "3. Ver estado" -ForegroundColor Gray
    Write-Host "4. Reiniciar todos" -ForegroundColor Gray
    Write-Host "5. Salir" -ForegroundColor Gray
    
    $choice = Read-Host "`nOpcion (1-5)"
    switch ($choice) {
        "1" { Start-Services }
        "2" { Stop-Services }
        "3" { Get-ServicesStatus }
        "4" { Restart-Services }
        "5" { exit }
        default { Write-Host "Opcion invalida" -ForegroundColor Red }
    }
} else {
    switch ($args[0].ToLower()) {
        "start" { Start-Services }
        "stop" { Stop-Services }
        "status" { Get-ServicesStatus }
        "restart" { Restart-Services }
        default { 
            Write-Host "Uso: .\manage-services.ps1 [start|stop|status|restart]" -ForegroundColor Yellow
        }
    }
}

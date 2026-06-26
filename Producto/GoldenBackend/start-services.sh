#!/bin/bash
echo "🍔 Golden Burgers - Iniciando microservicios..."
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$BASE_DIR/.env.local"
echo -e "${GREEN}✅ Variables de entorno cargadas${NC}"

ORDEN=("GESTIONUSUARIO" "GESTIONCATALOGO" "GESTIONPEDIDO" "GESTIONVENTA" "GESTIONCONTACTO" "GESTIONTURNOS" "GESTIONCUENTAS" "API-GATEWAY")
PUERTOS=("8081" "8082" "8083" "8084" "8085" "8086" "8087" "9090")

wait_for_port() {
  local PUERTO=$1
  local MS=$2
  local INTENTOS=0
  local MAX=60  # espera hasta 60 segundos (30 x 2s)
  echo -e "${YELLOW}   ⏳ Esperando que $MS levante en puerto $PUERTO...${NC}"
  while ! netstat -ano | grep -q ":$PUERTO.*LISTENING"; do
    sleep 2
    INTENTOS=$((INTENTOS + 1))
    if [ $INTENTOS -ge $MAX ]; then
      echo -e "${RED}   ❌ $MS no levantó en tiempo esperado. Revisa logs/$MS.log${NC}"
      return 1
    fi
  done
  echo -e "${GREEN}   ✅ $MS está arriba en puerto $PUERTO${NC}"
  return 0
}

FALLIDOS=()

for i in "${!ORDEN[@]}"; do
  MS=${ORDEN[$i]}
  PUERTO=${PUERTOS[$i]}
  MS_DIR="$BASE_DIR/$MS"
  echo -e "${YELLOW}🚀 Iniciando $MS...${NC}"
  (cd "$MS_DIR" && mvn spring-boot:run) > "$BASE_DIR/logs/$MS.log" 2>&1 &
  
  if wait_for_port $PUERTO $MS; then
    :
  else
    FALLIDOS+=($MS)
  fi
  echo ""
done

echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
if [ ${#FALLIDOS[@]} -eq 0 ]; then
  echo -e "${GREEN}  ✅ Todos los servicios iniciados correctamente   ${NC}"
else
  echo -e "${RED}  ⚠️  Servicios con problemas: ${FALLIDOS[*]}${NC}"
fi
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""
echo "📋 Ver logs en tiempo real:"
for MS in "${ORDEN[@]}"; do
  echo "   tail -f $BASE_DIR/logs/$MS.log"
done
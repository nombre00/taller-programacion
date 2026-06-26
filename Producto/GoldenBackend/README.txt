PARA OPERAR EL PROYECTO:

PARA LEVANTAR LOS MICROSERVICIOS DEL BACKEND: 
source .env.local	(GIT BASH)
bash start-services.sh	(GIT BASH)

PARA DETENER LOS SERVICIOS: 
taskkill //F //IM java.exe	(GIT BASH)

PARA RECARGAR UN SERVICIO DESPUES DE HACER UNA EDICIÓN (TIENES QUE CORRERLO EN SU CARPETA RAÍZ)
mvn clean install -DskipTests


PARA LEVANTAR EL FRONTEND:
npm run dev


USUARIOS:
ADMIN: admin@goldenburgers.cl		admin2026!


PARA REVISAR LOSGS:
tail -f logs/GESTIONUSUARIO.log
tail -n 100 logs/gestionusuario.log

tail -n 50 logs/API-GATEWAY.log
tail -f logs/API-GATEWAY.log

tail -f logs/GESTIONCATALOGO.log
tail -n 500 logs/GESTIONCATALOGO.log

tail -n 100 logs/GESTIONPEDIDO.log

tail -n 100 logs/GESTIONTURNOS.log



Código de recuperación de Twilio:
TYMEYCXSS5KZX5XBHTDTENR6


Para revisar que el programa compila bien evitando los test para mayor velocidad:
./mvnw clean package -DskipTests






# ====================================================================
# URLS DE MICROSERVICIOS
# ====================================================================
microservices.gestion-usuario.url=http://localhost:8081
microservices.gestion-catalogo.url=http://localhost:8082
microservices.gestion-pedido.url=http://localhost:8083
microservices.gestion-venta.url=http://localhost:8084
microservices.gestion-contacto.url=http://localhost:8085
microservices.gestion-turnos.url=http://localhost:8086
microservices.gestion-cuentas.url=http://localhost:8087











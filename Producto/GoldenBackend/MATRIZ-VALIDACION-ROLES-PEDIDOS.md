# Matriz de Validación de Roles - GestionPedidos

## Resumen
Todos los endpoints de GestionPedidos ahora están protegidos con validación de roles usando `@PreAuthorize`. Los roles se extraen del token JWT y se validan automáticamente en cada petición.

## Roles Definidos
- **ADMIN** (rolId = 1) - Administrador del sistema
- **TRABAJADOR** (rolId = 2) - Personal de la tienda/cocina
- **CLIENTE** (rolId = 3) - Cliente final

## Mapeo de Autorización

| Endpoint | Método | Ruta | Roles Permitidos | Descripción |
|----------|--------|------|------------------|-------------|
| listarPedidos | GET | `/api/pedidos` | ADMIN, TRABAJADOR | Listar todos los pedidos del sistema |
| obtenerPedido | GET | `/api/pedidos/{id}` | ADMIN, TRABAJADOR, CLIENTE | Obtener detalles de un pedido específico |
| listarPedidosPorCliente | GET | `/api/pedidos/cliente/{idCliente}` | ADMIN, TRABAJADOR, CLIENTE | Listar pedidos de un cliente específico |
| crearPedidoCompleto | POST | `/api/pedidos/completo` | ADMIN, TRABAJADOR, CLIENTE | Crear un nuevo pedido |
| cancelarPedido | DELETE | `/api/pedidos/{id}` | ADMIN, CLIENTE | Cancelar/eliminar un pedido |
| cambiarEstadoPedido | PUT | `/api/pedidos/cambiar-estado/{idPedido}/estado/{idEstado}` | ADMIN, TRABAJADOR | Cambiar el estado de un pedido (sin realizar venta) |
| actualizarEstadoPedidoPagado | PUT | `/api/pedidos/procesar/{idPedido}` | CLIENTE | Marcar pedido como pagado (genera venta automática) |
| obtenerDetallesCliente | GET | `/api/pedidos/detalles/cliente/{idCliente}` | ADMIN, TRABAJADOR | Obtener detalles de los productos en pedidos de un cliente |

## Cómo Funciona la Validación

### 1. Extracción del Rol desde el Token JWT
El filtro `JwtAuthenticationFilter` extrae el `rolNombre` del token JWT:
```java
final String rolNombre = jwtService.extractRolNombre(internalToken);
```

### 2. Conversión a Formato Spring Security
En `CustomUserDetails.getAuthorities()`, se convierte el nombre del rol:
```java
String roleName = "ROLE_" + rolNombre.toUpperCase();
// "Admin" -> "ROLE_ADMIN"
// "Trabajador" -> "ROLE_TRABAJADOR"
// "Cliente" -> "ROLE_CLIENTE"
```

### 3. Validación en el Endpoint
Spring Security valida automáticamente el `@PreAuthorize` en cada petición:
```java
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
@GetMapping
public ResponseEntity<List<Pedido>> listarPedidos() { ... }
```

Si el usuario no tiene un rol permitido, recibe un error **403 Forbidden**.

## Comportamiento de Errores

| Error | Causa | Solución |
|-------|-------|----------|
| **401 Unauthorized** | Token no está presente o es inválido | Enviar header `X-Internal-Token` o `Authorization: Bearer <token>` válido |
| **403 Forbidden** | Token es válido pero el rol no tiene permiso | Usar una cuenta con el rol correcto para acceder al endpoint |
| **404 Not Found** | El recurso no existe | Verificar que el ID del pedido sea correcto |

## Ejemplo de Uso

### Caso 1: Cliente queriendo listar todos los pedidos
```bash
# ❌ RECHAZADO - 403 Forbidden
curl -X GET "http://localhost:8080/api/pedidos" \
  -H "Authorization: Bearer <cliente_token>"
```

**Motivo**: El endpoint requiere ADMIN o TRABAJADOR.

### Caso 2: Trabajador listando todos los pedidos
```bash
# ✅ ACEPTADO - 200 OK
curl -X GET "http://localhost:8080/api/pedidos" \
  -H "Authorization: Bearer <trabajador_token>"
```

**Motivo**: El token contiene rolNombre = "Trabajador".

### Caso 3: Cliente creando un pedido
```bash
# ✅ ACEPTADO - 201 Created
curl -X POST "http://localhost:8080/api/pedidos/completo" \
  -H "Authorization: Bearer <cliente_token>" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

**Motivo**: El endpoint permite ADMIN, TRABAJADOR, CLIENTE.

## Cambios Realizados

✅ **Agregadas protecciones @PreAuthorize a 3 métodos que faltaban**:
1. `cambiarEstadoPedido()` - Solo ADMIN, TRABAJADOR
2. `actualizarEstadoPedidoPagado()` - Solo CLIENTE
3. `obtenerDetallesCliente()` - Solo ADMIN, TRABAJADOR

## Verificación

Para verificar que los roles se están validando correctamente:

1. **Compilar**: `mvn clean package -DskipTests`
2. **Ejecutar GestionPedidos**: `java -jar GestionPedidos-0.0.1-SNAPSHOT.jar`
3. **Probar con diferentes roles**: Usa tokens de diferentes usuarios (admin, trabajador, cliente)
4. **Revisar logs**: Busca mensajes de "Usuario autenticado" con el rol correspondiente

## Notas Importantes

- Los roles vienen del token JWT generado por el API Gateway
- El API Gateway extrae los roles de la base de datos de GestionUsuario
- Si el rol en la base de datos cambia, el cambio se refleja en los nuevos tokens
- Los tokens en caché no se actualizan hasta que expiren

## Próximos Pasos

1. Compilar el proyecto con `mvn clean package -DskipTests`
2. Reiniciar GestionPedidos
3. Probar acceso a endpoints con diferentes usuarios/roles
4. Revisar logs para confirmar validación de roles

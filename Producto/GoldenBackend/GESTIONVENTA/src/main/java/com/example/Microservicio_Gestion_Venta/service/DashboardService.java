package com.example.Microservicio_Gestion_Venta.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- VENTAS POR PERÍODOS CON NOMBRES REALES ---
    
    /**
     * Ventas del día actual
     */
    public Map<String, Object> getVentasHoy() {
        try {
            String sql = """
                SELECT 
                    NVL(SUM(monto_total_venta), 0) as venta_hoy,
                    COUNT(*) as cantidad_ventas
                FROM Venta 
                WHERE fecha_venta >= TRUNC(SYSDATE) 
                  AND fecha_venta < TRUNC(SYSDATE) + 1
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas de hoy: " + e.getMessage());
        }
    }

    /**
     * Ventas del mes actual
     */
    public Map<String, Object> getVentasMesActual() {
        try {
            String sql = """
                SELECT 
                    NVL(SUM(monto_total_venta), 0) as venta_mes_actual,
                    COUNT(*) as cantidad_ventas,
                    EXTRACT(MONTH FROM SYSDATE) as mes_actual
                FROM Venta 
                WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR FROM SYSDATE)
                  AND EXTRACT(MONTH FROM fecha_venta) = EXTRACT(MONTH FROM SYSDATE)
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas del mes: " + e.getMessage());
        }
    }

    /**
     * Ventas del año actual
     */
    public Map<String, Object> getVentasAnioActual() {
        try {
            String sql = """
                SELECT 
                    NVL(SUM(monto_total_venta), 0) as venta_anio_actual,
                    COUNT(*) as cantidad_ventas,
                    EXTRACT(YEAR FROM SYSDATE) as anio_actual
                FROM Venta 
                WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR FROM SYSDATE)
                """;
            
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas del año: " + e.getMessage());
        }
    }

    /**
     * Resumen completo de ventas (hoy, mes, año)
     */
    public Map<String, Object> getResumenVentasCompleto() {
        Map<String, Object> resumen = new HashMap<>();
        
        try {
            Map<String, Object> ventasHoy = getVentasHoy();
            Map<String, Object> ventasMes = getVentasMesActual();
            Map<String, Object> ventasAnio = getVentasAnioActual();
            
            resumen.put("ventasHoy", ventasHoy);
            resumen.put("ventasMesActual", ventasMes);
            resumen.put("ventasAnioActual", ventasAnio);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener resumen completo: " + e.getMessage());
        }
        
        return resumen;
    }

    /**
     * Ventas por mes del año actual
     */
    public List<Map<String, Object>> getVentasPorMesAnioActual() {
        try {
            String sql = """
                SELECT 
                    EXTRACT(MONTH FROM fecha_venta) as mes,
                    TO_CHAR(fecha_venta, 'Month') as nombre_mes,
                    NVL(SUM(monto_total_venta), 0) as total_ventas,
                    COUNT(*) as cantidad_ventas
                FROM Venta 
                WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR FROM SYSDATE)
                GROUP BY EXTRACT(MONTH FROM fecha_venta), TO_CHAR(fecha_venta, 'Month')
                ORDER BY mes
                """;
            
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas por mes: " + e.getMessage());
        }
    }

    // --- KPIs  ---
    
    public Map<String, Object> obtenerKPIs(String periodo) {
        Map<String, Object> kpis = new HashMap<>();

        try {
            if (periodo.length() != 6) {
                throw new IllegalArgumentException("Periodo debe ser YYYYMM");
            }
            
            String año = periodo.substring(0, 4);
            String mes = periodo.substring(4);

            // Total de ventas
            Double ventasTotales = jdbcTemplate.queryForObject(
                """
                SELECT NVL(SUM(monto_total_venta), 0)
                FROM Venta
                WHERE EXTRACT(YEAR FROM fecha_venta) = ? 
                  AND EXTRACT(MONTH FROM fecha_venta) = ?
                """,
                Double.class,
                año, mes
            );

            // Clientes nuevos (registrados en ese periodo)
            Integer clientesNuevos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM Cliente 
                WHERE EXTRACT(YEAR FROM fecha_creacion) = ? 
                  AND EXTRACT(MONTH FROM fecha_creacion) = ?
                """,
                Integer.class,
                año, mes
            );

            // Clientes activos (que realizaron compras ese periodo)
            Integer clientesActivos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT p.id_cliente)
                FROM Venta v
                JOIN Pedido p ON v.id_pedido = p.id_pedido
                WHERE EXTRACT(YEAR FROM v.fecha_venta) = ? 
                  AND EXTRACT(MONTH FROM v.fecha_venta) = ?
                """,
                Integer.class,
                año, mes
            );

            kpis.put("ventasTotales", ventasTotales);
            kpis.put("clientesNuevos", clientesNuevos);
            kpis.put("clientesActivos", clientesActivos);

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener KPIs: " + e.getMessage());
        }

        return kpis;
    }

    // --- VENTAS POR CATEGORÍA  ---
    
    public List<Map<String, Object>> getVentasPorCategoria(String periodo) {
        try {
            if (periodo.length() != 6) {
                throw new IllegalArgumentException("Periodo debe ser YYYYMM");
            }
            
            String año = periodo.substring(0, 4);
            String mes = periodo.substring(4);
            
            String sql = """
                SELECT cp.nombre_categoria AS categoria,
                       NVL(SUM(v.monto_total_venta), 0) AS total_ventas
                FROM Venta v
                JOIN Pedido p ON v.id_pedido = p.id_pedido
                JOIN DetallePedido dp ON p.id_pedido = dp.id_pedido
                JOIN Producto pr ON dp.id_producto = pr.id_producto
                JOIN CategoriaProducto cp ON pr.id_categoria = cp.id_categoria
                WHERE EXTRACT(YEAR FROM v.fecha_venta) = ? 
                  AND EXTRACT(MONTH FROM v.fecha_venta) = ?
                GROUP BY cp.nombre_categoria
                ORDER BY total_ventas DESC
            """;

            return jdbcTemplate.queryForList(sql, año, mes);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas por categoría: " + e.getMessage());
        }
    }

    // --- VENTAS POR CIUDAD  ---
    
    public List<Map<String, Object>> getVentasPorCiudad(String periodo) {
        try {
            if (periodo.length() != 6) {
                throw new IllegalArgumentException("Periodo debe ser YYYYMM");
            }
            
            String año = periodo.substring(0, 4);
            String mes = periodo.substring(4);
            
            String sql = """
                SELECT c.nombre_ciudad AS ciudad,
                       NVL(SUM(v.monto_total_venta), 0) AS total_ventas
                FROM Venta v
                JOIN Pedido p ON v.id_pedido = p.id_pedido
                JOIN Cliente cl ON p.id_cliente = cl.id_cliente
                JOIN DireccionCliente dc ON cl.id_cliente = dc.id_cliente
                JOIN Ciudad c ON dc.id_ciudad = c.id_ciudad
                WHERE EXTRACT(YEAR FROM v.fecha_venta) = ? 
                  AND EXTRACT(MONTH FROM v.fecha_venta) = ?
                GROUP BY c.nombre_ciudad
                ORDER BY total_ventas DESC
            """;

            return jdbcTemplate.queryForList(sql, año, mes);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener ventas por ciudad: " + e.getMessage());
        }
    }

    // --- MÉTODOS ADICIONALES ÚTILES ---
    
    /**
     * Productos más vendidos del mes
     */
    public List<Map<String, Object>> getProductosMasVendidos(String periodo) {
        try {
            if (periodo.length() != 6) {
                throw new IllegalArgumentException("Periodo debe ser YYYYMM");
            }
            
            String año = periodo.substring(0, 4);
            String mes = periodo.substring(4);
            
            String sql = """
                SELECT 
                    pr.nombre_producto as producto,
                    SUM(dp.cantidad) as cantidad_vendida,
                    SUM(dp.subtotal_linea) as total_ventas
                FROM Venta v
                JOIN Pedido p ON v.id_pedido = p.id_pedido
                JOIN DetallePedido dp ON p.id_pedido = dp.id_pedido
                JOIN Producto pr ON dp.id_producto = pr.id_producto
                WHERE EXTRACT(YEAR FROM v.fecha_venta) = ? 
                  AND EXTRACT(MONTH FROM v.fecha_venta) = ?
                GROUP BY pr.nombre_producto
                ORDER BY cantidad_vendida DESC
            """;

            return jdbcTemplate.queryForList(sql, año, mes);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener productos más vendidos: " + e.getMessage());
        }
    }
}
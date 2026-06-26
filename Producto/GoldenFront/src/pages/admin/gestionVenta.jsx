import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import '../../styles/gestionVentas.css';
import * as boletaService from '../../services/boletaService';
import * as ventaService from '../../services/ventaService';
import * as pedidosService from '../../services/pedidosService';


function GestionVenta() {
  // Estados para datos
  const [boletas, setBoletas] = useState([]);
  const [boletasFiltradas, setBoletasFiltradas] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [filtroPeriodo, setFiltroPeriodo] = useState('todas'); // 'hoy', 'mes', 'anio', 'todas'

  // Estados de carga y error
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Cargar boletas al montar
  useEffect(() => {
    cargarBoletas();
  }, []);

  // Recargar cuando cambia el filtro de periodo
  useEffect(() => {
    aplicarFiltroPeriodo();
  }, [filtroPeriodo, boletas]);

  // Filtrar boletas cuando cambia la búsqueda
  useEffect(() => {
    filtrarBoletas();
  }, [busqueda, boletas]);

  // Aplicar filtro de periodo a las boletas cargadas
  const aplicarFiltroPeriodo = () => {
    if (filtroPeriodo === 'todas') {
      setBoletasFiltradas(boletas);
      return;
    }

    const ahora = new Date();
    const hoy = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate());
    const primerDiaMes = new Date(ahora.getFullYear(), ahora.getMonth(), 1);
    const primerDiaAnio = new Date(ahora.getFullYear(), 0, 1);

    const boletasFiltradas = boletas.filter(boleta => {
      const fechaVenta = boleta.venta?.fecha_venta || boleta.venta?.fechaVenta;//relacionada con la venta
      if (!fechaVenta) return false;

      const fecha = new Date(fechaVenta);
      
      switch (filtroPeriodo) {
        case 'hoy':
          return fecha >= hoy;
        case 'mes':
          return fecha >= primerDiaMes;
        case 'anio':
          return fecha >= primerDiaAnio;
        default:
          return true;
      }
    });

    setBoletasFiltradas(boletasFiltradas);
  };

  // Cargar todas las boletas
  const cargarBoletas = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('Cargando boletas desde /api/boletas...');
      const boletasData = await boletaService.getBoletas();
      console.log('Boletas cargadas:', boletasData);
      console.log('Primera boleta (estructura):', boletasData[0]);
          
      // Ordenar por ID descendente (más recientes primero)
      const boletasOrdenadas = Array.isArray(boletasData)
        ? boletasData.sort((a, b) => (b.id_boleta || b.idBoleta) - (a.id_boleta || a.idBoleta))
        : [];
      
      setBoletas(boletasOrdenadas);
      setBoletasFiltradas(boletasOrdenadas);
    } catch (err) {
      console.error('Error al cargar boletas:', err);
      console.error('Status:', err.response?.status);
      console.error('URL:', err.config?.url);
      
      let mensajeError = 'Error al cargar boletas';
      
      if (err.response?.status === 404) {
        mensajeError = 'El endpoint /api/boletas no está disponible en el backend.';
      } else if (err.response?.status === 403) {
        mensajeError = 'No tienes permisos para acceder a las boletas.';
      } else if (err.response?.status === 401) {
        mensajeError = 'Sesión expirada. Por favor inicia sesión nuevamente.';
      } else if (err.message === 'Network Error') {
        mensajeError = 'Error de conexión. Verifica que el backend esté corriendo en http://localhost:8080';
      } else {
        mensajeError = `Error: ${err.response?.data?.message || err.message}`;
      }
      
      setError(mensajeError);
      setBoletas([]);
      setBoletasFiltradas([]);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar boletas por ID, ID Pedido o Total
  const filtrarBoletas = () => {
    if (!busqueda.trim()) {
      setBoletasFiltradas(boletas);
      return;
    }

    const terminoBusqueda = busqueda.toLowerCase().trim();
    const filtradas = boletas.filter(boleta => {
      const idBoleta = (boleta.id_boleta || boleta.idBoleta || '').toString();
      const idVenta = (boleta.venta?.id_venta || boleta.venta?.idVenta || '').toString();
      const idPedido = (boleta.venta?.id_pedido || boleta.venta?.idPedido || '').toString();
      const total = (boleta.venta?.total_venta || boleta.venta?.totalVenta || 0).toString();
      const numeroSII = (boleta.numero_sii || boleta.numeroSii || boleta.folio_sii || '').toString();

      return idBoleta.includes(terminoBusqueda) ||
             idVenta.includes(terminoBusqueda) ||
             idPedido.includes(terminoBusqueda) ||
             total.includes(terminoBusqueda) ||
             numeroSII.toLowerCase().includes(terminoBusqueda);
    });

    setBoletasFiltradas(filtradas);
  };

  // Eliminar boleta, venta y revertir pedido a Cancelado
  const handleEliminarBoleta = async (id) => {
    if (window.confirm('¿Está seguro de que desea eliminar esta boleta y su venta? El pedido será marcado como "Cancelado".')) {
      setLoading(true);
      try {
        // 1. Obtener la boleta para acceder al ID de venta y pedido
        const boleta = boletas.find(b => (b.idBoleta || b.id_boleta) === id);
        if (!boleta) {
          throw new Error('Boleta no encontrada');
        }

        const idVenta = boleta.venta?.idVenta || boleta.venta?.id_venta;
        const idPedido = boleta.venta?.idPedido || boleta.venta?.id_pedido;

        console.log('Iniciando eliminación:');
        console.log('  - ID Boleta:', id);
        console.log('  - ID Venta:', idVenta);
        console.log('  - ID Pedido:', idPedido);

        // 2. Eliminar la boleta primero (tiene FK a Venta)
        await boletaService.eliminarBoleta(id);
        console.log('Boleta eliminada');

        // 3. Eliminar la venta (tiene FK a Pedido)
        if (idVenta) {
          await ventaService.eliminarVenta(idVenta);
          console.log(' Venta eliminada');
        }

        // 4. Revertir estado del pedido a "Cancelado" (estado 7)
        // Esto marca el pedido como cancelado al eliminar la venta
        if (idPedido) {
          try {
            await pedidosService.actualizarEstadoPedido(idPedido, 7);
            console.log('Pedido marcado como Cancelado');
          } catch (err) {
            console.warn('No se pudo cambiar el estado del pedido:', err.message);
            console.warn('  El pedido quedará con su estado actual');
          }
        }
        
        // 5. Recargar boletas después de eliminar
        await cargarBoletas();
        
        alert('Operación exitosa:\n- Boleta eliminada\n- Venta eliminada\n- Pedido marcado como "Cancelado"');
      } catch (err) {
        console.error(' Error al eliminar:', err);
        alert('Error al eliminar: ' + err.message);
      } finally {
        setLoading(false);
      }
    }
  };

  // Formatear fecha
  const formatearFecha = (fecha) => {
    if (!fecha) return '-';
    try {
      const date = new Date(fecha);
      return date.toLocaleDateString('es-CL', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return fecha;
    }
  };

  // Formatear moneda
  const formatearMoneda = (monto) => {
    return `$${(monto || 0).toLocaleString('es-CL', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  return (
    <div className="admin-layout-ventas">
      <Sidebar />
      <div className="content-ventas">
        <h1>Gestión de Ventas</h1>

        {error && (
          <div className="mensaje-error">
            {error}
          </div>
        )}

        {/* Filtros de periodo */}
        <div className="filtros-periodo">
          <button
            className={`btn-filtro ${filtroPeriodo === 'todas' ? 'activo' : ''}`}
            onClick={() => setFiltroPeriodo('todas')}
            disabled={loading}
          >
            Todas
          </button>
          <button
            className={`btn-filtro ${filtroPeriodo === 'hoy' ? 'activo' : ''}`}
            onClick={() => setFiltroPeriodo('hoy')}
            disabled={loading}
          >
            Hoy
          </button>
          <button
            className={`btn-filtro ${filtroPeriodo === 'mes' ? 'activo' : ''}`}
            onClick={() => setFiltroPeriodo('mes')}
            disabled={loading}
          >
            Este Mes
          </button>
          <button
            className={`btn-filtro ${filtroPeriodo === 'anio' ? 'activo' : ''}`}
            onClick={() => setFiltroPeriodo('anio')}
            disabled={loading}
          >
            Este Año
          </button>
        </div>

        {/* Barra de búsqueda */}
        <div className="barra-busqueda">
          <input
            type="text"
            className="input-busqueda"
            placeholder="Buscar por ID Venta, ID Pedido, Total o Número SII..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
          <button
            className="btn-limpiar"
            onClick={() => setBusqueda('')}
          >
            Limpiar
          </button>
          <button
            className="btn-recargar"
            onClick={cargarBoletas}
            disabled={loading}
          >
            Recargar
          </button>
        </div>

        {/* Tabla de ventas */}
        <div className="tabla-container-ventas">
          <div className="tabla-scroll">
            <table className="tabla-ventas">
              <thead>
                <tr>
                  <th>ID Venta</th>
                  <th>ID Pedido</th>
                  <th>Fecha Venta</th>
                  <th>Total Venta</th>
                  <th>Número SII</th>
                  <th>IVA</th>
                  <th>Total con IVA</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="8">
                      <div className="estado-cargando">
                        Cargando ventas...
                      </div>
                    </td>
                  </tr>
                ) : boletasFiltradas.length > 0 ? (
                  boletasFiltradas.map(boleta => {
                    // Extraer datos de la boleta (nivel superior)
                    const idBoleta = boleta.id_boleta || boleta.idBoleta;
                    const numeroSII = boleta.numero_sii || boleta.numeroSii || '-';
                    const iva = boleta.iva || 0;
                    const totalConIva = boleta.totalConIva || 0;
                    
                    // Extraer datos de la venta (anidada)
                    const venta = boleta.venta || {};
                    const idVenta = venta.id_venta || venta.idVenta;
                    const idPedido = venta.id_pedido || venta.idPedido;
                    const fechaVenta = venta.fecha_venta || venta.fechaVenta;
                    const totalVenta = venta.total_venta || venta.totalVenta || 0;

                    return (
                      <tr key={idBoleta}>
                        <td>
                          <span className="venta-id">{idVenta}</span>
                        </td>
                        <td>
                          <span className="pedido">
                            Pedido #{idPedido}
                          </span>
                        </td>
                        <td>{formatearFecha(fechaVenta)}</td>
                        <td>
                          <span className="monto-venta">
                            {formatearMoneda(totalVenta)}
                          </span>
                        </td>
                        <td>
                          <span className="badge-sii">
                            {numeroSII}
                          </span>
                        </td>
                        <td>
                          {formatearMoneda(iva)}
                        </td>
                        <td>
                          <span className="monto-total-iva">
                            {formatearMoneda(totalConIva)}
                          </span>
                        </td>
                        <td>
                          <button
                            onClick={() => handleEliminarBoleta(idBoleta)}
                            disabled={loading}
                            className="btn-eliminar-venta"
                          >
                            Eliminar
                          </button>
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan="8">
                      <div className="estado-vacio">
                        {busqueda ? 'No se encontraron ventas con ese criterio de búsqueda' : 'No hay ventas registradas'}
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* Resumen */}
          {boletasFiltradas.length > 0 && (
            <div className="resumen-ventas">
              <div className="resumen-contador">
                Mostrando <strong>{boletasFiltradas.length}</strong> de <strong>{boletas.length}</strong> boletas
              </div>
              <div className="resumen-total">
                Total ventas mostradas: <strong>{formatearMoneda(
                  boletasFiltradas.reduce((sum, b) => sum + (b.venta?.total_venta || 0), 0)
                )}</strong>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default GestionVenta;

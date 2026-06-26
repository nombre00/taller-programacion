// src/pages/admin/GestionPedidos.jsx


import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import '../../styles/gestionPedidos.css';
import * as pedidosService from '../../services/pedidosService';
import * as usuariosService from '../../services/usuariosService';
import * as productosService from '../../services/productosService';
import * as ventaService from '../../services/ventaService';
import * as boletaService from '../../services/boletaService';

function GestionPedidos() {
  // Estados del formulario
  const [idCliente, setIdCliente] = useState('');
  const [idProducto, setIdProducto] = useState('');
  const [cantidad, setCantidad] = useState('1');
  const [idEstadoPedido, setIdEstadoPedido] = useState('1');
  const [idMetodoPago, setIdMetodoPago] = useState('');
  const [idTipoEntrega, setIdTipoEntrega] = useState('');
  const [idDireccion, setIdDireccion] = useState('');
  const [montoEnvio, setMontoEnvio] = useState('0');
  const [notaCliente, setNotaCliente] = useState('');
  
  // Estado para carrito temporal de productos
  const [productosCarrito, setProductosCarrito] = useState([]);

  // Estados para datos
  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [metodosPago, setMetodosPago] = useState([]);
  const [tiposEntrega, setTiposEntrega] = useState([]);
  const [direccionesCliente, setDireccionesCliente] = useState([]);

  // Estados de carga
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Estado para búsqueda de pedidos por cliente
  const [busquedaCliente, setBusquedaCliente] = useState('');
  const [pedidosFiltrados, setPedidosFiltrados] = useState([]);

  // CAMBIO 1: tiempoEspera parte vacío (no en 20)
  const [modalProcesar, setModalProcesar] = useState({ visible: false, idPedido: null, tiempoEspera: '' });

  // Filtrar pedidos automáticamente cuando cambia la búsqueda
  useEffect(() => {
    if (!busquedaCliente.trim()) {
      setPedidosFiltrados([]);
      return;
    }
    handleBuscarPorCliente();
  }, [busquedaCliente, clientes, pedidos]);

  // Cargar datos al montar
  useEffect(() => {
    inicializarDatos();
  }, []);

  // Monitorear cambios en clientes
  useEffect(() => {
    console.log(' Estado de clientes actualizado. Total:', clientes.length);
    if (clientes.length > 0) {
      console.log(' Lista de clientes disponibles:', clientes.map(c => ({ 
        id: c.idCliente , 
        nombre:c.nombreCliente ,
        todosLosCampos: c
      })));
    }
  }, [clientes]);

  // Cargar direcciones cuando cambia el cliente
  useEffect(() => {
    if (idCliente) {
      cargarDireccionesCliente(idCliente);
    } else {
      setDireccionesCliente([]);
      setIdDireccion('');
    }
  }, [idCliente]);

  // Helper: Obtener nombre del cliente por ID
  const getNombreCliente = (idCliente) => {
    if (!idCliente || !clientes.length) return 'N/A';
    const cliente = clientes.find(c => (c.idCliente) === idCliente);
    return cliente?.nombreCliente || 'N/A';
  };

  // Helper: Obtener nombre del método de pago por ID
  const getNombreMetodoPago = (idMetodoPago) => {
    if (!idMetodoPago) return '-';
    const mapeoMetodosPago = {
      1: 'Webpay',
      2: 'Efectivo',
      3: 'Mercado Pago'
    };
    return mapeoMetodosPago[idMetodoPago] || `Método ${idMetodoPago}`;
  };

  // Helper: Obtener nombre del tipo de entrega por ID
  const getNombreTipoEntrega = (idTipoEntrega) => {
    if (!idTipoEntrega) return '-';
    const mapeoTiposEntrega = {
      1: 'Delivery',
      2: 'Retiro en Local'
    };
    return mapeoTiposEntrega[idTipoEntrega] || `Tipo ${idTipoEntrega}`;
  };

  // Helper: Obtener nombre del estado por ID
  const getNombreEstadoPedido = (idEstadoPedido) => {
    if (!idEstadoPedido) return 'Pendiente';
    const mapeoEstados = {
      1: 'Pendiente de Pago',
      2: 'Pagado',
      7: 'Cancelado'
    };
    return mapeoEstados[idEstadoPedido] || `Estado ${idEstadoPedido}`;
  };

  // Inicializar todos los datos
  const inicializarDatos = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('Cargando datos iniciales...');
      
      const token = localStorage.getItem('authToken');
      const user = localStorage.getItem('user');
      console.log('Token disponible:', !!token);
      console.log('Token (primeros 50 chars):', token?.substring(0, 50) + '...');
      console.log('Usuario:', user);
      
      try {
        console.log('Llamando a getPedidos()...');
        const pedidosData = await pedidosService.getPedidos();
        const pedidosOrdenados = Array.isArray(pedidosData) 
          ? pedidosData.sort((a, b) => b.idPedido - a.idPedido)
          : [];
        setPedidos(pedidosOrdenados);
        console.log('Pedidos cargados correctamente:', pedidosData.length, 'items');
      } catch (err) {
        console.warn('Error cargando pedidos:', err);
        console.warn('Status:', err.response?.status);
        console.warn('Data:', err.response?.data);
        console.warn('Mensaje:', err.response?.data?.message || err.message);
        setPedidos([]);
        setError(`Error al cargar pedidos: ${err.response?.status || 'desconocido'} - ${err.response?.data?.message || err.message}`)
      }
      
      try {
        console.log('Llamando a obtenerTodosClientes()...');
        const clientesData = await usuariosService.obtenerTodosClientes();
        setClientes(Array.isArray(clientesData) ? clientesData : []);
        console.log('Clientes cargados:', clientesData.length, 'clientes');
        console.log(' Detalle clientes:', clientesData);
      } catch (err) {
        console.warn('Error cargando clientes:', err);
        console.warn('Status:', err.response?.status);
        console.warn('Mensaje:', err.response?.data?.message || err.message);
        setClientes([]);
        if (!error) {
          setError('No se pudieron cargar los clientes. Error 500 en el backend.');
        }
      }
      
      setMetodosPago([
        { idMetodoPago: 1, nombre: 'Webpay' },
        { idMetodoPago: 2, nombre: 'Efectivo' },
        { idMetodoPago: 3, nombre: 'Mercado Pago' }
      ]);
      
      setTiposEntrega([
        { idTipoEntrega: 1, nombre: 'Delivery' },
        { idTipoEntrega: 2, nombre: 'Retiro en Local' }
      ]);
      
      try {
        console.log('Cargando productos disponibles desde /api/catalogo/productos...');
        const productosData = await productosService.obtenerProductosDisponibles();
        setProductos(Array.isArray(productosData) ? productosData : []);
        console.log('Productos cargados desde backend:', productosData.length, 'productos');
        console.log(' Muestra de productos:', productosData.slice(0, 2));
      } catch (err) {
        console.error(' Error cargando productos desde backend:', err);
        console.error('Status:', err.response?.status);
        console.error('Mensaje:', err.response?.data?.message || err.message);
        setProductos([]);
        setError('No se pudieron cargar los productos del catálogo.');
      }
      
    } catch (err) {
      setError(err.message);
      console.error('Error cargando datos:', err);
    } finally {
      setLoading(false);
    }
  };

  // Cargar direcciones de un cliente
  const cargarDireccionesCliente = async (id) => {
    try {
      const response = await usuariosService.obtenerDireccionesPorCliente(id);
      setDireccionesCliente(Array.isArray(response) ? response : []);
    } catch (err) {
      console.error('Error cargando direcciones:', err);
      setDireccionesCliente([]);
    }
  };

  // Obtener precio del producto
  const obtenerPrecioProducto = (productoId = idProducto) => {
    const producto = productos.find(p => (p.idProducto || p.id) == productoId);
    return producto ? (producto.precioBase || producto.precio || 0) : 0;
  };
  
  // Obtener nombre del producto
  const getNombreProducto = (productoId) => {
    const producto = productos.find(p => (p.idProducto || p.id) == productoId);
    return producto ? (producto.nombreProducto || producto.nombre || 'Producto') : 'Producto';
  };

  // Función para buscar pedidos por nombre de cliente
  const handleBuscarPorCliente = async () => {
    if (!busquedaCliente.trim()) {
      setPedidosFiltrados([]);
      return;
    }
    
    try {
      const clientesEncontrados = clientes.filter(c => 
        (c.nombreCliente)
          .toLowerCase()
          .includes(busquedaCliente.toLowerCase())
      );

      if (clientesEncontrados.length === 0) {
        setPedidosFiltrados([]);
        return;
      }

      const promesas = clientesEncontrados.map(cliente => {
        const idCliente = cliente.idCliente;
        return pedidosService.getPedidosPorCliente(idCliente).catch(() => []);
      });

      const resultados = await Promise.all(promesas); 
      const todosPedidos = resultados.flat();
      
      const pedidosUnicos = todosPedidos.reduce((acc, pedido) => {
        const idPedido = pedido.idPedido;
        if (!acc.find(p => p.idPedido === idPedido)) {
          acc.push(pedido);
        }
        return acc;
      }, []);
      
      setPedidosFiltrados(pedidosUnicos);
    } catch (err) {
      console.warn('Error al buscar pedidos:', err);
      setPedidosFiltrados([]);
    }
  };

  // Función para limpiar la búsqueda y mostrar todos los pedidos
  const handleLimpiarBusqueda = () => {
    setBusquedaCliente('');
    setPedidosFiltrados([]);
  };

  const pedidosAMostrar = busquedaCliente.trim() ? pedidosFiltrados : pedidos;
  
  // Agregar producto al carrito temporal
  const handleAgregarProducto = (e) => {
    e.preventDefault();

    if (!idCliente) {
      alert('Por favor, seleccione un CLIENTE antes de agregar productos');
      return;
    }

    if (!idMetodoPago) {
      alert('Por favor, seleccione un MÉTODO DE PAGO antes de agregar productos');
      return;
    }

    if (!idTipoEntrega) {
      alert('Por favor, seleccione un TIPO DE ENTREGA antes de agregar productos');
      return;
    }

    if (!idProducto || !cantidad || cantidad <= 0) {
      alert('Seleccione un producto y cantidad válida');
      return;
    }

    const precioUnitario = obtenerPrecioProducto(idProducto);
    const cantidadNum = parseInt(cantidad);
    const subtotal = precioUnitario * cantidadNum;

    const nuevoProducto = {
      id: Date.now(),
      idProducto: parseInt(idProducto),
      nombreProducto: getNombreProducto(idProducto),
      cantidad: cantidadNum,
      precioUnitario: precioUnitario,
      subtotal: subtotal
    };

    setProductosCarrito([...productosCarrito, nuevoProducto]);
    setIdProducto('');
    setCantidad('1');
  };
  
  // Eliminar producto del carrito temporal
  const handleEliminarProductoCarrito = (id) => {
    setProductosCarrito(productosCarrito.filter(p => p.id !== id));
  };
  
  // Calcular subtotal del carrito
  const calcularSubtotalCarrito = () => {
    return productosCarrito.reduce((sum, p) => sum + p.subtotal, 0);
  };

  // Crear pedido
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (productosCarrito.length === 0) {
      alert('Debes agregar al menos un producto antes de crear el pedido.');
      return;
    }

    if (!idCliente || !idEstadoPedido || !idMetodoPago || !idTipoEntrega) {
      alert('Por favor, complete todos los campos obligatorios.');
      return;
    }

    const subtotal = calcularSubtotalCarrito();
    const envio = parseFloat(montoEnvio) || 0;
    const total = subtotal + envio;

    const nuevoPedido = {
      idCliente: parseInt(idCliente),
      idEstadoPedido: parseInt(idEstadoPedido),
      idMetodoPago: parseInt(idMetodoPago),
      idTipoEntrega: parseInt(idTipoEntrega),
      idDireccionEntrega: idDireccion ? parseInt(idDireccion) : null,
      montoSubtotal: subtotal,
      montoEnvio: envio,
      montoTotal: total,
      notaCliente: notaCliente,
      detalles: productosCarrito.map(p => ({
        idProducto: parseInt(p.idProducto),
        cantidad: p.cantidad,
        precioUnitario: p.precioUnitario,
        subtotalLinea: p.subtotal
      }))
    };

    setLoading(true);
    try {
      console.log(' Enviando pedido al backend:', nuevoPedido);
      const response = await pedidosService.crearPedido(nuevoPedido);
      console.log(' Respuesta del backend:', response);
      
      const pedidosActualizados = await pedidosService.getPedidos();
      const pedidosOrdenados = Array.isArray(pedidosActualizados)
        ? pedidosActualizados.sort((a, b) => b.idPedido - a.idPedido)
        : [];
      setPedidos(pedidosOrdenados);
      
      alert('Pedido creado exitosamente');

      setIdCliente('');
      setIdProducto('');
      setCantidad('1');
      setIdEstadoPedido('1');
      setIdMetodoPago('');
      setIdTipoEntrega('');
      setIdDireccion('');
      setMontoEnvio('0');
      setNotaCliente('');
      setProductosCarrito([]);
    } catch (err) {
      console.error(' Error al crear pedido:', err);
      setError(err.message);
      alert('Error al crear pedido: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Eliminar pedido (con Boleta y Venta si existen)
  const handleEliminarPedido = async (id) => {
    let tieneVenta = false;
    try {
      const ventas = await ventaService.getVentas();
      const venta = ventas.find(v => (v.idPedido || v.id_pedido) === id);
      tieneVenta = !!venta;
    } catch (err) {
      console.warn('No se pudo verificar ventas:', err.message);
    }

    const mensaje = tieneVenta 
      ? '¿Está seguro de que desea CANCELAR este pedido?\n\nEsto eliminará:\n- La venta asociada\n- La boleta (si existe)\n- El estado cambiará a "Cancelado"'
      : '¿Está seguro de que desea ELIMINAR este pedido?\n\nEl pedido será eliminado permanentemente.';

    if (window.confirm(mensaje)) {
      setLoading(true);
      try {
        console.log('Iniciando eliminación del pedido:', id);
        
        let idVenta = null;
        let idBoleta = null;
        
        try {
          const ventas = await ventaService.getVentas();
          const venta = ventas.find(v => (v.idPedido || v.id_pedido) === id);
          
          if (venta) {
            idVenta = venta.idVenta || venta.id_venta;
            console.log('Venta encontrada:', idVenta);
            
            try {
              const boletas = await boletaService.getBoletas();
              const boleta = boletas.find(b => {
                const idVentaBoleta = b.venta?.idVenta || b.venta?.id_venta;
                return idVentaBoleta === idVenta;
              });
              
              if (boleta) {
                idBoleta = boleta.idBoleta || boleta.id_boleta;
                console.log('Boleta encontrada:', idBoleta);
              }
            } catch (err) {
              console.warn('No se pudo buscar boletas:', err.message);
            }
          }
        } catch (err) {
          console.warn('No se pudo buscar ventas:', err.message);
        }
        
        if (idBoleta) {
          try {
            await boletaService.eliminarBoleta(idBoleta);
            console.log('Boleta eliminada');
          } catch (err) {
            console.warn('No se pudo eliminar boleta:', err.message);
          }
        }
        
        if (idVenta) {
          try {
            await ventaService.eliminarVenta(idVenta);
            console.log('Venta eliminada');
            
            try {
              await pedidosService.actualizarEstadoPedido(id, 7);
              console.log('Estado del pedido cambiado a Cancelado');
            } catch (err) {
              console.warn('No se pudo actualizar el estado del pedido:', err.message);
            }
          } catch (err) {
            console.warn('No se pudo eliminar venta:', err.message);
          }
        } else {
          try {
            await pedidosService.eliminarPedido(id);
            console.log('Pedido eliminado');
          } catch (err) {
            console.error('Error al eliminar pedido:', err);
            throw err;
          }
        }
        
        const pedidosActualizados = await pedidosService.getPedidos();
        const pedidosOrdenados = Array.isArray(pedidosActualizados)
          ? pedidosActualizados.sort((a, b) => b.idPedido - a.idPedido)
          : [];
        setPedidos(pedidosOrdenados);
        
        if (idVenta) {
          alert('Venta cancelada exitosamente' + 
                (idBoleta ? '\n- Boleta eliminada' : '') + 
                '\n- Estado del pedido cambiado a Cancelado');
        } else {
          alert('Pedido eliminado exitosamente');
        }
      } catch (err) {
        console.error('Error al eliminar:', err);
        alert('Error al eliminar: ' + err.message);
      } finally {
        setLoading(false);
      }
    }
  };

  // CAMBIO 2: handleMarcarComoPagado recibe tiempoEspera como parámetro
  // null = sin WhatsApp (flujo V1), número = con WhatsApp (flujo V3)
  const handleMarcarComoPagado = async (tiempoEspera) => {
    const { idPedido } = modalProcesar;
    setModalProcesar({ visible: false, idPedido: null, tiempoEspera: '' });
    setLoading(true);
    try {
      await pedidosService.actualizarPedidoAPagado(idPedido, tiempoEspera);
      setPedidos(pedidos.map(p =>
        p.idPedido === idPedido ? { ...p, idEstadoPedido: 2 } : p
      ));
      const mensaje = tiempoEspera != null
        ? 'Pedido marcado como pagado. WhatsApp enviado al cliente.'
        : 'Pedido marcado como pagado sin notificación WhatsApp.';
      alert(mensaje);
    } catch (err) {
      console.error('Error al procesar pago:', err);
      alert('Error al procesar pago: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleAdminLogout = () => {
    console.log('Cerrando sesión...');
  };

  return (
    <div className="admin-layout-pedidos">
      <Sidebar onLogoutAdmin={handleAdminLogout} />

      <div className="content">
        <h1>Gestión de Pedidos</h1>

        {error && (
          <div style={{
            background: '#dc3545',
            color: 'white',
            padding: '15px',
            borderRadius: '5px',
            marginBottom: '20px'
          }}>
            Error: {error}
          </div>
        )}

        <div className="pedidos-wrapper-vertical">
          {/* CREAR PEDIDO */}
          <div className="crear-pedido-container-full">
            <h2>Crear Nuevo Pedido</h2>

            <form className="formPedidos" onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Cliente *</label>
                  <select
                    required
                    value={idCliente}
                    onChange={(e) => setIdCliente(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  >
                    <option value="">Seleccione Cliente</option>
                    {clientes.map(cliente => (
                      <option 
                        key={cliente.idCliente} 
                        value={cliente.idCliente}
                      >
                        {cliente.NOMBRE_CLIENTE || cliente.nombreCliente || cliente.nombre}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Estado del Pedido *</label>
                  <select
                    required
                    value={idEstadoPedido}
                    onChange={(e) => setIdEstadoPedido(e.target.value)}
                    className="form-input"
                    disabled={true}
                  >
                    <option value="1">Pendiente de Pago</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Producto ({productos.length} disponibles)</label>
                  <select
                    value={idProducto}
                    onChange={(e) => setIdProducto(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  >
                    <option value="">Seleccione Producto</option>
                    {productos.length === 0 && (
                      <option value="" disabled>No hay productos disponibles</option>
                    )}
                    {productos.map(producto => {
                      const id = producto.idProducto || producto.id;
                      const nombre = producto.nombreProducto || producto.nombre || producto.name || 'Sin nombre';
                      const precio = producto.precioBase || producto.precio || producto.price || 0;
                      return (
                        <option key={id} value={id}>
                          {nombre} - ${Number(precio).toFixed(2)}
                        </option>
                      );
                    })}
                  </select>
                </div>
                <div className="form-group">
                  <label>Cantidad</label>
                  <input
                    type="number"
                    min="1"
                    value={cantidad}
                    onChange={(e) => setCantidad(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Método de Pago *</label>
                  <select
                    required
                    value={idMetodoPago}
                    onChange={(e) => setIdMetodoPago(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  >
                    <option value="">Seleccione Método</option>
                    {metodosPago.map(metodo => (
                      <option key={metodo.idMetodoPago || metodo.id} value={metodo.idMetodoPago || metodo.id}>
                        {metodo.nombre}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Tipo de Entrega *</label>
                  <select
                    required
                    value={idTipoEntrega}
                    onChange={(e) => setIdTipoEntrega(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  >
                    <option value="">Seleccione Tipo</option>
                    {tiposEntrega.map(tipo => (
                      <option key={tipo.idTipoEntrega} value={tipo.idTipoEntrega}>
                        {tipo.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Dirección de Entrega</label>
                  <select
                    value={idDireccion}
                    onChange={(e) => setIdDireccion(e.target.value)}
                    className="form-input"
                    disabled={loading || !idCliente || direccionesCliente.length === 0}
                  >
                    <option value="">Seleccione dirección</option>
                    {direccionesCliente.map(dir => (
                      <option key={dir.idDireccion} value={dir.idDireccion}>
                        {dir.alias ? `${dir.alias} - ` : ''}{dir.direccion}
                      </option>
                    ))}
                  </select>
                  {idCliente && direccionesCliente.length === 0 && (
                    <small style={{ color: '#999', fontSize: '0.85rem', marginTop: '4px', display: 'block' }}>
                      El cliente no tiene direcciones registradas
                    </small>
                  )}
                </div>

                <div className="form-group">
                  <label>Monto de Envío ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={montoEnvio}
                    onChange={(e) => setMontoEnvio(e.target.value)}
                    className="form-input"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="form-row full">
                <div className="form-group">
                  <label>Nota del Cliente</label>
                  <textarea
                    value={notaCliente}
                    onChange={(e) => setNotaCliente(e.target.value)}
                    className="form-input form-textarea"
                    rows="2"
                    disabled={loading}
                    placeholder="Agregue notas adicionales..."
                  />
                </div>
              </div>

              {productosCarrito.length > 0 && (
                <div className="carrito-productos">
                  <h3>Productos en el Pedido</h3>
                  <div className="tabla-carrito-wrapper">
                    <table className="tabla-carrito">
                      <thead>
                        <tr>
                          <th>Producto</th>
                          <th>Precio Unit.</th>
                          <th>Cantidad</th>
                          <th>Subtotal</th>
                          <th>Acción</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productosCarrito.map((item) => (
                          <tr key={item.id}>
                            <td>{item.nombreProducto}</td>
                            <td>${item.precioUnitario.toFixed(2)}</td>
                            <td>{item.cantidad}</td>
                            <td><strong>${item.subtotal.toFixed(2)}</strong></td>
                            <td>
                              <button 
                                type="button"
                                className="btn-eliminar"
                                onClick={() => handleEliminarProductoCarrito(item.id)}
                                style={{ fontSize: '0.85em', padding: '5px 10px', width: 'auto' }}
                              >
                                 Quitar
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              <div className="resumen-pedido">
                <div className="resumen-fila">
                  <span className="resumen-label">Subtotal Productos:</span>
                  <span className="resumen-valor">${calcularSubtotalCarrito().toFixed(2)}</span>
                </div>
                <div className="resumen-fila" style={{ color: '#999', fontSize: '0.9em' }}>
                  <span className="resumen-label">IVA (19% sobre productos):</span>
                  <span className="resumen-valor">${(calcularSubtotalCarrito() * 0.19).toFixed(2)}</span>
                </div>
                <div className="resumen-fila">
                  <span className="resumen-label">Envío:</span>
                  <span className="resumen-valor">${(parseFloat(montoEnvio) || 0).toFixed(2)}</span>
                </div>
                <div className="resumen-fila total" style={{ borderTop: '2px solid #ffcc00', paddingTop: '10px', marginTop: '8px' }}>
                  <span className="resumen-label-total">TOTAL A PAGAR (con IVA):</span>
                  <span className="resumen-valor-total">${(calcularSubtotalCarrito() * 1.19 + (parseFloat(montoEnvio) || 0)).toFixed(2)}</span>
                </div>
                <small style={{ display: 'block', marginTop: '8px', color: '#999', fontSize: '0.85em', fontStyle: 'italic' }}>
                  * El IVA se registrará en la boleta al momento del pago
                </small>
              </div>

              <div className="form-actions">
                <button 
                  type="button" 
                  className="btn-agregar-producto" 
                  onClick={handleAgregarProducto}
                  disabled={loading || !idProducto || !cantidad}
                >
                   Agregar Producto
                </button>
                <button type="submit" className="btn-agregar" disabled={loading}>
                  {loading ? 'Creando...' : ' Crear Pedido'}
                </button>
                <button
                  type="button"
                  className="btn-limpiar"
                  onClick={() => {
                    setIdCliente('');
                    setIdProducto('');
                    setCantidad('1');
                    setIdEstadoPedido('1');
                    setIdMetodoPago('');
                    setIdTipoEntrega('');
                    setIdDireccion('');
                    setMontoEnvio('0');
                    setNotaCliente('');
                    setProductosCarrito([]);
                  }}
                  disabled={loading}
                >
                  Limpiar Todo
                </button>
              </div>
            </form>
          </div>

          {/* LISTA DE PEDIDOS */}
          <div className="lista-pedidos-container-full">
            <h2>Pedidos Registrados</h2>
            
            <div style={{ 
              marginBottom: '20px', 
              padding: '15px', 
              background: '#2d2d2d', 
              borderRadius: '8px',
              border: '1px solid #555',
              display: 'flex',
              gap: '10px',
              alignItems: 'center'
            }}>
              <input
                type="text"
                placeholder="Buscar por nombre de cliente..."
                value={busquedaCliente}
                onChange={(e) => setBusquedaCliente(e.target.value)}
                style={{
                  flex: 1,
                  padding: '10px 12px',
                  border: '1px solid #555',
                  borderRadius: '5px',
                  fontSize: '14px',
                  background: '#1a1a1a',
                  color: '#fdfdfd'
                }}
                disabled={loading}
              />
              {busquedaCliente.trim() && (
                <button
                  onClick={handleLimpiarBusqueda}
                  style={{
                    padding: '10px 20px',
                    background: '#555',
                    color: '#fdfdfd',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 'bold'
                  }}
                >
                  ✖ Limpiar
                </button>
              )}
            </div>

            {pedidosFiltrados.length > 0 && (
              <div style={{
                padding: '10px 15px',
                background: '#2d2d2d',
                color: '#ffcc00',
                border: '1px solid #555',
                borderRadius: '5px',
                marginBottom: '15px',
                fontSize: '14px',
                fontWeight: '600'
              }}>
                 Mostrando {pedidosFiltrados.length} pedido(s) filtrado(s) por cliente
              </div>
            )}

            <div className="table-responsive">
              <table className="tabla-pedidos">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Fecha</th>
                    <th>Productos</th>
                    <th>Estado</th>
                    <th>Método Pago</th>
                    <th>Tipo Entrega</th>
                    <th>Subtotal</th>
                    <th>Envío</th>
                    <th>Total</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidosAMostrar.length > 0 ? (
                    pedidosAMostrar.map(pedido => (
                      <tr key={pedido.ID_PEDIDO || pedido.idPedido}>
                        <td>{pedido.ID_PEDIDO || pedido.idPedido}</td>
                        <td>{getNombreCliente(pedido.ID_CLIENTE || pedido.idCliente)}</td>
                        <td>{pedido.fechaPedido ? new Date(pedido.fechaPedido).toLocaleString() : 'Sin fecha'}</td>
                        <td>
                          {pedido.detalles && pedido.detalles.length > 0 ? (
                            <div style={{ fontSize: '0.9em' }}>
                              {pedido.detalles.map((detalle, index) => (
                                <div key={index} style={{ marginBottom: '5px', borderBottom: index < pedido.detalles.length - 1 ? '1px solid #eee' : 'none', paddingBottom: '5px' }}>
                                  <strong>{getNombreProducto(detalle.idProducto || detalle.ID_PRODUCTO)}</strong>
                                  <br />
                                  <span style={{ color: '#666' }}>
                                    Cant: {detalle.cantidad} × ${(detalle.precioUnitario)?.toFixed(2) || '0.00'} = ${(detalle.subtotalLinea)?.toFixed(2) || '0.00'}
                                  </span>
                                </div>
                              ))}
                              <div style={{ marginTop: '8px', paddingTop: '5px', borderTop: '2px solid #333', fontWeight: 'bold' }}>
                                Total items: {pedido.detalles.reduce((sum, d) => sum + (d.CANTIDAD || d.cantidad || 0), 0)}
                              </div>
                            </div>
                          ) : (
                            <span style={{ color: '#999' }}>Sin productos</span>
                          )}
                        </td>
                        <td>
                          <span className="badge-estado">
                            {getNombreEstadoPedido(pedido.idEstadoPedido)}
                          </span>
                        </td>
                        <td>{getNombreMetodoPago(pedido.idMetodoPago)}</td>
                        <td>{getNombreTipoEntrega(pedido.idTipoEntrega)}</td>
                        <td>${(pedido.montoSubtotal || 0).toFixed(2)}</td>
                        <td>${(pedido.montoEnvio || 0).toFixed(2)}</td>
                        <td><strong>${(pedido.montoTotal || 0).toFixed(2)}</strong></td>
                        <td>
                          <div style={{ display: 'flex', gap: '5px', flexDirection: 'column' }}>
                            {/* CAMBIO 3: tiempoEspera inicial vacío */}
                            {(pedido.idEstadoPedido) === 1 && (
                              <button
                                className="btn-agregar"
                                onClick={() => setModalProcesar({ visible: true, idPedido: pedido.idPedido, tiempoEspera: '' })}
                                disabled={loading}
                                style={{ fontSize: '0.85em', padding: '5px 10px' }}
                              >
                                ✅ Marcar Pagado
                              </button>
                            )}
                            <button
                              className="btn-eliminar"
                              onClick={() => handleEliminarPedido(pedido.idPedido)}
                              disabled={loading}
                              style={{ fontSize: '0.85em', padding: '5px 10px' }}
                            >
                               Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="10" style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
                        No hay pedidos registrados
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* CAMBIO 4: Modal con dos botones de confirmación */}
      {modalProcesar.visible && (
        <div style={{
          position: 'fixed', inset: 0,
          background: 'rgba(0,0,0,0.7)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: '#2d2d2d',
            border: '1px solid #555',
            borderRadius: '10px',
            padding: '30px',
            width: '380px',
            color: '#fdfdfd'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '8px' }}>Confirmar Pedido</h3>
            <p style={{ color: '#aaa', fontSize: '0.9em', marginBottom: '20px' }}>
              Ingresa el tiempo de espera estimado para notificar al cliente por WhatsApp,
              o confirma directamente sin enviar notificación.
            </p>

            <label style={{ display: 'block', marginBottom: '6px', fontWeight: '600' }}>
              Tiempo de espera (minutos){' '}
              <span style={{ color: '#aaa', fontWeight: '400' }}>— opcional</span>
            </label>
            <input
              type="number"
              min="1"
              max="120"
              placeholder="Ej: 20"
              value={modalProcesar.tiempoEspera}
              onChange={(e) => setModalProcesar(prev => ({
                ...prev,
                tiempoEspera: e.target.value === '' ? '' : parseInt(e.target.value) || ''
              }))}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '5px',
                border: '1px solid #555',
                background: '#1a1a1a',
                color: '#fdfdfd',
                fontSize: '1.1em',
                marginBottom: '24px',
                boxSizing: 'border-box'
              }}
            />

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
              {/* Cancelar — cierra el modal sin hacer nada */}
              <button
                onClick={() => setModalProcesar({ visible: false, idPedido: null, tiempoEspera: '' })}
                style={{
                  padding: '10px 20px',
                  background: '#555',
                  color: '#fdfdfd',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                Cancelar
              </button>

              {/* Confirmar sin WhatsApp — siempre activo */}
              <button
                onClick={() => handleMarcarComoPagado(null)}
                disabled={loading}
                style={{
                  padding: '10px 20px',
                  background: '#444',
                  color: '#fdfdfd',
                  border: '1px solid #777',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                Confirmar sin WhatsApp
              </button>

              {/* Confirmar con WhatsApp — solo activo si hay tiempo ingresado */}
              <button
                onClick={() => handleMarcarComoPagado(modalProcesar.tiempoEspera)}
                disabled={loading || modalProcesar.tiempoEspera === '' || modalProcesar.tiempoEspera < 1}
                style={{
                  padding: '10px 20px',
                  background: modalProcesar.tiempoEspera !== '' && modalProcesar.tiempoEspera >= 1
                    ? '#ffcc00'
                    : '#666',
                  color: modalProcesar.tiempoEspera !== '' && modalProcesar.tiempoEspera >= 1
                    ? '#1a1a1a'
                    : '#999',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: modalProcesar.tiempoEspera !== '' && modalProcesar.tiempoEspera >= 1
                    ? 'pointer'
                    : 'not-allowed',
                  fontWeight: '700'
                }}
              >
                Confirmar y enviar WhatsApp
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default GestionPedidos;
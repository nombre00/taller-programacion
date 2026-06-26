// src/pages/client/CheckoutPag.jsx
// Página de checkout - Crea el pedido completo aquí

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Modal, Button, Form, Spinner, Alert } from 'react-bootstrap';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
import {
  obtenerClientePorUid,
  obtenerDireccionesPorCliente,
  crearDireccion,
  obtenerTodasCiudades
} from '../../services/usuariosService';
import { crearPedido, actualizarPedidoAPagado } from '../../services/pedidosService';
import { crearPreferenciaPago } from '../../services/pagoService';
import '../../styles/checkout.css';

function CheckoutPag() {
  const navigate = useNavigate();

  // Estados
  const [carrito, setCarrito] = useState([]);
  const [tipoPedido, setTipoPedido] = useState('delivery');
  const [metodoPago, setMetodoPago] = useState('efectivo');
  const [aceptaTerminos, setAceptaTerminos] = useState(false);
  const [cliente, setCliente] = useState(null);
  const [direcciones, setDirecciones] = useState([]);
  const [direccionSeleccionada, setDireccionSeleccionada] = useState(null);
  const [ciudades, setCiudades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [procesandoPago, setProcesandoPago] = useState(false);

  const [formData, setFormData] = useState({
    nombre: '',
    telefono: '',
    email: ''
  });

  const [showModalDireccion, setShowModalDireccion] = useState(false);
  const [formDireccion, setFormDireccion] = useState({
    idCiudad: '',
    direccion: '',
    alias: ''
  });

  // Cargar datos al inicio
  useEffect(() => {
    const cargarDatos = async () => {
      // 1. Verificar que haya productos en el carrito
      const carritoGuardado = localStorage.getItem('carrito');
      if (!carritoGuardado) {
        alert('No hay productos en el carrito');
        navigate('/carrito');
        return;
      }

      const carritoParseado = JSON.parse(carritoGuardado);
      if (carritoParseado.length === 0) {
        alert('El carrito está vacío');
        navigate('/catalogo');
        return;
      }

      setCarrito(carritoParseado);
      console.log('📦 Carrito cargado:', carritoParseado.length, 'productos');

      // 2. Cargar datos del cliente
      try {
        const firebaseUid = localStorage.getItem('userId');
        if (!firebaseUid) {
          navigate('/login');
          return;
        }

        const [clienteData, ciudadesData] = await Promise.all([
          obtenerClientePorUid(firebaseUid),
          obtenerTodasCiudades()
        ]);

        setCliente(clienteData);
        setCiudades(ciudadesData);

        setFormData({
          nombre: clienteData.nombreCliente || '',
          telefono: clienteData.telefonoCliente || '',
          email: clienteData.usuario?.email || clienteData.email || ''
        });

        if (clienteData.idCliente) {
          const direccionesData = await obtenerDireccionesPorCliente(clienteData.idCliente);
          setDirecciones(direccionesData);
          if (direccionesData.length > 0) {
            setDireccionSeleccionada(direccionesData[0]);
          }
        }

      } catch (error) {
        console.error('Error cargando datos:', error);
      } finally {
        setLoading(false);
      }
    };

    cargarDatos();
  }, [navigate]);

  // Funciones de cálculo
  function calcularSubtotal() {
    return carrito.reduce((total, item) => total + (item.precio * item.cantidad), 0);
  }

  function calcularDelivery() {
    return tipoPedido === 'delivery' ? 2500 : 0;
  }

  function calcularTotal() {
    return calcularSubtotal() + calcularDelivery();
  }

  // Handlers
  function handleInputChange(e) {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  }

  function handleChangeDireccion(e) {
    const { name, value } = e.target;
    setFormDireccion(prev => ({ ...prev, [name]: value }));
  }

  function handleAbrirModalDireccion() {
    setFormDireccion({ idCiudad: '', direccion: '', alias: '' });
    setShowModalDireccion(true);
  }

  function handleCerrarModalDireccion() {
    setShowModalDireccion(false);
  }

  async function recargarDirecciones() {
    try {
      if (cliente?.idCliente) {
        const direccionesData = await obtenerDireccionesPorCliente(cliente.idCliente);
        setDirecciones(direccionesData);
        if (!direccionSeleccionada && direccionesData.length > 0) {
          setDireccionSeleccionada(direccionesData[0]);
        }
      }
    } catch (error) {
      console.error('Error recargando direcciones:', error);
    }
  }

  async function handleGuardarDireccion(e) {
    e.preventDefault();
    if (!formDireccion.direccion.trim() || !formDireccion.idCiudad) {
      alert('Por favor, completa todos los campos obligatorios');
      return;
    }

    try {
      setGuardando(true);
      await crearDireccion({
        idCliente: cliente.idCliente,
        idCiudad: parseInt(formDireccion.idCiudad),
        direccion: formDireccion.direccion,
        alias: formDireccion.alias
      });
      await recargarDirecciones();
      handleCerrarModalDireccion();
      alert('Dirección agregada exitosamente');
    } catch (error) {
      console.error('Error guardando dirección:', error);
      alert('Error al guardar la dirección');
    } finally {
      setGuardando(false);
    }
  }

  // Validación
  function validarFormulario() {
    if (!formData.nombre.trim()) {
      alert('Por favor ingresa tu nombre');
      return false;
    }
    if (!formData.telefono.trim()) {
      alert('Por favor ingresa tu teléfono');
      return false;
    }
    if (!formData.email.trim()) {
      alert('Por favor ingresa tu email');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      alert('Por favor ingresa un email válido');
      return false;
    }
    if (tipoPedido === 'delivery' && !direccionSeleccionada) {
      alert('Por favor selecciona una dirección de entrega');
      return false;
    }
    if (!aceptaTerminos) {
      alert('Debes aceptar los términos y condiciones');
      return false;
    }
    return true;
  }

  // Procesar pagos
  async function procesarPagoEfectivo(idPedido) {
    try {
      console.log('💵 Procesando pago en efectivo...');
      await actualizarPedidoAPagado(idPedido);
      
      localStorage.removeItem('carrito');
      window.dispatchEvent(new Event('storage'));

      alert(`¡Pedido confirmado!\n\nNúmero: ${idPedido}\nTotal: $${calcularTotal().toLocaleString('es-CL')}\n\nPagarás en efectivo al recibir.`);
      navigate('/inicio');
    } catch (error) {
      console.error('Error:', error);
      alert('Hubo un error al confirmar el pedido.');
    }
  }

  async function procesarPagoMercadoPago(idPedido) {
    try {
      console.log('💳 Procesando Mercado Pago...');
      
      const datosPreferencia = {
        idPedido: idPedido,
        montoPago: calcularTotal(),
        descripcion: `Pedido #${idPedido} - ${carrito.length} producto(s)`,
        emailPagador: formData.email,
        nombrePagador: formData.nombre
      };

      const preferenciaResponse = await crearPreferenciaPago(datosPreferencia);

      localStorage.setItem('pedidoPendientePago', JSON.stringify({
        idPedido: idPedido,
        idPago: preferenciaResponse.idPago,
        timestamp: new Date().toISOString()
      }));

      if (preferenciaResponse.urlPago) {
        window.location.href = preferenciaResponse.urlPago;
      } else {
        throw new Error('No se recibió URL de pago');
      }
    } catch (error) {
      console.error('Error Mercado Pago:', error);
      alert('Error al procesar el pago.');
    }
  }

  // Confirmar pedido
  async function handleConfirmarPedido(e) {
    e.preventDefault();
    if (!validarFormulario()) return;

    setProcesandoPago(true);

    try {
      console.log('📦 Creando pedido completo...');

      const tipoEntregaId = tipoPedido === 'delivery' ? 1 : 2;
      const metodoPagoId = metodoPago === 'efectivo' ? 1 : 2;

      const pedidoCompleto = {
        idCliente: parseInt(cliente.idCliente),
        idEstadoPedido: 1,
        idMetodoPago: metodoPagoId,
        idTipoEntrega: tipoEntregaId,
        montoSubtotal: parseFloat(calcularSubtotal()),
        montoEnvio: parseFloat(calcularDelivery()),
        montoTotal: parseFloat(calcularTotal()),
        fechaHoraPedido: new Date().toISOString(),
        notasCliente: null,
        detalles: carrito.map(item => ({
          idProducto: parseInt(item.id),
          cantidad: parseInt(item.cantidad),
          precioUnitario: parseFloat(item.precio),
          subtotalLinea: parseFloat(item.precio * item.cantidad)
        }))
      };

      if (tipoPedido === 'delivery' && direccionSeleccionada?.idDireccion) {
        pedidoCompleto.idDireccionEntrega = parseInt(direccionSeleccionada.idDireccion);
      }

      console.log('📤 Enviando pedido:', pedidoCompleto);
      
      const pedidoCreado = await crearPedido(pedidoCompleto);
      const idPedido = pedidoCreado.idPedido;

      console.log('✅ Pedido creado:', idPedido);

      if (metodoPago === 'efectivo') {
        await procesarPagoEfectivo(idPedido);
      } else if (metodoPago === 'mercadopago') {
        await procesarPagoMercadoPago(idPedido);
      }

    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al procesar el pedido.');
    } finally {
      setProcesandoPago(false);
    }
  }

  if (loading) {
    return (
      <div className="pagina-completa">
        <HeaderComp />
        <main className="contenido-principal">
          <div className="container py-5 text-center">
            <Spinner animation="border" />
            <p className="mt-3">Cargando...</p>
          </div>
        </main>
        <FooterComp />
      </div>
    );
  }

  return (
    <div className="pagina-completa">
      <HeaderComp />
      <main className="contenido-principal">
        <div className="container py-5">
          <h1 className="checkout-titulo">Finalizar Compra</h1>
          <div className="checkout-grid">
            <div className="checkout-form-section">
              <form onSubmit={handleConfirmarPedido}>
                
                <div className="form-seccion">
                  <h3 className="form-seccion-titulo">Tipo de Pedido</h3>
                  <div className="tipo-pedido-opciones">
                    <label className={`tipo-pedido-opcion ${tipoPedido === 'delivery' ? 'active' : ''}`}>
                      <input type="radio" name="tipoPedido" value="delivery" checked={tipoPedido === 'delivery'} onChange={(e) => setTipoPedido(e.target.value)} />
                      <div className="opcion-contenido">
                        <span className="opcion-titulo">Delivery</span>
                        <span className="opcion-precio">+$2.500</span>
                      </div>
                    </label>
                    <label className={`tipo-pedido-opcion ${tipoPedido === 'retiro' ? 'active' : ''}`}>
                      <input type="radio" name="tipoPedido" value="retiro" checked={tipoPedido === 'retiro'} onChange={(e) => setTipoPedido(e.target.value)} />
                      <div className="opcion-contenido">
                        <span className="opcion-titulo">Retiro en tienda</span>
                        <span className="opcion-precio">Gratis</span>
                      </div>
                    </label>
                  </div>
                </div>

                <div className="form-seccion">
                  <h3 className="form-seccion-titulo">Información Personal</h3>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Nombre *</label>
                      <input type="text" name="nombre" value={formData.nombre} onChange={handleInputChange} required />
                    </div>
                    <div className="form-group">
                      <label>Teléfono *</label>
                      <input type="tel" name="telefono" value={formData.telefono} onChange={handleInputChange} maxLength="9" required />
                    </div>
                  </div>
                  <Alert variant="info" className="mt-3 mb-0">
                    <i className="bi bi-envelope-fill me-2"></i>
                    <strong>Email:</strong> {formData.email}
                  </Alert>
                </div>

                {tipoPedido === 'delivery' && (
                  <div className="form-seccion">
                    <h3 className="form-seccion-titulo">Dirección</h3>
                    {direcciones.length > 0 ? (
                      <>
                        <select className="form-control" value={direccionSeleccionada?.idDireccion || ''} onChange={(e) => setDireccionSeleccionada(direcciones.find(d => d.idDireccion === parseInt(e.target.value)))} required>
                          <option value="">Selecciona</option>
                          {direcciones.map((dir) => (
                            <option key={dir.idDireccion} value={dir.idDireccion}>
                              {dir.alias ? `${dir.alias} - ` : ''}{dir.direccion}, {dir.ciudad?.nombreCiudad}
                            </option>
                          ))}
                        </select>
                        <button type="button" className="btn btn-outline-primary mt-2" onClick={handleAbrirModalDireccion}>
                          + Agregar dirección
                        </button>
                      </>
                    ) : (
                      <button type="button" className="btn btn-primary" onClick={handleAbrirModalDireccion}>
                        + Agregar dirección
                      </button>
                    )}
                  </div>
                )}

                <div className="form-seccion">
                  <h3 className="form-seccion-titulo">Método de Pago</h3>
                  <div className="metodos-pago">
                    <label className={`metodo-pago ${metodoPago === 'efectivo' ? 'active' : ''}`}>
                      <input type="radio" name="metodoPago" value="efectivo" checked={metodoPago === 'efectivo'} onChange={(e) => setMetodoPago(e.target.value)} />
                      <div className="metodo-info">
                        <i className="bi bi-cash"></i>
                        <span>Efectivo</span>
                      </div>
                    </label>
                    <label className={`metodo-pago ${metodoPago === 'mercadopago' ? 'active' : ''}`}>
                      <input type="radio" name="metodoPago" value="mercadopago" checked={metodoPago === 'mercadopago'} onChange={(e) => setMetodoPago(e.target.value)} />
                      <div className="metodo-info">
                        <i className="bi bi-wallet2"></i>
                        <span>Mercado Pago</span>
                      </div>
                    </label>
                  </div>
                </div>

                <div className="form-seccion">
                  <label className="checkbox-container">
                    <input type="checkbox" checked={aceptaTerminos} onChange={(e) => setAceptaTerminos(e.target.checked)} />
                    <span>Acepto los términos</span>
                  </label>
                </div>

                <button type="submit" className="btn-confirmar-pedido" disabled={procesandoPago}>
                  {procesandoPago ? (
                    <>
                      <Spinner as="span" animation="border" size="sm" className="me-2" />
                      Procesando...
                    </>
                  ) : (
                    metodoPago === 'mercadopago' ? 'Ir a Pagar' : 'Confirmar Pedido'
                  )}
                </button>
              </form>
            </div>

            <div className="checkout-resumen">
              <div className="resumen-card">
                <h2 className="resumen-titulo">Resumen</h2>
                <div className="resumen-linea">
                  <span>Subtotal:</span>
                  <span>${calcularSubtotal().toLocaleString('es-CL')}</span>
                </div>
                <div className="resumen-linea">
                  <span>Delivery:</span>
                  <span>{calcularDelivery() === 0 ? 'Gratis' : `$${calcularDelivery().toLocaleString('es-CL')}`}</span>
                </div>
                <hr />
                <div className="resumen-total">
                  <span>Total:</span>
                  <span>${calcularTotal().toLocaleString('es-CL')}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
      <FooterComp />

      <Modal show={showModalDireccion} onHide={handleCerrarModalDireccion}>
        <Modal.Header closeButton>
          <Modal.Title>Agregar Dirección</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleGuardarDireccion}>
            <Form.Group className="mb-3">
              <Form.Label className="text-dark">Ciudad *</Form.Label>
              <Form.Select name="idCiudad" value={formDireccion.idCiudad} onChange={handleChangeDireccion} required>
                <option value="">Selecciona</option>
                {ciudades.map((c) => (
                  <option key={c.idCiudad} value={c.idCiudad}>{c.nombreCiudad}</option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="text-dark" >Dirección *</Form.Label>
              <Form.Control type="text" name="direccion" value={formDireccion.direccion} onChange={handleChangeDireccion} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="text-dark" >Alias</Form.Label>
              <Form.Control type="text" name="alias" value={formDireccion.alias} onChange={handleChangeDireccion} />
            </Form.Group>
            <div className="d-flex gap-2 justify-content-end">
              <Button variant="secondary" onClick={handleCerrarModalDireccion}>Cancelar</Button>
              <Button variant="primary" type="submit" disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar'}
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </div>
  );
}

export default CheckoutPag;
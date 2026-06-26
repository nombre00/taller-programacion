// src/pages/client/PagoExitoPage.jsx

import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Spinner, Alert } from 'react-bootstrap';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
import { actualizarPedidoAPagado } from '../../services/pedidosService'; 
import '../../styles/PagoResultado.css'; 
function PagoExitoPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [procesando, setProcesando] = useState(true);
  const [error, setError] = useState(null);
  const [pedidoInfo, setPedidoInfo] = useState(null);

  useEffect(() => {
    procesarPagoExitoso();
  }, []);

  const procesarPagoExitoso = async () => {
    try {
      // Obtener información del pedido del localStorage
      const pedidoPendienteStr = localStorage.getItem('pedidoPendientePago');
      
      if (!pedidoPendienteStr) {
        setError('No se encontró información del pedido');
        setProcesando(false);
        return;
      }

      const pedidoPendiente = JSON.parse(pedidoPendienteStr);
      
      // Obtener parámetros de Mercado Pago
      const collectionId = searchParams.get('collection_id');
      const collectionStatus = searchParams.get('collection_status');
      const paymentId = searchParams.get('payment_id');
      const status = searchParams.get('status');
      const externalReference = searchParams.get('external_reference');

      console.log(' Pago exitoso - Parámetros recibidos:', {
        collectionId,
        collectionStatus,
        paymentId,
        status,
        externalReference
      });

      // Actualizar pedido a pagado y generar venta
      await actualizarPedidoAPagado(pedidoPendiente.idPedido);
      
      // Limpiar carrito y datos de pedido pendiente
      localStorage.removeItem('carrito');
      localStorage.removeItem('pedidoPendientePago');
      window.dispatchEvent(new Event('storage'));

      setPedidoInfo({
        idPedido: pedidoPendiente.idPedido,
        idPago: paymentId || pedidoPendiente.idPago
      });

      setProcesando(false);

    } catch (error) {
      console.error('Error al procesar pago exitoso:', error);
      setError('Hubo un error al confirmar tu pago. Por favor, contacta a soporte.');
      setProcesando(false);
    }
  };

  const handleVolverInicio = () => {
    navigate('/inicio');
  };

  const handleVerPedidos = () => {
    navigate('/mis-pedidos');
  };

  if (procesando) {
    return (
      <div className="pagina-completa">
        <HeaderComp />
        <main className="contenido-principal">
          <div className="container py-5">
            <div className="pago-resultado-container">
              <Spinner animation="border" variant="primary" />
              <h2 className="mt-4">Procesando tu pago...</h2>
              <p className="text-muted">Por favor espera mientras confirmamos tu pago</p>
            </div>
          </div>
        </main>
        <FooterComp />
      </div>
    );
  }

  if (error) {
    return (
      <div className="pagina-completa">
        <HeaderComp />
        <main className="contenido-principal">
          <div className="container py-5">
            <div className="pago-resultado-container">
              <div className="icono-resultado error">
                <i className="bi bi-x-circle"></i>
              </div>
              <h2>Error al procesar el pago</h2>
              <Alert variant="danger" className="mt-3">
                {error}
              </Alert>
              <button className="btn btn-primary mt-3" onClick={handleVolverInicio}>
                Volver al Inicio
              </button>
            </div>
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
          <div className="pago-resultado-container">
            <div className="icono-resultado exito">
              <i className="bi bi-check-circle"></i>
            </div>
            <h2>¡Pago Exitoso!</h2>
            <p className="mensaje-principal">Tu pedido ha sido confirmado y pagado correctamente</p>
            
            {pedidoInfo && (
              <div className="info-pedido">
                <div className="info-item">
                  <span className="label">Número de Pedido:</span>
                  <span className="value">#{pedidoInfo.idPedido}</span>
                </div>
                <div className="info-item">
                  <span className="label">ID de Pago:</span>
                  <span className="value">{pedidoInfo.idPago}</span>
                </div>
              </div>
            )}

            <Alert variant="success" className="mt-4">
              <i className="bi bi-envelope-check me-2"></i>
              Te hemos enviado un email con los detalles de tu pedido y tu boleta electrónica.
            </Alert>

            <div className="acciones-container">
              <button className="btn btn-primary" onClick={handleVerPedidos}>
                <i className="bi bi-list-ul me-2"></i>
                Ver Mis Pedidos
              </button>
              <button className="btn btn-outline-secondary" onClick={handleVolverInicio}>
                <i className="bi bi-house me-2"></i>
                Volver al Inicio
              </button>
            </div>
          </div>
        </div>
      </main>
      <FooterComp />
    </div>
  );
}

export default PagoExitoPage;
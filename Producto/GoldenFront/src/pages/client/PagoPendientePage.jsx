// src/pages/client/PagoPendientePage.jsx

import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Alert } from 'react-bootstrap';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
import '../../styles/PagoResultado.css'; // ← CORREGIDO: ../../styles/

function PagoPendientePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [pedidoInfo, setPedidoInfo] = useState(null);

  useEffect(() => {
    // Obtener información del pedido del localStorage
    const pedidoPendienteStr = localStorage.getItem('pedidoPendientePago');
    
    if (pedidoPendienteStr) {
      const pedidoPendiente = JSON.parse(pedidoPendienteStr);
      setPedidoInfo(pedidoPendiente);
    }

    // Log de parámetros de Mercado Pago
    const collectionId = searchParams.get('collection_id');
    const collectionStatus = searchParams.get('collection_status');
    const externalReference = searchParams.get('external_reference');

    console.log('⏳ Pago pendiente - Parámetros recibidos:', {
      collectionId,
      collectionStatus,
      externalReference
    });
  }, [searchParams]);

  const handleVerPedidos = () => {
    navigate('/mis-pedidos');
  };

  const handleVolverInicio = () => {
    navigate('/inicio');
  };

  const obtenerMensajePendiente = () => {
    const collectionStatus = searchParams.get('collection_status');
    
    switch (collectionStatus) {
      case 'pending':
        return 'Tu pago está siendo procesado';
      case 'in_process':
        return 'Estamos procesando tu pago';
      case 'in_mediation':
        return 'Tu pago está en mediación';
      default:
        return 'Tu pago está pendiente de confirmación';
    }
  };

  return (
    <div className="pagina-completa">
      <HeaderComp />
      <main className="contenido-principal">
        <div className="container py-5">
          <div className="pago-resultado-container">
            <div className="icono-resultado pendiente">
              <i className="bi bi-clock-history"></i>
            </div>
            <h2>Pago Pendiente</h2>
            <p className="mensaje-principal">{obtenerMensajePendiente()}</p>
            
            {pedidoInfo && (
              <div className="info-pedido">
                <div className="info-item">
                  <span className="label">Número de Pedido:</span>
                  <span className="value">#{pedidoInfo.idPedido}</span>
                </div>
              </div>
            )}

            <Alert variant="info" className="mt-4">
              <i className="bi bi-info-circle me-2"></i>
              Te notificaremos por email cuando tu pago sea confirmado.
            </Alert>

            <div className="info-adicional">
              <h5>¿Qué significa esto?</h5>
              <p>
                Tu pago puede estar pendiente por varios motivos:
              </p>
              <ul className="text-start">
                <li>Pagos en efectivo o transferencia que aún no se han acreditado</li>
                <li>Verificaciones de seguridad adicionales</li>
                <li>Pagos con débito que tardan en procesarse</li>
                <li>Problemas temporales con el medio de pago</li>
              </ul>
              <p className="mt-3">
                <strong>No te preocupes:</strong> Recibirás un email con el estado actualizado de tu pago.
              </p>
            </div>

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

export default PagoPendientePage;
// src/pages/client/PagoFalloPage.jsx

import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Alert } from 'react-bootstrap';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
import '../../styles/PagoResultado.css'; 

function PagoFalloPage() {
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
    const status = searchParams.get('status');
    const statusDetail = searchParams.get('status_detail');
    const externalReference = searchParams.get('external_reference');

    console.log('❌ Pago fallido - Parámetros recibidos:', {
      status,
      statusDetail,
      externalReference
    });
  }, [searchParams]);

  const handleVolverCheckout = () => {
    navigate('/checkout');
  };

  const handleVolverInicio = () => {
    // Limpiar datos del pedido pendiente
    localStorage.removeItem('pedidoPendientePago');
    navigate('/inicio');
  };

  const obtenerMensajeError = () => {
    const statusDetail = searchParams.get('status_detail');
    
    switch (statusDetail) {
      case 'cc_rejected_insufficient_amount':
        return 'Fondos insuficientes en tu tarjeta';
      case 'cc_rejected_bad_filled_security_code':
        return 'Código de seguridad incorrecto';
      case 'cc_rejected_call_for_authorize':
        return 'Debes autorizar el pago con tu banco';
      case 'cc_rejected_card_disabled':
        return 'Tu tarjeta está deshabilitada';
      case 'cc_rejected_duplicated_payment':
        return 'Pago duplicado';
      case 'cc_rejected_invalid_installments':
        return 'Cantidad de cuotas inválida';
      default:
        return 'El pago fue rechazado';
    }
  };

  return (
    <div className="pagina-completa">
      <HeaderComp />
      <main className="contenido-principal">
        <div className="container py-5">
          <div className="pago-resultado-container">
            <div className="icono-resultado error">
              <i className="bi bi-x-circle"></i>
            </div>
            <h2>Pago Rechazado</h2>
            <p className="mensaje-principal">{obtenerMensajeError()}</p>
            
            {pedidoInfo && (
              <div className="info-pedido">
                <div className="info-item">
                  <span className="label">Pedido #:</span>
                  <span className="value">{pedidoInfo.idPedido}</span>
                </div>
              </div>
            )}

            <Alert variant="warning" className="mt-4">
              <i className="bi bi-exclamation-triangle me-2"></i>
              Tu pedido no ha sido procesado. Por favor, verifica los datos de tu tarjeta e intenta nuevamente.
            </Alert>

            <div className="consejos-container">
              <h5>Recomendaciones:</h5>
              <ul className="text-start">
                <li>Verifica que los datos de tu tarjeta sean correctos</li>
                <li>Asegúrate de tener fondos suficientes</li>
                <li>Intenta con otro medio de pago</li>
                <li>Contacta a tu banco si el problema persiste</li>
              </ul>
            </div>

            <div className="acciones-container">
              <button className="btn btn-primary" onClick={handleVolverCheckout}>
                <i className="bi bi-arrow-clockwise me-2"></i>
                Intentar Nuevamente
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

export default PagoFalloPage;
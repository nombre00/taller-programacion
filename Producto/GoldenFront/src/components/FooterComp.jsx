// src/components/FooterComp.jsx

import React from 'react';
import { Link } from 'react-router-dom'; // Para la navegación interna

export default function FooterComp() {

  return (
    <footer className="site-footer pt-5">
      <div className="container">
        <div className="row g-4">
          <div className="col-12 col-md-3">
            <h5 className="text-warning mb-3">Golden Burger</h5>
            <ul className="list-unstyled">
              <li className="mb-2"><i className="bi bi-geo-alt-fill me-2"></i>Etchevers 210, Viña del Mar</li>
              <li className="mb-2"><i className="bi bi-telephone-fill me-2"></i>+569 71334173</li>
              <li className="mb-2"><i className="bi bi-envelope-fill me-2"></i>Goldenpagos2@gmail.com</li>
              <li className="mt-3 d-flex gap-3">
                
                {/* Para enlaces externos, seguimos usando <a> pero con seguridad adicional */}
                <a href="https://www.instagram.com/goldenburger.cl?igsh=MWt4bjUxbTY4N3lp" className="fs-4 text-light" target="_blank" rel="noopener noreferrer">
                  <i className="bi bi-instagram"></i>
                </a>
                <a href="https://www.facebook.com/?locale=es_LA" className="fs-4 text-light" target="_blank" rel="noopener noreferrer">
                  <i className="bi bi-facebook"></i>
                </a>
              </li>
            </ul>
          </div>

          <div className="col-12 col-md-3">
            <h5 className="text-warning mb-3">Ubicación</h5>
            <div className="ratio ratio-4x3 rounded overflow-hidden">
              <iframe 
                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3344.471676839659!2d-71.55422088481105!3d-33.04409398089209!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x9689de0e5153a5cd%3A0x6b8f3b0e3b0e3b0e!2sEtchevers%20210%2C%20Vi%C3%B1a%20del%20Mar%2C%20Valpara%C3%ADso!5e0!3m2!1ses!2scl!4v1668520000000"
                width="600" 
                height="450" 
                style={{ border: 0 }} 
                allowFullScreen="" 
                loading="lazy" 
                referrerPolicy="no-referrer-when-downgrade">
              </iframe>
            </div>
          </div>

          <div className="col-12 col-md-3">
            {/* Espacio vacío como en tu HTML original */}
          </div>

          <div className="col-12 col-md-3 text-center">
            <h5 className="text-warning mb-3">¿Hambre?</h5>
            <p className="small mb-3">No esperes más y revisa nuestras increíbles opciones.</p>
            
            <Link to="/catalogo" className="btn btn-warning text-dark fw-semibold">
              Comprar Ahora
            </Link>
          </div>
        </div>
      </div>
      <div className="bg-black text-center text-secondary small py-3 mt-5">
        © {new Date().getFullYear()} Golden Burger — Todos los derechos reservados
      </div>
    </footer>
  );
}
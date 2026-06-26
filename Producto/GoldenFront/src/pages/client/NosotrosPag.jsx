import React from 'react';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
// 1. Eliminamos la importación de 'Card'

function NosotrosPag() {
  return (
    <div className="pagina-completa">
      <HeaderComp />

      <main>
        <div className="container py-5">
          {/* 2. Reemplazamos <Card> por <div> */}
          {/* Añadimos 'text-light' y 'rounded' para mantener el estilo */}
          <div 
            className="shadow-lg p-4 mx-auto text-light rounded" 
            style={{ maxWidth: '900px', backgroundColor: '#3a3c43' }}
          >
            {/* 3. Reemplazamos <Card.Body> por <div> */}
            <div>
              <h2 className="text-center mb-4" style={{ color: '#ffc107' }}>Sobre Nosotros</h2>
              <p className="text-center">
                Bienvenidos a <strong>Golden Burger</strong>, tu lugar favorito para disfrutar de hamburguesas únicas...
              </p>
              <div className="row text-center mt-5">
                {/* ... El contenido de Misión, Visión, Valores va aquí sin cambios ... */}
                <div className="col-md-4 mb-4">
                  <i className="bi bi-bullseye display-4 mb-2" style={{ color: '#ffc107' }}></i>
                  <h5 className="mt-2">Misión</h5>
                  <p>Brindar hamburguesas deliciosas y un servicio cálido...</p>
                </div>
                <div className="col-md-4 mb-4">
                  <i className="bi bi-eye display-4 mb-2" style={{ color: '#ffc107' }}></i>
                  <h5 className="mt-2">Visión</h5>
                  <p>Ser la cadena de hamburguesas preferida en nuestra ciudad...</p>
                </div>
                <div className="col-md-4 mb-4">
                  <i className="bi bi-heart-fill display-4 mb-2" style={{ color: '#ffc107' }}></i>
                  <h5 className="mt-2">Valores</h5>
                  <p>Calidad, compromiso, creatividad y pasión...</p>
                </div>
              </div>
            </div>
          </div>

          <br /><br /><br />

          {/* 2. Hacemos lo mismo para la segunda tarjeta */}
          <div 
            className="shadow-lg p-4 mx-auto text-light rounded" 
            style={{ maxWidth: '900px', backgroundColor: '#3a3c43' }}
          >
            {/* 3. Reemplazamos <Card.Body> por <div> */}
            <div>
              <p className="text-center">
                <strong>Dirección: </strong> Etchevers 210, Viña del Mar<br />
                <strong>Teléfono: </strong> +569 71334173<br />
                <strong>Email: </strong> Goldenpagos2@gmail.com<br />
                <a href="https://www.instagram.com/goldenburger.cl?igsh=MWt4bjUxbTY4N3lp" className="fs-4 text-light" target="_blank" rel="noopener noreferrer"><i className="bi bi-instagram"></i></a>
                <a href="https://www.facebook.com/?locale=es_LA" className="fs-4 text-light" target="_blank" rel="noopener noreferrer"><i className="bi bi-facebook"></i></a>
              </p>
              <div className="text-center">
                <iframe 
                  src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3344.471676839659!2d-71.55422088481105!3d-33.04409398089209!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x9689de0e5153a5cd%3A0x6b8f3b0e3b0e3b0e!2sEtchevers%20210%2C%20Vi%C3%B1a%20del%20Mar%2C%20Valpara%C3%ADso!5e0!3m2!1ses!2scl!4v1668520000000"
                  width="600" 
                  height="450" 
                  style={{ border: 0, maxWidth: '100%' }} 
                  allowFullScreen="" 
                  loading="lazy" 
                  referrerPolicy="no-referrer-when-downgrade">
                </iframe>
              </div>
            </div>
          </div>
        </div>
      </main>

      <FooterComp />
    </div>
  );
}

export default NosotrosPag;
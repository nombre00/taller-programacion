import React from 'react';
import { Link } from 'react-router-dom';

// --- COMPONENTES ---
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';
import Carousel from 'react-bootstrap/Carousel';
import Button from 'react-bootstrap/Button';
import Card from 'react-bootstrap/Card';
import '../../styles/Inicio.css';

// --- DATOS Y IMÁGENES ---
import { productosDB } from '../../data/dataBase.js';

// Imágenes para el carrusel
import carrusel1 from '/src/assets/img/Carrusel1.png';
import carrusel2 from '/src/assets/img/Carrusel2.png';
import carrusel3 from '/src/assets/img/Carrusel3.png';

// Imágenes para las tarjetas de productos
import goldenImg from '/src/assets/img/Golden.PNG';
import baconBbqImg from '/src/assets/img/BaconBBQ.PNG';
import ClasicaImg from '/src/assets/img/Clasica.PNG';

// Mapeo para conectar las rutas de texto de la DB con las imágenes importadas
const imageMap = {
  '/src/assets/img/Golden.PNG': goldenImg,
  '/src/assets/img/BaconBBQ.PNG': baconBbqImg,
  '/src/assets/img/Clasica.PNG': ClasicaImg
};

// --- COMPONENTE TARJETA DE PRODUCTO ---
function ProductoCard({ producto }) {
  const formatPrice = (price) =>
    new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(price);

  const handleAddToCart = () => {
    const nuevoItem = {
      id: producto.id,
      nombre: producto.nombre,
      precio: producto.precio,
      cantidad: 1
    };
    console.log('Añadiendo al carrito:', nuevoItem); 
  };
  

  return (
    <Card>
      <div className="card-img-container">
        <Card.Img src={producto.imagen} />
      </div>
      <Card.Body>
        <div>
          <Card.Title>{producto.nombre}</Card.Title>
          <Card.Text>{producto.descripcion}</Card.Text>
        </div>
        <div className="card-footer">
          <span className="price">{formatPrice(producto.precio)}</span>
          <button className="btn-add-cart" onClick={handleAddToCart}>
            +
          </button>
        </div>
      </Card.Body>
    </Card>
  );
}

// --- COMPONENTE PRINCIPAL ---
function InicioPag() {
  const masVendidos = productosDB.filter(
    (producto) =>
      producto.nombre === 'Golden' ||
      producto.nombre === 'Bacon BBQ' ||
      producto.nombre === 'Clásica'
  );

  return (
    <div className="pagina-completa">
      <HeaderComp />
      <main className="contenido-principal">
       
        <Carousel>
          <Carousel.Item interval={2000}>
            <img className="d-block w-100" src={carrusel1} alt="Promoción de hamburguesas" />
            <Carousel.Caption>
              <h3>Las Mejores Hamburguesas</h3>
              <p>Hechas con ingredientes frescos y de la mejor calidad.</p>
            </Carousel.Caption>
          </Carousel.Item>
          <Carousel.Item interval={2000}>
            <img className="d-block w-100" src={carrusel2} alt="Promoción de combos" />
            <Carousel.Caption>
              <h3>Combos Imperdibles</h3>
              <p>Acompaña tu burger favorita con nuestras papas y refrescos.</p>
            </Carousel.Caption>
          </Carousel.Item>
          <Carousel.Item interval={2000}>
            <img className="d-block w-100" src={carrusel3} alt="Oferta especial" />
            <Carousel.Caption>
              <h3>Pide Ahora</h3>
              <p>Disfruta del auténtico sabor de Golden Burger en tu casa.</p>
            </Carousel.Caption>
          </Carousel.Item>
        </Carousel>

        {/* --- SECCIÓN "MÁS VENDIDOS" --- */}
        <section className="container my-5 py-5">
          <h2 className="text-center section-title mb-4">LOS MÁS VENDIDOS</h2>
          <div className="row g-4">
            {masVendidos.map((producto) => (
              <div key={producto.id} className="col-md-4">
                <ProductoCard producto={producto} />
              </div>
            ))}
          </div>
        </section>

        {/* --- SECCIÓN "PRÓXIMAMENTE" --- */}
        <section className="container my-5 py-5">
          <div className="row">
            <div className="col text-center">
              <h2 className="section-title">PRÓXIMAMENTE</h2>
            </div>
          </div>
        </section>
      </main>
      <FooterComp />
    </div>
  );
}

export default InicioPag;

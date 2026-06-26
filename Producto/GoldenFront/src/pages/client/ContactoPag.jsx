// --- COMPONENTES ---
import React, { useState } from 'react';
import HeaderComp from '../../components/HeaderComp';
import FooterComp from '../../components/FooterComp';

// Importaciones de Bootstrap
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert';
import Spinner from 'react-bootstrap/Spinner';

// IMPORTAR EL SERVICIO
import { enviarMensajeContacto } from '../../services/contactoService';

function ContactoPag() {
  const [validated, setValidated] = useState(false);
  const [formData, setFormData] = useState({
    nombreRemitente: '',
    emailRemitente: '',
    asunto: 'Consulta general', // Asunto por defecto
    mensaje: ''
  });

  // Estados para manejar el envío
  const [enviando, setEnviando] = useState(false);
  const [mensajeEnviado, setMensajeEnviado] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (event) => {
    const { id, value } = event.target;
    setFormData({
      ...formData,
      [id]: value
    });
  };

  const handleSubmit = async (event) => {
    const form = event.currentTarget;
    event.preventDefault();
    event.stopPropagation();

    if (form.checkValidity() === false) {
      setValidated(true);
      return;
    }

    // Enviar mensaje y mostrar confirmación
    setEnviando(true);
    setError(null);

    try {
      await enviarMensajeContacto(formData);
      
      // Mensaje enviado con éxito
      setMensajeEnviado(true);
      setValidated(false);
      
      // Limpiar el formulario
      setFormData({
        nombreRemitente: '',
        emailRemitente: '',
        asunto: 'Consulta general',
        mensaje: ''
      });

    } catch (err) {
      console.error("Error al enviar mensaje:", err);
      setError("Hubo un problema al enviar tu mensaje. Por favor, intenta nuevamente.");
    } finally {
      setEnviando(false);
    }
  };

  // Función para volver a mostrar el formulario
  const volverAlFormulario = () => {
    setMensajeEnviado(false);
    setError(null);
  };

  return (
    <div className="pagina-completa">
      <HeaderComp />

      <main>
        <div className="container my-5 py-5">
          <Row className="g-4">

            {/* --- COLUMNA IZQUIERDA (Información de Contacto) --- */}
            <Col md={5}>
              <div 
                className="h-100 shadow-lg p-4 text-light rounded" 
                style={{ backgroundColor: '#3a3c43' }}
              >
                <div className="text-center d-flex flex-column justify-content-center">
                  <h2 className="text-center mb-4" style={{ color: '#ffc107' }}>Información de Contacto</h2>
                  <ul className="list-unstyled">
                    <li className="mb-4 d-flex flex-column align-items-center">
                      <i className="bi bi-geo-alt-fill mb-2 fs-2"></i>
                      <span className="fs-5">Etchevers 210, Viña del Mar</span>
                    </li>
                    <li className="mb-4 d-flex flex-column align-items-center">
                      <i className="bi bi-telephone-fill mb-2 fs-2"></i>
                      <span className="fs-5">+569 71334173</span>
                    </li>
                    <li className="mb-4 d-flex flex-column align-items-center">
                      <i className="bi bi-envelope-fill mb-2 fs-2"></i>
                      <span className="fs-5">Goldenpagos2@gmail.com</span>
                    </li>
                    <li className="mt-4 d-flex gap-4 justify-content-center">
                      <a href="https://www.instagram.com/goldenburger.cl?igsh=MWt4bjUxbTY4N3lp" className="fs-3 text-light" target="_blank" rel="noopener noreferrer" data-testid="instagram-link">
                        <i className="bi bi-instagram"></i>
                      </a>
                      <a href="https://www.facebook.com/?locale=es_LA" className="fs-3 text-light" target="_blank" rel="noopener noreferrer" data-testid="facebook-link">
                        <i className="bi bi-facebook"></i>
                      </a>
                    </li>
                  </ul>
                </div>
              </div>
            </Col>

            {/* --- COLUMNA DERECHA (Formulario o Confirmación) --- */}
            <Col md={7}>
              <div 
                className="shadow-lg p-4 text-light rounded" 
                style={{ backgroundColor: '#3a3c43' }}
              >
                <div>
                  <h2 className="text-center mb-4" style={{ color: '#ffc107' }}>Formulario de Contacto</h2>

                  {/* Mensaje de confirmación después de enviar */}
                  {mensajeEnviado ? (
                    <div className="text-center py-5">
                      <div className="mb-4">
                        <i className="bi bi-check-circle-fill text-success" style={{ fontSize: '5rem' }}></i>
                      </div>
                      <Alert variant="success" className="mb-4">
                        <Alert.Heading>¡Mensaje enviado con éxito!</Alert.Heading>
                        <p className="mb-0">
                          Gracias por contactarnos. Hemos recibido tu mensaje y te responderemos a la brevedad.
                        </p>
                      </Alert>
                      <Button variant="warning" onClick={volverAlFormulario}>
                        Enviar otro mensaje
                      </Button>
                    </div>
                  ) : (
                    <>
                      {/* Mostrar error si existe */}
                      {error && (
                        <Alert variant="danger" dismissible onClose={() => setError(null)}>
                          {error}
                        </Alert>
                      )}

                      {/* Formulario */}
                      <Form noValidate validated={validated} onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="nombreRemitente">
                          <Form.Label>Nombre</Form.Label>
                          <Form.Control
                            type="text"
                            required
                            maxLength="100"
                            value={formData.nombreRemitente}
                            onChange={handleChange}
                            disabled={enviando}
                          />
                          <Form.Text className="text-muted">
                            {formData.nombreRemitente.length} / 100 caracteres
                          </Form.Text>
                          <Form.Control.Feedback type="invalid">
                            Debe ingresar un nombre (máx. 100 caracteres).
                          </Form.Control.Feedback>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="emailRemitente">
                          <Form.Label>Correo</Form.Label>
                          <Form.Control
                            type="email"
                            required
                            value={formData.emailRemitente}
                            onChange={handleChange}
                            disabled={enviando}
                          />
                          <Form.Control.Feedback type="invalid">
                            Ingrese un correo válido.
                          </Form.Control.Feedback>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="asunto">
                          <Form.Label>Asunto</Form.Label>
                          <Form.Select
                            value={formData.asunto}
                            onChange={handleChange}
                            disabled={enviando}
                          >
                            <option value="Consulta general">Consulta general</option>
                            <option value="Pedido especial">Pedido especial</option>
                            <option value="Sugerencia">Sugerencia</option>
                            <option value="Reclamo">Reclamo</option>
                            <option value="Otro">Otro</option>
                          </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="mensaje">
                          <Form.Label>Mensaje</Form.Label>
                          <Form.Control
                            as="textarea"
                            rows={4}
                            required
                            maxLength="500"
                            value={formData.mensaje}
                            onChange={handleChange}
                            disabled={enviando}
                          />
                          <Form.Text className="text-muted">
                            {formData.mensaje.length} / 500 caracteres
                          </Form.Text>
                          <Form.Control.Feedback type="invalid">
                            Debe ingresar un mensaje (máx. 500 caracteres).
                          </Form.Control.Feedback>
                        </Form.Group>

                        <div className="text-center mt-4">
                          <Button 
                            type="submit" 
                            variant="warning" 
                            className="px-5"
                            disabled={enviando}
                          >
                            {enviando ? (
                              <>
                                <Spinner
                                  as="span"
                                  animation="border"
                                  size="sm"
                                  role="status"
                                  aria-hidden="true"
                                  className="me-2"
                                />
                                Enviando...
                              </>
                            ) : (
                              'Enviar'
                            )}
                          </Button>
                        </div>
                      </Form>
                    </>
                  )}
                </div>
              </div>
            </Col>

          </Row>
        </div>
      </main>

      <FooterComp />
    </div>
  );
}

export default ContactoPag;

// src/pages/admin/GestionContacto.jsx

import React, { useEffect, useState } from "react";
import Sidebar from "../../components/Sidebar";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import Badge from "react-bootstrap/Badge";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import ButtonGroup from "react-bootstrap/ButtonGroup";

import {
  listarMensajesContacto,
  eliminarMensajeContacto,
  actualizarEstadoMensaje,
} from "../../services/contactoService";

import "../../styles/estilosAdmin.css";

const GestionContacto = () => {
  const [mensajes, setMensajes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalMsg, setModalMsg] = useState(null);

  // Filtro de estado (archivados eliminado)
  const [filtroEstado, setFiltroEstado] = useState("todos");

  const loadMensajes = async () => {
    try {
      const data = await listarMensajesContacto();
      setMensajes(data);
    } catch (err) {
      console.error("Error al cargar mensajes:", err);
      alert("No se pudieron cargar los mensajes.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMensajes();
  }, []);

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Está seguro de que desea eliminar este mensaje?")) return;

    try {
      await eliminarMensajeContacto(id);
      setMensajes((prev) => prev.filter((m) => m.idMensaje !== id));
    } catch (err) {
      console.error(err);
      alert("Error al eliminar el mensaje.");
    }
  };

  const handleCambiarEstado = async (id, nuevoEstado) => {
    try {
      await actualizarEstadoMensaje(id, nuevoEstado);

      setMensajes((prev) =>
        prev.map((m) =>
          m.idMensaje === id ? { ...m, leido: nuevoEstado } : m
        )
      );
    } catch (err) {
      console.error("Error al actualizar estado:", err);
      alert("No se pudo actualizar el estado.");
    }
  };

  // Badge SOLO para 0 y 1
  const estadoBadge = (estado) => {
    return estado === 0
      ? <Badge bg="warning" text="dark">No leído</Badge>
      : <Badge bg="success">Leído</Badge>;
  };

  // Filtrar por estado (archivados removidos)
  const mensajesFiltrados = mensajes.filter((m) => {
    if (filtroEstado === "no-leidos") return m.leido === 0;
    if (filtroEstado === "leidos") return m.leido === 1;
    return true;
  });

  // Ordenar fecha
  const mensajesOrdenados = [...mensajesFiltrados].sort(
    (a, b) => new Date(b.fechaRecibido) - new Date(a.fechaRecibido)
  );

  const formatearFecha = (fechaStr) => {
    if (!fechaStr) return "";
    const fecha = new Date(fechaStr);
    return fecha.toLocaleString("es-CL", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleVerMensaje = async (msg) => {
    setModalMsg(msg);

    if (msg.leido === 0) {
      await handleCambiarEstado(msg.idMensaje, 1);
    }
  };

  return (
    <div className="dashboard-layout d-flex">
      <Sidebar />

      <div className="dashboard-content p-4 w-100">
        <h1 className="text-light mb-4">
          <i className="bi bi-envelope-fill me-2"></i>
          Gestión de Mensajes de Contacto
        </h1>

        {/* FILTROS - archivados removidos */}
        <div className="mb-4 p-3 rounded filtro-container">
          <Row className="align-items-center">
            <Col md={7}>
              <div className="d-flex align-items-center gap-3">
                <span className="text-light fw-bold">Filtrar por estado:</span>
                <ButtonGroup>
                  <Button
                    variant={filtroEstado === "todos" ? "warning" : "outline-warning"}
                    size="sm"
                    onClick={() => setFiltroEstado("todos")}
                  >
                    Todos ({mensajes.length})
                  </Button>

                  <Button
                    variant={filtroEstado === "no-leidos" ? "warning" : "outline-warning"}
                    size="sm"
                    onClick={() => setFiltroEstado("no-leidos")}
                  >
                    No leídos ({mensajes.filter(m => m.leido === 0).length})
                  </Button>

                  <Button
                    variant={filtroEstado === "leidos" ? "warning" : "outline-warning"}
                    size="sm"
                    onClick={() => setFiltroEstado("leidos")}
                  >
                    Leídos ({mensajes.filter(m => m.leido === 1).length})
                  </Button>
                </ButtonGroup>
              </div>
            </Col>

            <Col md={5} className="text-end text-muted">
              Mostrando {mensajesOrdenados.length} mensaje(s)
            </Col>
          </Row>
        </div>

        {/* TABLA */}
        <div
          className="table-responsive shadow-lg rounded"
          style={{ maxHeight: "70vh", overflowY: "auto", border: "1px solid rgba(255,255,255,0.1)" }}
        >
          <Table striped bordered hover variant="dark" className="mb-0">
            <thead style={{ position: "sticky", top: 0, background: "#1a1c20" }}>
              <tr>
                <th style={{ width: "160px" }}>Fecha</th>
                <th>Remitente</th>
                <th>Correo</th>
                <th>Asunto</th>
                <th>Extracto</th>
                <th style={{ width: "110px" }}>Estado</th>
                <th style={{ width: "180px" }}>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {mensajesOrdenados.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center text-light py-5">
                    <i className="bi bi-inbox fs-1 d-block mb-3 text-muted"></i>
                    No hay mensajes
                  </td>
                </tr>
              ) : (
                mensajesOrdenados.map((msg) => (
                  <tr key={msg.idMensaje} className={msg.leido === 0 ? "fila-no-leido" : ""}>
                    <td>{formatearFecha(msg.fechaRecibido)}</td>
                    <td>{msg.nombreRemitente}</td>
                    <td className="text-break">{msg.emailRemitente}</td>
                    <td>{msg.asunto}</td>
                    <td>
                      {msg.mensaje.length > 50 ? msg.mensaje.substring(0, 50) + "..." : msg.mensaje}
                    </td>

                    <td>{estadoBadge(msg.leido)}</td>

                    {/* ACCIONES — archivado eliminado */}
                    <td>
                      <div className="d-flex flex-wrap gap-1">
                        {/* Ver */}
                        <Button variant="info" size="sm" onClick={() => handleVerMensaje(msg)}>
                          <i className="bi bi-eye-fill"></i>
                        </Button>

                        {/* Marcar como leído */}
                        {msg.leido !== 1 && (
                          <Button variant="success" size="sm" onClick={() => handleCambiarEstado(msg.idMensaje, 1)}>
                            <i className="bi bi-check-circle"></i>
                          </Button>
                        )}

                        {/* Marcar como no leído */}
                        {msg.leido !== 0 && (
                          <Button variant="warning" size="sm" onClick={() => handleCambiarEstado(msg.idMensaje, 0)}>
                            <i className="bi bi-envelope"></i>
                          </Button>
                        )}

                        {/* ELIMINAR */}
                        <Button variant="danger" size="sm" onClick={() => handleEliminar(msg.idMensaje)}>
                          <i className="bi bi-trash-fill"></i>
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </div>
      </div>

      {/* MODAL — fondo claro */}
      <Modal show={!!modalMsg} onHide={() => setModalMsg(null)} centered size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Mensaje de {modalMsg?.nombreRemitente}</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <p><strong>Correo:</strong> {modalMsg?.emailRemitente}</p>
          <p><strong>Asunto:</strong> {modalMsg?.asunto}</p>
          <p><strong>Fecha:</strong> {formatearFecha(modalMsg?.fechaRecibido)}</p>

          {/* CAMBIO DE FONDO AQUÍ */}
          <div className="p-3 mt-2 rounded" style={{ background: "#f8f9fa", color: "#000" }}>
            {modalMsg?.mensaje}
          </div>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setModalMsg(null)}>Cerrar</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default GestionContacto;

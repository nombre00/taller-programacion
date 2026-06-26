// src/pages/admin/turnos/configuracion/PlantillasTurno.jsx
import React, { useEffect, useState } from "react";
import {
  getPlantillas,
  createPlantilla,
  updatePlantilla,
  deletePlantilla,
  createSlot,
  updateSlot,
} from "../../../../services/plantillasService.mock";
import { getPosiciones } from "../../../../services/posicionesService.mock";

const FORM_VACIO = { nombre: "", horaInicio: "", horaTermino: "", descripcion: "", idPosicion: "" };

function PlantillasTurno() {
  const [plantillas, setPlantillas]           = useState([]);
  const [posiciones, setPosiciones]           = useState([]);
  const [plantillaActiva, setPlantillaActiva] = useState(null);
  const [form, setForm]                       = useState(FORM_VACIO);
  const [editandoId, setEditandoId]           = useState(null);
  const [error, setError]                     = useState("");
  const [cargando, setCargando]               = useState(false);

  useEffect(() => { cargar(); }, []);

  const cargar = async () => {
    setCargando(true);
    try {
      const [pData, posData] = await Promise.all([getPlantillas(), getPosiciones()]);
      setPlantillas(pData);
      setPosiciones(posData);
    } catch {
      setError("Error al cargar plantillas.");
    } finally {
      setCargando(false);
    }
  };

  const handleChange = (e) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async () => {
    if (!form.nombre || !form.horaInicio || !form.horaTermino || !form.idPosicion) {
      setError("Nombre, horario y cargo son obligatorios.");
      return;
    }
    setError("");
    try {
      if (editandoId) {
        // Actualizar plantilla
        await updatePlantilla(editandoId, {
          nombre:      form.nombre,
          horaInicio:  form.horaInicio,
          horaTermino: form.horaTermino,
          descripcion: form.descripcion,
        });
        // Actualizar slot si cambió la posición
        const plantillaActual = plantillas.find(p => p.idPlantilla === editandoId);
        const slotActual = plantillaActual?.slots?.[0];
        if (slotActual && String(slotActual.idPosicion) !== String(form.idPosicion)) {
          await updateSlot(slotActual.idSlot, {
            idPosicion: form.idPosicion,
            nombre:     slotActual.nombre,
            cantidad:   1,
          });
        }
      } else {
        // Crear plantilla y luego su slot automáticamente
        const nueva = await createPlantilla({
          nombre:      form.nombre,
          horaInicio:  form.horaInicio,
          horaTermino: form.horaTermino,
          descripcion: form.descripcion,
        });
        await createSlot({
          idPlantilla: nueva.idPlantilla,
          idPosicion:  form.idPosicion,
          cantidad:    1,
        });
      }
      setForm(FORM_VACIO);
      setEditandoId(null);
      await cargar();
    } catch {
      setError("Error al guardar la plantilla.");
    }
  };

  const handleEditar = (p) => {
    setEditandoId(p.idPlantilla);
    setForm({
      nombre:      p.nombre,
      horaInicio:  p.horaInicio,
      horaTermino: p.horaTermino,
      descripcion: p.descripcion || "",
      idPosicion:  p.slots?.[0]?.idPosicion || "",
    });
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Eliminar esta plantilla?")) return;
    try {
      await deletePlantilla(id);
      if (plantillaActiva === id) setPlantillaActiva(null);
      await cargar();
    } catch {
      setError("No se puede eliminar — puede tener asignaciones activas.");
    }
  };

  const handleCancelar = () => {
    setForm(FORM_VACIO);
    setEditandoId(null);
    setError("");
  };

  const togglePlantilla = (id) =>
    setPlantillaActiva(prev => prev === id ? null : id);

  const getPosicionColor = (idPosicion) =>
    posiciones.find(p => p.idPosicion === Number(idPosicion))?.color || "#888";

  const getPosicionNombre = (idPosicion) =>
    posiciones.find(p => p.idPosicion === Number(idPosicion))?.nombre || "—";

  if (cargando) return <p style={{ color: "#aaa", padding: "20px" }}>Cargando plantillas...</p>;

  return (
    <div>
      {error && <p style={{ color: "#ff6b6b", marginBottom: "16px" }}>{error}</p>}

      {/* Formulario plantilla */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>
          {editandoId ? "✏️ Editar Plantilla" : "➕ Nueva Plantilla"}
        </h2>

        <div style={estiloGrid}>
          <div style={estiloCampo}>
            <label style={estiloLabel}>Nombre *</label>
            <input
              name="nombre"
              value={form.nombre}
              onChange={handleChange}
              placeholder="Ej: Turno Cocinero Tarde"
              style={estiloInput}
            />
          </div>

          <div style={estiloCampo}>
            <label style={estiloLabel}>Hora inicio *</label>
            <input
              name="horaInicio"
              type="time"
              value={form.horaInicio}
              onChange={handleChange}
              style={estiloInput}
            />
          </div>

          <div style={estiloCampo}>
            <label style={estiloLabel}>Hora término *</label>
            <input
              name="horaTermino"
              type="time"
              value={form.horaTermino}
              onChange={handleChange}
              style={estiloInput}
            />
          </div>

          <div style={estiloCampo}>
            <label style={estiloLabel}>Cargo *</label>
            <select
              name="idPosicion"
              value={form.idPosicion}
              onChange={handleChange}
              style={estiloInput}
            >
              <option value="">Seleccionar cargo...</option>
              {posiciones.map(pos => (
                <option key={pos.idPosicion} value={pos.idPosicion}>
                  {pos.nombre}
                </option>
              ))}
            </select>
          </div>

          <div style={{ ...estiloCampo, gridColumn: "2 / -1" }}>
            <label style={estiloLabel}>Descripción</label>
            <input
              name="descripcion"
              value={form.descripcion}
              onChange={handleChange}
              placeholder="Opcional"
              style={estiloInput}
            />
          </div>
        </div>

        <div style={{ display: "flex", gap: "10px", marginTop: "16px" }}>
          <button onClick={handleSubmit} style={estiloBotonPrimario}>
            {editandoId ? "Actualizar" : "Crear"}
          </button>
          {editandoId && (
            <button onClick={handleCancelar} style={estiloBotonSecundario}>
              Cancelar
            </button>
          )}
        </div>
      </div>

      {/* Lista de plantillas */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>📋 Plantillas registradas</h2>
        {plantillas.length === 0 ? (
          <p style={{ color: "#888" }}>No hay plantillas registradas.</p>
        ) : (
          plantillas.map(p => {
            const slot = p.slots?.[0];
            return (
              <div key={p.idPlantilla} style={{ marginBottom: "8px" }}>

                {/* Fila plantilla */}
                <div
                  onClick={() => togglePlantilla(p.idPlantilla)}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "12px 16px",
                    backgroundColor: plantillaActiva === p.idPlantilla ? "#3a3a3a" : "#242424",
                    borderRadius: plantillaActiva === p.idPlantilla ? "8px 8px 0 0" : "8px",
                    cursor: "pointer",
                    borderLeft: `3px solid ${slot ? slot.colorPosicion : "#444"}`,
                    transition: "all 0.2s",
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
                    <span style={{ color: "#ffcc00", fontSize: "1em" }}>
                      {plantillaActiva === p.idPlantilla ? "▼" : "▶"}
                    </span>
                    <span style={{ color: "#fff", fontWeight: "bold" }}>{p.nombre}</span>
                    <span style={{ color: "#aaa", fontSize: "0.85em" }}>
                      {p.horaInicio} – {p.horaTermino}
                    </span>
                    {slot && (
                      <span style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                        backgroundColor: "#1a1a1a",
                        padding: "2px 10px",
                        borderRadius: "12px",
                        border: "1px solid #444",
                        fontSize: "0.8em",
                        color: "#ddd",
                      }}>
                        <span style={{
                          width: "8px", height: "8px", borderRadius: "50%",
                          backgroundColor: slot.colorPosicion,
                          display: "inline-block",
                        }} />
                        {slot.nombrePosicion}
                      </span>
                    )}
                  </div>

                  <div
                    style={{ display: "flex", gap: "8px" }}
                    onClick={e => e.stopPropagation()}
                  >
                    <button onClick={() => handleEditar(p)} style={estiloBotonEditar}>
                      ✏️ Editar
                    </button>
                    <button onClick={() => handleEliminar(p.idPlantilla)} style={estiloBotonEliminar}>
                      🗑️ Eliminar
                    </button>
                  </div>
                </div>

                {/* Panel detalle expandido */}
                {plantillaActiva === p.idPlantilla && (
                  <div style={{
                    backgroundColor: "#1e1e1e",
                    border: "1px solid #3a3a3a",
                    borderTop: "none",
                    borderRadius: "0 0 8px 8px",
                    padding: "20px",
                  }}>
                    <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>

                      <div style={estiloDetalleFila}>
                        <span style={estiloDetalleLabel}>Horario</span>
                        <span style={estiloDetalleValor}>{p.horaInicio} – {p.horaTermino}</span>
                      </div>

                      <div style={estiloDetalleFila}>
                        <span style={estiloDetalleLabel}>Cargo</span>
                        <span style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <span style={{
                            width: "10px", height: "10px", borderRadius: "50%",
                            backgroundColor: slot?.colorPosicion || "#888",
                            display: "inline-block",
                          }} />
                          <span style={estiloDetalleValor}>{slot?.nombrePosicion || "—"}</span>
                        </span>
                      </div>

                      {p.descripcion && (
                        <div style={estiloDetalleFila}>
                          <span style={estiloDetalleLabel}>Descripción</span>
                          <span style={{ ...estiloDetalleValor, color: "#aaa" }}>{p.descripcion}</span>
                        </div>
                      )}

                    </div>
                  </div>
                )}

              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

// Estilos
const estiloSeccion = {
  backgroundColor: "#2d2d2d",
  borderRadius: "8px",
  padding: "24px",
  marginBottom: "30px",
};
const estiloTitulo = {
  color: "#ffcc00",
  fontSize: "1.1rem",
  fontWeight: "bold",
  marginBottom: "16px",
  paddingBottom: "10px",
  borderBottom: "1px solid #444",
};
const estiloGrid = {
  display: "grid",
  gridTemplateColumns: "1fr 1fr 1fr",
  gap: "16px",
};
const estiloCampo = {
  display: "flex",
  flexDirection: "column",
  gap: "6px",
};
const estiloLabel = {
  color: "#ccc",
  fontSize: "0.85em",
};
const estiloInput = {
  padding: "8px 12px",
  borderRadius: "6px",
  border: "1px solid #444",
  backgroundColor: "#1a1a1a",
  color: "#fff",
  fontSize: "0.9em",
};
const estiloBotonPrimario = {
  padding: "8px 20px",
  backgroundColor: "#ffcc00",
  color: "#1a1a1a",
  border: "none",
  borderRadius: "6px",
  fontWeight: "bold",
  cursor: "pointer",
};
const estiloBotonSecundario = {
  padding: "8px 20px",
  backgroundColor: "transparent",
  color: "#aaa",
  border: "1px solid #444",
  borderRadius: "6px",
  cursor: "pointer",
};
const estiloBotonEditar = {
  padding: "6px 12px",
  backgroundColor: "#2a2a2a",
  color: "#ffcc00",
  border: "1px solid #ffcc00",
  borderRadius: "6px",
  cursor: "pointer",
  fontSize: "0.85em",
};
const estiloBotonEliminar = {
  padding: "6px 12px",
  backgroundColor: "#2a2a2a",
  color: "#ff6b6b",
  border: "1px solid #ff6b6b",
  borderRadius: "6px",
  cursor: "pointer",
  fontSize: "0.85em",
};
const estiloDetalleFila = {
  display: "flex",
  alignItems: "center",
  gap: "16px",
};
const estiloDetalleLabel = {
  color: "#888",
  fontSize: "0.85em",
  minWidth: "90px",
};
const estiloDetalleValor = {
  color: "#ddd",
  fontSize: "0.9em",
};

export default PlantillasTurno;
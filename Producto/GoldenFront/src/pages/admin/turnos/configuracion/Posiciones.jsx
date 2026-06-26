// src/pages/admin/turnos/configuracion/Posiciones.jsx
import React, { useEffect, useState } from "react";
import {
  getPosiciones,
  createPosicion,
  updatePosicion,
  deletePosicion,
} from "../../../../services/posicionesService.mock";

const FORM_VACIO = { nombre: "", descripcion: "", sueldo: "", color: "#ffcc00" };

function Posiciones() {
  const [posiciones, setPosiciones]   = useState([]);
  const [form, setForm]               = useState(FORM_VACIO);
  const [editandoId, setEditandoId]   = useState(null);
  const [error, setError]             = useState("");
  const [cargando, setCargando]       = useState(false);

  useEffect(() => { cargar(); }, []);

  const cargar = async () => {
    try {
      const data = await getPosiciones();
      setPosiciones(data);
    } catch {
      setError("Error al cargar posiciones.");
    }
  };

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async () => {
    if (!form.nombre || !form.sueldo) {
      setError("Nombre y sueldo son obligatorios.");
      return;
    }
    setError("");
    setCargando(true);
    try {
      if (editandoId) {
        await updatePosicion(editandoId, form);
      } else {
        await createPosicion(form);
      }
      setForm(FORM_VACIO);
      setEditandoId(null);
      await cargar();
    } catch {
      setError("Error al guardar la posición.");
    } finally {
      setCargando(false);
    }
  };

  const handleEditar = (p) => {
    setEditandoId(p.idPosicion);
    setForm({
      nombre:      p.nombre,
      descripcion: p.descripcion || "",
      sueldo:      p.sueldo,
      color:       p.color || "#ffcc00",
    });
    setError("");
  };

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Eliminar esta posición?")) return;
    try {
      await deletePosicion(id);
      await cargar();
    } catch {
      setError("No se puede eliminar — puede estar en uso.");
    }
  };

  const handleCancelar = () => {
    setForm(FORM_VACIO);
    setEditandoId(null);
    setError("");
  };

  return (
    <div>
      {/* Formulario */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>
          {editandoId ? "✏️ Editar Posición" : "➕ Nueva Posición"}
        </h2>

        <div style={estiloGrid}>
          <div style={estiloCampo}>
            <label style={estiloLabel}>Nombre *</label>
            <input
              name="nombre"
              value={form.nombre}
              onChange={handleChange}
              placeholder="Ej: Cajero"
              style={estiloInput}
            />
          </div>

          <div style={estiloCampo}>
            <label style={estiloLabel}>Sueldo base *</label>
            <input
              name="sueldo"
              type="number"
              value={form.sueldo}
              onChange={handleChange}
              placeholder="Ej: 500000"
              style={estiloInput}
            />
          </div>

          <div style={estiloCampo}>
            <label style={estiloLabel}>Color</label>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <input
                name="color"
                type="color"
                value={form.color}
                onChange={handleChange}
                style={{ width: "48px", height: "36px", border: "none", background: "none", cursor: "pointer" }}
              />
              <span style={{ color: "#aaa", fontSize: "0.85em" }}>{form.color}</span>
            </div>
          </div>

          <div style={{ ...estiloCampo, gridColumn: "1 / -1" }}>
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

        {error && <p style={{ color: "#ff6b6b", marginTop: "10px" }}>{error}</p>}

        <div style={{ display: "flex", gap: "10px", marginTop: "16px" }}>
          <button onClick={handleSubmit} disabled={cargando} style={estiloBotonPrimario}>
            {cargando ? "Guardando..." : editandoId ? "Actualizar" : "Crear"}
          </button>
          {editandoId && (
            <button onClick={handleCancelar} style={estiloBotonSecundario}>
              Cancelar
            </button>
          )}
        </div>
      </div>

      {/* Tabla */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>📋 Posiciones registradas</h2>
        {posiciones.length === 0 ? (
          <p style={{ color: "#888" }}>No hay posiciones registradas.</p>
        ) : (
          <table style={estiloTabla}>
            <thead>
              <tr>
                {["Color", "Nombre", "Descripción", "Sueldo base", "Acciones"].map(h => (
                  <th key={h} style={estiloTh}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {posiciones.map(p => (
                <tr key={p.idPosicion} style={estiloTr}>
                  <td style={estiloTd}>
                    <span style={{
                      display: "inline-block",
                      width: "20px",
                      height: "20px",
                      borderRadius: "50%",
                      backgroundColor: p.color || "#ffcc00",
                    }} />
                  </td>
                  <td style={estiloTd}>{p.nombre}</td>
                  <td style={{ ...estiloTd, color: "#aaa" }}>{p.descripcion || "—"}</td>
                  <td style={estiloTd}>${Number(p.sueldo).toLocaleString("es-CL")}</td>
                  <td style={estiloTd}>
                    <div style={{ display: "flex", gap: "8px" }}>
                      <button onClick={() => handleEditar(p)} style={estiloBotonEditar}>
                        ✏️ Editar
                      </button>
                      <button onClick={() => handleEliminar(p.idPosicion)} style={estiloBotonEliminar}>
                        🗑️ Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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
const estiloTabla = {
  width: "100%",
  borderCollapse: "collapse",
};
const estiloTh = {
  textAlign: "left",
  padding: "10px 14px",
  color: "#ffcc00",
  borderBottom: "1px solid #444",
  fontSize: "0.85em",
};
const estiloTd = {
  padding: "10px 14px",
  color: "#ddd",
  borderBottom: "1px solid #333",
  fontSize: "0.9em",
};
const estiloTr = {
  transition: "background 0.15s",
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

export default Posiciones;
// src/pages/admin/turnos/Planificacion.jsx
import React, { useState, useEffect } from "react";
import "../../../styles/gestionProd.css";

import {
  getCalendarios,
  createCalendario,
  updateCalendario,
  deleteCalendario,
  getHorariosPorCalendario,
  asignarTrabajador,
  deleteHorario,
  getSemanasParaSelect,
  getTrabajadoresActivos,
} from "../../../services/planificacionService.mock";

const DIAS = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"];

const estiloSeccion = {
  backgroundColor: "#2d2d2d",
  borderRadius: "8px",
  padding: "24px",
  marginBottom: "30px",
};

const estiloTituloSeccion = {
  color: "#ffcc00",
  fontSize: "1.2rem",
  fontWeight: "bold",
  marginBottom: "16px",
  paddingBottom: "10px",
  borderBottom: "1px solid #444",
};

const estiloInput = {
  backgroundColor: "#1a1a1a",
  border: "1px solid #444",
  borderRadius: "6px",
  color: "#fff",
  padding: "8px 12px",
  fontSize: "0.9rem",
  width: "100%",
  boxSizing: "border-box",
};

const estiloBoton = (variante = "primary") => ({
  padding: "8px 16px",
  borderRadius: "6px",
  border: "none",
  cursor: "pointer",
  fontWeight: "bold",
  fontSize: "0.85rem",
  backgroundColor:
    variante === "primary"  ? "#ffcc00" :
    variante === "danger"   ? "#c0392b" :
    variante === "ghost"    ? "transparent" : "#444",
  color:
    variante === "primary"  ? "#1a1a1a" :
    variante === "ghost"    ? "#aaa" : "#fff",
  border: variante === "ghost" ? "1px solid #555" : "none",
});

function etiquetaEstado(estado) {
  const mapa = {
    pendiente:  { label: "Pendiente",  color: "#f39c12" },
    confirmado: { label: "Confirmado", color: "#27ae60" },
    ausente:    { label: "Ausente",    color: "#c0392b" },
  };
  const e = mapa[estado] || { label: estado, color: "#888" };
  return (
    <span style={{
      backgroundColor: e.color + "22",
      color: e.color,
      border: `1px solid ${e.color}55`,
      borderRadius: "12px",
      padding: "2px 10px",
      fontSize: "0.78rem",
      fontWeight: "bold",
    }}>
      {e.label}
    </span>
  );
}

function agruparPorFecha(horarios) {
  return horarios.reduce((acc, h) => {
    if (!acc[h.fechaTrabajo]) acc[h.fechaTrabajo] = [];
    acc[h.fechaTrabajo].push(h);
    return acc;
  }, {});
}

function formatearFecha(fechaStr) {
  const [y, m, d] = fechaStr.split("-");
  const fecha = new Date(Number(y), Number(m) - 1, Number(d));
  const dia = DIAS[fecha.getDay() === 0 ? 6 : fecha.getDay() - 1];
  return `${dia} ${d}/${m}/${y}`;
}

function esFechaLunes(fechaStr) {
  const [y, m, d] = fechaStr.split("-");
  const fecha = new Date(Number(y), Number(m) - 1, Number(d));
  return fecha.getDay() === 1;
}

// ── Formulario de calendario ─────────────────────────────────────────────────

function FormularioCalendario({ semanas, inicial, onGuardar, onCancelar }) {
  const [form, setForm] = useState(
    inicial || { idSemana: "", fechaInicio: "", fechaFin: "" }
  );
  const [error, setError] = useState("");

  const cambiar = (campo, valor) => setForm(f => ({ ...f, [campo]: valor }));

  const guardar = () => {
    if (!form.idSemana || !form.fechaInicio || !form.fechaFin) {
      setError("Todos los campos son obligatorios.");
      return;
    }
    if (!esFechaLunes(form.fechaInicio)) {
      setError("La fecha de inicio debe ser lunes.");
      return;
    }
    if (form.fechaFin <= form.fechaInicio) {
      setError("La fecha de fin debe ser posterior al inicio.");
      return;
    }
    setError("");
    onGuardar(form);
  };

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "12px", alignItems: "end" }}>
      <div>
        <label style={{ color: "#aaa", fontSize: "0.8rem", display: "block", marginBottom: "4px" }}>
          Semana tipo
        </label>
        <select
          value={form.idSemana}
          onChange={e => cambiar("idSemana", e.target.value)}
          style={estiloInput}
        >
          <option value="">— Seleccionar —</option>
          {semanas.filter(s => s.activo).map(s => (
            <option key={s.idSemana} value={s.idSemana}>{s.nombre}</option>
          ))}
        </select>
      </div>

      <div>
        <label style={{ color: "#aaa", fontSize: "0.8rem", display: "block", marginBottom: "4px" }}>
          Fecha inicio <span style={{ color: "#888", fontWeight: "normal" }}>(debe ser lunes)</span>
        </label>
        <input
          type="date"
          value={form.fechaInicio}
          onChange={e => cambiar("fechaInicio", e.target.value)}
          style={estiloInput}
        />
      </div>

      <div>
        <label style={{ color: "#aaa", fontSize: "0.8rem", display: "block", marginBottom: "4px" }}>
          Fecha fin
        </label>
        <input
          type="date"
          value={form.fechaFin}
          onChange={e => cambiar("fechaFin", e.target.value)}
          style={estiloInput}
        />
      </div>

      {error && (
        <div style={{ gridColumn: "1 / -1", color: "#e74c3c", fontSize: "0.85rem" }}>
          ⚠ {error}
        </div>
      )}

      <div style={{ gridColumn: "1 / -1", display: "flex", gap: "10px" }}>
        <button style={estiloBoton("primary")} onClick={guardar}>
          {inicial ? "💾 Guardar cambios" : "➕ Crear calendario"}
        </button>
        {onCancelar && (
          <button style={estiloBoton("ghost")} onClick={onCancelar}>Cancelar</button>
        )}
      </div>
    </div>
  );
}

// ── Tabla de horarios de un calendario ──────────────────────────────────────

function TablaHorarios({ idCalendario, trabajadores, onHorarioActualizado }) {
  const [horarios, setHorarios]   = useState([]);
  const [cargando, setCargando]   = useState(true);
  const [asignando, setAsignando] = useState({});

  useEffect(() => {
    setCargando(true);
    getHorariosPorCalendario(idCalendario)
      .then(setHorarios)
      .finally(() => setCargando(false));
  }, [idCalendario]);

  const handleAsignar = async (idHorario, idTrabajador) => {
    if (!idTrabajador) return;
    try {
      const actualizado = await asignarTrabajador(idHorario, Number(idTrabajador));
      setHorarios(hs => hs.map(h => h.idHorario === idHorario ? actualizado : h));
      setAsignando(a => ({ ...a, [idHorario]: "" }));
      onHorarioActualizado && onHorarioActualizado();
    } catch (e) {
      alert("Error al asignar trabajador.");
    }
  };

  const handleEliminar = async (idHorario) => {
    if (!window.confirm("¿Eliminar este horario pendiente?")) return;
    try {
      await deleteHorario(idHorario);
      setHorarios(hs => hs.filter(h => h.idHorario !== idHorario));
    } catch (e) {
      alert("Solo se pueden eliminar horarios en estado pendiente.");
    }
  };

  if (cargando) return <p style={{ color: "#888", padding: "12px" }}>Cargando horarios...</p>;
  if (!horarios.length) return <p style={{ color: "#888", padding: "12px" }}>No hay horarios generados para este calendario.</p>;

  const porFecha = agruparPorFecha(horarios);

  return (
    <div style={{ marginTop: "12px" }}>
      {Object.entries(porFecha).sort(([a], [b]) => a.localeCompare(b)).map(([fecha, filas]) => (
        <div key={fecha} style={{ marginBottom: "16px" }}>
          <div style={{
            color: "#ffcc00",
            fontSize: "0.85rem",
            fontWeight: "bold",
            marginBottom: "6px",
            paddingLeft: "4px",
          }}>
            {formatearFecha(fecha)}
          </div>
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "0.85rem" }}>
            <thead>
              <tr style={{ color: "#888", borderBottom: "1px solid #444" }}>
                <th style={{ textAlign: "left",  padding: "4px 8px" }}>Plantilla</th>
                <th style={{ textAlign: "left",  padding: "4px 8px" }}>Horario</th>
                <th style={{ textAlign: "left",  padding: "4px 8px" }}>Posición</th>
                <th style={{ textAlign: "left",  padding: "4px 8px" }}>Trabajador</th>
                <th style={{ textAlign: "left",  padding: "4px 8px" }}>Estado</th>
                <th style={{ textAlign: "center", padding: "4px 8px" }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filas.map(h => (
                <tr key={h.idHorario} style={{ borderBottom: "1px solid #333" }}>
                  <td style={{ padding: "6px 8px", color: "#fff" }}>{h.nombrePlantilla}</td>
                  <td style={{ padding: "6px 8px", color: "#aaa" }}>{h.horaInicio}–{h.horaTermino}</td>
                  <td style={{ padding: "6px 8px", color: "#aaa" }}>{h.nombrePosicion}</td>
                  <td style={{ padding: "6px 8px" }}>
                    {h.estado === "pendiente" ? (
                      <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
                        <select
                          value={asignando[h.idHorario] || ""}
                          onChange={e => setAsignando(a => ({ ...a, [h.idHorario]: e.target.value }))}
                          style={{ ...estiloInput, width: "160px", padding: "4px 8px" }}
                        >
                          <option value="">— Asignar —</option>
                          {trabajadores.map(t => (
                            <option key={t.idTrabajador} value={t.idTrabajador}>{t.nombre}</option>
                          ))}
                        </select>
                        <button
                          style={estiloBoton("primary")}
                          onClick={() => handleAsignar(h.idHorario, asignando[h.idHorario])}
                        >
                          ✓
                        </button>
                      </div>
                    ) : (
                      <span style={{ color: "#fff" }}>{h.nombreTrabajador || "—"}</span>
                    )}
                  </td>
                  <td style={{ padding: "6px 8px" }}>{etiquetaEstado(h.estado)}</td>
                  <td style={{ padding: "6px 8px", textAlign: "center" }}>
                    {h.estado === "pendiente" && (
                      <button
                        style={estiloBoton("danger")}
                        onClick={() => handleEliminar(h.idHorario)}
                      >
                        🗑
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
    </div>
  );
}

// ── Componente principal ─────────────────────────────────────────────────────

function Planificacion() {
  const [calendarios,   setCalendarios]   = useState([]);
  const [semanas,       setSemanas]       = useState([]);
  const [trabajadores,  setTrabajadores]  = useState([]);
  const [expandido,     setExpandido]     = useState(null);
  const [editando,      setEditando]      = useState(null);
  const [cargando,      setCargando]      = useState(true);

  useEffect(() => {
    Promise.all([getCalendarios(), getSemanasParaSelect(), getTrabajadoresActivos()])
      .then(([cals, sems, trabs]) => {
        setCalendarios(cals);
        setSemanas(sems);
        setTrabajadores(trabs);
      })
      .finally(() => setCargando(false));
  }, []);

  const handleCrear = async (form) => {
    try {
      const nuevo = await createCalendario(form);
      setCalendarios(cs => [...cs, nuevo]);
      setExpandido(nuevo.idCalendario);
    } catch (e) {
      alert("Error al crear calendario.");
    }
  };

  const handleEditar = async (form) => {
    try {
      const actualizado = await updateCalendario(editando.idCalendario, form);
      setCalendarios(cs => cs.map(c => c.idCalendario === editando.idCalendario ? actualizado : c));
      setEditando(null);
    } catch (e) {
      alert("Error al actualizar calendario.");
    }
  };

  const handleEliminar = async (idCalendario) => {
    if (!window.confirm("¿Eliminar este calendario? Los horarios generados no se eliminarán automáticamente.")) return;
    try {
      await deleteCalendario(idCalendario);
      setCalendarios(cs => cs.filter(c => c.idCalendario !== idCalendario));
      if (expandido === idCalendario) setExpandido(null);
    } catch (e) {
      alert("Error al eliminar calendario.");
    }
  };

  const toggleExpandir = (id) => setExpandido(e => e === id ? null : id);

  if (cargando) return (
    <main className="prod-admin-content">
      <p style={{ color: "#aaa", textAlign: "center", marginTop: "60px" }}>Cargando...</p>
    </main>
  );

  return (
    <main className="prod-admin-content">
      <h1 className="titulo-gp">📋 Planificación</h1>
      <p style={{ color: "#aaa", textAlign: "center", marginBottom: "40px" }}>
        Aplica semanas tipo al calendario y asigna trabajadores a los turnos generados.
      </p>

      {/* ── Sección A: Crear calendario ── */}
      <div style={estiloSeccion}>
        <h2 style={estiloTituloSeccion}>Aplicar semana al calendario</h2>
        <FormularioCalendario
          semanas={semanas}
          onGuardar={handleCrear}
        />
      </div>

      {/* ── Sección A: Lista de calendarios ── */}
      {calendarios.length > 0 && (
        <div style={estiloSeccion}>
          <h2 style={estiloTituloSeccion}>Calendarios creados</h2>
          {calendarios.map(c => (
            <div key={c.idCalendario}>
              {/* Fila del calendario */}
              {editando?.idCalendario === c.idCalendario ? (
                <div style={{ padding: "16px", backgroundColor: "#242424", borderRadius: "6px", marginBottom: "8px" }}>
                  <FormularioCalendario
                    semanas={semanas}
                    inicial={{ idSemana: c.idSemana, fechaInicio: c.fechaInicio, fechaFin: c.fechaFin }}
                    onGuardar={handleEditar}
                    onCancelar={() => setEditando(null)}
                  />
                </div>
              ) : (
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "12px 16px",
                    backgroundColor: expandido === c.idCalendario ? "#242424" : "#333",
                    borderRadius: expandido === c.idCalendario ? "6px 6px 0 0" : "6px",
                    marginBottom: expandido === c.idCalendario ? "0" : "8px",
                    cursor: "pointer",
                    borderLeft: "3px solid #ffcc00",
                  }}
                  onClick={() => toggleExpandir(c.idCalendario)}
                >
                  <div>
                    <span style={{ color: "#fff", fontWeight: "bold" }}>{c.nombreSemana}</span>
                    <span style={{ color: "#888", fontSize: "0.85rem", marginLeft: "12px" }}>
                      {formatearFecha(c.fechaInicio)} → {formatearFecha(c.fechaFin)}
                    </span>
                  </div>
                  <div style={{ display: "flex", gap: "8px" }} onClick={e => e.stopPropagation()}>
                    <button style={estiloBoton("ghost")} onClick={() => setEditando(c)}>✏️ Editar</button>
                    <button style={estiloBoton("danger")} onClick={() => handleEliminar(c.idCalendario)}>🗑</button>
                    <span style={{ color: "#888", alignSelf: "center", marginLeft: "4px" }}>
                      {expandido === c.idCalendario ? "▲" : "▼"}
                    </span>
                  </div>
                </div>
              )}

              {/* Panel expandido con horarios */}
              {expandido === c.idCalendario && (
                <div style={{
                  backgroundColor: "#242424",
                  borderRadius: "0 0 6px 6px",
                  padding: "0 16px 16px",
                  marginBottom: "8px",
                }}>
                  <TablaHorarios
                    idCalendario={c.idCalendario}
                    trabajadores={trabajadores}
                  />
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </main>
  );
}

export default Planificacion;
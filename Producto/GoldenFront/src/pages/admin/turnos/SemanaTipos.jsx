// src/pages/admin/turnos/SemanaTipos.jsx
import React, { useEffect, useState } from "react";
import {
  getSemanaTipos,
  createSemanaTipo,
  updateSemanaTipo,
  deleteSemanaTipo,
  getAsignacionesPorSemana,
  createAsignacion,
  deleteAsignacion,
  getPlantillasParaSelect,
} from "../../../services/semanaTiposService.mock";
import "../../../styles/gestionProd.css";

const DIAS = [
  { numero: 1, label: "Lunes" },
  { numero: 2, label: "Martes" },
  { numero: 3, label: "Miércoles" },
  { numero: 4, label: "Jueves" },
  { numero: 5, label: "Viernes" },
  { numero: 6, label: "Sábado" },
  { numero: 7, label: "Domingo" },
];

const FORM_SEMANA_VACIO = { nombre: "", descripcion: "" };

function SemanaTipos() {
  const [semanas, setSemanas]                     = useState([]);
  const [plantillas, setPlantillas]               = useState([]);
  const [semanaActiva, setSemanaActiva]           = useState(null);       // id expandida
  const [asignacionesPorSemana, setAsignacionesPorSemana] = useState({}); // { idSemana: [...] }
  const [formSemana, setFormSemana]               = useState(FORM_SEMANA_VACIO);
  const [editandoSemanaId, setEditandoSemanaId]   = useState(null);
  // { idSemana: { diaSemana: idPlantilla } } — controla qué select está abierto por día
  const [agregandoEn, setAgregandoEn]             = useState({});
  const [error, setError]                         = useState("");
  const [cargando, setCargando]                   = useState(false);

  useEffect(() => { cargar(); }, []);

  const cargar = async () => {
    setCargando(true);
    try {
      const [sData, pData] = await Promise.all([getSemanaTipos(), getPlantillasParaSelect()]);
      setSemanas(sData);
      setPlantillas(pData);
    } catch {
      setError("Error al cargar datos.");
    } finally {
      setCargando(false);
    }
  };

  const cargarAsignaciones = async (idSemana) => {
    try {
      const data = await getAsignacionesPorSemana(idSemana);
      setAsignacionesPorSemana(prev => ({ ...prev, [idSemana]: data }));
    } catch {
      setError("Error al cargar asignaciones.");
    }
  };

  // — SemanaTipo —

  const handleChangeSemana = (e) =>
    setFormSemana(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmitSemana = async () => {
    if (!formSemana.nombre.trim()) {
      setError("El nombre es obligatorio.");
      return;
    }
    setError("");
    try {
      if (editandoSemanaId) {
        await updateSemanaTipo(editandoSemanaId, formSemana);
      } else {
        await createSemanaTipo(formSemana);
      }
      setFormSemana(FORM_SEMANA_VACIO);
      setEditandoSemanaId(null);
      await cargar();
    } catch {
      setError("Error al guardar la semana tipo.");
    }
  };

  const handleEditarSemana = (s) => {
    setEditandoSemanaId(s.idSemana);
    setFormSemana({ nombre: s.nombre, descripcion: s.descripcion || "" });
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDesactivarSemana = async (id) => {
    if (!window.confirm("¿Desactivar esta semana tipo?")) return;
    try {
      await deleteSemanaTipo(id);
      if (semanaActiva === id) setSemanaActiva(null);
      await cargar();
    } catch {
      setError("Error al desactivar la semana tipo.");
    }
  };

  const handleCancelarSemana = () => {
    setFormSemana(FORM_SEMANA_VACIO);
    setEditandoSemanaId(null);
    setError("");
  };

  const toggleSemana = async (id) => {
    if (semanaActiva === id) {
      setSemanaActiva(null);
      return;
    }
    setSemanaActiva(id);
    setAgregandoEn({});
    setError("");
    if (!asignacionesPorSemana[id]) {
      await cargarAsignaciones(id);
    }
  };

  // — AsignacionTurno —

  // Abre el mini-select de una celda de día
  const handleAbrirAgregar = (idSemana, diaSemana) => {
    setAgregandoEn(prev => {
      const key = `${idSemana}-${diaSemana}`;
      // Si ya está abierto, cierra; si no, abre con plantilla vacía
      if (prev[key] !== undefined) {
        const next = { ...prev };
        delete next[key];
        return next;
      }
      return { ...prev, [key]: "" };
    });
    setError("");
  };

  const handleChangeSelectAsignacion = (idSemana, diaSemana, idPlantilla) => {
    const key = `${idSemana}-${diaSemana}`;
    setAgregandoEn(prev => ({ ...prev, [key]: idPlantilla }));
  };

  const handleConfirmarAsignacion = async (idSemana, diaSemana) => {
    const key = `${idSemana}-${diaSemana}`;
    const idPlantilla = agregandoEn[key];
    if (!idPlantilla) {
      setError("Selecciona una plantilla.");
      return;
    }
    setError("");
    try {
      await createAsignacion({ idSemana, idPlantilla, diaSemana });
      // Cierra el selector y recarga asignaciones de esta semana
      setAgregandoEn(prev => {
        const next = { ...prev };
        delete next[key];
        return next;
      });
      await cargarAsignaciones(idSemana);
    } catch {
      setError("Error al crear la asignación.");
    }
  };

  const handleEliminarAsignacion = async (idAsignacion, idSemana) => {
    if (!window.confirm("¿Eliminar esta asignación?")) return;
    try {
      await deleteAsignacion(idAsignacion);
      await cargarAsignaciones(idSemana);
    } catch {
      setError("No se puede eliminar — puede tener horarios generados.");
    }
  };

  // Helpers
  const asignacionesDeEstaSemana = (idSemana) =>
    asignacionesPorSemana[idSemana] || [];

  const asignacionesPorDia = (idSemana, diaSemana) =>
    asignacionesDeEstaSemana(idSemana).filter(a => a.diaSemana === diaSemana);

  const horasPlantilla = (idPlantilla) => {
    const p = plantillas.find(p => p.idPlantilla === idPlantilla);
    return p ? `${p.horaInicio} – ${p.horaTermino}` : "";
  };

  if (cargando) return <p style={{ color: "#aaa", padding: "20px" }}>Cargando semanas tipo...</p>;

  return (
    <main className="prod-admin-content">
      <h1 className="titulo-gp">🗓️ Semanas Tipo</h1>
      <p style={{ color: "#aaa", textAlign: "center", marginBottom: "40px" }}>
        Define los tipos de semana y asigna plantillas de turno a cada día.
      </p>

      {error && <p style={{ color: "#ff6b6b", marginBottom: "16px" }}>{error}</p>}

      {/* Formulario semana tipo */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>
          {editandoSemanaId ? "✏️ Editar Semana Tipo" : "➕ Nueva Semana Tipo"}
        </h2>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
          <div style={estiloCampo}>
            <label style={estiloLabel}>Nombre *</label>
            <input
              name="nombre"
              value={formSemana.nombre}
              onChange={handleChangeSemana}
              placeholder="Ej: Semana Normal"
              style={estiloInput}
            />
          </div>
          <div style={estiloCampo}>
            <label style={estiloLabel}>Descripción</label>
            <input
              name="descripcion"
              value={formSemana.descripcion}
              onChange={handleChangeSemana}
              placeholder="Opcional"
              style={estiloInput}
            />
          </div>
        </div>

        <div style={{ display: "flex", gap: "10px", marginTop: "16px" }}>
          <button onClick={handleSubmitSemana} style={estiloBotonPrimario}>
            {editandoSemanaId ? "Actualizar" : "Crear"}
          </button>
          {editandoSemanaId && (
            <button onClick={handleCancelarSemana} style={estiloBotonSecundario}>
              Cancelar
            </button>
          )}
        </div>
      </div>

      {/* Lista de semanas tipo */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>📋 Semanas registradas</h2>

        {semanas.length === 0 ? (
          <p style={{ color: "#888" }}>No hay semanas tipo registradas.</p>
        ) : (
          <>
            {/* Activas */}
            {semanas.filter(s => s.activo).map(s => (
              <FilaSemana
                key={s.idSemana}
                semana={s}
                activa={semanaActiva === s.idSemana}
                onToggle={() => toggleSemana(s.idSemana)}
                onEditar={() => handleEditarSemana(s)}
                onDesactivar={() => handleDesactivarSemana(s.idSemana)}
                dias={DIAS}
                asignacionesPorDia={(dia) => asignacionesPorDia(s.idSemana, dia)}
                plantillas={plantillas}
                horasPlantilla={horasPlantilla}
                agregandoEn={agregandoEn}
                onAbrirAgregar={(dia) => handleAbrirAgregar(s.idSemana, dia)}
                onChangeSelect={(dia, val) => handleChangeSelectAsignacion(s.idSemana, dia, val)}
                onConfirmarAsignacion={(dia) => handleConfirmarAsignacion(s.idSemana, dia)}
                onEliminarAsignacion={(idAsignacion) => handleEliminarAsignacion(idAsignacion, s.idSemana)}
              />
            ))}

            {/* Inactivas */}
            {semanas.filter(s => !s.activo).length > 0 && (
              <>
                <p style={{ color: "#666", fontSize: "0.85em", margin: "20px 0 10px" }}>
                  — Semanas inactivas —
                </p>
                {semanas.filter(s => !s.activo).map(s => (
                  <FilaSemana
                    key={s.idSemana}
                    semana={s}
                    inactiva
                    activa={false}
                    onToggle={() => {}}
                    onEditar={() => {}}
                    onDesactivar={() => {}}
                    dias={DIAS}
                    asignacionesPorDia={() => []}
                    plantillas={plantillas}
                    horasPlantilla={horasPlantilla}
                    agregandoEn={{}}
                    onAbrirAgregar={() => {}}
                    onChangeSelect={() => {}}
                    onConfirmarAsignacion={() => {}}
                    onEliminarAsignacion={() => {}}
                  />
                ))}
              </>
            )}
          </>
        )}
      </div>
    </main>
  );
}

// — Subcomponente fila semana —

function FilaSemana({
  semana, activa, inactiva,
  onToggle, onEditar, onDesactivar,
  dias, asignacionesPorDia, plantillas, horasPlantilla,
  agregandoEn, onAbrirAgregar, onChangeSelect, onConfirmarAsignacion, onEliminarAsignacion,
}) {
  return (
    <div style={{ marginBottom: "8px", opacity: inactiva ? 0.45 : 1 }}>

      {/* Cabecera fila */}
      <div
        onClick={!inactiva ? onToggle : undefined}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "12px 16px",
          backgroundColor: activa ? "#3a3a3a" : "#242424",
          borderRadius: activa ? "8px 8px 0 0" : "8px",
          cursor: inactiva ? "default" : "pointer",
          borderLeft: activa ? "3px solid #ffcc00" : "3px solid transparent",
          transition: "all 0.2s",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          {!inactiva && (
            <span style={{ color: "#ffcc00", fontSize: "1em" }}>
              {activa ? "▼" : "▶"}
            </span>
          )}
          <span style={{ color: "#fff", fontWeight: "bold" }}>{semana.nombre}</span>
          {semana.descripcion && (
            <span style={{ color: "#666", fontSize: "0.8em" }}>{semana.descripcion}</span>
          )}
          {inactiva && (
            <span style={{
              backgroundColor: "#1a1a1a",
              color: "#888",
              fontSize: "0.75em",
              padding: "2px 8px",
              borderRadius: "12px",
              border: "1px solid #444",
            }}>
              Inactiva
            </span>
          )}
        </div>

        {!inactiva && (
          <div style={{ display: "flex", gap: "8px" }} onClick={e => e.stopPropagation()}>
            <button onClick={onEditar} style={estiloBotonEditar}>✏️ Editar</button>
            <button onClick={onDesactivar} style={estiloBotonEliminar}>🔴 Desactivar</button>
          </div>
        )}
      </div>

      {/* Panel expandido — 7 días */}
      {activa && (
        <div style={{
          backgroundColor: "#1e1e1e",
          border: "1px solid #3a3a3a",
          borderTop: "none",
          borderRadius: "0 0 8px 8px",
          padding: "20px",
        }}>
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                {["Día", "Plantillas asignadas", "Agregar turno"].map(h => (
                  <th key={h} style={estiloTh}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {dias.map(({ numero, label }) => {
                const asignaciones = asignacionesPorDia(numero);
                const key = `${semana.idSemana}-${numero}`;
                const selectorAbierto = agregandoEn[key] !== undefined;
                const valorSelector   = agregandoEn[key] || "";
                const esFinde = numero >= 6;

                return (
                  <tr key={numero} style={{ backgroundColor: esFinde ? "#252520" : "transparent" }}>

                    {/* Día */}
                    <td style={{ ...estiloTd, width: "110px", fontWeight: "bold", color: esFinde ? "#ffcc00" : "#ddd" }}>
                      {label}
                      {esFinde && <span style={{ fontSize: "0.7em", color: "#888", marginLeft: "4px" }}>FS</span>}
                    </td>

                    {/* Plantillas asignadas */}
                    <td style={estiloTd}>
                      {asignaciones.length === 0 ? (
                        <span style={{ color: "#555", fontStyle: "italic", fontSize: "0.85em" }}>Día libre</span>
                      ) : (
                        <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                          {asignaciones.map(a => (
                            <div
                              key={a.idAsignacion}
                              style={{
                                display: "inline-flex",
                                alignItems: "center",
                                gap: "8px",
                                backgroundColor: "#2a2a2a",
                                border: "1px solid #444",
                                borderRadius: "6px",
                                padding: "4px 10px",
                                width: "fit-content",
                              }}
                            >
                              <span style={{ color: "#fff", fontSize: "0.9em" }}>{a.nombrePlantilla}</span>
                              <span style={{ color: "#888", fontSize: "0.78em" }}>
                                {horasPlantilla(a.idPlantilla)}
                              </span>
                              <button
                                onClick={() => onEliminarAsignacion(a.idAsignacion)}
                                title="Eliminar asignación"
                                style={{
                                  background: "transparent",
                                  border: "none",
                                  color: "#ff6b6b",
                                  cursor: "pointer",
                                  fontSize: "0.85em",
                                  padding: "0 2px",
                                  lineHeight: 1,
                                }}
                              >
                                ✕
                              </button>
                            </div>
                          ))}
                        </div>
                      )}
                    </td>

                    {/* Selector agregar turno */}
                    <td style={{ ...estiloTd, width: "260px" }}>
                      {selectorAbierto ? (
                        <div style={{ display: "flex", gap: "8px", alignItems: "center", flexWrap: "wrap" }}>
                          <select
                            value={valorSelector}
                            onChange={e => onChangeSelect(numero, e.target.value)}
                            style={{ ...estiloInput, width: "160px", padding: "5px 8px" }}
                            autoFocus
                          >
                            <option value="">Seleccionar...</option>
                            {plantillas.map(p => (
                              <option key={p.idPlantilla} value={p.idPlantilla}>
                                {p.nombre} ({p.horaInicio}–{p.horaTermino})
                              </option>
                            ))}
                          </select>
                          <button
                            onClick={() => onConfirmarAsignacion(numero)}
                            style={{ ...estiloBotonPrimario, padding: "5px 12px", fontSize: "0.85em" }}
                          >
                            ✓
                          </button>
                          <button
                            onClick={() => onAbrirAgregar(numero)}
                            style={{ ...estiloBotonSecundario, padding: "5px 10px", fontSize: "0.85em" }}
                          >
                            ✕
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => onAbrirAgregar(numero)}
                          style={{
                            background: "transparent",
                            border: "1px dashed #555",
                            color: "#888",
                            borderRadius: "6px",
                            padding: "4px 12px",
                            cursor: "pointer",
                            fontSize: "0.82em",
                            transition: "all 0.2s",
                          }}
                          onMouseEnter={e => { e.target.style.borderColor = "#ffcc00"; e.target.style.color = "#ffcc00"; }}
                          onMouseLeave={e => { e.target.style.borderColor = "#555"; e.target.style.color = "#888"; }}
                        >
                          ➕ Agregar turno
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// — Estilos —
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
  borderBottom: "1px solid #2a2a2a",
  fontSize: "0.9em",
  verticalAlign: "middle",
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

export default SemanaTipos;
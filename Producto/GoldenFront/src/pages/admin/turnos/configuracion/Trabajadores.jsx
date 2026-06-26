// src/pages/admin/turnos/configuracion/Trabajadores.jsx
import React, { useEffect, useState } from "react";
import {
  getTrabajadoresLocal,
  getTrabajadoresUsuario,
  createTrabajadorLocal,
  updateTrabajadorLocal,
  desactivarTrabajador,
} from "../../../../services/trabajadoresLocalService.mock";
import { getPosiciones } from "../../../../services/posicionesService.mock";

function Trabajadores() {
  const [trabajadores, setTrabajadores] = useState([]);
  const [posiciones, setPosiciones]     = useState([]);
  const [editandoId, setEditandoId]     = useState(null);
  const [formEditar, setFormEditar]     = useState({ nombre: "", idPosicion: "" });
  const [error, setError]               = useState("");
  const [cargando, setCargando]         = useState(true);

  useEffect(() => { inicializar(); }, []);

  const inicializar = async () => {
    setCargando(true);
    setError("");
    try {
      const [posData, usuariosData] = await Promise.all([
        getPosiciones(),
        getTrabajadoresUsuario(),
      ]);
      setPosiciones(posData);

      // Sincronización: intentar crear cada trabajador de GESTIONUSUARIO en local
      await Promise.all(
        usuariosData.map(t =>
          createTrabajadorLocal({
            idTrabajador: t.idTrabajador,
            nombre:       t.nombreTrabajador,
            idPosicion:   null,
          }).catch(() => {}) // ignorar si ya existe
        )
      );

      const localData = await getTrabajadoresLocal();
      setTrabajadores(localData);
    } catch {
      setError("Error al cargar trabajadores.");
    } finally {
      setCargando(false);
    }
  };

  const handleEditar = (t) => {
    setEditandoId(t.idTrabajador);
    setFormEditar({
      nombre:     t.nombre,
      idPosicion: t.idPosicion || "",
    });
    setError("");
  };

  const handleGuardar = async (id) => {
    if (!formEditar.nombre) {
      setError("El nombre es obligatorio.");
      return;
    }
    try {
      await updateTrabajadorLocal(id, {
        nombre:     formEditar.nombre,
        idPosicion: formEditar.idPosicion || null,
      });
      setEditandoId(null);
      const data = await getTrabajadoresLocal();
      setTrabajadores(data);
    } catch {
      setError("Error al actualizar el trabajador.");
    }
  };

  const handleDesactivar = async (id) => {
    if (!window.confirm("¿Desactivar este trabajador?")) return;
    try {
      await desactivarTrabajador(id);
      const data = await getTrabajadoresLocal();
      setTrabajadores(data);
    } catch {
      setError("No se puede desactivar — puede tener horarios activos.");
    }
  };

  const handleCancelar = () => {
    setEditandoId(null);
    setFormEditar({ nombre: "", idPosicion: "" });
    setError("");
  };

  const activos   = trabajadores.filter(t => t.activo);
  const inactivos = trabajadores.filter(t => !t.activo);

  if (cargando) return <p style={{ color: "#aaa", padding: "20px" }}>Sincronizando trabajadores...</p>;

  return (
    <div>
      {error && <p style={{ color: "#ff6b6b", marginBottom: "16px" }}>{error}</p>}

      {/* Tabla activos */}
      <div style={estiloSeccion}>
        <h2 style={estiloTitulo}>👷 Trabajadores activos</h2>
        {activos.length === 0 ? (
          <p style={{ color: "#888" }}>No hay trabajadores activos.</p>
        ) : (
          <table style={estiloTabla}>
            <thead>
              <tr>
                {["ID", "Nombre", "Posición", "Acciones"].map(h => (
                  <th key={h} style={estiloTh}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {activos.map(t => (
                <tr key={t.idTrabajador}>
                  <td style={estiloTd}>{t.idTrabajador}</td>

                  <td style={estiloTd}>
                    {editandoId === t.idTrabajador ? (
                      <input
                        value={formEditar.nombre}
                        onChange={e => setFormEditar(p => ({ ...p, nombre: e.target.value }))}
                        style={estiloInput}
                      />
                    ) : t.nombre}
                  </td>

                  <td style={estiloTd}>
                    {editandoId === t.idTrabajador ? (
                      <select
                        value={formEditar.idPosicion}
                        onChange={e => setFormEditar(p => ({ ...p, idPosicion: e.target.value }))}
                        style={estiloInput}
                      >
                        <option value="">Sin posición</option>
                        {posiciones.map(p => (
                          <option key={p.idPosicion} value={p.idPosicion}>
                            {p.nombre}
                          </option>
                        ))}
                      </select>
                    ) : (
                      t.nombrePosicion ? (
                        <span style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <span style={{
                            width: "12px", height: "12px", borderRadius: "50%",
                            backgroundColor: posiciones.find(p => p.idPosicion === t.idPosicion)?.color || "#888",
                            display: "inline-block",
                          }} />
                          {t.nombrePosicion}
                        </span>
                      ) : <span style={{ color: "#888" }}>Sin asignar</span>
                    )}
                  </td>

                  <td style={estiloTd}>
                    {editandoId === t.idTrabajador ? (
                      <div style={{ display: "flex", gap: "8px" }}>
                        <button onClick={() => handleGuardar(t.idTrabajador)} style={estiloBotonPrimario}>
                          Guardar
                        </button>
                        <button onClick={handleCancelar} style={estiloBotonSecundario}>
                          Cancelar
                        </button>
                      </div>
                    ) : (
                      <div style={{ display: "flex", gap: "8px" }}>
                        <button onClick={() => handleEditar(t)} style={estiloBotonEditar}>
                          ✏️ Editar
                        </button>
                        <button onClick={() => handleDesactivar(t.idTrabajador)} style={estiloBotonEliminar}>
                          🚫 Desactivar
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Tabla inactivos */}
      {inactivos.length > 0 && (
        <div style={estiloSeccion}>
          <h2 style={estiloTitulo}>🚫 Trabajadores inactivos</h2>
          <table style={estiloTabla}>
            <thead>
              <tr>
                {["ID", "Nombre", "Posición"].map(h => (
                  <th key={h} style={estiloTh}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {inactivos.map(t => (
                <tr key={t.idTrabajador} style={{ opacity: 0.5 }}>
                  <td style={estiloTd}>{t.idTrabajador}</td>
                  <td style={estiloTd}>{t.nombre}</td>
                  <td style={estiloTd}>{t.nombrePosicion || <span style={{ color: "#888" }}>Sin asignar</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
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
const estiloTabla = { width: "100%", borderCollapse: "collapse" };
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
const estiloInput = {
  padding: "6px 10px",
  borderRadius: "6px",
  border: "1px solid #444",
  backgroundColor: "#1a1a1a",
  color: "#fff",
  fontSize: "0.9em",
  width: "100%",
};
const estiloBotonPrimario = {
  padding: "6px 16px",
  backgroundColor: "#ffcc00",
  color: "#1a1a1a",
  border: "none",
  borderRadius: "6px",
  fontWeight: "bold",
  cursor: "pointer",
};
const estiloBotonSecundario = {
  padding: "6px 16px",
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

export default Trabajadores;
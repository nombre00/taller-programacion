import React, { useState } from "react";
import Sidebar from "../../components/Sidebar";
import ConfiguracionTurnos from "./turnos/ConfiguracionTurnos";
import SemanaTipos from "./turnos/SemanaTipos";
import Planificacion from "./turnos/Planificacion";
import CalendarioTurnos from "./turnos/CalendarioTurnos";
import "../../styles/gestionProd.css";

const PESTANAS = [
  { id: "calendario",   label: "📅 Calendario" },
  { id: "planificacion", label: "📋 Planificación" },
  { id: "semanas",      label: "🗓️ Semanas Tipo" },
  { id: "configuracion", label: "⚙️ Configuración" },
];

function GestionTurnos() {
  const [pestanaActiva, setPestanaActiva] = useState("configuracion");

  return (
    <section className="gestion-admin-layout">
      <Sidebar />

      <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: "100vh" }}>

        {/* Barra de pestañas */}
        <div style={{
          display: "flex",
          borderBottom: "2px solid #ffcc00",
          backgroundColor: "#1a1a1a",
          padding: "0 20px",
          flexWrap: "wrap",
        }}>
          {PESTANAS.map(p => (
            <button
              key={p.id}
              onClick={() => setPestanaActiva(p.id)}
              style={{
                padding: "14px 24px",
                background: "transparent",
                border: "none",
                borderBottom: pestanaActiva === p.id ? "3px solid #ffcc00" : "3px solid transparent",
                color: pestanaActiva === p.id ? "#ffcc00" : "#aaa",
                fontWeight: pestanaActiva === p.id ? "bold" : "normal",
                fontSize: "0.95em",
                cursor: "pointer",
                transition: "all 0.2s",
                marginBottom: "-2px",
                whiteSpace: "nowrap",
              }}
            >
              {p.label}
            </button>
          ))}
        </div>

        {/* Contenido de la pestaña activa */}
        {pestanaActiva === "configuracion"  && <ConfiguracionTurnos />}
        {pestanaActiva === "semanas"         && <SemanaTipos />}
        {pestanaActiva === "planificacion"   && <Planificacion />}
        {pestanaActiva === "calendario"      && <CalendarioTurnos />}
      </div>
    </section>
  );
}

export default GestionTurnos;
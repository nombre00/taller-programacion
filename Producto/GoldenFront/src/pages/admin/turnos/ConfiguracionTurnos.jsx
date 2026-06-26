import React, { useState } from "react";
import "../../../styles/gestionProd.css";
import Posiciones from "./configuracion/Posiciones";
import PlantillasTurno from "./configuracion/PlantillasTurno";
import Trabajadores from "./configuracion/Trabajadores";

const SUB_PESTANAS = [
  { id: "posiciones",  label: "📍 Posiciones" },
  { id: "plantillas",  label: "🗂️ Plantillas" },
  { id: "trabajadores", label: "👷 Trabajadores" },
];

function ConfiguracionTurnos() {
  const [subPestanaActiva, setSubPestanaActiva] = useState("posiciones");

  return (
    <main className="prod-admin-content">
      <h1 className="titulo-gp">⚙️ Configuración de Turnos</h1>

      {/* Sub-pestañas */}
      <div style={{
        display: "flex",
        borderBottom: "2px solid #444",
        marginBottom: "30px",
        flexWrap: "wrap",
      }}>
        {SUB_PESTANAS.map(p => (
          <button
            key={p.id}
            onClick={() => setSubPestanaActiva(p.id)}
            style={{
              padding: "10px 20px",
              background: "transparent",
              border: "none",
              borderBottom: subPestanaActiva === p.id ? "3px solid #ffcc00" : "3px solid transparent",
              color: subPestanaActiva === p.id ? "#ffcc00" : "#aaa",
              fontWeight: subPestanaActiva === p.id ? "bold" : "normal",
              fontSize: "0.9em",
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

      {/* Contenido */}
      {subPestanaActiva === "posiciones"   && <Posiciones />}
      {subPestanaActiva === "plantillas"   && <PlantillasTurno />}
      {subPestanaActiva === "trabajadores" && <Trabajadores />}
    </main>
  );
}

export default ConfiguracionTurnos;
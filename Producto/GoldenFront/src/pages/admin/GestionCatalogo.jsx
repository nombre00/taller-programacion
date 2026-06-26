import React, { useState } from "react";
import Sidebar from "../../components/Sidebar";
import GestionProductos from "./gestionProductos";
import GestionMateriasPrimas from "./GestionMateriasPrimas";
import GestionRecetas from "./GestionRecetas";
import "../../styles/gestionProd.css";

const PESTANAS = [
  { id: "productos", label: "Productos" },
  { id: "materias-primas", label: "Materias Primas" },
  { id: "recetas", label: "Recetas" },
];

function GestionCatalogo() {
  const [pestanaActiva, setPestanaActiva] = useState("productos");

  const handleAdminLogout = () => {
    console.log("Cerrando sesión del administrador...");
  };

  return (
    <section className="gestion-admin-layout">
      <Sidebar onLogoutAdmin={handleAdminLogout} />

      <div style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        {/* Pestañas */}
        <div style={{
          display: "flex",
          borderBottom: "2px solid #ffcc00",
          backgroundColor: "#1a1a1a",
          padding: "0 20px",
        }}>
          {PESTANAS.map(p => (
            <button
              key={p.id}
              onClick={() => setPestanaActiva(p.id)}
              style={{
                padding: "14px 28px",
                background: "transparent",
                border: "none",
                borderBottom: pestanaActiva === p.id ? "3px solid #ffcc00" : "3px solid transparent",
                color: pestanaActiva === p.id ? "#ffcc00" : "#aaa",
                fontWeight: pestanaActiva === p.id ? "bold" : "normal",
                fontSize: "1em",
                cursor: "pointer",
                transition: "all 0.2s",
                marginBottom: "-2px",
              }}
            >
              {p.label}
            </button>
          ))}
        </div>

        {/* Contenido de la pestaña activa */}
        {pestanaActiva === "productos" && <GestionProductos standalone={false} />}
        {pestanaActiva === "materias-primas" && <GestionMateriasPrimas />}
        {pestanaActiva === "recetas" && <GestionRecetas />}
      </div>
    </section>
  );
}

export default GestionCatalogo;
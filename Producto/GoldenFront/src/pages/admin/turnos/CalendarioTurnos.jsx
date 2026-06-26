// src/pages/admin/turnos/CalendarioTurnos.jsx

import React, { useState, useEffect } from "react";
import { getHorariosPorMes } from "../../../services/calendarioTurnosService.mock";
import "../../../styles/gestionProd.css";

const DIAS_SEMANA = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];

const NOMBRES_MES = [
  "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
  "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
];

// Devuelve el lunes de la semana a la que pertenece la fecha
/* function lunesDe(fecha) {
  const d = new Date(fecha);
  const dia = d.getDay(); // 0=dom, 1=lun...
  const diff = (dia === 0 ? -6 : 1 - dia);
  d.setDate(d.getDate() + diff);
  return d;
} */
function lunesDe(fecha) {
  const d = new Date(fecha);
  d.setHours(12, 0, 0, 0); // Evita problemas de zona horaria

  const dia = d.getDay(); // 0=dom, 1=lun...
  const diff = dia === 0 ? -6 : 1 - dia;
  d.setDate(d.getDate() + diff);
  return d;
}

// Construye la grilla del mes: array de semanas, cada semana es array de 7 fechas (o null si es del mes anterior/siguiente)
function construirGrilla(anio, mes) {
  const primerDia = new Date(anio, mes - 1, 1);
  const ultimoDia = new Date(anio, mes, 0);
  const inicioGrilla = lunesDe(primerDia);

  const semanas = [];
  const cursor = new Date(inicioGrilla);

  while (cursor <= ultimoDia || semanas.length === 0) {
    const semana = [];
    for (let i = 0; i < 7; i++) {
      semana.push(new Date(cursor));
      cursor.setDate(cursor.getDate() + 1);
    }
    semanas.push(semana);
    if (cursor > ultimoDia) break;
  }

  return semanas;
}

// Agrupa horarios por clave "horaInicio|horaTermino", ordenados por horaInicio
function agruparPorFranja(horariosDia) {
  const grupos = {};
  for (const h of horariosDia) {
    const clave = `${h.horaInicio}|${h.horaTermino}`;
    if (!grupos[clave]) grupos[clave] = { horaInicio: h.horaInicio, horaTermino: h.horaTermino, horarios: [] };
    grupos[clave].horarios.push(h);
  }
  return Object.values(grupos).sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
}

// Formatea fecha como "YYYY-MM-DD" sin problemas de zona horaria
function formatFecha(fecha) {
  const y = fecha.getFullYear();
  const m = String(fecha.getMonth() + 1).padStart(2, "0");
  const d = String(fecha.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

// ——————————————————————————————————————————
// Tarjeta individual de un horario
// ——————————————————————————————————————————
function TarjetaHorario({ horario }) {
  const pendiente = horario.estado === "pendiente";
  return (
    <div style={{
      display:       "flex",
      alignItems:    "center",
      gap:           "5px",
      padding:       "3px 6px",
      borderRadius:  "4px",
      backgroundColor: pendiente ? "#2a2a2a" : "#1e1e1e",
      border:        `0.5px solid ${pendiente ? "#555" : "#333"}`,
      marginBottom:  "2px",
    }}>
      <span style={{
        width:        "8px",
        height:       "8px",
        borderRadius: "2px",
        backgroundColor: pendiente ? "#666" : horario.colorPosicion,
        flexShrink:   0,
      }} />
      <span style={{
        fontSize:   "13px",
        color:      pendiente ? "#777" : "#ddd",
        fontStyle:  pendiente ? "italic" : "normal",
        whiteSpace: "nowrap",
        overflow:   "hidden",
        textOverflow: "ellipsis",
        maxWidth:   "100%",
      }}>
        {pendiente ? "Sin asignar" : horario.nombreTrabajador}
      </span>
    </div>
  );
}

// ——————————————————————————————————————————
// Celda de un día
// ——————————————————————————————————————————
function CeldaDia({ fecha, esMesActual, esHoy, horariosDia }) {
  const franjas = agruparPorFranja(horariosDia);

  return (
    <div style={{
      backgroundColor: esMesActual ? "#1e1e1e" : "#161616",
      minHeight:       "100px",
      padding:         "6px",
      display:         "flex",
      flexDirection:   "column",
      gap:             "4px",
    }}>
      {/* Número del día */}
      <div style={{ display: "flex", justifyContent: "flex-end" }}>
        <span style={{
          fontSize:        "16px",
          fontWeight:      esHoy ? "bold" : "normal",
          color:           esHoy ? "#1a1a1a" : esMesActual ? "#aaa" : "#444",
          backgroundColor: esHoy ? "#ffcc00" : "transparent",
          borderRadius:    "50%",
          width:           "20px",
          height:          "20px",
          display:         "flex",
          alignItems:      "center",
          justifyContent:  "center",
        }}>
          {fecha.getDate()}
        </span>
      </div>

      {/* Franjas horarias */}
      {esMesActual && franjas.map(franja => (
        <div key={`${franja.horaInicio}-${franja.horaTermino}`}>
          {/* Encabezado de franja */}
          <div style={{
            fontSize:     "14px",
            color:        "#666",
            marginBottom: "2px",
            paddingLeft:  "2px",
          }}>
            {franja.horaInicio} – {franja.horaTermino}
          </div>
          {/* Tarjetas */}
          {franja.horarios.map(h => (
            <TarjetaHorario key={h.idHorario} horario={h} />
          ))}
        </div>
      ))}
    </div>
  );
}

// ——————————————————————————————————————————
// Componente principal
// ——————————————————————————————————————————
function CalendarioTurnos() {
  const hoy = new Date();
  const [anio, setAnio] = useState(hoy.getFullYear());
  const [mes,  setMes]  = useState(hoy.getMonth() + 1);
  const [horariosPorFecha, setHorariosPorFecha] = useState({});
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    setCargando(true);
    getHorariosPorMes(anio, mes)
      .then(lista => {
        const mapa = {};
        for (const h of lista) {
          if (!mapa[h.fechaTrabajo]) mapa[h.fechaTrabajo] = [];
          mapa[h.fechaTrabajo].push(h);
        }
        setHorariosPorFecha(mapa);
      })
      .finally(() => setCargando(false));
  }, [anio, mes]);

  const irMesAnterior = () => {
    if (mes === 1) { setAnio(a => a - 1); setMes(12); }
    else           { setMes(m => m - 1); }
  };

  const irMesSiguiente = () => {
    if (mes === 12) { setAnio(a => a + 1); setMes(1); }
    else            { setMes(m => m + 1); }
  };

  const grilla = construirGrilla(anio, mes);
  const hoyStr = formatFecha(hoy);

  return (
    <main className="prod-admin-content">
      <h1 className="titulo-gp">📅 Calendario de Turnos</h1>

      {/* Navegación de mes */}
      <div style={{
        display:        "flex",
        alignItems:     "center",
        justifyContent: "center",
        gap:            "20px",
        marginBottom:   "24px",
      }}>
        <button onClick={irMesAnterior} style={estiloBotonNav}>‹</button>
        <span style={{ fontSize: "1.1em", fontWeight: "bold", color: "#ffcc00", minWidth: "180px", textAlign: "center" }}>
          {NOMBRES_MES[mes - 1]} {anio}
        </span>
        <button onClick={irMesSiguiente} style={estiloBotonNav}>›</button>
      </div>

      {cargando ? (
        <p style={{ color: "#888", textAlign: "center" }}>Cargando horarios...</p>
      ) : (
        <div style={{
          border:       "1px solid #333",
          borderRadius: "8px",
          overflow:     "hidden",
        }}>
          {/* Encabezados de días */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(7, minmax(0, 1fr))" }}>
            {DIAS_SEMANA.map(d => (
              <div key={d} style={{
                backgroundColor: "#2d2d2d",
                padding:         "10px 0",
                textAlign:       "center",
                fontSize:        "12px",
                fontWeight:      "500",
                color:           "#aaa",
                borderBottom:    "1px solid #333",
              }}>
                {d}
              </div>
            ))}
          </div>

          {/* Semanas */}
          {grilla.map((semana, si) => (
            <div key={si} style={{
              display:             "grid",
              gridTemplateColumns: "repeat(7, minmax(0, 1fr))",
              borderTop:           si === 0 ? "none" : "1px solid #333",
            }}>
              {semana.map((fecha, di) => {
                const fechaStr    = formatFecha(fecha);
                const esMesActual = fecha.getMonth() + 1 === mes && fecha.getFullYear() === anio;
                const esHoy       = fechaStr === hoyStr;
                const horariosDia = horariosPorFecha[fechaStr] || [];

                return (
                  <div key={di} style={{ borderLeft: di === 0 ? "none" : "1px solid #333" }}>
                    <CeldaDia
                      fecha={fecha}
                      esMesActual={esMesActual}
                      esHoy={esHoy}
                      horariosDia={horariosDia}
                    />
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      )}

      {/* Leyenda de estados */}
      <div style={{ display: "flex", gap: "16px", marginTop: "16px", flexWrap: "wrap", alignItems: "center" }}>
        <span style={{ fontSize: "12px", color: "#666" }}>Referencias:</span>
        <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <span style={{ width: "8px", height: "8px", borderRadius: "2px", backgroundColor: "#4CAF50", display: "inline-block" }} />
          <span style={{ fontSize: "12px", color: "#aaa" }}>Cajero</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <span style={{ width: "8px", height: "8px", borderRadius: "2px", backgroundColor: "#FF9800", display: "inline-block" }} />
          <span style={{ fontSize: "12px", color: "#aaa" }}>Cocinero</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <span style={{ width: "8px", height: "8px", borderRadius: "2px", backgroundColor: "#2196F3", display: "inline-block" }} />
          <span style={{ fontSize: "12px", color: "#aaa" }}>Delivery</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <span style={{ width: "8px", height: "8px", borderRadius: "2px", backgroundColor: "#666", display: "inline-block" }} />
          <span style={{ fontSize: "12px", color: "#aaa" }}>Sin asignar</span>
        </div>
      </div>
    </main>
  );
}

const estiloBotonNav = {
  background:   "transparent",
  border:       "1px solid #444",
  color:        "#ffcc00",
  borderRadius: "6px",
  padding:      "6px 14px",
  fontSize:     "18px",
  cursor:       "pointer",
};

export default CalendarioTurnos;
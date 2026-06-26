import React, { useEffect, useRef, useState } from "react";
import Sidebar from "../../components/Sidebar";
import Chart from "chart.js/auto";
import ChartDataLabels from "chartjs-plugin-datalabels";

import {
  getResumenVentas,
  getVentasMesActual,
  getVentasAnioActual,
  getVentasPorMes,
  getVentasPorCategoria,
  getVentasPorCiudad,
} from "../../services/dashboardService";

export default function Dashboard() {
  Chart.register(ChartDataLabels);

  // Canvas refs
  const ventasMesRef = useRef(null);
  const ventasCategoriaRef = useRef(null);
  const ventasCiudadRef = useRef(null);

  // Chart instances
  const chartsRef = useRef({});

  // Estado
  const [resumen, setResumen] = useState(null);
  const [ventasMesActual, setVentasMesActual] = useState(null);
  const [ventasAnioActual, setVentasAnioActual] = useState(null);

  const [ventasPorMes, setVentasPorMes] = useState([]);
  const [ventasPorCategoria, setVentasPorCategoria] = useState([]);
  const [ventasPorCiudad, setVentasPorCiudad] = useState([]);

  const [periodo, setPeriodo] = useState(() => {
    const ahora = new Date();
    return `${ahora.getFullYear()}${String(ahora.getMonth() + 1).padStart(2, "0")}`;
  });

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarDatos();
  }, [periodo]);

  const cargarDatos = async () => {
    try {
      setCargando(true);
      setError(null);

      const [
        resumenData,
        ventasMesData,
        categoriasData,
        ciudadesData
      ] = await Promise.all([
        getResumenVentas(),
        getVentasPorMes(),
        getVentasPorCategoria(periodo),
        getVentasPorCiudad(periodo),
      ]);

      setResumen(resumenData);
      setVentasPorMes(ventasMesData);
      setVentasPorCategoria(categoriasData);
      setVentasPorCiudad(ciudadesData);

      setVentasMesActual(resumenData?.ventasMesActual || null);
      setVentasAnioActual(resumenData?.ventasAnioActual || null);

      renderCharts(ventasMesData, categoriasData, ciudadesData);

    } catch (err) {
      console.error("❌ Error cargando dashboard:", err);
      setError("Error cargando datos del backend");
    } finally {
      setCargando(false);
    }
  };

  const limpiarGraficos = () => {
    Object.values(chartsRef.current).forEach(chart => chart?.destroy());
    chartsRef.current = {};
  };

  const renderCharts = (ventasMesData, categoriasData, ciudadesData) => {
    limpiarGraficos();

    // === GRÁFICO BARRAS: VENTAS POR MES ===
    if (ventasMesRef.current && ventasMesData.length > 0) {
      chartsRef.current.ventasMes = new Chart(ventasMesRef.current, {
        type: "bar",
        data: {
          labels: ventasMesData.map(v => v.NOMBRE_MES.trim()),
          datasets: [
            {
              label: "Ventas ($)",
              data: ventasMesData.map(v => v.TOTAL_VENTAS),
              backgroundColor: "#f39c12",
            }
          ]
        },
        options: {
          responsive: true,
          plugins: {
            datalabels: {
              color: "#000",
              anchor: "end",
              align: "top",
              formatter: (value) => "$" + value.toLocaleString("es-CL")
            },
          }
        }
      });
    }

    // === PIE: VENTAS POR CATEGORÍA (AJUSTADO) ===
    if (ventasCategoriaRef.current && categoriasData.length > 0) {
      chartsRef.current.categorias = new Chart(ventasCategoriaRef.current, {
        type: "pie",
        data: {
          labels: categoriasData.map(c => c.CATEGORIA), // 🔥 NOMBRE DE CATEGORÍAS
          datasets: [
            {
              data: categoriasData.map(c => c.TOTAL_VENTAS),
              backgroundColor: ["#2980b9", "#f1c40f", "#8e44ad"]
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                color: "#000",     // 🔥 Texto negro (mejor contraste con fondo claro)
                font: { size: 14 }
              }
            },
            datalabels: {
              color: "#000",       // 🔥 Etiquetas dentro del gráfico
              formatter: (value, ctx) => {
                const total = ctx.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
                const porcentaje = ((value / total) * 100).toFixed(1);
                return `${porcentaje}%`;
              },
              font: { weight: "bold", size: 12 }
            }
          }
        }
      });
    }


    // === BARRAS HORIZONTALES: VENTAS POR CIUDAD ===
    if (ventasCiudadRef.current && ciudadesData.length > 0) {
      chartsRef.current.ciudades = new Chart(ventasCiudadRef.current, {
        type: "bar",
        data: {
          labels: ciudadesData.map(c => c.CIUDAD),
          datasets: [
            {
              label: "Ventas ($)",
              data: ciudadesData.map(c => c.TOTAL_VENTAS),
              backgroundColor: "#3498db"
            }
          ]
        },
        options: {
          indexAxis: "y"
        }
      });
    }
  };

  return (
    <div className="d-flex dashboard-container">
      <Sidebar adminName="Administrador" />
      <main className="flex-fill p-4">
        <h1 className="fw-bold text-light">Dashboard de Ventas</h1>

        {/* === KPIs (SOLO 3) === */}
        <div className="row my-4">
          <div className="col-md-4">
            <div className="card p-3 text-center">
              <h6>📅 Ventas Mes</h6>
              <p className="fs-4">
                ${ventasMesActual?.VENTA_MES_ACTUAL?.toLocaleString("es-CL") || 0}
              </p>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card p-3 text-center">
              <h6>📆 Ventas Año</h6>
              <p className="fs-4">
                ${ventasAnioActual?.VENTA_ANIO_ACTUAL?.toLocaleString("es-CL") || 0}
              </p>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card p-3 text-center">
              <h6>🔥 Ventas Hoy</h6>
              <p className="fs-4">
                ${resumen?.ventasHoy?.VENTA_HOY?.toLocaleString("es-CL") || 0}
              </p>
            </div>
          </div>
        </div>

        {/* === GRÁFICOS === */}
        <div className="row g-4 mt-3">

          {/* === CARD: VENTAS POR MES === */}
          <div className="col-md-6">
            <div className="card shadow-sm p-3" style={{ background: "#f8f9fa" }}>
              <h5 className="text-center mb-3 text-dark">📊 Ventas por Mes</h5>
              <div style={{ width: "100%", height: "320px" }}>
                <canvas ref={ventasMesRef}></canvas>
              </div>
            </div>
          </div>

          {/* === CARD: VENTAS POR CIUDAD === */}
          <div className="col-md-6">
            <div className="card shadow-sm p-3" style={{ background: "#f8f9fa" }}>
              <h5 className="text-center mb-3 text-dark">🏙️ Ventas por Ciudad</h5>
              <div style={{ width: "100%", height: "320px" }}>
                <canvas ref={ventasCiudadRef}></canvas>
              </div>
            </div>
          </div>

          {/* === CARD: VENTAS POR CATEGORÍA === */}
          <div className="col-md-6">
            <div className="card shadow-sm p-3" style={{ background: "#f8f9fa" }}>
              <h5 className="text-center mb-3 text-dark">🏷️ Ventas por Categoría</h5>
              <div style={{ width: "100%", height: "320px" }}>
                <canvas ref={ventasCategoriaRef}></canvas>
              </div>
            </div>
          </div>

        </div>

      </main>
    </div>
  );
}

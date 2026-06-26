import api from "../config/api";

/**
    * Servicio de gestión del dashboard
 */



// Resumen completo de ventas (hoy, mes, año)
export const getResumenVentas = async () => {
  try {
    const response = await api.get("/dashboard/resumen-ventas");
    return response.data;
  } catch (error) {
    console.error("Error al obtener resumen de ventas:", error);
    throw error;
  }
};

// Ventas de hoy
export const getVentasHoy = async () => {
  try {
    const response = await api.get("/dashboard/ventas-hoy");
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas de hoy:", error);
    throw error;
  }
};

// Ventas del mes actual
export const getVentasMesActual = async () => {
  try {
    const response = await api.get("/dashboard/ventas-mes-actual");
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas del mes actual:", error);
    throw error;
  }
};

// Ventas del año actual
export const getVentasAnioActual = async () => {
  try {
    const response = await api.get("/dashboard/ventas-anio-actual");
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas del año actual:", error);
    throw error;
  }
};

// Ventas agrupadas por mes (para gráficos)
export const getVentasPorMes = async () => {
  try {
    const response = await api.get("/dashboard/ventas-por-mes");
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas por mes:", error);
    throw error;
  }
};

// --- ENDPOINTS ORIGINALES ---

// KPIs según periodo (hoy, mes, anio)
export const getKpis = async (periodo) => {
  try {
    const response = await api.get(`/dashboard/kpis`, {
      params: { periodo },
    });
    return response.data;
  } catch (error) {
    console.error("Error al obtener KPIs:", error);
    throw error;
  }
};

// Ventas por categoría según periodo
export const getVentasPorCategoria = async (periodo) => {
  try {
    const response = await api.get(`/dashboard/ventas-categoria`, {
      params: { periodo },
    });
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas por categoría:", error);
    throw error;
  }
};

// Ventas por ciudad según periodo
export const getVentasPorCiudad = async (periodo) => {
  try {
    const response = await api.get(`/dashboard/ventas-ciudad`, {
      params: { periodo },
    });
    return response.data;
  } catch (error) {
    console.error("Error al obtener ventas por ciudad:", error);
    throw error;
  }
};


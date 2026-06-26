import api from "../config/api";


//   LISTAR TODAS LAS BOLETAS

export const getBoletas = async () => {
  try {
    const response = await api.get("/boletas");
    return response.data;
  } catch (error) {
    console.error("Error al obtener boletas:", error);
    throw error;
  }
};


   //OBTENER BOLETA POR ID

export const getBoletaById = async (id) => {
  try {
    const response = await api.get(`/boletas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener boleta ${id}:`, error);
    throw error;
  }
};


   //CREAR NUEVA BOLETA

export const crearBoleta = async (boleta) => {
  try {
    const response = await api.post("/boletas", boleta);
    return response.data;
  } catch (error) {
    console.error("Error al crear boleta:", error);
    throw error.response?.data || error;
  }
};

//   ACTUALIZAR BOLETA EXISTENTE

export const actualizarBoleta = async (id, boleta) => {
  try {
    const response = await api.put(`/boletas/${id}`, boleta);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar boleta ${id}:`, error);
    throw error.response?.data || error;
  }
};

//   ELIMINAR BOLETA

export const eliminarBoleta = async (id) => {
  try {
    const response = await api.delete(`/boletas/${id}`);
    return response.status === 204;
  } catch (error) {
    console.error(`Error al eliminar boleta ${id}:`, error);
    throw error.response?.data || error;
  }
};

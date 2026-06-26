import axios from "axios";// axios es una librería para hacer peticiones HTTP

// Configuración de URLs según el ambiente
const API_URLS = {
  // En desarrollo, usa directamente la VM
  development: "http://localhost:9090/api", //puerto de la apigateway del backend
  // development: "http://161.153.219.128:8080/api",

  // En producción, apunta directamente a la VM del backend
  production: "http://161.153.219.128:8080/api"
};

// Detectar el ambiente actual
const isDevelopment = import.meta.env.DEV;
const baseURL = isDevelopment ? API_URLS.development : API_URLS.production;
// Para desarrollo dejamos la url como desarrollo.
// const baseURL = "http://localhost:9090/api"

console.log(`Ambiente: ${isDevelopment ? 'Desarrollo' : 'Producción'}`);
console.log(` API Base URL: ${baseURL}`);

// Crear instancia de Axios con configuración base
const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json"
  },
  timeout: 30000, // 30 segundos
  withCredentials: false // Cambiar a true si usas cookies
});

// Interceptor para agregar el token JWT en cada request para rutas protegidas, valida el rol del usuario segun la autenticación
api.interceptors.request.use(
  (config) => {
    // Obtener token del localStorage
    const token = localStorage.getItem("authToken");

    if (token) {
      // Agregar token en el header Authorization (estándar)
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Si el body es FormData, eliminar el Content-Type para que el navegador lo configure automáticamente
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }

    // Log en desarrollo
    if (isDevelopment) {
      console.log(` ${config.method.toUpperCase()} ${config.url}`);
    }

    return config;
  },
  (error) => {
    console.error(" Error en request:", error);
    return Promise.reject(error);
  }
);

// Interceptor para manejo de respuestas y errores
api.interceptors.response.use(
  (response) => {
    // Log en desarrollo
    if (isDevelopment) {
      console.log(` ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error) => {
    // Manejo de errores comunes
    if (error.response) {
      // El servidor respondió con un código de error
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // Token inválido o expirado
          console.error("No autorizado - Token inválido");
          localStorage.removeItem("authToken");
          localStorage.removeItem("user");
          // Redirigir al login
          if (!window.location.pathname.includes("/login")) {
            window.location.href = "/login";
          }
          break;

        case 403:
          console.error(" Acceso prohibido");
          alert("No tienes permisos para realizar esta acción");
          break;

        case 404:
          console.error(" Recurso no encontrado");
          break;

        case 500:
          console.error(" Error del servidor");
          alert("Error en el servidor. Por favor, intenta más tarde.");
          break;

        default:
          console.error(` Error ${status}:`, data);
      }
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta
      console.error(" No se pudo conectar con el servidor");
      alert("No se pudo conectar con el servidor. Verifica tu conexión.");
    } else {
      // Algo pasó al configurar la petición
      console.error(" Error:", error.message);
    }

    return Promise.reject(error);
  }
);

export default api;
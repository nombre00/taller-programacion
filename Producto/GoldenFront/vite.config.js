import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: "./",

  // Configuración del servidor de desarrollo
  server: {
    port: 5173,

    // PROXY - Redirige /api/* al backend
    
    proxy: {
      // ESPECÍFICO: Solo para subida de imágenes → directo al microservicio (evita problema API Gateway)
      '^/api/catalogo/productos/.*/imagen$': {
        target: 'http://161.153.219.128:8084', // Directo a GESTIONCATALOGO
        changeOrigin: true,
        secure: false,
        ws: true,
      },

      '/api': {
        // En desarrollo local: backend en localhost
       // target: 'http://localhost:8080',

        // Para testing contra la VM, comentar línea anterior y descomentar la siguiente:
         target: 'http://161.153.219.128:8080',

        changeOrigin: true,
        secure: false,
        ws: true,
        rewrite: (path) => {
          console.log('Original path:', path);
          // No cambiar la ruta, dejar /api/... como está
          return path;
        },

        // Log de las peticiones (útil para debug)
        configure: (proxy,) => {
          proxy.on('error', (err, ) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req,) => {
            console.log('Sending Request:', req.method, req.url);
            console.log('Headers:', proxyReq.getHeaders());
          });
          proxy.on('proxyRes', (proxyRes, req, ) => {
            console.log('Received Response:', proxyRes.statusCode, req.url);
          });
        },
      },
    },
  },

  // Configuración de testing
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./tests/setup.js",
    coverage: {
      provider: "v8",
      reporter: ["text", "html"],
    },
  },
});

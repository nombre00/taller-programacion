// Importaciones principales necesarias para las pruebas
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";

import { render, screen, fireEvent } from "@testing-library/react"; // Permite renderizar el componente y simular acciones del usuario
import { BrowserRouter } from "react-router-dom"; // Simula el contexto de enrutamiento
import NuevoCliente from "../src/pages/admin/nuevoCliente"; // Componente que se va a probar

// MOCK DEL SIDEBAR
// Se crea un componente falso para evitar renderizar la interfaz completa
vi.mock("../components/Sidebar", () => ({
  default: () => <div>Sidebar mock</div>,
}));

// MOCK DE NAVEGACIÓN (useNavigate y useSearchParams)
// Se reemplazan los hooks de React Router para controlar la navegación y parámetros de URL
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate, // Simula la navegación
    useSearchParams: () => [new URLSearchParams("")], // Simula no tener parámetros de búsqueda por defecto
  };
});

// Configuración general antes y después de cada prueba
beforeEach(() => localStorage.clear()); // Limpia el almacenamiento local antes de cada test
afterEach(() => localStorage.clear()); // Limpia después también, por seguridad

// CONJUNTO PRINCIPAL DE PRUEBAS
describe("NuevoCliente.jsx con validaciones visuales", () => {

  // Prueba 1: Renderizado inicial del formulario
  // Verifica que los campos "Nombre" y "Correo" estén presentes en el DOM
  it("debería renderizar campos de nombre y correo", () => {
    render(
      <BrowserRouter>
        <NuevoCliente />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/correo/i)).toBeInTheDocument();
  });

  // Prueba 2: Validaciones visuales (mensajes de error)
  // Simula el envío del formulario vacío y comprueba que se muestran los mensajes de error en pantalla
  it("debería mostrar mensajes de error en rojo si se intenta enviar vacío", () => {
    render(
      <BrowserRouter>
        <NuevoCliente />
      </BrowserRouter>
    );

    fireEvent.click(screen.getByRole("button", { name: /crear cliente/i }));

    expect(screen.getByText(/debe ingresar un nombre/i)).toBeInTheDocument();
    expect(screen.getByText(/debe ingresar un correo/i)).toBeInTheDocument();
  });

  // Prueba 3: Creación de un nuevo cliente válido
  // Simula la introducción de datos válidos, clic en "Crear Cliente", y verifica que:
  // - El cliente se guarda en localStorage
  // - Se muestra un mensaje de éxito
  // - Redirige correctamente al panel de gestión
  it("debería crear un nuevo cliente si los campos son válidos", () => {
    const alertMock = vi.spyOn(window, "alert").mockImplementation(() => {}); // Evita que se muestre el alert real
    render(
      <BrowserRouter>
        <NuevoCliente />
      </BrowserRouter>
    );

    // Simula ingreso de datos válidos
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: "Pedro Soto" },
    });
    fireEvent.change(screen.getByLabelText(/correo/i), {
      target: { value: "pedro@test.com" },
    });

    // Simula clic en el botón "Crear Cliente"
    fireEvent.click(screen.getByRole("button", { name: /crear cliente/i }));

    // Validaciones posteriores
    const clientes = JSON.parse(localStorage.getItem("clientes"));
    expect(clientes).toHaveLength(1);
    expect(clientes[0].nombre).toBe("Pedro Soto");
    expect(alertMock).toHaveBeenCalledWith("Cliente creado correctamente.");
    expect(mockNavigate).toHaveBeenCalledWith("/admin/gestion-usuarios");

    alertMock.mockRestore(); // Restaura el comportamiento original del alert
  });

  // Prueba 4: Modo edición de cliente
  // Verifica que si la página se carga con parámetros (edit=0), se cargan los datos existentes
  // y que se pueden actualizar correctamente
  it("debería cargar datos en modo edición y actualizarlos correctamente", async () => {
    vi.resetModules(); // Limpia los mocks para reconfigurar
    const clienteOriginal = [
      { nombre: "Ana López", correo: "ana@test.com" },
    ];
    localStorage.setItem("clientes", JSON.stringify(clienteOriginal)); // Simula un cliente guardado previamente

    // Mock actualizado: simula que la URL contiene el parámetro "edit=0"
    vi.doMock("react-router-dom", async () => {
      const actual = await vi.importActual("react-router-dom");
      return {
        ...actual,
        useNavigate: () => mockNavigate,
        useSearchParams: () => [new URLSearchParams("edit=0")],
      };
    });

    // Importa nuevamente el componente ya con el mock activo
    const { default: NuevoClienteEdit } = await import("../src/pages/admin/nuevoCliente");
    const alertMock = vi.spyOn(window, "alert").mockImplementation(() => {});

    render(
      <BrowserRouter>
        <NuevoClienteEdit />
      </BrowserRouter>
    );

    // Comprueba que los campos cargan los valores previos
    expect(await screen.findByDisplayValue("Ana López")).toBeInTheDocument();
    expect(screen.getByDisplayValue("ana@test.com")).toBeInTheDocument();

    // Simula una modificación del nombre
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: "Ana María López" },
    });
    fireEvent.click(screen.getByRole("button", { name: /guardar cambios/i }));

    // Verifica que el cliente fue actualizado correctamente en el localStorage
    const clientesActualizados = JSON.parse(localStorage.getItem("clientes"));
    expect(clientesActualizados[0].nombre).toBe("Ana María López");
    expect(alertMock).toHaveBeenCalledWith("Cliente actualizado correctamente.");
    expect(mockNavigate).toHaveBeenCalledWith("/admin/gestion-usuarios");

    alertMock.mockRestore(); // Restaura el alert original
  });
});

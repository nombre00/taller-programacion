// Importaciones necesarias para las pruebas unitarias
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";

import { render, screen, fireEvent } from "@testing-library/react"; // Permite renderizar el componente y simular interacciones
import { BrowserRouter } from "react-router-dom"; // Crea un entorno de navegación simulado
import NuevoUsuario from "../src/pages/admin/nuevoUsuario"; // Componente a probar

// MOCK DEL SIDEBAR
// Se reemplaza el componente Sidebar real por una versión mínima de prueba
// Esto evita dependencias visuales o errores por estilos externos
vi.mock("../components/Sidebar", () => ({
  default: () => <div>Sidebar mock</div>,
}));

// MOCK DE NAVEGACIÓN Y PARÁMETROS
// Se simula el comportamiento del hook useNavigate para controlar la redirección
// También se define useSearchParams, que por defecto no contiene parámetros (modo "nuevo")
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useSearchParams: () => [new URLSearchParams("")],
  };
});

// Configuración general
// Se limpia el localStorage antes y después de cada prueba
beforeEach(() => localStorage.clear());
afterEach(() => localStorage.clear());

// CONJUNTO PRINCIPAL DE PRUEBAS
describe("NuevoUsuario.jsx con validaciones y edición", () => {

  // Prueba 1: Verifica que el formulario tenga todos los campos necesarios
  it("debería renderizar todos los campos del formulario", () => {
    render(
      <BrowserRouter>
        <NuevoUsuario />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/RUN/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Nombre/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Apellidos/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Correo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Rol/i)).toBeInTheDocument();
  });

  // Prueba 2: Validaciones visuales al intentar enviar el formulario vacío
  // Verifica que se muestren mensajes de error específicos por cada campo obligatorio
  it("debería mostrar mensajes de error en rojo cuando se intenta enviar vacío", () => {
    render(
      <BrowserRouter>
        <NuevoUsuario />
      </BrowserRouter>
    );

    // Simula el clic en el botón de envío sin rellenar los campos
    const boton = screen.getByRole("button", { name: /crear usuario|guardar cambios/i });
    fireEvent.click(boton);

    // Comprueba que todos los mensajes de error aparecen
    expect(screen.getByText("Debe ingresar un RUN.")).toBeInTheDocument();
    expect(screen.getByText("Debe ingresar un nombre.")).toBeInTheDocument();
    expect(screen.getByText("Debe ingresar los apellidos.")).toBeInTheDocument();
    expect(screen.getByText("Debe ingresar un correo.")).toBeInTheDocument();
    expect(screen.getByText("Debe seleccionar un rol.")).toBeInTheDocument();
  });

  // Prueba 3: Creación de un nuevo usuario válido
  // Simula el llenado correcto del formulario y valida que:
  // - Se guarde en localStorage
  // - Se muestre el alert de éxito
  // - Se realice la redirección esperada
  it("debería guardar correctamente un nuevo usuario si los campos son válidos", () => {
    const alertMock = vi.spyOn(window, "alert").mockImplementation(() => {}); // Desactiva el alert real
    render(
      <BrowserRouter>
        <NuevoUsuario />
      </BrowserRouter>
    );

    // Simula ingreso de datos válidos
    fireEvent.change(screen.getByLabelText(/RUN/i), { target: { value: "11111111-1" } });
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: "Pedro" } });
    fireEvent.change(screen.getByLabelText(/Apellidos/i), { target: { value: "Soto" } });
    fireEvent.change(screen.getByLabelText(/Correo/i), { target: { value: "pedro@test.com" } });
    fireEvent.change(screen.getByLabelText(/Rol/i), { target: { value: "Admin" } });

    // Simula clic en el botón "Crear Usuario"
    fireEvent.click(screen.getByRole("button", { name: /crear usuario/i }));

    // Verifica que se haya guardado correctamente en localStorage
    const usuariosGuardados = JSON.parse(localStorage.getItem("usuarios"));
    expect(usuariosGuardados).toHaveLength(1);
    expect(usuariosGuardados[0].nombre).toBe("Pedro");

    // Verifica alert y navegación
    expect(alertMock).toHaveBeenCalledWith("Usuario creado correctamente.");
    expect(mockNavigate).toHaveBeenCalledWith("/admin/gestion-usuarios");

    alertMock.mockRestore(); // Restaura el alert original
  });

  // Prueba 4: Edición de un usuario existente
  // Carga un usuario simulado en localStorage y verifica que:
  // - Los campos se llenen con los datos existentes
  // - Se puedan editar
  // - Se actualice el localStorage correctamente
  it("debería cargar los datos en modo edición y actualizar correctamente", async () => {
    vi.resetModules(); // Limpia los mocks previos para redefinirlos

    // Crea un usuario simulado preexistente
    const usuarioOriginal = [
      { run: "22222222-2", nombre: "Ana", apellidos: "López", email: "ana@test.com", rol: "Cajero" },
    ];
    localStorage.setItem("usuarios", JSON.stringify(usuarioOriginal));

    // Mock de router con parámetro "edit=0" (modo edición)
    vi.doMock("react-router-dom", async () => {
      const actual = await vi.importActual("react-router-dom");
      return {
        ...actual,
        useNavigate: () => mockNavigate,
        useSearchParams: () => [new URLSearchParams("edit=0")],
      };
    });

    // Importa nuevamente el componente con el mock aplicado
    const { default: NuevoUsuarioEdit } = await import("../src/pages/admin/nuevoUsuario");
    const alertMock = vi.spyOn(window, "alert").mockImplementation(() => {});

    render(
      <BrowserRouter>
        <NuevoUsuarioEdit />
      </BrowserRouter>
    );

    // Verifica que los campos se cargan correctamente
    expect(await screen.findByDisplayValue("Ana")).toBeInTheDocument();
    expect(screen.getByDisplayValue("López")).toBeInTheDocument();
    expect(screen.getByDisplayValue("ana@test.com")).toBeInTheDocument();

    // Simula cambio de nombre y guarda
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: "Ana María" } });
    fireEvent.click(screen.getByRole("button", { name: /guardar cambios/i }));

    // Verifica actualización en localStorage
    const usuariosActualizados = JSON.parse(localStorage.getItem("usuarios"));
    expect(usuariosActualizados[0].nombre).toBe("Ana María");

    // Comprueba que se mostró el mensaje y redirigió correctamente
    expect(alertMock).toHaveBeenCalledWith("Usuario actualizado correctamente.");
    expect(mockNavigate).toHaveBeenCalledWith("/admin/gestion-usuarios");

    alertMock.mockRestore(); // Restaura alert original
  });
});

import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import CheckOut from "../src/pages/client/CheckOut";

// Mock de useNavigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Helper para renderizar con Router
const renderWithRouter = (component) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("Componente CheckOut", () => {
  beforeEach(() => {
    // Mock de localStorage con un carrito válido
    const carritoMock = [
      { id: 1, nombre: "Pizza Margherita", precio: 8990, cantidad: 2 },
      { id: 2, nombre: "Pizza Pepperoni", precio: 9990, cantidad: 1 },
    ];
    localStorage.setItem("carrito", JSON.stringify(carritoMock));
    mockNavigate.mockClear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("renderiza el título correctamente", () => {
    renderWithRouter(<CheckOut />);
    expect(screen.getByText("Finalizar Compra")).toBeInTheDocument();
  });

  it("cambia el precio de delivery al seleccionar Retiro en tienda", () => {
    renderWithRouter(<CheckOut />);
    
    // Verificar que inicialmente muestra el precio de delivery
    expect(screen.getByText("$2.500")).toBeInTheDocument();
    
    // Hacer clic en la opción de "Retiro en tienda"
    const retiroRadio = screen.getByRole("radio", { name: /retiro en tienda/i });
    fireEvent.click(retiroRadio);
    
    // Verificar que ahora el delivery dice "Gratis" - usar getAllByText porque aparece múltiples veces
    const textoGratis = screen.getAllByText("Gratis");
    expect(textoGratis.length).toBeGreaterThan(0);
  });
});
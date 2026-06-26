import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, beforeEach, vi } from "vitest";
import CatalogoPag from "../src/pages/client/CatalogoPag";
import { BrowserRouter } from "react-router-dom";

// Mock del servicio de productos
vi.mock("../src/services/productosService", () => ({
  obtenerProductosDisponibles: vi.fn(() => Promise.resolve([
    {
      id: 1,
      nombre: "Combo Clásico",
      categoria: "Combos",
      precio: 5990,
      descripcion: "Hamburguesa + papas + bebida",
      imagen: "https://example.com/combo.jpg",
      disponible: true
    },
    {
      id: 2,
      nombre: "Burger Especial",
      categoria: "Burgers",
      precio: 4990,
      descripcion: "Hamburguesa con queso",
      imagen: "https://example.com/burger.jpg",
      disponible: true
    }
  ]))
}));

// Función auxiliar para renderizar el componente con Router
const renderCatalogo = () => {
  render(
    <BrowserRouter>
      <CatalogoPag />
    </BrowserRouter>
  );
};

describe("Componente Catálogo", () => {
  beforeEach(() => {
    // Mockear scrollIntoView para evitar errores en pruebas
    Element.prototype.scrollIntoView = vi.fn();
    // Limpiar localStorage antes de cada test
    localStorage.clear();
  });

  it("renderiza el título de categoría Combos", async () => {
    renderCatalogo();
    // Esperar a que los productos se carguen
    await waitFor(() => {
      const tituloCombos = screen.getByRole('heading', { name: /Combos/i, level: 2 });
      expect(tituloCombos).toBeInTheDocument();
    });
  });

  it("muestra el botón de agregar al carrito", async () => {
    renderCatalogo();
    // Esperar a que los productos se carguen
    await waitFor(() => {
      const botonesAgregar = screen.getAllByTitle("Agregar al carrito");
      expect(botonesAgregar.length).toBeGreaterThan(0);
    });
  });

  it("hace scroll a la categoría al hacer click en el botón de categoría", async () => {
    renderCatalogo();
    // Esperar a que los productos se carguen
    await waitFor(() => {
      const botonCategoria = screen.getByRole('button', { name: /Burgers/i });
      expect(botonCategoria).toBeInTheDocument();
    });

    const botonCategoria = screen.getByRole('button', { name: /Burgers/i });
    fireEvent.click(botonCategoria);

    // Verificar que la sección existe
    const seccionBurgers = document.getElementById('burgers');
    expect(seccionBurgers).toBeInTheDocument();
  });
});
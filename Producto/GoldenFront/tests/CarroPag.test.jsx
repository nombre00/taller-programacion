import React from "react";
import { render, screen } from "@testing-library/react"; //se importa en el archivo setupTests.jsx
import { describe, it, expect, beforeEach, vi } from "vitest"; // se usa vitest como framework de pruebas unitarias
import CarroPag from "../src/pages/client/CarroPag";
import { BrowserRouter } from "react-router-dom";

//Se crea un mock para simular la navegacion
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => { //se usa un vi.mock para simular el módulo react-router-dom
  const actual = await vi.importActual("react-router-dom"); //Se importa la implementación real del módulo
  return { //se retorna un objeto con todas las exportaciones reales y se sobreescribe useNavigate
    ...actual, // actual contiene todas las exportaciones reales de react-router-dom
    useNavigate: () => mockNavigate, //te lleva a la función mockNavigate cuando se llame useNavigate
  };
});

// Mock de un producto en el carrito
// se crea un objeto que representa un producto
//  en el carrito de compras, con sus atributos,
//  igual que en carroPag.jsx
const mockProducto = {
  id: "1-Simple",
  nombre: "Golden",
  precio: 7990,
  cantidad: 1,
  size: "Simple",
  imagen: "/src/assets/img/Golden.PNG",
  descripcion: "Hamburguesa Golden"
};

// sirve para renderizar el componente CarroPag con un producto en el carrito
const renderCarrito = () => {
  // Simular un producto en el carrito
  localStorage.setItem('carrito', JSON.stringify([mockProducto]));
  render( //se renderiza el componente CarroPag dentro de un BrowserRouter
    <BrowserRouter> 
      <CarroPag />
    </BrowserRouter>
  );
};

describe("Componente Carrito", () => { 
  beforeEach(() => {
    localStorage.clear();
    mockNavigate.mockClear();
  });

  it("muestra el producto agregado en el carrito", () => {
    renderCarrito();
    expect(screen.getByText("Golden")).toBeInTheDocument();
  });

  it("muestra el texto 'Subtotal:' en el resumen", () => {
    renderCarrito();
    expect(screen.getByText("Subtotal:")).toBeInTheDocument();
  });
});
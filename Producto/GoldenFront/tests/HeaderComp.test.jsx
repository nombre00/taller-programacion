import React from 'react';
import { render, screen, cleanup, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import HeaderComp from '../src/components/HeaderComp';
// 1. Mock de useNavigate (necesario porque el componente lo usa)
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// 2. Función auxiliar para renderizar el componente
// Usamos BrowserRouter porque HeaderComp usa Link y NavLink
const renderHeader = () => {
  render(
    <BrowserRouter>
      <HeaderComp />
    </BrowserRouter>
  );
};

// 3. Conjunto de pruebas para HeaderComp
describe('Componente HeaderComp', () => {

  // Limpiar localStorage y mocks antes de CADA prueba
  beforeEach(() => {
    localStorage.clear();
    mockNavigate.mockClear();
  });

  // Limpiar el DOM después de CADA prueba
  afterEach(() => {
    cleanup();
  });

  // --- Pruebas de Autenticación (Estado del Usuario) ---

  it("debería mostrar el botón 'Iniciar sesión' si el usuario no está logueado", () => {
    // localStorage está limpio (gracias a beforeEach)
    renderHeader();

    // Buscamos el botón por su texto
    expect(screen.getByRole('button', { name: /iniciar sesión/i })).toBeInTheDocument();
  });

  it("debería mostrar 'Hola, [nombre]' si el usuario está logueado", () => {
    // 1. Simulamos el estado de "logueado" en localStorage
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('userName', 'Fabián');

    // 2. Renderizamos el componente
    renderHeader();

    // 3. Verificamos que muestre el saludo y no el botón de login
    expect(screen.getByText('Hola, Fabián')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /iniciar sesión/i })).not.toBeInTheDocument();
  });

  // --- Pruebas del Contador del Carrito ---

  it("no debería mostrar el contador del carrito si el carrito está vacío", () => {
    localStorage.setItem('carrito', '[]'); // Carrito vacío
    renderHeader();

    // Buscamos un número. `queryBy` no da error si no lo encuentra.
    // Usamos una expresión regular (/\d+/) para buscar cualquier número.
    expect(screen.queryByText(/\d+/)).not.toBeInTheDocument();
  });

  it("debería mostrar el contador correcto si el carrito tiene productos", () => {
    // 1. Simulamos un carrito con 5 productos (3 + 2)
    const mockCarrito = [
      { id: 1, cantidad: 3 },
      { id: 2, cantidad: 2 }
    ];
    localStorage.setItem('carrito', JSON.stringify(mockCarrito));

    // 2. Renderizamos
    renderHeader();

    // 3. Verificamos que el número '5' esté en el documento
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it("debería actualizar el contador cuando se dispara un evento 'storage'", async () => {
    // 1. Renderizamos con el carrito vacío
    localStorage.setItem('carrito', '[]');
    renderHeader();

    // Verificamos que no hay contador
    expect(screen.queryByText(/\d+/)).not.toBeInTheDocument();

    // 2. Simulamos que OTRA PÁGINA (ej. Catalogo) añade algo al carrito
    const mockCarritoNuevo = [{ id: 1, cantidad: 3 }];
    localStorage.setItem('carrito', JSON.stringify(mockCarritoNuevo));
    
    // 3. Disparamos el evento 'storage' manualmente
    fireEvent(window, new Event('storage'));

    // 4. Esperamos a que React re-renderice el componente (es asíncrono)
    await waitFor(() => {
      // 5. Verificamos que el nuevo contador '3' haya aparecido
      expect(screen.getByText('3')).toBeInTheDocument();
    });
  });
});
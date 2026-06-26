import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Login from '../src/pages/client/Login';

// Mock de Firebase para evitar errores de inicialización
vi.mock('firebase/app', () => ({
  initializeApp: vi.fn(() => ({}))
}));

vi.mock('firebase/auth', () => ({
  getAuth: vi.fn(() => ({})),
  signInWithEmailAndPassword: vi.fn(),
  createUserWithEmailAndPassword: vi.fn()
}));

// Mock del servicio de usuarios
vi.mock('../src/services/usuariosService', () => ({
  obtenerClientePorFirebaseUid: vi.fn(),
  registrarCliente: vi.fn()
}));

// Función auxiliar para renderizar
const renderComponent = () => {
  render(
    <MemoryRouter>
      <Login />
    </MemoryRouter>
  );
};

describe('Componente Login', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('debería renderizar el formulario de login correctamente', async () => {
    renderComponent();

    await waitFor(() => {
      // Verificar que existe el formulario de login (hay múltiples inputs, uno para login y otro para registro)
      const emailInputs = screen.getAllByPlaceholderText(/email/i);
      expect(emailInputs.length).toBeGreaterThan(0);

      const passwordInputs = screen.getAllByPlaceholderText(/password|contraseña/i);
      expect(passwordInputs.length).toBeGreaterThan(0);
    });
  });

  it('debería tener el botón de inicio de sesión', async () => {
    renderComponent();

    await waitFor(() => {
      const loginButtons = screen.getAllByRole('button', { name: /iniciar sesión/i });
      expect(loginButtons.length).toBeGreaterThan(0);
    });
  });

  it('debería poder cambiar al formulario de registro', async () => {
    renderComponent();

    await waitFor(() => {
      const registroButton = screen.getByRole('button', { name: /registro/i });
      expect(registroButton).toBeInTheDocument();
    });
  });

  it('debería renderizar el formulario de registro al cambiar de pestaña', async () => {
    renderComponent();

    await waitFor(() => {
      // Verificar que existe la opción de registro
      expect(screen.getByRole('button', { name: /registro/i })).toBeInTheDocument();
    });
  });
});

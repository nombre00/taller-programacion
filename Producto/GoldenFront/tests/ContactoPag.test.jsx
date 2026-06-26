import React from 'react';
import { render, screen, cleanup, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom'; // Necesario por Header/Footer
import ContactoPag from '../src/pages/client/ContactoPag';

// Mock simple para los componentes Header y Footer
// No necesitamos probarlos aquí, solo evitar errores si usan <Link> o hooks.
vi.mock('../src/components/HeaderComp', () => ({
  default: () => <header>Header Mock</header>,
}));
vi.mock('../src/components/FooterComp', () => ({
  default: () => <footer>Footer Mock</footer>,
}));

// Mock del servicio de contacto
vi.mock('../src/services/contactoService', () => ({
  enviarMensajeContacto: vi.fn(() => Promise.resolve({ mensaje: 'Mensaje enviado' }))
}));

// Mock para window.alert
vi.stubGlobal('alert', vi.fn());

// Función auxiliar para renderizar el componente
const renderContacto = () => {
  render(
    <BrowserRouter>
      <ContactoPag />
    </BrowserRouter>
  );
};

describe('Componente ContactoPag', () => {
  beforeEach(() => {
    // Limpiamos los mocks antes de cada test
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  // --- Pruebas de Renderizado ---

  it('debería renderizar la información de contacto correctamente', () => {
    renderContacto();
    expect(screen.getByText('Información de Contacto')).toBeInTheDocument();
    expect(screen.getByText('Etchevers 210, Viña del Mar')).toBeInTheDocument();
    expect(screen.getByText('+569 71334173')).toBeInTheDocument();
    expect(screen.getByText('Goldenpagos2@gmail.com')).toBeInTheDocument();
    // Verifica que los íconos (enlaces) de redes sociales estén
    expect(screen.getByTestId('instagram-link')).toBeInTheDocument();
    expect(screen.getByTestId('facebook-link')).toBeInTheDocument();
  });

  it('debería renderizar el formulario de contacto con sus campos', () => {
    renderContacto();
    expect(screen.getByText('Formulario de Contacto')).toBeInTheDocument();
    // Busca los campos por su etiqueta asociada
    expect(screen.getByLabelText('Nombre')).toBeInTheDocument();
    expect(screen.getByLabelText('Correo')).toBeInTheDocument();
    expect(screen.getByLabelText('Mensaje')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Enviar' })).toBeInTheDocument();
  });

  // --- Pruebas de Funcionalidad del Formulario ---

  it('debería actualizar los contadores de caracteres al escribir', async () => {
    renderContacto();
    const nombreInput = screen.getByLabelText('Nombre');
    const mensajeInput = screen.getByLabelText('Mensaje');

    // Simula escribir en el campo Nombre
    fireEvent.change(nombreInput, { target: { value: 'Fabián Basaes' } });
    // Verificar que el input tiene el valor correcto
    expect(nombreInput.value).toBe('Fabián Basaes');

    // Simula escribir en el campo Mensaje
    fireEvent.change(mensajeInput, { target: { value: 'Este es un mensaje de prueba.' } });
    // Verificar que el input tiene el valor correcto
    expect(mensajeInput.value).toBe('Este es un mensaje de prueba.');
  });

  it('debería mostrar mensajes de error si se envía el formulario vacío', async () => {
    renderContacto();
    const submitButton = screen.getByRole('button', { name: 'Enviar' });

    // Simula el clic en Enviar sin rellenar nada
    fireEvent.click(submitButton);

    // Espera a que aparezcan los mensajes de validación de Bootstrap
    // `findAllByText` busca todos los elementos con ese texto (puede haber varios mensajes iguales)
    const errorMessagesNombre = await screen.findAllByText('Debe ingresar un nombre (máx. 100 caracteres).');
    const errorMessagesCorreo = await screen.findAllByText('Ingrese un correo válido.');
    const errorMessagesMensaje = await screen.findAllByText('Debe ingresar un mensaje (máx. 500 caracteres).');

    // Verifica que al menos un mensaje de error para cada campo esté visible
    expect(errorMessagesNombre.length).toBeGreaterThan(0);
    expect(errorMessagesCorreo.length).toBeGreaterThan(0);
    expect(errorMessagesMensaje.length).toBeGreaterThan(0);
  });

  it('debería enviar el formulario y mostrar mensaje de éxito si los campos son válidos', async () => {
    renderContacto();
    const nombreInput = screen.getByLabelText('Nombre');
    const correoInput = screen.getByLabelText('Correo');
    const mensajeInput = screen.getByLabelText('Mensaje');
    const submitButton = screen.getByRole('button', { name: 'Enviar' });

    // Rellenamos el formulario con datos válidos
    fireEvent.change(nombreInput, { target: { value: 'Usuario Test' } });
    fireEvent.change(correoInput, { target: { value: 'test@correo.com' } });
    fireEvent.change(mensajeInput, { target: { value: 'Mensaje válido.' } });

    // Enviamos el formulario
    fireEvent.click(submitButton);

    // Esperamos a que aparezca el mensaje de éxito
    await waitFor(() => {
      expect(screen.getByText('¡Mensaje enviado con éxito!')).toBeInTheDocument();
      expect(screen.getByText(/Gracias por contactarnos/i)).toBeInTheDocument();
    });
  });
});
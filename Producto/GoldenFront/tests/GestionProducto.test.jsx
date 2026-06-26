import React from 'react';
import { render, screen, waitFor, fireEvent, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';

// Importaciones
import GestionProductos from '../src/pages/admin/gestionProductos';

// Mock del Sidebar
vi.mock('../src/components/Sidebar', () => ({
    default: () => <div data-testid="mocked-sidebar">Sidebar</div>,
}));

// Mock del servicio de productos
const mockProductos = [
    {
        id: 1,
        nombre: 'Hamburguesa Clásica',
        idCategoria: 1,
        precio: 5990,
        descripcion: 'Deliciosa hamburguesa',
        imagen: 'https://example.com/burger.jpg',
        disponible: true
    },
    {
        id: 2,
        nombre: 'Coca Cola',
        idCategoria: 2,
        precio: 1500,
        descripcion: 'Bebida refrescante',
        imagen: null,
        disponible: true
    }
];

vi.mock('../src/services/productosService', () => ({
    obtenerTodosProductos: vi.fn(() => Promise.resolve(mockProductos)),
    crearProducto: vi.fn((data) => Promise.resolve({ id: 3, ...data })),
    actualizarProducto: vi.fn((id, data) => Promise.resolve({ id, ...data })),
    eliminarProducto: vi.fn((id) => Promise.resolve({ mensaje: 'Eliminado' })),
    subirImagenProducto: vi.fn(() => Promise.resolve({ imageUrl: 'https://example.com/new.jpg' }))
}));

// Mock de funciones globales
beforeEach(() => {
    localStorage.clear();
    window.confirm = vi.fn(() => true);
    window.alert = vi.fn();
    window.URL.createObjectURL = vi.fn(() => 'mock-image-url');
});

describe('Componente GestionProductos', () => {

    it('debería renderizar el título correctamente', async () => {
        render(<GestionProductos />);
        await waitFor(() => {
            expect(screen.getByText('Gestión de Productos')).toBeInTheDocument();
        });
    });

    it('debería renderizar todos los inputs con sus labels', async () => {
        render(<GestionProductos />);

        // Esperar a que el componente cargue
        await waitFor(() => {
            expect(screen.getByLabelText('Nombre del Producto')).toBeInTheDocument();
        });

        // Verificar que los inputs existen usando labels
        expect(screen.getByLabelText('Nombre del Producto')).toBeInTheDocument();
        expect(screen.getByLabelText('Categoría')).toBeInTheDocument();
        expect(screen.getByLabelText('Precio')).toBeInTheDocument();
        expect(screen.getByLabelText('Descripción')).toBeInTheDocument();
        expect(screen.getByLabelText('Disponibilidad')).toBeInTheDocument();

        // Verificar que los inputs tienen los atributos correctos
        const inputNombre = screen.getByLabelText('Nombre del Producto');
        expect(inputNombre).toHaveAttribute('type', 'text');
        expect(inputNombre).toHaveAttribute('name', 'nombre');

        const inputPrecio = screen.getByLabelText('Precio');
        expect(inputPrecio).toHaveAttribute('type', 'text');
    });

    it('debería renderizar el botón "Agregar Producto"', async () => {
        render(<GestionProductos />);

        await waitFor(() => {
            const botonAgregar = screen.getByDisplayValue(/Agregar Producto/i);
            expect(botonAgregar).toBeInTheDocument();
            expect(botonAgregar).toHaveAttribute('type', 'submit');
        });
    });

    it('debería permitir seleccionar una imagen', async () => {
        render(<GestionProductos />);

        await waitFor(() => {
            expect(screen.getByLabelText('Imagen del Producto')).toBeInTheDocument();
        });

        const inputImagen = screen.getByLabelText('Imagen del Producto');
        expect(inputImagen).toHaveAttribute('type', 'file');
        expect(inputImagen).toHaveAttribute('accept', 'image/*');
    });

    it('debería renderizar botones "Eliminar" para cada producto', async () => {
        render(<GestionProductos />);

        await waitFor(() => {
            const botonesEliminar = screen.getAllByRole('button', { name: /Eliminar/i });
            expect(botonesEliminar.length).toBeGreaterThan(0);
        });
    });

    it('debería mostrar confirmación al eliminar un producto', async () => {
        render(<GestionProductos />);

        await waitFor(() => {
            expect(screen.getAllByRole('button', { name: /Eliminar/i }).length).toBeGreaterThan(0);
        });

        const botonesEliminar = screen.getAllByRole('button', { name: /Eliminar/i });
        fireEvent.click(botonesEliminar[0]);

        expect(window.confirm).toHaveBeenCalledWith('¿Estás seguro de eliminar este producto?');
    });

    it('debería renderizar la tabla con las columnas correctas', async () => {
        render(<GestionProductos />);

        await waitFor(() => {
            const tabla = screen.getByRole('table');
            const headers = within(tabla).getAllByRole('columnheader');

            const textosHeaders = headers.map(header => header.textContent);
            expect(textosHeaders).toContain('Nombre');
            expect(textosHeaders).toContain('Categoría'); // Con acento
            expect(textosHeaders).toContain('Precio');
            expect(textosHeaders).toContain('Descripción'); // Con acento
            expect(textosHeaders).toContain('Disponible'); // No "Stock"
            expect(textosHeaders).toContain('Imagen');
            expect(textosHeaders).toContain('Acciones');
        });
    });

    it('debería renderizar el sidebar', () => {
        render(<GestionProductos />);
        expect(screen.getByTestId('mocked-sidebar')).toBeInTheDocument();
    });
});
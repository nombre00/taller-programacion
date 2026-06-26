import { describe, it, expect, afterEach, vi, beforeEach } from "vitest";
import { render, screen, cleanup, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Dashboard from "../src/pages/admin/dashboard";

// Mock del Sidebar
vi.mock('../src/components/Sidebar', () => ({
  default: () => <div data-testid="sidebar">Sidebar</div>,
}));

// Mock de los servicios del dashboard
const mockData = {
  resumen: {
    ventasHoy: { VENTA_HOY: 50000 },
    ventasMesActual: { VENTA_MES_ACTUAL: 1000000 },
    ventasAnioActual: { VENTA_ANIO_ACTUAL: 10000000 }
  },
  ventasMes: [
    { MES: 'Enero', TOTAL_VENTA: 500000 },
    { MES: 'Febrero', TOTAL_VENTA: 600000 }
  ],
  ventasCategoria: [
    { CATEGORIA: 'Hamburguesas', TOTAL_VENTA: 300000 },
    { CATEGORIA: 'Bebidas', TOTAL_VENTA: 200000 }
  ],
  ventasCiudad: [
    { CIUDAD: 'Santiago', TOTAL_VENTA: 400000 },
    { CIUDAD: 'Valparaíso', TOTAL_VENTA: 300000 }
  ]
};

vi.mock('../src/services/dashboardService', () => ({
  getResumenVentas: vi.fn(() => Promise.resolve(mockData.resumen)),
  getVentasMesActual: vi.fn(() => Promise.resolve(mockData.resumen.ventasMesActual)),
  getVentasAnioActual: vi.fn(() => Promise.resolve(mockData.resumen.ventasAnioActual)),
  getVentasPorMes: vi.fn(() => Promise.resolve(mockData.ventasMes)),
  getVentasPorCategoria: vi.fn(() => Promise.resolve(mockData.ventasCategoria)),
  getVentasPorCiudad: vi.fn(() => Promise.resolve(mockData.ventasCiudad))
}));

// Mock de Chart.js
vi.mock('chart.js/auto', () => {
  const ChartMock = vi.fn(() => ({
    destroy: vi.fn(),
    update: vi.fn()
  }));
  ChartMock.register = vi.fn();

  return {
    default: ChartMock
  };
});

vi.mock('chartjs-plugin-datalabels', () => ({
  default: {}
}));

describe("Dashboard Admin", () => {
  beforeEach(() => {
    // Mock de HTMLCanvasElement.getContext para evitar errores con Chart.js
    HTMLCanvasElement.prototype.getContext = vi.fn(() => ({
      fillStyle: '',
      fillRect: vi.fn(),
      clearRect: vi.fn(),
      getImageData: vi.fn(),
      putImageData: vi.fn(),
      createImageData: vi.fn(),
      setTransform: vi.fn(),
      drawImage: vi.fn(),
      save: vi.fn(),
      restore: vi.fn(),
      beginPath: vi.fn(),
      moveTo: vi.fn(),
      lineTo: vi.fn(),
      closePath: vi.fn(),
      stroke: vi.fn(),
      translate: vi.fn(),
      scale: vi.fn(),
      rotate: vi.fn(),
      arc: vi.fn(),
      fill: vi.fn(),
      measureText: vi.fn(() => ({ width: 0 })),
      transform: vi.fn(),
      rect: vi.fn(),
      clip: vi.fn(),
    }));
  });

  afterEach(() => cleanup());

  it("debería renderizar el título principal correctamente", async () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole("heading", { level: 1, name: /Dashboard de Ventas/i })).toBeInTheDocument();
    });
  });

  it("debería mostrar las tarjetas de KPIs con sus títulos", async () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Ventas Mes/i)).toBeInTheDocument();
      expect(screen.getByText(/Ventas Año/i)).toBeInTheDocument();
      expect(screen.getByText(/Ventas Hoy/i)).toBeInTheDocument();
    });
  });

  it("debería renderizar los tres gráficos principales", async () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Ventas por Mes/i)).toBeInTheDocument();
      expect(screen.getByText(/Ventas por Ciudad/i)).toBeInTheDocument();
      expect(screen.getByText(/Ventas por Categoría/i)).toBeInTheDocument();
    });
  });

  it("debería incluir el Sidebar en el layout", () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    expect(screen.getByTestId('sidebar')).toBeInTheDocument();
  });

  it("debería crear y limpiar correctamente los gráficos (sin errores)", async () => {
    const { unmount } = render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Dashboard de Ventas/i)).toBeInTheDocument();
    });

    // No debería haber errores al desmontar el componente
    expect(() => unmount()).not.toThrow();
  });
});

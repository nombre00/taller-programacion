import '@testing-library/jest-dom'
import { vi } from 'vitest';

// Le damos a 'window' una versión falsa de esta API
const ResizeObserverMock = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Asigna el mock al objeto 'window' global de jsdom
vi.stubGlobal('ResizeObserver', ResizeObserverMock);

// ==========================================
// Componente: NuevoCliente.jsx
// Descripción: Página para crear o editar un cliente
// en el sistema. Utiliza localStorage como base de datos
// y permite la validación de campos antes de guardar.
// ==========================================

import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Sidebar from "../../components/Sidebar";

// --- Funciones locales para manipular los clientes en localStorage ---

// Lee la lista de clientes desde localStorage (o retorna un arreglo vacío si no hay datos)
const readClientes = () => JSON.parse(localStorage.getItem("clientes")) || [];

// Crea un nuevo cliente agregándolo al array existente y guardándolo en localStorage
const createCliente = (cliente) => {
  const clientes = readClientes();
  clientes.push(cliente);
  localStorage.setItem("clientes", JSON.stringify(clientes));
};

// Actualiza un cliente existente en la posición indicada
const updateCliente = (index, cliente) => {
  const clientes = readClientes();
  clientes[index] = cliente;
  localStorage.setItem("clientes", JSON.stringify(clientes));
};

// ================================================================
// Componente principal: NuevoCliente
// ================================================================
export default function NuevoCliente() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const editIndex = searchParams.get("edit");

  // Estado que representa los datos del cliente actual
  const [cliente, setCliente] = useState({
    nombre: "",
    correo: "",
  });

   // Estado que guarda mensajes de error por campo
  const [errores, setErrores] = useState({ nombre: "", correo: "" });

  // --- useEffect: Si el componente se usa en modo "edición" ---
  // Carga los datos del cliente correspondiente desde localStorage
  useEffect(() => {
    if (editIndex !== null) {
      const clientes = readClientes();
      if (clientes[editIndex]) setCliente(clientes[editIndex]);
    }
  }, [editIndex]);

  // --- Manejo de cambios en los inputs del formulario ---
  const handleChange = (e) => {
    const { name, value } = e.target;

    // Actualiza el valor del campo modificado
    setCliente({ ...cliente, [name]: value });

    // Limpia el error del campo al comenzar a escribir
    setErrores({ ...errores, [name]: "" }); 
  };

  // --- Validación y guardado del cliente ---
  const handleSubmit = (e) => {
    e.preventDefault();// Previene el envío por defecto del formulario
    const nuevosErrores = { nombre: "", correo: "" };
    let valido = true;

    // Validación del campo nombre
    if (!cliente.nombre.trim()) {
      nuevosErrores.nombre = "Debe ingresar un nombre";
      valido = false;
    }
    // Validación del campo correo
    if (!cliente.correo.trim()) {
      nuevosErrores.correo = "Debe ingresar un correo";
      valido = false;
    }

    // Actualiza los errores en caso de haber
    setErrores(nuevosErrores);

    // Si hay errores, se detiene la ejecución
    if (!valido) return;

    // Si se está editando un cliente existente
    if (editIndex !== null) {
      updateCliente(editIndex, cliente);
      alert("Cliente actualizado correctamente.");
    } else {
      // Si es un nuevo cliente, se crea
      createCliente(cliente);
      alert("Cliente creado correctamente.");
    }

    // Redirige al panel principal de gestión
    navigate("/admin/gestion-usuarios");
  };

  // ================================================================
  // Renderizado del componente (estructura visual)
  // ================================================================
  return (
    <div className="admin-layout">
      <Sidebar
        adminName="Administrador"
        onLogoutAdmin={() => alert("Cerrando sesión")}
      />

      <main className="admin-content">
        <h1 className="mb-4">
          {editIndex !== null ? "Editar Cliente" : "Nuevo Cliente"}
        </h1>

        <form className="card p-4 shadow-sm" onSubmit={handleSubmit}>
          {/* Nombre */}
          <div className="mb-3">
            <label htmlFor="nombre" className="form-label">
              Nombre
            </label>
            <input
              type="text"
              id="nombre"
              name="nombre"
              className="form-control"
              value={cliente.nombre}
              onChange={handleChange}
            />
            {errores.nombre && (
              <p className="text-danger mt-1" role="alert">
                {errores.nombre}
              </p>
            )}
          </div>

          {/* Correo */}
          <div className="mb-3">
            <label htmlFor="correo" className="form-label">
              Correo
            </label>
            <input
              type="email"
              id="correo"
              name="correo"
              className="form-control"
              value={cliente.correo}
              onChange={handleChange}
            />
            {errores.correo && (
              <p className="text-danger mt-1" role="alert">
                {errores.correo}
              </p>
            )}
          </div>

          <button type="submit" className="btn btn-success">
            {editIndex !== null ? "Guardar Cambios" : "Crear Cliente"}
          </button>
        </form>
      </main>
    </div>
  );
}

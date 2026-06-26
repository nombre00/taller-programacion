// ==========================================
// Componente: NuevoUsuario.jsx
// Descripción: Vista para crear o editar un usuario (trabajador)
// dentro del sistema administrativo. Usa localStorage para guardar
// la información y permite validación de campos antes de guardar.
// ==========================================
import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Sidebar from "../../components/Sidebar";

// --- Funciones locales para manejo de usuarios en localStorage ---

// Obtiene los usuarios almacenados o retorna un arreglo vacío si no hay datos
const readUsuarios = () => JSON.parse(localStorage.getItem("usuarios")) || [];

// Crea un nuevo usuario agregándolo al array existente
const createUsuario = (usuario) => {
  const usuarios = readUsuarios();
  usuarios.push(usuario);
  localStorage.setItem("usuarios", JSON.stringify(usuarios));
};

// Actualiza un usuario existente en base a su índice
const updateUsuario = (index, usuario) => {
  const usuarios = readUsuarios();
  usuarios[index] = usuario;
  localStorage.setItem("usuarios", JSON.stringify(usuarios));
};

// ================================================================
// Componente principal: NuevoUsuario
// ================================================================
export default function NuevoUsuario() {
  const navigate = useNavigate();// Hook de navegación entre rutas
  const [searchParams] = useSearchParams();// Hook para leer parámetros de la URL
  const editIndex = searchParams.get("edit");// Si existe, indica que se está editando un usuario

  // Estado principal del usuario que se crea o edita
  const [usuario, setUsuario] = useState({
    run: "",
    nombre: "",
    apellidos: "",
    email: "",
    rol: "",
  });

  // Estado que almacena los errores de validación campo a campo
  const [errores, setErrores] = useState({});

  // --- useEffect: Cargar datos si el componente se usa para edición ---
  useEffect(() => {
    if (editIndex !== null) {
      const usuarios = readUsuarios();
      if (usuarios[editIndex]) setUsuario(usuarios[editIndex]);
    }
  }, [editIndex]);

  // --- Manejo de cambios en los inputs ---
  // Actualiza el estado del usuario conforme se escribe
  const handleChange = (e) => {
    const { name, value } = e.target;
    setUsuario({ ...usuario, [name]: value });
    // Limpia los errores del campo que se está editando
    setErrores({ ...errores, [name]: "" });
  };

  // --- Validación de todos los campos obligatorios ---
  const validarCampos = () => {
    const nuevosErrores = {};

    if (!usuario.run.trim()) nuevosErrores.run = "Debe ingresar un RUN.";
    if (!usuario.nombre.trim()) nuevosErrores.nombre = "Debe ingresar un nombre.";
    if (!usuario.apellidos.trim()) nuevosErrores.apellidos = "Debe ingresar los apellidos.";
    if (!usuario.email.trim()) nuevosErrores.email = "Debe ingresar un correo.";
    if (!usuario.rol.trim()) nuevosErrores.rol = "Debe seleccionar un rol.";

    setErrores(nuevosErrores);

    // Devuelve true si no hay errores
    return Object.keys(nuevosErrores).length === 0;
  };

  // --- Manejo del envío del formulario ---
  const handleSubmit = (e) => {
    e.preventDefault();// Previene el comportamiento por defecto del formulario

    // Si hay errores, se detiene la ejecución
    if (!validarCampos()) return;

    // Si hay índice de edición, se actualiza el usuario existente
    if (editIndex !== null) {
      updateUsuario(editIndex, usuario);
      alert("Usuario actualizado correctamente.");
    } else {
      createUsuario(usuario);
      alert("Usuario creado correctamente.");
    }

    navigate("/admin/gestion-usuarios");
  };

  // ================================================================
  // Renderizado del componente (estructura visual)
  // ================================================================
  return (
    <div className="admin-layout">
      <Sidebar adminName="Administrador" onLogoutAdmin={() => alert("Cerrando sesión")} />

      <main className="admin-content">
        <h1 className="mb-4">{editIndex !== null ? "Editar Usuario" : "Nuevo Usuario"}</h1>

        <form className="card p-4 shadow-sm" onSubmit={handleSubmit}>
          {/* RUN */}
          <div className="mb-3">
            <label htmlFor="run" className="form-label">RUN</label>
            <input
              id="run"
              type="text"
              name="run"
              className="form-control"
              value={usuario.run}
              onChange={handleChange}
            />
            {errores.run && <p className="text-danger">{errores.run}</p>}
          </div>

          {/* Nombre */}
          <div className="mb-3">
            <label htmlFor="nombre" className="form-label">Nombre</label>
            <input
              id="nombre"
              type="text"
              name="nombre"
              className="form-control"
              value={usuario.nombre}
              onChange={handleChange}
            />
            {errores.nombre && <p className="text-danger">{errores.nombre}</p>}
          </div>

          {/* Apellidos */}
          <div className="mb-3">
            <label htmlFor="apellidos" className="form-label">Apellidos</label>
            <input
              id="apellidos"
              type="text"
              name="apellidos"
              className="form-control"
              value={usuario.apellidos}
              onChange={handleChange}
            />
            {errores.apellidos && <p className="text-danger">{errores.apellidos}</p>}
          </div>

          {/* Correo */}
          <div className="mb-3">
            <label htmlFor="email" className="form-label">Correo</label>
            <input
              id="email"
              type="email"
              name="email"
              className="form-control"
              value={usuario.email}
              onChange={handleChange}
            />
            {errores.email && <p className="text-danger">{errores.email}</p>}
          </div>

          {/* Rol */}
          <div className="mb-3">
            <label htmlFor="rol" className="form-label">Rol</label>
            <select
              id="rol"
              name="rol"
              className="form-select"
              value={usuario.rol}
              onChange={handleChange}
            >
              <option value="">Seleccione un rol</option>
              <option value="Admin">Admin</option>
              <option value="Cajero">Cajero</option>
              <option value="Cocinero">Cocinero</option>
              <option value="Despacho">Despacho</option>
            </select>
            {errores.rol && <p className="text-danger">{errores.rol}</p>}
          </div>

          <button type="submit" className="btn btn-success">
            {editIndex !== null ? "Guardar Cambios" : "Crear Usuario"}
          </button>
        </form>
      </main>
    </div>
  );
}

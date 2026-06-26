import React, { useState, useEffect } from "react";
import * as recetasService from "../../services/recetasService.mock";
import * as mpService from "../../services/materiaPrimaService.mock";
import * as productosService from "../../services/productosService";
import "../../styles/gestionProd.css";

function GestionRecetas() {
  const [recetas, setRecetas] = useState([]);
  const [productos, setProductos] = useState([]);
  const [materiasPrimas, setMateriasPrimas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editando, setEditando] = useState(null);

  const [formData, setFormData] = useState({
    idProducto: "",
    descripcion: "",
  });

  // Lista dinámica de ingredientes
  const [detalles, setDetalles] = useState([
    { idMateriaPrima: "", cantidad: "", unidadMedida: "" }
  ]);

  useEffect(() => {
    cargarTodo();
  }, []);

  const cargarTodo = async () => {
    try {
      setLoading(true);
      const [r, p, mp] = await Promise.all([
        recetasService.obtenerTodasRecetas(),
        productosService.obtenerTodosProductos(),
        mpService.obtenerTodasMateriasPrimas(),
      ]);
      setRecetas(r);
      setProductos(p);
      setMateriasPrimas(mp.filter(m => m.activo));
    } catch (error) {
      alert("Error al cargar datos: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // Manejo de filas de ingredientes
  const handleDetalleChange = (index, field, value) => {
    setDetalles(prev => prev.map((d, i) =>
      i === index ? { ...d, [field]: value } : d
    ));
  };

  const agregarDetalle = () => {
    setDetalles(prev => [...prev, { idMateriaPrima: "", cantidad: "", unidadMedida: "" }]);
  };

  const quitarDetalle = (index) => {
    if (detalles.length === 1) return; // Al menos un ingrediente
    setDetalles(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validar que todos los detalles estén completos
    const detallesValidos = detalles.every(d =>
      d.idMateriaPrima && d.cantidad && d.unidadMedida
    );
    if (!detallesValidos) {
      alert("Completa todos los campos de los ingredientes");
      return;
    }

    try {
      setLoading(true);
      const payload = {
        idProducto: parseInt(formData.idProducto),
        descripcion: formData.descripcion,
        detalles: detalles.map(d => ({
          idMateriaPrima: parseInt(d.idMateriaPrima),
          cantidad: parseFloat(d.cantidad),
          unidadMedida: d.unidadMedida,
        })),
      };

      if (editando) {
        await recetasService.actualizarReceta(editando.idReceta, payload);
        alert("Receta actualizada exitosamente");
      } else {
        await recetasService.crearReceta(payload);
        alert("Receta creada exitosamente");
      }

      handleCancelar();
      await cargarTodo();
    } catch (error) {
      alert("Error al guardar: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleEditar = (receta) => {
    setEditando(receta);
    setFormData({
      idProducto: receta.idProducto?.toString() || "",
      descripcion: receta.descripcion || "",
    });
    setDetalles(
      receta.detalles?.length > 0
        ? receta.detalles.map(d => ({
            idMateriaPrima: d.idMateriaPrima?.toString() || "",
            cantidad: d.cantidad?.toString() || "",
            unidadMedida: d.unidadMedida || "",
          }))
        : [{ idMateriaPrima: "", cantidad: "", unidadMedida: "" }]
    );
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDesactivar = async (id) => {
    if (!window.confirm("¿Desactivar esta receta?")) return;
    try {
      setLoading(true);
      await recetasService.desactivarReceta(id);
      await cargarTodo();
      alert("Receta desactivada");
    } catch (error) {
      alert("Error al desactivar: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCancelar = () => {
    setEditando(null);
    setFormData({ idProducto: "", descripcion: "" });
    setDetalles([{ idMateriaPrima: "", cantidad: "", unidadMedida: "" }]);
  };

  const nombreProducto = (id) => {
    const p = productos.find(p => (p.idProducto || p.id) === id);
    return p ? (p.nombreProducto || p.nombre) : `Producto ${id}`;
  };

  return (
    <main className="prod-admin-content">
      {/* Formulario */}
      <div className="form-container-producto">
        <form className="form-producto" onSubmit={handleSubmit}>
          <h1 className="titulo-gp">
            {editando ? "Editar Receta" : "Nueva Receta"}
          </h1>
          {editando && (
            <p style={{ color: "#ffc107", marginBottom: "15px" }}>
              <strong>Editando receta de:</strong> {nombreProducto(editando.idProducto)}
            </p>
          )}

          <label htmlFor="idProducto">Producto asociado</label>
          <select
            id="idProducto" name="idProducto"
            value={formData.idProducto} onChange={handleChange}
            required disabled={!!editando}
          >
            <option value="">Seleccionar producto</option>
            {productos.map(p => (
              <option key={p.idProducto || p.id} value={p.idProducto || p.id}>
                {p.nombreProducto || p.nombre}
              </option>
            ))}
          </select>
          {editando && (
            <small style={{ color: "#aaa" }}>El producto no se puede cambiar al editar</small>
          )}

          <label htmlFor="descripcion">Descripción</label>
          <textarea
            id="descripcion" name="descripcion"
            placeholder="Descripción opcional de la receta"
            value={formData.descripcion} onChange={handleChange}
            rows="2"
          />

          {/* Ingredientes dinámicos */}
          <label style={{ marginTop: "15px" }}>Ingredientes</label>
          {detalles.map((detalle, index) => (
            <div key={index} style={{
              display: "flex", gap: "8px", alignItems: "center",
              marginBottom: "8px", flexWrap: "wrap"
            }}>
              <select
                value={detalle.idMateriaPrima}
                onChange={e => handleDetalleChange(index, "idMateriaPrima", e.target.value)}
                required style={{ flex: "2", minWidth: "140px" }}
              >
                <option value="">Materia prima</option>
                {materiasPrimas.map(mp => (
                  <option key={mp.idMateriaPrima} value={mp.idMateriaPrima}>
                    {mp.nombre} ({mp.unidadMedida})
                  </option>
                ))}
              </select>

              <input
                type="number" step="0.001" min="0.001"
                placeholder="Cantidad"
                value={detalle.cantidad}
                onChange={e => handleDetalleChange(index, "cantidad", e.target.value)}
                required style={{ flex: "1", minWidth: "80px" }}
              />

              <select
                value={detalle.unidadMedida}
                onChange={e => handleDetalleChange(index, "unidadMedida", e.target.value)}
                required style={{ flex: "1", minWidth: "80px" }}
              >
                <option value="">Unidad</option>
                <option value="kg">kg</option>
                <option value="g">g</option>
                <option value="l">l</option>
                <option value="ml">ml</option>
                <option value="unidad">unidad</option>
              </select>

              <button type="button"
                onClick={() => quitarDetalle(index)}
                disabled={detalles.length === 1}
                style={{
                  background: "transparent", border: "none",
                  color: detalles.length === 1 ? "#555" : "#ff4444",
                  fontSize: "1.2em", cursor: detalles.length === 1 ? "default" : "pointer"
                }}>
                ✕
              </button>
            </div>
          ))}

          <button type="button" onClick={agregarDetalle}
            style={{
              background: "transparent", border: "1px dashed #ffcc00",
              color: "#ffcc00", padding: "6px 12px", borderRadius: "4px",
              cursor: "pointer", marginBottom: "15px", width: "100%"
            }}>
            + Agregar ingrediente
          </button>

          <div style={{ display: "flex", gap: "10px" }}>
            <input
              className="btn-agregar" type="submit"
              value={loading ? "Guardando..." : editando ? "Actualizar Receta" : "Crear Receta"}
              disabled={loading}
              style={{ flex: editando ? "1" : "auto" }}
            />
            {editando && (
              <button type="button" className="btn-eliminar"
                onClick={handleCancelar} disabled={loading}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Tabla de recetas */}
      <table className="tabla-producto">
        <thead>
          <tr>
            <th>ID</th>
            <th>Producto</th>
            <th>Descripción</th>
            <th>Ingredientes</th>
            <th>Costo Total</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan="7" style={{ textAlign: "center" }}>Cargando...</td></tr>
          ) : recetas.length === 0 ? (
            <tr><td colSpan="7" style={{ textAlign: "center" }}>No hay recetas registradas</td></tr>
          ) : (
            recetas.map(receta => (
              <tr key={receta.idReceta}>
                <td data-label="ID">{receta.idReceta}</td>
                <td data-label="Producto">{receta.nombreProducto}</td>
                <td data-label="Descripción">{receta.descripcion || "—"}</td>
                <td data-label="Ingredientes">
                  {receta.detalles?.map((d, i) => (
                    <div key={i} style={{ fontSize: "0.85em" }}>
                      {d.nombreMateriaPrima}: {d.cantidad} {d.unidadMedida}
                    </div>
                  ))}
                </td>
                <td data-label="Costo Total">${receta.costoTotal ?? 0}</td>
                <td data-label="Estado">
                  <span style={{ color: receta.activo ? "green" : "red", fontWeight: "bold" }}>
                    {receta.activo ? "Activa" : "Inactiva"}
                  </span>
                </td>
                <td data-label="Acciones">
                  <div style={{ display: "flex", gap: "5px", justifyContent: "center" }}>
                    <button className="btn-agregar"
                      onClick={() => handleEditar(receta)} disabled={loading}
                      style={{ fontSize: "0.85em", padding: "6px 12px" }}>
                      Editar
                    </button>
                    {receta.activo && (
                      <button className="btn-eliminar"
                        onClick={() => handleDesactivar(receta.idReceta)} disabled={loading}
                        style={{ fontSize: "0.85em", padding: "6px 12px" }}>
                        Desactivar
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </main>
  );
}

export default GestionRecetas;
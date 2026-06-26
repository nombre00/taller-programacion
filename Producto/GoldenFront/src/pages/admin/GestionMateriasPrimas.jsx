import React, { useState, useEffect } from "react";
import * as mpService from "../../services/materiaPrimaService.mock";
import "../../styles/gestionProd.css";

function GestionMateriasPrimas() {
  const [materiasPrimas, setMateriasPrimas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editando, setEditando] = useState(null);

  const [formData, setFormData] = useState({
    nombre: "",
    unidadMedida: "",
    stockActual: "",
    stockMinimo: "",
  });

  useEffect(() => {
    cargar();
  }, []);

  const cargar = async () => {
    try {
      setLoading(true);
      const data = await mpService.obtenerTodasMateriasPrimas();
      setMateriasPrimas(data);
    } catch (error) {
      alert("Error al cargar materias primas: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const payload = {
        nombre: formData.nombre,
        unidadMedida: formData.unidadMedida,
        stockActual: parseFloat(formData.stockActual),
        stockMinimo: parseFloat(formData.stockMinimo),
      };

      if (editando) {
        await mpService.actualizarMateriaPrima(editando.idMateriaPrima, payload);
        alert("Materia prima actualizada exitosamente");
      } else {
        await mpService.crearMateriaPrima(payload);
        alert("Materia prima creada exitosamente");
      }

      handleCancelar();
      await cargar();
    } catch (error) {
      alert("Error al guardar: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleEditar = (mp) => {
    setEditando(mp);
    setFormData({
      nombre: mp.nombre,
      unidadMedida: mp.unidadMedida,
      stockActual: mp.stockActual?.toString() || "0",
      stockMinimo: mp.stockMinimo?.toString() || "0",
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDesactivar = async (id) => {
    if (!window.confirm("¿Desactivar esta materia prima?")) return;
    try {
      setLoading(true);
      await mpService.desactivarMateriaPrima(id);
      await cargar();
      alert("Materia prima desactivada");
    } catch (error) {
      alert("Error al desactivar: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCancelar = () => {
    setEditando(null);
    setFormData({ nombre: "", unidadMedida: "", stockActual: "", stockMinimo: "" });
  };

  return (
    <main className="prod-admin-content">
      {/* Formulario */}
      <div className="form-container-producto">
        <form className="form-producto" onSubmit={handleSubmit}>
          <h1 className="titulo-gp">
            {editando ? "Editar Materia Prima" : "Nueva Materia Prima"}
          </h1>
          {editando && (
            <p style={{ color: "#ffc107", marginBottom: "15px" }}>
              <strong>Editando:</strong> {editando.nombre}
            </p>
          )}

          <label htmlFor="nombre">Nombre</label>
          <input
            id="nombre" name="nombre" type="text"
            placeholder="Ej: Carne de vacuno"
            value={formData.nombre} onChange={handleChange} required
          />

          <label htmlFor="unidadMedida">Unidad de Medida</label>
          <select
            id="unidadMedida" name="unidadMedida"
            value={formData.unidadMedida} onChange={handleChange} required
          >
            <option value="">Seleccionar unidad</option>
            <option value="kg">kg</option>
            <option value="g">g</option>
            <option value="l">l</option>
            <option value="ml">ml</option>
            <option value="unidad">unidad</option>
          </select>

          <label htmlFor="stockActual">Stock Actual</label>
          <input
            id="stockActual" name="stockActual" type="number"
            step="0.001" min="0"
            placeholder="0.000"
            value={formData.stockActual} onChange={handleChange} required
          />

          <label htmlFor="stockMinimo">Stock Mínimo</label>
          <input
            id="stockMinimo" name="stockMinimo" type="number"
            step="0.001" min="0"
            placeholder="0.000"
            value={formData.stockMinimo} onChange={handleChange} required
          />

          <div style={{ display: "flex", gap: "10px" }}>
            <input
              className="btn-agregar" type="submit"
              value={loading ? "Guardando..." : editando ? "Actualizar" : "Agregar Materia Prima"}
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

      {/* Tabla */}
      <table className="tabla-producto">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Unidad</th>
            <th>Stock Actual</th>
            <th>Stock Mínimo</th>
            <th>Costo Prom.</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan="8" style={{ textAlign: "center" }}>Cargando...</td></tr>
          ) : materiasPrimas.length === 0 ? (
            <tr><td colSpan="8" style={{ textAlign: "center" }}>No hay materias primas registradas</td></tr>
          ) : (
            materiasPrimas.map((mp) => {
              const stockBajo = mp.stockActual <= mp.stockMinimo;
              return (
                <tr key={mp.idMateriaPrima}>
                  <td data-label="ID">{mp.idMateriaPrima}</td>
                  <td data-label="Nombre">{mp.nombre}</td>
                  <td data-label="Unidad">{mp.unidadMedida}</td>
                  <td data-label="Stock Actual" style={{ color: stockBajo ? "red" : "inherit", fontWeight: stockBajo ? "bold" : "normal" }}>
                    {mp.stockActual} {stockBajo && "⚠️"}
                  </td>
                  <td data-label="Stock Mínimo">{mp.stockMinimo}</td>
                  <td data-label="Costo Prom.">${mp.costoUnitarioPromedio ?? 0}</td>
                  <td data-label="Estado">
                    <span style={{ color: mp.activo ? "green" : "red", fontWeight: "bold" }}>
                      {mp.activo ? "Activo" : "Inactivo"}
                    </span>
                  </td>
                  <td data-label="Acciones">
                    <div style={{ display: "flex", gap: "5px", justifyContent: "center" }}>
                      <button className="btn-agregar"
                        onClick={() => handleEditar(mp)} disabled={loading}
                        style={{ fontSize: "0.85em", padding: "6px 12px" }}>
                        Editar
                      </button>
                      {mp.activo && (
                        <button className="btn-eliminar"
                          onClick={() => handleDesactivar(mp.idMateriaPrima)} disabled={loading}
                          style={{ fontSize: "0.85em", padding: "6px 12px" }}>
                          Desactivar
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </main>
  );
}

export default GestionMateriasPrimas;
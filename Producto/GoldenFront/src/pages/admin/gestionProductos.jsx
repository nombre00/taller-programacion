import React, { useState, useEffect } from "react";
import "../../styles/gestionProd.css";
import * as productosService from "../../services/productosService";

function GestionProductos() {

  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    nombre: "",
    idCategoria: "",
    precio: "",
    descripcion: "",
    imagen: "",
    disponible: true,
  });

  const [imagenFile, setImagenFile] = useState(null);
  const [imagenPreview, setImagenPreview] = useState(null);
  const [productoEditando, setProductoEditando] = useState(null);

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    try {
      setLoading(true);
      const data = await productosService.obtenerTodosProductos();
      setProductos(data);
    } catch (error) {
      console.error("Error al cargar productos:", error);
      alert("Error al cargar productos: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImagenFile(file);
      const reader = new FileReader();
      reader.onloadend = () => setImagenPreview(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    let productoResultado = null;
    let imagenSubidaExitosamente = false;

    try {
      setLoading(true);

      const productoData = {
        nombre: formData.nombre,
        idCategoria: parseInt(formData.idCategoria),
        precio: parseFloat(formData.precio),
        descripcion: formData.descripcion,
        disponible: formData.disponible === true || formData.disponible === 'true',
      };

      if (productoEditando) {
        const idProducto = productoEditando.idProducto || productoEditando.id;
        if (!imagenFile) productoData.imagen = formData.imagen;
        productoResultado = await productosService.actualizarProducto(idProducto, productoData);
      } else {
        productoData.imagen = "";
        productoResultado = await productosService.crearProducto(productoData);
      }

      const idProducto = productoResultado.idProducto || productoResultado.id;

      if (imagenFile && idProducto) {
        try {
          await productosService.subirImagenProducto(idProducto, imagenFile);
          imagenSubidaExitosamente = true;
        } catch (imagenError) {
          alert(`Producto guardado, pero hubo un error al subir la imagen.\n\nError: ${imagenError.response?.data?.message || imagenError.message}`);
        }
      }

      await cargarProductos();
      handleCancelarEdicion();
      e.target.reset();

      const accion = productoEditando ? "actualizado" : "agregado";
      if (imagenSubidaExitosamente || !imagenFile) {
        alert(`Producto ${accion} exitosamente`);
      }
    } catch (error) {
      const accion = productoEditando ? "actualizar" : "agregar";
      alert(`Error al ${accion} producto: ` + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Estás seguro de eliminar este producto?")) return;
    try {
      setLoading(true);
      await productosService.eliminarProducto(id);
      setProductos(prev => prev.filter(p => (p.idProducto || p.id) !== id));
      alert("Producto eliminado exitosamente");
    } catch (error) {
      alert("Error al eliminar producto: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleEditar = (producto) => {
    setProductoEditando(producto);
    setFormData({
      nombre: producto.nombreProducto || producto.nombre || "",
      idCategoria: producto.idCategoria?.toString() || "",
      precio: (producto.precioBase || producto.precio)?.toString() || "",
      descripcion: producto.descripcion || "",
      imagen: producto.imagen || "",
      disponible: producto.disponible ?? true,
    });
    setImagenPreview(producto.imagen || null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCancelarEdicion = () => {
    setProductoEditando(null);
    setFormData({ nombre: "", idCategoria: "", precio: "", descripcion: "", imagen: "", disponible: true });
    setImagenFile(null);
    setImagenPreview(null);
  };

  return (
    <main className="prod-admin-content">
      <div className="form-container-producto">
        <form className="form-producto" onSubmit={handleSubmit}>
          <h1 className="titulo-gp">
            {productoEditando ? 'Editar Producto' : 'Gestión de Productos'}
          </h1>
          {productoEditando && (
            <p style={{ color: '#ffc107', marginBottom: '15px' }}>
              <strong>Editando:</strong> {productoEditando.nombreProducto || productoEditando.nombre}
            </p>
          )}

          <label htmlFor="nombre">Nombre del Producto</label>
          <input id="nombre" type="text" name="nombre"
            placeholder="Nombre del producto"
            value={formData.nombre} onChange={handleChange} required />

          <label htmlFor="idCategoria">Categoría</label>
          <select id="idCategoria" name="idCategoria"
            value={formData.idCategoria} onChange={handleChange} required>
            <option value="">Seleccionar categoría</option>
            <option value="1">Hamburguesas</option>
            <option value="2">Bebidas</option>
            <option value="3">Acompañamientos</option>
            <option value="4">Postres</option>
            <option value="5">Combos</option>
          </select>

          <label htmlFor="precio">Precio</label>
          <input id="precio" type="text" name="precio"
            placeholder="Precio"
            value={formData.precio} onChange={handleChange} required />

          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" name="descripcion"
            placeholder="Descripción del producto"
            value={formData.descripcion} onChange={handleChange}
            rows="3" required />

          <label htmlFor="imagenFile">Imagen del Producto</label>
          <input id="imagenFile" type="file" name="imagenFile"
            accept="image/*" onChange={handleImageChange} />

          {imagenPreview && (
            <div style={{ marginTop: '10px', textAlign: 'center' }}>
              <img src={imagenPreview} alt="Preview"
                style={{ maxWidth: '200px', maxHeight: '200px', borderRadius: '8px', border: '2px solid #ffcc00' }} />
            </div>
          )}

          <label htmlFor="disponible">Disponibilidad</label>
          <select id="disponible" name="disponible"
            value={formData.disponible} onChange={handleChange} required>
            <option value={true}>Disponible</option>
            <option value={false}>No disponible</option>
          </select>

          <div style={{ display: 'flex', gap: '10px' }}>
            <input className="btn-agregar" type="submit"
              value={loading ? "Guardando..." : productoEditando ? "Actualizar Producto" : "Agregar Producto"}
              disabled={loading}
              style={{ flex: productoEditando ? '1' : 'auto' }} />
            {productoEditando && (
              <button type="button" className="btn-eliminar"
                onClick={handleCancelarEdicion} disabled={loading}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>

      <table className="tabla-producto">
        <thead>
          <tr>
            <th>ID</th><th>Nombre</th><th>Categoría</th><th>Precio</th>
            <th>Descripción</th><th>Imagen</th><th>Disponible</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan="8" style={{ textAlign: 'center' }}>Cargando productos...</td></tr>
          ) : productos.length === 0 ? (
            <tr><td colSpan="8" style={{ textAlign: 'center' }}>No hay productos registrados</td></tr>
          ) : (
            productos.map((prod) => (
              <tr key={prod.idProducto || prod.id}>
                <td data-label="ID">{prod.idProducto || prod.id}</td>
                <td data-label="Nombre">{prod.nombreProducto || prod.nombre}</td>
                <td data-label="Categoría">
                  {prod.idCategoria === 1 ? 'Hamburguesas' :
                    prod.idCategoria === 2 ? 'Bebidas' :
                    prod.idCategoria === 3 ? 'Acompañamientos' :
                    prod.idCategoria === 4 ? 'Postres' :
                    prod.idCategoria === 5 ? 'Combos' :
                    `Categoría ${prod.idCategoria}`}
                </td>
                <td data-label="Precio">${prod.precioBase || prod.precio}</td>
                <td data-label="Descripción">{prod.descripcion}</td>
                <td data-label="Imagen">
                  {prod.imagen
                    ? <img src={prod.imagen} alt={prod.nombreProducto || prod.nombre} width="50" />
                    : <span>Sin imagen</span>}
                </td>
                <td data-label="Disponible">
                  <span style={{ color: prod.disponible ? 'green' : 'red', fontWeight: 'bold' }}>
                    {prod.disponible ? 'Sí' : 'No'}
                  </span>
                </td>
                <td data-label="Acciones">
                  <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap', justifyContent: 'center' }}>
                    <button className="btn-agregar" onClick={() => handleEditar(prod)}
                      disabled={loading} style={{ fontSize: '0.85em', padding: '6px 12px', minWidth: '70px' }}>
                      Editar
                    </button>
                    <button className="btn-eliminar" onClick={() => handleEliminar(prod.idProducto || prod.id)}
                      disabled={loading} style={{ fontSize: '0.85em', padding: '6px 12px', minWidth: '70px' }}>
                      Eliminar
                    </button>
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

export default GestionProductos;
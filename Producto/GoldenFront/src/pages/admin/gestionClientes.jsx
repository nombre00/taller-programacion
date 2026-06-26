import React, { useState, useEffect } from "react";
import { Modal, Form, Button, Spinner, Alert } from 'react-bootstrap';
import Sidebar from "../../components/Sidebar";
import {
  obtenerTodosClientes,
  //registrarCliente,
  registrarClientePorAdmin,
  actualizarCliente,
  eliminarCliente,
  obtenerDireccionesPorCliente,
  crearDireccion,
  actualizarDireccion,
  eliminarDireccion,
  obtenerTodasCiudades
} from "../../services/usuariosService";

// Firebase imports
import { initializeApp } from "firebase/app";
import { getAuth, createUserWithEmailAndPassword } from "firebase/auth";

// Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyD_2NIG34JLQ3fPr2SRzwr3PRTb9IedILY",
  authDomain: "goldenburgers-60680.firebaseapp.com",
  projectId: "goldenburgers-60680",
  storageBucket: "goldenburgers-60680.firebasestorage.app",
  messagingSenderId: "200007088077",
  appId: "1:200007088077:web:b0578771a57f0ecb733684",
  measurementId: "G-HWX8VTT56V"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);


// --- Componente Principal ---
export default function GestionClientes() {

  // --- Estados del componente ---
  const [clientes, setClientes] = useState([]);
  const [ciudades, setCiudades] = useState([]);
  const [clienteSeleccionado, setClienteSeleccionado] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });
  const [busqueda, setBusqueda] = useState('');

  // --- Estados para modal de cliente ---
  const [showModalCliente, setShowModalCliente] = useState(false);
  const [clienteEditando, setClienteEditando] = useState(null);
  const [pasoCreacion, setPasoCreacion] = useState(1); // Paso 1: Firebase, Paso 2: Datos cliente
  const [firebaseUidCreado, setFirebaseUidCreado] = useState(''); // UID del usuario creado en Firebase
  const [formCliente, setFormCliente] = useState({
    idUsuario: '',  // Firebase UID (solo usado al editar)
    nombre: '',
    telefono: '',
    email: '',
    password: ''    // Contraseña (solo al crear)
  });

  // --- Estados para modal de dirección ---
  const [showModalDireccion, setShowModalDireccion] = useState(false);
  const [direccionEditando, setDireccionEditando] = useState(null);
  const [formDireccion, setFormDireccion] = useState({
    idCiudad: '',
    direccion: '',
    alias: ''
  });

  // --- CARGAR DATOS INICIALES ---
  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  const cargarDatosIniciales = async () => {
    try {
      setCargando(true);

      // Cargar clientes y ciudades en paralelo
      const [clientesData, ciudadesData] = await Promise.all([
        obtenerTodosClientes(),
        obtenerTodasCiudades()
      ]);

      console.log('📋 Clientes cargados:', clientesData);
      console.log('🏙️ Ciudades cargadas:', ciudadesData);

      // Debug: verificar estructura del primer cliente
      if (clientesData.length > 0) {
        console.log('👤 Primer cliente completo:', clientesData[0]);
        console.log('📧 Email del primer cliente:', clientesData[0].usuario?.email);
      }

      setClientes(clientesData);
      setCiudades(ciudadesData);

    } catch (error) {
      console.error('Error al cargar datos iniciales:', error);
      setMensaje({
        tipo: 'danger',
        texto: 'Error al cargar los datos. Por favor, recarga la página.'
      });
    } finally {
      setCargando(false);
    }
  };

  // --- Función para recargar los datos ---
  const handleRecargarDatos = async () => {
    setMensaje({ tipo: '', texto: '' });
    await cargarDatosIniciales();
    setMensaje({
      tipo: 'success',
      texto: '✓ Datos recargados exitosamente'
    });
  };

  // Filtrar clientes según búsqueda
  const clientesFiltrados = clientes.filter(cliente => {
    if (!busqueda.trim()) return true;

    const terminoBusqueda = busqueda.toLowerCase();
    const nombre = cliente.nombreCliente?.toLowerCase() || '';
    const telefono = cliente.telefonoCliente?.toString() || '';
    const email = (cliente.usuario?.email || cliente.email || '').toLowerCase();
    const idCliente = cliente.idCliente?.toString() || '';
    const fechaCreacion = cliente.usuario?.fechaCreacion || '';

    return (
      nombre.includes(terminoBusqueda) ||
      telefono.includes(terminoBusqueda) ||
      email.includes(terminoBusqueda) ||
      idCliente.includes(terminoBusqueda) ||
      fechaCreacion.includes(terminoBusqueda)
    );
  });

  // Recargar direcciones del cliente seleccionado
  const recargarDireccionesCliente = async (idCliente) => {
    try {
      const direcciones = await obtenerDireccionesPorCliente(idCliente);

      // Actualizar el cliente en la lista
      setClientes(prev => prev.map(c =>
        c.idCliente === idCliente
          ? { ...c, direcciones }
          : c
      ));

      // Actualizar cliente seleccionado si es el mismo
      if (clienteSeleccionado?.idCliente === idCliente) {
        setClienteSeleccionado(prev => ({ ...prev, direcciones }));
      }
    } catch (error) {
      console.error('Error al recargar direcciones:', error);
    }
  };

  // --- FUNCIONES PARA CLIENTE ---
  
  // Abrir modal para crear/editar cliente
  const handleAbrirModalCliente = (cliente = null) => {
    if (cliente) {
      // Modo edición (sin pasos, va directo)
      setClienteEditando(cliente);
      setPasoCreacion(1); // No usa pasos al editar
      setFirebaseUidCreado('');
      setFormCliente({
        idUsuario: cliente.idUsuario || '',
        nombre: cliente.nombreCliente || '',
        telefono: cliente.telefonoCliente || '',
        email: cliente.usuario?.email || cliente.email || '',
        password: ''
      });
    } else {
      // Modo creación (empieza en paso 1: crear en Firebase)
      setClienteEditando(null);
      setPasoCreacion(1);
      setFirebaseUidCreado('');
      setFormCliente({
        idUsuario: '',
        nombre: '',
        telefono: '',
        email: '',
        password: ''
      });
    }
    setShowModalCliente(true);
  };

  // Cerrar modal de cliente
  const handleCerrarModalCliente = () => {
    setShowModalCliente(false);
    setClienteEditando(null);
    setPasoCreacion(1);
    setFirebaseUidCreado('');
    setFormCliente({
      idUsuario: '',
      nombre: '',
      telefono: '',
      email: '',
      password: ''
    });
  };

  // Manejar cambios en formulario de cliente
  const handleChangeCliente = (e) => {
    const { name, value } = e.target;
    setFormCliente(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // PASO 1: Crear usuario en Firebase
  const handlePaso1_CrearEnFirebase = async (e) => {
    e.preventDefault();

    try {
      // Validaciones
      if (!formCliente.email.trim() || !formCliente.password.trim()) {
        setMensaje({
          tipo: 'warning',
          texto: 'Por favor, completa email y contraseña'
        });
        return;
      }

      if (formCliente.password.length < 6) {
        setMensaje({
          tipo: 'warning',
          texto: 'La contraseña debe tener al menos 6 caracteres'
        });
        return;
      }

      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      console.log('🔐 PASO 1: Creando usuario en Firebase...');
      console.log('📧 Email:', formCliente.email.trim());

      // Crear usuario en Firebase Authentication
      const userCredential = await createUserWithEmailAndPassword(
        auth,
        formCliente.email.trim(),
        formCliente.password
      );
      const firebaseUid = userCredential.user.uid;

      console.log('✅ Usuario creado en Firebase exitosamente');
      console.log('🆔 Firebase UID:', firebaseUid);

      // Guardar el UID para usarlo en el paso 2
      setFirebaseUidCreado(firebaseUid);

      // Avanzar al paso 2
      setPasoCreacion(2);

      setMensaje({
        tipo: 'success',
        texto: '✓ Usuario creado en Firebase. Ahora completa los datos del cliente.'
      });

    } catch (error) {
      console.error('❌ Error en Paso 1:', error);

      if (error.code === 'auth/email-already-in-use') {
        setMensaje({
          tipo: 'danger',
          texto: 'Este email ya está registrado en Firebase'
        });
      } else if (error.code === 'auth/weak-password') {
        setMensaje({
          tipo: 'danger',
          texto: 'La contraseña debe tener al menos 6 caracteres'
        });
      } else if (error.code === 'auth/invalid-email') {
        setMensaje({
          tipo: 'danger',
          texto: 'El email no es válido'
        });
      } else {
        setMensaje({
          tipo: 'danger',
          texto: 'Error al crear usuario en Firebase: ' + error.message
        });
      }
    } finally {
      setGuardando(false);
    }
  };

  // PASO 2: Registrar cliente en Oracle
  const handlePaso2_RegistrarEnOracle = async (e) => {
    e.preventDefault();

    try {
      // Validaciones
      if (!formCliente.nombre.trim()) {
        setMensaje({
          tipo: 'warning',
          texto: 'Por favor, ingresa el nombre del cliente'
        });
        return;
      }

      if (!firebaseUidCreado) {
        setMensaje({
          tipo: 'danger',
          texto: 'Error: No se encontró el UID de Firebase. Vuelve al paso 1.'
        });
        return;
      }

      // Validar teléfono si está presente
      if (formCliente.telefono.trim()) {
        // Limpiar el teléfono (solo números)
        const telefonoLimpio = formCliente.telefono.replace(/\D/g, '');

        if (telefonoLimpio.length !== 9) {
          setMensaje({
            tipo: 'warning',
            texto: 'El teléfono debe tener exactamente 9 dígitos numéricos'
          });
          return;
        }

        // Actualizar el teléfono limpio en el formulario
        setFormCliente(prev => ({ ...prev, telefono: telefonoLimpio }));
      }

      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      console.log('💾 PASO 2: Registrando cliente en Oracle...');
      console.log('🔑 Usando token del ADMIN (desde localStorage)');

      // Verificar que el token existe
      const adminToken = localStorage.getItem("authToken");
      const userRole = localStorage.getItem("userRole");
      const userName = localStorage.getItem("userName");

      console.log('👤 Usuario actual:', userName);
      console.log('🎭 Rol actual:', userRole);
      console.log('🔐 Token existe:', !!adminToken);
      console.log('🔐 Token (primeros 50 chars):', adminToken?.substring(0, 50) + '...');

      if (!adminToken) {
        setMensaje({
          tipo: 'danger',
          texto: 'Error: No se encontró token de autenticación. Por favor, inicia sesión nuevamente.'
        });
        setGuardando(false);
        return;
      }

      console.log('📦 Datos a enviar:', {
        idUsuario: firebaseUidCreado,
        nombreCliente: formCliente.nombre.trim(),
        email: formCliente.email.trim(),
        telefonoCliente: formCliente.telefono.trim() || null
      });

      // Registrar en Oracle usando el servicio de ADMIN (usa el token del ADMIN automáticamente)
      const nuevoCliente = await registrarClientePorAdmin({
        idUsuario: firebaseUidCreado,
        nombreCliente: formCliente.nombre.trim(),
        email: formCliente.email.trim(),
        telefonoCliente: formCliente.telefono.trim() || null
      });

      console.log('✅ Cliente registrado en Oracle exitosamente:', nuevoCliente);

      // Actualizar lista local
      setClientes(prev => [...prev, nuevoCliente]);

      handleCerrarModalCliente();
      setMensaje({
        tipo: 'success',
        texto: '✓ Cliente creado exitosamente en Firebase y Oracle'
      });

    } catch (error) {
      console.error('❌ Error en Paso 2:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al registrar el cliente en Oracle.'
      });
    } finally {
      setGuardando(false);
    }
  };

  // Actualizar cliente (solo para editar, no para crear)
  const handleGuardarCliente = async (e) => {
    e.preventDefault();

    try {
      // Validaciones
      if (!formCliente.nombre.trim()) {
        setMensaje({
          tipo: 'warning',
          texto: 'Por favor, ingresa el nombre del cliente'
        });
        return;
      }

      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      // Actualizar cliente existente
      const clienteActualizado = await actualizarCliente(
        clienteEditando.idCliente,
        formCliente.nombre,
        formCliente.telefono
      );

      // Actualizar en la lista local
      setClientes(prev => prev.map(c =>
        c.idCliente === clienteEditando.idCliente
          ? { ...c, nombreCliente: clienteActualizado.nombreCliente, telefonoCliente: clienteActualizado.telefonoCliente }
          : c
      ));

      // Si el cliente editado está seleccionado, actualizar la selección
      if (clienteSeleccionado && clienteSeleccionado.idCliente === clienteEditando.idCliente) {
        setClienteSeleccionado(prev => ({
          ...prev,
          nombreCliente: clienteActualizado.nombreCliente,
          telefonoCliente: clienteActualizado.telefonoCliente
        }));
      }

      handleCerrarModalCliente();
      setMensaje({
        tipo: 'success',
        texto: '✓ Cliente actualizado exitosamente'
      });

    } catch (error) {
      console.error('❌ Error actualizando cliente:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al actualizar el cliente.'
      });
    } finally {
      setGuardando(false);
    }
  };

  // --- Función para eliminar clientes ---
  const handleEliminar = async (idCliente) => {
    if (!window.confirm("¿Seguro que deseas eliminar este cliente? Esta acción no se puede deshacer.")) {
      return;
    }

    try {
      await eliminarCliente(idCliente);

      // Actualizar lista local
      setClientes(prev => prev.filter(c => c.idCliente !== idCliente));

      // Si el cliente eliminado estaba seleccionado, limpiar selección
      if (clienteSeleccionado && clienteSeleccionado.idCliente === idCliente) {
        setClienteSeleccionado(null);
      }

      setMensaje({
        tipo: 'success',
        texto: '✓ Cliente eliminado exitosamente'
      });

    } catch (error) {
      console.error('Error al eliminar cliente:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al eliminar el cliente. Por favor, intenta nuevamente.'
      });
    }
  };

  // --- FUNCIONES PARA DIRECCIÓN ---

  // Abrir modal para crear/editar dirección
  const handleAbrirModalDireccion = (direccion = null) => {
    if (!clienteSeleccionado) {
      setMensaje({
        tipo: 'warning',
        texto: 'Por favor, selecciona un cliente primero'
      });
      return;
    }

    if (direccion) {
      // Modo edición
      setDireccionEditando(direccion);
      setFormDireccion({
        idCiudad: direccion.ciudad?.idCiudad || direccion.idCiudad || '',
        direccion: direccion.direccion || '',
        alias: direccion.alias || ''
      });
    } else {
      // Modo creación
      setDireccionEditando(null);
      setFormDireccion({
        idCiudad: '',
        direccion: '',
        alias: ''
      });
    }
    setShowModalDireccion(true);
  };

  // Cerrar modal de dirección
  const handleCerrarModalDireccion = () => {
    setShowModalDireccion(false);
    setDireccionEditando(null);
    setFormDireccion({
      idCiudad: '',
      direccion: '',
      alias: ''
    });
  };

  // Manejar cambios en formulario de dirección
  const handleChangeDireccion = (e) => {
    const { name, value } = e.target;
    setFormDireccion(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Guardar dirección (crear o actualizar)
  const handleGuardarDireccion = async (e) => {
    e.preventDefault();

    try {
      // Validaciones
      if (!formDireccion.direccion.trim() || !formDireccion.idCiudad) {
        setMensaje({
          tipo: 'warning',
          texto: 'Por favor, completa todos los campos obligatorios de la dirección'
        });
        return;
      }

      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      if (direccionEditando) {
        // ACTUALIZAR dirección existente
        await actualizarDireccion(direccionEditando.idDireccion, {
          idCiudad: parseInt(formDireccion.idCiudad),
          direccion: formDireccion.direccion,
          alias: formDireccion.alias
        });

        // Recargar direcciones del cliente
        await recargarDireccionesCliente(clienteSeleccionado.idCliente);

        handleCerrarModalDireccion();
        setMensaje({
          tipo: 'success',
          texto: '✓ Dirección actualizada exitosamente'
        });

      } else {
        // CREAR nueva dirección
        await crearDireccion({
          idCliente: clienteSeleccionado.idCliente,
          idCiudad: parseInt(formDireccion.idCiudad),
          direccion: formDireccion.direccion,
          alias: formDireccion.alias
        });

        // Recargar direcciones del cliente
        await recargarDireccionesCliente(clienteSeleccionado.idCliente);

        handleCerrarModalDireccion();
        setMensaje({
          tipo: 'success',
          texto: '✓ Dirección agregada exitosamente'
        });
      }

    } catch (error) {
      console.error('Error guardando dirección:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al guardar la dirección. Por favor, intenta nuevamente.'
      });
    } finally {
      setGuardando(false);
    }
  };

  // Eliminar dirección
  const handleEliminarDireccion = async (idDireccion) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta dirección? Esta acción no se puede deshacer.')) {
      return;
    }

    try {
      await eliminarDireccion(idDireccion);

      // Recargar direcciones del cliente
      await recargarDireccionesCliente(clienteSeleccionado.idCliente);

      setMensaje({
        tipo: 'success',
        texto: '✓ Dirección eliminada exitosamente'
      });

    } catch (error) {
      console.error('Error al eliminar dirección:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al eliminar la dirección. Por favor, intenta nuevamente.'
      });
    }
  };

  // --- Seleccionar cliente para ver sus direcciones ---
  const handleSeleccionarCliente = async (cliente) => {
    try {
      // Cargar direcciones del cliente si aún no están cargadas
      const direcciones = await obtenerDireccionesPorCliente(cliente.idCliente);
      setClienteSeleccionado({ ...cliente, direcciones });
    } catch (error) {
      console.error('Error al cargar direcciones:', error);
      setClienteSeleccionado({ ...cliente, direcciones: [] });
    }
  };

  // --- Obtener nombre de ciudad ---
  const getNombreCiudad = (idCiudad) => {
    const ciudad = ciudades.find(c => c.idCiudad === idCiudad);
    return ciudad ? ciudad.nombreCiudad : `Ciudad ID: ${idCiudad}`;
  };

   // --- Renderizado ---
  return (
    <div className="admin-layout">
      <Sidebar adminName="Administrador" onLogoutAdmin={() => console.log("Cerrando sesión")} />

      <main className="admin-content">
        <h1 className="mb-4">Gestión de Clientes</h1>

        {/* Alertas de mensajes */}
        {mensaje.texto && (
          <Alert
            variant={mensaje.tipo}
            dismissible
            onClose={() => setMensaje({ tipo: '', texto: '' })}
            className="mb-4"
          >
            {mensaje.texto}
          </Alert>
        )}

        {/* Spinner de carga */}
        {cargando ? (
          <div className="text-center py-5">
            <Spinner animation="border" variant="warning" />
            <p className="mt-3">Cargando datos...</p>
          </div>
        ) : (
          <>

        {/* ---------- SECCIÓN: CLIENTES ---------- */}
        <section className="mb-5">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2>Clientes Registrados</h2>
            <div className="d-flex gap-2 align-items-center">
              {/* Buscador */}
              <div className="position-relative">
                <input
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="🔍 Buscar cliente..."
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                  style={{ minWidth: '250px', paddingRight: busqueda ? '30px' : '10px' }}
                />
                {busqueda && (
                  <button
                    className="btn btn-link position-absolute top-50 end-0 translate-middle-y p-0 me-2"
                    onClick={() => setBusqueda('')}
                    style={{ fontSize: '14px', color: '#999' }}
                    title="Limpiar búsqueda"
                  >
                    <i className="bi bi-x-circle-fill"></i>
                  </button>
                )}
              </div>
              <button
                className="btn btn-outline-light"
                onClick={handleRecargarDatos}
                disabled={cargando}
                title="Recargar lista de clientes"
              >
                {cargando ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <i className="bi bi-arrow-clockwise"></i>
                )}
              </button>
              <button
                className="btn btn-warning fw-semibold"
                onClick={() => handleAbrirModalCliente()}
              >
                <i className="bi bi-person-plus-fill me-2"></i>Nuevo Cliente
              </button>
            </div>
          </div>

          {/* Tabla de clientes */}
          <div className="table-responsive">
            <table className="table table-dark table-hover align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre Completo</th>
                  <th>Teléfono</th>
                  <th>Email</th>
                  <th>Fecha Registro</th>
                  <th className="text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {clientesFiltrados.length > 0 ? (
                  clientesFiltrados.map((c) => (
                    <tr
                      key={c.idCliente}
                      onClick={() => handleSeleccionarCliente(c)}
                      style={{ cursor: 'pointer' }}
                      className={clienteSeleccionado?.idCliente === c.idCliente ? 'table-active' : ''}
                    >
                      <td>{c.idCliente}</td>
                      <td>{c.nombreCliente}</td>
                      <td>{c.telefonoCliente || 'No registrado'}</td>
                      <td>{c.usuario?.email || c.email || 'No disponible'}</td>
                      <td>Cliente del sistema</td>
                      <td className="text-center">
                        <button
                          className="btn btn-sm btn-outline-light me-2"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleAbrirModalCliente(c);
                          }}
                        >
                          <i className="bi bi-pencil-square"></i>
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleEliminar(c.idCliente);
                          }}
                        >
                          <i className="bi bi-trash3"></i>
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center text-muted">
                      {busqueda ? `No se encontraron clientes con "${busqueda}"` : 'No hay clientes registrados'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* ---------- SECCIÓN: DIRECCIONES DEL CLIENTE SELECCIONADO ---------- */}
        {clienteSeleccionado && (
          <section>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2>
                Direcciones de {clienteSeleccionado.nombreCliente}
                <button
                  className="btn btn-sm btn-outline-secondary ms-3"
                  onClick={() => setClienteSeleccionado(null)}
                >
                  <i className="bi bi-x-circle me-1"></i>
                  Limpiar selección
                </button>
              </h2>
              <button
                className="btn btn-warning fw-semibold"
                onClick={() => handleAbrirModalDireccion()}
              >
                <i className="bi bi-plus-circle me-2"></i>Agregar Dirección
              </button>
            </div>

            {/* Tabla de direcciones */}
            <div className="table-responsive">
              <table className="table table-dark table-hover align-middle">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Alias</th>
                    <th>Dirección</th>
                    <th>Ciudad</th>
                    <th className="text-center">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {clienteSeleccionado.direcciones && clienteSeleccionado.direcciones.length > 0 ? (
                    clienteSeleccionado.direcciones.map((dir) => (
                      <tr key={dir.idDireccion}>
                        <td>{dir.idDireccion}</td>
                        <td>
                          {dir.alias ? (
                            <span className="badge bg-warning text-dark">{dir.alias}</span>
                          ) : (
                            <span className="text-muted">Sin alias</span>
                          )}
                        </td>
                        <td>{dir.direccion}</td>
                        <td>
                          <i className="bi bi-geo-alt-fill me-1 text-warning"></i>
                          {dir.ciudad?.nombreCiudad || dir.nombreCiudad || getNombreCiudad(dir.idCiudad || dir.ciudad?.idCiudad)}
                        </td>
                        <td className="text-center">
                          <button
                            className="btn btn-sm btn-outline-light me-2"
                            onClick={() => handleAbrirModalDireccion(dir)}
                          >
                            <i className="bi bi-pencil-square"></i>
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleEliminarDireccion(dir.idDireccion)}
                          >
                            <i className="bi bi-trash3"></i>
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" className="text-center text-muted">
                        Este cliente no tiene direcciones registradas
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {/* ---------- MODAL PARA CLIENTE ---------- */}
        <Modal show={showModalCliente} onHide={handleCerrarModalCliente} size="lg">
          <Modal.Header closeButton className="bg-dark text-white">
            <Modal.Title>
              {clienteEditando
                ? 'Editar Cliente'
                : `Nuevo Cliente ${!clienteEditando ? `- Paso ${pasoCreacion} de 2` : ''}`
              }
            </Modal.Title>
          </Modal.Header>

          {/* MODO EDICIÓN */}
          {clienteEditando && (
            <Form onSubmit={handleGuardarCliente}>
              <Modal.Body className="bg-dark text-white">
                <Form.Group className="mb-3">
                  <Form.Label>Nombre Completo *</Form.Label>
                  <Form.Control
                    type="text"
                    name="nombre"
                    value={formCliente.nombre}
                    onChange={handleChangeCliente}
                    required
                    placeholder="Ej: Juan Pérez González"
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Email</Form.Label>
                  <Form.Control
                    type="email"
                    value={formCliente.email}
                    disabled
                  />
                  <Form.Text className="text-muted">
                    El email no se puede modificar
                  </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Teléfono</Form.Label>
                  <Form.Control
                    type="tel"
                    name="telefono"
                    value={formCliente.telefono}
                    onChange={handleChangeCliente}
                    placeholder="+56912345678"
                  />
                </Form.Group>
              </Modal.Body>
              <Modal.Footer className="bg-dark">
                <Button variant="secondary" onClick={handleCerrarModalCliente} disabled={guardando}>
                  Cancelar
                </Button>
                <Button variant="warning" type="submit" disabled={guardando} className="text-dark fw-semibold">
                  {guardando ? (
                    <><Spinner as="span" animation="border" size="sm" className="me-2" />Guardando...</>
                  ) : (
                    <><i className="bi bi-check-circle me-2"></i>Actualizar</>
                  )}
                </Button>
              </Modal.Footer>
            </Form>
          )}

          {/* MODO CREACIÓN - PASO 1: Crear en Firebase */}
          {!clienteEditando && pasoCreacion === 1 && (
            <Form onSubmit={handlePaso1_CrearEnFirebase}>
              <Modal.Body className="bg-dark text-white">
                <Alert variant="info" className="mb-3">
                  <i className="bi bi-info-circle me-2"></i>
                  <strong>Paso 1:</strong> Primero crearemos la cuenta en Firebase
                </Alert>

                <Form.Group className="mb-3">
                  <Form.Label>Email *</Form.Label>
                  <Form.Control
                    type="email"
                    name="email"
                    value={formCliente.email}
                    onChange={handleChangeCliente}
                    required
                    placeholder="ejemplo@correo.com"
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Contraseña *</Form.Label>
                  <Form.Control
                    type="password"
                    name="password"
                    value={formCliente.password}
                    onChange={handleChangeCliente}
                    required
                    placeholder="Mínimo 6 caracteres"
                    minLength="6"
                  />
                  <Form.Text className="text-muted">
                    Se creará el usuario en Firebase Authentication
                  </Form.Text>
                </Form.Group>
              </Modal.Body>
              <Modal.Footer className="bg-dark">
                <Button variant="secondary" onClick={handleCerrarModalCliente} disabled={guardando}>
                  Cancelar
                </Button>
                <Button variant="warning" type="submit" disabled={guardando} className="text-dark fw-semibold">
                  {guardando ? (
                    <><Spinner as="span" animation="border" size="sm" className="me-2" />Creando...</>
                  ) : (
                    <><i className="bi bi-arrow-right me-2"></i>Siguiente</>
                  )}
                </Button>
              </Modal.Footer>
            </Form>
          )}

          {/* MODO CREACIÓN - PASO 2: Completar datos y registrar en Oracle */}
          {!clienteEditando && pasoCreacion === 2 && (
            <Form onSubmit={handlePaso2_RegistrarEnOracle}>
              <Modal.Body className="bg-dark text-white">
                <Alert variant="success" className="mb-3">
                  <i className="bi bi-check-circle me-2"></i>
                  <strong>Usuario creado en Firebase!</strong> Ahora completa los datos del cliente.
                </Alert>

                <Form.Group className="mb-3">
                  <Form.Label>Email (creado en Firebase)</Form.Label>
                  <Form.Control
                    type="email"
                    value={formCliente.email}
                    disabled
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Nombre Completo *</Form.Label>
                  <Form.Control
                    type="text"
                    name="nombre"
                    value={formCliente.nombre}
                    onChange={handleChangeCliente}
                    required
                    placeholder="Ej: Juan Pérez González"
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Teléfono</Form.Label>
                  <Form.Control
                    type="tel"
                    name="telefono"
                    value={formCliente.telefono}
                    onChange={handleChangeCliente}
                    placeholder="912345678"
                    pattern="[0-9]{9}"
                    maxLength="9"
                  />
                  <Form.Text className="text-muted">
                    Debe tener exactamente 9 dígitos numéricos (opcional)
                  </Form.Text>
                </Form.Group>
              </Modal.Body>
              <Modal.Footer className="bg-dark">
                <Button
                  variant="secondary"
                  onClick={() => setPasoCreacion(1)}
                  disabled={guardando}
                >
                  <i className="bi bi-arrow-left me-2"></i>Volver
                </Button>
                <Button variant="warning" type="submit" disabled={guardando} className="text-dark fw-semibold">
                  {guardando ? (
                    <><Spinner as="span" animation="border" size="sm" className="me-2" />Guardando...</>
                  ) : (
                    <><i className="bi bi-check-circle me-2"></i>Guardar</>
                  )}
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Modal>

        {/* ---------- MODAL PARA DIRECCIÓN ---------- */}
        <Modal show={showModalDireccion} onHide={handleCerrarModalDireccion} size="lg">
          <Modal.Header closeButton className="bg-dark text-white">
            <Modal.Title>
              {direccionEditando ? 'Editar Dirección' : 'Agregar Nueva Dirección'}
            </Modal.Title>
          </Modal.Header>
          <Form onSubmit={handleGuardarDireccion}>
            <Modal.Body className="bg-dark text-white">
              <Form.Group className="mb-3">
                <Form.Label>Dirección Completa *</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  name="direccion"
                  value={formDireccion.direccion}
                  onChange={handleChangeDireccion}
                  required
                  placeholder="Ej: Av. Libertador Bernardo O'Higgins 1234, Depto 501, Torre A"
                />
                <Form.Text className="text-muted">
                  Incluye calle, número, depto/casa, referencias, etc.
                </Form.Text>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Ciudad *</Form.Label>
                <Form.Select
                  name="idCiudad"
                  value={formDireccion.idCiudad}
                  onChange={handleChangeDireccion}
                  required
                >
                  <option value="">Selecciona una ciudad</option>
                  {ciudades.map((ciudad) => (
                    <option key={ciudad.idCiudad} value={ciudad.idCiudad}>
                      {ciudad.nombreCiudad}
                    </option>
                  ))}
                </Form.Select>
                <Form.Text className="text-muted">
                  Selecciona la ciudad donde se encuentra la dirección
                </Form.Text>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Alias (Opcional)</Form.Label>
                <Form.Control
                  type="text"
                  name="alias"
                  value={formDireccion.alias}
                  onChange={handleChangeDireccion}
                  placeholder="Ej: Casa, Trabajo, Oficina"
                  maxLength="50"
                />
                <Form.Text className="text-muted">
                  Un nombre corto para identificar esta dirección
                </Form.Text>
              </Form.Group>
            </Modal.Body>
            <Modal.Footer className="bg-dark">
              <Button 
                variant="secondary" 
                onClick={handleCerrarModalDireccion}
                disabled={guardando}
              >
                Cancelar
              </Button>
              <Button 
                variant="warning" 
                type="submit"
                disabled={guardando}
                className="text-dark fw-semibold"
              >
                {guardando ? (
                  <>
                    <Spinner
                      as="span"
                      animation="border"
                      size="sm"
                      className="me-2"
                    />
                    Guardando...
                  </>
                ) : (
                  <>
                    <i className="bi bi-check-circle me-2"></i>
                    {direccionEditando ? 'Actualizar' : 'Guardar'}
                  </>
                )}
              </Button>
            </Modal.Footer>
          </Form>
        </Modal>
        </>
        )}
      </main>
    </div>
  );
}

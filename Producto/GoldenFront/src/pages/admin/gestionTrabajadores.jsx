
import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { Modal, Form, Button, Spinner, Alert } from 'react-bootstrap';
import {
  obtenerTodosTrabajadores,
  registrarTrabajador,
  actualizarTrabajador,
  actualizarEmailTrabajador,
  cambiarRolTrabajador,
  eliminarTrabajador,
  obtenerTodosRoles
} from '../../services/usuariosService';

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

export default function GestionTrabajadores() {
  // --- Estados del componente ---
  const [trabajadores, setTrabajadores] = useState([]);
  const [roles, setRoles] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [trabajadorEditando, setTrabajadorEditando] = useState(null);
  const [showModalTrabajador, setShowModalTrabajador] = useState(false);
  const [formTrabajador, setFormTrabajador] = useState({
    idUsuario: '',
    nombre: '',
    rut: '',
    email: '',
    password: '', // Contraseña para Firebase
    rol: 2
  });
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });
  const [pasoCreacion, setPasoCreacion] = useState(1); // Paso 1: Firebase, Paso 2: Oracle
  const [firebaseUidCreado, setFirebaseUidCreado] = useState(null);
  const [busqueda, setBusqueda] = useState('');

  // --- Cargar datos iniciales al montar el componente ---
  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  // --- Función para cargar trabajadores y roles desde el backend ---
  const cargarDatosIniciales = async () => {
    try {
      setCargando(true);
      const [trabajadoresData, rolesData] = await Promise.all([
        obtenerTodosTrabajadores(),
        obtenerTodosRoles()
      ]);

      console.log('📋 Trabajadores cargados:', trabajadoresData);
      console.log('🎭 Roles cargados:', rolesData);

      // Debug: verificar estructura del primer trabajador
      if (trabajadoresData.length > 0) {
        console.log('👤 Primer trabajador completo:', trabajadoresData[0]);
        console.log('🎭 Rol del primer trabajador:', trabajadoresData[0].usuario?.rol);
        console.log('📧 Email del primer trabajador:', trabajadoresData[0].usuario?.email);
        console.log('✅ Nombre del rol:', trabajadoresData[0].usuario?.rol?.nombreRol);
      }

      setTrabajadores(trabajadoresData);
      setRoles(rolesData);
    } catch (error) {
      console.error('❌ Error al cargar datos iniciales:', error);
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

  // --- Obtener nombre de rol ---
  const getNombreRol = (idRol) => {
    const rol = roles.find(r => r.idRol === idRol);
    return rol ? rol.nombreRol : `Rol ID: ${idRol}`;
  };

  // Filtrar trabajadores según búsqueda
  const trabajadoresFiltrados = trabajadores.filter(trabajador => {
    if (!busqueda.trim()) return true;

    const terminoBusqueda = busqueda.toLowerCase();
    const nombre = trabajador.nombreTrabajador?.toLowerCase() || '';
    const rut = trabajador.rutTrabajador?.toLowerCase() || '';
    const email = (trabajador.usuario?.email || '').toLowerCase();
    const idTrabajador = trabajador.idTrabajador?.toString() || '';
    const rol = getNombreRol(trabajador.usuario?.idRol)?.toLowerCase() || '';

    return (
      nombre.includes(terminoBusqueda) ||
      rut.includes(terminoBusqueda) ||
      email.includes(terminoBusqueda) ||
      idTrabajador.includes(terminoBusqueda) ||
      rol.includes(terminoBusqueda)
    );
  });

  // --- Abrir modal para crear/editar trabajador ---
  const handleAbrirModalTrabajador = (trabajador = null) => {
    if (trabajador) {
      // EDITAR: Ir directo al paso 2 (no necesita Firebase)
      setTrabajadorEditando(trabajador);
      setFormTrabajador({
        idUsuario: trabajador.idUsuario || trabajador.usuario?.idUsuario || '',
        nombre: trabajador.nombreTrabajador || trabajador.nombre || '',
        rut: trabajador.rutTrabajador || trabajador.rut || '',
        email: trabajador.usuario?.email || trabajador.email || '',
        password: '',
        rol: trabajador.usuario?.rol?.idRol || trabajador.idRol || trabajador.rol?.idRol || trabajador.rol || 2
      });
      setPasoCreacion(2); // Editar no usa Firebase
      setFirebaseUidCreado(null);
    } else {
      // CREAR: Empezar desde paso 1 (Firebase)
      setTrabajadorEditando(null);
      setFormTrabajador({
        idUsuario: '',
        nombre: '',
        rut: '',
        email: '',
        password: '',
        rol: 2
      });
      setPasoCreacion(1); // Crear empieza en paso 1
      setFirebaseUidCreado(null);
    }
    setMensaje({ tipo: '', texto: '' });
    setShowModalTrabajador(true);
  };

  // --- Cerrar modal ---
  const handleCerrarModalTrabajador = () => {
    setShowModalTrabajador(false);
    setTrabajadorEditando(null);
    setFormTrabajador({
      idUsuario: '',
      nombre: '',
      rut: '',
      email: '',
      password: '',
      rol: 2
    });
    setPasoCreacion(1);
    setFirebaseUidCreado(null);
    setMensaje({ tipo: '', texto: '' });
  };

  // --- Manejar cambios en formulario ---
  const handleChangeTrabajador = (e) => {
    const { name, value } = e.target;
    setFormTrabajador(prev => ({
      ...prev,
      [name]: name === 'rol' ? parseInt(value) : value
    }));
  };

  // --- PASO 1: Crear usuario en Firebase ---
  const handleCrearUsuarioFirebase = async (e) => {
    e.preventDefault();

    if (!formTrabajador.email.trim() || !formTrabajador.password.trim()) {
      setMensaje({
        tipo: 'warning',
        texto: 'Por favor, ingresa email y contraseña'
      });
      return;
    }

    try {
      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      console.log('🔥 PASO 1: Creando usuario en Firebase...');

      // Crear usuario en Firebase
      const userCredential = await createUserWithEmailAndPassword(
        auth,
        formTrabajador.email.trim(),
        formTrabajador.password.trim()
      );

      const firebaseUid = userCredential.user.uid;
      console.log('✅ Usuario creado en Firebase con UID:', firebaseUid);

      // Guardar el UID y avanzar al paso 2
      setFirebaseUidCreado(firebaseUid);
      setPasoCreacion(2);

      setMensaje({
        tipo: 'success',
        texto: '✓ Usuario creado en Firebase. Ahora completa los datos del trabajador.'
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

  // --- PASO 2: Guardar trabajador en Oracle ---
  const handleGuardarTrabajador = async (e) => {
    e.preventDefault();

    if (!formTrabajador.nombre.trim() || !formTrabajador.rut.trim()) {
      setMensaje({ tipo: 'warning', texto: 'Completa todos los campos obligatorios' });
      return;
    }

    try {
      setGuardando(true);
      setMensaje({ tipo: '', texto: '' });

      if (trabajadorEditando) {
        // ========== EDITAR trabajador existente ==========
        const idTrabajador = trabajadorEditando.idTrabajador || trabajadorEditando.id;

        console.log('📝 Actualizando trabajador ID:', idTrabajador);

        // Actualizar nombre y RUT
        await actualizarTrabajador(idTrabajador, formTrabajador.nombre.trim(), formTrabajador.rut.trim());

        // Si el email cambió, actualizarlo
        const emailActual = trabajadorEditando.usuario?.email || trabajadorEditando.email;
        if (formTrabajador.email.trim() && formTrabajador.email.trim() !== emailActual) {
          await actualizarEmailTrabajador(idTrabajador, {
            nuevoEmail: formTrabajador.email.trim()
          });
        }

        // Si el rol cambió, actualizarlo
        const rolActual = trabajadorEditando.usuario?.rol?.idRol || trabajadorEditando.idRol || trabajadorEditando.rol?.idRol || trabajadorEditando.rol;
        if (formTrabajador.rol !== rolActual) {
          await cambiarRolTrabajador(idTrabajador, {
            idRol: formTrabajador.rol
          });
        }

        // Recargar lista actualizada
        await cargarDatosIniciales();
        handleCerrarModalTrabajador();
        setMensaje({ tipo: 'success', texto: '✓ Trabajador actualizado exitosamente' });

      } else {
        // ========== CREAR nuevo trabajador ==========
        console.log('💾 PASO 2: Registrando trabajador en Oracle...');
        console.log('Firebase UID:', firebaseUidCreado);

        if (!firebaseUidCreado) {
          setMensaje({
            tipo: 'danger',
            texto: 'Error: No se encontró el UID de Firebase. Reinicia el proceso.'
          });
          setGuardando(false);
          return;
        }

        // Registrar en Oracle usando el servicio de usuarios
        const nuevoTrabajador = await registrarTrabajador({
          idUsuario: firebaseUidCreado,
          nombreTrabajador: formTrabajador.nombre.trim(),
          rutTrabajador: formTrabajador.rut.trim(),
          email: formTrabajador.email.trim(),
          idRol: formTrabajador.rol
        });

        console.log('✅ Trabajador creado exitosamente:', nuevoTrabajador);

        // Recargar la lista completa para mostrar el nuevo trabajador
        await cargarDatosIniciales();
        handleCerrarModalTrabajador();
        setMensaje({ tipo: 'success', texto: '✓ Trabajador creado exitosamente' });
      }
    } catch (error) {
      console.error('❌ Error al guardar trabajador:', error);
      setMensaje({
        tipo: 'danger',
        texto: error.response?.data?.message || 'Error al guardar el trabajador.'
      });
    } finally {
      setGuardando(false);
    }
  };

  // --- Eliminar trabajador ---
  const handleEliminarTrabajador = async (trabajador) => {
    const nombreTrabajador = trabajador.nombreTrabajador || trabajador.nombre;

    if (window.confirm(`¿Seguro que deseas eliminar al trabajador ${nombreTrabajador}?`)) {
      try {
        const idTrabajador = trabajador.idTrabajador || trabajador.id;
        await eliminarTrabajador(idTrabajador);

        setTrabajadores(prev => prev.filter(t =>
          (t.idTrabajador || t.id) !== idTrabajador
        ));
        setMensaje({ tipo: 'success', texto: '✓ Trabajador eliminado exitosamente' });
      } catch (error) {
        console.error('❌ Error al eliminar trabajador:', error);
        setMensaje({
          tipo: 'danger',
          texto: error.response?.data?.message || 'Error al eliminar el trabajador.'
        });
      }
    }
  };

  // --- Renderizado ---
  return (
    <div className="admin-layout">
      <Sidebar adminName="Administrador" onLogoutAdmin={() => console.log("Cerrando sesión")} />
      <main className="admin-content">
        <h1 className="mb-4">Gestión de Trabajadores</h1>
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
        <section>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2>Trabajadores</h2>
            <div className="d-flex gap-2 align-items-center">
              {/* Buscador */}
              <div className="position-relative">
                <input
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="🔍 Buscar trabajador..."
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
                title="Recargar lista de trabajadores"
              >
                {cargando ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <i className="bi bi-arrow-clockwise"></i>
                )}
              </button>
              <button
                className="btn btn-warning fw-semibold"
                onClick={() => handleAbrirModalTrabajador()}
              >
                <i className="bi bi-person-plus-fill me-2"></i>Nuevo Trabajador
              </button>
            </div>
          </div>
          <div className="table-responsive">
            <table className="table table-dark table-hover align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre Completo</th>
                  <th>RUT</th>
                  <th>Email</th>
                  <th>Rol</th>
                  <th className="text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {cargando ? (
                  <tr>
                    <td colSpan="6" className="text-center py-5">
                      <Spinner animation="border" variant="warning" />
                      <p className="mt-3 text-muted">Cargando trabajadores...</p>
                    </td>
                  </tr>
                ) : trabajadoresFiltrados.length > 0 ? (
                  trabajadoresFiltrados.map((t) => (
                    <tr key={t.idTrabajador || t.id}>
                      <td>{t.idTrabajador || t.id}</td>
                      <td>{t.nombreTrabajador || t.nombre || 'No disponible'}</td>
                      <td>{t.rutTrabajador || t.rut || 'No disponible'}</td>
                      <td>{t.usuario?.email || t.email || 'No disponible'}</td>
                      <td>{t.usuario?.rol?.nombreRol || getNombreRol(t.usuario?.rol?.idRol) || 'No disponible'}</td>
                      <td className="text-center">
                        <button
                          className="btn btn-sm btn-outline-light me-2"
                          onClick={() => handleAbrirModalTrabajador(t)}
                        >
                          <i className="bi bi-pencil-square"></i>
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleEliminarTrabajador(t)}
                        >
                          <i className="bi bi-trash3"></i>
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center text-muted">
                      {busqueda ? `No se encontraron trabajadores con "${busqueda}"` : 'No hay trabajadores registrados'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* MODAL PARA TRABAJADOR - 2 PASOS */}
        <Modal show={showModalTrabajador} onHide={handleCerrarModalTrabajador} size="lg">
          <Modal.Header closeButton className="bg-dark text-white">
            <Modal.Title>
              {trabajadorEditando
                ? 'Editar Trabajador'
                : `Nuevo Trabajador - Paso ${pasoCreacion} de 2`
              }
            </Modal.Title>
          </Modal.Header>

          {/* Mostrar alerta de mensajes */}
          {mensaje.texto && (
            <Alert
              variant={mensaje.tipo}
              dismissible
              onClose={() => setMensaje({ tipo: '', texto: '' })}
              className="m-3 mb-0"
            >
              {mensaje.texto}
            </Alert>
          )}

          {/* PASO 1: Crear usuario en Firebase (solo para nuevos trabajadores) */}
          {!trabajadorEditando && pasoCreacion === 1 && (
            <Form onSubmit={handleCrearUsuarioFirebase}>
              <Modal.Body className="bg-dark text-white">
                <p className="text-warning mb-3">
                  <i className="bi bi-info-circle me-2"></i>
                  Primero crea el usuario en Firebase Authentication
                </p>

                <Form.Group className="mb-3">
                  <Form.Label>Email *</Form.Label>
                  <Form.Control
                    type="email"
                    name="email"
                    value={formTrabajador.email}
                    onChange={handleChangeTrabajador}
                    required
                    placeholder="trabajador@ejemplo.com"
                    autoFocus
                  />
                  <Form.Text className="text-muted">
                    Este será el email de inicio de sesión del trabajador
                  </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Contraseña *</Form.Label>
                  <Form.Control
                    type="password"
                    name="password"
                    value={formTrabajador.password}
                    onChange={handleChangeTrabajador}
                    required
                    minLength="6"
                    placeholder="Mínimo 6 caracteres"
                  />
                  <Form.Text className="text-muted">
                    Contraseña temporal para el trabajador (mínimo 6 caracteres)
                  </Form.Text>
                </Form.Group>
              </Modal.Body>

              <Modal.Footer className="bg-dark">
                <Button
                  variant="secondary"
                  onClick={handleCerrarModalTrabajador}
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
                      Creando usuario...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-arrow-right-circle me-2"></i>
                      Continuar al Paso 2
                    </>
                  )}
                </Button>
              </Modal.Footer>
            </Form>
          )}

          {/* PASO 2: Registrar trabajador en Oracle (o editar) */}
          {(trabajadorEditando || pasoCreacion === 2) && (
            <Form onSubmit={handleGuardarTrabajador}>
              <Modal.Body className="bg-dark text-white">
                {!trabajadorEditando && (
                  <p className="text-success mb-3">
                    <i className="bi bi-check-circle me-2"></i>
                    Usuario creado en Firebase. Ahora completa los datos del trabajador.
                  </p>
                )}

                <Form.Group className="mb-3">
                  <Form.Label>Nombre Completo *</Form.Label>
                  <Form.Control
                    type="text"
                    name="nombre"
                    value={formTrabajador.nombre}
                    onChange={handleChangeTrabajador}
                    required
                    placeholder="Ej: Juan Pérez González"
                    autoFocus={!trabajadorEditando}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>RUT *</Form.Label>
                  <Form.Control
                    type="text"
                    name="rut"
                    value={formTrabajador.rut}
                    onChange={handleChangeTrabajador}
                    required
                    placeholder="Ej: 12.345.678-9"
                  />
                  <Form.Text className="text-muted">
                    Formato: 12.345.678-9
                  </Form.Text>
                </Form.Group>

                {trabajadorEditando && (
                  <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control
                      type="email"
                      name="email"
                      value={formTrabajador.email}
                      disabled
                    />
                    <Form.Text className="text-muted">
                      El email no se puede editar desde aquí
                    </Form.Text>
                  </Form.Group>
                )}

                <Form.Group className="mb-3">
                  <Form.Label>Rol *</Form.Label>
                  <Form.Select
                    name="rol"
                    value={formTrabajador.rol}
                    onChange={handleChangeTrabajador}
                    required
                  >
                    {roles.length > 0 ? (
                      roles
                        .filter(rol => rol.idRol !== 3) // Excluir rol Cliente
                        .map(rol => (
                          <option key={rol.idRol} value={rol.idRol}>
                            {rol.nombreRol}
                          </option>
                        ))
                    ) : (
                      <>
                        <option value={1}>Admin</option>
                        <option value={2}>Trabajador</option>
                      </>
                    )}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    El rol determina los permisos del trabajador
                  </Form.Text>
                </Form.Group>
              </Modal.Body>

              <Modal.Footer className="bg-dark">
                <Button
                  variant="secondary"
                  onClick={handleCerrarModalTrabajador}
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
                      {trabajadorEditando ? 'Actualizar Trabajador' : 'Crear Trabajador'}
                    </>
                  )}
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Modal>
      </main>
    </div>
  );
}

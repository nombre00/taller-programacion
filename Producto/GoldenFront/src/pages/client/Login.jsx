import React, { useState } from 'react';
import '../../styles/login2.css';
import { useNavigate } from 'react-router-dom';
import 'bootstrap-icons/font/bootstrap-icons.css';
import api from '../../config/api';
import * as usuariosService from '../../services/usuariosService';

// Firebase imports
import { initializeApp } from "firebase/app";
import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword } from "firebase/auth";

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

function Login() {
  const [isRegisterActive, setIsRegisterActive] = useState(false);
  const [registerName, setRegisterName] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  // --- Login contra Firebase y Backend ---
  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      // PASO 1: Autenticar con Firebase
      const userCredential = await signInWithEmailAndPassword(auth, loginEmail, loginPassword);
      const firebaseToken = await userCredential.user.getIdToken();
      const firebaseUid = userCredential.user.uid; // ✅ Obtener UID directamente de Firebase

      console.log("🔐 Firebase UID:", firebaseUid);

      // PASO 2: Enviar token Firebase al API Gateway
      const response = await api.post("/auth/login", {
        firebaseToken: firebaseToken
      });

      console.log("Respuesta del servidor:", response.data);

      // PASO 3: Guardar respuesta del backend
      // El backend devuelve: internalToken (no necesita decodificar)
      const token = response.data.internalToken;
      const userData = response.data.user;
      
      console.log("Token recibido del backend:", token);
      console.log("Token type:", typeof token);
      console.log("Token length:", token?.length);
      console.log("Token primeros 50 chars:", token?.substring(0, 50));
      
      if (token && userData) {
        console.log("========== DATOS DEL USUARIO ==========");
        console.log("Usuario completo:", userData);
        console.log("Campos disponibles:", Object.keys(userData));
        console.log("Rol:", userData.rolNombre);
        console.log("ID Usuario:", userData.idUsuario);
        console.log("========================================");

        // Guardar token e información del usuario
        localStorage.setItem("authToken", token);
        localStorage.setItem("user", JSON.stringify(userData));
        localStorage.setItem("userName", userData.email);
        localStorage.setItem("userId", firebaseUid); // ✅ Firebase UID obtenido directamente de Firebase
        localStorage.setItem("userRole", userData.rolNombre); // "Admin", "Trabajador", "Cliente"
        localStorage.setItem("isLoggedIn", "true");

        // Verificar que se guardó correctamente
        console.log("✅ Token guardado:", localStorage.getItem("authToken")?.substring(0, 20) + "...");
        console.log("✅ User ID guardado:", localStorage.getItem("userId"));
        console.log("✅ Role guardado:", localStorage.getItem("userRole"));
        console.log("✅ isLoggedIn:", localStorage.getItem("isLoggedIn"));

        // PASO 4: Redirigir según el rol
        if (userData.rolNombre === "Admin" || userData.rolNombre === "Trabajador") {
          console.log("Redirigiendo a dashboard admin...");
          navigate("/admin/dashboard");
        } else {
          console.log("Redirigiendo a inicio...");
          navigate("/inicio");
        }
      } else {
        console.error("Estructura de respuesta inesperada:", response.data);
        setError("Error: El servidor no devolvió un token válido.");
      }
    } catch (err) {
      console.error("Error en login:", err);
      if (err.code === "auth/user-not-found") {
        setError("Usuario no encontrado");
      } else if (err.code === "auth/wrong-password") {
        setError("Contraseña incorrecta");
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 401) {
        setError("Email o contraseña incorrectos");
      } else if (err.response?.status === 403) {
        setError("Tu cuenta no tiene acceso al sistema");
      } else {
        setError("Error al iniciar Sesion. Intenta de nuevo.");
      }
    } finally {
      setLoading(false);
    }
  };

  // --- Registro contra Firebase y Backend ---
  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    if (!registerName || !registerEmail || !registerPassword) {
      setError("Por favor, completa todos los campos.");
      setLoading(false);
      return;
    }

    const emailValido =
      registerEmail.endsWith("@duocuc.cl") ||
      registerEmail.endsWith("@profesor.duocuc.cl") ||
      registerEmail.endsWith("@gmail.com");

    if (!emailValido) {
      setError("El correo debe tener un dominio válido: @duocuc.cl, @profesor.duocuc.cl o @gmail.com");
      setLoading(false);
      return;
    }

    try {
      // PASO 1: Crear usuario en Firebase Authentication
      const userCredential = await createUserWithEmailAndPassword(auth, registerEmail, registerPassword);
      const firebaseUid = userCredential.user.uid;
      
      // PASO 2: Registrar cliente en el backend
      await usuariosService.registrarCliente({
        idUsuario: firebaseUid,
        nombreCliente: registerName,
        email: registerEmail
      });

      setError("");
      alert("¡Registro exitoso! Ahora puedes iniciar sesión.");
      setRegisterName("");
      setRegisterEmail("");
      setRegisterPassword("");
      setIsRegisterActive(false);
    } catch (err) {
      console.error("Error en registro:", err);
      if (err.code === "auth/email-already-in-use") {
        setError("Este email ya está registrado.");
      } else if (err.code === "auth/weak-password") {
        setError("La contraseña debe tener al menos 6 caracteres.");
      } else if (err.code === "auth/invalid-email") {
        setError("El email no es válido.");
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 409) {
        setError("Este email ya está registrado.");
      } else if (err.response?.status === 400) {
        setError("Datos inválidos. Verifica que todos los campos sean correctos.");
      } else {
        setError("Error al registrarse. Intenta de nuevo.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className='login-page-container'>
      <div className="login-wrapper">
        <a
          href="#" id="close-login" className="close-button" onClick={(e) => {
            e.preventDefault();
            navigate("/");
          }}
        >
          <i className="bi bi-x-lg"></i>
        </a>

        <div className={`container ${isRegisterActive ? 'right-panel-active' : ''}`} id="login-container-box">

          {/* FORMULARIO REGISTRO */}
          <div className="form-container-login register-container">
            <form onSubmit={handleRegisterSubmit}>
              <h1>Regístrate aquí</h1>
              {error && <p style={{ color: 'red', fontSize: '12px', marginBottom: '10px' }}>{error}</p>}
              <input 
                type="text" 
                placeholder="Nombre" 
                value={registerName}
                onChange={(e) => setRegisterName(e.target.value)} 
                required 
                disabled={loading}
              />
              <input 
                type="email" 
                placeholder="Email" 
                value={registerEmail}
                onChange={(e) => setRegisterEmail(e.target.value)} 
                required 
                disabled={loading}
              />
              <input 
                type="password" 
                placeholder="Contraseña" 
                value={registerPassword}
                onChange={(e) => setRegisterPassword(e.target.value)} 
                minLength="4" 
                maxLength="10" 
                required 
                disabled={loading}
              />
              <button type="submit" className="btn-register" disabled={loading}>
                {loading ? "Registrando..." : "Regístrate"}
              </button>
            </form>
          </div>

          {/* FORMULARIO LOGIN */}
          <div className="form-container-login login-container">
            <form onSubmit={handleLoginSubmit}>
              <h1>Iniciar Sesión</h1>
              {error && <p style={{ color: 'red', fontSize: '12px', marginBottom: '10px' }}>{error}</p>}
              <input 
                type="email" 
                placeholder="Email" 
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)} 
                required 
                disabled={loading}
              />
              <input 
                type="password" 
                placeholder="Contraseña" 
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)} 
                required 
                disabled={loading}
              />
              <button type="submit" className="btn-register" disabled={loading}>
                {loading ? "Iniciando..." : "Iniciar Sesión"}
              </button>
            </form>
          </div>

          {/* PANEL ANIMADO */}
          <div className="overlay-container">
            <div className="overlay">
              <div className="overlay-panel overlay-left">
                <h1 className="title">Bienvenido a <br /> Golden Burger</h1>
                <p>Las mejores hamburguesas de la quinta región.</p>
                <button className="ghost" onClick={() => setIsRegisterActive(false)} id="login">Iniciar Sesión</button>
              </div>
              <div className="overlay-panel overlay-right">
                <h1 className="title">Comienza <br /> Registrándote</h1>
                <p>Obtén un 10% de descuento en tu primera compra al registrarte.</p>
                <button className="ghost" onClick={() => setIsRegisterActive(true)} id="register">Registro</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default Login;

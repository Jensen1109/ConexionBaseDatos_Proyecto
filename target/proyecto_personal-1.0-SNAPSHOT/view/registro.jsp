<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="modelos.Usuario" %>
<%
    String error   = (String) request.getAttribute("error");
    String exito   = (String) request.getAttribute("exito");
    String ctx     = request.getContextPath();

    // Si hay sesión activa de admin, el formulario va al UsuarioControlador
    // Si no, va al RegistroControlador (flujo público legacy)
    Usuario usuarioActual = null;
    jakarta.servlet.http.HttpSession sess = request.getSession(false);
    if (sess != null) usuarioActual = (Usuario) sess.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);

    String formAction = esAdmin
        ? ctx + "/UsuarioControlador"
        : ctx + "/RegistroControlador";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Registro de Usuario — Tienda Don Pedro</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            background: #f1f5f9;
            display: flex; align-items: center; justify-content: center;
            min-height: 100vh; padding: 1.5rem;
        }
        .card {
            background: #fff; border-radius: 16px;
            padding: 2.2rem 2rem; width: 100%; max-width: 500px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
        }
        .card__logo {
            font-size: 1.6rem; font-weight: 800; color: #1e293b;
            margin-bottom: 0.25rem;
        }
        .card__sub  { color: #64748b; font-size: 0.85rem; margin-bottom: 1.6rem; }
        .alert-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626;
            border-radius: 8px; padding: 0.7rem 1rem; margin-bottom: 1rem;
            font-size: 0.875rem;
        }
        .alert-ok {
            background: #f0fdf4; border: 1px solid #86efac; color: #16a34a;
            border-radius: 8px; padding: 0.7rem 1rem; margin-bottom: 1rem;
            font-size: 0.875rem;
        }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.9rem; }
        .form-group { display: flex; flex-direction: column; gap: 0.3rem; }
        .form-group--full { grid-column: 1 / -1; }
        label { font-size: 0.82rem; font-weight: 600; color: #475569; }
        input, select {
            padding: 0.55rem 0.75rem; border: 1px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; outline: none;
            transition: border-color 0.2s;
        }
        input:focus, select:focus { border-color: #3b82f6; }
        input.input-ok    { border-color: #22c55e !important; }
        input.input-error { border-color: #ef4444 !important; background: #fff5f5 !important; }
        .field-error { display: none; color: #dc2626; font-size: 0.75rem; margin-top: 0.15rem; }
        .field-error.visible { display: flex; align-items: center; gap: 0.25rem; }
        .btn-submit {
            width: 100%; padding: 0.7rem; background: #22c55e; color: #fff;
            border: none; border-radius: 8px; font-size: 0.95rem;
            font-weight: 700; cursor: pointer; margin-top: 1.2rem;
            transition: background 0.2s;
        }
        .btn-submit:hover { background: #16a34a; }
        .card__footer { text-align: center; margin-top: 1rem; font-size: 0.82rem; color: #64748b; }
        .card__footer a { color: #3b82f6; text-decoration: none; }
        @media (max-width: 480px) { .form-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
    <div class="card">
        <div class="card__logo">
            <% if (esAdmin) { %>
            <i class="fas fa-user-plus" style="color:#22c55e;"></i>
            Nuevo usuario
            <% } else { %>
            <i class="fas fa-store" style="color:#22c55e;"></i>
            Tienda Don Pedro
            <% } %>
        </div>
        <div class="card__sub">
            <% if (esAdmin) { %>
            Registra un nuevo admin o empleado en el sistema
            <% } else { %>
            Crea tu cuenta para acceder al sistema
            <% } %>
        </div>

        <% if (error != null) { %>
        <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>
        <% if (exito != null) { %>
        <div class="alert-ok"><i class="fas fa-check-circle"></i> <%= exito %></div>
        <% } %>

        <form action="<%= formAction %>" method="post" novalidate onsubmit="return validarRegistro()">
            <input type="hidden" name="accion" value="registrar">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nombre">Nombre *</label>
                    <input type="text" id="nombre" name="nombre" placeholder="Nombre"
                           oninput="rgValidarNombre(this,'rgErrNombre')" maxlength="60">
                    <span class="field-error" id="rgErrNombre"></span>
                </div>
                <div class="form-group">
                    <label for="apellido">Apellido *</label>
                    <input type="text" id="apellido" name="apellido" placeholder="Apellido"
                           oninput="rgValidarNombre(this,'rgErrApellido')" maxlength="60">
                    <span class="field-error" id="rgErrApellido"></span>
                </div>
                <div class="form-group form-group--full">
                    <label for="email">Email *</label>
                    <input type="text" id="email" name="email" placeholder="correo@ejemplo.com"
                           oninput="rgValidarEmail(this)" maxlength="150" autocomplete="email">
                    <span class="field-error" id="rgErrEmail"></span>
                </div>
                <div class="form-group">
                    <label for="cedula">Cédula *</label>
                    <input type="text" id="cedula" name="cedula" placeholder="Mín. 8 — máx. 15 dígitos"
                           oninput="rgValidarCedula(this)"
                           onkeypress="return /[0-9]/.test(event.key)"
                           maxlength="15">
                    <span class="field-error" id="rgErrCedula"></span>
                </div>
                <div class="form-group">
                    <label for="contrasena">Contraseña *</label>
                    <input type="password" id="contrasena" name="contrasena"
                           placeholder="Mínimo 6 caracteres"
                           oninput="rgValidarPass(this)" autocomplete="new-password">
                    <span class="field-error" id="rgErrPass"></span>
                </div>
                <% if (esAdmin) { %>
                <div class="form-group form-group--full">
                    <label for="idRol">Rol *</label>
                    <select id="idRol" name="idRol" required>
                        <option value="1">Admin</option>
                        <option value="2" selected>Empleado</option>
                    </select>
                </div>
                <% } %>
            </div>
            <button type="submit" class="btn-submit">
                <i class="fas fa-user-plus"></i>
                <%= esAdmin ? "Crear usuario" : "Registrarse" %>
            </button>
        </form>

        <div class="card__footer">
            <% if (esAdmin) { %>
            <a href="<%= ctx %>/UsuarioControlador">
                <i class="fas fa-arrow-left"></i> Volver a gestión de usuarios
            </a>
            <% } else { %>
            ¿Ya tienes cuenta?
            <a href="<%= ctx %>/LoginControlador">Inicia sesión</a>
            <% } %>
        </div>
    </div>
    <script>
        function rgSetError(id, errId, msg) {
            var inp = document.getElementById(id);
            var err = document.getElementById(errId);
            inp.classList.remove('input-ok'); inp.classList.add('input-error');
            err.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
            err.classList.add('visible');
        }
        function rgSetOk(id, errId) {
            var inp = document.getElementById(id);
            var err = document.getElementById(errId);
            inp.classList.remove('input-error'); inp.classList.add('input-ok');
            err.textContent = ''; err.classList.remove('visible');
        }
        function rgClear(id, errId) {
            var inp = document.getElementById(id);
            var err = document.getElementById(errId);
            inp.classList.remove('input-ok','input-error');
            err.textContent = ''; err.classList.remove('visible');
        }

        function rgValidarNombre(inp, errId) {
            var v = inp.value.trim();
            if (v === '') { rgClear(inp.id, errId); return null; }
            if (!/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/.test(v))
                { rgSetError(inp.id, errId, 'Solo se permiten letras, sin números ni símbolos.'); return false; }
            if (v.length < 2)
                { rgSetError(inp.id, errId, 'Debe tener al menos 2 caracteres.'); return false; }
            rgSetOk(inp.id, errId); return true;
        }
        function rgValidarEmail(inp) {
            var v = inp.value.trim();
            if (v === '') { rgClear('email','rgErrEmail'); return null; }
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
                { rgSetError('email','rgErrEmail','Ingresa un correo válido, ej: nombre@gmail.com'); return false; }
            rgSetOk('email','rgErrEmail'); return true;
        }
        function rgValidarCedula(inp) {
            var v = inp.value.trim();
            if (v === '') { rgClear('cedula','rgErrCedula'); return null; }
            if (!/^\d+$/.test(v))
                { rgSetError('cedula','rgErrCedula','La cédula solo puede contener números.'); return false; }
            if (v.length < 8)
                { rgSetError('cedula','rgErrCedula','La cédula debe tener mínimo 8 dígitos.'); return false; }
            if (v.length > 15)
                { rgSetError('cedula','rgErrCedula','La cédula no puede superar 15 dígitos.'); return false; }
            rgSetOk('cedula','rgErrCedula'); return true;
        }
        function rgValidarPass(inp) {
            var v = inp.value;
            if (v === '') { rgClear('contrasena','rgErrPass'); return null; }
            if (v.length < 6)
                { rgSetError('contrasena','rgErrPass','La contraseña debe tener mínimo 6 caracteres.'); return false; }
            rgSetOk('contrasena','rgErrPass'); return true;
        }

        function validarRegistro() {
            var okNombre   = rgValidarNombre(document.getElementById('nombre'),   'rgErrNombre');
            var okApellido = rgValidarNombre(document.getElementById('apellido'), 'rgErrApellido');
            var okEmail    = rgValidarEmail(document.getElementById('email'));
            var okCedula   = rgValidarCedula(document.getElementById('cedula'));
            var okPass     = rgValidarPass(document.getElementById('contrasena'));

            if (document.getElementById('nombre').value.trim() === '')
                { rgSetError('nombre','rgErrNombre','El nombre es obligatorio.'); okNombre = false; }
            if (document.getElementById('apellido').value.trim() === '')
                { rgSetError('apellido','rgErrApellido','El apellido es obligatorio.'); okApellido = false; }
            if (document.getElementById('email').value.trim() === '')
                { rgSetError('email','rgErrEmail','El email es obligatorio.'); okEmail = false; }
            if (document.getElementById('cedula').value.trim() === '')
                { rgSetError('cedula','rgErrCedula','La cédula es obligatoria.'); okCedula = false; }
            if (document.getElementById('contrasena').value === '')
                { rgSetError('contrasena','rgErrPass','La contraseña es obligatoria.'); okPass = false; }

            if (okNombre === false || okApellido === false || okEmail === false ||
                okCedula === false || okPass === false) {
                var primerError = document.querySelector('.input-error');
                if (primerError) primerError.scrollIntoView({behavior:'smooth', block:'center'});
                return false;
            }
            return true;
        }
    </script>
</body>
</html>

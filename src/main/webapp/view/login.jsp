<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String error = (String) request.getAttribute("error");
    String ctx   = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Iniciar sesión — Tienda Don Pedro</title>
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
            padding: 2.4rem 2rem; width: 100%; max-width: 420px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
        }
        .card__logo {
            font-size: 1.6rem; font-weight: 800; color: #1e293b;
            margin-bottom: 0.2rem;
        }
        .card__sub { color: #64748b; font-size: 0.85rem; margin-bottom: 1.8rem; }
        .alert-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626;
            border-radius: 8px; padding: 0.7rem 1rem; margin-bottom: 1.2rem;
            font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem;
        }
        .form-group { display: flex; flex-direction: column; gap: 0.3rem; margin-bottom: 1rem; }
        label { font-size: 0.82rem; font-weight: 600; color: #475569; }
        input {
            padding: 0.6rem 0.75rem; border: 1px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; outline: none;
            transition: border-color 0.2s; background: #f8fafc;
        }
        input:focus { border-color: #3b82f6; background: #fff; }
        input.input-ok    { border-color: #22c55e !important; }
        input.input-error { border-color: #ef4444 !important; background: #fff5f5 !important; }
        .field-error { display: none; color: #dc2626; font-size: 0.75rem; margin-top: 0.15rem; }
        .field-error.visible { display: flex; align-items: center; gap: 0.25rem; }
        .btn-submit {
            width: 100%; padding: 0.7rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 8px; font-size: 0.95rem;
            font-weight: 700; cursor: pointer; margin-top: 0.5rem;
            transition: background 0.2s; display: flex; align-items: center;
            justify-content: center; gap: 0.5rem;
        }
        .btn-submit:hover { background: #2563eb; }
        .card__footer { text-align: center; margin-top: 1.2rem; font-size: 0.82rem; color: #64748b; }
        .card__footer a { color: #3b82f6; text-decoration: none; font-weight: 600; }
        .card__footer a:hover { text-decoration: underline; }
        .divider {
            display: flex; align-items: center; gap: 0.75rem;
            margin: 1.2rem 0; color: #94a3b8; font-size: 0.78rem;
        }
        .divider::before, .divider::after {
            content: ''; flex: 1; height: 1px; background: #e2e8f0;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="card__logo">
            <i class="fas fa-store" style="color:#3b82f6;"></i>
            Tienda Don Pedro
        </div>
        <p class="card__sub">Ingresa tus credenciales para acceder al sistema</p>

        <% if (error != null) { %>
        <div class="alert-error">
            <i class="fas fa-exclamation-circle"></i> <%= error %>
        </div>
        <% } %>

        <form action="<%= ctx %>/LoginControlador" method="post" novalidate onsubmit="return validarLogin()">
            <div class="form-group">
                <label for="email">Email o cédula</label>
                <input type="text" id="email" name="email"
                       placeholder="correo@ejemplo.com o número de cédula"
                       oninput="lnValidarEmail(this)" autocomplete="username">
                <span class="field-error" id="lnErrEmail"></span>
            </div>
            <div class="form-group">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena"
                       placeholder="Tu contraseña"
                       oninput="lnValidarPass(this)" autocomplete="current-password">
                <span class="field-error" id="lnErrPass"></span>
            </div>
            <button type="submit" class="btn-submit">
                <i class="fas fa-sign-in-alt"></i> Iniciar sesión
            </button>
        </form>

        <div class="divider">o</div>

        <div class="card__footer">
            ¿No tienes cuenta?
            <a href="<%= ctx %>/RegistroControlador">
                <i class="fas fa-user-plus"></i> Regístrate aquíPc
            </a>
        </div>
    </div>
    <script>
        function lnSetError(id, errId, msg) {
            var inp = document.getElementById(id);
            var err = document.getElementById(errId);
            inp.classList.remove('input-ok'); inp.classList.add('input-error');
            err.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
            err.classList.add('visible');
        }
        function lnSetOk(id, errId) {
            var inp = document.getElementById(id);
            var err = document.getElementById(errId);
            inp.classList.remove('input-error'); inp.classList.add('input-ok');
            err.textContent = ''; err.classList.remove('visible');
        }
        function lnValidarEmail(inp) {
            var v = inp.value.trim();
            if (v === '') { inp.classList.remove('input-ok','input-error'); document.getElementById('lnErrEmail').classList.remove('visible'); return false; }
            if (v.length < 3) { lnSetError('email','lnErrEmail','Ingresa tu email o cédula.'); return false; }
            lnSetOk('email','lnErrEmail'); return true;
        }
        function lnValidarPass(inp) {
            var v = inp.value;
            if (v === '') { inp.classList.remove('input-ok','input-error'); document.getElementById('lnErrPass').classList.remove('visible'); return false; }
            if (v.length < 1) { lnSetError('contrasena','lnErrPass','Ingresa tu contraseña.'); return false; }
            lnSetOk('contrasena','lnErrPass'); return true;
        }
        function validarLogin() {
            var email = document.getElementById('email').value.trim();
            var pass  = document.getElementById('contrasena').value;
            var ok = true;
            if (!email) { lnSetError('email','lnErrEmail','El email o cédula es obligatorio.'); ok = false; }
            if (!pass)  { lnSetError('contrasena','lnErrPass','La contraseña es obligatoria.'); ok = false; }
            return ok;
        }
    </script>
</body>
</html>

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

        <form action="<%= formAction %>" method="post">
            <input type="hidden" name="accion" value="registrar">
            <div class="form-grid">
                <div class="form-group">
                    <label for="nombre">Nombre *</label>
                    <input type="text" id="nombre" name="nombre" placeholder="Nombre" required>
                </div>
                <div class="form-group">
                    <label for="apellido">Apellido *</label>
                    <input type="text" id="apellido" name="apellido" placeholder="Apellido" required>
                </div>
                <div class="form-group form-group--full">
                    <label for="email">Email *</label>
                    <input type="email" id="email" name="email" placeholder="correo@ejemplo.com" required>
                </div>
                <div class="form-group">
                    <label for="cedula">Cédula *</label>
                    <input type="text" id="cedula" name="cedula" placeholder="Número de cédula" required>
                </div>
                <div class="form-group">
                    <label for="contrasena">Contraseña *</label>
                    <input type="password" id="contrasena" name="contrasena" placeholder="Mínimo 6 caracteres" required>
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
</body>
</html>

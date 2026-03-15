<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="modelos.Usuario" %>
<%
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    String  ctx    = request.getContextPath();
    String  error  = (String) request.getAttribute("error");
    String  exito  = (String) request.getAttribute("exito");
    boolean exitoParam = "1".equals(request.getParameter("exito"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Mi perfil — Tienda Don Pedro</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body { display: flex; min-height: 100vh; font-family: 'Segoe UI', system-ui, sans-serif; background: #f1f5f9; }

        /* ── SIDEBAR ── */
        .sidebar {
            width: 230px; background: #1e293b; min-height: 100vh;
            display: flex; flex-direction: column;
            position: fixed; top: 0; left: 0; height: 100vh; overflow-y: auto;
            z-index: 100; transition: transform 0.3s;
        }
        .sidebar__brand { padding: 1.4rem 1.2rem; border-bottom: 1px solid rgba(255,255,255,0.08); }
        .sidebar__brand-title { color: #f8fafc; font-size: 1rem; font-weight: 700; }
        .sidebar__brand-sub { color: #64748b; font-size: 0.72rem; margin-top: 2px; }
        .sidebar__section { padding: 1.2rem 0 0.4rem; }
        .sidebar__label {
            color: #475569; font-size: 0.63rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.1em;
            padding: 0 1.2rem; display: block; margin-bottom: 0.2rem;
        }
        .sidebar__link {
            display: flex; align-items: center; gap: 0.65rem;
            color: #94a3b8; text-decoration: none;
            padding: 0.55rem 1.2rem; font-size: 0.85rem;
            border-left: 3px solid transparent; transition: all 0.18s;
        }
        .sidebar__link i { width: 15px; text-align: center; font-size: 0.8rem; }
        .sidebar__link:hover { color: #e2e8f0; background: rgba(255,255,255,0.05); border-left-color: #3b82f6; }
        .sidebar__link--activo { color: #fff; background: rgba(59,130,246,0.12); border-left-color: #3b82f6; }
        .sidebar__link--logout { color: #f87171 !important; }
        .sidebar__link--logout:hover { color: #fff !important; background: rgba(239,68,68,0.12) !important; border-left-color: #ef4444 !important; }

        .sidebar__user-card {
            display: flex; align-items: center; gap: 0.65rem;
            padding: 0.4rem 1.2rem 0.6rem;
        }
        .sidebar__user-avatar {
            width: 32px; height: 32px; border-radius: 50%;
            background: #3b82f6; color: #fff; flex-shrink: 0;
            display: flex; align-items: center; justify-content: center;
            font-size: 0.72rem; font-weight: 700; text-transform: uppercase;
        }
        .sidebar__user-name { color: #e2e8f0; font-size: 0.82rem; font-weight: 600; line-height: 1.3; }
        .sidebar__user-role { color: #64748b; font-size: 0.7rem; }
        .sidebar__section--cuenta { border-top: 1px solid rgba(255,255,255,0.08); margin-top: 0.5rem; }

        /* ── HAMBURGER ── */
        .hamburger-btn {
            display: none; position: fixed; top: 0.8rem; left: 0.8rem;
            z-index: 150; background: #1e293b; color: #fff;
            border: none; border-radius: 8px; padding: 0.55rem 0.75rem;
            font-size: 1rem; cursor: pointer;
        }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        /* ── MAIN ── */
        .main { margin-left: 230px; flex: 1; padding: 2rem 2.5rem; }
        .page-header { margin-bottom: 2rem; }
        .page-header__title { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .page-header__sub { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── ALERTS ── */
        .alert {
            border-radius: 8px; padding: 0.75rem 1rem;
            font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem;
            margin-bottom: 1.5rem;
        }
        .alert--error { background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626; }
        .alert--ok    { background: #f0fdf4; border: 1px solid #86efac; color: #16a34a; }

        /* ── GRID ── */
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; max-width: 800px; }

        /* ── CARD ── */
        .card {
            background: #fff; border-radius: 14px;
            box-shadow: 0 1px 6px rgba(0,0,0,0.06);
            padding: 1.8rem;
        }
        .card__title {
            font-size: 1rem; font-weight: 700; color: #1e293b;
            margin-bottom: 1.2rem; display: flex; align-items: center; gap: 0.5rem;
        }
        .card__title i { color: #3b82f6; }

        /* ── FORM ── */
        .form-group { display: flex; flex-direction: column; gap: 0.3rem; margin-bottom: 1rem; }
        .form-group:last-child { margin-bottom: 0; }
        label { font-size: 0.8rem; font-weight: 600; color: #475569; }
        input {
            padding: 0.6rem 0.75rem; border: 1px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; outline: none;
            transition: border-color 0.2s; background: #f8fafc;
        }
        input:focus { border-color: #3b82f6; background: #fff; }
        input[readonly] { background: #f1f5f9; color: #64748b; cursor: not-allowed; }
        .btn-submit {
            width: 100%; padding: 0.65rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 8px; font-size: 0.9rem; font-weight: 600;
            cursor: pointer; margin-top: 1rem; transition: background 0.2s;
            display: flex; align-items: center; justify-content: center; gap: 0.4rem;
        }
        .btn-submit:hover { background: #2563eb; }
        .hint { font-size: 0.75rem; color: #94a3b8; margin-top: 0.2rem; }

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; gap: 0.4rem; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <button class="hamburger-btn" onclick="toggleSidebar()"><i class="fas fa-bars"></i></button>
    <div class="overlay" id="overlay" onclick="toggleSidebar()"></div>

    <aside class="sidebar" id="sidebar">
        <div class="sidebar__brand">
            <div class="sidebar__brand-title">Tienda Don Pedro</div>
            <div class="sidebar__brand-sub">Panel de gestión</div>
        </div>

        <div class="sidebar__section">
            <span class="sidebar__label">Productos</span>
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__link">
                <i class="fas fa-box"></i> Ver productos
            </a>
            <% if (usuarioActual != null && usuarioActual.getIdRol() == 1) { %>
            <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="sidebar__link">
                <i class="fas fa-plus-circle"></i> Registrar producto
            </a>
            <% } %>
            <a href="<%= ctx %>/ProductoControlador?accion=stock" class="sidebar__link">
                <i class="fas fa-chart-bar"></i> Control de stock
            </a>
        </div>

        <% if (usuarioActual != null && usuarioActual.getIdRol() == 1) { %>
        <div class="sidebar__section">
            <span class="sidebar__label">Ventas</span>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">
                <i class="fas fa-cart-plus"></i> Registrar venta
            </a>
            <a href="<%= ctx %>/PedidoControlador" class="sidebar__link">
                <i class="fas fa-history"></i> Historial de venta
            </a>
            <a href="<%= ctx %>/ReporteControlador" class="sidebar__link">
                <i class="fas fa-chart-line"></i> Reportes
            </a>
        </div>
        <div class="sidebar__section">
            <span class="sidebar__label">Clientes</span>
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link">
                <i class="fas fa-users"></i> Ver / Editar clientes
            </a>
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
            </a>
        </div>
        <div class="sidebar__section">
            <span class="sidebar__label">Sistema</span>
            <a href="<%= ctx %>/UsuarioControlador" class="sidebar__link">
                <i class="fas fa-user-cog"></i> Gestión de usuarios
            </a>
        </div>
        <% } %>

        <div class="sidebar__section sidebar__section--cuenta">
            <span class="sidebar__label">Mi cuenta</span>
            <div class="sidebar__user-card">
                <div class="sidebar__user-avatar">
                    <%= usuarioActual != null ? String.valueOf(usuarioActual.getNombre().charAt(0)).toUpperCase() + String.valueOf(usuarioActual.getApellido().charAt(0)).toUpperCase() : "?" %>
                </div>
                <div>
                    <div class="sidebar__user-name"><%= usuarioActual != null ? usuarioActual.getNombre() + " " + usuarioActual.getApellido() : "" %></div>
                    <div class="sidebar__user-role"><%= usuarioActual != null && usuarioActual.getIdRol() == 1 ? "Administrador" : "Empleado" %></div>
                </div>
            </div>
            <a href="<%= ctx %>/PerfilControlador" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-user-circle"></i> Mi perfil
            </a>
            <a href="<%= ctx %>/LogoutControlador" class="sidebar__link sidebar__link--logout">
                <i class="fas fa-sign-out-alt"></i> Cerrar sesión
            </a>
        </div>
    </aside>

    <main class="main">
        <div class="page-header">
            <h1 class="page-header__title"><i class="fas fa-user-circle" style="color:#3b82f6;"></i> Mi perfil</h1>
            <p class="page-header__sub">Gestiona tus datos personales y contraseña</p>
        </div>

        <% if (exitoParam) { %>
        <div class="alert alert--ok"><i class="fas fa-check-circle"></i> Perfil actualizado correctamente.</div>
        <% } %>
        <% if (exito != null) { %>
        <div class="alert alert--ok"><i class="fas fa-check-circle"></i> <%= exito %></div>
        <% } %>
        <% if (error != null) { %>
        <div class="alert alert--error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>

        <form action="<%= ctx %>/PerfilControlador" method="post">
            <div class="grid">
                <!-- Datos personales -->
                <div class="card">
                    <h2 class="card__title"><i class="fas fa-id-card"></i> Datos personales</h2>

                    <div class="form-group">
                        <label for="nombre">Nombre *</label>
                        <input type="text" id="nombre" name="nombre"
                               value="<%= usuarioActual != null ? usuarioActual.getNombre() : "" %>" required>
                    </div>
                    <div class="form-group">
                        <label for="apellido">Apellido *</label>
                        <input type="text" id="apellido" name="apellido"
                               value="<%= usuarioActual != null ? usuarioActual.getApellido() : "" %>" required>
                    </div>
                    <div class="form-group">
                        <label for="email">Email *</label>
                        <input type="email" id="email" name="email"
                               value="<%= usuarioActual != null ? usuarioActual.getEmail() : "" %>" required>
                    </div>
                    <div class="form-group">
                        <label>Cédula</label>
                        <input type="text" value="<%= usuarioActual != null ? usuarioActual.getCedula() : "" %>" readonly>
                        <span class="hint">La cédula no se puede modificar.</span>
                    </div>
                    <div class="form-group">
                        <label>Rol</label>
                        <input type="text" value="<%= usuarioActual != null && usuarioActual.getIdRol() == 1 ? "Administrador" : "Empleado" %>" readonly>
                    </div>
                </div>

                <!-- Cambiar contraseña -->
                <div class="card">
                    <h2 class="card__title"><i class="fas fa-lock"></i> Cambiar contraseña</h2>
                    <p style="color:#64748b;font-size:0.82rem;margin-bottom:1rem;">
                        Deja estos campos en blanco si no deseas cambiar tu contraseña.
                    </p>
                    <div class="form-group">
                        <label for="contrasenaActual">Contraseña actual</label>
                        <input type="password" id="contrasenaActual" name="contrasenaActual"
                               placeholder="Tu contraseña actual">
                    </div>
                    <div class="form-group">
                        <label for="contrasenaNueva">Nueva contraseña</label>
                        <input type="password" id="contrasenaNueva" name="contrasenaNueva"
                               placeholder="Mínimo 6 caracteres">
                    </div>
                    <div class="form-group">
                        <label for="confirmar">Confirmar nueva contraseña</label>
                        <input type="password" id="confirmar" name="confirmar"
                               placeholder="Repite la nueva contraseña">
                    </div>
                </div>
            </div>

            <div style="max-width:800px; margin-top:1.2rem;">
                <button type="submit" class="btn-submit" style="max-width:220px;">
                    <i class="fas fa-save"></i> Guardar cambios
                </button>
            </div>
        </form>
    </main>

    <script>
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

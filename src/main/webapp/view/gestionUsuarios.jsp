<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Usuario" %>
<%
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    String error   = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Gestión de Usuarios — Tienda Don Pedro</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body { display: flex; min-height: 100vh; font-family: 'Segoe UI', system-ui, sans-serif; background: #f1f5f9; }

        /* ── SIDEBAR ── */
        .sidebar {
            width: 230px; background: #1e293b; min-height: 100vh;
            display: flex; flex-direction: column;
            position: fixed; top: 0; left: 0; height: 100vh;
            overflow-y: auto; z-index: 100; transition: transform 0.3s;
        }
        .sidebar__brand { padding: 1.4rem 1.2rem; border-bottom: 1px solid rgba(255,255,255,0.08); }
        .sidebar__brand-title { color: #f8fafc; font-size: 1rem; font-weight: 700; }
        .sidebar__brand-sub   { color: #64748b; font-size: 0.72rem; margin-top: 2px; }
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
        .sidebar__footer { margin-top: auto; padding: 1rem 1.2rem; border-top: 1px solid rgba(255,255,255,0.06); }
        .sidebar__user { color: #94a3b8; font-size: 0.8rem; }

        /* ── MAIN ── */
        .main { margin-left: 230px; flex: 1; padding: 2rem 2.5rem; }
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.8rem; flex-wrap: wrap; gap: 1rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub   { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── ALERT ── */
        .alert-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626;
            border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem;
            font-size: 0.875rem;
        }

        /* ── BOTÓN ── */
        .btn-primary {
            display: inline-flex; align-items: center; gap: 0.4rem;
            background: #22c55e; color: #fff; text-decoration: none;
            padding: 0.55rem 1.2rem; border-radius: 8px; font-size: 0.88rem;
            font-weight: 600; transition: background 0.2s;
        }
        .btn-primary:hover { background: #16a34a; }

        /* ── TABLA ── */
        .table-card {
            background: #fff; border-radius: 12px;
            box-shadow: 0 1px 6px rgba(0,0,0,0.06); overflow: hidden;
        }
        .table-toolbar {
            padding: 1rem 1.2rem; border-bottom: 1px solid #f1f5f9;
            display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;
        }
        .search-box {
            display: flex; align-items: center; gap: 0.5rem;
            background: #f8fafc; border: 1px solid #e2e8f0;
            border-radius: 8px; padding: 0.4rem 0.8rem; flex: 1; max-width: 300px;
        }
        .search-box input { border: none; background: transparent; outline: none; width: 100%; font-size: 0.85rem; }
        .search-box i { color: #94a3b8; font-size: 0.8rem; }

        table { width: 100%; border-collapse: collapse; }
        th {
            background: #f8fafc; padding: 0.75rem 1rem;
            text-align: left; font-size: 0.78rem; font-weight: 600;
            color: #64748b; text-transform: uppercase; letter-spacing: 0.05em;
            border-bottom: 1px solid #e2e8f0;
        }
        td { padding: 0.85rem 1rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #fafafa; }

        .avatar {
            width: 34px; height: 34px; border-radius: 50%;
            display: inline-flex; align-items: center; justify-content: center;
            font-weight: 700; font-size: 0.78rem; color: #fff;
            background: #3b82f6; margin-right: 0.5rem; vertical-align: middle;
        }
        .badge-rol {
            display: inline-block; padding: 0.22rem 0.65rem;
            border-radius: 20px; font-size: 0.72rem; font-weight: 700;
        }
        .badge-rol--admin    { background: #fef3c7; color: #d97706; }
        .badge-rol--empleado { background: #e0f2fe; color: #0284c7; }

        .btn-edit { background: #3b82f6; color: #fff; }
        .btn-del  { background: #ef4444; color: #fff; }
        .btn-edit, .btn-del {
            display: inline-flex; align-items: center; gap: 0.3rem;
            padding: 0.32rem 0.7rem; border-radius: 6px; font-size: 0.75rem;
            font-weight: 600; text-decoration: none; border: none; cursor: pointer;
            transition: opacity 0.15s;
        }
        .btn-edit:hover, .btn-del:hover { opacity: 0.85; }
        .td-acciones { white-space: nowrap; }

        .empty-row td { text-align: center; padding: 3.5rem; color: #94a3b8; }
        .empty-row i  { font-size: 2.5rem; display: block; margin-bottom: 0.5rem; }

        /* ── MODAL ── */
        .modal {
            display: none; position: fixed; inset: 0;
            background: rgba(0,0,0,0.45); z-index: 200;
            align-items: center; justify-content: center;
        }
        .modal:target { display: flex; }
        .modal__box {
            background: #fff; border-radius: 12px; padding: 1.6rem;
            width: 90%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.16);
        }
        .modal__title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.2rem; }
        .modal__form  { display: flex; flex-direction: column; gap: 0.9rem; }
        .modal__group { display: flex; flex-direction: column; gap: 0.3rem; }
        .modal__label { font-size: 0.8rem; font-weight: 600; color: #475569; }
        .modal__input, .modal__select {
            padding: 0.5rem 0.75rem; border: 1px solid #e2e8f0;
            border-radius: 6px; font-size: 0.875rem; outline: none;
            transition: border-color 0.2s;
        }
        .modal__input:focus, .modal__select:focus { border-color: #3b82f6; }
        .modal__actions { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 0.5rem; }
        .btn-save   { background: #3b82f6; color: #fff; padding: 0.5rem 1.1rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.875rem; font-weight: 600; }
        .btn-cancel { background: #f1f5f9; color: #475569; padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; font-size: 0.875rem; font-weight: 600; }
        .btn-danger { background: #ef4444; color: #fff; padding: 0.5rem 1.1rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.875rem; font-weight: 600; }

        /* ── RESPONSIVE ── */
        .hamburger-btn {
            display: none; position: fixed; top: 0.8rem; left: 0.8rem;
            z-index: 150; background: #1e293b; color: #fff;
            border: none; border-radius: 8px; padding: 0.55rem 0.75rem;
            font-size: 1rem; cursor: pointer;
        }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
        }
        .sidebar__section--cuenta { border-top: 1px solid rgba(255,255,255,0.08); margin-top: 0.5rem; }
        .sidebar__user-card { display: flex; align-items: center; gap: 0.65rem; padding: 0.4rem 1.2rem 0.6rem; }
        .sidebar__user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: #fff; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; }
        .sidebar__user-name { color: #e2e8f0; font-size: 0.82rem; font-weight: 600; line-height: 1.3; }
        .sidebar__user-role { color: #64748b; font-size: 0.7rem; }
        .sidebar__link--logout { color: #f87171 !important; }
        .sidebar__link--logout:hover { color: #fff !important; background: rgba(239,68,68,0.12) !important; border-left-color: #ef4444 !important; }
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
            <a href="<%= ctx %>/ProductoControlador?accion=stock" class="sidebar__link">
                <i class="fas fa-chart-bar"></i> Control de stock
            </a>
        </div>
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
            <span class="sidebar__label">Clientes y Deudas</span>
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link">
                <i class="fas fa-users"></i> Ver / Editar clientes
            </a>
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
            </a>
        </div>
        <div class="sidebar__section">
            <span class="sidebar__label">Sistema</span>
            <a href="<%= ctx %>/UsuarioControlador" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-user-cog"></i> Gestión de usuarios
            </a>
        </div>
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
            <a href="<%= ctx %>/PerfilControlador" class="sidebar__link">
                <i class="fas fa-user-circle"></i> Mi perfil
            </a>
            <a href="<%= ctx %>/LogoutControlador" class="sidebar__link sidebar__link--logout">
                <i class="fas fa-sign-out-alt"></i> Cerrar sesión
            </a>
        </div>
    </aside>

    <main class="main">
        <div class="page-header">
            <div>
                <h1 class="page-header__title">Gestión de Usuarios</h1>
                <p class="page-header__sub">
                    <%= usuarios != null ? usuarios.size() : 0 %> usuario(s) del sistema
                </p>
            </div>
            <a href="<%= ctx %>/UsuarioControlador?accion=nuevo" class="btn-primary">
                <i class="fas fa-user-plus"></i> Nuevo usuario
            </a>
        </div>

        <% if (error != null) { %>
        <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>

        <div class="table-card">
            <div class="table-toolbar">
                <div class="search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" id="buscador" placeholder="Buscar por nombre..." oninput="filtrar()">
                </div>
            </div>
            <table id="tablaUsuarios">
                <thead>
                    <tr>
                        <th>Usuario</th>
                        <th>Email</th>
                        <th>Cédula</th>
                        <th>Rol</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (usuarios == null || usuarios.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="5"><i class="fas fa-users-slash"></i>No hay usuarios registrados.</td>
                    </tr>
                    <% } else { for (Usuario u : usuarios) {
                        String inicial = u.getNombre() != null && !u.getNombre().isEmpty()
                            ? String.valueOf(u.getNombre().charAt(0)).toUpperCase() : "?";
                        boolean esElMismo = (usuarioActual != null && usuarioActual.getIdUsuario() == u.getIdUsuario());
                    %>
                    <tr data-nombre="<%= u.getNombre().toLowerCase() %>">
                        <td>
                            <span class="avatar"><%= inicial %></span>
                            <%= u.getNombre() %> <%= u.getApellido() != null ? u.getApellido() : "" %>
                            <% if (esElMismo) { %> <span style="color:#64748b;font-size:0.72rem;">(tú)</span><% } %>
                        </td>
                        <td><%= u.getEmail() %></td>
                        <td><%= u.getCedula() != null ? u.getCedula() : "—" %></td>
                        <td>
                            <span class="badge-rol <%= u.getIdRol() == 1 ? "badge-rol--admin" : "badge-rol--empleado" %>">
                                <%= u.getIdRol() == 1 ? "Admin" : "Empleado" %>
                            </span>
                        </td>
                        <td class="td-acciones">
                            <a href="#modal-editar-<%= u.getIdUsuario() %>" class="btn-edit">
                                <i class="fas fa-edit"></i> Editar
                            </a>
                            <% if (!esElMismo) { %>
                            <a href="#modal-eliminar-<%= u.getIdUsuario() %>" class="btn-del">
                                <i class="fas fa-trash"></i> Eliminar
                            </a>
                            <% } %>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </main>

    <!-- MODALES EDITAR -->
    <% if (usuarios != null) { for (Usuario u : usuarios) { %>
    <div id="modal-editar-<%= u.getIdUsuario() %>" class="modal">
        <div class="modal__box">
            <div class="modal__title"><i class="fas fa-user-edit"></i> Editar usuario</div>
            <form class="modal__form" action="<%= ctx %>/UsuarioControlador" method="post">
                <input type="hidden" name="accion"     value="actualizar">
                <input type="hidden" name="idUsuario"  value="<%= u.getIdUsuario() %>">
                <div class="modal__group">
                    <label class="modal__label">Nombre</label>
                    <input type="text" name="nombre" class="modal__input" value="<%= u.getNombre() %>" required>
                </div>
                <div class="modal__group">
                    <label class="modal__label">Apellido</label>
                    <input type="text" name="apellido" class="modal__input" value="<%= u.getApellido() != null ? u.getApellido() : "" %>">
                </div>
                <div class="modal__group">
                    <label class="modal__label">Email</label>
                    <input type="email" name="email" class="modal__input" value="<%= u.getEmail() %>" required>
                </div>
                <div class="modal__group">
                    <label class="modal__label">Rol</label>
                    <select name="idRol" class="modal__select">
                        <option value="1" <%= u.getIdRol() == 1 ? "selected" : "" %>>Admin</option>
                        <option value="2" <%= u.getIdRol() == 2 ? "selected" : "" %>>Empleado</option>
                    </select>
                </div>
                <div class="modal__actions">
                    <a href="#" class="btn-cancel">Cancelar</a>
                    <button type="submit" class="btn-save">Guardar cambios</button>
                </div>
            </form>
        </div>
    </div>
    <% } } %>

    <!-- MODALES ELIMINAR -->
    <% if (usuarios != null) { for (Usuario u : usuarios) {
        boolean esElMismo = (usuarioActual != null && usuarioActual.getIdUsuario() == u.getIdUsuario());
        if (!esElMismo) { %>
    <div id="modal-eliminar-<%= u.getIdUsuario() %>" class="modal">
        <div class="modal__box">
            <div class="modal__title" style="color:#ef4444;"><i class="fas fa-exclamation-triangle"></i> Confirmar eliminación</div>
            <p style="font-size:0.875rem;color:#475569;margin-bottom:1.2rem;">
                Se eliminará al usuario <strong><%= u.getNombre() %> <%= u.getApellido() != null ? u.getApellido() : "" %></strong>
                de forma permanente. Esta acción no se puede deshacer.
            </p>
            <form action="<%= ctx %>/UsuarioControlador" method="post">
                <input type="hidden" name="accion"    value="eliminar">
                <input type="hidden" name="idUsuario" value="<%= u.getIdUsuario() %>">
                <div class="modal__actions">
                    <a href="#" class="btn-cancel">Cancelar</a>
                    <button type="submit" class="btn-danger"><i class="fas fa-trash"></i> Eliminar</button>
                </div>
            </form>
        </div>
    </div>
    <% } } } %>

    <script>
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
        function filtrar() {
            const q = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaUsuarios tbody tr[data-nombre]').forEach(tr => {
                tr.style.display = tr.dataset.nombre.includes(q) ? '' : 'none';
            });
        }
    </script>
</body>
</html>

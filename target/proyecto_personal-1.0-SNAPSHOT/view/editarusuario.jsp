<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Usuario" %>
<%
    List<Usuario> clientes = (List<Usuario>) request.getAttribute("clientes");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Clientes — Tienda Don Pedro</title>
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

        /* ── MAIN ── */
        .main { margin-left: 230px; flex: 1; padding: 2rem 2.5rem; }
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.8rem; flex-wrap: wrap; gap: 1rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub   { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        .alert-error { background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626; border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; }

        /* ── TABLE CARD ── */
        .table-card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden; }
        .table-toolbar { display: flex; align-items: center; gap: 1rem; padding: 1rem 1.2rem; border-bottom: 1px solid #f1f5f9; }
        .search-box { display: flex; align-items: center; gap: 0.5rem; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 0.45rem 0.8rem; background: #f8fafc; flex: 1; max-width: 320px; transition: border-color 0.18s; }
        .search-box:focus-within { border-color: #3b82f6; background: #fff; }
        .search-box i { color: #94a3b8; font-size: 0.85rem; }
        .search-box input { border: none; outline: none; background: transparent; font-size: 0.875rem; color: #1e293b; width: 100%; }
        .search-box input::placeholder { color: #94a3b8; }

        table { width: 100%; border-collapse: collapse; }
        thead tr { background: #f8fafc; }
        th { padding: 0.8rem 1.2rem; text-align: left; font-size: 0.75rem; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #f1f5f9; }
        td { padding: 0.85rem 1.2rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; vertical-align: middle; }
        tbody tr { transition: background 0.15s; }
        tbody tr:hover { background: #f8fafc; }
        tbody tr:last-child td { border-bottom: none; }
        .td-name  { font-weight: 600; color: #1e293b; }
        .td-email { color: #64748b; font-size: 0.82rem; }
        .td-cedula{ color: #94a3b8; font-size: 0.8rem; font-family: monospace; }

        /* ── AVATAR ── */
        .avatar { width: 36px; height: 36px; border-radius: 50%; background: #eff6ff; color: #3b82f6; display: inline-flex; align-items: center; justify-content: center; font-size: 0.85rem; font-weight: 700; margin-right: 0.5rem; }

        /* ── ACTION BUTTONS ── */
        .btn-edit { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.35rem 0.7rem; background: #eff6ff; color: #3b82f6; border: none; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; text-decoration: none; transition: background 0.18s; }
        .btn-edit:hover { background: #3b82f6; color: #fff; }
        .btn-del  { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.35rem 0.7rem; background: #fef2f2; color: #ef4444; border: none; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; text-decoration: none; transition: background 0.18s; }
        .btn-del:hover  { background: #ef4444; color: #fff; }

        /* ── EMPTY ── */
        .empty-row td { text-align: center; padding: 3.5rem; color: #94a3b8; }
        .empty-row i  { font-size: 2.5rem; display: block; margin-bottom: 0.5rem; }

        /* ── MODAL ── */
        .modal { display: none; position: fixed; inset: 0; background: rgba(15,23,42,0.55); z-index: 200; align-items: center; justify-content: center; padding: 1rem; }
        .modal:target { display: flex; }
        .modal__box { background: #fff; border-radius: 16px; padding: 2rem; width: 100%; max-width: 420px; box-shadow: 0 20px 50px rgba(0,0,0,0.2); }
        .modal__title { font-size: 1.1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }
        .modal__title i { color: #3b82f6; }
        .modal__form { display: flex; flex-direction: column; gap: 1rem; }
        .modal__group { display: flex; flex-direction: column; gap: 0.35rem; }
        .modal__label { font-size: 0.8rem; font-weight: 600; color: #374151; }
        .modal__input { padding: 0.6rem 0.85rem; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 0.875rem; color: #1e293b; background: #f8fafc; font-family: inherit; width: 100%; }
        .modal__input:focus { outline: none; border-color: #3b82f6; background: #fff; box-shadow: 0 0 0 3px rgba(59,130,246,0.08); }
        .modal__actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
        .btn-save   { flex: 1; padding: 0.65rem; background: #3b82f6; color: #fff; border: none; border-radius: 8px; font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: background 0.18s; }
        .btn-save:hover { background: #2563eb; }
        .btn-cancel { flex: 1; padding: 0.65rem; background: #f1f5f9; color: #475569; border: none; border-radius: 8px; font-size: 0.9rem; font-weight: 600; cursor: pointer; text-decoration: none; text-align: center; transition: background 0.18s; }
        .btn-cancel:hover { background: #e2e8f0; }

        /* ── MODAL ELIMINAR ── */
        .modal-del__icon { width: 52px; height: 52px; border-radius: 50%; background: #fef2f2; color: #ef4444; font-size: 1.3rem; display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem; }
        .modal-del__msg { color: #64748b; font-size: 0.9rem; text-align: center; margin-bottom: 1.5rem; line-height: 1.5; }
        .modal-del__title { font-size: 1.1rem; font-weight: 700; color: #1e293b; text-align: center; margin-bottom: 0.5rem; }
        .btn-danger { flex: 1; padding: 0.65rem; background: #ef4444; color: #fff; border: none; border-radius: 8px; font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: background 0.18s; }
        .btn-danger:hover { background: #dc2626; }

        /* ── HAMBURGER ── */
        .hamburger-btn { display: none; position: fixed; top: 0.8rem; left: 0.8rem; z-index: 150; background: #1e293b; color: #fff; border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer; }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            th:nth-child(3), td:nth-child(3),
            th:nth-child(4), td:nth-child(4) { display: none; }
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
            <% if (esAdmin) { %>
            <a href="<%= ctx %>/ReporteControlador" class="sidebar__link">
                <i class="fas fa-chart-line"></i> Reportes
            </a>
            <% } %>
        </div>
        <div class="sidebar__section">
            <span class="sidebar__label">Clientes</span>
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-users"></i> Ver / Editar clientes
            </a>
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
            </a>
        </div>
    </aside>

    <main class="main">
        <div class="page-header">
            <div>
                <h1 class="page-header__title">Clientes</h1>
                <p class="page-header__sub"><%= clientes != null ? clientes.size() : 0 %> cliente(s) registrado(s)</p>
            </div>
        </div>

        <% if (error != null) { %>
        <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>

        <div class="table-card">
            <div class="table-toolbar">
                <div class="search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" id="buscador" placeholder="Buscar cliente..." oninput="filtrar()">
                </div>
            </div>
            <table id="tablaClientes">
                <thead>
                    <tr>
                        <th>Cliente</th>
                        <th>Email</th>
                        <th>Cédula</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (clientes == null || clientes.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="4"><i class="fas fa-users"></i>No hay clientes registrados.</td>
                    </tr>
                    <% } else {
                        for (Usuario c : clientes) {
                            String inicial = c.getNombre() != null && !c.getNombre().isEmpty()
                                ? String.valueOf(c.getNombre().charAt(0)).toUpperCase() : "?";
                    %>
                    <tr data-nombre="<%= c.getNombre().toLowerCase() %>">
                        <td class="td-name">
                            <span class="avatar"><%= inicial %></span>
                            <%= c.getNombre() %> <%= c.getApellido() != null ? c.getApellido() : "" %>
                        </td>
                        <td class="td-email"><%= c.getEmail() %></td>
                        <td class="td-cedula"><%= c.getCedula() != null ? c.getCedula() : "—" %></td>
                        <td>
                            <a href="#modal-editar-<%= c.getIdUsuario() %>" class="btn-edit">
                                <i class="fas fa-pencil"></i> Editar
                            </a>
                            <a href="#modal-eliminar-<%= c.getIdUsuario() %>" class="btn-del">
                                <i class="fas fa-trash"></i> Eliminar
                            </a>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </main>

    <!-- MODALES EDITAR -->
    <% if (clientes != null) { for (Usuario c : clientes) { %>
    <div id="modal-editar-<%= c.getIdUsuario() %>" class="modal">
        <div class="modal__box">
            <div class="modal__title"><i class="fas fa-user-edit"></i> Editar cliente</div>
            <form class="modal__form" action="<%= ctx %>/ClienteControlador" method="post">
                <input type="hidden" name="accion"    value="actualizar">
                <input type="hidden" name="idUsuario" value="<%= c.getIdUsuario() %>">
                <div class="modal__group">
                    <label class="modal__label">Nombre</label>
                    <input type="text" name="nombre" class="modal__input" value="<%= c.getNombre() %>" required>
                </div>
                <div class="modal__group">
                    <label class="modal__label">Apellido</label>
                    <input type="text" name="apellido" class="modal__input" value="<%= c.getApellido() != null ? c.getApellido() : "" %>">
                </div>
                <div class="modal__group">
                    <label class="modal__label">Email</label>
                    <input type="email" name="email" class="modal__input" value="<%= c.getEmail() %>" required>
                </div>
                <div class="modal__actions">
                    <button type="submit" class="btn-save">Guardar cambios</button>
                    <a href="#" class="btn-cancel">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
    <% } } %>

    <!-- MODALES ELIMINAR -->
    <% if (clientes != null) { for (Usuario c : clientes) { %>
    <div id="modal-eliminar-<%= c.getIdUsuario() %>" class="modal">
        <div class="modal__box">
            <div class="modal-del__icon"><i class="fas fa-trash"></i></div>
            <div class="modal-del__title">¿Eliminar cliente?</div>
            <p class="modal-del__msg">
                Se eliminará a <strong><%= c.getNombre() %> <%= c.getApellido() != null ? c.getApellido() : "" %></strong> de forma permanente.
            </p>
            <div class="modal__actions">
                <form action="<%= ctx %>/ClienteControlador" method="post" style="flex:1; display:flex;">
                    <input type="hidden" name="accion"    value="eliminar">
                    <input type="hidden" name="idUsuario" value="<%= c.getIdUsuario() %>">
                    <button type="submit" class="btn-danger" style="width:100%;">Sí, eliminar</button>
                </form>
                <a href="#" class="btn-cancel">Cancelar</a>
            </div>
        </div>
    </div>
    <% } } %>

    <script>
        function filtrar() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaClientes tbody tr[data-nombre]').forEach(tr => {
                tr.style.display = tr.dataset.nombre.includes(texto) ? '' : 'none';
            });
        }
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

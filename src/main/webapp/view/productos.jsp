<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Usuario" %>
<%
    // Recibimos la lista de productos activos que el controlador puso en el request
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    // Recibimos la lista de productos desactivados (activo = false) para mostrarla al admin
    List<Producto> productosInactivos = (List<Producto>) request.getAttribute("productosInactivos");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
    request.setAttribute("_paginaActiva", "productos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Productos — Tienda Don Pedro</title>
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

        /* ── MAIN ── */
        .main { margin-left: 230px; flex: 1; padding: 2rem 2.5rem; }
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }
        .alert-error { border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626; }

        /* ── GRID ── */
        .grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1.5rem;
        }

        /* ── CARD ── */
        .card {
            background: #fff; border-radius: 14px; overflow: hidden;
            box-shadow: 0 1px 4px rgba(0,0,0,0.06);
            transition: transform 0.2s, box-shadow 0.2s;
            display: flex; flex-direction: column;
        }
        .card:hover { transform: translateY(-4px); box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
        .card__img { width: 100%; height: 175px; object-fit: cover; display: block; background: #f1f5f9; }
        .card__body { padding: 1.1rem; flex: 1; display: flex; flex-direction: column; }
        .card__name {
            font-size: 0.98rem; font-weight: 700; color: #1e293b;
            margin-bottom: 0.3rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
        }
        .card__desc {
            font-size: 0.8rem; color: #64748b; line-height: 1.45;
            margin-bottom: 0.9rem; flex: 1;
            display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
            min-height: 2.2em;
        }
        .card__footer { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
        .card__price { font-size: 1.05rem; font-weight: 700; color: #22c55e; white-space: nowrap; }
        .card__actions { display: flex; gap: 0.4rem; }

        /* ── BUTTONS ── */
        .btn {
            display: inline-flex; align-items: center; gap: 0.3rem;
            padding: 0.38rem 0.7rem; border-radius: 7px;
            font-size: 0.76rem; font-weight: 500;
            text-decoration: none; cursor: pointer; border: none;
            transition: background 0.18s, color 0.18s;
        }
        .btn--edit { background: #eff6ff; color: #3b82f6; }
        .btn--edit:hover { background: #3b82f6; color: #fff; }
        .btn--delete { background: #fef2f2; color: #ef4444; }
        .btn--delete:hover { background: #ef4444; color: #fff; }
        .btn--confirm { background: #ef4444; color: #fff; padding: 0.55rem 1.4rem; font-size: 0.875rem; border-radius: 8px; }
        .btn--confirm:hover { background: #dc2626; }
        .btn--cancel-modal {
            background: #f1f5f9; color: #475569;
            padding: 0.55rem 1.4rem; font-size: 0.875rem;
            border-radius: 8px; text-decoration: none; cursor: pointer; border: none;
        }
        .btn--cancel-modal:hover { background: #e2e8f0; }

        /* ── EMPTY STATE ── */
        .empty { grid-column: 1 / -1; text-align: center; padding: 5rem 2rem; color: #94a3b8; }
        .empty i { font-size: 3.5rem; margin-bottom: 1rem; display: block; }
        .empty p { font-size: 1rem; }

        /* ── FAB ── */
        .fab {
            position: fixed; bottom: 2rem; right: 2rem;
            width: 54px; height: 54px; border-radius: 50%;
            background: #22c55e; color: #fff;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.5rem; text-decoration: none;
            box-shadow: 0 4px 16px rgba(34,197,94,0.45);
            transition: transform 0.2s, box-shadow 0.2s; z-index: 50;
        }
        .fab:hover { transform: scale(1.1); box-shadow: 0 6px 22px rgba(34,197,94,0.55); }

        /* ── MODAL ── */
        .modal {
            display: none; position: fixed; inset: 0;
            background: rgba(15,23,42,0.55); z-index: 200;
            align-items: center; justify-content: center;
            padding: 1rem;
        }
        .modal:target { display: flex; }
        .modal__box {
            background: #fff; border-radius: 16px; padding: 2rem 1.8rem;
            width: 100%; max-width: 360px; text-align: center;
            box-shadow: 0 20px 50px rgba(0,0,0,0.2);
        }
        .modal__icon {
            width: 56px; height: 56px; border-radius: 50%;
            background: #fef2f2; color: #ef4444;
            font-size: 1.4rem; display: flex;
            align-items: center; justify-content: center; margin: 0 auto 1rem;
        }
        .modal__title { font-size: 1.1rem; font-weight: 700; color: #1e293b; margin-bottom: 0.5rem; }
        .modal__msg { color: #64748b; font-size: 0.875rem; line-height: 1.5; margin-bottom: 1.5rem; }
        .modal__actions { display: flex; gap: 0.75rem; justify-content: center; }

        /* ── SECCIÓN INACTIVOS ── */
        .section-inactivos {
            margin-top: 2.5rem;
        }
        .section-inactivos__title {
            font-size: 1rem; font-weight: 700; color: #64748b;
            margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem;
        }
        .card--inactivo { opacity: 0.6; }
        .card--inactivo:hover { opacity: 1; }
        .btn--restore { background: #f0fdf4; color: #16a34a; }
        .btn--restore:hover { background: #16a34a; color: #fff; }

        /* ── HAMBURGER BUTTON ── */
        .hamburger-btn {
            display: none; position: fixed; top: 0.8rem; left: 0.8rem;
            z-index: 150; background: #1e293b; color: #fff;
            border: none; border-radius: 8px; padding: 0.55rem 0.75rem;
            font-size: 1rem; cursor: pointer;
        }
        .overlay {
            display: none; position: fixed; inset: 0;
            background: rgba(0,0,0,0.45); z-index: 90;
        }

        /* ── RESPONSIVE ── */
        @media (max-width: 1100px) { .grid { grid-template-columns: repeat(2, 1fr); } }
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; gap: 0.4rem; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            .grid { grid-template-columns: repeat(2, 1fr); gap: 1rem; }
        }
        @media (max-width: 480px) { .grid { grid-template-columns: 1fr; } }
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

    <button class="hamburger-btn" onclick="toggleSidebar()">
        <i class="fas fa-bars"></i>
    </button>
    <div class="overlay" id="overlay" onclick="toggleSidebar()"></div>

    <jsp:include page="sidebar.jsp" />

    <main class="main">
        <div class="page-header">
            <div>
                <h1 class="page-header__title">Productos</h1>
                <p class="page-header__sub"><%= productos != null ? productos.size() : 0 %> producto(s) en catálogo</p>
            </div>
        </div>

        <% if (error != null) { %>
        <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>

        <div class="grid">
            <% if (productos == null || productos.isEmpty()) { %>
            <div class="empty">
                <i class="fas fa-box-open"></i>
                <p>No hay productos registrados aún.</p>
            </div>
            <% } else {
                for (Producto p : productos) { %>
            <article class="card">
                <img class="card__img"
                     src="<%= (p.getImagenUrl() != null && !p.getImagenUrl().isEmpty())
                              ? ctx + "/uploads/productos/" + p.getImagenUrl()
                              : "https://placehold.co/400x200/e2e8f0/94a3b8?text=Producto" %>"
                     alt="<%= p.getNombre() %>"
                     onerror="this.src='https://placehold.co/400x200/e2e8f0/94a3b8?text=Producto'">
                <div class="card__body">
                    <h2 class="card__name" title="<%= p.getNombre() %>"><%= p.getNombre() %></h2>
                    <p class="card__desc"><%= p.getDescripcion() != null && !p.getDescripcion().isEmpty() ? p.getDescripcion() : "Sin descripción" %></p>
                    <div class="card__footer">
                        <span class="card__price">$&nbsp;<%= String.format("%,.0f", p.getPrecio()) %></span>
                        <% if (esAdmin) { %>
                        <div class="card__actions">
                            <a href="<%= ctx %>/ProductoControlador?accion=editar&id=<%= p.getIdProducto() %>"
                               class="btn btn--edit" title="Editar">
                                <i class="fas fa-pencil"></i> Editar
                            </a>
                            <a href="#modal-del-<%= p.getIdProducto() %>"
                               class="btn btn--delete" title="Eliminar">
                                <i class="fas fa-trash"></i>
                            </a>
                        </div>
                        <% } %>
                    </div>
                </div>
            </article>
            <% } } %>
        </div>
        <%-- SECCIÓN PRODUCTOS DESACTIVADOS: solo visible si el usuario es admin Y hay productos inactivos --%>
        <%-- esAdmin: verifica que el rol sea 1 (administrador) --%>
        <%-- productosInactivos != null && !productosInactivos.isEmpty(): evita mostrar la sección si no hay ninguno --%>
        <% if (esAdmin && productosInactivos != null && !productosInactivos.isEmpty()) { %>
        <div class="section-inactivos">
            <%-- Título de la sección con el conteo de productos desactivados --%>
            <p class="section-inactivos__title">
                <i class="fas fa-box"></i> Productos desactivados (<%= productosInactivos.size() %>)
            </p>
            <%-- Usamos el mismo grid de tarjetas del catálogo activo para mantener el diseño uniforme --%>
            <div class="grid">
                <%-- Recorremos cada producto desactivado para mostrar su tarjeta --%>
                <% for (Producto p : productosInactivos) { %>
                <%-- card--inactivo aplica opacidad reducida para que visualmente se note que está desactivado --%>
                <article class="card card--inactivo">
                    <%-- Mostramos la imagen del producto; si no tiene imagen, mostramos un placeholder --%>
                    <img class="card__img"
                         src="<%= (p.getImagenUrl() != null && !p.getImagenUrl().isEmpty())
                                  ? ctx + "/uploads/productos/" + p.getImagenUrl()
                                  : "https://placehold.co/400x200/e2e8f0/94a3b8?text=Producto" %>"
                         alt="<%= p.getNombre() %>"
                         <%-- onerror: si la imagen no carga (archivo eliminado), muestra el placeholder automáticamente --%>
                         onerror="this.src='https://placehold.co/400x200/e2e8f0/94a3b8?text=Producto'">
                    <div class="card__body">
                        <%-- Nombre del producto desactivado --%>
                        <h2 class="card__name" title="<%= p.getNombre() %>"><%= p.getNombre() %></h2>
                        <%-- Descripción del producto; si no tiene, muestra "Sin descripción" --%>
                        <p class="card__desc"><%= p.getDescripcion() != null && !p.getDescripcion().isEmpty() ? p.getDescripcion() : "Sin descripción" %></p>
                        <div class="card__footer">
                            <%-- Precio del producto (se guarda aunque esté desactivado) --%>
                            <span class="card__price">$&nbsp;<%= String.format("%,.0f", p.getPrecio()) %></span>
                            <%-- Formulario de restaurar: envía POST al controlador con accion=restaurar e id del producto --%>
                            <form action="<%= ctx %>/ProductoControlador" method="post" style="display:inline">
                                <%-- Campo oculto que le dice al controlador qué acción ejecutar --%>
                                <input type="hidden" name="accion" value="restaurar">
                                <%-- Campo oculto con el ID del producto a restaurar --%>
                                <input type="hidden" name="id" value="<%= p.getIdProducto() %>">
                                <%-- Botón que envía el formulario: llama a productoDAO.activar(id) en el controlador --%>
                                <button type="submit" class="btn btn--restore">
                                    <i class="fas fa-rotate-left"></i> Restaurar
                                </button>
                            </form>
                        </div>
                    </div>
                </article>
                <% } %>
            </div>
        </div>
        <% } %>

    </main>

    <% if (esAdmin) { %>
    <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="fab" title="Nuevo producto">
        <i class="fas fa-plus"></i>
    </a>
    <% } %>

    <!-- MODALES DE ELIMINACIÓN -->
    <% if (productos != null) {
        for (Producto p : productos) { %>
    <div id="modal-del-<%= p.getIdProducto() %>" class="modal">
        <div class="modal__box">
            <div class="modal__icon"><i class="fas fa-trash"></i></div>
            <h3 class="modal__title">¿Eliminar producto?</h3>
            <p class="modal__msg">
                Se desactivará <strong>"<%= p.getNombre() %>"</strong> y dejará de aparecer en el catálogo.
                Puedes restaurarlo desde la sección de productos desactivados.
            </p>
            <div class="modal__actions">
                <form action="<%= ctx %>/ProductoControlador" method="post" style="display:inline">
                    <input type="hidden" name="accion" value="eliminar">
                    <input type="hidden" name="id" value="<%= p.getIdProducto() %>">
                    <button type="submit" class="btn btn--confirm">Sí, eliminar</button>
                </form>
                <a href="#" class="btn--cancel-modal">Cancelar</a>
            </div>
        </div>
    </div>
    <% } } %>

    <script>
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

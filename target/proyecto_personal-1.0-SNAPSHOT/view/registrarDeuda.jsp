<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Usuario" %>
<%
    List<Usuario> clientes = (List<Usuario>) request.getAttribute("clientes");
    String error  = (String) request.getAttribute("error");
    String ctx    = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Registrar Deuda — Tienda Don Pedro</title>
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
        .main {
            margin-left: 230px; flex: 1; padding: 3rem 2rem;
            display: flex; justify-content: center; align-items: flex-start;
        }

        /* ── CARD ── */
        .card {
            background: #fff; border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            padding: 2.5rem; width: 100%; max-width: 520px;
        }
        .card__back { display: inline-flex; align-items: center; gap: 0.5rem; color: #64748b; text-decoration: none; font-size: 0.85rem; margin-bottom: 1rem; transition: color 0.18s; }
        .card__back:hover { color: #1e293b; }
        .card__icon { width: 52px; height: 52px; border-radius: 14px; background: #fef2f2; color: #ef4444; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; margin-bottom: 1rem; }
        .card__title { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .card__subtitle { color: #64748b; font-size: 0.875rem; margin-top: 0.3rem; margin-bottom: 2rem; }

        /* ── ERROR ── */
        .alert-error { background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626; border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; }

        /* ── FORM ── */
        .form { display: flex; flex-direction: column; gap: 1.2rem; }
        .form__group { display: flex; flex-direction: column; gap: 0.4rem; }
        .form__label { font-size: 0.82rem; font-weight: 600; color: #374151; }
        .form__select, .form__input, .form__textarea {
            padding: 0.65rem 0.9rem; border: 1.5px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; color: #1e293b;
            background: #f8fafc; transition: border-color 0.18s; font-family: inherit; width: 100%;
        }
        .form__select:focus, .form__input:focus, .form__textarea:focus {
            outline: none; border-color: #ef4444; background: #fff;
            box-shadow: 0 0 0 3px rgba(239,68,68,0.08);
        }
        .form__textarea { resize: vertical; min-height: 80px; }

        /* ── MONTO BOX ── */
        .monto-box { position: relative; }
        .monto-prefix { position: absolute; left: 0.9rem; top: 50%; transform: translateY(-50%); color: #94a3b8; font-weight: 600; }
        .monto-box .form__input { padding-left: 1.8rem; }

        /* ── ACTIONS ── */
        .form__actions { display: flex; gap: 1rem; align-items: center; margin-top: 0.5rem; }
        .btn-submit {
            flex: 1; padding: 0.75rem; background: #ef4444; color: #fff;
            border: none; border-radius: 10px; font-size: 0.95rem; font-weight: 700;
            cursor: pointer; transition: background 0.18s, transform 0.15s;
            display: flex; align-items: center; justify-content: center; gap: 0.5rem;
        }
        .btn-submit:hover { background: #dc2626; transform: translateY(-1px); }
        .btn-cancel { color: #64748b; text-decoration: none; font-size: 0.875rem; transition: color 0.18s; white-space: nowrap; }
        .btn-cancel:hover { color: #1e293b; }

        /* ── INFO BOX ── */
        .info-box { background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; padding: 0.9rem 1rem; display: flex; gap: 0.6rem; align-items: flex-start; }
        .info-box i { color: #d97706; margin-top: 2px; flex-shrink: 0; }
        .info-box p { font-size: 0.82rem; color: #92400e; line-height: 1.5; }

        /* ── HAMBURGER ── */
        .hamburger-btn { display: none; position: fixed; top: 0.8rem; left: 0.8rem; z-index: 150; background: #1e293b; color: #fff; border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer; }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4.5rem; }
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
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link">
                <i class="fas fa-users"></i> Ver / Editar clientes
            </a>
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
            </a>
        </div>
    </aside>

    <main class="main">
        <div class="card">
            <a href="<%= ctx %>/DeudaControlador" class="card__back">
                <i class="fas fa-arrow-left"></i> Volver a deudores
            </a>
            <div class="card__icon"><i class="fas fa-file-invoice-dollar"></i></div>
            <h1 class="card__title">Registrar deuda</h1>
            <p class="card__subtitle">Registre manualmente una deuda pendiente para un cliente.</p>

            <% if (error != null) { %>
            <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>

            <form class="form" action="<%= ctx %>/DeudaControlador" method="post">
                <input type="hidden" name="accion" value="registrar">

                <div class="form__group">
                    <label class="form__label">Cliente *</label>
                    <select name="idCliente" class="form__select" required>
                        <option value="">-- Seleccionar cliente --</option>
                        <% if (clientes != null) {
                            for (Usuario c : clientes) { %>
                        <option value="<%= c.getIdUsuario() %>">
                            <%= c.getNombre() %> <%= c.getApellido() != null ? c.getApellido() : "" %>
                            <% if (c.getCedula() != null && !c.getCedula().isEmpty()) { %>
                            — C.C. <%= c.getCedula() %>
                            <% } %>
                        </option>
                        <% } } %>
                    </select>
                </div>

                <div class="form__group">
                    <label class="form__label">Monto de la deuda *</label>
                    <div class="monto-box">
                        <span class="monto-prefix">$</span>
                        <input type="number" name="montoPendiente" class="form__input"
                               placeholder="0" min="0.01" step="0.01" required>
                    </div>
                </div>

                <div class="form__group">
                    <label class="form__label">Descripción / motivo</label>
                    <textarea name="descripcion" class="form__textarea"
                              placeholder="Ej: Compra del 5 de marzo, mercancía al fiado..."></textarea>
                </div>

                <div class="info-box">
                    <i class="fas fa-info-circle"></i>
                    <p>
                        Esta deuda quedará registrada como pendiente en el módulo de deudores.
                        Podrá registrar abonos desde allí cuando el cliente pague.
                    </p>
                </div>

                <div class="form__actions">
                    <button type="submit" class="btn-submit">
                        <i class="fas fa-save"></i> Registrar deuda
                    </button>
                    <a href="<%= ctx %>/DeudaControlador" class="btn-cancel">Cancelar</a>
                </div>
            </form>
        </div>
    </main>

    <script>
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

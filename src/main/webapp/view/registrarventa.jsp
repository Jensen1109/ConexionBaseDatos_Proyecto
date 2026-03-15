<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Cliente, modelos.Usuario, modelos.MetodoPago" %>
<%
    List<Producto>   productos   = (List<Producto>)   request.getAttribute("productos");
    List<Cliente>    clientes    = (List<Cliente>)    request.getAttribute("clientes");
    List<MetodoPago> metodosPago = (List<MetodoPago>) request.getAttribute("metodosPago");
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
    <title>Registrar Venta — Tienda Don Pedro</title>
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
        .page-header { margin-bottom: 1.8rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub   { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── ERROR ── */
        .alert-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626;
            border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem;
            font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem;
        }

        /* ── LAYOUT GRID ── */
        .layout { display: grid; grid-template-columns: 1fr 320px; gap: 1.5rem; align-items: start; }

        /* ── CARD ── */
        .card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 1.5rem; }
        .card__title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.2rem; display: flex; align-items: center; gap: 0.5rem; }
        .card__title i { color: #3b82f6; }

        /* ── FORM FIELDS ── */
        .form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1rem; }
        .form-label { font-size: 0.8rem; font-weight: 600; color: #374151; }
        .form-select, .form-input {
            padding: 0.6rem 0.85rem; border: 1.5px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; color: #1e293b;
            background: #f8fafc; transition: border-color 0.18s; font-family: inherit; width: 100%;
        }
        .form-select:focus, .form-input:focus {
            outline: none; border-color: #3b82f6; background: #fff;
            box-shadow: 0 0 0 3px rgba(59,130,246,0.08);
        }

        /* ── CLIENTE SEARCH ── */
        .cliente-search-wrap { position: relative; }
        .cliente-dropdown {
            position: absolute; top: calc(100% + 4px); left: 0; right: 0;
            background: #fff; border: 1.5px solid #3b82f6; border-radius: 8px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.1); z-index: 50;
            max-height: 180px; overflow-y: auto; display: none;
        }
        .cliente-option {
            padding: 0.6rem 0.85rem; font-size: 0.85rem; cursor: pointer;
            border-bottom: 1px solid #f1f5f9; transition: background 0.15s;
        }
        .cliente-option:last-child { border-bottom: none; }
        .cliente-option:hover { background: #eff6ff; }
        .cliente-option .ced { color: #94a3b8; font-size: 0.75rem; margin-left: 0.4rem; }
        .cliente-seleccionado {
            display: none; align-items: center; gap: 0.5rem;
            padding: 0.55rem 0.85rem; background: #eff6ff;
            border: 1.5px solid #3b82f6; border-radius: 8px; font-size: 0.875rem;
        }
        .cliente-seleccionado span { flex: 1; font-weight: 600; color: #1e293b; }
        .btn-clear-cliente {
            background: none; border: none; color: #94a3b8; cursor: pointer;
            font-size: 0.8rem; padding: 0.1rem 0.3rem; border-radius: 4px;
        }
        .btn-clear-cliente:hover { color: #ef4444; }
        .btn-nuevo-cliente {
            display: inline-flex; align-items: center; gap: 0.35rem;
            background: none; border: 1px dashed #94a3b8; color: #64748b;
            border-radius: 7px; padding: 0.4rem 0.8rem; font-size: 0.78rem;
            cursor: pointer; margin-top: 0.4rem; transition: all 0.18s;
        }
        .btn-nuevo-cliente:hover { border-color: #22c55e; color: #16a34a; }
        .nuevo-cliente-panel {
            display: none; background: #f8fafc; border: 1px solid #e2e8f0;
            border-radius: 10px; padding: 1rem; margin-top: 0.5rem;
        }
        .nuevo-cliente-panel .panel-title {
            font-size: 0.8rem; font-weight: 700; color: #475569;
            margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.35rem;
        }
        .nuevo-cliente-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.6rem; }
        @media (max-width: 600px) { .nuevo-cliente-grid { grid-template-columns: 1fr; } }

        /* ── AGREGAR PRODUCTO ── */
        .add-row { display: flex; gap: 0.75rem; align-items: flex-end; margin-bottom: 1rem; flex-wrap: wrap; }
        .add-row .form-group { margin-bottom: 0; flex: 1; min-width: 160px; }
        .qty-group { width: 85px; flex-shrink: 0; }
        .btn-add {
            display: inline-flex; align-items: center; gap: 0.4rem;
            padding: 0.6rem 1rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 8px; font-size: 0.85rem; font-weight: 600;
            cursor: pointer; white-space: nowrap; transition: background 0.18s; flex-shrink: 0;
        }
        .btn-add:hover { background: #2563eb; }

        /* ── PRODUCTS TABLE ── */
        .products-table { width: 100%; border-collapse: collapse; }
        .products-table thead tr { background: #f8fafc; }
        .products-table th {
            padding: 0.65rem 0.85rem; text-align: left;
            font-size: 0.72rem; font-weight: 700; color: #475569;
            text-transform: uppercase; letter-spacing: 0.05em;
            border-bottom: 1px solid #f1f5f9;
        }
        .products-table td { padding: 0.75rem 0.85rem; font-size: 0.85rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        .products-table tbody tr:last-child td { border-bottom: none; }
        .td-prod  { font-weight: 600; color: #1e293b; }
        .td-price { color: #22c55e; font-weight: 700; }
        .btn-remove { background: none; border: none; color: #94a3b8; cursor: pointer; font-size: 1rem; padding: 0.2rem 0.4rem; border-radius: 4px; transition: color 0.15s; }
        .btn-remove:hover { color: #ef4444; }
        .empty-productos { text-align: center; padding: 1.8rem; color: #94a3b8; font-size: 0.85rem; }

        /* ── SUMMARY ── */
        .summary-line { display: flex; justify-content: space-between; align-items: center; padding: 0.7rem 0; border-bottom: 1px solid #f1f5f9; }
        .summary-line:last-of-type { border-bottom: none; }
        .summary-label { color: #64748b; font-size: 0.875rem; }
        .summary-val   { font-weight: 700; color: #1e293b; }
        .summary-total { font-size: 1.4rem; color: #22c55e; }

        /* ── FIADO ── */
        .fiado-row {
            display: flex; align-items: center; gap: 0.75rem;
            padding: 0.9rem; background: #fef2f2; border-radius: 8px;
            margin: 1rem 0; cursor: pointer;
        }
        .fiado-row input[type="checkbox"] { width: 18px; height: 18px; cursor: pointer; accent-color: #ef4444; }
        .fiado-label { font-size: 0.875rem; font-weight: 600; color: #dc2626; cursor: pointer; display: block; }
        .fiado-sub   { font-size: 0.75rem; color: #94a3b8; }

        /* ── SUBMIT ── */
        .btn-submit {
            width: 100%; padding: 0.85rem; background: #22c55e; color: #fff;
            border: none; border-radius: 10px; font-size: 1rem; font-weight: 700;
            cursor: pointer; transition: background 0.18s, transform 0.15s;
            display: flex; align-items: center; justify-content: center; gap: 0.5rem;
        }
        .btn-submit:hover { background: #16a34a; transform: translateY(-1px); }
        .btn-link { display: block; text-align: center; margin-top: 0.75rem; color: #64748b; font-size: 0.85rem; text-decoration: none; }
        .btn-link:hover { color: #1e293b; }

        /* ── HAMBURGER ── */
        .hamburger-btn {
            display: none; position: fixed; top: 0.8rem; left: 0.8rem;
            z-index: 150; background: #1e293b; color: #fff;
            border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer;
        }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        @media (max-width: 900px)  { .layout { grid-template-columns: 1fr; } }
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
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link sidebar__link--activo">
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
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
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
            <h1 class="page-header__title">Registrar Venta</h1>
            <p class="page-header__sub">Seleccione cliente, productos y método de pago.</p>
        </div>

        <% if (error != null) { %>
        <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>

        <form id="formVenta" action="<%= ctx %>/PedidoControlador" method="post" onsubmit="return validar()">
            <input type="hidden" name="accion" value="registrar">

            <div class="layout">
                <!-- IZQUIERDA -->
                <div>
                    <div class="card" style="margin-bottom:1.5rem;">
                        <div class="card__title"><i class="fas fa-user"></i> Datos de la venta</div>

                        <!-- CLIENTE -->
                        <div class="form-group">
                            <label class="form-label">
                                Cliente
                                <span id="clienteHint" style="color:#94a3b8;font-weight:400;">(opcional — requerido en fiado)</span>
                            </label>

                            <!-- Búsqueda -->
                            <div class="cliente-search-wrap" id="searchWrap">
                                <input type="text" id="buscarCliente" class="form-input"
                                       placeholder="Escribe nombre o cédula..."
                                       autocomplete="off"
                                       oninput="filtrarClientes(this.value)"
                                       onfocus="filtrarClientes(this.value)">
                                <div class="cliente-dropdown" id="clienteDropdown"></div>
                            </div>

                            <!-- Cliente seleccionado -->
                            <div class="cliente-seleccionado" id="clienteSeleccionado">
                                <i class="fas fa-user-check" style="color:#3b82f6;"></i>
                                <span id="nombreClienteSeleccionado"></span>
                                <button type="button" class="btn-clear-cliente" onclick="limpiarCliente()" title="Cambiar cliente">
                                    <i class="fas fa-times"></i> Cambiar
                                </button>
                            </div>

                            <!-- Campo oculto con el ID -->
                            <input type="hidden" name="idCliente" id="idClienteHidden">

                            <!-- Botón nuevo cliente -->
                            <button type="button" class="btn-nuevo-cliente" id="btnNuevoCliente"
                                    onclick="toggleNuevoCliente()">
                                <i class="fas fa-user-plus"></i> Registrar cliente nuevo
                            </button>

                            <!-- Panel inline nuevo cliente -->
                            <div class="nuevo-cliente-panel" id="nuevoClientePanel">
                                <div class="panel-title">
                                    <i class="fas fa-user-plus" style="color:#22c55e;"></i>
                                    Datos del nuevo cliente
                                </div>
                                <div class="nuevo-cliente-grid">
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Nombre *</label>
                                        <input type="text" name="nuevoClienteNombre" id="nuevoClienteNombre"
                                               class="form-input" placeholder="Nombre">
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Apellido *</label>
                                        <input type="text" name="nuevoClienteApellido"
                                               class="form-input" placeholder="Apellido">
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Cédula *</label>
                                        <input type="text" name="nuevoClienteCedula"
                                               class="form-input" placeholder="Número de cédula">
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Teléfono</label>
                                        <input type="text" name="nuevoClienteTelefono"
                                               class="form-input" placeholder="Teléfono (opcional)">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Método de pago *</label>
                            <select name="idPago" class="form-select" required>
                                <option value="">-- Seleccionar método --</option>
                                <% if (metodosPago != null) { for (MetodoPago mp : metodosPago) { %>
                                <option value="<%= mp.getIdPago() %>"><%= mp.getNombre() %></option>
                                <% } } %>
                            </select>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card__title"><i class="fas fa-box"></i> Agregar productos</div>
                        <div class="add-row">
                            <div class="form-group" style="flex:1; margin-bottom:0;">
                                <label class="form-label">Producto</label>
                                <select id="selectProducto" class="form-select">
                                    <option value="">-- Seleccionar --</option>
                                    <% if (productos != null) { for (Producto p : productos) {
                                        if (p.getPrecio() == null || p.getNombre() == null) continue;
                                        String nomOpt = p.getNombre()
                                            .replace("&", "&amp;")
                                            .replace("\"", "&quot;")
                                            .replace("<", "&lt;")
                                            .replace(">", "&gt;");
                                    %>
                                    <option value="<%= p.getIdProducto() %>"><%= nomOpt %> — $<%= String.format("%,.0f", p.getPrecio()) %></option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="form-group qty-group" style="margin-bottom:0;">
                                <label class="form-label">Cant.</label>
                                <input type="number" id="inputCantidad" class="form-input" value="1" min="1">
                            </div>
                            <button type="button" class="btn-add" onclick="agregarProducto()">
                                <i class="fas fa-plus"></i> Agregar
                            </button>
                        </div>

                        <table class="products-table">
                            <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th>Cant.</th>
                                    <th>Precio unit.</th>
                                    <th>Subtotal</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody id="tablaProductos">
                                <tr id="filaVacia">
                                    <td colspan="5" class="empty-productos">
                                        <i class="fas fa-shopping-cart" style="font-size:1.8rem;display:block;margin-bottom:0.4rem;"></i>
                                        Sin productos — agrega uno arriba
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- DERECHA -->
                <div class="card">
                    <div class="card__title"><i class="fas fa-calculator"></i> Resumen</div>

                    <div class="summary-line">
                        <span class="summary-label"><i class="fas fa-box" style="color:#3b82f6;margin-right:4px;"></i> Productos</span>
                        <span class="summary-val" id="cantProductos">0</span>
                    </div>
                    <div class="summary-line">
                        <span class="summary-label"><i class="fas fa-dollar-sign" style="color:#22c55e;margin-right:4px;"></i> Total</span>
                        <span class="summary-val summary-total" id="totalDisplay">$0</span>
                    </div>

                    <div class="fiado-row">
                        <input type="checkbox" name="fiado" value="on" id="chkFiado">
                        <div>
                            <label class="fiado-label" for="chkFiado">Venta a fiado</label>
                            <span class="fiado-sub">Se registra como deuda pendiente</span>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">
                        <i class="fas fa-check-circle"></i> Guardar venta
                    </button>
                    <a href="<%= ctx %>/PedidoControlador" class="btn-link">
                        <i class="fas fa-history"></i> Ver historial
                    </a>
                </div>
            </div>
        </form>
    </main>

    <script>
        /* ── DATOS DE PRODUCTOS (cargados desde servidor) ── */
        const productosData = [<%
            if (productos != null) { for (Producto p : productos) {
                if (p.getPrecio() == null || p.getNombre() == null) continue;
                String nomJS = p.getNombre().replace("\\", "\\\\").replace("'", "\\'");
        %>{ id: <%= p.getIdProducto() %>, nombre: '<%= nomJS %>', precio: <%= p.getPrecio().toPlainString() %>, stock: <%= p.getStock() %> },<% } } %>
        ];

        /* ── DATOS DE CLIENTES (cargados desde servidor) ── */
        const clientes = [
            <% if (clientes != null) { for (Cliente c : clientes) {
                String _nom = c.getNombre() != null ? c.getNombre().replace("'", "\\'") : "";
                String _ape = c.getApellido() != null ? c.getApellido().replace("'", "\\'") : "";
                String _ced = c.getCedula() != null ? c.getCedula().replace("'", "\\'") : "";
            %>
            { id: <%= c.getIdCliente() %>, nombre: '<%= _nom %>', apellido: '<%= _ape %>', cedula: '<%= _ced %>' },
            <% } } %>
        ];

        /* ── BÚSQUEDA DE CLIENTES ── */
        let nuevoClientePanelAbierto = false;

        function filtrarClientes(q) {
            const dropdown = document.getElementById('clienteDropdown');
            const seleccionado = document.getElementById('idClienteHidden').value;
            if (seleccionado) return; // ya hay uno seleccionado

            q = q.trim().toLowerCase();
            if (q.length === 0) { dropdown.style.display = 'none'; return; }

            const filtrados = clientes.filter(c =>
                (c.nombre + ' ' + c.apellido).toLowerCase().includes(q) ||
                c.cedula.toLowerCase().includes(q)
            ).slice(0, 10);

            if (filtrados.length === 0) {
                dropdown.innerHTML = '<div class="cliente-option" style="color:#94a3b8;cursor:default;">Sin resultados</div>';
            } else {
                dropdown.innerHTML = filtrados.map(c =>
                    `<div class="cliente-option" onclick="seleccionarCliente(${c.id}, '${c.nombre} ${c.apellido}')">
                        ${c.nombre} ${c.apellido}
                        <span class="ced">${c.cedula}</span>
                    </div>`
                ).join('');
            }
            dropdown.style.display = 'block';
        }

        function seleccionarCliente(id, nombreCompleto) {
            document.getElementById('idClienteHidden').value = id;
            document.getElementById('nombreClienteSeleccionado').textContent = nombreCompleto;
            document.getElementById('clienteSeleccionado').style.display = 'flex';
            document.getElementById('searchWrap').style.display = 'none';
            document.getElementById('clienteDropdown').style.display = 'none';
            document.getElementById('btnNuevoCliente').style.display = 'none';
            // Cerrar panel nuevo cliente si estaba abierto
            if (nuevoClientePanelAbierto) toggleNuevoCliente();
        }

        function limpiarCliente() {
            document.getElementById('idClienteHidden').value = '';
            document.getElementById('buscarCliente').value = '';
            document.getElementById('clienteSeleccionado').style.display = 'none';
            document.getElementById('searchWrap').style.display = 'block';
            document.getElementById('btnNuevoCliente').style.display = 'inline-flex';
        }

        function toggleNuevoCliente() {
            const panel = document.getElementById('nuevoClientePanel');
            nuevoClientePanelAbierto = !nuevoClientePanelAbierto;
            panel.style.display = nuevoClientePanelAbierto ? 'block' : 'none';
            const btn = document.getElementById('btnNuevoCliente');
            btn.innerHTML = nuevoClientePanelAbierto
                ? '<i class="fas fa-times"></i> Cancelar'
                : '<i class="fas fa-user-plus"></i> Registrar cliente nuevo';
            btn.style.borderColor = nuevoClientePanelAbierto ? '#ef4444' : '';
            btn.style.color       = nuevoClientePanelAbierto ? '#dc2626' : '';
            // Si abre el panel, limpiar cliente seleccionado
            if (nuevoClientePanelAbierto) {
                document.getElementById('idClienteHidden').value = '';
                document.getElementById('buscarCliente').value = '';
                document.getElementById('clienteSeleccionado').style.display = 'none';
                document.getElementById('searchWrap').style.display = 'none';
            } else {
                document.getElementById('searchWrap').style.display = 'block';
                // Limpiar campos del panel
                document.getElementById('nuevoClienteNombre').value = '';
                document.querySelector('[name="nuevoClienteApellido"]').value = '';
                document.querySelector('[name="nuevoClienteCedula"]').value = '';
                document.querySelector('[name="nuevoClienteTelefono"]').value = '';
            }
        }

        // Cerrar dropdown al hacer clic fuera
        document.addEventListener('click', function(e) {
            if (!document.getElementById('searchWrap').contains(e.target))
                document.getElementById('clienteDropdown').style.display = 'none';
        });

        /* ── PRODUCTOS ── */
        let filaId = 0;

        function agregarProducto() {
            const sel  = document.getElementById('selectProducto');
            const id   = sel.value;
            const cant = parseInt(document.getElementById('inputCantidad').value) || 1;
            if (!id) { alert('Selecciona un producto.'); return; }

            const prod = productosData.find(function(p) { return p.id == id; });
            if (!prod) { alert('Producto no encontrado.'); return; }

            const precio = prod.precio;
            const stock  = prod.stock;
            const nombre = prod.nombre;

            if (!precio || precio <= 0) { alert('Este producto tiene precio inválido.'); return; }
            if (cant <= 0) { alert('La cantidad debe ser mayor a 0.'); return; }
            if (cant > stock) { alert('Stock insuficiente. Disponible: ' + stock); return; }

            document.getElementById('filaVacia').style.display = 'none';
            filaId++;
            const subtotal = precio * cant;
            const tbody = document.getElementById('tablaProductos');
            const tr = document.createElement('tr');
            tr.id = 'fila-' + filaId;
            const fid = filaId;
            tr.innerHTML =
                '<td class="td-prod">' + nombre + '</td>' +
                '<td>' + cant + '</td>' +
                '<td class="td-price">$' + precio.toLocaleString('es-CO') + '</td>' +
                '<td style="font-weight:600;">$' + subtotal.toLocaleString('es-CO') + '</td>' +
                '<td>' +
                    '<button type="button" class="btn-remove" onclick="eliminarFila(\'fila-' + fid + '\')">' +
                        '<i class="fas fa-times"></i>' +
                    '</button>' +
                    '<input type="hidden" name="idProducto"     value="' + id + '">' +
                    '<input type="hidden" name="cantidad"       value="' + cant + '">' +
                    '<input type="hidden" name="precioUnitario" value="' + precio + '">' +
                '</td>';
            tbody.appendChild(tr);
            sel.value = '';
            document.getElementById('inputCantidad').value = 1;
            actualizarResumen();
        }

        function eliminarFila(id) {
            const fila = document.getElementById(id);
            if (fila) { fila.remove(); actualizarResumen(); }
            if (document.querySelectorAll('input[name="idProducto"]').length === 0)
                document.getElementById('filaVacia').style.display = '';
        }

        function actualizarResumen() {
            const cantidades = document.querySelectorAll('input[name="cantidad"]');
            const precios    = document.querySelectorAll('input[name="precioUnitario"]');
            let total = 0;
            for (let i = 0; i < cantidades.length; i++)
                total += parseInt(cantidades[i].value) * parseFloat(precios[i].value);
            document.getElementById('totalDisplay').textContent = '$' + total.toLocaleString('es-CO');
            document.getElementById('cantProductos').textContent = cantidades.length;
        }

        /* ── VALIDACIÓN ── */
        function validar() {
            if (document.querySelectorAll('input[name="idProducto"]').length === 0) {
                alert('Agrega al menos un producto a la venta.');
                return false;
            }
            // Si el panel de nuevo cliente está abierto, validar campos obligatorios
            if (nuevoClientePanelAbierto) {
                const nombre = document.getElementById('nuevoClienteNombre').value.trim();
                const cedula = document.querySelector('[name="nuevoClienteCedula"]').value.trim();
                if (!nombre) { alert('Ingresa el nombre del nuevo cliente.'); return false; }
                if (!cedula) { alert('Ingresa la cédula del nuevo cliente.'); return false; }
            }
            // Fiado requiere cliente
            const fiado = document.getElementById('chkFiado').checked;
            const idCliente = document.getElementById('idClienteHidden').value;
            if (fiado && !idCliente && !nuevoClientePanelAbierto) {
                alert('Para venta a fiado debes seleccionar o registrar un cliente.');
                return false;
            }
            return true;
        }

        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

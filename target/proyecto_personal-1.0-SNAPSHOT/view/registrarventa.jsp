<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Usuario, modelos.MetodoPago" %>
<%
    List<Producto>   productos   = (List<Producto>)   request.getAttribute("productos");
    List<MetodoPago> metodosPago = (List<MetodoPago>) request.getAttribute("metodosPago");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
    // ID del cliente "Admin Tienda" para control de fiado en JavaScript
    Integer idAdminTienda = (Integer) request.getAttribute("idAdminTienda");
    if (idAdminTienda == null) idAdminTienda = 0;
    request.setAttribute("_paginaActiva", "registrarVenta");
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

        /* ── TOAST NOTIFICACIÓN ── */
        .toast-container {
            position: fixed; top: 20px; right: 20px; z-index: 9999;
            display: flex; flex-direction: column; gap: 10px;
        }
        .toast {
            background: #fff; border-left: 4px solid #ef4444; border-radius: 8px;
            padding: 14px 20px; min-width: 300px; max-width: 420px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
            display: flex; align-items: center; gap: 10px;
            animation: toastIn 0.3s ease, toastOut 0.4s ease 3.6s forwards;
            font-family: 'Segoe UI', system-ui, sans-serif; font-size: 0.92rem;
        }
        .toast i { color: #ef4444; font-size: 1.2rem; }
        .toast span { flex: 1; color: #1e293b; }
        @keyframes toastIn { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: translateX(0); } }
        @keyframes toastOut { from { opacity: 1; } to { opacity: 0; transform: translateX(40px); } }

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
        .form-input.input-ok    { border-color: #22c55e; }
        .form-input.input-error { border-color: #ef4444; background: #fff5f5; }
        .field-error { display: none; color: #dc2626; font-size: 0.7rem; margin-top: 0.15rem; }
        .field-error.visible    { display: flex; align-items: center; gap: 0.25rem; }

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

    <jsp:include page="sidebar.jsp" />

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
                                <span id="clienteHint" style="color:#94a3b8;font-weight:400;">(opcional — si no se selecciona, se asigna a "Admin Tienda")</span>
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
                                        <input type="text" name="nuevoClienteNombre" id="nvNombre"
                                               class="form-input" placeholder="Nombre"
                                               oninput="nvValidarNombre(this, 'nvErrNombre')"
                                               maxlength="60">
                                        <span class="field-error" id="nvErrNombre"></span>
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Apellido *</label>
                                        <input type="text" name="nuevoClienteApellido" id="nvApellido"
                                               class="form-input" placeholder="Apellido"
                                               oninput="nvValidarNombre(this, 'nvErrApellido')"
                                               maxlength="60">
                                        <span class="field-error" id="nvErrApellido"></span>
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Cédula *</label>
                                        <input type="text" name="nuevoClienteCedula" id="nvCedula"
                                               class="form-input" placeholder="Mín. 8 — máx. 15 dígitos"
                                               oninput="nvValidarCedula(this)"
                                               onkeypress="return /[0-9]/.test(event.key)"
                                               maxlength="15">
                                        <span class="field-error" id="nvErrCedula"></span>
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Teléfono</label>
                                        <input type="text" name="nuevoClienteTelefono" id="nvTelefono"
                                               class="form-input" placeholder="Teléfono (opcional)"
                                               oninput="nvValidarTelefono(this)"
                                               onkeypress="return /[0-9]/.test(event.key)"
                                               maxlength="15">
                                        <span class="field-error" id="nvErrTelefono"></span>
                                    </div>
                                    <div class="form-group" style="margin-bottom:0;">
                                        <label class="form-label">Correo electrónico</label>
                                        <input type="text" name="nuevoClienteEmail" id="nvEmail"
                                               class="form-input" placeholder="correo@ejemplo.com (opcional)"
                                               oninput="nvValidarEmail(this)"
                                               maxlength="150">
                                        <span class="field-error" id="nvErrEmail"></span>
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

    <div class="toast-container" id="toastContainer"></div>
    <script>
        /* ── TOAST NOTIFICACIÓN ── */
        function mostrarToast(mensaje) {
            var container = document.getElementById('toastContainer');
            var toast = document.createElement('div');
            toast.className = 'toast';
            toast.innerHTML = '<i class="fas fa-exclamation-circle"></i><span>' + mensaje + '</span>';
            container.appendChild(toast);
            setTimeout(function() { toast.remove(); }, 4000);
        }

        /* ── DATOS DE PRODUCTOS (cargados desde servidor) ── */
        const productosData = [<%
            if (productos != null) { for (Producto p : productos) {
                if (p.getPrecio() == null || p.getNombre() == null) continue;
                String nomJS = p.getNombre().replace("\\", "\\\\").replace("'", "\\'");
        %>{ id: <%= p.getIdProducto() %>, nombre: '<%= nomJS %>', precio: <%= p.getPrecio().toPlainString() %>, stock: <%= p.getStock() %>, unidad: '<%= p.getUnidadMedida() != null ? p.getUnidadMedida().replace("'", "\\'") : "" %>' },<% } } %>
        ];

        /* ── CLIENTE ADMIN TIENDA (ventas anónimas) ── */
        const ID_ADMIN_TIENDA = <%= idAdminTienda %>;

        /* ── BÚSQUEDA DE CLIENTES (AJAX en tiempo real) ── */
        const _ctx = '<%= ctx %>';
        let nuevoClientePanelAbierto = false;
        let _debounce;

        function filtrarClientes(q) {
            const dropdown = document.getElementById('clienteDropdown');
            if (document.getElementById('idClienteHidden').value) return;

            q = q.trim();
            if (q.length < 2) { dropdown.style.display = 'none'; return; }

            clearTimeout(_debounce);
            _debounce = setTimeout(function() {
                fetch(_ctx + '/ClienteControlador?accion=buscar&q=' + encodeURIComponent(q))
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        if (data.length === 0) {
                            dropdown.innerHTML = '<div class="cliente-option" style="color:#94a3b8;cursor:default;">Sin resultados</div>';
                        } else {
                            dropdown.innerHTML = data.map(function(c) {
                                var nombre = c.nombre + ' ' + c.apellido;
                                var safe   = nombre.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
                                return '<div class="cliente-option" onclick="seleccionarCliente(' + c.id + ', \'' + safe + '\')">' +
                                    nombre + '<span class="ced">' + c.cedula + '</span></div>';
                            }).join('');
                        }
                        dropdown.style.display = 'block';
                    })
                    .catch(function() { dropdown.style.display = 'none'; });
            }, 250);
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
            // Si es Admin Tienda, deshabilitar fiado
            controlarFiado(id);
            guardarCarrito();
        }

        function limpiarCliente() {
            document.getElementById('idClienteHidden').value = '';
            document.getElementById('buscarCliente').value = '';
            document.getElementById('clienteSeleccionado').style.display = 'none';
            document.getElementById('searchWrap').style.display = 'block';
            document.getElementById('btnNuevoCliente').style.display = 'inline-flex';
            // Re-habilitar fiado al limpiar cliente
            controlarFiado(0);
            guardarCarrito();
        }

        /** Deshabilita/habilita el checkbox de fiado según si el cliente es Admin Tienda */
        function controlarFiado(idCliente) {
            var chk = document.getElementById('chkFiado');
            var fiadoRow = chk.closest('.fiado-row');
            if (ID_ADMIN_TIENDA > 0 && parseInt(idCliente) === ID_ADMIN_TIENDA) {
                chk.checked = false;
                chk.disabled = true;
                fiadoRow.style.opacity = '0.5';
                fiadoRow.title = 'No se puede fiar al cliente Admin Tienda';
            } else {
                chk.disabled = false;
                fiadoRow.style.opacity = '1';
                fiadoRow.title = '';
            }
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
                // Limpiar campos del panel y estados de validación
                ['nvNombre','nvApellido','nvCedula','nvTelefono','nvEmail'].forEach(function(id) {
                    var el = document.getElementById(id);
                    if (el) { el.value = ''; el.className = 'form-input'; }
                });
                ['nvErrNombre','nvErrApellido','nvErrCedula','nvErrTelefono','nvErrEmail'].forEach(function(id) {
                    var el = document.getElementById(id);
                    if (el) { el.textContent = ''; el.className = 'field-error'; }
                });
            }
            guardarCarrito();
        }

        // Cerrar dropdown al hacer clic fuera
        document.addEventListener('click', function(e) {
            if (!document.getElementById('searchWrap').contains(e.target))
                document.getElementById('clienteDropdown').style.display = 'none';
        });

        /* ── PRODUCTOS ── */
        let filaId = 0;

        /** Agrega una fila al carrito (visual + hidden inputs) */
        function insertarFila(id, nombre, precio, cant, unidad) {
            document.getElementById('filaVacia').style.display = 'none';
            filaId++;
            var subtotal = precio * cant;
            var tbody = document.getElementById('tablaProductos');
            var tr = document.createElement('tr');
            tr.id = 'fila-' + filaId;
            var fid = filaId;
            var unidadTexto = unidad ? ' <span style="color:#64748b;font-size:0.78rem;">(' + unidad + ')</span>' : '';
            tr.innerHTML =
                '<td class="td-prod">' + nombre + '</td>' +
                '<td>' + cant + unidadTexto + '</td>' +
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
        }

        function agregarProducto() {
            const sel  = document.getElementById('selectProducto');
            const id   = sel.value;
            const cant = parseInt(document.getElementById('inputCantidad').value) || 1;
            if (!id) { mostrarToast('Selecciona un producto.'); return; }

            const prod = productosData.find(function(p) { return p.id == id; });
            if (!prod) { mostrarToast('Producto no encontrado.'); return; }

            const precio = prod.precio;
            const stock  = prod.stock;
            const nombre = prod.nombre;
            const unidad = prod.unidad || '';

            if (!precio || precio <= 0) { mostrarToast('Este producto tiene precio inválido.'); return; }
            if (cant <= 0) { mostrarToast('La cantidad debe ser mayor a 0.'); return; }
            if (cant > stock) { mostrarToast('Stock insuficiente. Disponible: ' + stock); return; }

            insertarFila(id, nombre, precio, cant, unidad);
            sel.value = '';
            document.getElementById('inputCantidad').value = 1;
            actualizarResumen();
            guardarCarrito();
        }

        function eliminarFila(id) {
            const fila = document.getElementById(id);
            if (fila) { fila.remove(); actualizarResumen(); guardarCarrito(); }
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

        /* ── PERSISTENCIA DEL CARRITO (sessionStorage) ── */

        /** Guarda TODO el estado de la venta en sessionStorage */
        function guardarCarrito() {
            var estado = {};

            // Guardar productos del carrito
            var items = [];
            var ids       = document.querySelectorAll('input[name="idProducto"]');
            var cantidades = document.querySelectorAll('input[name="cantidad"]');
            var precios    = document.querySelectorAll('input[name="precioUnitario"]');
            for (var i = 0; i < ids.length; i++) {
                var prod = productosData.find(function(p) { return p.id == ids[i].value; });
                items.push({
                    id: ids[i].value,
                    nombre: prod ? prod.nombre : 'Producto',
                    precio: parseFloat(precios[i].value),
                    cantidad: parseInt(cantidades[i].value),
                    unidad: prod ? (prod.unidad || '') : ''
                });
            }
            estado.carrito = items;

            // Guardar cliente seleccionado
            var idCliente = document.getElementById('idClienteHidden').value;
            var nombreCliente = document.getElementById('nombreClienteSeleccionado').textContent;
            if (idCliente) {
                estado.cliente = { id: idCliente, nombre: nombreCliente };
            }

            // Guardar método de pago
            var selectPago = document.querySelector('select[name="idPago"]');
            if (selectPago && selectPago.value) {
                estado.idPago = selectPago.value;
            }

            // Guardar fiado
            estado.fiado = document.getElementById('chkFiado').checked;

            // Guardar panel nuevo cliente si está abierto
            if (nuevoClientePanelAbierto) {
                estado.nuevoCliente = {
                    abierto: true,
                    nombre: document.getElementById('nvNombre').value,
                    apellido: document.getElementById('nvApellido').value,
                    cedula: document.getElementById('nvCedula').value,
                    telefono: document.getElementById('nvTelefono').value,
                    email: document.getElementById('nvEmail').value
                };
            }

            sessionStorage.setItem('carritoVenta', JSON.stringify(estado));
        }

        /** Restaura TODO el estado de la venta desde sessionStorage */
        function restaurarCarrito() {
            var datos = sessionStorage.getItem('carritoVenta');
            if (!datos) return;
            var estado = JSON.parse(datos);

            // Restaurar productos del carrito
            if (estado.carrito && estado.carrito.length > 0) {
                for (var i = 0; i < estado.carrito.length; i++) {
                    insertarFila(estado.carrito[i].id, estado.carrito[i].nombre, estado.carrito[i].precio, estado.carrito[i].cantidad, estado.carrito[i].unidad);
                }
                actualizarResumen();
            }

            // Restaurar cliente seleccionado
            if (estado.cliente && estado.cliente.id) {
                seleccionarCliente(parseInt(estado.cliente.id), estado.cliente.nombre);
            }

            // Restaurar método de pago
            if (estado.idPago) {
                var selectPago = document.querySelector('select[name="idPago"]');
                if (selectPago) selectPago.value = estado.idPago;
            }

            // Restaurar fiado
            if (estado.fiado) {
                var chk = document.getElementById('chkFiado');
                if (!chk.disabled) chk.checked = true;
            }

            // Restaurar panel nuevo cliente
            if (estado.nuevoCliente && estado.nuevoCliente.abierto) {
                toggleNuevoCliente();
                document.getElementById('nvNombre').value = estado.nuevoCliente.nombre || '';
                document.getElementById('nvApellido').value = estado.nuevoCliente.apellido || '';
                document.getElementById('nvCedula').value = estado.nuevoCliente.cedula || '';
                document.getElementById('nvTelefono').value = estado.nuevoCliente.telefono || '';
                document.getElementById('nvEmail').value = estado.nuevoCliente.email || '';
            }
        }

        /** Limpia todo el estado guardado */
        function limpiarCarrito() {
            sessionStorage.removeItem('carritoVenta');
        }

        // Restaurar al cargar la página
        restaurarCarrito();

        // Guardar cuando cambie el método de pago o el fiado
        var _selectPago = document.querySelector('select[name="idPago"]');
        if (_selectPago) _selectPago.addEventListener('change', guardarCarrito);
        document.getElementById('chkFiado').addEventListener('change', guardarCarrito);

        // Guardar cuando se escriba en los campos de nuevo cliente
        ['nvNombre','nvApellido','nvCedula','nvTelefono','nvEmail'].forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.addEventListener('input', guardarCarrito);
        });

        /* ── VALIDACIÓN NUEVO CLIENTE ── */
        function nvSetError(inputId, errId, msg) {
            var inp = document.getElementById(inputId);
            var err = document.getElementById(errId);
            if (inp) { inp.classList.remove('input-ok'); inp.classList.add('input-error'); }
            if (err) { err.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg; err.classList.add('visible'); }
        }
        function nvSetOk(inputId, errId) {
            var inp = document.getElementById(inputId);
            var err = document.getElementById(errId);
            if (inp) { inp.classList.remove('input-error'); inp.classList.add('input-ok'); }
            if (err) { err.textContent = ''; err.classList.remove('visible'); }
        }
        function nvClear(inputId, errId) {
            var inp = document.getElementById(inputId);
            var err = document.getElementById(errId);
            if (inp) { inp.classList.remove('input-ok','input-error'); }
            if (err) { err.textContent = ''; err.classList.remove('visible'); }
        }

        function nvValidarNombre(inp, errId) {
            var v = inp.value.trim();
            if (v === '') { nvClear(inp.id, errId); return null; }
            if (!/^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/.test(v))
                { nvSetError(inp.id, errId, 'Solo se permiten letras, sin números ni símbolos.'); return false; }
            if (v.length < 2)
                { nvSetError(inp.id, errId, 'Debe tener al menos 2 caracteres.'); return false; }
            nvSetOk(inp.id, errId); return true;
        }
        function nvValidarCedula(inp) {
            var v = inp.value.trim();
            if (v === '') { nvClear('nvCedula','nvErrCedula'); return null; }
            if (!/^\d+$/.test(v))
                { nvSetError('nvCedula','nvErrCedula','La cédula solo puede contener números.'); return false; }
            if (v.length < 8)
                { nvSetError('nvCedula','nvErrCedula','La cédula debe tener mínimo 8 dígitos.'); return false; }
            if (v.length > 15)
                { nvSetError('nvCedula','nvErrCedula','La cédula no puede superar 15 dígitos.'); return false; }
            nvSetOk('nvCedula','nvErrCedula'); return true;
        }
        function nvValidarTelefono(inp) {
            inp.value = inp.value.replace(/\D/g, '');
            var v = inp.value.trim();
            if (v === '') { nvClear('nvTelefono','nvErrTelefono'); return null; }
            if (v.length < 7)
                { nvSetError('nvTelefono','nvErrTelefono','El teléfono debe tener mínimo 7 dígitos.'); return false; }
            if (v.length > 10)
                { nvSetError('nvTelefono','nvErrTelefono','El teléfono no puede superar 10 dígitos.'); return false; }
            nvSetOk('nvTelefono','nvErrTelefono'); return true;
        }
        function nvValidarEmail(inp) {
            var v = inp.value.trim();
            if (v === '') { nvClear('nvEmail','nvErrEmail'); return null; }
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
                { nvSetError('nvEmail','nvErrEmail','Ingresa un correo válido, ej: nombre@gmail.com'); return false; }
            nvSetOk('nvEmail','nvErrEmail'); return true;
        }

        /* ── VALIDACIÓN FORMULARIO PRINCIPAL ── */
        function validar() {
            if (document.querySelectorAll('input[name="idProducto"]').length === 0) {
                mostrarToast('Agrega al menos un producto a la venta.');
                return false;
            }
            // Si el panel de nuevo cliente está abierto, validar todos sus campos
            if (nuevoClientePanelAbierto) {
                var nombreInp = document.getElementById('nvNombre');
                var apellidoInp = document.getElementById('nvApellido');
                var cedulaInp = document.getElementById('nvCedula');
                var telInp = document.getElementById('nvTelefono');
                var emailInp = document.getElementById('nvEmail');

                var okNombre   = nvValidarNombre(nombreInp,   'nvErrNombre');
                var okApellido = nvValidarNombre(apellidoInp, 'nvErrApellido');
                var okCedula   = nvValidarCedula(cedulaInp);
                var okTel      = nvValidarTelefono(telInp);
                var okEmail    = nvValidarEmail(emailInp);

                // Nombre y cédula son obligatorios
                if (nombreInp.value.trim() === '') {
                    nvSetError('nvNombre','nvErrNombre','El nombre es obligatorio.');
                    okNombre = false;
                }
                if (apellidoInp.value.trim() === '') {
                    nvSetError('nvApellido','nvErrApellido','El apellido es obligatorio.');
                    okApellido = false;
                }
                if (cedulaInp.value.trim() === '') {
                    nvSetError('nvCedula','nvErrCedula','La cédula es obligatoria.');
                    okCedula = false;
                }

                if (okNombre === false || okApellido === false || okCedula === false ||
                    okTel === false || okEmail === false) {
                    // Scroll al primer error
                    var primerError = document.querySelector('#nuevoClientePanel .input-error');
                    if (primerError) primerError.scrollIntoView({behavior:'smooth', block:'center'});
                    return false;
                }
            }
            // Fiado requiere cliente
            var fiado = document.getElementById('chkFiado').checked;
            var idCliente = document.getElementById('idClienteHidden').value;
            if (fiado && !idCliente && !nuevoClientePanelAbierto) {
                mostrarToast('Para venta a fiado debes seleccionar o registrar un cliente.');
                return false;
            }
            // Limpiar carrito guardado porque la venta se va a enviar
            limpiarCarrito();
            return true;
        }

        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

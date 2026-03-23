<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map, modelos.Pedido, modelos.DetallePedido, modelos.Usuario" %>
<%
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    Map<Integer, List<DetallePedido>> detallesPorPedido =
        (Map<Integer, List<DetallePedido>>) request.getAttribute("detallesPorPedido");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
    request.setAttribute("_paginaActiva", "historial");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Historial de Ventas — Tienda Don Pedro</title>
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
        .btn-primary {
            display: inline-flex; align-items: center; gap: 0.5rem;
            padding: 0.6rem 1.2rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 8px; font-size: 0.875rem;
            font-weight: 600; text-decoration: none; cursor: pointer;
            transition: background 0.18s;
        }
        .btn-primary:hover { background: #2563eb; }

        /* ── STATS ── */
        .stats { display: flex; gap: 1rem; margin-bottom: 1.8rem; flex-wrap: wrap; }
        .stat-card {
            background: #fff; border-radius: 12px; padding: 1rem 1.4rem;
            flex: 1; min-width: 140px; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
            display: flex; align-items: center; gap: 0.9rem;
        }
        .stat-card__icon {
            width: 42px; height: 42px; border-radius: 10px;
            display: flex; align-items: center; justify-content: center; font-size: 1.1rem;
        }
        .stat-card__icon--blue  { background: #eff6ff; color: #3b82f6; }
        .stat-card__icon--green { background: #f0fdf4; color: #22c55e; }
        .stat-card__icon--amber { background: #fffbeb; color: #f59e0b; }
        .stat-card__val { font-size: 1.4rem; font-weight: 700; color: #1e293b; }
        .stat-card__lbl { font-size: 0.75rem; color: #64748b; }

        /* ── TABLE CARD ── */
        .table-card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden; }
        .table-toolbar {
            display: flex; align-items: center; gap: 1rem;
            padding: 1rem 1.2rem; border-bottom: 1px solid #f1f5f9; flex-wrap: wrap;
        }
        .search-box {
            display: flex; align-items: center; gap: 0.5rem;
            border: 1.5px solid #e2e8f0; border-radius: 8px;
            padding: 0.45rem 0.8rem; background: #f8fafc;
            flex: 1; max-width: 320px; transition: border-color 0.18s;
        }
        .search-box:focus-within { border-color: #3b82f6; background: #fff; }
        .search-box i { color: #94a3b8; font-size: 0.85rem; }
        .search-box input {
            border: none; outline: none; background: transparent;
            font-size: 0.875rem; color: #1e293b; width: 100%;
        }
        .search-box input::placeholder { color: #94a3b8; }

        /* ── TABLE ── */
        table { width: 100%; border-collapse: collapse; }
        thead tr { background: #f8fafc; }
        th {
            padding: 0.8rem 1.2rem; text-align: left;
            font-size: 0.75rem; font-weight: 700; color: #475569;
            text-transform: uppercase; letter-spacing: 0.05em;
            border-bottom: 1px solid #f1f5f9;
        }
        td { padding: 0.9rem 1.2rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        tbody tr { transition: background 0.15s; }
        tbody tr:hover { background: #f8fafc; }
        tbody tr:last-child td { border-bottom: none; }

        .td-cliente { font-weight: 600; color: #1e293b; }
        .td-total   { font-weight: 700; color: #22c55e; }
        .td-fecha   { color: #64748b; font-size: 0.82rem; }

        /* ── BADGES ── */
        .badge {
            display: inline-flex; align-items: center; gap: 0.3rem;
            padding: 0.28rem 0.7rem; border-radius: 20px;
            font-size: 0.73rem; font-weight: 700;
        }
        .badge--pagado  { background: #f0fdf4; color: #16a34a; }
        .badge--credito { background: #fef2f2; color: #dc2626; }
        .badge::before { content: ''; width: 5px; height: 5px; border-radius: 50%; background: currentColor; }

        /* ── EMPTY ── */
        .empty-row td { text-align: center; padding: 3.5rem; color: #94a3b8; }
        .empty-row i  { font-size: 2.5rem; display: block; margin-bottom: 0.5rem; }

        /* ── HAMBURGER ── */
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
            th:nth-child(4), td:nth-child(4) { display: none; }
        }
        .sidebar__section--cuenta { border-top: 1px solid rgba(255,255,255,0.08); margin-top: 0.5rem; }
        .sidebar__user-card { display: flex; align-items: center; gap: 0.65rem; padding: 0.4rem 1.2rem 0.6rem; }
        .sidebar__user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: #fff; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; }
        .sidebar__user-name { color: #e2e8f0; font-size: 0.82rem; font-weight: 600; line-height: 1.3; }
        .sidebar__user-role { color: #64748b; font-size: 0.7rem; }
        .sidebar__link--logout { color: #f87171 !important; }
        .sidebar__link--logout:hover { color: #fff !important; background: rgba(239,68,68,0.12) !important; border-left-color: #ef4444 !important; }

        /* ── BOTÓN OJO ── */
        .btn-ver {
            background: none; border: none; cursor: pointer;
            color: #3b82f6; font-size: 1rem; padding: 0.2rem 0.4rem;
            border-radius: 6px; transition: background 0.15s;
        }
        .btn-ver:hover { background: #eff6ff; color: #2563eb; }

        /* ── MODAL ── */
        .modal-overlay {
            display: none; position: fixed; inset: 0;
            background: rgba(0,0,0,0.5); z-index: 500;
            align-items: center; justify-content: center;
        }
        .modal-overlay.open { display: flex; }
        .modal {
            background: #fff; border-radius: 14px;
            width: 90%; max-width: 560px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.18);
            overflow: hidden;
        }
        .modal__header {
            display: flex; justify-content: space-between; align-items: center;
            padding: 1rem 1.4rem; border-bottom: 1px solid #f1f5f9;
            background: #f8fafc;
        }
        .modal__title { font-size: 1rem; font-weight: 700; color: #1e293b; }
        .modal__close {
            background: none; border: none; font-size: 1.2rem;
            color: #94a3b8; cursor: pointer; line-height: 1;
        }
        .modal__close:hover { color: #1e293b; }
        .modal__body { padding: 1.2rem 1.4rem; }
        .modal__table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
        .modal__table th {
            text-align: left; padding: 0.5rem 0.7rem;
            background: #f8fafc; color: #475569;
            font-size: 0.72rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.05em;
            border-bottom: 1px solid #e2e8f0;
        }
        .modal__table td {
            padding: 0.6rem 0.7rem; border-bottom: 1px solid #f1f5f9; color: #334155;
        }
        .modal__table tbody tr:last-child td { border-bottom: none; }
        .modal__table .td-num { font-weight: 700; color: #22c55e; }
        .modal__footer {
            padding: 0.8rem 1.4rem; border-top: 1px solid #f1f5f9;
            text-align: right; font-size: 0.875rem;
            background: #f8fafc;
        }
        .modal__total { font-weight: 700; color: #1e293b; font-size: 1rem; }
    </style>
</head>
<body>

    <button class="hamburger-btn" onclick="toggleSidebar()"><i class="fas fa-bars"></i></button>
    <div class="overlay" id="overlay" onclick="toggleSidebar()"></div>

    <jsp:include page="sidebar.jsp" />

    <main class="main">
        <div class="page-header">
            <div>
                <h1 class="page-header__title">Historial de Ventas</h1>
                <p class="page-header__sub"><%= pedidos != null ? pedidos.size() : 0 %> venta(s) registrada(s)</p>
            </div>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="btn-primary">
                <i class="fas fa-plus"></i> Nueva venta
            </a>
        </div>

        <%
            int totalVentas = pedidos != null ? pedidos.size() : 0;
            int pagados = 0, creditos = 0;
            java.math.BigDecimal sumaTotal = java.math.BigDecimal.ZERO;
            if (pedidos != null) {
                for (Pedido p : pedidos) {
                    if ("pagado".equalsIgnoreCase(p.getEstado())) pagados++;
                    else creditos++;
                    if (p.getTotal() != null) sumaTotal = sumaTotal.add(p.getTotal());
                }
            }
        %>
        <div class="stats">
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--blue"><i class="fas fa-receipt"></i></div>
                <div><div class="stat-card__val"><%= totalVentas %></div><div class="stat-card__lbl">Total ventas</div></div>
            </div>
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--green"><i class="fas fa-check-circle"></i></div>
                <div><div class="stat-card__val"><%= pagados %></div><div class="stat-card__lbl">Pagadas</div></div>
            </div>
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--amber"><i class="fas fa-clock"></i></div>
                <div><div class="stat-card__val"><%= creditos %></div><div class="stat-card__lbl">A crédito</div></div>
            </div>
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--green"><i class="fas fa-dollar-sign"></i></div>
                <div><div class="stat-card__val">$<%= String.format("%,.0f", sumaTotal) %></div><div class="stat-card__lbl">Total acumulado</div></div>
            </div>
        </div>

        <div class="table-card">
            <div class="table-toolbar">
                <div class="search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" id="buscador" placeholder="Buscar cliente..." oninput="filtrar()">
                </div>
                <form method="get" action="<%= ctx %>/PedidoControlador" style="display:flex;gap:0.5rem;align-items:center;flex-wrap:wrap;">
                    <input type="date" name="fechaInicio"
                           value="<%= request.getAttribute("fechaInicio") != null ? request.getAttribute("fechaInicio") : "" %>"
                           style="padding:0.45rem 0.7rem;border:1px solid #e2e8f0;border-radius:6px;font-size:0.83rem;">
                    <span style="color:#64748b;font-size:0.82rem;">al</span>
                    <input type="date" name="fechaFin"
                           value="<%= request.getAttribute("fechaFin") != null ? request.getAttribute("fechaFin") : "" %>"
                           style="padding:0.45rem 0.7rem;border:1px solid #e2e8f0;border-radius:6px;font-size:0.83rem;">
                    <button type="submit" style="padding:0.45rem 1rem;background:#3b82f6;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:0.83rem;">Filtrar</button>
                    <a href="<%= ctx %>/PedidoControlador" style="padding:0.45rem 0.7rem;color:#64748b;font-size:0.83rem;text-decoration:none;">Limpiar</a>
                </form>
            </div>
            <table id="tablaHistorial">
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Cliente</th>
                        <th>Total</th>
                        <th>Estado</th>
                        <th>Detalle</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (pedidos == null || pedidos.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="5"><i class="fas fa-receipt"></i>No hay ventas registradas aún.</td>
                    </tr>
                    <% } else {
                        for (Pedido p : pedidos) {
                            String fechaStr = p.getFechaVenta() != null
                                ? p.getFechaVenta().toLocalDate().toString() : "-";
                            String cliente = (p.getNombreCliente() != null && !p.getNombreCliente().isBlank())
                                ? p.getNombreCliente()
                                : (p.getIdCliente() > 0 ? "Cliente #" + p.getIdCliente() : "Sin cliente");
                            String estado = p.getEstado() != null ? p.getEstado() : "credito";
                            String badgeClass = "pagado".equalsIgnoreCase(estado)
                                ? "badge--pagado" : "badge--credito";
                            String estadoLabel = "pagado".equalsIgnoreCase(estado) ? "Pagado" : "Crédito";
                    %>
                    <tr data-cliente="<%= cliente.toLowerCase() %>">
                        <td class="td-fecha"><%= fechaStr %></td>
                        <td class="td-cliente"><%= cliente %></td>
                        <td class="td-total">$<%= String.format("%,.0f", p.getTotal()) %></td>
                        <td><span class="badge <%= badgeClass %>"><%= estadoLabel %></span></td>
                        <td>
                            <button class="btn-ver" title="Ver productos de esta venta"
                                    onclick="verDetalle(<%= p.getIdPedido() %>, '<%= fechaStr %>', '<%= cliente.replace("'", "\\'") %>')">
                                <i class="fas fa-eye"></i>
                            </button>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </main>

    <!-- ── MODAL DETALLE DE VENTA ── -->
    <div class="modal-overlay" id="modalOverlay">
        <div class="modal">
            <div class="modal__header">
                <span class="modal__title" id="modalTitulo">Detalle de la venta</span>
                <button class="modal__close" onclick="cerrarModal()">&#x2715;</button>
            </div>
            <div class="modal__body">
                <table class="modal__table">
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>Precio unitario</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody id="modalCuerpo"></tbody>
                </table>
            </div>
            <div class="modal__footer">
                Total: <span class="modal__total" id="modalTotal"></span>
            </div>
        </div>
    </div>

    <%-- Declarar _detalles ANTES de que los bloques script lo llenen --%>
    <script>var _detalles = {};</script>

    <%-- Datos de detalles embebidos en el HTML para cada pedido --%>
    <% if (detallesPorPedido != null) {
        for (Map.Entry<Integer, List<DetallePedido>> entry : detallesPorPedido.entrySet()) {
            int idP = entry.getKey();
            List<DetallePedido> dets = entry.getValue();
    %>
    <script>
        _detalles[<%= idP %>] = [
            <% for (int i = 0; i < dets.size(); i++) {
                DetallePedido d = dets.get(i);
                java.math.BigDecimal subtotal = d.getPrecioUnitario() != null
                    ? d.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(d.getCantidadVendida()))
                    : java.math.BigDecimal.ZERO;
            %>
            {nombre:"<%= d.getNombreProducto() != null ? d.getNombreProducto().replace("\"","\\\"") : "Producto" %>",
             cantidad:<%= d.getCantidadVendida() %>,
             precio:<%= d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0 %>,
             subtotal:<%= subtotal %>}<%= i < dets.size()-1 ? "," : "" %>
            <% } %>
        ];
    </script>
    <% } } %>

    <script>
        function verDetalle(idPedido, fecha, cliente) {
            var datos = _detalles[idPedido];
            document.getElementById('modalTitulo').textContent =
                'Venta del ' + fecha + ' — ' + cliente;

            var cuerpo = document.getElementById('modalCuerpo');
            cuerpo.innerHTML = '';

            if (!datos || datos.length === 0) {
                cuerpo.innerHTML = '<tr><td colspan="4" style="text-align:center;color:#94a3b8;padding:1.5rem">Sin productos registrados</td></tr>';
            } else {
                var totalModal = 0;
                datos.forEach(function(d) {
                    totalModal += d.subtotal;
                    var tr = document.createElement('tr');
                    tr.innerHTML =
                        '<td>' + d.nombre + '</td>' +
                        '<td class="td-num">' + d.cantidad + '</td>' +
                        '<td>$' + Number(d.precio).toLocaleString('es-CO', {minimumFractionDigits:0}) + '</td>' +
                        '<td class="td-num">$' + Number(d.subtotal).toLocaleString('es-CO', {minimumFractionDigits:0}) + '</td>';
                    cuerpo.appendChild(tr);
                });
                document.getElementById('modalTotal').textContent =
                    '$' + totalModal.toLocaleString('es-CO', {minimumFractionDigits:0});
            }

            document.getElementById('modalOverlay').classList.add('open');
        }

        function cerrarModal() {
            document.getElementById('modalOverlay').classList.remove('open');
        }

        // Cerrar modal al hacer click fuera de él
        document.getElementById('modalOverlay').addEventListener('click', function(e) {
            if (e.target === this) cerrarModal();
        });

        function filtrar() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaHistorial tbody tr[data-cliente]').forEach(tr => {
                tr.style.display = tr.dataset.cliente.includes(texto) ? '' : 'none';
            });
        }
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }

        /* ── PERSISTENCIA DE BÚSQUEDA ── */
        function guardarBusquedaHistorial() {
            sessionStorage.setItem('historialBusqueda', document.getElementById('buscador').value);
        }
        function restaurarBusquedaHistorial() {
            var texto = sessionStorage.getItem('historialBusqueda');
            if (texto) {
                document.getElementById('buscador').value = texto;
                filtrar();
            }
        }
        document.getElementById('buscador').addEventListener('input', guardarBusquedaHistorial);
        restaurarBusquedaHistorial();
    </script>
</body>
</html>

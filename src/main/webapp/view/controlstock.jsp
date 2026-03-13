<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Usuario" %>
<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
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
    <title>Control de Stock — Tienda Don Pedro</title>
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

        /* ── PAGE HEADER ── */
        .page-header { margin-bottom: 2rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── STATS ROW ── */
        .stats { display: flex; gap: 1rem; margin-bottom: 1.8rem; flex-wrap: wrap; }
        .stat-card {
            background: #fff; border-radius: 12px; padding: 1rem 1.4rem;
            flex: 1; min-width: 140px; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
            display: flex; align-items: center; gap: 0.9rem;
        }
        .stat-card__icon {
            width: 42px; height: 42px; border-radius: 10px;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.1rem;
        }
        .stat-card__icon--total { background: #eff6ff; color: #3b82f6; }
        .stat-card__icon--ok    { background: #f0fdf4; color: #22c55e; }
        .stat-card__icon--crit  { background: #fef2f2; color: #ef4444; }
        .stat-card__val { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .stat-card__lbl { font-size: 0.75rem; color: #64748b; }

        /* ── TABLE CARD ── */
        .table-card {
            background: #fff; border-radius: 14px;
            box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden;
        }
        .table-toolbar {
            display: flex; align-items: center; gap: 1rem;
            padding: 1rem 1.2rem; border-bottom: 1px solid #f1f5f9;
            flex-wrap: wrap;
        }
        .search-box {
            display: flex; align-items: center; gap: 0.5rem;
            border: 1.5px solid #e2e8f0; border-radius: 8px;
            padding: 0.45rem 0.8rem; background: #f8fafc;
            flex: 1; max-width: 320px; transition: border-color 0.18s;
        }
        .search-box:focus-within { border-color: #3b82f6; background: #fff; }
        .search-box i { color: #94a3b8; font-size: 0.85rem; }
        .search-box input { border: none; outline: none; background: transparent; font-size: 0.875rem; color: #1e293b; width: 100%; }
        .search-box input::placeholder { color: #94a3b8; }

        .filter-group { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
        .filter-group span { font-size: 0.8rem; color: #64748b; font-weight: 500; }
        .filter-chip {
            padding: 0.3rem 0.7rem; border-radius: 20px; font-size: 0.75rem;
            font-weight: 600; cursor: pointer; border: 1.5px solid transparent;
            transition: all 0.18s; background: #f1f5f9; color: #64748b;
        }
        .filter-chip:hover { border-color: #cbd5e1; }
        .filter-chip.active-ok   { background: #f0fdf4; color: #16a34a; border-color: #86efac; }
        .filter-chip.active-crit { background: #fef2f2; color: #dc2626; border-color: #fca5a5; }

        /* ── TABLE ── */
        table { width: 100%; border-collapse: collapse; }
        thead tr { background: #f8fafc; }
        th {
            padding: 0.8rem 1.2rem; text-align: left;
            font-size: 0.75rem; font-weight: 700; color: #475569;
            text-transform: uppercase; letter-spacing: 0.05em;
            border-bottom: 1px solid #f1f5f9;
        }
        td {
            padding: 0.85rem 1.2rem; font-size: 0.875rem; color: #334155;
            border-bottom: 1px solid #f8fafc;
        }
        tbody tr { transition: background 0.15s; }
        tbody tr:hover { background: #f8fafc; }
        tbody tr:last-child td { border-bottom: none; }
        .product-name { font-weight: 600; color: #1e293b; }
        .stock-num { font-weight: 700; font-size: 0.95rem; }
        .stock-num--critico { color: #ef4444; }
        .stock-num--ok      { color: #22c55e; }

        /* ── BADGES ── */
        .badge {
            display: inline-flex; align-items: center; gap: 0.35rem;
            padding: 0.3rem 0.75rem; border-radius: 20px;
            font-size: 0.75rem; font-weight: 700;
        }
        .badge--ok   { background: #f0fdf4; color: #16a34a; }
        .badge--crit { background: #fef2f2; color: #dc2626; }
        .badge::before { content: ''; width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

        /* ── EMPTY ── */
        .empty-row td { text-align: center; padding: 3rem; color: #94a3b8; font-size: 0.9rem; }

        /* ── HAMBURGER ── */
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

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            .stats { display: grid; grid-template-columns: repeat(3, 1fr); }
            th:nth-child(3), td:nth-child(3) { display: none; }
        }
    </style>
</head>
<body>

    <button class="hamburger-btn" onclick="toggleSidebar()">
        <i class="fas fa-bars"></i>
    </button>
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
            <% if (esAdmin) { %>
            <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="sidebar__link">
                <i class="fas fa-plus-circle"></i> Registrar producto
            </a>
            <% } %>
            <a href="<%= ctx %>/ProductoControlador?accion=stock" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-chart-bar"></i> Control de stock
            </a>
        </div>

        <% if (esAdmin) { %>
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
        <% } %>
    </aside>

    <main class="main">
        <div class="page-header">
            <h1 class="page-header__title">Control de Stock</h1>
            <p class="page-header__sub">Estado actual del inventario de productos</p>
        </div>

        <%
            int totalProds = 0, totalOk = 0, totalCrit = 0;
            if (productos != null) {
                totalProds = productos.size();
                for (Producto p : productos) {
                    if (p.getStock() > p.getStockMinimo()) totalOk++;
                    else totalCrit++;
                }
            }
        %>
        <div class="stats">
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--total"><i class="fas fa-boxes"></i></div>
                <div>
                    <div class="stat-card__val"><%= totalProds %></div>
                    <div class="stat-card__lbl">Total productos</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--ok"><i class="fas fa-check-circle"></i></div>
                <div>
                    <div class="stat-card__val"><%= totalOk %></div>
                    <div class="stat-card__lbl">Con stock OK</div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--crit"><i class="fas fa-exclamation-triangle"></i></div>
                <div>
                    <div class="stat-card__val"><%= totalCrit %></div>
                    <div class="stat-card__lbl">Stock crítico</div>
                </div>
            </div>
        </div>

        <div class="table-card">
            <div class="table-toolbar">
                <div class="search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" id="buscador" placeholder="Buscar producto..." oninput="filtrar()">
                </div>
                <div class="filter-group">
                    <span>Filtrar:</span>
                    <button class="filter-chip" id="chip-ok"   onclick="toggleFiltro('ok')">OK</button>
                    <button class="filter-chip" id="chip-crit" onclick="toggleFiltro('crit')">Crítico</button>
                </div>
            </div>

            <table id="tablaStock">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Stock actual</th>
                        <th>Stock mínimo</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (productos == null || productos.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="4"><i class="fas fa-box-open" style="font-size:2rem;display:block;margin-bottom:0.5rem;"></i> No hay productos registrados.</td>
                    </tr>
                    <% } else {
                        for (Producto p : productos) {
                            boolean esOk = p.getStock() > p.getStockMinimo();
                            String estado = esOk ? "ok" : "crit";
                    %>
                    <tr data-estado="<%= estado %>" data-nombre="<%= p.getNombre().toLowerCase() %>">
                        <td class="product-name"><%= p.getNombre() %></td>
                        <td class="stock-num <%= esOk ? "stock-num--ok" : "stock-num--critico" %>">
                            <%= p.getStock() %>
                        </td>
                        <td style="color:#64748b;"><%= p.getStockMinimo() %></td>
                        <td>
                            <% if (esOk) { %>
                            <span class="badge badge--ok"><i class="fas fa-check"></i> OK</span>
                            <% } else { %>
                            <span class="badge badge--crit"><i class="fas fa-exclamation"></i> CRÍTICO</span>
                            <% } %>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </main>

    <script>
        let filtroActivo = null;

        function filtrar() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaStock tbody tr[data-estado]').forEach(tr => {
                const nombre  = tr.dataset.nombre  || '';
                const estado  = tr.dataset.estado  || '';
                const coincideTexto  = nombre.includes(texto);
                const coincideFiltro = !filtroActivo || estado === filtroActivo;
                tr.style.display = (coincideTexto && coincideFiltro) ? '' : 'none';
            });
        }

        function toggleFiltro(tipo) {
            filtroActivo = (filtroActivo === tipo) ? null : tipo;
            document.getElementById('chip-ok').classList.toggle('active-ok',   filtroActivo === 'ok');
            document.getElementById('chip-crit').classList.toggle('active-crit', filtroActivo === 'crit');
            filtrar();
        }

        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, java.math.BigDecimal, modelos.Usuario" %>
<%
    BigDecimal     totalVentas   = (BigDecimal)    request.getAttribute("totalVentasMes");
    List<Producto> stockBajo     = (List<Producto>) request.getAttribute("productosStockBajo");
    BigDecimal     totalDeudas   = (BigDecimal)    request.getAttribute("totalDeudasPendientes");
    Integer        totalClientes = (Integer)       request.getAttribute("totalClientes");
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
    <title>Reportes — Tienda Don Pedro</title>
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
        .page-header { margin-bottom: 2rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub   { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── KPI GRID ── */
        .kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.2rem; margin-bottom: 2rem; }
        .kpi {
            background: #fff; border-radius: 14px; padding: 1.3rem 1.2rem;
            box-shadow: 0 1px 4px rgba(0,0,0,0.06);
            display: flex; flex-direction: column; gap: 0.5rem;
            border-top: 3px solid transparent;
        }
        .kpi--ventas   { border-top-color: #22c55e; }
        .kpi--deudas   { border-top-color: #ef4444; }
        .kpi--clientes { border-top-color: #3b82f6; }
        .kpi--stock    { border-top-color: #f59e0b; }
        .kpi__icon-row { display: flex; justify-content: space-between; align-items: center; }
        .kpi__icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1rem; }
        .kpi--ventas   .kpi__icon { background: #f0fdf4; color: #22c55e; }
        .kpi--deudas   .kpi__icon { background: #fef2f2; color: #ef4444; }
        .kpi--clientes .kpi__icon { background: #eff6ff; color: #3b82f6; }
        .kpi--stock    .kpi__icon { background: #fffbeb; color: #f59e0b; }
        .kpi__trend { font-size: 0.72rem; color: #94a3b8; }
        .kpi__val { font-size: 1.6rem; font-weight: 800; color: #1e293b; }
        .kpi__lbl { font-size: 0.78rem; color: #64748b; font-weight: 500; }

        /* ── SECTION TITLE ── */
        .section-title { font-size: 1.05rem; font-weight: 700; color: #1e293b; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem; }
        .section-title i { color: #f59e0b; }

        /* ── TABLE CARD ── */
        .table-card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        thead tr { background: #f8fafc; }
        th { padding: 0.8rem 1.2rem; text-align: left; font-size: 0.75rem; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #f1f5f9; }
        td { padding: 0.9rem 1.2rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        tbody tr { transition: background 0.15s; }
        tbody tr:hover { background: #fafafa; }
        tbody tr:last-child td { border-bottom: none; }
        .td-name { font-weight: 600; color: #1e293b; }
        .td-crit { color: #ef4444; font-weight: 700; }
        .td-bajo { color: #f59e0b; font-weight: 700; }
        .badge-crit { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.25rem 0.6rem; border-radius: 20px; font-size: 0.72rem; font-weight: 700; background: #fef2f2; color: #dc2626; }
        .badge-bajo { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.25rem 0.6rem; border-radius: 20px; font-size: 0.72rem; font-weight: 700; background: #fffbeb; color: #d97706; }
        .empty-row td { text-align: center; padding: 3rem; color: #94a3b8; }

        /* ── HAMBURGER ── */
        .hamburger-btn { display: none; position: fixed; top: 0.8rem; left: 0.8rem; z-index: 150; background: #1e293b; color: #fff; border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer; }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        @media (max-width: 1100px) { .kpis { grid-template-columns: repeat(2, 1fr); } }
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            .kpis { grid-template-columns: repeat(2, 1fr); }
        }
        @media (max-width: 480px) { .kpis { grid-template-columns: 1fr; } }
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
            <a href="<%= ctx %>/ReporteControlador" class="sidebar__link sidebar__link--activo">
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
    </aside>

    <main class="main">
        <div class="page-header">
            <h1 class="page-header__title">Reportes</h1>
            <p class="page-header__sub">Resumen general del negocio</p>
        </div>

        <div class="kpis">
            <div class="kpi kpi--ventas">
                <div class="kpi__icon-row">
                    <div class="kpi__icon"><i class="fas fa-dollar-sign"></i></div>
                    <span class="kpi__trend">Este mes</span>
                </div>
                <div class="kpi__val">$<%= totalVentas != null ? String.format("%,.0f", totalVentas) : "0" %></div>
                <div class="kpi__lbl">Ventas del mes</div>
            </div>
            <div class="kpi kpi--deudas">
                <div class="kpi__icon-row">
                    <div class="kpi__icon"><i class="fas fa-file-invoice-dollar"></i></div>
                    <span class="kpi__trend">Pendiente</span>
                </div>
                <div class="kpi__val">$<%= totalDeudas != null ? String.format("%,.0f", totalDeudas) : "0" %></div>
                <div class="kpi__lbl">Deudas pendientes</div>
            </div>
            <div class="kpi kpi--clientes">
                <div class="kpi__icon-row">
                    <div class="kpi__icon"><i class="fas fa-users"></i></div>
                    <span class="kpi__trend">Registrados</span>
                </div>
                <div class="kpi__val"><%= totalClientes != null ? totalClientes : "0" %></div>
                <div class="kpi__lbl">Clientes</div>
            </div>
            <div class="kpi kpi--stock">
                <div class="kpi__icon-row">
                    <div class="kpi__icon"><i class="fas fa-exclamation-triangle"></i></div>
                    <span class="kpi__trend">Atención</span>
                </div>
                <div class="kpi__val"><%= stockBajo != null ? stockBajo.size() : "0" %></div>
                <div class="kpi__lbl">Stock bajo/crítico</div>
            </div>
        </div>

        <div class="section-title">
            <i class="fas fa-exclamation-triangle"></i>
            Productos con stock bajo o crítico
        </div>
        <div class="table-card">
            <table>
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Stock actual</th>
                        <th>Stock mínimo</th>
                        <th>Precio</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (stockBajo == null || stockBajo.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="5">
                            <i class="fas fa-check-circle" style="font-size:2rem;color:#22c55e;display:block;margin-bottom:0.5rem;"></i>
                            Todos los productos tienen stock suficiente.
                        </td>
                    </tr>
                    <% } else {
                        for (Producto p : stockBajo) {
                            boolean esCritico = p.getStock() == 0 || (p.getStockMinimo() > 0 && p.getStock() <= p.getStockMinimo() * 0.2);
                    %>
                    <tr>
                        <td class="td-name"><%= p.getNombre() %></td>
                        <td class="<%= esCritico ? "td-crit" : "td-bajo" %>"><%= p.getStock() %></td>
                        <td style="color:#64748b;"><%= p.getStockMinimo() %></td>
                        <td style="color:#64748b;">$<%= String.format("%,.0f", p.getPrecio()) %></td>
                        <td>
                            <% if (esCritico) { %>
                            <span class="badge-crit"><i class="fas fa-times-circle"></i> CRÍTICO</span>
                            <% } else { %>
                            <span class="badge-bajo"><i class="fas fa-arrow-down"></i> BAJO</span>
                            <% } %>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
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

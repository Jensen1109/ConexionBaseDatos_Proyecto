<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Deuda, java.math.BigDecimal, modelos.Usuario" %>
<%
    List<Deuda> deudas         = (List<Deuda>)    request.getAttribute("deudas");
    BigDecimal  totalPendiente = (BigDecimal)     request.getAttribute("totalPendiente");
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
    request.setAttribute("_paginaActiva", "deudores");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Deudores — Tienda Don Pedro</title>
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
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.8rem; flex-wrap: wrap; gap: 1rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub   { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }
        .btn-primary { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.6rem 1.2rem; background: #ef4444; color: #fff; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; text-decoration: none; cursor: pointer; transition: background 0.18s; }
        .btn-primary:hover { background: #dc2626; }

        /* ── KPI ── */
        .kpi-banner {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            border-radius: 14px; padding: 1.3rem 1.5rem;
            display: flex; align-items: center; gap: 1rem;
            margin-bottom: 1.8rem; color: #fff;
            box-shadow: 0 4px 16px rgba(239,68,68,0.3);
        }
        .kpi-banner__icon { font-size: 2rem; opacity: 0.85; }
        .kpi-banner__val  { font-size: 1.8rem; font-weight: 800; }
        .kpi-banner__lbl  { font-size: 0.85rem; opacity: 0.85; }

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
        tbody tr:hover { background: #fef9f9; }
        tbody tr:last-child td { border-bottom: none; }
        .td-name  { font-weight: 600; color: #1e293b; }
        .td-monto { font-weight: 700; color: #ef4444; font-size: 1rem; }
        .td-fecha { color: #94a3b8; font-size: 0.8rem; }

        /* ── ABONO FORM ── */
        .abono-form { display: flex; align-items: center; gap: 0.5rem; }
        .abono-input {
            width: 100px; padding: 0.45rem 0.7rem;
            border: 1.5px solid #e2e8f0; border-radius: 7px;
            font-size: 0.82rem; color: #1e293b; background: #f8fafc;
            font-family: inherit; transition: border-color 0.18s;
        }
        .abono-input:focus { outline: none; border-color: #22c55e; background: #fff; }
        .abono-input::-webkit-outer-spin-button,
        .abono-input::-webkit-inner-spin-button { -webkit-appearance: none; }
        .abono-input[type=number] { -moz-appearance: textfield; }
        .btn-abono { padding: 0.45rem 0.7rem; background: #22c55e; color: #fff; border: none; border-radius: 7px; cursor: pointer; font-size: 0.875rem; transition: background 0.18s; }
        .btn-abono:hover { background: #16a34a; }

        /* ── AVATAR ── */
        .avatar { width: 32px; height: 32px; border-radius: 50%; background: #fef2f2; color: #ef4444; display: inline-flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: 700; margin-right: 0.5rem; }

        /* ── EMPTY ── */
        .empty-row td { text-align: center; padding: 3.5rem; color: #94a3b8; }
        .empty-row i  { font-size: 2.5rem; display: block; margin-bottom: 0.5rem; }

        /* ── HAMBURGER ── */
        .hamburger-btn { display: none; position: fixed; top: 0.8rem; left: 0.8rem; z-index: 150; background: #1e293b; color: #fff; border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer; }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
            th:nth-child(3), td:nth-child(3) { display: none; }
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
            <div>
                <h1 class="page-header__title">Deudores</h1>
                <p class="page-header__sub"><%= deudas != null ? deudas.size() : 0 %> deuda(s) activa(s)</p>
            </div>
        </div>

        <% if (totalPendiente != null) { %>
        <div class="kpi-banner">
            <div class="kpi-banner__icon"><i class="fas fa-file-invoice-dollar"></i></div>
            <div>
                <div class="kpi-banner__val">$<%= String.format("%,.0f", totalPendiente) %></div>
                <div class="kpi-banner__lbl">Total pendiente por cobrar</div>
            </div>
        </div>
        <% } %>

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
            <table id="tablaDeudores">
                <thead>
                    <tr>
                        <th>Cliente</th>
                        <th>Monto pendiente</th>
                        <th>Último abono</th>
                        <th>Registrar abono</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (deudas == null || deudas.isEmpty()) { %>
                    <tr class="empty-row">
                        <td colspan="4">
                            <i class="fas fa-check-circle" style="color:#22c55e;"></i>
                            No hay deudas activas. ¡Todo al día!
                        </td>
                    </tr>
                    <% } else {
                        for (Deuda d : deudas) {
                            String cliente    = d.getNombreCliente() != null ? d.getNombreCliente() : "Pedido #" + d.getIdPedido();
                            String inicial    = !cliente.isEmpty() ? String.valueOf(cliente.charAt(0)).toUpperCase() : "?";
                            String fechaAbono = d.getFechaAbono() != null ? d.getFechaAbono().toString() : "—";
                            String ultimoAbono = d.getAbono() != null ? "$" + String.format("%,.0f", d.getAbono()) : "$0";
                    %>
                    <tr data-cliente="<%= cliente.toLowerCase() %>">
                        <td class="td-name">
                            <span class="avatar"><%= inicial %></span>
                            <%= cliente %>
                        </td>
                        <td class="td-monto">$<%= String.format("%,.0f", d.getMontoPendiente()) %></td>
                        <td class="td-fecha"><%= ultimoAbono %> &nbsp;·&nbsp; <%= fechaAbono %></td>
                        <td>
                            <form class="abono-form" action="<%= ctx %>/DeudaControlador" method="post"
                                  onsubmit="return prepararRecibo(this, '<%= cliente.replace("'", "\\'") %>', <%= d.getMontoPendiente().toPlainString() %>)">
                                <input type="hidden" name="idDeuda" value="<%= d.getIdDeuda() %>">
                                <input type="number" name="monto" class="abono-input"
                                       placeholder="$0" min="0.01" step="0.01" required>
                                <button type="submit" class="btn-abono" title="Confirmar abono">
                                    <i class="fas fa-check"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
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

        function filtrar() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaDeudores tbody tr[data-cliente]').forEach(tr => {
                tr.style.display = tr.dataset.cliente.includes(texto) ? '' : 'none';
            });
        }
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }

        /* ── COMPROBANTE DE ABONO ── */

        /** Valida el abono y guarda los datos del recibo antes de enviar el formulario */
        function prepararRecibo(form, nombreCliente, montoPendiente) {
            var monto = parseFloat(form.monto.value);
            if (isNaN(monto) || monto <= 0) {
                mostrarToast('Ingresa un monto válido mayor a 0.');
                return false;
            }
            if (monto > montoPendiente) {
                mostrarToast('El abono ($' + monto.toLocaleString('es-CO') + ') supera el monto pendiente ($' + montoPendiente.toLocaleString('es-CO') + ').');
                return false;
            }

            // Guardar datos del recibo en sessionStorage para mostrarlo después del redirect
            var recibo = {
                cliente: nombreCliente,
                abono: monto,
                pendienteAnterior: montoPendiente,
                pendienteNuevo: montoPendiente - monto,
                fecha: new Date().toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' }),
                hora: new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })
            };
            sessionStorage.setItem('reciboAbono', JSON.stringify(recibo));
            return true; // Enviar formulario
        }

        /** Abre la ventana de impresión con el comprobante */
        function mostrarRecibo(recibo) {
            var ventana = window.open('', 'Comprobante', 'width=400,height=550');
            ventana.document.write(
                '<!DOCTYPE html><html><head><meta charset="UTF-8">' +
                '<title>Comprobante de Abono</title>' +
                '<style>' +
                    'body { font-family: "Segoe UI", sans-serif; padding: 20px; max-width: 350px; margin: 0 auto; color: #1e293b; }' +
                    '.header { text-align: center; border-bottom: 2px dashed #cbd5e1; padding-bottom: 12px; margin-bottom: 15px; }' +
                    '.header h2 { font-size: 1.1rem; margin: 0 0 4px; }' +
                    '.header p { font-size: 0.75rem; color: #64748b; margin: 2px 0; }' +
                    '.titulo { text-align: center; font-size: 0.95rem; font-weight: 700; margin: 12px 0; color: #22c55e; text-transform: uppercase; letter-spacing: 1px; }' +
                    '.linea { display: flex; justify-content: space-between; padding: 6px 0; font-size: 0.85rem; border-bottom: 1px solid #f1f5f9; }' +
                    '.linea:last-child { border-bottom: none; }' +
                    '.label { color: #64748b; }' +
                    '.valor { font-weight: 600; }' +
                    '.valor-abono { color: #22c55e; font-size: 1.1rem; font-weight: 800; }' +
                    '.valor-pendiente { color: #ef4444; font-weight: 700; }' +
                    '.footer { text-align: center; border-top: 2px dashed #cbd5e1; padding-top: 12px; margin-top: 15px; font-size: 0.72rem; color: #94a3b8; }' +
                    '.btn-imprimir { display: block; width: 100%; padding: 10px; margin-top: 15px; background: #3b82f6; color: #fff; border: none; border-radius: 8px; font-size: 0.9rem; font-weight: 600; cursor: pointer; }' +
                    '.btn-imprimir:hover { background: #2563eb; }' +
                    '@media print { .btn-imprimir { display: none; } }' +
                '</style></head><body>' +

                '<div class="header">' +
                    '<h2>Tienda Don Pedro</h2>' +
                    '<p>Comprobante de Abono</p>' +
                    '<p>' + recibo.fecha + ' — ' + recibo.hora + '</p>' +
                '</div>' +

                '<div class="titulo">Abono registrado</div>' +

                '<div class="linea"><span class="label">Cliente</span><span class="valor">' + recibo.cliente + '</span></div>' +
                '<div class="linea"><span class="label">Deuda anterior</span><span class="valor">$' + recibo.pendienteAnterior.toLocaleString('es-CO') + '</span></div>' +
                '<div class="linea"><span class="label">Abono realizado</span><span class="valor-abono">$' + recibo.abono.toLocaleString('es-CO') + '</span></div>' +
                '<div class="linea"><span class="label">Saldo pendiente</span><span class="valor-pendiente">' +
                    (recibo.pendienteNuevo <= 0 ? 'PAGADA' : '$' + recibo.pendienteNuevo.toLocaleString('es-CO')) +
                '</span></div>' +

                '<div class="footer">' +
                    '<p>Este comprobante es un respaldo del abono registrado.</p>' +
                    '<p>Gracias por su pago.</p>' +
                '</div>' +

                '<button class="btn-imprimir" onclick="window.print()">Imprimir comprobante</button>' +

                '</body></html>'
            );
            ventana.document.close();
        }

        /* ── PERSISTENCIA DE ABONOS Y BÚSQUEDA ── */
        function guardarDatosDeudores() {
            var datos = { busqueda: document.getElementById('buscador').value, abonos: {} };
            document.querySelectorAll('.abono-form').forEach(function(form) {
                var id = form.querySelector('input[name="idDeuda"]').value;
                var monto = form.querySelector('input[name="monto"]').value;
                if (monto) datos.abonos[id] = monto;
            });
            sessionStorage.setItem('deudoresDatos', JSON.stringify(datos));
        }
        function restaurarDatosDeudores() {
            var datos = sessionStorage.getItem('deudoresDatos');
            if (!datos) return;
            var obj = JSON.parse(datos);
            if (obj.busqueda) {
                document.getElementById('buscador').value = obj.busqueda;
                filtrar();
            }
            if (obj.abonos) {
                document.querySelectorAll('.abono-form').forEach(function(form) {
                    var id = form.querySelector('input[name="idDeuda"]').value;
                    if (obj.abonos[id]) form.querySelector('input[name="monto"]').value = obj.abonos[id];
                });
            }
        }
        document.getElementById('buscador').addEventListener('input', guardarDatosDeudores);
        document.querySelectorAll('.abono-input').forEach(function(inp) {
            inp.addEventListener('input', guardarDatosDeudores);
        });
        restaurarDatosDeudores();

        // Al cargar la página, verificar si hay un recibo pendiente de mostrar
        (function() {
            var datos = sessionStorage.getItem('reciboAbono');
            if (datos) {
                sessionStorage.removeItem('reciboAbono');
                sessionStorage.removeItem('deudoresDatos');
                var recibo = JSON.parse(datos);
                setTimeout(function() { mostrarRecibo(recibo); }, 300);
            }
        })();
    </script>
</body>
</html>

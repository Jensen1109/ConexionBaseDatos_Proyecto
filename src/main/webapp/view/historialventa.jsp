<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Pedido" %>
<%
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/historialventa.css">
    <link rel="stylesheet" href="<%= ctx %>/css/historialventa-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Historial de ventas</title>
</head>

<body>
    <aside class="sidebar">
        <input type="checkbox" id="hamburger-toggle" class="hamburger-checkbox">
        <label for="hamburger-toggle" class="hamburger-overlay"></label>

        <nav class="hamburger-nav">
            <label for="hamburger-toggle" class="hamburger-close">
                <i class="fa-solid fa-xmark"></i>
            </label>
            <h2 class="hamburger-titulo">Tienda Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="hamburger-link hamburger-link--activo">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="hamburger-link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="hamburger-link">Deudores</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda<br>Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="sidebar__link sidebar__link--activo">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="sidebar__link">Deudores</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span><span></span><span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="historial-venta">

            <div class="historial-venta__busqueda">
                <i class="fas fa-search historial-venta__icono-busqueda"></i>
                <input type="text" id="buscador" class="historial-venta__input-busqueda"
                       placeholder="Buscar cliente..." oninput="filtrar()">
            </div>

            <table class="contenedor-tabla" id="tablaHistorial">
                <thead class="historial-venta__tabla-head">
                    <tr>
                        <th class="historial-venta__columna">Fecha</th>
                        <th class="historial-venta__columna">Cliente</th>
                        <th class="historial-venta__columna">Total</th>
                        <th class="historial-venta__columna">Estado</th>
                    </tr>
                </thead>
                <tbody class="historial-venta__tabla-body">
                    <% if (pedidos == null || pedidos.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;">No hay ventas registradas.</td>
                    </tr>
                    <% } else {
                        for (Pedido p : pedidos) {
                            String claseEstado = "completado".equals(p.getEstado())
                                ? "historial-venta__estado--completado"
                                : "historial-venta__estado--pendiente";
                            String fechaStr = p.getFechaVenta() != null
                                ? p.getFechaVenta().toLocalDate().toString() : "-";
                            String cliente = p.getNombreCliente() != null
                                ? p.getNombreCliente() : "Cliente #" + p.getIdCliente();
                    %>
                    <tr class="historial-venta__item">
                        <td class="historial-venta__valor" data-label="Fecha"><%= fechaStr %></td>
                        <td class="historial-venta__valor" data-label="Cliente"><%= cliente %></td>
                        <td class="historial-venta__valor" data-label="Total">$<%= p.getTotal() %></td>
                        <td class="historial-venta__valor <%= claseEstado %>" data-label="Estado">
                            <%= p.getEstado() %>
                        </td>
                    </tr>
                    <%  }
                    } %>
                </tbody>
            </table>

        </div>
    </main>

    <script>
        function filtrar() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaHistorial tbody tr.historial-venta__item').forEach(tr => {
                const cliente = tr.querySelectorAll('td')[1].textContent.toLowerCase();
                tr.style.display = cliente.includes(texto) ? '' : 'none';
            });
        }
    </script>
</body>

</html>

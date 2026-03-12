<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Deuda, java.math.BigDecimal" %>
<%
    List<Deuda> deudas        = (List<Deuda>)    request.getAttribute("deudas");
    BigDecimal  totalPendiente = (BigDecimal)     request.getAttribute("totalPendiente");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/deudores.css">
    <link rel="stylesheet" href="<%= ctx %>/css/deudores-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Deudores</title>
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
            <a href="<%= ctx %>/PedidoControlador"             class="hamburger-link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="hamburger-link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="hamburger-link hamburger-link--activo">Deudores</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="sidebar__link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="sidebar__link sidebar__link--activo">Deudores</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span><span></span><span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="deudores">
            <h1 class="deudores__titulo">Deudores</h1>

            <% if (totalPendiente != null) { %>
            <p class="deudores__total">Total pendiente: <strong>$<%= totalPendiente %></strong></p>
            <% } %>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <p style="color:red;"><%= error %></p>
            <% } %>

            <table class="contenedor-tabla">
                <thead class="deudores__tabla-head">
                    <tr>
                        <th class="deudores__columna">Cliente</th>
                        <th class="deudores__columna">Monto pendiente</th>
                        <th class="deudores__columna">Último abono</th>
                        <th class="deudores__columna">Registrar abono</th>
                    </tr>
                </thead>
                <tbody class="deudores__tabla-body">
                    <% if (deudas == null || deudas.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;">No hay deudas pendientes.</td>
                    </tr>
                    <% } else {
                        for (Deuda d : deudas) {
                            String cliente   = d.getNombreCliente() != null
                                               ? d.getNombreCliente() : "Pedido #" + d.getIdPedido();
                            String fechaAbono = d.getFechaAbono() != null
                                               ? d.getFechaAbono().toString() : "-";
                            String ultimoAbono = d.getAbono() != null
                                               ? "$" + d.getAbono() : "$0";
                    %>
                    <tr class="deudores__item">
                        <td class="deudores__valor" data-label="Cliente"><%= cliente %></td>
                        <td class="deudores__valor deudores__monto" data-label="Monto">
                            $<%= d.getMontoPendiente() %>
                        </td>
                        <td class="deudores__valor" data-label="Último abono">
                            <%= ultimoAbono %> (<%= fechaAbono %>)
                        </td>
                        <td class="deudores__valor deudores__abono-celda" data-label="Abono">
                            <form action="<%= ctx %>/DeudaControlador" method="post">
                                <input type="hidden" name="idDeuda" value="<%= d.getIdDeuda() %>">
                                <input type="number" name="monto" class="deudores__input-abono"
                                       placeholder="$0" min="0.01" step="0.01" required>
                                <button type="submit" class="deudores__btn-abonar">
                                    <i class="fa-solid fa-check"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <%  }
                    } %>
                </tbody>
            </table>

        </div>
    </main>
</body>

</html>

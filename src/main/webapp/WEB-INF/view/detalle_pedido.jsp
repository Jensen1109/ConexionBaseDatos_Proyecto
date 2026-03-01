<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.DetallePedido" %>
<%
    List<DetallePedido> detalles = (List<DetallePedido>) request.getAttribute("detalles");
    Integer             idPedido = (Integer)             request.getAttribute("idPedido");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Detalle del pedido</title>
</head>
<body>
<main class="main">
    <h1>Detalle — Pedido #<%= idPedido %></h1>
    <a href="<%= ctx %>/PedidoControlador">← Volver a pedidos</a>

    <table border="1" cellpadding="6">
        <thead>
            <tr>
                <th>ID Detalle</th><th>Producto ID</th>
                <th>Cantidad</th><th>Precio unitario</th><th>Subtotal</th>
            </tr>
        </thead>
        <tbody>
        <% java.math.BigDecimal totalGeneral = java.math.BigDecimal.ZERO;
           if (detalles != null) for (DetallePedido d : detalles) {
               java.math.BigDecimal subtotal =
                   d.getPrecioUnitario().multiply(
                       java.math.BigDecimal.valueOf(d.getCantidadVendida()));
               totalGeneral = totalGeneral.add(subtotal); %>
            <tr>
                <td><%= d.getIdDetalle() %></td>
                <td><%= d.getIdProducto() %></td>
                <td><%= d.getCantidadVendida() %></td>
                <td>$<%= d.getPrecioUnitario() %></td>
                <td>$<%= subtotal %></td>
            </tr>
        <% } %>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="4"><strong>Total</strong></td>
                <td><strong>$<%= totalGeneral %></strong></td>
            </tr>
        </tfoot>
    </table>
</main>
</body>
</html>

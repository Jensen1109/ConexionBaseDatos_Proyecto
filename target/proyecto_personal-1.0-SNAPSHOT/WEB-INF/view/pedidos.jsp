<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Pedido, modelos.Producto, modelos.MetodoPago" %>
<%
    List<Pedido>     pedidos     = (List<Pedido>)     request.getAttribute("pedidos");
    List<Producto>   productos   = (List<Producto>)   request.getAttribute("productos");
    List<MetodoPago> metodosPago = (List<MetodoPago>) request.getAttribute("metodosPago");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Pedidos</title>
</head>
<body>
<main class="main">
    <h1>Pedidos</h1>

    <table border="1" cellpadding="6">
        <thead>
            <tr>
                <th>ID</th><th>Cliente ID</th><th>Fecha</th>
                <th>Total</th><th>Estado</th><th>Acciones</th>
            </tr>
        </thead>
        <tbody>
        <% if (pedidos != null) for (Pedido p : pedidos) { %>
            <tr>
                <td><%= p.getIdPedido() %></td>
                <td><%= p.getIdCliente() %></td>
                <td><%= p.getFechaVenta() %></td>
                <td>$<%= p.getTotal() %></td>
                <td><%= p.getEstado() %></td>
                <td>
                    <a href="<%= ctx %>/DetallePedidoControlador?idPedido=<%= p.getIdPedido() %>">Ver detalle</a>
                    <form action="<%= ctx %>/PedidoControlador" method="post" style="display:inline">
                        <input type="hidden" name="accion"   value="cambiarEstado">
                        <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
                        <select name="estado">
                            <option value="pendiente">Pendiente</option>
                            <option value="entregado">Entregado</option>
                            <option value="cancelado">Cancelado</option>
                        </select>
                        <button type="submit">Cambiar</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>

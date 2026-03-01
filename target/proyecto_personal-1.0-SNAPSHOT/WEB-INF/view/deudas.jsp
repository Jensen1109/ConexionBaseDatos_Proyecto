<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Deuda, java.math.BigDecimal" %>
<%
    List<Deuda>  deudas         = (List<Deuda>) request.getAttribute("deudas");
    BigDecimal   totalPendiente = (BigDecimal)  request.getAttribute("totalPendiente");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/deudores.css">
    <title>Deudas</title>
</head>
<body>
<main class="main">
    <h1>Deudas pendientes</h1>
    <p><strong>Total pendiente: $<%= totalPendiente %></strong></p>

    <table border="1" cellpadding="6">
        <thead>
            <tr>
                <th>ID</th><th>Pedido</th><th>Monto pendiente</th>
                <th>Abono</th><th>Fecha abono</th><th>Estado</th><th>Registrar abono</th>
            </tr>
        </thead>
        <tbody>
        <% if (deudas != null) for (Deuda d : deudas) { %>
            <tr>
                <td><%= d.getIdDeuda() %></td>
                <td><%= d.getIdPedido() %></td>
                <td>$<%= d.getMontoPendiente() %></td>
                <td>$<%= d.getAbono() != null ? d.getAbono() : "0" %></td>
                <td><%= d.getFechaAbono() != null ? d.getFechaAbono() : "-" %></td>
                <td><%= d.getEstado() %></td>
                <td>
                    <form action="<%= ctx %>/DeudaControlador" method="post">
                        <input type="hidden" name="idDeuda" value="<%= d.getIdDeuda() %>">
                        <input type="number" name="monto" step="0.01" min="0.01" placeholder="Monto" required>
                        <button type="submit">Abonar</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.MetodoPago" %>
<%
    List<MetodoPago> metodos = (List<MetodoPago>) request.getAttribute("metodosPago");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Métodos de pago</title>
</head>
<body>
<main class="main">
    <h1>Métodos de pago</h1>

    <form action="<%= ctx %>/MetodoPagoControlador" method="post">
        <input type="hidden" name="accion" value="crear">
        <input type="text" name="nombre" placeholder="Ej: Efectivo, Nequi…" required>
        <button type="submit">Agregar</button>
    </form>

    <table border="1" cellpadding="6">
        <thead>
            <tr><th>ID</th><th>Nombre</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <% if (metodos != null) for (MetodoPago m : metodos) { %>
            <tr>
                <td><%= m.getIdPago() %></td>
                <td><%= m.getNombre() %></td>
                <td>
                    <form action="<%= ctx %>/MetodoPagoControlador" method="post" style="display:inline">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="id"     value="<%= m.getIdPago() %>">
                        <button type="submit">Eliminar</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>

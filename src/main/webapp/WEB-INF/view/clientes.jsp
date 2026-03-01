<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Usuario" %>
<%
    List<Usuario> clientes = (List<Usuario>) request.getAttribute("clientes");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Clientes</title>
</head>
<body>
<main class="main">
    <h1>Clientes registrados</h1>

    <table border="1" cellpadding="6">
        <thead>
            <tr>
                <th>ID</th><th>Nombre</th><th>Apellido</th>
                <th>Email</th><th>Cédula</th><th>Teléfonos</th><th>Pedidos</th>
            </tr>
        </thead>
        <tbody>
        <% if (clientes != null) for (Usuario u : clientes) { %>
            <tr>
                <td><%= u.getIdUsuario() %></td>
                <td><%= u.getNombre() %></td>
                <td><%= u.getApellido() %></td>
                <td><%= u.getEmail() %></td>
                <td><%= u.getCedula() %></td>
                <td>
                    <a href="<%= ctx %>/TelefonoControlador?idUsuario=<%= u.getIdUsuario() %>">Ver</a>
                </td>
                <td>
                    <a href="<%= ctx %>/PedidoControlador?idCliente=<%= u.getIdUsuario() %>">Ver</a>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>

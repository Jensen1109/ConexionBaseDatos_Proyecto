<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Telefono" %>
<%
    List<Telefono> telefonos = (List<Telefono>) request.getAttribute("telefonos");
    Integer        idUsuario = (Integer)        request.getAttribute("idUsuario");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Teléfonos</title>
</head>
<body>
<main class="main">
    <h1>Teléfonos — Usuario #<%= idUsuario %></h1>
    <a href="<%= ctx %>/ClienteControlador">← Volver a clientes</a>

    <form action="<%= ctx %>/TelefonoControlador" method="post">
        <input type="hidden" name="idUsuario" value="<%= idUsuario %>">
        <input type="hidden" name="accion"    value="agregar">
        <input type="tel"   name="telefono"  placeholder="Número de teléfono" required>
        <button type="submit">Agregar</button>
    </form>

    <table border="1" cellpadding="6">
        <thead>
            <tr><th>ID</th><th>Teléfono</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <% if (telefonos != null) for (Telefono t : telefonos) { %>
            <tr>
                <td><%= t.getIdTelefono() %></td>
                <td><%= t.getTelefono() %></td>
                <td>
                    <form action="<%= ctx %>/TelefonoControlador" method="post" style="display:inline">
                        <input type="hidden" name="idUsuario"  value="<%= idUsuario %>">
                        <input type="hidden" name="idTelefono" value="<%= t.getIdTelefono() %>">
                        <input type="hidden" name="accion"     value="eliminar">
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Categoria" %>
<%
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Categorías</title>
</head>
<body>
<main class="main">
    <h1>Categorías</h1>

    <% String error = (String) request.getAttribute("error");
       if (error != null) { %>
        <p style="color:red"><%= error %></p>
    <% } %>

    <form action="<%= ctx %>/CategoriaControlador" method="post">
        <input type="hidden" name="accion" value="crear">
        <input type="text" name="nombre" placeholder="Nueva categoría" required>
        <button type="submit">Agregar</button>
    </form>

    <table border="1" cellpadding="6">
        <thead>
            <tr><th>ID</th><th>Nombre</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <% if (categorias != null) for (Categoria c : categorias) { %>
            <tr>
                <td><%= c.getIdCategoria() %></td>
                <td><%= c.getNombre() %></td>
                <td>
                    <form action="<%= ctx %>/CategoriaControlador" method="post" style="display:inline">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="id" value="<%= c.getIdCategoria() %>">
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

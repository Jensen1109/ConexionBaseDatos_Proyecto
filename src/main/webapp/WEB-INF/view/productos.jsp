<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Categoria" %>
<%
    List<Producto>  productos  = (List<Producto>)  request.getAttribute("productos");
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/productos.css">
    <title>Productos</title>
</head>
<body>
<main class="main">
    <h1>Productos</h1>

    <form action="<%= ctx %>/ProductoControlador" method="post">
        <input type="hidden" name="accion" value="crear">
        <input  type="text"   name="nombre"         placeholder="Nombre"        required>
        <input  type="text"   name="descripcion"    placeholder="Descripción">
        <input  type="number" name="precio"         placeholder="Precio"        step="0.01" required>
        <input  type="number" name="stock"          placeholder="Stock"         required>
        <input  type="number" name="stockMinimo"    placeholder="Stock mínimo"  required>
        <input  type="text"   name="unidadMedida"   placeholder="Unidad (kg, unidad…)">
        <input  type="date"   name="fechaVencimiento">
        <select name="idCategoria" required>
            <option value="">-- Categoría --</option>
            <% if (categorias != null) for (Categoria c : categorias) { %>
                <option value="<%= c.getIdCategoria() %>"><%= c.getNombre() %></option>
            <% } %>
        </select>
        <button type="submit">Agregar producto</button>
    </form>

    <table border="1" cellpadding="6">
        <thead>
            <tr>
                <th>ID</th><th>Nombre</th><th>Precio</th>
                <th>Stock</th><th>Mín.</th><th>Unidad</th><th>Acciones</th>
            </tr>
        </thead>
        <tbody>
        <% if (productos != null) for (Producto p : productos) { %>
            <tr>
                <td><%= p.getIdProducto() %></td>
                <td><%= p.getNombre() %></td>
                <td>$<%= p.getPrecio() %></td>
                <td><%= p.getStock() %></td>
                <td><%= p.getStockMinimo() %></td>
                <td><%= p.getUnidadMedida() %></td>
                <td>
                    <a href="<%= ctx %>/ImagenControlador?idProducto=<%= p.getIdProducto() %>">Imágenes</a>
                    <form action="<%= ctx %>/ProductoControlador" method="post" style="display:inline">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="id"     value="<%= p.getIdProducto() %>">
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

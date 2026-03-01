<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Imagen" %>
<%
    List<Imagen> imagenes   = (List<Imagen>) request.getAttribute("imagenes");
    Integer      idProducto = (Integer)      request.getAttribute("idProducto");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Imágenes del producto</title>
</head>
<body>
<main class="main">
    <h1>Imágenes — Producto #<%= idProducto %></h1>
    <a href="<%= ctx %>/ProductoControlador">← Volver a productos</a>

    <form action="<%= ctx %>/ImagenControlador" method="post">
        <input type="hidden" name="idProducto" value="<%= idProducto %>">
        <input type="hidden" name="accion"     value="agregar">
        <input type="url"   name="url" placeholder="URL de la imagen" required style="width:400px">
        <button type="submit">Agregar imagen</button>
    </form>

    <div style="display:flex;flex-wrap:wrap;gap:12px;margin-top:16px">
    <% if (imagenes != null) for (Imagen img : imagenes) { %>
        <div style="text-align:center">
            <img src="<%= img.getUrl() %>" alt="imagen" style="width:150px;height:150px;object-fit:cover">
            <br>
            <form action="<%= ctx %>/ImagenControlador" method="post">
                <input type="hidden" name="idProducto" value="<%= idProducto %>">
                <input type="hidden" name="idImagen"   value="<%= img.getIdImagen() %>">
                <input type="hidden" name="accion"     value="eliminar">
                <button type="submit">Eliminar</button>
            </form>
        </div>
    <% } %>
    </div>
</main>
</body>
</html>

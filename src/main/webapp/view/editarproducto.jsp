<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Categoria" %>
<%
    Producto producto   = (Producto)            request.getAttribute("producto");
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    String error = (String) request.getAttribute("error");
    String ctx   = request.getContextPath();

    if (producto == null) {
        response.sendRedirect(ctx + "/ProductoControlador");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/editarproducto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Edición de producto</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <a href="<%= ctx %>/ProductoControlador" class="header__link">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Edición de producto</h2>
        </div>
    </header>

    <main class="main">

        <% if (error != null) { %>
            <p style="color:red;"><%= error %></p>
        <% } %>

        <form class="formulario" action="<%= ctx %>/ProductoControlador" method="post">
            <input type="hidden" name="accion" value="actualizar">
            <input type="hidden" name="id"     value="<%= producto.getIdProducto() %>">

            <div class="formulario__producto">
                <label for="nombre" class="formulario__etiqueta">Nombre del producto</label>
                <input type="text" id="nombre" name="nombre" class="formulario__input"
                    value="<%= producto.getNombre() %>" required>
            </div>

            <div class="formulario__producto">
                <label for="descripcion" class="formulario__etiqueta">Descripción</label>
                <textarea id="descripcion" name="descripcion" class="formulario__input"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>
            </div>

            <div class="formulario__producto">
                <label for="precio" class="formulario__etiqueta">Precio</label>
                <input type="number" id="precio" name="precio" class="formulario__input"
                    value="<%= producto.getPrecio() %>" step="0.01" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="stock" class="formulario__etiqueta">Cantidad (stock)</label>
                <input type="number" id="stock" name="stock" class="formulario__input"
                    value="<%= producto.getStock() %>" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="stockMinimo" class="formulario__etiqueta">Stock mínimo</label>
                <input type="number" id="stockMinimo" name="stockMinimo" class="formulario__input"
                    value="<%= producto.getStockMinimo() %>" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="unidadMedida" class="formulario__etiqueta">Unidad de medida</label>
                <input type="text" id="unidadMedida" name="unidadMedida" class="formulario__input"
                    value="<%= producto.getUnidadMedida() != null ? producto.getUnidadMedida() : "" %>">
            </div>

            <div class="formulario__producto">
                <label for="fechaVencimiento" class="formulario__etiqueta">Fecha de vencimiento</label>
                <input type="date" id="fechaVencimiento" name="fechaVencimiento" class="formulario__input"
                    value="<%= producto.getFechaVencimiento() != null ? producto.getFechaVencimiento().toString() : "" %>">
            </div>

            <div class="formulario__producto">
                <label for="idCategoria" class="formulario__etiqueta">Categoría</label>
                <select id="idCategoria" name="idCategoria" class="formulario__input" required>
                    <option value="">-- Seleccione una categoría --</option>
                    <% if (categorias != null) {
                        for (Categoria c : categorias) {
                            boolean seleccionada = c.getIdCategoria() == producto.getIdCategoria();
                    %>
                    <option value="<%= c.getIdCategoria() %>" <%= seleccionada ? "selected" : "" %>><%= c.getNombre() %></option>
                    <%  }
                    } %>
                </select>
            </div>

            <button type="submit" class="formulario__boton">Confirmar edición</button>
        </form>

    </main>
</body>

</html>

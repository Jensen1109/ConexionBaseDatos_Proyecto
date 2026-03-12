<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Categoria" %>
<%
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/registroProducto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Registro de producto</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <a class="header__link" href="<%= ctx %>/ProductoControlador">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Registrar Producto</h2>
        </div>
    </header>

    <main class="main">

        <% if (error != null) { %>
            <p style="color:red;"><%= error %></p>
        <% } %>

        <form class="formulario" action="<%= ctx %>/ProductoControlador" method="post">
            <input type="hidden" name="accion" value="insertar">

            <div class="formulario__producto">
                <label for="nombre" class="formulario__etiqueta">Nombre del producto</label>
                <input type="text" id="nombre" name="nombre" class="formulario__input"
                    placeholder="Ingrese el nombre" required>
            </div>

            <div class="formulario__producto">
                <label for="descripcion" class="formulario__etiqueta">Descripción</label>
                <textarea id="descripcion" name="descripcion" class="formulario__input"
                    placeholder="Descripción del producto"></textarea>
            </div>

            <div class="formulario__producto">
                <label for="precio" class="formulario__etiqueta">Precio</label>
                <input type="number" id="precio" name="precio" class="formulario__input"
                    placeholder="0.00" step="0.01" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="stock" class="formulario__etiqueta">Cantidad (stock)</label>
                <input type="number" id="stock" name="stock" class="formulario__input"
                    placeholder="0" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="stockMinimo" class="formulario__etiqueta">Stock mínimo</label>
                <input type="number" id="stockMinimo" name="stockMinimo" class="formulario__input"
                    placeholder="0" min="0" required>
            </div>

            <div class="formulario__producto">
                <label for="unidadMedida" class="formulario__etiqueta">Unidad de medida</label>
                <input type="text" id="unidadMedida" name="unidadMedida" class="formulario__input"
                    placeholder="kg, unidad, litro…">
            </div>

            <div class="formulario__producto">
                <label for="fechaVencimiento" class="formulario__etiqueta">Fecha de vencimiento</label>
                <input type="date" id="fechaVencimiento" name="fechaVencimiento" class="formulario__input">
            </div>

            <div class="formulario__producto">
                <label for="idCategoria" class="formulario__etiqueta">Categoría</label>
                <select id="idCategoria" name="idCategoria" class="formulario__input" required>
                    <option value="">-- Seleccione una categoría --</option>
                    <% if (categorias != null) {
                        for (Categoria c : categorias) { %>
                    <option value="<%= c.getIdCategoria() %>"><%= c.getNombre() %></option>
                    <%  }
                    } %>
                </select>
            </div>

            <button type="submit" class="formulario__boton">Registrar producto</button>
        </form>

    </main>
</body>

</html>

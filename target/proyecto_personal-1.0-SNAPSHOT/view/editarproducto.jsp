<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/editarproducto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Edición de producto</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <a href="productos.jsp" class="header__link">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Edicion de producto</h2>
        </div>
    </header>

    <main class="main">

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <p style="color:red;"><%= error %></p>
        <% } %>

        <form class="formulario" action="${pageContext.request.contextPath}/ProductoControlador" method="post">
            <input type="hidden" name="accion" value="actualizar">
            <input type="hidden" name="id"     value="${param.id}">

            <div class="formulario__producto">
                <label for="nombre" class="formulario__etiqueta">Nombre del producto</label>
                <input type="text" id="nombre" name="nombre" class="formulario__input"
                    placeholder="Papa criolla">
            </div>

            <div class="formulario__producto">
                <label for="Descripcion" class="formulario__etiqueta">Descripcion</label>
                <textarea name="descripcion" id="Descripcion" class="formulario__input"></textarea>
            </div>

            <div class="formulario__producto">
                <label for="precio" class="formulario__etiqueta">Precio</label>
                <input type="number" id="precio" name="precio" class="formulario__input"
                    placeholder="$ 50.000" step="0.01">
            </div>

            <div class="formulario__producto">
                <label for="cantidad" class="formulario__etiqueta">Cantidad (stock)</label>
                <input type="number" id="cantidad" name="stock" class="formulario__input"
                    placeholder="20">
            </div>

            <button type="submit" class="formulario__boton">Confirmar edicion</button>
        </form>

    </main>
</body>

</html>

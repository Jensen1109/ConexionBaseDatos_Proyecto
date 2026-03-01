<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/registroProducto.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Registro de producto</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <a class="header__link" href="productos.jsp">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Bienvenido</h2>
        </div>
    </header>

    <main class="main">

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <p style="color:red;"><%= error %></p>
        <% } %>

        <form class="formulario" action="${pageContext.request.contextPath}/ProductoControlador" method="post">
            <input type="hidden" name="accion" value="crear">

            <div class="formulario__producto">
                <label for="nombre" class="formulario__etiqueta">Nombre del producto</label>
                <input type="text" id="nombre" name="nombre" class="formulario__input"
                    placeholder="Ingrese el nombre" required>
            </div>

            <div class="formulario__producto">
                <label for="Descripcion" class="formulario__etiqueta">Descripcion</label>
                <textarea id="Descripcion" name="descripcion" class="formulario__input"></textarea>
            </div>

            <div class="formulario__producto">
                <label for="precio" class="formulario__etiqueta">Precio</label>
                <input type="number" id="precio" name="precio" class="formulario__input"
                    placeholder="Ingrese el precio" step="0.01" required>
            </div>

            <div class="formulario__producto">
                <label for="Cantidad" class="formulario__etiqueta">Cantidad (stock)</label>
                <input type="number" id="Cantidad" name="stock" class="formulario__input"
                    placeholder="Ingrese la cantidad" required>
            </div>

            <div class="formulario__producto">
                <label for="stockMinimo" class="formulario__etiqueta">Stock mínimo</label>
                <input type="number" id="stockMinimo" name="stockMinimo" class="formulario__input"
                    placeholder="Stock mínimo" required>
            </div>

            <div class="formulario__producto">
                <label for="unidadMedida" class="formulario__etiqueta">Unidad de medida</label>
                <input type="text" id="unidadMedida" name="unidadMedida" class="formulario__input"
                    placeholder="kg, unidad, litro…">
            </div>

            <button type="submit" class="formulario__boton">Registrar producto</button>
        </form>

    </main>
</body>

</html>

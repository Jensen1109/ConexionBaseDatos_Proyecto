<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <title>Iniciar Sesión</title>
</head>

<body>
    <main class="registro">

        <section class="registro__imagen-contenedor">
            <img src="${pageContext.request.contextPath}/assets/img/img supermercado.jpg"
                 alt="Persona en supermercado con canasta de compra"
                 class="registro__imagen">
        </section>

        <section class="registro__formulario">

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <p style="color:red;"><%= error %></p>
            <% } %>

            <form class="formulario" action="${pageContext.request.contextPath}/LoginControlador" method="post">

                <div class="formulario__campo">
                    <label for="email" class="formulario__etiqueta">Email</label>
                    <input type="email" id="email" name="email" class="formulario__input"
                        placeholder="Ingrese el correo" required>
                </div>

                <div class="formulario__campo">
                    <label for="contrasena" class="formulario__etiqueta">Contraseña</label>
                    <div class="formulario__input-contenedor">
                        <input type="password" id="contrasena" name="contrasena" class="formulario__input"
                            placeholder="Ingrese la contraseña" required>
                    </div>
                </div>

                <button type="submit" class="formulario__boton">Ingresar</button>
            </form>

        </section>
    </main>
</body>

</html>

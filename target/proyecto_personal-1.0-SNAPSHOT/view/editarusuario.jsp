<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/editarusuario.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/editarusuario-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Editar Usuario</title>
</head>

<body>
    <aside class="sidebar">
        <input type="checkbox" id="hamburger-toggle" class="hamburger-checkbox">
        <label for="hamburger-toggle" class="hamburger-overlay"></label>

        <nav class="hamburger-nav">
            <label for="hamburger-toggle" class="hamburger-close">
                <i class="fa-solid fa-xmark"></i>
            </label>
            <h2 class="hamburger-titulo">Tienda Don Pedro</h2>
            <a href="historialventa.jsp" class="hamburger-link">Historial</a>
            <a href="registrarventa.jsp" class="hamburger-link">Registrar Venta</a>
            <a href="deudores.jsp" class="hamburger-link">Deudores</a>
            <a href="editarusuario.jsp" class="hamburger-link hamburger-link--activo">Usuarios</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="productos.jsp" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda<br>Don Pedro</h2>
            <a href="historialventa.jsp" class="sidebar__link">Historial</a>
            <a href="registrarventa.jsp" class="sidebar__link">Registrar Venta</a>
            <a href="deudores.jsp" class="sidebar__link">Deudores</a>
            <a href="editarusuario.jsp" class="sidebar__link sidebar__link--activo">Usuarios</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span>
                <span></span>
                <span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="editar-usuario">

            <h2 class="editar-usuario__titulo">Editar Usuario</h2>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <p style="color:red;"><%= error %></p>
            <% } %>

            <form class="editar-usuario__formulario"
                  action="${pageContext.request.contextPath}/ClienteControlador" method="post">
                <input type="hidden" name="accion" value="actualizar">

                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Nombre</label>
                    <input type="text" name="nombre" class="editar-usuario__input" value="Pedro Perez">
                </div>

                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Correo electrónico</label>
                    <input type="email" name="email" class="editar-usuario__input" value="pedroperez@gmail.com">
                </div>

                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Contraseña</label>
                    <input type="password" name="contrasena" class="editar-usuario__input">
                </div>

                <button type="submit" class="editar-usuario__btn-guardar">Guardar</button>
            </form>

        </div>
    </main>
</body>

</html>

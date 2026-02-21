<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Tienda del Barrio</title>
</head>
<body>

    <h2>Iniciar Sesión</h2>

    <!-- Muestra el error si el controlador lo envió -->
    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
        <p style="color:red;"><%= error %></p>
    <% } %>

    <!-- El action debe coincidir con @WebServlet en tu controlador -->
    <form action="LoginControlador" method="post">
        <label>Email:</label><br>
        <input type="email" name="email" required /><br><br>

        <label>Contraseña:</label><br>
        <input type="password" name="contrasena" required /><br><br>

        <button type="submit">Entrar</button>
    </form>

</body>
</html>
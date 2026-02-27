<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="modelos.Usuario" %>
<%
    // Si no hay sesión, redirige al login
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("usuarioLogueado") == null) {
        response.sendRedirect(request.getContextPath() + "/LoginControlador");
        return;
    }
    String nombre = (String) sesion.getAttribute("nombreUsuario");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Inicio - Tienda del Barrio</title>
</head>
<body>
    <h1>¡Bienvenido, <%= nombre %>!</h1>
    <p>Iniciaste sesión correctamente.</p>
    <a href="LogoutControlador">Cerrar sesión</a>  
    
</body>

</html>
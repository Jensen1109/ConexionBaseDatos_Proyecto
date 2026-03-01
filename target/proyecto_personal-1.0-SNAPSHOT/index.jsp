<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${pageContext.request.contextPath}/styles.css" rel="stylesheet">
    <title>Mi Barrio</title>
</head>
<body>
    <main class="main">
        <div class="contenedorimg">
            <img class="main__logo" src="${pageContext.request.contextPath}/assets/img/unnamed-removebg-preview 2.png">
        </div>
        <div class="contenedor-enlaces">
            <a class="contenedor-enlaces__registro" href="${pageContext.request.contextPath}/RegistroControlador">Registrar Usuario</a>
            <a class="contenedor-enlaces__registro" href="${pageContext.request.contextPath}/LoginControlador">Iniciar Sesion</a>
        </div>
    </main>
</body>
</html>
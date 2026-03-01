<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/historialventa.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/historialventa-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Historial de ventas</title>
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
            <a href="historialventa.jsp" class="hamburger-link hamburger-link--activo">Historial</a>
            <a href="registrarventa.jsp" class="hamburger-link">Registrar Venta</a>
            <a href="deudores.jsp" class="hamburger-link">Deudores</a>
            <a href="editarusuario.jsp" class="hamburger-link">Usuarios</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="productos.jsp" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda<br>Don Pedro</h2>
            <a href="historialventa.jsp" class="sidebar__link sidebar__link--activo">Historial</a>
            <a href="registrarventa.jsp" class="sidebar__link">Registrar Venta</a>
            <a href="deudores.jsp" class="sidebar__link">Deudores</a>
            <a href="editarusuario.jsp" class="sidebar__link">Usuarios</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span>
                <span></span>
                <span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="historial-venta">

            <div class="historial-venta__busqueda">
                <i class="fas fa-search historial-venta__icono-busqueda"></i>
                <input type="text" class="historial-venta__input-busqueda" placeholder="Buscar">
            </div>

            <table class="contenedor-tabla">
                <thead class="historial-venta__tabla-head">
                    <tr>
                        <th class="historial-venta__columna">Fecha</th>
                        <th class="historial-venta__columna">Cliente</th>
                        <th class="historial-venta__columna">Total</th>
                        <th class="historial-venta__columna">Estado</th>
                    </tr>
                </thead>
                <tbody class="historial-venta__tabla-body">
                    <tr class="historial-venta__item">
                        <td class="historial-venta__valor" data-label="Fecha">12/04/2024</td>
                        <td class="historial-venta__valor" data-label="Cliente">María González</td>
                        <td class="historial-venta__valor" data-label="Total">$15.000</td>
                        <td class="historial-venta__valor historial-venta__estado--completado"
                            data-label="Estado">Completado</td>
                    </tr>
                    <tr class="historial-venta__item">
                        <td class="historial-venta__valor" data-label="Fecha">10/04/2024</td>
                        <td class="historial-venta__valor" data-label="Cliente">Juan Pérez</td>
                        <td class="historial-venta__valor" data-label="Total">$8.000</td>
                        <td class="historial-venta__valor historial-venta__estado--pendiente"
                            data-label="Estado">Pendiente</td>
                    </tr>
                    <tr class="historial-venta__item">
                        <td class="historial-venta__valor" data-label="Fecha">08/04/2024</td>
                        <td class="historial-venta__valor" data-label="Cliente">Ana Gómez</td>
                        <td class="historial-venta__valor" data-label="Total">$5.000</td>
                        <td class="historial-venta__valor historial-venta__estado--completado"
                            data-label="Estado">Completado</td>
                    </tr>
                </tbody>
            </table>

        </div>
    </main>
</body>

</html>

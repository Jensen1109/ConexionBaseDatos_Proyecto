<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/deudores.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/deudores-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Deudores</title>
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
            <a href="deudores.jsp" class="hamburger-link hamburger-link--activo">Deudores</a>
            <a href="editarusuario.jsp" class="hamburger-link">Usuarios</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="productos.jsp" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda Don Pedro</h2>
            <a href="historialventa.jsp" class="sidebar__link">Historial</a>
            <a href="registrarventa.jsp" class="sidebar__link">Registrar Venta</a>
            <a href="deudores.jsp" class="sidebar__link sidebar__link--activo">Deudores</a>
            <a href="editarusuario.jsp" class="sidebar__link">Usuarios</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span>
                <span></span>
                <span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="deudores">
            <h1 class="deudores__titulo">Deudores</h1>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <p style="color:red;"><%= error %></p>
            <% } %>

            <table class="contenedor-tabla">
                <thead class="deudores__tabla-head">
                    <tr>
                        <th class="deudores__columna">Nombre</th>
                        <th class="deudores__columna">Monto</th>
                        <th class="deudores__columna">Fecha</th>
                        <th class="deudores__columna">Abono</th>
                        <th class="deudores__columna">Acciones</th>
                    </tr>
                </thead>
                <tbody class="deudores__tabla-body">
                    <tr class="deudores__item">
                        <td class="deudores__valor" data-label="Nombre">Juan Perez</td>
                        <td class="deudores__valor deudores__monto" data-label="Monto">$150,000</td>
                        <td class="deudores__valor" data-label="Fecha">12/03/2024</td>
                        <td class="deudores__valor deudores__abono-celda" data-label="Abono">
                            <form action="${pageContext.request.contextPath}/DeudaControlador" method="post">
                                <input type="hidden" name="idDeuda" value="1">
                                <input type="number" name="monto" class="deudores__input-abono"
                                    placeholder="$0" min="0" step="0.01">
                                <button type="submit" class="deudores__btn-abonar">
                                    <i class="fa-solid fa-check"></i>
                                </button>
                            </form>
                        </td>
                        <td class="deudores__valor deudores__acciones">
                            <button class="deudores__btn-eliminar">
                                <i class="fa-regular fa-trash-can"></i>
                            </button>
                        </td>
                    </tr>
                    <tr class="deudores__item">
                        <td class="deudores__valor" data-label="Nombre">Maria Rodriguez</td>
                        <td class="deudores__valor deudores__monto" data-label="Monto">$200,000</td>
                        <td class="deudores__valor" data-label="Fecha">15/03/2024</td>
                        <td class="deudores__valor deudores__abono-celda" data-label="Abono">
                            <form action="${pageContext.request.contextPath}/DeudaControlador" method="post">
                                <input type="hidden" name="idDeuda" value="2">
                                <input type="number" name="monto" class="deudores__input-abono"
                                    placeholder="$0" min="0" step="0.01">
                                <button type="submit" class="deudores__btn-abonar">
                                    <i class="fa-solid fa-check"></i>
                                </button>
                            </form>
                        </td>
                        <td class="deudores__valor deudores__acciones">
                            <button class="deudores__btn-eliminar">
                                <i class="fa-regular fa-trash-can"></i>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="deudores__footer">
                <button class="deudores__btn-agregar">Agregar</button>
            </div>
        </div>
    </main>
</body>

</html>

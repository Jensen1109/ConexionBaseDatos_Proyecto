<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/productos.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/custom/productos-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Productos</title>
</head>

<body>
    <!-- HEADER CON MENU HAMBURGUESA PARA MOVIL -->
    <header class="header-mobile">
        <input type="checkbox" id="menu-toggle" class="menu-toggle">
        <label for="menu-toggle" class="menu-hamburguesa">
            <span class="hamburguesa-linea"></span>
            <span class="hamburguesa-linea"></span>
            <span class="hamburguesa-linea"></span>
        </label>
        <h1 class="header-mobile__titulo">Productos</h1>

        <nav class="nav-mobile">
            <div class="nav-mobile__seccion">
                <h3 class="nav-mobile__titulo">Productos</h3>
                <a href="#" class="nav-mobile__link">Ver productos</a>
                <a href="registroProducto.jsp" class="nav-mobile__link">Registrar productos</a>
                <a href="controlstock.jsp" class="nav-mobile__link">Control de stock</a>
            </div>
            <div class="nav-mobile__seccion">
                <h3 class="nav-mobile__titulo">Ventas</h3>
                <a href="registrarventa.jsp" class="nav-mobile__link">Registrar venta</a>
                <a href="historialventa.jsp" class="nav-mobile__link">Historial de venta</a>
                <a href="reportes.jsp" class="nav-mobile__link">Reportes</a>
            </div>
            <div class="nav-mobile__seccion">
                <h3 class="nav-mobile__titulo">Clientes</h3>
                <a href="editarusuario.jsp" class="nav-mobile__link">Ver / Editar clientes</a>
                <a href="deudores.jsp" class="nav-mobile__link">Deudores</a>
            </div>
        </nav>
    </header>

    <!-- SIDEBAR PARA DESKTOP -->
    <aside class="sidebar">
        <div class="contenedor__sidebar">
            <h2 class="sidebar__titulo">Productos</h2>
            <a href="#" class="sidebar__link">Ver productos</a>
            <a href="registroProducto.jsp" class="sidebar__link">Registrar productos</a>
            <a href="controlstock.jsp" class="sidebar__link">Control de stock</a>

            <h2 class="sidebar__titulo">Ventas</h2>
            <a href="registrarventa.jsp" class="sidebar__link">Registrar venta</a>
            <a href="historialventa.jsp" class="sidebar__link">Historial de venta</a>
            <a href="reportes.jsp" class="sidebar__link">Reportes</a>

            <h2 class="sidebar__titulo">Clientes</h2>
            <a href="editarusuario.jsp" class="sidebar__link">Ver / Editar clientes</a>
            <a href="deudores.jsp" class="sidebar__link">Deudores</a>
        </div>
    </aside>

    <!-- CONTENIDO PRINCIPAL -->
    <main class="main">
        <div class="galeria">

            <article class="producto">
                <img src="${pageContext.request.contextPath}/assets/img/papa-criolla-3432646_1280.jpg"
                     class="producto__imagen" alt="Papa criolla">
                <div class="producto__contenido">
                    <h2 class="producto__titulo">Papa criolla</h2>
                    <p class="producto__descripcion">Fuente de carbohidratos, potasio, proteínas y vitaminas A, C y B</p>
                    <div class="contenedor__precio">
                        <p class="producto__precio">$ 10.000</p>
                        <div class="contenedor__iconos">
                            <a href="editarproducto.jsp" class="producto__icono" title="Editar">
                                <i class="fa-solid fa-pencil"></i>
                            </a>
                            <a href="#modal-eliminar-1" class="producto__icono" title="Eliminar">
                                <i class="fa-solid fa-trash"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </article>

            <article class="producto">
                <img src="${pageContext.request.contextPath}/assets/img/cherry-tomato-4330441_1280.jpg"
                     class="producto__imagen" alt="Tomate">
                <div class="producto__contenido">
                    <h2 class="producto__titulo">Tomate</h2>
                    <p class="producto__descripcion">Bajo en calorías y rico en vitamina C y ácido fólico</p>
                    <div class="contenedor__precio">
                        <p class="producto__precio">$ 10.000</p>
                        <div class="contenedor__iconos">
                            <a href="editarproducto.jsp" class="producto__icono" title="Editar">
                                <i class="fa-solid fa-pencil"></i>
                            </a>
                            <a href="#modal-eliminar-2" class="producto__icono" title="Eliminar">
                                <i class="fa-solid fa-trash"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </article>

            <article class="producto">
                <img src="${pageContext.request.contextPath}/assets/img/ham-3130701_1280.jpg"
                     class="producto__imagen" alt="Carne">
                <div class="producto__contenido">
                    <h2 class="producto__titulo">Carne</h2>
                    <p class="producto__descripcion">Proteínas, hierro y vitaminas del grupo B</p>
                    <div class="contenedor__precio">
                        <p class="producto__precio">$ 10.000</p>
                        <div class="contenedor__iconos">
                            <a href="editarproducto.jsp" class="producto__icono" title="Editar">
                                <i class="fa-solid fa-pencil"></i>
                            </a>
                            <a href="#modal-eliminar-3" class="producto__icono" title="Eliminar">
                                <i class="fa-solid fa-trash"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </article>

            <article class="producto">
                <img src="${pageContext.request.contextPath}/assets/img/harvest-7458975_1280.jpg"
                     class="producto__imagen" alt="Manzana">
                <div class="producto__contenido">
                    <h2 class="producto__titulo">Manzana</h2>
                    <p class="producto__descripcion">Es rica en agua (aproximadamente un 85%), lo que la hace
                        refrescante e hidratante</p>
                    <div class="contenedor__precio">
                        <p class="producto__precio">$ 10.000</p>
                        <div class="contenedor__iconos">
                            <a href="editarproducto.jsp" class="producto__icono" title="Editar">
                                <i class="fa-solid fa-pencil"></i>
                            </a>
                            <a href="#modal-eliminar-4" class="producto__icono" title="Eliminar">
                                <i class="fa-solid fa-trash"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </article>

        </div>
    </main>

    <!-- MODALES DE CONFIRMACIÓN -->
    <div id="modal-eliminar-1" class="ventana-emergente">
        <div class="ventana-emergente__contenido">
            <div class="ventana-emergente__icono ventana-emergente__icono--advertencia">
                <i class="fa-solid fa-question"></i>
            </div>
            <h2 class="ventana-emergente__titulo">¿Eliminar Producto?</h2>
            <p class="ventana-emergente__mensaje">¿Está seguro que desea eliminar este producto?</p>
            <div class="ventana-emergente__botones">
                <a href="#modal-exito-1" class="ventana-emergente__boton ventana-emergente__boton--si">Sí</a>
                <a href="#" class="ventana-emergente__boton ventana-emergente__boton--no">No</a>
            </div>
        </div>
    </div>

    <div id="modal-exito-1" class="ventana-emergente">
        <div class="ventana-emergente__contenido">
            <div class="ventana-emergente__icono">
                <i class="fa-solid fa-check-circle"></i>
            </div>
            <h2 class="ventana-emergente__titulo">¡Producto Eliminado!</h2>
            <p class="ventana-emergente__mensaje">El producto ha sido eliminado exitosamente.</p>
            <a href="#" class="ventana-emergente__boton">Aceptar</a>
        </div>
    </div>
</body>

</html>

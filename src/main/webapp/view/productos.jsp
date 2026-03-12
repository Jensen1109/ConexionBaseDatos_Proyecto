<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Usuario" %>
<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    String ctx = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/productos.css">
    <link rel="stylesheet" href="<%= ctx %>/css/custom/productos-mediaqueries.css">
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
                <a href="<%= ctx %>/ProductoControlador" class="nav-mobile__link">Ver productos</a>
                <% if (esAdmin) { %>
                <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="nav-mobile__link">Registrar productos</a>
                <% } %>
                <a href="<%= ctx %>/ProductoControlador?accion=stock" class="nav-mobile__link">Control de stock</a>
            </div>
            <% if (esAdmin) { %>
            <div class="nav-mobile__seccion">
                <h3 class="nav-mobile__titulo">Ventas</h3>
                <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="nav-mobile__link">Registrar venta</a>
                <a href="<%= ctx %>/PedidoControlador"              class="nav-mobile__link">Historial de venta</a>
                <a href="<%= ctx %>/ReporteControlador"             class="nav-mobile__link">Reportes</a>
            </div>
            <div class="nav-mobile__seccion">
                <h3 class="nav-mobile__titulo">Clientes</h3>
                <a href="<%= ctx %>/ClienteControlador" class="nav-mobile__link">Ver / Editar clientes</a>
                <a href="<%= ctx %>/DeudaControlador"   class="nav-mobile__link">Deudores</a>
            </div>
            <% } %>
        </nav>
    </header>

    <!-- SIDEBAR PARA DESKTOP -->
    <aside class="sidebar">
        <div class="contenedor__sidebar">
            <h2 class="sidebar__titulo">Productos</h2>
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__link">Ver productos</a>
            <% if (esAdmin) { %>
            <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="sidebar__link">Registrar productos</a>
            <% } %>
            <a href="<%= ctx %>/ProductoControlador?accion=stock" class="sidebar__link">Control de stock</a>

            <% if (esAdmin) { %>
            <h2 class="sidebar__titulo">Ventas</h2>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">Registrar venta</a>
            <a href="<%= ctx %>/PedidoControlador"              class="sidebar__link">Historial de venta</a>
            <a href="<%= ctx %>/ReporteControlador"             class="sidebar__link">Reportes</a>

            <h2 class="sidebar__titulo">Clientes</h2>
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link">Ver / Editar clientes</a>
            <a href="<%= ctx %>/DeudaControlador"   class="sidebar__link">Deudores</a>
            <% } %>
        </div>
    </aside>

    <!-- CONTENIDO PRINCIPAL -->
    <main class="main">
        <div class="galeria">

            <% if (productos == null || productos.isEmpty()) { %>
                <p>No hay productos registrados.</p>
            <% } else {
                for (Producto p : productos) { %>
            <article class="producto">
                <div class="producto__contenido">
                    <h2 class="producto__titulo"><%= p.getNombre() %></h2>
                    <p class="producto__descripcion"><%= p.getDescripcion() != null ? p.getDescripcion() : "" %></p>
                    <div class="contenedor__precio">
                        <p class="producto__precio">$ <%= p.getPrecio() %></p>
                        <% if (esAdmin) { %>
                        <div class="contenedor__iconos">
                            <a href="<%= ctx %>/ProductoControlador?accion=editar&id=<%= p.getIdProducto() %>"
                               class="producto__icono" title="Editar">
                                <i class="fa-solid fa-pencil"></i>
                            </a>
                            <a href="#modal-eliminar-<%= p.getIdProducto() %>"
                               class="producto__icono" title="Eliminar">
                                <i class="fa-solid fa-trash"></i>
                            </a>
                        </div>
                        <% } %>
                    </div>
                </div>
            </article>
            <%  }
            } %>

        </div>
    </main>

    <!-- MODALES DE CONFIRMACIÓN (uno por producto) -->
    <% if (productos != null) {
        for (Producto p : productos) { %>
    <div id="modal-eliminar-<%= p.getIdProducto() %>" class="ventana-emergente">
        <div class="ventana-emergente__contenido">
            <div class="ventana-emergente__icono ventana-emergente__icono--advertencia">
                <i class="fa-solid fa-question"></i>
            </div>
            <h2 class="ventana-emergente__titulo">¿Eliminar Producto?</h2>
            <p class="ventana-emergente__mensaje">¿Está seguro que desea eliminar "<strong><%= p.getNombre() %></strong>"?</p>
            <div class="ventana-emergente__botones">
                <form action="<%= ctx %>/ProductoControlador" method="post" style="display:inline">
                    <input type="hidden" name="accion" value="eliminar">
                    <input type="hidden" name="id"     value="<%= p.getIdProducto() %>">
                    <button type="submit" class="ventana-emergente__boton ventana-emergente__boton--si">Sí</button>
                </form>
                <a href="#" class="ventana-emergente__boton ventana-emergente__boton--no">No</a>
            </div>
        </div>
    </div>
    <% } } %>

</body>

</html>

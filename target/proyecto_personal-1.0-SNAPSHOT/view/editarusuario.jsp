<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Usuario" %>
<%
    List<Usuario> clientes = (List<Usuario>) request.getAttribute("clientes");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/editarusuario.css">
    <link rel="stylesheet" href="<%= ctx %>/css/editarusuario-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Clientes</title>
</head>

<body>
    <aside class="sidebar">
        <input type="checkbox" id="hamburger-toggle" class="hamburger-checkbox">
        <label for="hamburger-toggle" class="hamburger-overlay"></label>

        <!-- CORREGIDO: todos los links apuntan a controladores -->
        <nav class="hamburger-nav">
            <label for="hamburger-toggle" class="hamburger-close">
                <i class="fa-solid fa-xmark"></i>
            </label>
            <h2 class="hamburger-titulo">Tienda Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="hamburger-link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="hamburger-link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="hamburger-link">Deudores</a>
            <a href="<%= ctx %>/ClienteControlador"             class="hamburger-link hamburger-link--activo">Clientes</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda<br>Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="sidebar__link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="sidebar__link">Deudores</a>
            <a href="<%= ctx %>/ClienteControlador"             class="sidebar__link sidebar__link--activo">Clientes</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span><span></span><span></span>
            </label>
        </div>
    </aside>

    <main class="main">
        <div class="editar-usuario">

            <h2 class="editar-usuario__titulo">Clientes registrados</h2>

            <% if (error != null) { %>
                <p style="color:red;"><%= error %></p>
            <% } %>

            <!-- ── TABLA DE CLIENTES ── -->
            <table class="contenedor-tabla">
                <thead>
                    <tr>
                        <th class="editar-usuario__th">Nombre</th>
                        <th class="editar-usuario__th">Apellido</th>
                        <th class="editar-usuario__th">Email</th>
                        <th class="editar-usuario__th">Cédula</th>
                        <th class="editar-usuario__th">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (clientes == null || clientes.isEmpty()) { %>
                    <tr>
                        <td colspan="5" style="text-align:center;">No hay clientes registrados.</td>
                    </tr>
                    <% } else {
                        for (Usuario c : clientes) { %>
                    <tr class="editar-usuario__fila">
                        <td class="editar-usuario__celda"><%= c.getNombre() %></td>
                        <td class="editar-usuario__celda"><%= c.getApellido() != null ? c.getApellido() : "" %></td>
                        <td class="editar-usuario__celda"><%= c.getEmail() %></td>
                        <td class="editar-usuario__celda"><%= c.getCedula() != null ? c.getCedula() : "" %></td>
                        <td class="editar-usuario__celda">
                            <a href="#modal-editar-<%= c.getIdUsuario() %>"
                               class="editar-usuario__btn">Editar</a>
                            <a href="#modal-eliminar-<%= c.getIdUsuario() %>"
                               class="editar-usuario__btn editar-usuario__btn--eliminar">Eliminar</a>
                        </td>
                    </tr>
                    <%  }
                    } %>
                </tbody>
            </table>

        </div>
    </main>

    <!-- ── MODALES DE EDICIÓN (uno por cliente) ── -->
    <% if (clientes != null) {
        for (Usuario c : clientes) { %>
    <div id="modal-editar-<%= c.getIdUsuario() %>" class="ventana-emergente">
        <div class="ventana-emergente__contenido">
            <h2 class="ventana-emergente__titulo">Editar cliente</h2>
            <form class="editar-usuario__formulario"
                  action="<%= ctx %>/ClienteControlador" method="post">
                <input type="hidden" name="accion"    value="actualizar">
                <input type="hidden" name="idUsuario" value="<%= c.getIdUsuario() %>">

                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Nombre</label>
                    <input type="text" name="nombre" class="editar-usuario__input"
                           value="<%= c.getNombre() %>" required>
                </div>
                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Apellido</label>
                    <input type="text" name="apellido" class="editar-usuario__input"
                           value="<%= c.getApellido() != null ? c.getApellido() : "" %>">
                </div>
                <div class="editar-usuario__campo">
                    <label class="editar-usuario__label">Email</label>
                    <input type="email" name="email" class="editar-usuario__input"
                           value="<%= c.getEmail() %>" required>
                </div>

                <div class="ventana-emergente__botones">
                    <button type="submit" class="ventana-emergente__boton ventana-emergente__boton--si">
                        Guardar
                    </button>
                    <a href="#" class="ventana-emergente__boton ventana-emergente__boton--no">
                        Cancelar
                    </a>
                </div>
            </form>
        </div>
    </div>
    <% } } %>

    <!-- ── MODALES DE ELIMINACIÓN (uno por cliente) ── -->
    <% if (clientes != null) {
        for (Usuario c : clientes) { %>
    <div id="modal-eliminar-<%= c.getIdUsuario() %>" class="ventana-emergente">
        <div class="ventana-emergente__contenido">
            <div class="ventana-emergente__icono ventana-emergente__icono--advertencia">
                <i class="fa-solid fa-question"></i>
            </div>
            <h2 class="ventana-emergente__titulo">¿Eliminar cliente?</h2>
            <p class="ventana-emergente__mensaje">
                ¿Está seguro que desea eliminar a
                <strong><%= c.getNombre() %> <%= c.getApellido() != null ? c.getApellido() : "" %></strong>?
            </p>
            <div class="ventana-emergente__botones">
                <form action="<%= ctx %>/ClienteControlador" method="post" style="display:inline">
                    <input type="hidden" name="accion"    value="eliminar">
                    <input type="hidden" name="idUsuario" value="<%= c.getIdUsuario() %>">
                    <button type="submit" class="ventana-emergente__boton ventana-emergente__boton--si">
                        Sí, eliminar
                    </button>
                </form>
                <a href="#" class="ventana-emergente__boton ventana-emergente__boton--no">Cancelar</a>
            </div>
        </div>
    </div>
    <% } } %>

</body>

</html>

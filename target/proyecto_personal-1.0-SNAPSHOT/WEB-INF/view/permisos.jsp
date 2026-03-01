<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Permisos, modelos.Rol" %>
<%
    List<Permisos> permisos          = (List<Permisos>) request.getAttribute("permisos");
    List<Rol>      roles             = (List<Rol>)      request.getAttribute("roles");
    List<Integer>  permisosDelRol    = (List<Integer>)  request.getAttribute("permisosDelRol");
    Integer        idRolSeleccionado = (Integer)        request.getAttribute("idRolSeleccionado");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <title>Permisos</title>
</head>
<body>
<main class="main">
    <h1>Gestión de permisos</h1>

    <form method="get" action="<%= ctx %>/PermisosControlador">
        <label>Ver permisos del rol:
            <select name="idRol" onchange="this.form.submit()">
                <option value="">-- Selecciona --</option>
                <% if (roles != null) for (Rol r : roles) {
                    boolean sel = idRolSeleccionado != null && idRolSeleccionado == r.getIdRol(); %>
                    <option value="<%= r.getIdRol() %>" <%= sel ? "selected" : "" %>>
                        <%= r.getNombre() %>
                    </option>
                <% } %>
            </select>
        </label>
    </form>

    <% if (idRolSeleccionado != null && permisos != null) { %>
    <table border="1" cellpadding="6">
        <thead>
            <tr><th>Permiso</th><th>Descripción</th><th>Asignado</th><th>Acción</th></tr>
        </thead>
        <tbody>
        <% for (Permisos p : permisos) {
               boolean tienePermiso = permisosDelRol != null
                       && permisosDelRol.contains(p.getIdPermiso()); %>
            <tr>
                <td><%= p.getNombre() %></td>
                <td><%= p.getDescripcion() %></td>
                <td><%= tienePermiso ? "✅" : "—" %></td>
                <td>
                    <form action="<%= ctx %>/PermisosControlador" method="post">
                        <input type="hidden" name="idRol"     value="<%= idRolSeleccionado %>">
                        <input type="hidden" name="idPermiso" value="<%= p.getIdPermiso() %>">
                        <input type="hidden" name="accion"    value="<%= tienePermiso ? "quitar" : "asignar" %>">
                        <button type="submit"><%= tienePermiso ? "Quitar" : "Asignar" %></button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
</main>
</body>
</html>

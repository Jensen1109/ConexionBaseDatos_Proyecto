<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="modelos.Usuario" %>
<%
    String  _act   = (String)  request.getAttribute("_paginaActiva");
    if (_act == null) _act = "";
    String  _ctx   = request.getContextPath();
    Usuario _usr   = (Usuario) session.getAttribute("usuarioLogueado");
    boolean _admin = (_usr != null && _usr.getIdRol() == 1);
%>
<aside class="sidebar" id="sidebar">
    <div class="sidebar__brand">
        <div class="sidebar__brand-title">Tienda Don Pedro</div>
        <div class="sidebar__brand-sub">Panel de gestión</div>
    </div>

    <!-- ── PRODUCTOS ── -->
    <div class="sidebar__section">
        <span class="sidebar__label">Productos</span>
        <a href="<%= _ctx %>/ProductoControlador"
           class="sidebar__link <%= "productos".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-box"></i> Ver productos
        </a>
        <% if (_admin) { %>
        <a href="<%= _ctx %>/ProductoControlador?accion=nuevo"
           class="sidebar__link <%= "registrarProducto".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-plus-circle"></i> Registrar producto
        </a>
        <% } %>
        <a href="<%= _ctx %>/ProductoControlador?accion=stock"
           class="sidebar__link <%= "stock".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-chart-bar"></i> Control de stock
        </a>
    </div>

    <!-- ── VENTAS ── -->
    <div class="sidebar__section">
        <span class="sidebar__label">Ventas</span>
        <a href="<%= _ctx %>/PedidoControlador?accion=nuevo"
           class="sidebar__link <%= "registrarVenta".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-cart-plus"></i> Registrar venta
        </a>
        <a href="<%= _ctx %>/PedidoControlador"
           class="sidebar__link <%= "historial".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-history"></i> Historial de venta
        </a>
        <a href="<%= _ctx %>/ReporteControlador"
           class="sidebar__link <%= "reportes".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-chart-line"></i> Reportes
        </a>
    </div>

    <!-- ── CLIENTES (solo admin) ── -->
    <% if (_admin) { %>
    <div class="sidebar__section">
        <span class="sidebar__label">Clientes</span>
        <a href="<%= _ctx %>/ClienteControlador"
           class="sidebar__link <%= "clientes".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-users"></i> Ver / Editar clientes
        </a>
        <a href="<%= _ctx %>/DeudaControlador"
           class="sidebar__link <%= "deudores".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-file-invoice-dollar"></i> Deudores
        </a>
    </div>

    <!-- ── SISTEMA (solo admin) ── -->
    <div class="sidebar__section">
        <span class="sidebar__label">Sistema</span>
        <a href="<%= _ctx %>/UsuarioControlador"
           class="sidebar__link <%= "usuarios".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-users-cog"></i> Gestión de usuarios
        </a>
    </div>
    <% } %>

    <!-- ── MI CUENTA ── -->
    <div class="sidebar__section sidebar__section--cuenta">
        <span class="sidebar__label">Mi cuenta</span>
        <div class="sidebar__user-card">
            <div class="sidebar__user-avatar">
                <%= _usr != null
                    ? String.valueOf(_usr.getNombre().charAt(0)).toUpperCase()
                      + String.valueOf(_usr.getApellido().charAt(0)).toUpperCase()
                    : "?" %>
            </div>
            <div>
                <div class="sidebar__user-name">
                    <%= _usr != null ? _usr.getNombre() + " " + _usr.getApellido() : "" %>
                </div>
                <div class="sidebar__user-role">
                    <%= _usr != null && _usr.getIdRol() == 1 ? "Administrador" : "Empleado" %>
                </div>
            </div>
        </div>
        <a href="<%= _ctx %>/PerfilControlador"
           class="sidebar__link <%= "perfil".equals(_act) ? "sidebar__link--activo" : "" %>">
            <i class="fas fa-user-circle"></i> Mi perfil
        </a>
        <a href="<%= _ctx %>/LogoutControlador" class="sidebar__link sidebar__link--logout">
            <i class="fas fa-sign-out-alt"></i> Cerrar sesión
        </a>
    </div>
</aside>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, java.math.BigDecimal" %>
<%
    BigDecimal    totalVentas   = (BigDecimal)    request.getAttribute("totalVentasMes");
    List<Producto> stockBajo    = (List<Producto>) request.getAttribute("productosStockBajo");
    BigDecimal    totalDeudas   = (BigDecimal)    request.getAttribute("totalDeudasPendientes");
    Integer       totalClientes = (Integer)       request.getAttribute("totalClientes");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/reportes.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Reportes</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <!-- CORREGIDO: antes apuntaba a productos.jsp directamente -->
            <a href="<%= ctx %>/ProductoControlador" class="header__link">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Reportes</h2>
        </div>
    </header>

    <main class="main">
        <div class="reportes">

            <!-- ── TARJETAS RESUMEN ── -->
            <div class="reportes__resumen">

                <div class="reportes__tarjeta">
                    <i class="fas fa-dollar-sign reportes__tarjeta-icono"></i>
                    <p class="reportes__tarjeta-label">Ventas del mes</p>
                    <p class="reportes__tarjeta-valor">$<%= totalVentas != null ? totalVentas : "0" %></p>
                </div>

                <div class="reportes__tarjeta">
                    <i class="fas fa-clock reportes__tarjeta-icono"></i>
                    <p class="reportes__tarjeta-label">Deudas pendientes</p>
                    <p class="reportes__tarjeta-valor">$<%= totalDeudas != null ? totalDeudas : "0" %></p>
                </div>

                <div class="reportes__tarjeta">
                    <i class="fas fa-users reportes__tarjeta-icono"></i>
                    <p class="reportes__tarjeta-label">Clientes registrados</p>
                    <p class="reportes__tarjeta-valor"><%= totalClientes != null ? totalClientes : "0" %></p>
                </div>

                <div class="reportes__tarjeta">
                    <i class="fas fa-exclamation-triangle reportes__tarjeta-icono"></i>
                    <p class="reportes__tarjeta-label">Productos stock bajo</p>
                    <p class="reportes__tarjeta-valor"><%= stockBajo != null ? stockBajo.size() : "0" %></p>
                </div>

            </div>

            <!-- ── TABLA PRODUCTOS CON STOCK BAJO ── -->
            <h3 class="reportes__subtitulo">Productos con stock bajo o crítico</h3>

            <table class="contenedor-tabla">
                <thead class="reportes__tabla-head">
                    <tr>
                        <th class="reportes__columna">Producto</th>
                        <th class="reportes__columna">Stock actual</th>
                        <th class="reportes__columna">Stock mínimo</th>
                        <th class="reportes__columna">Precio</th>
                    </tr>
                </thead>
                <tbody class="reportes__tabla-body">
                    <% if (stockBajo == null || stockBajo.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;">
                            Todos los productos tienen stock suficiente.
                        </td>
                    </tr>
                    <% } else {
                        for (Producto p : stockBajo) { %>
                    <tr class="reportes__item">
                        <td class="reportes__valor"><%= p.getNombre() %></td>
                        <td class="reportes__valor"><%= p.getStock() %></td>
                        <td class="reportes__valor"><%= p.getStockMinimo() %></td>
                        <td class="reportes__valor">$<%= p.getPrecio() %></td>
                    </tr>
                    <%  }
                    } %>
                </tbody>
            </table>

        </div>
    </main>
</body>

</html>

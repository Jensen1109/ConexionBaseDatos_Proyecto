<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.math.BigDecimal, modelos.Producto" %>
<%
    BigDecimal    totalVentas    = (BigDecimal)   request.getAttribute("totalVentasMes");
    List<Producto> stockBajo     = (List<Producto>) request.getAttribute("productosStockBajo");
    BigDecimal    totalDeudas    = (BigDecimal)   request.getAttribute("totalDeudasPendientes");
    Integer       totalClientes  = (Integer)      request.getAttribute("totalClientes");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/reportes.css">
    <title>Reportes</title>
</head>
<body>
<main class="main">
    <h1>Reportes</h1>

    <div style="display:flex;gap:24px;flex-wrap:wrap;margin-bottom:32px">
        <div style="border:1px solid #ccc;padding:16px;border-radius:8px;min-width:180px">
            <h3>Ventas del mes</h3>
            <p style="font-size:1.5rem">$<%= totalVentas %></p>
        </div>
        <div style="border:1px solid #ccc;padding:16px;border-radius:8px;min-width:180px">
            <h3>Deudas pendientes</h3>
            <p style="font-size:1.5rem">$<%= totalDeudas %></p>
        </div>
        <div style="border:1px solid #ccc;padding:16px;border-radius:8px;min-width:180px">
            <h3>Clientes registrados</h3>
            <p style="font-size:1.5rem"><%= totalClientes %></p>
        </div>
        <div style="border:1px solid #ccc;padding:16px;border-radius:8px;min-width:180px">
            <h3>Productos stock bajo</h3>
            <p style="font-size:1.5rem"><%= stockBajo != null ? stockBajo.size() : 0 %></p>
        </div>
    </div>

    <h2>Productos con stock bajo o crítico</h2>
    <table border="1" cellpadding="6">
        <thead>
            <tr><th>Nombre</th><th>Stock actual</th><th>Stock mínimo</th><th>Unidad</th></tr>
        </thead>
        <tbody>
        <% if (stockBajo != null) for (Producto p : stockBajo) { %>
            <tr>
                <td><%= p.getNombre() %></td>
                <td style="color:red"><%= p.getStock() %></td>
                <td><%= p.getStockMinimo() %></td>
                <td><%= p.getUnidadMedida() %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
</main>
</body>
</html>

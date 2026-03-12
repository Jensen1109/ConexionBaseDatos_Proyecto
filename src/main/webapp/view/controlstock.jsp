<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto" %>
<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/controlstock.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Control de stock</title>
</head>

<body>
    <header class="header">
        <div class="header__contenedor">
            <a href="<%= ctx %>/ProductoControlador" class="header__link">
                <i class="fa-solid fa-arrow-left header__icono"></i>
            </a>
            <h2 class="header__titulo">Stock de productos</h2>
        </div>
    </header>

    <main class="main">
        <div class="stock-card">
            <h3 class="stock-card_titulo">Tabla stock</h3>

            <div class="stock-card__control">
                <div class="control-filtros">
                    <span class="control-filtros__titulo">Filtrar:</span>
                    <label class="control-filtro">
                        <input type="checkbox" class="control-filtro__checkbox" data-filtro="bajo">
                        <span class="control-filtro__texto control-filtro__texto--bajo">Bajo</span>
                    </label>
                    <label class="control-filtro">
                        <input type="checkbox" class="control-filtro__checkbox" data-filtro="suficiente">
                        <span class="control-filtro__texto control-filtro__texto--suficiente">Suficiente</span>
                    </label>
                    <label class="control-filtro">
                        <input type="checkbox" class="control-filtro__checkbox" data-filtro="critico">
                        <span class="control-filtro__texto control-filtro__texto--critico">Crítico</span>
                    </label>
                </div>

                <div class="stock-card__search">
                    <i class="fa-solid fa-magnifying-glass search__icon"></i>
                    <input type="text" id="buscador" class="search__input" placeholder="Buscar producto..."
                           oninput="filtrarTabla()">
                </div>
            </div>

            <table class="contenedor-tabla" id="tablaStock">
                <thead>
                    <tr>
                        <th class="tabla__header">Producto</th>
                        <th class="tabla__header">Stock actual</th>
                        <th class="tabla__header">Stock mínimo</th>
                        <th class="tabla__header">Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (productos == null || productos.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;">No hay productos registrados.</td>
                    </tr>
                    <% } else {
                        for (Producto p : productos) {
                            int stock   = p.getStock();
                            int minimo  = p.getStockMinimo();
                            String estado;
                            String claseEstado;
                            if (stock == 0 || (minimo > 0 && stock <= minimo * 0.2)) {
                                estado      = "Crítico";
                                claseEstado = "estado--critico";
                            } else if (stock < minimo) {
                                estado      = "Bajo";
                                claseEstado = "estado--bajo";
                            } else {
                                estado      = "Suficiente";
                                claseEstado = "estado--suficiente";
                            }
                    %>
                    <tr class="tabla__row" data-estado="<%= estado.toLowerCase().replace("í","i") %>">
                        <td class="tabla__data"><%= p.getNombre() %></td>
                        <td class="tabla__data"><%= stock %></td>
                        <td class="tabla__data"><%= minimo %></td>
                        <td class="tabla__data <%= claseEstado %>"><%= estado %></td>
                    </tr>
                    <%  }
                    } %>
                </tbody>
            </table>
        </div>
    </main>

    <script>
        // Filtro por checkboxes de estado
        document.querySelectorAll('.control-filtro__checkbox').forEach(cb => {
            cb.addEventListener('change', aplicarFiltros);
        });

        function aplicarFiltros() {
            const activos = [...document.querySelectorAll('.control-filtro__checkbox:checked')]
                            .map(cb => cb.dataset.filtro);
            document.querySelectorAll('#tablaStock tbody tr[data-estado]').forEach(tr => {
                tr.style.display = (activos.length === 0 || activos.includes(tr.dataset.estado))
                    ? '' : 'none';
            });
        }

        function filtrarTabla() {
            const texto = document.getElementById('buscador').value.toLowerCase();
            document.querySelectorAll('#tablaStock tbody tr[data-estado]').forEach(tr => {
                const nombre = tr.querySelector('td').textContent.toLowerCase();
                tr.style.display = nombre.includes(texto) ? '' : 'none';
            });
        }
    </script>
</body>

</html>

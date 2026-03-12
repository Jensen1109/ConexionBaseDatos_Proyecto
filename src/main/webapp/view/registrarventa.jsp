<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Usuario, modelos.MetodoPago" %>
<%
    List<Producto>   productos   = (List<Producto>)   request.getAttribute("productos");
    List<Usuario>    clientes    = (List<Usuario>)    request.getAttribute("clientes");
    List<MetodoPago> metodosPago = (List<MetodoPago>) request.getAttribute("metodosPago");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="<%= ctx %>/styles.css">
    <link rel="stylesheet" href="<%= ctx %>/css/registrarventa.css">
    <link rel="stylesheet" href="<%= ctx %>/css/registrarventa-mediaqueries.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <title>Registrar venta</title>
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
            <a href="<%= ctx %>/PedidoControlador"             class="hamburger-link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="hamburger-link hamburger-link--activo">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="hamburger-link">Deudores</a>
        </nav>

        <div class="contenedor__sidebar">
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__back">
                <i class="fa-solid fa-arrow-left"></i>
            </a>
            <h2 class="sidebar__titulo-principal">Tienda<br>Don Pedro</h2>
            <a href="<%= ctx %>/PedidoControlador"             class="sidebar__link">Historial</a>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link sidebar__link--activo">Registrar Venta</a>
            <a href="<%= ctx %>/DeudaControlador"               class="sidebar__link">Deudores</a>

            <label for="hamburger-toggle" class="hamburger-btn">
                <span></span><span></span><span></span>
            </label>
        </div>
    </aside>

    <main class="main">

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <p style="color:red;text-align:center;"><%= error %></p>
        <% } %>

        <form class="registro-venta" id="formVenta"
              action="<%= ctx %>/PedidoControlador" method="post"
              onsubmit="return validarFormulario()">

            <input type="hidden" name="accion" value="registrar">

            <div class="columna-izquierda">

                <!-- ── CLIENTE ── -->
                <div class="registro-venta__formulario">
                    <h3 class="registro-venta__label">Cliente</h3>
                    <select name="idCliente" class="registro-venta__input" required>
                        <option value="">-- Seleccionar cliente --</option>
                        <% if (clientes != null) {
                            for (Usuario c : clientes) { %>
                        <option value="<%= c.getIdUsuario() %>">
                            <%= c.getNombre() %> <%= c.getApellido() %>
                        </option>
                        <%  }
                        } %>
                    </select>
                </div>

                <!-- ── MÉTODO DE PAGO ── -->
                <div class="registro-venta__formulario">
                    <h3 class="registro-venta__label">Método de pago</h3>
                    <select name="idPago" class="registro-venta__input" required>
                        <option value="">-- Seleccionar método --</option>
                        <% if (metodosPago != null) {
                            for (MetodoPago mp : metodosPago) { %>
                        <option value="<%= mp.getIdPago() %>"><%= mp.getNombre() %></option>
                        <%  }
                        } %>
                    </select>
                </div>

                <!-- ── SELECTOR DE PRODUCTO ── -->
                <div class="registro-venta__busqueda">
                    <select id="selectProducto" class="registro-venta__input-busqueda">
                        <option value="">-- Buscar producto --</option>
                        <% if (productos != null) {
                            for (Producto p : productos) { %>
                        <option value="<%= p.getIdProducto() %>"
                                data-precio="<%= p.getPrecio() %>"
                                data-nombre="<%= p.getNombre().replace("\"","&quot;") %>"
                                data-stock="<%= p.getStock() %>">
                            <%= p.getNombre() %> — $<%= p.getPrecio() %>
                        </option>
                        <%  }
                        } %>
                    </select>
                    <input type="number" id="inputCantidad" class="registro-venta__input"
                           value="1" min="1" style="width:70px;">
                    <button type="button" class="registro-venta__btn-historial"
                            onclick="agregarProducto()">
                        <i class="fas fa-plus"></i> Agregar
                    </button>
                </div>

                <!-- ── TABLA DE PRODUCTOS SELECCIONADOS ── -->
                <table class="contenedor-tabla">
                    <thead class="registro-venta__tabla-head">
                        <tr>
                            <th class="registro-venta__columna">Cantidad</th>
                            <th class="registro-venta__columna">Producto</th>
                            <th class="registro-venta__columna">Precio Unit.</th>
                            <th class="registro-venta__columna">Subtotal</th>
                            <th class="registro-venta__columna"></th>
                        </tr>
                    </thead>
                    <tbody id="tablaProductos" class="registro-venta__tabla-body">
                        <!-- filas añadidas por JS -->
                    </tbody>
                </table>

            </div><!-- /columna-izquierda -->

            <div class="columna-derecha">
                <div class="registro-venta__resumen">
                    <div class="registro-venta__resumen-box">

                        <div class="registro-venta__linea-total">
                            <span class="registro-venta__total-label">Total</span>
                            <span id="totalDisplay" class="registro-venta__total-valor">$0</span>
                        </div>

                        <div class="registro-venta__linea-total">
                            <span class="registro-venta__total-label">Productos</span>
                            <span id="cantProductos" class="registro-venta__total-valor">0</span>
                        </div>

                        <label class="registro-venta__fiado">
                            <input type="checkbox" name="fiado" value="on"
                                   class="registrarventa__checkbox">
                            <p class="registrarventa__texto">Fiado (registra deuda)</p>
                        </label>

                        <a href="<%= ctx %>/PedidoControlador"
                           class="registro-venta__btn-historial">Ver historial</a>

                    </div>

                    <div class="registro-venta__acciones-finales">
                        <button type="submit" class="registro-venta__btn-guardar">
                            Guardar venta
                        </button>
                    </div>
                </div>
            </div><!-- /columna-derecha -->

        </form>
    </main>

    <script>
        let filaId = 0;

        function agregarProducto() {
            const sel  = document.getElementById('selectProducto');
            const opt  = sel.options[sel.selectedIndex];
            const cant = parseInt(document.getElementById('inputCantidad').value) || 1;

            if (!opt.value) { alert('Selecciona un producto.'); return; }

            const id     = opt.value;
            const nombre = opt.dataset.nombre;
            const precio = parseFloat(opt.dataset.precio);
            const stock  = parseInt(opt.dataset.stock);

            if (cant > stock) {
                alert('Stock insuficiente. Disponible: ' + stock);
                return;
            }

            filaId++;
            const subtotal = precio * cant;
            const tbody    = document.getElementById('tablaProductos');
            const tr       = document.createElement('tr');
            tr.className   = 'registro-venta__item';
            tr.id          = 'fila-' + filaId;
            tr.innerHTML   = `
                <td class="registro-venta__valor" data-label="Cantidad">${cant}</td>
                <td class="registro-venta__valor" data-label="Producto">${nombre}</td>
                <td class="registro-venta__valor" data-label="Precio Unit.">$${precio.toLocaleString('es-CO')}</td>
                <td class="registro-venta__valor" data-label="Subtotal">$${subtotal.toLocaleString('es-CO')}</td>
                <td class="registro-venta__valor">
                    <i class="fas fa-trash-alt registro-venta__icono-borrar"
                       style="cursor:pointer"
                       onclick="eliminarFila('fila-${filaId}')"></i>
                </td>
                <input type="hidden" name="idProducto"    value="${id}">
                <input type="hidden" name="cantidad"      value="${cant}">
                <input type="hidden" name="precioUnitario" value="${precio}">
            `;
            tbody.appendChild(tr);

            // reset selector
            sel.value = '';
            document.getElementById('inputCantidad').value = 1;
            actualizarResumen();
        }

        function eliminarFila(id) {
            const fila = document.getElementById(id);
            if (fila) { fila.remove(); actualizarResumen(); }
        }

        function actualizarResumen() {
            const cantidades = document.querySelectorAll('input[name="cantidad"]');
            const precios    = document.querySelectorAll('input[name="precioUnitario"]');
            let total = 0;
            for (let i = 0; i < cantidades.length; i++) {
                total += parseInt(cantidades[i].value) * parseFloat(precios[i].value);
            }
            document.getElementById('totalDisplay').textContent =
                '$' + total.toLocaleString('es-CO');
            document.getElementById('cantProductos').textContent = cantidades.length;
        }

        function validarFormulario() {
            const filas = document.querySelectorAll('input[name="idProducto"]');
            if (filas.length === 0) {
                alert('Agrega al menos un producto a la venta.');
                return false;
            }
            return true;
        }
    </script>
</body>

</html>

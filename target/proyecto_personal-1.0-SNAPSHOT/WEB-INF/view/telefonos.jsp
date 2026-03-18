<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Telefono" %>
<%
    List<Telefono> telefonos = (List<Telefono>) request.getAttribute("telefonos");
    Integer        idCliente = (Integer)        request.getAttribute("idCliente");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Teléfonos del cliente</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', system-ui, sans-serif; background: #f1f5f9; min-height: 100vh; padding: 2rem; }
        .card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 1.5rem; max-width: 520px; margin: 0 auto; }
        .card__title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.2rem; display: flex; align-items: center; gap: 0.5rem; }
        .card__title i { color: #3b82f6; }
        .btn-back { display: inline-flex; align-items: center; gap: 0.4rem; color: #64748b; text-decoration: none; font-size: 0.85rem; margin-bottom: 1.2rem; }
        .btn-back:hover { color: #1e293b; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 1.2rem; }
        th { padding: 0.55rem 0.8rem; text-align: left; font-size: 0.72rem; font-weight: 700; color: #475569; text-transform: uppercase; background: #f8fafc; border-bottom: 1px solid #f1f5f9; }
        td { padding: 0.75rem 0.8rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        .empty { text-align: center; color: #94a3b8; padding: 1.5rem; font-size: 0.875rem; }
        .form-row { display: flex; gap: 0.5rem; margin-top: 1rem; }
        .form-input { flex: 1; padding: 0.55rem 0.85rem; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 0.875rem; font-family: inherit; }
        .form-input:focus { outline: none; border-color: #3b82f6; }
        .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.55rem 1rem; border: none; border-radius: 8px; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
        .btn-add { background: #3b82f6; color: #fff; }
        .btn-add:hover { background: #2563eb; }
        .btn-del { background: none; color: #ef4444; border: none; cursor: pointer; font-size: 0.85rem; padding: 0.3rem 0.5rem; border-radius: 6px; }
        .btn-del:hover { background: #fef2f2; }

        /* ── MODAL CONFIRMACIÓN ── */
        .modal-overlay {
            display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.4);
            z-index: 9999; align-items: center; justify-content: center;
        }
        .modal-overlay.active { display: flex; }
        .modal-box {
            background: #fff; border-radius: 14px; padding: 1.8rem; max-width: 380px; width: 90%;
            box-shadow: 0 8px 30px rgba(0,0,0,0.2); text-align: center;
        }
        .modal-box i.fa-triangle-exclamation { font-size: 2rem; color: #f59e0b; margin-bottom: 0.8rem; }
        .modal-box p { font-size: 0.95rem; color: #1e293b; margin-bottom: 1.4rem; }
        .modal-btns { display: flex; gap: 0.6rem; justify-content: center; }
        .modal-btns button {
            padding: 0.5rem 1.2rem; border: none; border-radius: 8px;
            font-size: 0.85rem; font-weight: 600; cursor: pointer;
        }
        .btn-cancelar { background: #e2e8f0; color: #475569; }
        .btn-cancelar:hover { background: #cbd5e1; }
        .btn-confirmar { background: #ef4444; color: #fff; }
        .btn-confirmar:hover { background: #dc2626; }
    </style>
</head>
<body>
    <a class="btn-back" href="<%= ctx %>/ClienteControlador"><i class="fas fa-arrow-left"></i> Volver a clientes</a>

    <div class="card">
        <div class="card__title"><i class="fas fa-phone"></i> Teléfonos — Cliente #<%= idCliente %></div>

        <table>
            <thead>
                <tr><th>Teléfono</th><th></th></tr>
            </thead>
            <tbody>
            <% if (telefonos == null || telefonos.isEmpty()) { %>
                <tr><td colspan="2" class="empty"><i class="fas fa-phone-slash"></i> Sin teléfonos registrados</td></tr>
            <% } else { for (Telefono t : telefonos) { %>
                <tr>
                    <td><%= t.getTelefono() %></td>
                    <td>
                        <form action="<%= ctx %>/TelefonoControlador" method="post" style="display:inline">
                            <input type="hidden" name="idCliente"  value="<%= idCliente %>">
                            <input type="hidden" name="idTelefono" value="<%= t.getIdTelefono() %>">
                            <input type="hidden" name="accion"     value="eliminar">
                            <button type="button" class="btn-del"
                                    onclick="confirmarEliminar(this.closest('form'), '¿Eliminar este teléfono?')">
                                <i class="fas fa-trash"></i>
                            </button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>

        <form action="<%= ctx %>/TelefonoControlador" method="post">
            <input type="hidden" name="idCliente" value="<%= idCliente %>">
            <input type="hidden" name="accion"    value="agregar">
            <div class="form-row">
                <input type="tel" name="telefono" class="form-input"
                       placeholder="Nuevo número de teléfono" required
                       pattern="\d{1,15}" title="Solo números, máximo 15 dígitos">
                <button type="submit" class="btn btn-add">
                    <i class="fas fa-plus"></i> Agregar
                </button>
            </div>
        </form>
    </div>

    <!-- Modal de confirmación -->
    <div class="modal-overlay" id="modalConfirm">
        <div class="modal-box">
            <i class="fas fa-triangle-exclamation"></i>
            <p id="modalMsg"></p>
            <div class="modal-btns">
                <button class="btn-cancelar" onclick="cerrarModal()">Cancelar</button>
                <button class="btn-confirmar" id="btnConfirmar">Eliminar</button>
            </div>
        </div>
    </div>

    <script>
        var formPendiente = null;
        function confirmarEliminar(form, mensaje) {
            formPendiente = form;
            document.getElementById('modalMsg').textContent = mensaje;
            document.getElementById('modalConfirm').classList.add('active');
        }
        function cerrarModal() {
            document.getElementById('modalConfirm').classList.remove('active');
            formPendiente = null;
        }
        document.getElementById('btnConfirmar').addEventListener('click', function() {
            if (formPendiente) formPendiente.submit();
        });
    </script>
</body>
</html>

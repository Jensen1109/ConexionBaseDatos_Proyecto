<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Cliente, modelos.Usuario" %>
<%
    List<Cliente> clientes    = (List<Cliente>) request.getAttribute("clientes");
    String        error       = (String)        request.getAttribute("error");
    String        exito       = (String)        request.getAttribute("exito");
    String        ctx         = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);
    request.setAttribute("_paginaActiva", "clientes");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Clientes — Tienda Don Pedro</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body { display: flex; min-height: 100vh; font-family: 'Segoe UI', system-ui, sans-serif; background: #f1f5f9; }

        /* ── SIDEBAR ── */
        .sidebar {
            width: 230px; background: #1e293b; min-height: 100vh;
            display: flex; flex-direction: column;
            position: fixed; top: 0; left: 0; height: 100vh; overflow-y: auto;
            z-index: 100; transition: transform 0.3s;
        }
        .sidebar__brand { padding: 1.4rem 1.2rem; border-bottom: 1px solid rgba(255,255,255,0.08); }
        .sidebar__brand-title { color: #f8fafc; font-size: 1rem; font-weight: 700; }
        .sidebar__brand-sub { color: #64748b; font-size: 0.72rem; margin-top: 2px; }
        .sidebar__section { padding: 1.2rem 0 0.4rem; }
        .sidebar__label {
            color: #475569; font-size: 0.63rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.1em;
            padding: 0 1.2rem; display: block; margin-bottom: 0.2rem;
        }
        .sidebar__link {
            display: flex; align-items: center; gap: 0.65rem;
            color: #94a3b8; text-decoration: none;
            padding: 0.55rem 1.2rem; font-size: 0.85rem;
            border-left: 3px solid transparent; transition: all 0.18s;
        }
        .sidebar__link i { width: 15px; text-align: center; font-size: 0.8rem; }
        .sidebar__link:hover { color: #e2e8f0; background: rgba(255,255,255,0.05); border-left-color: #3b82f6; }
        .sidebar__link--activo { color: #fff; background: rgba(59,130,246,0.12); border-left-color: #3b82f6; }
        .sidebar__section--cuenta { border-top: 1px solid rgba(255,255,255,0.08); margin-top: 0.5rem; }
        .sidebar__user-card { display: flex; align-items: center; gap: 0.65rem; padding: 0.4rem 1.2rem 0.6rem; }
        .sidebar__user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: #fff; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; }
        .sidebar__user-name { color: #e2e8f0; font-size: 0.82rem; font-weight: 600; line-height: 1.3; }
        .sidebar__user-role { color: #64748b; font-size: 0.7rem; }
        .sidebar__link--logout { color: #f87171 !important; }
        .sidebar__link--logout:hover { color: #fff !important; background: rgba(239,68,68,0.12) !important; border-left-color: #ef4444 !important; }

        /* ── MAIN ── */
        .main { margin-left: 230px; flex: 1; padding: 2rem 2.5rem; }
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.8rem; }
        .page-header__title { font-size: 1.65rem; font-weight: 700; color: #1e293b; }
        .page-header__sub { color: #64748b; font-size: 0.85rem; margin-top: 0.2rem; }

        /* ── ALERTAS ── */
        .alert { border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.5rem; font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem; }
        .alert-error { background: #fef2f2; border: 1px solid #fca5a5; color: #dc2626; }
        .alert-ok    { background: #f0fdf4; border: 1px solid #86efac; color: #16a34a; }

        /* ── LAYOUT ── */
        .layout { display: grid; grid-template-columns: 1fr 320px; gap: 1.5rem; align-items: start; }
        @media (max-width: 900px) { .layout { grid-template-columns: 1fr; } }

        /* ── CARD ── */
        .card { background: #fff; border-radius: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 1.5rem; }
        .card__title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.2rem; display: flex; align-items: center; gap: 0.5rem; }
        .card__title i { color: #3b82f6; }

        /* ── TABLA ── */
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        thead tr { background: #f8fafc; }
        th { padding: 0.65rem 0.9rem; text-align: left; font-size: 0.72rem; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #f1f5f9; }
        td { padding: 0.8rem 0.9rem; font-size: 0.875rem; color: #334155; border-bottom: 1px solid #f8fafc; }
        tbody tr:last-child td { border-bottom: none; }
        tbody tr:hover { background: #f8fafc; }
        .td-nombre { font-weight: 600; color: #1e293b; }
        .td-ced { font-family: monospace; font-size: 0.82rem; color: #64748b; }
        .empty-row td { text-align: center; padding: 2rem; color: #94a3b8; font-size: 0.875rem; }

        /* ── BUSCAR ── */
        .search-box { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
        .search-box input { flex: 1; padding: 0.55rem 0.85rem; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 0.875rem; background: #f8fafc; font-family: inherit; }
        .search-box input:focus { outline: none; border-color: #3b82f6; background: #fff; }

        /* ── FORM ── */
        .form-group { display: flex; flex-direction: column; gap: 0.35rem; margin-bottom: 0.85rem; }
        .form-label { font-size: 0.78rem; font-weight: 600; color: #374151; }
        .form-input { padding: 0.55rem 0.85rem; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 0.875rem; color: #1e293b; background: #f8fafc; font-family: inherit; width: 100%; }
        .form-input:focus { outline: none; border-color: #3b82f6; background: #fff; box-shadow: 0 0 0 3px rgba(59,130,246,0.08); }

        .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.6rem 1.1rem; border: none; border-radius: 8px; font-size: 0.85rem; font-weight: 600; cursor: pointer; transition: background 0.18s; }
        .btn-primary { background: #3b82f6; color: #fff; width: 100%; justify-content: center; padding: 0.7rem; }
        .btn-primary:hover { background: #2563eb; }
        .btn-icon { background: none; border: none; cursor: pointer; padding: 0.3rem 0.5rem; border-radius: 6px; font-size: 0.9rem; transition: background 0.15s; }
        .btn-edit   { color: #3b82f6; } .btn-edit:hover   { background: #eff6ff; }
        .btn-delete { color: #ef4444; } .btn-delete:hover { background: #fef2f2; }

        /* ── MODAL EDITAR ── */
        .modal-overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 200; align-items: center; justify-content: center; }
        .modal-overlay.open { display: flex; }
        .modal { background: #fff; border-radius: 14px; padding: 1.5rem; width: 100%; max-width: 420px; box-shadow: 0 8px 32px rgba(0,0,0,0.15); }
        .modal__title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 1.2rem; display: flex; align-items: center; gap: 0.5rem; }
        .modal__title i { color: #3b82f6; }
        .modal__footer { display: flex; gap: 0.75rem; margin-top: 1rem; }
        .btn-secondary { background: #f1f5f9; color: #475569; flex: 1; justify-content: center; }
        .btn-secondary:hover { background: #e2e8f0; }
        .btn-save { background: #22c55e; color: #fff; flex: 1; justify-content: center; }
        .btn-save:hover { background: #16a34a; }

        /* ── HAMBURGER ── */
        .hamburger-btn { display: none; position: fixed; top: 0.8rem; left: 0.8rem; z-index: 150; background: #1e293b; color: #fff; border: none; border-radius: 8px; padding: 0.55rem 0.75rem; font-size: 1rem; cursor: pointer; }
        .overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 90; }
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4rem; }
        }

        /* ── VALIDACIÓN ── */
        .form-input.input-ok    { border-color: #22c55e; }
        .form-input.input-error { border-color: #ef4444; background: #fff5f5; }
        .field-error { display: none; color: #dc2626; font-size: 0.73rem; margin-top: 0.2rem; }
        .field-error.visible    { display: flex; align-items: center; gap: 0.3rem; }
    </style>
</head>
<body>

    <button class="hamburger-btn" onclick="toggleSidebar()"><i class="fas fa-bars"></i></button>
    <div class="overlay" id="overlay" onclick="toggleSidebar()"></div>

    <jsp:include page="sidebar.jsp" />

    <main class="main">
        <div class="page-header">
            <div>
                <h1 class="page-header__title">Clientes</h1>
                <p class="page-header__sub">Gestiona los clientes registrados en la tienda.</p>
            </div>
        </div>

        <% if (error != null) { %>
        <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
        <% } %>
        <% if (exito != null) { %>
        <div class="alert alert-ok"><i class="fas fa-check-circle"></i> <%= exito %></div>
        <% } %>

        <div class="layout">
            <!-- TABLA DE CLIENTES -->
            <div class="card">
                <div class="card__title"><i class="fas fa-users"></i> Clientes registrados
                    <span style="margin-left:auto;font-size:0.78rem;font-weight:400;color:#64748b;">
                        <%= clientes != null ? clientes.size() : 0 %> cliente(s)
                    </span>
                </div>

                <div class="search-box">
                    <input type="text" id="buscar" placeholder="Buscar por nombre o cédula..."
                           oninput="filtrar(this.value)">
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Cédula</th>
                                <th>Correo</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody id="tbodyClientes">
                            <% if (clientes == null || clientes.isEmpty()) { %>
                            <tr class="empty-row">
                                <td colspan="4">
                                    <i class="fas fa-user-slash" style="font-size:1.5rem;display:block;margin-bottom:0.5rem;"></i>
                                    No hay clientes registrados aún
                                </td>
                            </tr>
                            <% } else { for (Cliente c : clientes) {
                                String nomC = c.getNombre() != null ? c.getNombre() : "";
                                String apeC = c.getApellido() != null ? c.getApellido() : "";
                                String cedC = c.getCedula() != null ? c.getCedula() : "";
                                String emlC = c.getEmail() != null ? c.getEmail() : "—";
                            %>
                            <tr class="fila-cliente"
                                data-nombre="<%= (nomC + " " + apeC).toLowerCase() %>"
                                data-cedula="<%= cedC.toLowerCase() %>">
                                <td class="td-nombre"><%= nomC %> <%= apeC %></td>
                                <td class="td-ced"><%= cedC %></td>
                                <td><%= emlC %></td>
                                <td style="white-space:nowrap;">
                                    <button type="button" class="btn-icon btn-edit"
                                            title="Editar"
                                            onclick="abrirEditar(<%= c.getIdCliente() %>, '<%= nomC.replace("'","&#39;") %>', '<%= apeC.replace("'","&#39;") %>', '<%= cedC %>', '<%= emlC.equals("—") ? "" : emlC.replace("'","&#39;") %>')">
                                        <i class="fas fa-pen"></i>
                                    </button>
                                    <form method="post" action="<%= ctx %>/ClienteControlador" style="display:inline;"
                                          onsubmit="return confirm('¿Eliminar a <%= nomC %> <%= apeC %>?')">
                                        <input type="hidden" name="accion"    value="eliminar">
                                        <input type="hidden" name="idCliente" value="<%= c.getIdCliente() %>">
                                        <button type="submit" class="btn-icon btn-delete" title="Eliminar">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                            <% } } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- FORMULARIO NUEVO CLIENTE -->
            <div class="card">
                <div class="card__title"><i class="fas fa-user-plus"></i> Registrar cliente</div>
                <form method="post" action="<%= ctx %>/ClienteControlador" novalidate onsubmit="return validarFormCrear(this)">
                    <input type="hidden" name="accion" value="crear">
                    <div class="form-group">
                        <label class="form-label">Nombre *</label>
                        <input type="text" name="nombre" id="crNombre" class="form-input" placeholder="Nombre"
                               oninput="validarNombre(this, 'errCrNombre')">
                        <span class="field-error" id="errCrNombre"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Apellido *</label>
                        <input type="text" name="apellido" id="crApellido" class="form-input" placeholder="Apellido"
                               oninput="validarNombre(this, 'errCrApellido')">
                        <span class="field-error" id="errCrApellido"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Cédula *</label>
                        <input type="text" name="cedula" id="crCedula" class="form-input" placeholder="Mínimo 8 dígitos"
                               oninput="soloNumerosYValidar(this, 'errCrCedula', true)"
                               onkeypress="return soloDigitos(event)">
                        <span class="field-error" id="errCrCedula"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Teléfono</label>
                        <input type="text" name="telefono" id="crTelefono" class="form-input" placeholder="Teléfono (opcional)"
                               oninput="validarTelefono(this, 'errCrTelefono')"
                               onkeypress="return soloDigitos(event)">
                        <span class="field-error" id="errCrTelefono"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Correo electrónico</label>
                        <input type="text" name="email" id="crEmail" class="form-input" placeholder="correo@ejemplo.com (opcional)"
                               oninput="validarEmail(this, 'errCrEmail')">
                        <span class="field-error" id="errCrEmail"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Registrar cliente
                    </button>
                </form>
            </div>
        </div>
    </main>

    <!-- MODAL EDITAR -->
    <div class="modal-overlay" id="modalEditar">
        <div class="modal">
            <div class="modal__title"><i class="fas fa-pen"></i> Editar cliente</div>
            <form method="post" action="<%= ctx %>/ClienteControlador" novalidate onsubmit="return validarFormEditar(this)">
                <input type="hidden" name="accion"    value="actualizar">
                <input type="hidden" name="idCliente" id="editId">
                <div class="form-group">
                    <label class="form-label">Nombre *</label>
                    <input type="text" name="nombre" id="editNombre" class="form-input"
                           oninput="validarNombre(this, 'errEdNombre')">
                    <span class="field-error" id="errEdNombre"><i class="fas fa-exclamation-circle"></i></span>
                </div>
                <div class="form-group">
                    <label class="form-label">Apellido *</label>
                    <input type="text" name="apellido" id="editApellido" class="form-input"
                           oninput="validarNombre(this, 'errEdApellido')">
                    <span class="field-error" id="errEdApellido"><i class="fas fa-exclamation-circle"></i></span>
                </div>
                <div class="form-group">
                    <label class="form-label">Cédula *</label>
                    <input type="text" name="cedula" id="editCedula" class="form-input"
                           oninput="soloNumerosYValidar(this, 'errEdCedula', true)"
                           onkeypress="return soloDigitos(event)">
                    <span class="field-error" id="errEdCedula"><i class="fas fa-exclamation-circle"></i></span>
                </div>
                <div class="form-group">
                    <label class="form-label">Teléfono</label>
                    <input type="text" name="telefono" id="editTelefono" class="form-input"
                           oninput="validarTelefono(this, 'errEdTelefono')"
                           onkeypress="return soloDigitos(event)">
                    <span class="field-error" id="errEdTelefono"><i class="fas fa-exclamation-circle"></i></span>
                </div>
                <div class="form-group">
                    <label class="form-label">Correo electrónico</label>
                    <input type="text" name="email" id="editEmail" class="form-input" placeholder="correo@ejemplo.com"
                           oninput="validarEmail(this, 'errEdEmail')">
                    <span class="field-error" id="errEdEmail"><i class="fas fa-exclamation-circle"></i></span>
                </div>
                <div class="modal__footer">
                    <button type="button" class="btn btn-secondary" onclick="cerrarEditar()">
                        <i class="fas fa-times"></i> Cancelar
                    </button>
                    <button type="submit" class="btn btn-save">
                        <i class="fas fa-check"></i> Guardar
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        /* ── UTILIDADES DE VALIDACIÓN ── */
        function soloDigitos(e) {
            return /\d/.test(String.fromCharCode(e.which || e.keyCode));
        }

        function setError(input, errId, msg) {
            input.classList.remove('input-ok');
            input.classList.add('input-error');
            var span = document.getElementById(errId);
            span.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
            span.classList.add('visible');
        }

        function setOk(input, errId) {
            input.classList.remove('input-error');
            input.classList.add('input-ok');
            document.getElementById(errId).classList.remove('visible');
        }

        function clearState(input, errId) {
            input.classList.remove('input-error', 'input-ok');
            document.getElementById(errId).classList.remove('visible');
        }

        function validarNombre(input, errId) {
            var v = input.value.trim();
            if (v === '') { setError(input, errId, 'Este campo es obligatorio.'); return false; }
            if (v.length < 2) { setError(input, errId, 'Debe tener al menos 2 caracteres.'); return false; }
            if (!/^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]+$/.test(v)) { setError(input, errId, 'Solo se permiten letras y espacios.'); return false; }
            setOk(input, errId); return true;
        }

        function soloNumerosYValidar(input, errId, requerido) {
            input.value = input.value.replace(/\D/g, '');
            return validarCedula(input, errId, requerido);
        }

        function validarCedula(input, errId, requerido) {
            var v = input.value.trim();
            if (v === '' && !requerido) { clearState(input, errId); return true; }
            if (v === '') { setError(input, errId, 'La cédula es obligatoria.'); return false; }
            if (!/^\d+$/.test(v)) { setError(input, errId, 'La cédula solo debe contener números.'); return false; }
            if (v.length < 8) { setError(input, errId, 'La cédula debe tener mínimo 8 dígitos.'); return false; }
            if (v.length > 15) { setError(input, errId, 'La cédula no puede superar 15 dígitos.'); return false; }
            setOk(input, errId); return true;
        }

        function validarTelefono(input, errId) {
            input.value = input.value.replace(/\D/g, '');
            var v = input.value.trim();
            if (v === '') { clearState(input, errId); return true; }
            if (v.length > 15) { setError(input, errId, 'El teléfono no puede superar 15 dígitos.'); return false; }
            setOk(input, errId); return true;
        }

        function validarEmail(input, errId) {
            var v = input.value.trim();
            if (v === '') { clearState(input, errId); return true; }
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v)) {
                setError(input, errId, 'Correo inválido. Ej: nombre@gmail.com'); return false;
            }
            setOk(input, errId); return true;
        }

        /* ── VALIDAR FORM CREAR ── */
        function validarFormCrear(form) {
            var ok = true;
            ok = validarNombre(document.getElementById('crNombre'),   'errCrNombre')   && ok;
            ok = validarNombre(document.getElementById('crApellido'), 'errCrApellido') && ok;
            ok = validarCedula(document.getElementById('crCedula'),   'errCrCedula', true) && ok;
            ok = validarTelefono(document.getElementById('crTelefono'), 'errCrTelefono') && ok;
            ok = validarEmail(document.getElementById('crEmail'),     'errCrEmail')    && ok;
            if (!ok) { form.querySelector('.field-error.visible').scrollIntoView({behavior:'smooth', block:'center'}); }
            return ok;
        }

        /* ── VALIDAR FORM EDITAR ── */
        function validarFormEditar(form) {
            var ok = true;
            ok = validarNombre(document.getElementById('editNombre'),   'errEdNombre')   && ok;
            ok = validarNombre(document.getElementById('editApellido'), 'errEdApellido') && ok;
            ok = validarCedula(document.getElementById('editCedula'),   'errEdCedula', true) && ok;
            ok = validarTelefono(document.getElementById('editTelefono'), 'errEdTelefono') && ok;
            ok = validarEmail(document.getElementById('editEmail'),     'errEdEmail')    && ok;
            return ok;
        }

        /* ── BÚSQUEDA TABLA ── */
        function filtrar(q) {
            q = q.trim().toLowerCase();
            document.querySelectorAll('.fila-cliente').forEach(function(fila) {
                const nombre = fila.getAttribute('data-nombre') || '';
                const cedula = fila.getAttribute('data-cedula') || '';
                fila.style.display = (nombre.includes(q) || cedula.includes(q)) ? '' : 'none';
            });
        }

        /* ── MODAL EDITAR ── */
        function abrirEditar(id, nombre, apellido, cedula, email) {
            document.getElementById('editId').value       = id;
            document.getElementById('editNombre').value   = nombre;
            document.getElementById('editApellido').value = apellido;
            document.getElementById('editCedula').value   = cedula;
            document.getElementById('editTelefono').value = '';
            document.getElementById('editEmail').value    = email || '';
            // limpiar estados de validación al abrir
            ['editNombre','editApellido','editCedula','editTelefono','editEmail'].forEach(function(id) {
                var el = document.getElementById(id);
                el.classList.remove('input-ok','input-error');
            });
            ['errEdNombre','errEdApellido','errEdCedula','errEdTelefono','errEdEmail'].forEach(function(id) {
                document.getElementById(id).classList.remove('visible');
            });
            document.getElementById('modalEditar').classList.add('open');
        }

        function cerrarEditar() {
            document.getElementById('modalEditar').classList.remove('open');
        }

        document.getElementById('modalEditar').addEventListener('click', function(e) {
            if (e.target === this) cerrarEditar();
        });

        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }
    </script>
</body>
</html>

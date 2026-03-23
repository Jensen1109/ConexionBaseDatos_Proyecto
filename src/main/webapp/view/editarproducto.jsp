<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, modelos.Producto, modelos.Categoria, modelos.Usuario" %>
<%
    Producto producto        = (Producto)             request.getAttribute("producto");
    List<Categoria> categorias = (List<Categoria>)    request.getAttribute("categorias");
    String error = (String) request.getAttribute("error");
    String ctx   = request.getContextPath();
    Usuario usuarioActual = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdmin = (usuarioActual != null && usuarioActual.getIdRol() == 1);

    if (producto == null) {
        response.sendRedirect(ctx + "/ProductoControlador");
        return;
    }
    request.setAttribute("_paginaActiva", "editarProducto");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Editar Producto — Tienda Don Pedro</title>
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

        /* ── MAIN ── */
        .main {
            margin-left: 230px; flex: 1; padding: 3rem 2rem;
            display: flex; justify-content: center; align-items: flex-start;
        }

        /* ── CARD ── */
        .card {
            background: #fff; border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            padding: 2.5rem; width: 100%; max-width: 600px;
        }
        .card__header { margin-bottom: 2rem; }
        .card__back {
            display: inline-flex; align-items: center; gap: 0.5rem;
            color: #64748b; text-decoration: none; font-size: 0.85rem;
            margin-bottom: 1rem; transition: color 0.18s;
        }
        .card__back:hover { color: #1e293b; }
        .card__title { font-size: 1.5rem; font-weight: 700; color: #1e293b; }
        .card__subtitle { color: #64748b; font-size: 0.875rem; margin-top: 0.3rem; }
        .card__badge {
            display: inline-block; background: #eff6ff; color: #3b82f6;
            font-size: 0.75rem; font-weight: 600; padding: 0.25rem 0.7rem;
            border-radius: 20px; margin-top: 0.5rem;
        }

        /* ── ERROR ── */
        .alert-error {
            background: #fef2f2; border: 1px solid #fca5a5;
            color: #dc2626; border-radius: 8px;
            padding: 0.75rem 1rem; margin-bottom: 1.5rem;
            font-size: 0.875rem; display: flex; align-items: center; gap: 0.5rem;
        }

        /* ── FORM ── */
        .form { display: flex; flex-direction: column; gap: 1.25rem; }
        .form__row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
        .form__group { display: flex; flex-direction: column; gap: 0.4rem; }
        .form__label { font-size: 0.82rem; font-weight: 600; color: #374151; }
        .form__input, .form__select, .form__textarea {
            padding: 0.65rem 0.9rem;
            border: 1.5px solid #e2e8f0;
            border-radius: 8px; font-size: 0.875rem; color: #1e293b;
            background: #f8fafc; transition: border-color 0.18s, background 0.18s;
            font-family: inherit; width: 100%;
        }
        .form__input:focus, .form__select:focus, .form__textarea:focus {
            outline: none; border-color: #3b82f6; background: #fff;
            box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
        }
        .form__textarea { resize: vertical; min-height: 90px; }

        /* ── VALIDACIÓN ── */
        .form__input.input-ok,
        .form__select.input-ok  { border-color: #22c55e; }
        .form__input.input-error,
        .form__select.input-error { border-color: #ef4444; background: #fff5f5; }

        /* ── NUEVA CATEGORÍA INLINE ── */
        .cat-row { display: flex; align-items: flex-end; gap: 0.5rem; }
        .cat-row .form__select { flex: 1; }
        .btn-new-cat {
            padding: 0.65rem 0.75rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 8px; font-size: 0.85rem;
            cursor: pointer; transition: background 0.18s; flex-shrink: 0;
            display: flex; align-items: center; gap: 0.3rem;
        }
        .btn-new-cat:hover { background: #2563eb; }
        .new-cat-box {
            display: none; background: #f0f7ff; border: 1.5px solid #bfdbfe;
            border-radius: 8px; padding: 0.75rem; margin-top: 0.5rem;
        }
        .new-cat-box.visible { display: block; }
        .new-cat-inner { display: flex; gap: 0.5rem; align-items: center; }
        .new-cat-inner input {
            flex: 1; padding: 0.5rem 0.7rem; border: 1.5px solid #e2e8f0;
            border-radius: 6px; font-size: 0.82rem; font-family: inherit;
        }
        .new-cat-inner input:focus { outline: none; border-color: #3b82f6; }
        .btn-cat-save {
            padding: 0.5rem 0.8rem; background: #22c55e; color: #fff;
            border: none; border-radius: 6px; font-size: 0.8rem; font-weight: 600;
            cursor: pointer; transition: background 0.18s;
        }
        .btn-cat-save:hover { background: #16a34a; }
        .btn-cat-cancel {
            padding: 0.5rem 0.6rem; background: #e2e8f0; color: #64748b;
            border: none; border-radius: 6px; font-size: 0.8rem;
            cursor: pointer; transition: background 0.18s;
        }
        .btn-cat-cancel:hover { background: #cbd5e1; }
        .new-cat-msg { font-size: 0.75rem; margin-top: 0.4rem; }
        .field-error { display: none; color: #dc2626; font-size: 0.73rem; margin-top: 0.2rem; }
        .field-error.visible { display: flex; align-items: center; gap: 0.3rem; }

        /* ── IMAGE UPLOAD ── */
        .img-upload-box {
            border: 2px dashed #e2e8f0; border-radius: 10px;
            padding: 1.4rem; text-align: center; cursor: pointer;
            transition: border-color 0.18s, background 0.18s; position: relative;
            background: #f8fafc;
        }
        .img-upload-box:hover { border-color: #3b82f6; background: #f0f7ff; }
        .img-upload-box input[type=file] {
            position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%;
        }
        .img-upload-box i { font-size: 1.8rem; color: #94a3b8; display: block; margin-bottom: 0.4rem; }
        .img-upload-box .upload-text { font-size: 0.82rem; color: #64748b; }
        .img-upload-box .upload-hint { font-size: 0.75rem; color: #94a3b8; margin-top: 0.2rem; }
        .img-current {
            width: 100%; max-height: 180px; object-fit: cover;
            border-radius: 8px; margin-bottom: 0.75rem; display: block;
            border: 1px solid #e2e8f0;
        }
        .img-preview {
            width: 100%; max-height: 180px; object-fit: cover;
            border-radius: 8px; margin-top: 0.75rem; display: none;
            border: 1px solid #e2e8f0;
        }
        .img-label-current { font-size: 0.75rem; color: #64748b; margin-bottom: 0.4rem; display: block; }

        /* ── ACTIONS ── */
        .form__actions { display: flex; align-items: center; gap: 1rem; margin-top: 0.5rem; }
        .btn-submit {
            padding: 0.7rem 2rem; background: #3b82f6; color: #fff;
            border: none; border-radius: 9px; font-size: 0.9rem; font-weight: 600;
            cursor: pointer; transition: background 0.18s, transform 0.15s;
            display: inline-flex; align-items: center; gap: 0.5rem;
        }
        .btn-submit:hover { background: #2563eb; transform: translateY(-1px); }
        .btn-cancel {
            color: #64748b; text-decoration: none; font-size: 0.875rem;
            transition: color 0.18s;
        }
        .btn-cancel:hover { color: #1e293b; }

        /* ── HAMBURGER ── */
        .hamburger-btn {
            display: none; position: fixed; top: 0.8rem; left: 0.8rem;
            z-index: 150; background: #1e293b; color: #fff;
            border: none; border-radius: 8px; padding: 0.55rem 0.75rem;
            font-size: 1rem; cursor: pointer;
        }
        .overlay {
            display: none; position: fixed; inset: 0;
            background: rgba(0,0,0,0.45); z-index: 90;
        }

        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); }
            .sidebar.open { transform: translateX(0); }
            .overlay.open { display: block; }
            .hamburger-btn { display: flex; align-items: center; }
            .main { margin-left: 0; padding: 1rem; padding-top: 4.5rem; }
            .form__row { grid-template-columns: 1fr; }
        }
        .sidebar__section--cuenta { border-top: 1px solid rgba(255,255,255,0.08); margin-top: 0.5rem; }
        .sidebar__user-card { display: flex; align-items: center; gap: 0.65rem; padding: 0.4rem 1.2rem 0.6rem; }
        .sidebar__user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: #fff; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; }
        .sidebar__user-name { color: #e2e8f0; font-size: 0.82rem; font-weight: 600; line-height: 1.3; }
        .sidebar__user-role { color: #64748b; font-size: 0.7rem; }
        .sidebar__link--logout { color: #f87171 !important; }
        .sidebar__link--logout:hover { color: #fff !important; background: rgba(239,68,68,0.12) !important; border-left-color: #ef4444 !important; }
    </style>
</head>
<body>

    <button class="hamburger-btn" onclick="toggleSidebar()">
        <i class="fas fa-bars"></i>
    </button>
    <div class="overlay" id="overlay" onclick="toggleSidebar()"></div>

    <jsp:include page="sidebar.jsp" />

    <main class="main">
        <div class="card">
            <div class="card__header">
                <a href="<%= ctx %>/ProductoControlador" class="card__back">
                    <i class="fas fa-arrow-left"></i> Volver a productos
                </a>
                <h1 class="card__title">Editar producto</h1>
                <p class="card__subtitle">Modifique los datos y confirme los cambios.</p>
                <span class="card__badge"><i class="fas fa-tag"></i> ID #<%= producto.getIdProducto() %></span>
            </div>

            <% if (error != null) { %>
            <div class="alert-error">
                <i class="fas fa-exclamation-circle"></i> <%= error %>
            </div>
            <% } %>

            <form class="form" action="${pageContext.request.contextPath}/ProductoControlador"
                  method="post" enctype="multipart/form-data"
                  novalidate onsubmit="return validarFormProducto()">
                <input type="hidden" name="accion" value="actualizar">
                <input type="hidden" name="id"     value="<%= producto.getIdProducto() %>">

                <div class="form__group">
                    <label class="form__label">Imagen del producto</label>
                    <% if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) { %>
                    <span class="img-label-current"><i class="fas fa-image"></i> Imagen actual:</span>
                    <img class="img-current"
                         src="<%= ctx %>/uploads/productos/<%= producto.getImagenUrl() %>"
                         alt="Imagen actual"
                         onerror="this.style.display='none'">
                    <% } %>
                    <div class="img-upload-box" id="uploadBox">
                        <input type="file" name="imagen" id="imgInput" accept="image/*"
                               onchange="previewImage(this)">
                        <i class="fas fa-camera"></i>
                        <div class="upload-text"><%= (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) ? "Cambiar imagen" : "Subir imagen" %></div>
                        <div class="upload-hint">JPG, PNG, WEBP — máx. 5 MB. Dejar vacío para mantener la actual.</div>
                        <img id="imgPreview" class="img-preview" alt="Nueva imagen">
                    </div>
                </div>

                <div class="form__group">
                    <label class="form__label" for="nombre">Nombre del producto *</label>
                    <input type="text" id="nombre" name="nombre" class="form__input"
                           value="<%= producto.getNombre() %>" maxlength="80"
                           oninput="prValidarNombre(this)">
                    <span class="field-error" id="errNombre"><i class="fas fa-exclamation-circle"></i></span>
                </div>

                <div class="form__group">
                    <%
                        String descActual = producto.getDescripcion() != null ? producto.getDescripcion() : "";
                        int descLen = descActual.length();
                    %>
                    <label class="form__label" for="descripcion">Descripción
                        <span id="cntDesc" style="font-weight:400;color:#94a3b8;font-size:0.72rem;margin-left:0.4rem;"><%= descLen %> / 150</span>
                    </label>
                    <textarea id="descripcion" name="descripcion" class="form__textarea"
                              maxlength="150" oninput="prContarDesc(this)"><%= descActual %></textarea>
                    <span class="field-error" id="errDesc"><i class="fas fa-exclamation-circle"></i></span>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label class="form__label" for="precio">Precio ($) *</label>
                        <input type="number" id="precio" name="precio" class="form__input"
                               value="<%= producto.getPrecio() %>" step="0.01" min="0.01"
                               oninput="prValidarNumPos(this, 'errPrecio', true)">
                        <span class="field-error" id="errPrecio"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form__group">
                        <label class="form__label" for="unidadMedida">Unidad de medida</label>
                        <input type="text" id="unidadMedida" name="unidadMedida" class="form__input"
                               value="<%= producto.getUnidadMedida() != null ? producto.getUnidadMedida() : "" %>">
                    </div>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label class="form__label" for="stock">Stock actual *</label>
                        <input type="number" id="stock" name="stock" class="form__input"
                               value="<%= producto.getStock() %>" min="0"
                               oninput="prValidarNumPos(this, 'errStock', false)">
                        <span class="field-error" id="errStock"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                    <div class="form__group">
                        <label class="form__label" for="stockMinimo">Stock mínimo *</label>
                        <input type="number" id="stockMinimo" name="stockMinimo" class="form__input"
                               value="<%= producto.getStockMinimo() %>" min="0"
                               oninput="prValidarNumPos(this, 'errStockMin', false)">
                        <span class="field-error" id="errStockMin"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label class="form__label" for="fechaVencimiento">Fecha de vencimiento</label>
                        <input type="date" id="fechaVencimiento" name="fechaVencimiento" class="form__input"
                               min="<%= java.time.LocalDate.now().toString() %>"
                               value="<%= producto.getFechaVencimiento() != null ? producto.getFechaVencimiento().toString() : "" %>">
                    </div>
                    <div class="form__group">
                        <label class="form__label" for="idCategoria">Categoría *</label>
                        <div class="cat-row">
                            <select id="idCategoria" name="idCategoria" class="form__select"
                                    onchange="prValidarCategoria(this)">
                                <option value="">-- Seleccione --</option>
                                <% if (categorias != null) {
                                    for (Categoria c : categorias) {
                                        boolean sel = c.getIdCategoria() == producto.getIdCategoria();
                                %>
                                <option value="<%= c.getIdCategoria() %>" <%= sel ? "selected" : "" %>><%= c.getNombre() %></option>
                                <% } } %>
                            </select>
                            <button type="button" class="btn-new-cat" onclick="toggleNuevaCat()" title="Crear nueva categoría">
                                <i class="fas fa-plus"></i>
                            </button>
                        </div>
                        <div class="new-cat-box" id="newCatBox">
                            <div class="new-cat-inner">
                                <input type="text" id="newCatNombre" placeholder="Nombre de la categoría" maxlength="60">
                                <button type="button" class="btn-cat-save" onclick="crearCategoriaAjax()">
                                    <i class="fas fa-check"></i> Crear
                                </button>
                                <button type="button" class="btn-cat-cancel" onclick="toggleNuevaCat()">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                            <div class="new-cat-msg" id="newCatMsg"></div>
                        </div>
                        <span class="field-error" id="errCategoria"><i class="fas fa-exclamation-circle"></i></span>
                    </div>
                </div>

                <div class="form__actions">
                    <button type="submit" class="btn-submit">
                        <i class="fas fa-check"></i> Confirmar edición
                    </button>
                    <a href="<%= ctx %>/ProductoControlador" class="btn-cancel">Cancelar</a>
                </div>
            </form>
        </div>
    </main>

    <script>
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('open');
            document.getElementById('overlay').classList.toggle('open');
        }

        /* ── VALIDACIÓN PRODUCTO ── */
        function prSetError(el, errId, msg) {
            el.classList.remove('input-ok'); el.classList.add('input-error');
            var s = document.getElementById(errId);
            s.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
            s.classList.add('visible');
        }
        function prSetOk(el, errId) {
            el.classList.remove('input-error'); el.classList.add('input-ok');
            document.getElementById(errId).classList.remove('visible');
        }
        function prValidarNombre(el) {
            var v = el.value.trim();
            if (!v) { prSetError(el, 'errNombre', 'El nombre del producto es obligatorio.'); return false; }
            if (v.length < 2) { prSetError(el, 'errNombre', 'El nombre debe tener al menos 2 caracteres.'); return false; }
            if (v.length > 80) { prSetError(el, 'errNombre', 'El nombre no puede superar 80 caracteres.'); return false; }
            if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(v)) { prSetError(el, 'errNombre', 'El nombre solo puede contener letras y espacios, sin números ni símbolos.'); return false; }
            prSetOk(el, 'errNombre'); return true;
        }
        function prContarDesc(el) {
            var len = el.value.length;
            var cnt = document.getElementById('cntDesc');
            cnt.textContent = len + ' / 150';
            cnt.style.color = len >= 130 ? (len >= 150 ? '#ef4444' : '#f59e0b') : '#94a3b8';
        }
        function prValidarNumPos(el, errId, mayor0) {
            var raw = el.value.trim();
            var v   = parseFloat(raw);
            if (raw === '' || isNaN(v)) { prSetError(el, errId, 'Este campo es obligatorio.'); return false; }
            if (v < 0)                  { prSetError(el, errId, 'No puede ser negativo.'); return false; }
            if (mayor0 && v <= 0)       { prSetError(el, errId, 'Debe ser mayor a 0.'); return false; }
            prSetOk(el, errId); return true;
        }
        function prValidarCategoria(el) {
            if (!el.value) { prSetError(el, 'errCategoria', 'Selecciona una categoría.'); return false; }
            prSetOk(el, 'errCategoria'); return true;
        }
        function prValidarDesc(el) {
            if (el.value.length > 150) { prSetError(el, 'errDesc', 'La descripción no puede superar 150 caracteres.'); return false; }
            document.getElementById('errDesc').classList.remove('visible');
            return true;
        }
        function validarFormProducto() {
            var r1 = prValidarNombre(document.getElementById('nombre'));
            var r2 = prValidarDesc(document.getElementById('descripcion'));
            var r3 = prValidarNumPos(document.getElementById('precio'), 'errPrecio', true);
            var r4 = prValidarNumPos(document.getElementById('stock'), 'errStock', false);
            var r5 = prValidarNumPos(document.getElementById('stockMinimo'), 'errStockMin', false);
            var r6 = prValidarCategoria(document.getElementById('idCategoria'));
            return r1 && r2 && r3 && r4 && r5 && r6;
        }

        /* ── NUEVA CATEGORÍA AJAX ── */
        function toggleNuevaCat() {
            var box = document.getElementById('newCatBox');
            box.classList.toggle('visible');
            if (!box.classList.contains('visible')) {
                document.getElementById('newCatNombre').value = '';
                document.getElementById('newCatMsg').textContent = '';
            } else {
                document.getElementById('newCatNombre').focus();
            }
        }
        function crearCategoriaAjax() {
            var nombre = document.getElementById('newCatNombre').value.trim();
            var msg    = document.getElementById('newCatMsg');
            if (!nombre) { msg.style.color = '#dc2626'; msg.textContent = 'Escribe un nombre.'; return; }
            if (nombre.length < 2) { msg.style.color = '#dc2626'; msg.textContent = 'Mínimo 2 caracteres.'; return; }
            msg.style.color = '#3b82f6'; msg.textContent = 'Creando...';

            var xhr = new XMLHttpRequest();
            xhr.open('POST', '<%= ctx %>/CategoriaControlador', true);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        try {
                            var r = JSON.parse(xhr.responseText);
                            if (r.ok) {
                                var sel = document.getElementById('idCategoria');
                                var opt = document.createElement('option');
                                opt.value = r.id;
                                opt.textContent = r.nombre;
                                sel.appendChild(opt);
                                sel.value = r.id;
                                prValidarCategoria(sel);
                                toggleNuevaCat();
                                msg.style.color = '#22c55e'; msg.textContent = '';
                            } else {
                                msg.style.color = '#dc2626'; msg.textContent = r.msg || 'Error al crear.';
                            }
                        } catch(e) { msg.style.color = '#dc2626'; msg.textContent = 'Error inesperado.'; }
                    } else {
                        msg.style.color = '#dc2626'; msg.textContent = 'Error de conexión.';
                    }
                }
            };
            xhr.send('accion=crearAjax&nombre=' + encodeURIComponent(nombre));
        }

        function previewImage(input) {
            const preview = document.getElementById('imgPreview');
            if (input.files && input.files[0]) {
                const reader = new FileReader();
                reader.onload = e => {
                    preview.src = e.target.result;
                    preview.style.display = 'block';
                };
                reader.readAsDataURL(input.files[0]);
                document.querySelector('#uploadBox .upload-text').textContent = input.files[0].name;
            }
        }

        /* ── PERSISTENCIA DEL FORMULARIO EDITAR ── */
        var _idProducto = document.querySelector('input[name="id"]').value;
        var _camposEditar = ['nombre','descripcion','precio','unidadMedida','stock','stockMinimo','fechaVencimiento','idCategoria'];
        var _keyEditar = 'formEditarProducto_' + _idProducto;

        function guardarFormEditar() {
            var datos = {};
            _camposEditar.forEach(function(id) {
                var el = document.getElementById(id);
                if (el) datos[id] = el.value;
            });
            sessionStorage.setItem(_keyEditar, JSON.stringify(datos));
        }
        function restaurarFormEditar() {
            var datos = sessionStorage.getItem(_keyEditar);
            if (!datos) return;
            var obj = JSON.parse(datos);
            _camposEditar.forEach(function(id) {
                var el = document.getElementById(id);
                if (el && obj[id] !== undefined) el.value = obj[id];
            });
            var desc = document.getElementById('descripcion');
            if (desc && desc.value) prContarDesc(desc);
        }
        function limpiarFormEditar() { sessionStorage.removeItem(_keyEditar); }

        _camposEditar.forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.addEventListener('input', guardarFormEditar);
            if (el) el.addEventListener('change', guardarFormEditar);
        });
        restaurarFormEditar();

        var _origValidarEditar = validarFormProducto;
        validarFormProducto = function() {
            var ok = _origValidarEditar();
            if (ok) limpiarFormEditar();
            return ok;
        };
    </script>
</body>
</html>

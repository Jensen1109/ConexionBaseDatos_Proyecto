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

    <aside class="sidebar" id="sidebar">
        <div class="sidebar__brand">
            <div class="sidebar__brand-title">Tienda Don Pedro</div>
            <div class="sidebar__brand-sub">Panel de gestión</div>
        </div>

        <div class="sidebar__section">
            <span class="sidebar__label">Productos</span>
            <a href="<%= ctx %>/ProductoControlador" class="sidebar__link sidebar__link--activo">
                <i class="fas fa-box"></i> Ver productos
            </a>
            <% if (esAdmin) { %>
            <a href="<%= ctx %>/ProductoControlador?accion=nuevo" class="sidebar__link">
                <i class="fas fa-plus-circle"></i> Registrar producto
            </a>
            <% } %>
            <a href="<%= ctx %>/ProductoControlador?accion=stock" class="sidebar__link">
                <i class="fas fa-chart-bar"></i> Control de stock
            </a>
        </div>

        <% if (esAdmin) { %>
        <div class="sidebar__section">
            <span class="sidebar__label">Ventas</span>
            <a href="<%= ctx %>/PedidoControlador?accion=nuevo" class="sidebar__link">
                <i class="fas fa-cart-plus"></i> Registrar venta
            </a>
            <a href="<%= ctx %>/PedidoControlador" class="sidebar__link">
                <i class="fas fa-history"></i> Historial de venta
            </a>
            <a href="<%= ctx %>/ReporteControlador" class="sidebar__link">
                <i class="fas fa-chart-line"></i> Reportes
            </a>
        </div>
        <div class="sidebar__section">
            <span class="sidebar__label">Clientes</span>
            <a href="<%= ctx %>/ClienteControlador" class="sidebar__link">
                <i class="fas fa-users"></i> Ver / Editar clientes
            </a>
            <a href="<%= ctx %>/DeudaControlador" class="sidebar__link">
                <i class="fas fa-file-invoice-dollar"></i> Deudores
            </a>
        </div>
        <% } %>
        <div class="sidebar__section sidebar__section--cuenta">
            <span class="sidebar__label">Mi cuenta</span>
            <div class="sidebar__user-card">
                <div class="sidebar__user-avatar">
                    <%= usuarioActual != null ? String.valueOf(usuarioActual.getNombre().charAt(0)).toUpperCase() + String.valueOf(usuarioActual.getApellido().charAt(0)).toUpperCase() : "?" %>
                </div>
                <div>
                    <div class="sidebar__user-name"><%= usuarioActual != null ? usuarioActual.getNombre() + " " + usuarioActual.getApellido() : "" %></div>
                    <div class="sidebar__user-role"><%= usuarioActual != null && usuarioActual.getIdRol() == 1 ? "Administrador" : "Empleado" %></div>
                </div>
            </div>
            <a href="<%= ctx %>/PerfilControlador" class="sidebar__link">
                <i class="fas fa-user-circle"></i> Mi perfil
            </a>
            <a href="<%= ctx %>/LogoutControlador" class="sidebar__link sidebar__link--logout">
                <i class="fas fa-sign-out-alt"></i> Cerrar sesión
            </a>
        </div>
    </aside>

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
                  method="post" enctype="multipart/form-data">
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
                           value="<%= producto.getNombre() %>" required>
                </div>

                <div class="form__group">
                    <label class="form__label" for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" class="form__textarea"><%= producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label class="form__label" for="precio">Precio ($) *</label>
                        <input type="number" id="precio" name="precio" class="form__input"
                               value="<%= producto.getPrecio() %>" step="0.01" min="0" required>
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
                               value="<%= producto.getStock() %>" min="0" required>
                    </div>
                    <div class="form__group">
                        <label class="form__label" for="stockMinimo">Stock mínimo *</label>
                        <input type="number" id="stockMinimo" name="stockMinimo" class="form__input"
                               value="<%= producto.getStockMinimo() %>" min="0" required>
                    </div>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label class="form__label" for="fechaVencimiento">Fecha de vencimiento</label>
                        <input type="date" id="fechaVencimiento" name="fechaVencimiento" class="form__input"
                               value="<%= producto.getFechaVencimiento() != null ? producto.getFechaVencimiento().toString() : "" %>">
                    </div>
                    <div class="form__group">
                        <label class="form__label" for="idCategoria">Categoría *</label>
                        <select id="idCategoria" name="idCategoria" class="form__select" required>
                            <option value="">-- Seleccione --</option>
                            <% if (categorias != null) {
                                for (Categoria c : categorias) {
                                    boolean sel = c.getIdCategoria() == producto.getIdCategoria();
                            %>
                            <option value="<%= c.getIdCategoria() %>" <%= sel ? "selected" : "" %>><%= c.getNombre() %></option>
                            <% } } %>
                        </select>
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
    </script>
</body>
</html>

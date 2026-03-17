package controladores;

// Importar los DAOs necesarios para acceder a las tablas de BD
import dao.CategoriaDAO;  // Acceso a tabla Categoria
import dao.ImagenDAO;     // Acceso a tabla Imagen
import dao.PermisosDAO;   // Acceso a tabla Permisos y rol_permiso
import dao.ProductoDAO;   // Acceso a tabla Producto
// Importar los modelos
import modelos.Producto;  // Modelo que representa un producto
import modelos.Usuario;   // Modelo que representa un usuario logueado
// Importaciones de Jakarta Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig; // Para recibir archivos (imágenes)
import jakarta.servlet.annotation.WebServlet;       // Para registrar la URL
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;                   // Representa un archivo subido en el formulario
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;   // Para manejar precios con decimales exactos
import java.time.LocalDate;    // Para manejar fechas de vencimiento

/**
 * Controlador CRUD de productos.
 * Maneja: listar catálogo, registrar, editar, eliminar productos y ver control de stock.
 * Solo los administradores pueden crear, editar y eliminar (permiso GESTIONAR_PRODUCTOS).
 * Los empleados solo pueden ver el catálogo y el stock.
 *
 * @MultipartConfig permite recibir archivos subidos (imágenes de productos) hasta 5 MB.
 */
@WebServlet("/ProductoControlador")
@MultipartConfig(maxFileSize = 5242880) // 5 MB máximo por archivo (5 * 1024 * 1024 bytes)
public class ProductoControlador extends HttpServlet {

    // Carpeta donde se guardan las imágenes subidas de productos
    private static final String UPLOADS_DIR = "uploads" + File.separator + "productos";

    // Instancias de los DAOs que se usan en este controlador
    private final ProductoDAO  productoDAO  = new ProductoDAO();   // CRUD de productos
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();  // Listar categorías para el select
    private final ImagenDAO    imagenDAO    = new ImagenDAO();     // Guardar/actualizar URLs de imágenes
    private final PermisosDAO  permisosDAO  = new PermisosDAO();   // Verificar permisos del usuario

    // ═══════════════════════════════════════════════════
    // MÉTODOS DE SEGURIDAD: verificar sesión y permisos
    // ═══════════════════════════════════════════════════

    /**
     * Verifica que el usuario tenga una sesión activa (esté logueado).
     * Si no hay sesión, lo redirige al login.
     * @param req solicitud HTTP
     * @param res respuesta HTTP
     * @return true si hay sesión válida, false si se redirigió al login
     */
    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Aplicar headers anti-caché a la respuesta
        response(res);
        // Buscar sesión existente (false = no crear nueva si no existe)
        HttpSession s = req.getSession(false);
        // Si no hay sesión o no tiene usuario guardado → redirigir al login
        if (s == null || s.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false;
        }
        return true; // Sesión válida
    }

    /**
     * Verifica que el usuario tenga sesión activa Y el permiso específico.
     * Si no tiene permiso, lo redirige a la ruta de fallback.
     * @param req      solicitud HTTP
     * @param res      respuesta HTTP
     * @param permiso  nombre del permiso requerido (ej: "GESTIONAR_PRODUCTOS")
     * @param fallback ruta a la que redirigir si no tiene permiso
     * @return true si tiene sesión y permiso, false si se redirigió
     */
    private boolean verificarPermiso(HttpServletRequest req, HttpServletResponse res,
                                      String permiso, String fallback) throws IOException {
        // Primero verificar que haya sesión
        if (!verificarSesion(req, res)) return false;
        // Obtener el usuario logueado de la sesión
        Usuario u = (Usuario) req.getSession(false).getAttribute("usuarioLogueado");
        // Consultar en la tabla rol_permiso si este rol tiene este permiso
        if (!permisosDAO.tienePermiso(u.getIdRol(), permiso)) {
            // No tiene permiso → redirigir a la página de fallback
            res.sendRedirect(req.getContextPath() + fallback);
            return false;
        }
        return true; // Tiene permiso
    }

    /**
     * Aplica headers anti-caché a la respuesta HTTP.
     * Evita que el navegador guarde páginas protegidas en caché.
     */
    private void response(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
    }

    // ═══════════════════════════════════════════════════
    // GET: Listar productos, ver stock, formulario editar/nuevo
    // ═══════════════════════════════════════════════════

    /**
     * Maneja las peticiones GET según la acción solicitada en la URL.
     * Sin acción: listar catálogo | "nuevo": formulario crear | "editar": formulario editar | "stock": control de stock
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leer el parámetro "accion" de la URL (ej: ?accion=nuevo)
        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            // ─── FORMULARIO NUEVO PRODUCTO ───
            // Verificar que tenga permiso GESTIONAR_PRODUCTOS (solo admin)
            if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
            // Cargar las categorías para el select del formulario
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            // Mostrar el formulario de registro de producto
            request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);

        } else if ("editar".equals(accion)) {
            // ─── FORMULARIO EDITAR PRODUCTO ───
            if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
            // Obtener el ID del producto a editar desde la URL
            int id = Integer.parseInt(request.getParameter("id"));
            // Buscar el producto en la BD para prellenar el formulario
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null) {
                // Si el producto no existe, redirigir a la lista
                response.sendRedirect(request.getContextPath() + "/ProductoControlador");
                return;
            }
            // Pasar el producto y categorías al JSP para prellenar los campos
            request.setAttribute("producto",   producto);
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            // Mostrar el formulario de edición
            request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);

        } else if ("stock".equals(accion)) {
            // ─── CONTROL DE STOCK ───
            // Verificar permiso VER_STOCK (admin y empleados con ese permiso)
            if (!verificarPermiso(request, response, "VER_STOCK", "/ProductoControlador")) return;
            // Cargar todos los productos con su stock actual
            request.setAttribute("productos", productoDAO.listarTodos());
            // Mostrar la vista de control de stock
            request.getRequestDispatcher("/view/controlstock.jsp").forward(request, response);

        } else {
            // ─── CATÁLOGO (sin acción) ───
            // Cualquier usuario autenticado puede ver el catálogo
            if (!verificarSesion(request, response)) return;
            // Cargar todos los productos
            request.setAttribute("productos", productoDAO.listarTodos());
            // Mostrar las tarjetas de productos
            request.getRequestDispatcher("/view/productos.jsp").forward(request, response);
        }
    }

    // ═══════════════════════════════════════════════════
    // POST: Crear, actualizar o eliminar producto
    // ═══════════════════════════════════════════════════

    /**
     * Maneja las peticiones POST: crear nuevo producto, actualizar existente o eliminar.
     * Valida precio > 0, stock >= 0, nombre único y manejo de imagen.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que tenga permiso para gestionar productos (solo admin)
        if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
        // Establecer codificación UTF-8 para leer caracteres especiales (tildes, ñ)
        request.setCharacterEncoding("UTF-8");

        // Leer qué acción se está ejecutando: "eliminar", "actualizar" o crear (sin acción)
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            // ─── ELIMINAR PRODUCTO ───
            int id = Integer.parseInt(request.getParameter("id"));
            // Intentar eliminar (primero borra detalles de pedido asociados)
            if (!productoDAO.eliminar(id)) {
                // Si no se pudo eliminar, mostrar error
                request.setAttribute("productos", productoDAO.listarTodos());
                request.setAttribute("error", "No se pudo eliminar el producto. Intenta de nuevo.");
                request.getRequestDispatcher("/view/productos.jsp").forward(request, response);
                return;
            }

        } else if ("actualizar".equals(accion)) {
            // ─── ACTUALIZAR PRODUCTO EXISTENTE ───
            int id = Integer.parseInt(request.getParameter("id"));

            // Validar que el precio sea mayor a 0 y el stock no sea negativo
            BigDecimal precio = parseBigDecimal(request.getParameter("precio"));
            int stock         = parseInt(request.getParameter("stock"));
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 ||
                stock < 0) {
                // Precio o stock inválido → mostrar error con datos actuales del producto
                Producto existente = productoDAO.buscarPorId(id);
                request.setAttribute("producto",   existente);
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "El precio debe ser mayor a 0 y el stock no puede ser negativo.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

            // Validar que el nombre no esté duplicado (excluyendo el producto actual)
            String nombre = request.getParameter("nombre");
            if (productoDAO.nombreExisteExcluyendo(nombre, id)) {
                Producto existente = productoDAO.buscarPorId(id);
                request.setAttribute("producto",   existente);
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "Ya existe otro producto con ese nombre.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

            // Obtener el producto actual de la BD para comparar la imagen
            Producto existente = productoDAO.buscarPorId(id);
            // Construir un objeto Producto con los campos del formulario
            Producto p = construirCampos(request);
            p.setIdProducto(id); // Asignar el ID del producto que estamos editando

            // Verificar si se subió una imagen nueva
            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                // Sí se subió imagen nueva
                if (existente != null && existente.getIdImagen() > 0) {
                    // El producto ya tenía imagen → actualizar la URL en la tabla Imagen
                    imagenDAO.actualizarUrl(existente.getIdImagen(), nuevoArchivo);
                    p.setIdImagen(existente.getIdImagen()); // Mantener el mismo ID de imagen
                } else {
                    // El producto no tenía imagen → insertar nueva en tabla Imagen
                    int newImgId = imagenDAO.insertar(nuevoArchivo);
                    p.setIdImagen(newImgId); // Asignar el nuevo ID de imagen
                }
            } else {
                // No se subió imagen nueva → mantener la imagen actual
                p.setIdImagen(existente != null ? existente.getIdImagen() : 0);
            }

            // Ejecutar el UPDATE en la BD
            if (!productoDAO.actualizar(p)) {
                request.setAttribute("producto",   productoDAO.buscarPorId(id));
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "No se pudo actualizar el producto. Intenta de nuevo.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

        } else {
            // ─── CREAR PRODUCTO NUEVO ───

            // Validar precio > 0 y stock >= 0
            BigDecimal precio = parseBigDecimal(request.getParameter("precio"));
            int stock         = parseInt(request.getParameter("stock"));
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "El precio debe ser mayor a 0 y el stock no puede ser negativo.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }

            // Validar que no exista otro producto con el mismo nombre
            String nombre = request.getParameter("nombre");
            if (productoDAO.nombreExiste(nombre)) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "Ya existe un producto con ese nombre.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }

            // Construir el objeto Producto con los datos del formulario
            Producto p = construirCampos(request);

            // Si se subió una imagen, guardarla en disco y en la tabla Imagen
            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                int imgId = imagenDAO.insertar(nuevoArchivo); // Insertar URL en tabla Imagen
                p.setIdImagen(imgId); // Vincular la imagen al producto
            }

            // Ejecutar el INSERT en la BD
            if (!productoDAO.crear(p)) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "No se pudo guardar el producto. Verifica que la categoría sea válida e intenta de nuevo.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }
        }

        // Si todo salió bien, redirigir a la lista de productos
        response.sendRedirect(request.getContextPath() + "/ProductoControlador");
    }

    // ═══════════════════════════════════════════════════
    // MÉTODOS AUXILIARES (Helpers)
    // ═══════════════════════════════════════════════════

    /**
     * Lee los campos del formulario HTTP y los pone en un objeto Producto.
     * @param request solicitud HTTP con los parámetros del formulario
     * @return objeto Producto con los datos del formulario
     */
    private Producto construirCampos(HttpServletRequest request) {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));             // Nombre del producto
        p.setDescripcion(request.getParameter("descripcion"));   // Descripción del producto
        p.setPrecio(parseBigDecimal(request.getParameter("precio")));       // Precio
        p.setStock(parseInt(request.getParameter("stock")));                // Stock actual
        p.setStockMinimo(parseInt(request.getParameter("stockMinimo")));    // Stock mínimo para alerta
        p.setUnidadMedida(request.getParameter("unidadMedida"));            // kg, unidad, litro, etc.
        p.setIdCategoria(parseInt(request.getParameter("idCategoria")));    // FK a tabla Categoria
        // Fecha de vencimiento (opcional)
        String fecha = request.getParameter("fechaVencimiento");
        if (fecha != null && !fecha.isBlank()) {
            p.setFechaVencimiento(LocalDate.parse(fecha)); // Convertir String a LocalDate
        }
        return p;
    }

    /**
     * Guarda el archivo de imagen subido en el servidor si se envió uno.
     * Lo guarda en la carpeta desplegada (target) Y en src/main/webapp para que sobreviva un Clean and Build.
     * @param request solicitud HTTP con el archivo en la parte "imagen"
     * @return nombre único del archivo guardado, o null si no se subió imagen
     */
    private String guardarArchivoSiExiste(HttpServletRequest request)
            throws IOException, ServletException {
        // Obtener el archivo subido del formulario (campo name="imagen")
        Part filePart = request.getPart("imagen");
        // Extraer el nombre del archivo
        String fileName = getFileName(filePart);
        // Si no se subió archivo, retornar null
        if (fileName == null || fileName.isBlank()) return null;

        // Obtener la ruta real en disco donde se despliega la app (dentro de target)
        String deployedPath = getServletContext().getRealPath("") + File.separator + UPLOADS_DIR;
        File deployedDir = new File(deployedPath);
        // Crear la carpeta si no existe
        if (!deployedDir.exists()) deployedDir.mkdirs();

        // Generar nombre único: timestamp + nombre original (evitar colisiones)
        // replaceAll limpia caracteres especiales del nombre
        String uniqueName = System.currentTimeMillis() + "_" +
                            fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Guardar el archivo en disco
        filePart.write(deployedPath + File.separator + uniqueName);

        // Copiar también a src/main/webapp para que no se pierda al hacer Clean and Build
        try {
            String deployedRoot = getServletContext().getRealPath("").replace("\\", "/");
            String[] parts = deployedRoot.split("/target/");
            if (parts.length >= 2) {
                String srcPath = parts[0] + "/src/main/webapp/" + UPLOADS_DIR;
                File srcDir = new File(srcPath);
                if (srcDir.exists() || srcDir.mkdirs()) {
                    // Copiar el archivo de la carpeta desplegada a la carpeta fuente
                    Files.copy(
                        Paths.get(deployedPath, uniqueName),
                        Paths.get(srcPath, uniqueName),
                        StandardCopyOption.REPLACE_EXISTING // Si ya existe, reemplazar
                    );
                }
            }
        } catch (Exception e) {
            // Si falla la copia a src, solo avisar (no es crítico)
            System.err.println("Aviso: no se pudo copiar imagen a src: " + e.getMessage());
        }

        // Retornar el nombre único del archivo para guardarlo en la BD
        return uniqueName;
    }

    /**
     * Extrae el nombre del archivo original del header content-disposition de un Part.
     * Los navegadores envían el nombre del archivo dentro de este header.
     * @param part parte multipart del formulario
     * @return nombre del archivo, o null si no se encontró
     */
    private String getFileName(Part part) {
        if (part == null) return null;
        // Leer el header que contiene info del archivo: "form-data; name="imagen"; filename="foto.jpg""
        String header = part.getHeader("content-disposition");
        if (header == null) return null;
        // Buscar el token "filename" dentro del header
        for (String token : header.split(";")) {
            if (token.trim().startsWith("filename")) {
                // Extraer solo el nombre del archivo (quitar comillas y ruta)
                String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                return new File(name).getName(); // getName() quita la ruta, deja solo el nombre
            }
        }
        return null;
    }

    /**
     * Convierte un String a BigDecimal de forma segura (sin lanzar excepción).
     * Se usa para parsear precios que vienen como texto desde el formulario.
     * @param value cadena a convertir (ej: "10000.50")
     * @return BigDecimal parseado, o null si el valor es nulo, vacío o no es un número
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null; // No es un número válido
        }
    }

    /**
     * Convierte un String a int de forma segura (sin lanzar excepción).
     * Se usa para parsear cantidades que vienen como texto desde el formulario.
     * @param value cadena a convertir (ej: "50")
     * @return entero parseado, o 0 si el valor es nulo, vacío o no es un número
     */
    private int parseInt(String value) {
        try {
            if (value == null || value.isBlank()) return 0;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0; // No es un número válido, retornar 0 por defecto
        }
    }
}

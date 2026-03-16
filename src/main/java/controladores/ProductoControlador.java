package controladores;

import dao.CategoriaDAO;
import dao.ImagenDAO;
import dao.PermisosDAO;
import dao.ProductoDAO;
import modelos.Producto;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controlador CRUD de productos.
 * Solo los administradores pueden crear, editar y eliminar productos.
 * Los empleados solo pueden visualizar catálogo y stock.
 */
@WebServlet("/ProductoControlador")
@MultipartConfig(maxFileSize = 5242880) // 5 MB
public class ProductoControlador extends HttpServlet {

    private static final String UPLOADS_DIR = "uploads" + File.separator + "productos";

    private final ProductoDAO  productoDAO  = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ImagenDAO    imagenDAO    = new ImagenDAO();
    private final PermisosDAO  permisosDAO  = new PermisosDAO();

    // ─────────────────────────────────────────────
    // Verificaciones de sesión / rol
    // ─────────────────────────────────────────────
    /**
     * Verifica que exista una sesión activa; redirige al login si no hay.
     * @param req solicitud HTTP
     * @param res respuesta HTTP
     * @return true si la sesión es válida, false si se redirigió al login
     */
    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        response(res);
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false;
        }
        return true;
    }

    /**
     * Verifica sesión activa y que el usuario tenga el permiso indicado.
     * @param req      solicitud HTTP
     * @param res      respuesta HTTP
     * @param permiso  nombre del permiso requerido (ej: "GESTIONAR_PRODUCTOS")
     * @param fallback ruta a la que redirigir si no tiene el permiso
     * @return true si tiene sesión y permiso, false si se redirigió
     */
    private boolean verificarPermiso(HttpServletRequest req, HttpServletResponse res,
                                      String permiso, String fallback) throws IOException {
        if (!verificarSesion(req, res)) return false;
        Usuario u = (Usuario) req.getSession(false).getAttribute("usuarioLogueado");
        if (!permisosDAO.tienePermiso(u.getIdRol(), permiso)) {
            res.sendRedirect(req.getContextPath() + fallback);
            return false;
        }
        return true;
    }

    private void response(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
    }

    // ─────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);

        } else if ("editar".equals(accion)) {
            if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
            int id = Integer.parseInt(request.getParameter("id"));
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null) {
                response.sendRedirect(request.getContextPath() + "/ProductoControlador");
                return;
            }
            request.setAttribute("producto",   producto);
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);

        } else if ("stock".equals(accion)) {
            if (!verificarPermiso(request, response, "VER_STOCK", "/ProductoControlador")) return;
            request.setAttribute("productos", productoDAO.listarTodos());
            request.getRequestDispatcher("/view/controlstock.jsp").forward(request, response);

        } else {
            // Catálogo: cualquier usuario autenticado puede verlo
            if (!verificarSesion(request, response)) return;
            request.setAttribute("productos", productoDAO.listarTodos());
            request.getRequestDispatcher("/view/productos.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────
    // POST
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarPermiso(request, response, "GESTIONAR_PRODUCTOS", "/ProductoControlador")) return;
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            productoDAO.eliminar(id);

        } else if ("actualizar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));

            // Validar precio y stock > 0
            BigDecimal precio = parseBigDecimal(request.getParameter("precio"));
            int stock         = parseInt(request.getParameter("stock"));
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 ||
                stock < 0) {
                Producto existente = productoDAO.buscarPorId(id);
                request.setAttribute("producto",   existente);
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "El precio debe ser mayor a 0 y el stock no puede ser negativo.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

            // Validar nombre único (excluyendo el producto actual)
            String nombre = request.getParameter("nombre");
            if (productoDAO.nombreExisteExcluyendo(nombre, id)) {
                Producto existente = productoDAO.buscarPorId(id);
                request.setAttribute("producto",   existente);
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "Ya existe otro producto con ese nombre.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

            Producto existente = productoDAO.buscarPorId(id);
            Producto p = construirCampos(request);
            p.setIdProducto(id);

            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                if (existente != null && existente.getIdImagen() > 0) {
                    imagenDAO.actualizarUrl(existente.getIdImagen(), nuevoArchivo);
                    p.setIdImagen(existente.getIdImagen());
                } else {
                    int newImgId = imagenDAO.insertar(nuevoArchivo);
                    p.setIdImagen(newImgId);
                }
            } else {
                p.setIdImagen(existente != null ? existente.getIdImagen() : 0);
            }

            if (!productoDAO.actualizar(p)) {
                request.setAttribute("producto",   productoDAO.buscarPorId(id));
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "No se pudo actualizar el producto. Intenta de nuevo.");
                request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);
                return;
            }

        } else {
            // insertar
            // Validar precio > 0 y stock >= 0
            BigDecimal precio = parseBigDecimal(request.getParameter("precio"));
            int stock         = parseInt(request.getParameter("stock"));
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "El precio debe ser mayor a 0 y el stock no puede ser negativo.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }

            // Validar nombre único
            String nombre = request.getParameter("nombre");
            if (productoDAO.nombreExiste(nombre)) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "Ya existe un producto con ese nombre.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }

            Producto p = construirCampos(request);
            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                int imgId = imagenDAO.insertar(nuevoArchivo);
                p.setIdImagen(imgId);
            }

            if (!productoDAO.crear(p)) {
                request.setAttribute("categorias", categoriaDAO.listarTodas());
                request.setAttribute("error", "No se pudo guardar el producto. Verifica que la categoría sea válida e intenta de nuevo.");
                request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/ProductoControlador");
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    /**
     * Construye un objeto Producto con los campos del formulario HTTP.
     * @param request solicitud HTTP con los parámetros del formulario
     * @return objeto Producto con los datos ingresados
     */
    private Producto construirCampos(HttpServletRequest request) {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setPrecio(parseBigDecimal(request.getParameter("precio")));
        p.setStock(parseInt(request.getParameter("stock")));
        p.setStockMinimo(parseInt(request.getParameter("stockMinimo")));
        p.setUnidadMedida(request.getParameter("unidadMedida"));
        p.setIdCategoria(parseInt(request.getParameter("idCategoria")));
        String fecha = request.getParameter("fechaVencimiento");
        if (fecha != null && !fecha.isBlank()) {
            p.setFechaVencimiento(LocalDate.parse(fecha));
        }
        return p;
    }

    /**
     * Guarda el archivo de imagen subido en el directorio de uploads si existe.
     * También copia el archivo a src/main/webapp para que sobreviva Clean and Build.
     * @param request solicitud HTTP con la parte multipart "imagen"
     * @return nombre único del archivo guardado, o null si no se subió ninguno
     */
    private String guardarArchivoSiExiste(HttpServletRequest request)
            throws IOException, ServletException {
        Part filePart = request.getPart("imagen");
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isBlank()) return null;

        // Ruta desplegada (sirve inmediatamente)
        String deployedPath = getServletContext().getRealPath("") + File.separator + UPLOADS_DIR;
        File deployedDir = new File(deployedPath);
        if (!deployedDir.exists()) deployedDir.mkdirs();

        String uniqueName = System.currentTimeMillis() + "_" +
                            fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        filePart.write(deployedPath + File.separator + uniqueName);

        // Copia a src/main/webapp para que sobreviva "Clean and Build"
        try {
            String deployedRoot = getServletContext().getRealPath("").replace("\\", "/");
            String[] parts = deployedRoot.split("/target/");
            if (parts.length >= 2) {
                String srcPath = parts[0] + "/src/main/webapp/" + UPLOADS_DIR;
                File srcDir = new File(srcPath);
                if (srcDir.exists() || srcDir.mkdirs()) {
                    Files.copy(
                        Paths.get(deployedPath, uniqueName),
                        Paths.get(srcPath, uniqueName),
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: no se pudo copiar imagen a src: " + e.getMessage());
        }

        return uniqueName;
    }

    /**
     * Extrae el nombre del archivo del header content-disposition de una Part.
     * @param part parte multipart del formulario
     * @return nombre del archivo, o null si no se encontró
     */
    private String getFileName(Part part) {
        if (part == null) return null;
        String header = part.getHeader("content-disposition");
        if (header == null) return null;
        for (String token : header.split(";")) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                return new File(name).getName();
            }
        }
        return null;
    }

    /**
     * Convierte un String a BigDecimal de forma segura.
     * @param value cadena a convertir
     * @return BigDecimal parseado, o null si el valor es nulo, vacío o inválido
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte un String a int de forma segura.
     * @param value cadena a convertir
     * @return entero parseado, o 0 si el valor es nulo, vacío o inválido
     */
    private int parseInt(String value) {
        try {
            if (value == null || value.isBlank()) return 0;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

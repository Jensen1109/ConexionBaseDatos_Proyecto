package controladores;

import dao.CategoriaDAO;
import dao.ImagenDAO;
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
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/ProductoControlador")
@MultipartConfig(maxFileSize = 5242880) // 5 MB
public class ProductoControlador extends HttpServlet {

    private static final String UPLOADS_DIR = "uploads" + File.separator + "productos";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ImagenDAO imagenDAO = new ImagenDAO();

    // ─────────────────────────────────────────────
    // Verificaciones de sesión / rol
    // ─────────────────────────────────────────────
    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false;
        }
        return true;
    }

    private boolean verificarAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!verificarSesion(req, res)) return false;
        Usuario u = (Usuario) req.getSession(false).getAttribute("usuarioLogueado");
        if (u.getIdRol() != 1) {
            res.sendRedirect(req.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            if (!verificarAdmin(request, response)) return;
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);

        } else if ("editar".equals(accion)) {
            if (!verificarAdmin(request, response)) return;
            int id = Integer.parseInt(request.getParameter("id"));
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null) {
                response.sendRedirect(request.getContextPath() + "/ProductoControlador");
                return;
            }
            request.setAttribute("producto", producto);
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            request.getRequestDispatcher("/view/editarproducto.jsp").forward(request, response);

        } else if ("stock".equals(accion)) {
            if (!verificarSesion(request, response)) return;
            request.setAttribute("productos", productoDAO.listarTodos());
            request.getRequestDispatcher("/view/controlstock.jsp").forward(request, response);

        } else {
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

        if (!verificarAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            productoDAO.eliminar(id);

        } else if ("actualizar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));

            // Cargar producto existente para preservar id_imagen si no se sube nueva
            Producto existente = productoDAO.buscarPorId(id);
            Producto p = construirCampos(request);
            p.setIdProducto(id);

            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                if (existente != null && existente.getIdImagen() > 0) {
                    // Actualizar registro Imagen existente
                    imagenDAO.actualizarUrl(existente.getIdImagen(), nuevoArchivo);
                    p.setIdImagen(existente.getIdImagen());
                } else {
                    // Crear nuevo registro Imagen
                    int newImgId = imagenDAO.insertar(nuevoArchivo);
                    p.setIdImagen(newImgId);
                }
            } else {
                // Sin imagen nueva → preservar la existente
                p.setIdImagen(existente != null ? existente.getIdImagen() : 0);
            }

            productoDAO.actualizar(p);

        } else {
            // insertar
            Producto p = construirCampos(request);

            String nuevoArchivo = guardarArchivoSiExiste(request);
            if (nuevoArchivo != null) {
                int imgId = imagenDAO.insertar(nuevoArchivo);
                p.setIdImagen(imgId);
            }

            productoDAO.crear(p);
        }

        response.sendRedirect(request.getContextPath() + "/ProductoControlador");
    }

    // ─────────────────────────────────────────────
    // Construir Producto solo con los campos del formulario (sin imagen)
    // ─────────────────────────────────────────────
    private Producto construirCampos(HttpServletRequest request) {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setPrecio(new BigDecimal(request.getParameter("precio")));
        p.setStock(Integer.parseInt(request.getParameter("stock")));
        p.setStockMinimo(Integer.parseInt(request.getParameter("stockMinimo")));
        p.setUnidadMedida(request.getParameter("unidadMedida"));
        p.setIdCategoria(Integer.parseInt(request.getParameter("idCategoria")));

        String fecha = request.getParameter("fechaVencimiento");
        if (fecha != null && !fecha.isBlank()) {
            p.setFechaVencimiento(LocalDate.parse(fecha));
        }
        return p;
    }

    // ─────────────────────────────────────────────
    // Guardar archivo de imagen al disco si se subió uno
    // Retorna el nombre único del archivo, o null si no se subió nada
    // ─────────────────────────────────────────────
    private String guardarArchivoSiExiste(HttpServletRequest request)
            throws IOException, ServletException {

        Part filePart = request.getPart("imagen");
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isBlank()) return null;

        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOADS_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String uniqueName = System.currentTimeMillis() + "_" +
                            fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        filePart.write(uploadPath + File.separator + uniqueName);
        return uniqueName;
    }

    // Extrae el nombre de archivo del header Content-Disposition
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
}

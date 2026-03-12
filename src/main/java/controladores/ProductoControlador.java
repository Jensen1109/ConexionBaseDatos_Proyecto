package controladores;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import modelos.Producto;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/ProductoControlador")
public class ProductoControlador extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // ─────────────────────────────────────────────
    // Verifica que hay sesión activa (cualquier rol)
    // Si no hay sesión → /LoginControlador
    // ─────────────────────────────────────────────
    private boolean verificarSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // Verifica que el usuario es administrador (id_rol=1)
    // Si no tiene sesión     → /LoginControlador
    // Si tiene sesión pero NO es admin → /ProductoControlador (lista, sin loop)
    // ─────────────────────────────────────────────
    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!verificarSesion(request, response)) return false;

        Usuario usuario = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
        if (usuario.getIdRol() != 1) {
            // Está logueado pero no es admin: mostrar la lista, no redirigir al login
            request.getSession(false).setAttribute("errorAcceso", "Acción reservada para administradores.");
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // GET: listar / nuevo / editar
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            // Solo admin puede registrar productos
            if (!verificarAdmin(request, response)) return;
            request.setAttribute("categorias", categoriaDAO.listarTodas());
            request.getRequestDispatcher("/view/registroProducto.jsp").forward(request, response);

        } else if ("editar".equals(accion)) {
            // Solo admin puede editar productos
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
            // Control de stock: cualquier usuario logueado puede verlo
            if (!verificarSesion(request, response)) return;
            request.setAttribute("productos", productoDAO.listarTodos());
            request.getRequestDispatcher("/view/controlstock.jsp").forward(request, response);

        } else {
            // Listar productos: cualquier usuario logueado puede verlos
            if (!verificarSesion(request, response)) return;
            request.setAttribute("productos", productoDAO.listarTodos());
            request.getRequestDispatcher("/view/productos.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────
    // POST: insertar / actualizar / eliminar
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Todas las operaciones de escritura requieren admin
        if (!verificarAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            productoDAO.eliminar(id);

        } else if ("actualizar".equals(accion)) {
            Producto p = construirProducto(request);
            p.setIdProducto(Integer.parseInt(request.getParameter("id")));
            productoDAO.actualizar(p);

        } else {
            // insertar
            productoDAO.crear(construirProducto(request));
        }

        response.sendRedirect(request.getContextPath() + "/ProductoControlador");
    }

    // ─────────────────────────────────────────────
    // Construir Producto desde los parámetros del form
    // ─────────────────────────────────────────────
    private Producto construirProducto(HttpServletRequest request) {
        Producto p = new Producto();
        p.setNombre(request.getParameter("nombre"));
        p.setDescripcion(request.getParameter("descripcion"));
        p.setPrecio(new BigDecimal(request.getParameter("precio")));
        p.setStock(Integer.parseInt(request.getParameter("stock")));
        p.setStockMinimo(Integer.parseInt(request.getParameter("stockMinimo")));
        p.setUnidadMedida(request.getParameter("unidadMedida"));
        p.setIdCategoria(Integer.parseInt(request.getParameter("idCategoria")));
        p.setIdImagen(0);

        String fecha = request.getParameter("fechaVencimiento");
        if (fecha != null && !fecha.isBlank()) {
            p.setFechaVencimiento(LocalDate.parse(fecha));
        }
        return p;
    }
}

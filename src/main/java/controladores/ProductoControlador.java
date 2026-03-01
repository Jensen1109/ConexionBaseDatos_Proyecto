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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        request.setAttribute("productos", productoDAO.listarTodos());
        request.setAttribute("categorias", categoriaDAO.listarTodas());
        request.getRequestDispatcher("/WEB-INF/view/productos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            productoDAO.eliminar(id);
        } else {
            Producto p = new Producto();
            p.setNombre(request.getParameter("nombre"));
            p.setDescripcion(request.getParameter("descripcion"));
            p.setPrecio(new BigDecimal(request.getParameter("precio")));
            p.setStock(Integer.parseInt(request.getParameter("stock")));
            p.setStockMinimo(Integer.parseInt(request.getParameter("stockMinimo")));
            p.setUnidadMedida(request.getParameter("unidadMedida"));
            p.setIdCategoria(Integer.parseInt(request.getParameter("idCategoria")));
            p.setIdImagen(0);

            String fechaStr = request.getParameter("fechaVencimiento");
            if (fechaStr != null && !fechaStr.isBlank()) {
                p.setFechaVencimiento(LocalDate.parse(fechaStr));
            }

            productoDAO.crear(p);
        }

        response.sendRedirect(request.getContextPath() + "/ProductoControlador");
    }
}

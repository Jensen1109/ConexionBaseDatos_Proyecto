package controladores;

import dao.CategoriaDAO;
import modelos.Categoria;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/CategoriaControlador")
public class CategoriaControlador extends HttpServlet {

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

        request.setAttribute("categorias", categoriaDAO.listarTodas());
        request.getRequestDispatcher("/WEB-INF/view/categorias.jsp").forward(request, response);
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
            categoriaDAO.eliminar(id);
        } else {
            String nombre = request.getParameter("nombre");
            if (nombre != null && !nombre.isBlank()) {
                Categoria c = new Categoria();
                c.setNombre(nombre.trim());
                categoriaDAO.crear(c);
            }
        }

        response.sendRedirect(request.getContextPath() + "/CategoriaControlador");
    }
}

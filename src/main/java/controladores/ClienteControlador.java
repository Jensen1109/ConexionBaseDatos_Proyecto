package controladores;

import dao.UsuarioDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/ClienteControlador")
public class ClienteControlador extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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

        request.setAttribute("clientes", usuarioDAO.listarClientes());
        request.getRequestDispatcher("/WEB-INF/view/clientes.jsp").forward(request, response);
    }
}

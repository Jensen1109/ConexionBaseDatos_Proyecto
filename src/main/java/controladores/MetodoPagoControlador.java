package controladores;

import dao.MetodoPagoDAO;
import modelos.MetodoPago;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/MetodoPagoControlador")
public class MetodoPagoControlador extends HttpServlet {

    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();

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

        request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
        request.getRequestDispatcher("/WEB-INF/view/metodos_pago.jsp").forward(request, response);
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
            metodoPagoDAO.eliminar(id);
        } else {
            String nombre = request.getParameter("nombre");
            if (nombre != null && !nombre.isBlank()) {
                MetodoPago mp = new MetodoPago();
                mp.setNombre(nombre.trim());
                metodoPagoDAO.crear(mp);
            }
        }

        response.sendRedirect(request.getContextPath() + "/MetodoPagoControlador");
    }
}

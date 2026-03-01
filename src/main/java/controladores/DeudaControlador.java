package controladores;

import dao.DeudaDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/DeudaControlador")
public class DeudaControlador extends HttpServlet {

    private final DeudaDAO deudaDAO = new DeudaDAO();

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

        request.setAttribute("deudas", deudaDAO.listarPendientes());
        request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
        request.getRequestDispatcher("/WEB-INF/view/deudas.jsp").forward(request, response);
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

        int idDeuda = Integer.parseInt(request.getParameter("idDeuda"));
        BigDecimal monto = new BigDecimal(request.getParameter("monto"));
        deudaDAO.registrarAbono(idDeuda, monto);

        response.sendRedirect(request.getContextPath() + "/DeudaControlador");
    }
}

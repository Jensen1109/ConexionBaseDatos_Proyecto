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

    // ─────────────────────────────────────────────
    // Solo admin puede gestionar deudas
    // ─────────────────────────────────────────────
    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return false;
        }
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // GET: listar deudas pendientes con nombre de cliente
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setAttribute("deudas",        deudaDAO.listarPendientesConCliente());
        request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
        request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────
    // POST: registrar abono a una deuda
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");

        int idDeuda = Integer.parseInt(request.getParameter("idDeuda"));
        BigDecimal monto = new BigDecimal(request.getParameter("monto"));
        deudaDAO.registrarAbono(idDeuda, monto);

        response.sendRedirect(request.getContextPath() + "/DeudaControlador");
    }
}

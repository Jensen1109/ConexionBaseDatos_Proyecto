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

/**
 * Controlador de deudas y abonos.
 * Solo administradores pueden gestionar deudas.
 */
@WebServlet("/DeudaControlador")
public class DeudaControlador extends HttpServlet {

    private final DeudaDAO deudaDAO = new DeudaDAO();

    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        // Default: listar deudas activas con nombre del cliente
        request.setAttribute("deudas",        deudaDAO.listarActivasConCliente());
        request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
        request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        // Registrar abono a deuda existente
        String idDeudaStr = request.getParameter("idDeuda");
        String montoStr   = request.getParameter("monto");

        if (idDeudaStr == null || montoStr == null) {
            response.sendRedirect(request.getContextPath() + "/DeudaControlador");
            return;
        }

        int idDeuda        = Integer.parseInt(idDeudaStr);
        BigDecimal monto   = new BigDecimal(montoStr);

        boolean ok = deudaDAO.abonar(idDeuda, monto);
        if (!ok) {
            request.setAttribute("deudas",        deudaDAO.listarActivasConCliente());
            request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
            request.setAttribute("error", "El abono supera el monto pendiente.");
            request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/DeudaControlador");
    }
}

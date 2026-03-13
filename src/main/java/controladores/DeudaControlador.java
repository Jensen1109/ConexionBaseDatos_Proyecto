package controladores;

import dao.DeudaDAO;
import dao.UsuarioDAO;
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
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
    // GET: listar deudas pendientes o mostrar formulario de nueva deuda
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        String accion = request.getParameter("accion");

        if ("nuevoFormulario".equals(accion)) {
            request.setAttribute("clientes", usuarioDAO.listarClientes());
            request.getRequestDispatcher("/view/registrarDeuda.jsp").forward(request, response);
            return;
        }

        // Default: listar deudas
        request.setAttribute("deudas",        deudaDAO.listarPendientesConCliente());
        request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
        request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────
    // POST: registrar abono o registrar deuda nueva
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("registrar".equals(accion)) {
            int idCliente = Integer.parseInt(request.getParameter("idCliente"));
            BigDecimal monto = new BigDecimal(request.getParameter("montoPendiente"));
            deudaDAO.registrarDeudaDirecta(idCliente, monto);
            response.sendRedirect(request.getContextPath() + "/DeudaControlador");
            return;
        }

        // Default: registrar abono a deuda existente
        int idDeuda = Integer.parseInt(request.getParameter("idDeuda"));
        BigDecimal monto = new BigDecimal(request.getParameter("monto"));
        deudaDAO.registrarAbono(idDeuda, monto);

        response.sendRedirect(request.getContextPath() + "/DeudaControlador");
    }
}

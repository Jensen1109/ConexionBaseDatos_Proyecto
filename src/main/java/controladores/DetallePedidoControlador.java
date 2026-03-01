package controladores;

import dao.PedidoDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/DetallePedidoControlador")
public class DetallePedidoControlador extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

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

        String idParam = request.getParameter("idPedido");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/PedidoControlador");
            return;
        }

        int idPedido = Integer.parseInt(idParam);
        request.setAttribute("detalles",  pedidoDAO.listarDetalles(idPedido));
        request.setAttribute("idPedido",  idPedido);
        request.getRequestDispatcher("/WEB-INF/view/detalle_pedido.jsp").forward(request, response);
    }
}

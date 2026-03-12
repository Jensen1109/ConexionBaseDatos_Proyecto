package controladores;

import dao.ReporteDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/ReporteControlador")
public class ReporteControlador extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

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
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        request.setAttribute("totalVentasMes",      reporteDAO.totalVentasMes());
        request.setAttribute("productosStockBajo",  reporteDAO.productosStockBajo());
        request.setAttribute("totalDeudasPendientes", reporteDAO.totalDeudasPendientes());
        request.setAttribute("totalClientes",       reporteDAO.contarClientes());

        // CORREGIDO: /view/ (antes apuntaba a /WEB-INF/view/ que no existe)
        request.getRequestDispatcher("/view/reportes.jsp").forward(request, response);
    }
}

package controladores;

import dao.TelefonoDAO;
import modelos.Telefono;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/TelefonoControlador")
public class TelefonoControlador extends HttpServlet {

    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

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

        String idParam = request.getParameter("idUsuario");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/ClienteControlador");
            return;
        }

        int idUsuario = Integer.parseInt(idParam);
        request.setAttribute("telefonos", telefonoDAO.listarPorUsuario(idUsuario));
        request.setAttribute("idUsuario", idUsuario);
        request.getRequestDispatcher("/WEB-INF/view/telefonos.jsp").forward(request, response);
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

        int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
        String accion  = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idTelefono = Integer.parseInt(request.getParameter("idTelefono"));
            telefonoDAO.eliminar(idTelefono);
        } else {
            String numero = request.getParameter("telefono");
            if (numero != null && !numero.isBlank()) {
                Telefono t = new Telefono();
                t.setTelefono(numero.trim());
                t.setUsuarioId(idUsuario);
                telefonoDAO.agregar(t);
            }
        }

        response.sendRedirect(request.getContextPath() +
                "/TelefonoControlador?idUsuario=" + idUsuario);
    }
}

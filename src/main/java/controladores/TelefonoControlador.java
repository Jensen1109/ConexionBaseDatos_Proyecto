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

/**
 * Controlador para gestionar los teléfonos adicionales de un cliente.
 * Permite listar, agregar y eliminar teléfonos asociados a un cliente.
 * Solo accesible por administradores.
 */
@WebServlet("/TelefonoControlador")
public class TelefonoControlador extends HttpServlet {

    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

    /**
     * Muestra los teléfonos registrados para un cliente.
     * @param request  solicitud HTTP con parámetro idCliente
     * @param response respuesta HTTP
     */
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

        String idParam = request.getParameter("idCliente");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/ClienteControlador");
            return;
        }

        int idCliente = Integer.parseInt(idParam);
        request.setAttribute("telefonos", telefonoDAO.listarPorCliente(idCliente));
        request.setAttribute("idCliente", idCliente);
        request.getRequestDispatcher("/WEB-INF/view/telefonos.jsp").forward(request, response);
    }

    /**
     * Agrega o elimina un teléfono de un cliente.
     * @param request  solicitud HTTP con parámetros idCliente, accion y telefono
     * @param response respuesta HTTP
     */
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
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        int    idCliente = Integer.parseInt(request.getParameter("idCliente"));
        String accion    = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idTelefono = Integer.parseInt(request.getParameter("idTelefono"));
            telefonoDAO.eliminar(idTelefono);
        } else {
            String numero = request.getParameter("telefono");
            if (numero != null && !numero.isBlank()) {
                Telefono t = new Telefono();
                t.setTelefono(numero.trim());
                t.setClienteId(idCliente);
                telefonoDAO.agregar(t);
            }
        }

        response.sendRedirect(request.getContextPath() +
                "/TelefonoControlador?idCliente=" + idCliente);
    }
}

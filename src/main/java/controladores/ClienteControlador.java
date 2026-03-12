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

    // ─────────────────────────────────────────────
    // Solo admin puede gestionar clientes
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
            // CORREGIDO: antes redirigía a /index.jsp (que no existe como controlador)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // GET: listar clientes y mostrar editarusuario.jsp
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setAttribute("clientes", usuarioDAO.listarClientes());

        // CORREGIDO: /view/editarusuario.jsp (antes /WEB-INF/view/clientes.jsp)
        request.getRequestDispatcher("/view/editarusuario.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────
    // POST: editar datos de un cliente
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("actualizar".equals(accion)) {
            int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
            String nombre   = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String email    = request.getParameter("email");

            boolean ok = usuarioDAO.actualizarCliente(idUsuario, nombre, apellido, email);
            if (!ok) {
                request.setAttribute("error", "No se pudo actualizar el cliente.");
                request.setAttribute("clientes", usuarioDAO.listarClientes());
                request.getRequestDispatcher("/view/editarusuario.jsp").forward(request, response);
                return;
            }

        } else if ("eliminar".equals(accion)) {
            int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
            usuarioDAO.eliminarCliente(idUsuario);
        }

        response.sendRedirect(request.getContextPath() + "/ClienteControlador");
    }
}

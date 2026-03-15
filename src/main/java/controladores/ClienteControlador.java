package controladores;

import dao.ClienteDAO;
import dao.PermisosDAO;
import modelos.Cliente;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controlador CRUD de clientes (tabla Cliente).
 * Solo administradores pueden gestionar clientes.
 */
@WebServlet("/ClienteControlador")
public class ClienteControlador extends HttpServlet {

    private final ClienteDAO  clienteDAO  = new ClienteDAO();
    private final PermisosDAO permisosDAO = new PermisosDAO();

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
        if (!permisosDAO.tienePermiso(u.getIdRol(), "GESTIONAR_CLIENTES")) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;
        request.setAttribute("clientes", clienteDAO.listar());
        request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("crear".equals(accion)) {
            String nombre   = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String cedula   = request.getParameter("cedula");
            String telefono = request.getParameter("telefono");

            if (nombre == null || nombre.isBlank() ||
                apellido == null || apellido.isBlank() ||
                cedula == null || cedula.isBlank()) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "Nombre, apellido y cédula son obligatorios.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            if (clienteDAO.cedulaExiste(cedula)) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "Ya existe un cliente con esa cédula.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }

            Cliente c = new Cliente();
            c.setNombre(nombre);
            c.setApellido(apellido);
            c.setCedula(cedula);
            c.setTelefono(telefono);
            clienteDAO.crear(c);

        } else if ("actualizar".equals(accion)) {
            int    id       = Integer.parseInt(request.getParameter("idCliente"));
            String nombre   = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String cedula   = request.getParameter("cedula");
            String telefono = request.getParameter("telefono");

            if (clienteDAO.cedulaExisteExcluyendo(cedula, id)) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "Ya existe otro cliente con esa cédula.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            clienteDAO.actualizar(new Cliente(id, nombre, apellido, cedula, telefono));

        } else if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("idCliente"));
            clienteDAO.eliminar(id);
        }

        response.sendRedirect(request.getContextPath() + "/ClienteControlador");
    }
}

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
import java.util.List;

/**
 * Controlador CRUD de clientes (tabla Cliente).
 * Solo administradores pueden gestionar clientes.
 * La acción "buscar" está disponible para cualquier usuario autenticado.
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

        // Búsqueda AJAX: disponible para cualquier usuario autenticado (empleados y admins)
        if ("buscar".equals(request.getParameter("accion"))) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuarioLogueado") == null) {
                response.sendError(401);
                return;
            }
            String q = request.getParameter("q");
            if (q == null) q = "";
            List<Cliente> resultados = clienteDAO.buscarPorTexto(q.trim());

            response.setContentType("application/json; charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < resultados.size(); i++) {
                Cliente c = resultados.get(i);
                if (i > 0) json.append(",");
                json.append("{\"id\":").append(c.getIdCliente())
                    .append(",\"nombre\":\"").append(esc(c.getNombre())).append("\"")
                    .append(",\"apellido\":\"").append(esc(c.getApellido())).append("\"")
                    .append(",\"cedula\":\"").append(esc(c.getCedula())).append("\"}");
            }
            json.append("]");
            response.getWriter().write(json.toString());
            return;
        }

        if (!verificarAdmin(request, response)) return;
        request.setAttribute("clientes", clienteDAO.listar());
        request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
    }

    /** Escapa caracteres especiales JSON básicos. */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
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

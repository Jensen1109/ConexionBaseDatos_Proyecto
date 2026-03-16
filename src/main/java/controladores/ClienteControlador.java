package controladores;

import dao.ClienteDAO;
import dao.PermisosDAO;
import dao.TelefonoDAO;
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
    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

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

    /** Valida campos de cliente y retorna mensaje de error, o null si todo está bien. */
    private String validarCamposCliente(String nombre, String apellido, String cedula,
                                        String telefono, String email) {
        if (nombre == null || nombre.isBlank())   return "El nombre es obligatorio.";
        if (!nombre.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El nombre solo puede contener letras (mínimo 2 caracteres).";

        if (apellido == null || apellido.isBlank()) return "El apellido es obligatorio.";
        if (!apellido.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El apellido solo puede contener letras (mínimo 2 caracteres).";

        if (cedula == null || cedula.isBlank())   return "La cédula es obligatoria.";
        if (!cedula.trim().matches("\\d{8,15}"))
            return "La cédula debe contener solo números (mínimo 8, máximo 15 dígitos).";

        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("\\d{1,15}"))
            return "El teléfono solo puede contener números (máximo 15 dígitos).";

        if (email != null && !email.isBlank() &&
            !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "El correo electrónico no es válido. Ejemplo: nombre@gmail.com";

        return null;
    }

    /** Guarda o reemplaza el teléfono del cliente en la tabla Telefono. */
    private void sincronizarTelefono(int idCliente, String telefono, boolean esNuevo) {
        if (!esNuevo) telefonoDAO.eliminarPorCliente(idCliente);
        if (telefono != null && !telefono.isBlank()) {
            modelos.Telefono t = new modelos.Telefono(0, telefono.trim(), idCliente);
            telefonoDAO.agregar(t);
        }
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
            String email    = request.getParameter("email");

            String errMsg = validarCamposCliente(nombre, apellido, cedula, telefono, email);
            if (errMsg != null) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", errMsg);
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            if (clienteDAO.cedulaExiste(cedula.trim())) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "Ya existe un cliente con esa cédula.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }

            Cliente c = new Cliente();
            c.setNombre(nombre.trim());
            c.setApellido(apellido.trim());
            c.setCedula(cedula.trim());
            c.setEmail(email != null ? email.trim() : null);
            int idNuevo = clienteDAO.crearYObtenerIdCliente(c);

            // Guardar teléfono en tabla Telefono
            sincronizarTelefono(idNuevo, telefono, true);

        } else if ("actualizar".equals(accion)) {
            int    id       = Integer.parseInt(request.getParameter("idCliente"));
            String nombre   = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String cedula   = request.getParameter("cedula");
            String telefono = request.getParameter("telefono");
            String email    = request.getParameter("email");

            String errMsg = validarCamposCliente(nombre, apellido, cedula, telefono, email);
            if (errMsg != null) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", errMsg);
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            if (clienteDAO.cedulaExisteExcluyendo(cedula.trim(), id)) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "Ya existe otro cliente con esa cédula.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            Cliente c = new Cliente(id, nombre.trim(), apellido.trim(), cedula.trim());
            c.setEmail(email != null ? email.trim() : null);
            clienteDAO.actualizar(c);

            // Reemplazar teléfono en tabla Telefono
            sincronizarTelefono(id, telefono, false);

        } else if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("idCliente"));
            clienteDAO.eliminar(id);
        }

        response.sendRedirect(request.getContextPath() + "/ClienteControlador");
    }
}

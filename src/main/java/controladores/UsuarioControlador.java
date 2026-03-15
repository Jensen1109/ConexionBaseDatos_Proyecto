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

/**
 * Controlador de gestión de usuarios del sistema (admins y empleados).
 * Solo los administradores pueden registrar, editar y eliminar usuarios.
 * RF01: solo admin puede crear nuevos usuarios.
 */
@WebServlet("/UsuarioControlador")
public class UsuarioControlador extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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

    /** GET: lista todos los usuarios del sistema o muestra formulario de registro. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            // Formulario para registrar un nuevo usuario (admin o empleado)
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
        } else {
            // Lista todos los usuarios
            request.setAttribute("usuarios", usuarioDAO.listar());
            request.getRequestDispatcher("/view/gestionUsuarios.jsp").forward(request, response);
        }
    }

    /** POST: crear, actualizar o eliminar un usuario. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("registrar".equals(accion)) {
            String nombre     = request.getParameter("nombre");
            String apellido   = request.getParameter("apellido");
            String email      = request.getParameter("email");
            String contrasena = request.getParameter("contrasena");
            String cedula     = request.getParameter("cedula");
            String rolStr     = request.getParameter("idRol");

            // Validación de campos obligatorios
            if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank() ||
                email == null  || email.isBlank()  || contrasena == null || contrasena.isBlank() ||
                cedula == null || cedula.isBlank()  || rolStr == null || rolStr.isBlank()) {
                request.setAttribute("error", "Por favor completa todos los campos.");
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                return;
            }

            // Validar unicidad de email
            if (usuarioDAO.emailExiste(email)) {
                request.setAttribute("error", "Este email ya está registrado.");
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                return;
            }

            // Validar unicidad de cédula
            if (usuarioDAO.cedulaExiste(cedula)) {
                request.setAttribute("error", "Esta cédula ya está registrada.");
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setEmail(email);
            u.setCedula(cedula);
            u.setIdRol(Integer.parseInt(rolStr));

            boolean ok = usuarioDAO.registrar(u, contrasena);
            if (!ok) {
                request.setAttribute("error", "Error al registrar el usuario. Intenta de nuevo.");
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                return;
            }

        } else if ("actualizar".equals(accion)) {
            int    id       = Integer.parseInt(request.getParameter("idUsuario"));
            String nombre   = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String email    = request.getParameter("email");
            int    rol      = Integer.parseInt(request.getParameter("idRol"));

            Usuario u = new Usuario();
            u.setIdUsuario(id);
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setEmail(email);
            u.setIdRol(rol);
            usuarioDAO.actualizar(u);

        } else if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("idUsuario"));
            // No permitir que el admin se elimine a sí mismo
            Usuario actual = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
            if (actual.getIdUsuario() == id) {
                request.setAttribute("usuarios", usuarioDAO.listar());
                request.setAttribute("error", "No puedes eliminar tu propio usuario.");
                request.getRequestDispatcher("/view/gestionUsuarios.jsp").forward(request, response);
                return;
            }
            usuarioDAO.eliminar(id);
        }

        response.sendRedirect(request.getContextPath() + "/UsuarioControlador");
    }
}

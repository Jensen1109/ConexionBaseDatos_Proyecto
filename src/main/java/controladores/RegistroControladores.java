
package controladores;

import dao.UsuarioDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador de registro de usuarios.
 * Gestiona el formulario de creación de nuevas cuentas.
 * Acceso público (sin sesión requerida) para registro inicial;
 * cuando un admin está autenticado el formulario delega a UsuarioControlador.
 */
@WebServlet("/RegistroControlador")
public class RegistroControladores extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    
    /**
     * Muestra el formulario de registro.
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
    }

    /**
     * Procesa el formulario de registro: valida campos, verifica unicidad
     * de email y cédula, y crea el usuario con contraseña cifrada en BCrypt.
     * @param request  solicitud HTTP con parámetros del formulario
     * @param response respuesta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Recogemos los datos del formulario
        String nombre     = request.getParameter("nombre");
        String apellido   = request.getParameter("apellido");
        String email      = request.getParameter("email");
        String contrasena = request.getParameter("contrasena");
        String cedula     = request.getParameter("cedula");

        // Validación básica
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            email == null || email.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            cedula == null || cedula.isBlank()) {

            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return;
        }

        // Verificar si el email ya existe
        if (usuarioDAO.emailExiste(email)) {
            request.setAttribute("error", "Este email ya está registrado.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return;
        }

        // Verificar si la cédula ya existe
        if (usuarioDAO.cedulaExiste(cedula)) {
            request.setAttribute("error", "Esta cédula ya está registrada.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return;
        }

        // Crear el objeto usuario (rol 2 = cliente por defecto)
        Usuario u = new Usuario();
        u.setIdRol(2);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setCedula(cedula);

        boolean ok = usuarioDAO.registrar(u, contrasena);

        if (ok) {
            // Registro exitoso, redirigir al login
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
        } else {
            request.setAttribute("error", "Error al registrar. Intenta de nuevo.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
        }
    }
}
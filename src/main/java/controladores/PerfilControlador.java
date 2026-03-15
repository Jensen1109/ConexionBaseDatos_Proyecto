package controladores;

import dao.UsuarioDAO;
import modelos.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controlador de perfil propio.
 * Cualquier usuario autenticado puede ver y editar sus propios datos.
 */
@WebServlet("/PerfilControlador")
public class PerfilControlador extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!verificarSesion(request, response)) return;
        request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!verificarSesion(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        HttpSession session  = request.getSession(false);
        Usuario actual       = (Usuario) session.getAttribute("usuarioLogueado");
        int     idUsuario    = actual.getIdUsuario();

        String nombre    = request.getParameter("nombre");
        String apellido  = request.getParameter("apellido");
        String email     = request.getParameter("email");

        // Validación básica
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            email == null || email.isBlank()) {
            request.setAttribute("error", "Nombre, apellido y email son obligatorios.");
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            return;
        }

        // Email único (excluyendo el propio)
        if (usuarioDAO.emailExisteExcluyendo(email.trim(), idUsuario)) {
            request.setAttribute("error", "Ese email ya está en uso por otro usuario.");
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            return;
        }

        // Actualizar datos básicos
        boolean ok = usuarioDAO.actualizarPerfil(idUsuario, nombre.trim(), apellido.trim(), email.trim());
        if (!ok) {
            request.setAttribute("error", "No se pudo actualizar el perfil. Intenta de nuevo.");
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            return;
        }

        // Cambio de contraseña (opcional)
        String contrasenaActual = request.getParameter("contrasenaActual");
        String contrasenaNueva  = request.getParameter("contrasenaNueva");
        String confirmar        = request.getParameter("confirmar");

        if (contrasenaActual != null && !contrasenaActual.isBlank()) {
            String hashActual = usuarioDAO.obtenerHashContrasena(idUsuario);
            if (hashActual == null || !BCrypt.checkpw(contrasenaActual, hashActual)) {
                // Actualizar sesión con datos ya guardados antes de mostrar el error
                actual.setNombre(nombre.trim());
                actual.setApellido(apellido.trim());
                actual.setEmail(email.trim());
                session.setAttribute("usuarioLogueado", actual);
                request.setAttribute("exito", "Datos actualizados.");
                request.setAttribute("error", "La contraseña actual es incorrecta.");
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                return;
            }
            if (contrasenaNueva == null || contrasenaNueva.length() < 6) {
                request.setAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres.");
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                return;
            }
            if (!contrasenaNueva.equals(confirmar)) {
                request.setAttribute("error", "Las contraseñas nuevas no coinciden.");
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                return;
            }
            String nuevoHash = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));
            usuarioDAO.actualizarContrasena(idUsuario, nuevoHash);
        }

        // Actualizar datos en sesión
        actual.setNombre(nombre.trim());
        actual.setApellido(apellido.trim());
        actual.setEmail(email.trim());
        session.setAttribute("usuarioLogueado", actual);

        response.sendRedirect(request.getContextPath() + "/PerfilControlador?exito=1");
    }
}

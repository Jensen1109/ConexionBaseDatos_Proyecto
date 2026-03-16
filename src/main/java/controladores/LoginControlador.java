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
 * Controlador de autenticación.
 * Acepta login con email O cédula + contraseña.
 * Redirige según rol: Admin → ProductoControlador, Empleado → PedidoControlador?accion=nuevo
 */
@WebServlet("/LoginControlador")
public class LoginControlador extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Muestra el formulario de login. Si ya existe sesión activa,
     * redirige directamente al dashboard según el rol del usuario.
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Anti-caché: no guardar páginas autenticadas en historial del navegador
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            redirigirPorRol(request, response,
                    (Usuario) session.getAttribute("usuarioLogueado"));
            return;
        }

        request.getRequestDispatcher("/view/login.jsp").forward(request, response);
    }

    /**
     * Procesa las credenciales de login. Acepta email o cédula como identificador.
     * Crea la sesión y redirige según rol si las credenciales son correctas.
     * @param request  solicitud HTTP con parámetros "email" y "contrasena"
     * @param response respuesta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        String identificador = request.getParameter("email");      // campo "email" en el form
        String contrasena    = request.getParameter("contrasena");

        // Validación básica
        if (identificador == null || identificador.isBlank() ||
            contrasena    == null || contrasena.isBlank()) {
            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
            return;
        }

        // Detectar si el identificador es email (contiene @) o cédula
        Usuario usuario;
        if (identificador.trim().contains("@")) {
            usuario = usuarioDAO.login(identificador.trim(), contrasena);
        } else {
            usuario = usuarioDAO.loginPorCedula(identificador.trim(), contrasena);
        }

        if (usuario != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("nombreUsuario",   usuario.getNombre());
            session.setAttribute("rolUsuario",      usuario.getIdRol());
            session.setMaxInactiveInterval(30 * 60); // 30 minutos

            redirigirPorRol(request, response, usuario);
        } else {
            request.setAttribute("error", "Credenciales incorrectas. Verifica email/cédula y contraseña.");
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
        }
    }

    /**
     * Redirige al dashboard correspondiente según el rol del usuario.
     * Admin (1) → ProductoControlador
     * Empleado (2) → PedidoControlador?accion=nuevo
     */
    private void redirigirPorRol(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Usuario usuario) throws IOException {
        if (usuario.getIdRol() == 1) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
        } else {
            response.sendRedirect(request.getContextPath() + "/PedidoControlador?accion=nuevo");
        }
    }
}

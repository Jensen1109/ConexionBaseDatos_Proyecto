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

@WebServlet("/LoginControlador")
public class LoginControlador extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ─────────────────────────────────────────────
    // GET: muestra la página de login
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Si ya hay sesión activa, redirige al dashboard (evita el bucle con index.jsp)
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        request.getRequestDispatcher("/view/login.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────
    // POST: procesa el formulario de login
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email      = request.getParameter("email");
        String contrasena = request.getParameter("contrasena");

        // Validación básica de campos vacíos
        if (email == null || email.isBlank() || contrasena == null || contrasena.isBlank()) {
            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
            return;
        }

        // Intentar login
        Usuario usuario = usuarioDAO.login(email.trim(), contrasena);

        if (usuario != null) {
            // Login exitoso: crear sesión
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("nombreUsuario", usuario.getNombre());
            session.setAttribute("rolUsuario", usuario.getIdRol());
            session.setMaxInactiveInterval(30 * 60); // 30 minutos

            // Redirigir al dashboard (ProductoControlador maneja ambos roles)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");

        } else {
            // Login fallido
            request.setAttribute("error", "Email o contraseña incorrectos.");
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
        }
    }
}
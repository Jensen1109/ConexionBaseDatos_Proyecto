package controladores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controlador de cierre de sesión.
 * Invalida la sesión activa del usuario, aplica headers anti-caché
 * y redirige al formulario de login (RNF11).
 */
@WebServlet("/LogoutControlador")
public class LogoutControlador extends HttpServlet {

    /**
     * Invalida la sesión HTTP del usuario y redirige al login.
     * Aplica headers Cache-Control y Pragma para evitar acceso
     * a páginas protegidas mediante el botón "atrás" del navegador.
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Anti-caché: impide volver atrás a páginas protegidas tras logout
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/LoginControlador");
    }
}

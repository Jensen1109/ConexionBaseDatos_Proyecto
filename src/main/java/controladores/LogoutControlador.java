package controladores;

// Importaciones de Jakarta Servlet para manejar peticiones HTTP
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;  // Para registrar la URL del servlet
import jakarta.servlet.http.HttpServlet;        // Clase base de todos los servlets
import jakarta.servlet.http.HttpServletRequest; // Objeto con los datos de la petición
import jakarta.servlet.http.HttpServletResponse;// Objeto para enviar la respuesta
import jakarta.servlet.http.HttpSession;        // Sesión del usuario en el servidor
import java.io.IOException;

/**
 * Controlador de cierre de sesión (Logout).
 * Invalida la sesión activa del usuario, aplica headers anti-caché
 * y redirige al formulario de login.
 * Cumple con RNF11: Cierre de sesión seguro.
 */
@WebServlet("/LogoutControlador")
public class LogoutControlador extends HttpServlet {

    /**
     * Cierra la sesión del usuario cuando hace click en "Cerrar sesión".
     * 1. Pone headers anti-caché para que el navegador no guarde páginas protegidas
     * 2. Destruye la sesión completa (borra usuario, rol, permisos, todo)
     * 3. Redirige al login
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Headers anti-caché: le dicen al navegador que NO guarde esta página
        // "no-store" = no almacenar, "no-cache" = siempre pedir al servidor, "must-revalidate" = verificar antes de usar caché
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // "Pragma: no-cache" = compatibilidad con navegadores antiguos (HTTP 1.0)
        response.setHeader("Pragma", "no-cache");
        // "Expires: 0" = la página ya expiró inmediatamente, no reutilizar
        response.setDateHeader("Expires", 0);

        // Obtener la sesión actual sin crear una nueva (false = solo buscar la existente)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // INVALIDAR la sesión: destruye TODOS los datos guardados (usuario, nombre, rol)
            // Después de esto, el usuario ya no está logueado
            session.invalidate();
        }
        // Redirigir al formulario de login para que inicie sesión de nuevo si quiere
        response.sendRedirect(request.getContextPath() + "/LoginControlador");
    }
}

package controladores;

// Importaciones de Jakarta Servlet para crear filtros HTTP
import jakarta.servlet.Filter;           // Interfaz base que debe implementar todo filtro
import jakarta.servlet.FilterChain;      // Permite pasar la petición al siguiente filtro o servlet
import jakarta.servlet.FilterConfig;     // Configuración inicial del filtro
import jakarta.servlet.ServletException; // Excepción de servlets
import jakarta.servlet.ServletRequest;   // Petición genérica del servidor
import jakarta.servlet.ServletResponse;  // Respuesta genérica del servidor
import jakarta.servlet.annotation.WebFilter; // Anotación para registrar el filtro automáticamente
import jakarta.servlet.http.HttpServletRequest;  // Petición HTTP con métodos como getSession()
import jakarta.servlet.http.HttpServletResponse; // Respuesta HTTP con métodos como sendRedirect()
import jakarta.servlet.http.HttpSession;         // Sesión del usuario en el servidor
import java.io.IOException;

/**
 * Filtro global de sesión que protege TODAS las rutas del sistema.
 * Se ejecuta ANTES de cada petición HTTP que llegue al servidor.
 * Si el usuario no está logueado y la ruta no es pública, lo redirige al login.
 *
 * @WebFilter("/*") significa que intercepta TODAS las URLs del proyecto.
 */
@WebFilter("/*")
public class SesionFiltro implements Filter {

    // Se ejecuta una sola vez cuando arranca el servidor. No necesitamos inicializar nada.
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    /**
     * Método principal del filtro. Se ejecuta ANTES de cada petición HTTP.
     * Decide si la petición puede pasar (chain.doFilter) o si debe redirigir al login.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Convertir la petición genérica a HTTP para acceder a métodos como getRequestURI()
        HttpServletRequest httpReq  = (HttpServletRequest) request;
        // Convertir la respuesta genérica a HTTP para poder hacer sendRedirect()
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Obtener la URL que el usuario está pidiendo (ej: /proyecto_personal/ProductoControlador)
        String uri = httpReq.getRequestURI();
        // Obtener el contexto de la aplicación (ej: /proyecto_personal)
        String ctx = httpReq.getContextPath();

        // Definir las rutas PÚBLICAS que NO requieren estar logueado
       boolean esRutaPublica =
        uri.equals(ctx + "/")                            || // Página raíz del proyecto
        uri.equals(ctx + "/index.jsp")                   || // Página de inicio
        uri.equals(ctx + "/LoginControlador")            || // Controlador de login
        uri.equals(ctx + "/RegistroControlador")         || // Controlador de registro (nombre alterno)
        uri.equals(ctx + "/RegistroControladores")       || // Controlador de registro
        uri.startsWith(ctx + "/css/")                    || // Archivos de estilos CSS
        uri.startsWith(ctx + "/js/")                     || // Archivos JavaScript
        uri.startsWith(ctx + "/assets/")                 || // Recursos estáticos (fuentes, íconos)
        uri.endsWith("styles.css")                       || // Hoja de estilos principal
        uri.endsWith("login.jsp")                        || // Página de login
        uri.endsWith("registro.jsp")                     || // Página de registro
        uri.endsWith("login.html")                       || // Login en HTML
        uri.endsWith("registro.html")                    || // Registro en HTML
        uri.endsWith(".css")                             || // Cualquier archivo CSS
        uri.endsWith(".js")                              || // Cualquier archivo JavaScript
        uri.endsWith(".png")                             || // Imágenes PNG
        uri.endsWith(".jpg")                             || // Imágenes JPG
        uri.endsWith(".jpeg")                            || // Imágenes JPEG
        uri.endsWith(".ico")                             || // Íconos (favicon)
        uri.endsWith(".woff2")                           || // Fuentes web
        uri.endsWith(".ttf")                             || // Fuentes TrueType
        uri.endsWith(".svg")                             || // Imágenes vectoriales SVG
        uri.startsWith(ctx + "/uploads/");                  // Imágenes subidas de productos

        // Si la ruta es pública, dejar pasar sin verificar sesión
        if (esRutaPublica) {
            chain.doFilter(request, response); // Continúa normalmente hacia el servlet o JSP
            return; // Sale del filtro, no ejecuta más código
        }

        // Para rutas PROTEGIDAS: verificar si hay sesión activa
        // false = no crear sesión nueva si no existe (solo verificar)
        HttpSession session = httpReq.getSession(false);
        // Verificar que la sesión exista Y que tenga un usuario guardado dentro
        boolean logueado = session != null && session.getAttribute("usuarioLogueado") != null;

        if (logueado) {
            // El usuario SÍ está logueado → dejar pasar la petición
            chain.doFilter(request, response);
        } else {
            // El usuario NO está logueado → redirigir al formulario de login
            httpRes.sendRedirect(ctx + "/LoginControlador");
        }
    }

    // Se ejecuta cuando el servidor se apaga. No necesitamos limpiar nada.
    @Override
    public void destroy() {}
}

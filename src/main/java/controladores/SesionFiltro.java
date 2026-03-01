package controladores;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class SesionFiltro implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq  = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String uri = httpReq.getRequestURI();
        String ctx = httpReq.getContextPath();

        // Rutas públicas que no requieren sesión
       boolean esRutaPublica =
        uri.equals(ctx + "/")                            ||
        uri.equals(ctx + "/index.jsp")                   ||
        uri.equals(ctx + "/LoginControlador")            ||
        uri.equals(ctx + "/RegistroControlador")         ||
        uri.equals(ctx + "/RegistroControladores")       ||
        uri.startsWith(ctx + "/css/")                    ||
        uri.startsWith(ctx + "/js/")                     ||
        uri.startsWith(ctx + "/assets/")                 ||
        uri.endsWith("styles.css")                       ||
        uri.endsWith("login.jsp")                        ||
        uri.endsWith("registro.jsp")                     ||
        uri.endsWith("login.html")                       ||
        uri.endsWith("registro.html")                    ||
        uri.endsWith(".css")                             ||
        uri.endsWith(".js")                              ||
        uri.endsWith(".png")                             ||
        uri.endsWith(".jpg")                             ||
        uri.endsWith(".jpeg")                            ||
        uri.endsWith(".ico")                             ||
        uri.endsWith(".woff2")                           ||
        uri.endsWith(".ttf")                             ||
        uri.endsWith(".svg");
        if (esRutaPublica) {
            chain.doFilter(request, response);
            return;
        }

        // Para todo lo demás, verificar sesión
        HttpSession session = httpReq.getSession(false);
        boolean logueado = session != null && session.getAttribute("usuarioLogueado") != null;

        if (logueado) {
            chain.doFilter(request, response);
        } else {
            httpRes.sendRedirect(ctx + "/LoginControlador");
        }
    }

    @Override
    public void destroy() {}
}

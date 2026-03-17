// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase PermisosDAO para verificar permisos del usuario según su rol
import dao.PermisosDAO;
// Importar la clase ReporteDAO para obtener estadísticas y reportes del sistema
import dao.ReporteDAO;
// Importar el modelo Usuario para trabajar con el usuario que inició sesión
import modelos.Usuario;
// Importar ServletException para manejar errores del servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para mapear la URL del controlador
import jakarta.servlet.annotation.WebServlet;
// Importar la clase base HttpServlet de la que heredan todos los controladores
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para leer datos de la solicitud del navegador
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para enviar respuestas al navegador
import jakarta.servlet.http.HttpServletResponse;
// Importar HttpSession para manejar la sesión del usuario (login activo)
import jakarta.servlet.http.HttpSession;
// Importar IOException para manejar errores de entrada/salida
import java.io.IOException;

/**
 * Controlador de reportes y estadísticas del sistema.
 * Muestra un panel con indicadores clave: total de ventas del mes, productos con stock bajo,
 * total de deudas pendientes y cantidad de clientes registrados.
 * Solo los usuarios con permiso "VER_REPORTES" pueden acceder (verificado vía RBAC).
 * Si no tiene permiso, se redirige al catálogo de productos.
 */
// Mapear este servlet a la URL "/ReporteControlador" para que responda a esa ruta
@WebServlet("/ReporteControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class ReporteControlador extends HttpServlet {

    // Crear una instancia de ReporteDAO para consultar estadísticas y reportes en la BD
    private final ReporteDAO   reporteDAO   = new ReporteDAO();
    // Crear una instancia de PermisosDAO para verificar permisos del usuario según su rol
    private final PermisosDAO  permisosDAO  = new PermisosDAO();

    // ═══════════════════════════════════════════════════
    // GET: Mostrar panel de reportes con estadísticas
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Consulta las estadísticas del sistema y las muestra en el panel de reportes.
     * Verifica que el usuario tenga el permiso "VER_REPORTES" antes de mostrar la información.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión o si el usuario no ha iniciado sesión
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al login porque no hay sesión activa
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        // Obtener el objeto Usuario almacenado en la sesión (el usuario que inició sesión)
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Verificar que el usuario tenga el permiso "VER_REPORTES" usando el sistema RBAC
        if (!permisosDAO.tienePermiso(usuario.getIdRol(), "VER_REPORTES")) {
            // Si no tiene permiso, redirigir al catálogo de productos (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        // ─── CARGAR LAS ESTADÍSTICAS DEL SISTEMA ───

        // Obtener el total de ventas realizadas en el mes actual y pasarlo a la vista
        request.setAttribute("totalVentasMes",      reporteDAO.totalVentasMes());
        // Obtener la lista de productos que están por debajo del stock mínimo
        request.setAttribute("productosStockBajo",  reporteDAO.productosStockBajo());
        // Obtener el monto total de deudas pendientes (ventas a crédito sin pagar)
        request.setAttribute("totalDeudasPendientes", reporteDAO.totalDeudasPendientes());
        // Obtener la cantidad total de clientes registrados en el sistema
        request.setAttribute("totalClientes",       reporteDAO.contarClientes());

        // Reenviar la solicitud al archivo JSP que mostrará el panel de reportes
        request.getRequestDispatcher("/view/reportes.jsp").forward(request, response);
    }
}

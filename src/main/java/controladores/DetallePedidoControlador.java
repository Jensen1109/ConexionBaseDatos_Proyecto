// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase PedidoDAO para acceder a los detalles de un pedido en la BD
import dao.PedidoDAO;
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
 * Controlador de detalle de pedido (tabla detalle_pedido).
 * Muestra los productos vendidos dentro de un pedido específico.
 * Solo los administradores (id_rol=1) pueden ver los detalles de un pedido.
 * Se accede desde el historial de ventas pasando el parámetro ?idPedido=X.
 */
// Mapear este servlet a la URL "/DetallePedidoControlador" para que responda a esa ruta
@WebServlet("/DetallePedidoControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class DetallePedidoControlador extends HttpServlet {

    // Crear una instancia de PedidoDAO para consultar los detalles de pedidos en la BD
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    // ═══════════════════════════════════════════════════
    // GET: Ver los productos vendidos en un pedido
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Recibe el ID de un pedido y muestra la lista de productos vendidos en ese pedido.
     * Si no se envía un ID de pedido, redirige al historial de ventas.
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
        // Verificar que el usuario sea administrador (id_rol = 1)
        if (usuario.getIdRol() != 1) {
            // Si no es admin, redirigir al inicio (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        // Leer el parámetro "idPedido" de la URL (ej: ?idPedido=5)
        String idParam = request.getParameter("idPedido");
        // Si no se envió el ID del pedido o está vacío, redirigir al historial de ventas
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/PedidoControlador");
            return;
        }

        // Convertir el ID del pedido de texto a número entero
        int idPedido = Integer.parseInt(idParam);
        // Obtener la lista de detalles (productos vendidos) del pedido desde la BD
        request.setAttribute("detalles",  pedidoDAO.listarDetalles(idPedido));
        // Pasar el ID del pedido a la vista para mostrarlo como referencia
        request.setAttribute("idPedido",  idPedido);
        // Reenviar la solicitud al archivo JSP que mostrará la tabla de detalles del pedido
        request.getRequestDispatcher("/WEB-INF/view/detalle_pedido.jsp").forward(request, response);
    }
}

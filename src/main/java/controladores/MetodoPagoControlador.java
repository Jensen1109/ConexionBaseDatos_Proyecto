// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase MetodoPagoDAO para acceder a la base de datos de métodos de pago
import dao.MetodoPagoDAO;
// Importar el modelo MetodoPago que representa la entidad método de pago en la BD
import modelos.MetodoPago;
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
 * Controlador CRUD de métodos de pago (tabla MetodoPago).
 * Maneja: listar todos los métodos de pago, crear nuevo método y eliminar método existente.
 * Solo los administradores (id_rol=1) pueden acceder a este módulo.
 * Los métodos de pago se usan al registrar ventas (ej: Efectivo, Nequi, Tarjeta).
 */
// Mapear este servlet a la URL "/MetodoPagoControlador" para que responda a esa ruta
@WebServlet("/MetodoPagoControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class MetodoPagoControlador extends HttpServlet {

    // Crear una instancia de MetodoPagoDAO para realizar operaciones CRUD de métodos de pago en la BD
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();

    // ═══════════════════════════════════════════════════
    // GET: Listar todos los métodos de pago
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Lista todos los métodos de pago registrados y los muestra en la vista.
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

        // Obtener la lista completa de métodos de pago de la BD y pasarla como atributo a la vista
        request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
        // Reenviar la solicitud al archivo JSP que mostrará la tabla de métodos de pago
        request.getRequestDispatcher("/WEB-INF/view/metodos_pago.jsp").forward(request, response);
    }

    // ═══════════════════════════════════════════════════
    // POST: Crear o eliminar método de pago
    // ═══════════════════════════════════════════════════

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Maneja las acciones: crear nuevo método de pago y eliminar método existente.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva
        HttpSession session = request.getSession(false);
        // Verificar que exista sesión y que haya un usuario logueado
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Si no hay sesión, redirigir al login
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        // Obtener el usuario logueado de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Verificar que sea administrador
        if (usuario.getIdRol() != 1) {
            // Si no es admin, redirigir al inicio
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        // Obtener la acción que se quiere realizar ("eliminar" o crear por defecto)
        String accion = request.getParameter("accion");

        // ===== ACCIÓN: ELIMINAR un método de pago =====
        if ("eliminar".equals(accion)) {
            // Leer el ID del método de pago a eliminar y convertirlo a número entero
            int id = Integer.parseInt(request.getParameter("id"));
            // Ejecutar la eliminación del método de pago en la base de datos
            metodoPagoDAO.eliminar(id);

        // ===== ACCIÓN: CREAR un nuevo método de pago =====
        } else {
            // Leer el nombre del nuevo método de pago desde el formulario
            String nombre = request.getParameter("nombre");
            // Validar que el nombre no sea nulo ni esté vacío
            if (nombre != null && !nombre.isBlank()) {
                // Crear un nuevo objeto MetodoPago vacío
                MetodoPago mp = new MetodoPago();
                // Asignar el nombre quitando espacios en blanco al inicio y final
                mp.setNombre(nombre.trim());
                // Insertar el nuevo método de pago en la base de datos
                metodoPagoDAO.crear(mp);
            }
        }

        // Redirigir al listado de métodos de pago para que se actualice la vista
        response.sendRedirect(request.getContextPath() + "/MetodoPagoControlador");
    }
}

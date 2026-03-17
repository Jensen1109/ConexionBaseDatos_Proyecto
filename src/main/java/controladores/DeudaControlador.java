// Declaración del paquete al que pertenece esta clase (agrupación de controladores)
package controladores;

// Importar DeudaDAO para acceder a las operaciones de deudas en la base de datos
import dao.DeudaDAO;
// Importar PermisosDAO para verificar si el usuario tiene permiso de gestionar deudas
import dao.PermisosDAO;
// Importar el modelo Usuario para trabajar con los datos del usuario logueado
import modelos.Usuario;
// Importar ServletException para manejar errores internos del servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para mapear este controlador a una URL específica
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet, la clase base de la que heredan todos los servlets HTTP
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para leer los datos enviados por el navegador
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para enviar la respuesta de vuelta al navegador
import jakarta.servlet.http.HttpServletResponse;
// Importar HttpSession para manejar la sesión del usuario (verificar si está logueado)
import jakarta.servlet.http.HttpSession;
// Importar IOException para manejar errores de entrada/salida
import java.io.IOException;
// Importar BigDecimal para manejar montos de dinero con precisión decimal exacta
import java.math.BigDecimal;

/**
 * Controlador de deudas y abonos.
 * Requiere permiso GESTIONAR_DEUDAS.
 */
// Mapear este servlet a la URL "/DeudaControlador" para que el servidor lo reconozca
@WebServlet("/DeudaControlador")
// Declarar la clase que extiende HttpServlet para funcionar como controlador web
public class DeudaControlador extends HttpServlet {

    // Crear una instancia de DeudaDAO para realizar operaciones de deudas en la BD
    private final DeudaDAO    deudaDAO    = new DeudaDAO();
    // Crear una instancia de PermisosDAO para consultar permisos del usuario según su rol
    private final PermisosDAO permisosDAO = new PermisosDAO();

    /**
     * Método privado que verifica si el usuario tiene permiso para gestionar deudas.
     * Si no tiene permiso o no está logueado, lo redirige a otra página.
     * Retorna true si tiene permiso, false si no.
     */
    private boolean verificarPermiso(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Configurar cabecera para evitar que el navegador almacene la página en caché
        response.setHeader("Cache-Control", "no-store");
        // Cabecera adicional para navegadores antiguos, previene que se guarde en caché
        response.setHeader("Pragma", "no-cache");
        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión activa o si el usuario no ha iniciado sesión
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al formulario de login porque no hay usuario autenticado
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            // Retornar false indicando que NO tiene permiso de acceso
            return false;
        }
        // Obtener el objeto Usuario almacenado en la sesión (el usuario logueado)
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        // Consultar en la BD si el rol del usuario tiene el permiso "GESTIONAR_DEUDAS"
        if (!permisosDAO.tienePermiso(u.getIdRol(), "GESTIONAR_DEUDAS")) {
            // Si no tiene el permiso, redirigir a la vista de productos (página por defecto)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            // Retornar false indicando que NO tiene permiso
            return false;
        }
        // Si pasó todas las verificaciones, retornar true (sí tiene permiso)
        return true;
    }

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Muestra la lista de deudas activas con los datos del cliente.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario tenga permiso; si no, salir del método
        if (!verificarPermiso(request, response)) return;

        // Obtener todas las deudas activas (pendientes) junto con el nombre del cliente
        // Default: listar deudas activas con nombre del cliente
        request.setAttribute("deudas",        deudaDAO.listarActivasConCliente());
        // Calcular el total de dinero pendiente por cobrar de todas las deudas activas
        request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
        // Reenviar la solicitud al archivo JSP que mostrará la vista de deudores
        request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
    }

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Procesa el registro de un abono a una deuda existente.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario tenga permiso para gestionar deudas
        if (!verificarPermiso(request, response)) return;
        // Establecer codificación UTF-8 para leer correctamente caracteres especiales
        request.setCharacterEncoding("UTF-8");

        // Leer el ID de la deuda a la que se quiere abonar, enviado desde el formulario
        // Registrar abono a deuda existente
        String idDeudaStr = request.getParameter("idDeuda");
        // Leer el monto del abono que el cliente quiere pagar
        String montoStr   = request.getParameter("monto");

        // Verificar que ambos parámetros hayan sido enviados (no sean nulos)
        if (idDeudaStr == null || montoStr == null) {
            // Si faltan datos, redirigir al listado de deudas sin hacer nada
            response.sendRedirect(request.getContextPath() + "/DeudaControlador");
            // Salir del método
            return;
        }

        // Convertir el ID de la deuda de texto a número entero
        int idDeuda        = Integer.parseInt(idDeudaStr);
        // Convertir el monto del abono de texto a BigDecimal para precisión decimal
        BigDecimal monto   = new BigDecimal(montoStr);

        // Intentar registrar el abono en la BD; retorna true si fue exitoso
        boolean ok = deudaDAO.abonar(idDeuda, monto);
        // Si el abono NO fue exitoso (ej: el monto supera lo que se debe)
        if (!ok) {
            // Recargar la lista de deudas activas para mostrar en la vista
            request.setAttribute("deudas",        deudaDAO.listarActivasConCliente());
            // Recargar el total pendiente actualizado
            request.setAttribute("totalPendiente", deudaDAO.totalPendiente());
            // Establecer el mensaje de error para mostrarlo al usuario en la vista
            request.setAttribute("error", "El abono supera el monto pendiente.");
            // Reenviar a la vista de deudores mostrando el error
            request.getRequestDispatcher("/view/deudores.jsp").forward(request, response);
            // Salir del método sin redirigir
            return;
        }

        // Si el abono fue exitoso, redirigir al listado de deudas para ver los cambios
        response.sendRedirect(request.getContextPath() + "/DeudaControlador");
    }
}

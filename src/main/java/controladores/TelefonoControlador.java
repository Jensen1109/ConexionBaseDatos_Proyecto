// Declaración del paquete al que pertenece esta clase (agrupación de controladores)
package controladores;

// Importar TelefonoDAO para realizar operaciones CRUD de teléfonos en la base de datos
import dao.TelefonoDAO;
// Importar el modelo Telefono que representa un registro de teléfono en la BD
import modelos.Telefono;
// Importar el modelo Usuario para verificar los datos del usuario logueado
import modelos.Usuario;
// Importar ServletException para manejar errores internos del servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para registrar este servlet con una URL específica
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet, la clase base para crear controladores HTTP en Jakarta
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para leer los datos enviados por el navegador
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para enviar la respuesta al navegador
import jakarta.servlet.http.HttpServletResponse;
// Importar HttpSession para acceder a la sesión del usuario autenticado
import jakarta.servlet.http.HttpSession;
// Importar IOException para manejar errores de entrada/salida en la comunicación HTTP
import java.io.IOException;

/**
 * Controlador para gestionar los teléfonos adicionales de un cliente.
 * Permite listar, agregar y eliminar teléfonos asociados a un cliente.
 * Solo accesible por administradores.
 */
// Mapear este servlet a la URL "/TelefonoControlador" para que responda a esa ruta
@WebServlet("/TelefonoControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class TelefonoControlador extends HttpServlet {

    // Crear una instancia de TelefonoDAO para realizar operaciones de teléfonos en la BD
    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

    /**
     * Muestra los teléfonos registrados para un cliente.
     * @param request  solicitud HTTP con parámetro idCliente
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión o si el usuario no ha iniciado sesión
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al login porque no hay un usuario autenticado
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            // Salir del método sin continuar
            return;
        }
        // Obtener el objeto Usuario almacenado en la sesión (el usuario logueado actualmente)
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Verificar si el usuario NO es administrador (idRol=1 es admin)
        if (usuario.getIdRol() != 1) {
            // Si no es admin, redirigir a la página de productos (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            // Salir del método sin mostrar los teléfonos
            return;
        }

        // Leer el parámetro "idCliente" de la URL para saber de qué cliente se quieren los teléfonos
        String idParam = request.getParameter("idCliente");
        // Verificar que el parámetro no sea nulo ni esté vacío
        if (idParam == null || idParam.isBlank()) {
            // Si no se proporcionó el ID del cliente, redirigir al listado de clientes
            response.sendRedirect(request.getContextPath() + "/ClienteControlador");
            // Salir del método
            return;
        }

        // Convertir el ID del cliente de texto a número entero
        int idCliente = Integer.parseInt(idParam);
        // Consultar en la BD todos los teléfonos asociados a este cliente y pasarlos a la vista
        request.setAttribute("telefonos", telefonoDAO.listarPorCliente(idCliente));
        // Pasar el ID del cliente a la vista para que los formularios lo incluyan
        request.setAttribute("idCliente", idCliente);
        // Reenviar la solicitud al archivo JSP que mostrará la lista de teléfonos del cliente
        request.getRequestDispatcher("/WEB-INF/view/telefonos.jsp").forward(request, response);
    }

    /**
     * Agrega o elimina un teléfono de un cliente.
     * @param request  solicitud HTTP con parámetros idCliente, accion y telefono
     * @param response respuesta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión o si el usuario no está logueado
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al login si no hay sesión activa
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            // Salir del método sin procesar la solicitud
            return;
        }
        // Obtener el objeto Usuario de la sesión para verificar su rol
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Verificar si el usuario NO es administrador (solo admins pueden gestionar teléfonos)
        if (usuario.getIdRol() != 1) {
            // Si no es admin, redirigir a productos (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            // Salir del método sin hacer cambios
            return;
        }

        // Leer el ID del cliente al que pertenece el teléfono y convertirlo a entero
        int    idCliente = Integer.parseInt(request.getParameter("idCliente"));
        // Leer la acción a realizar (puede ser "eliminar" o agregar por defecto)
        String accion    = request.getParameter("accion");

        // Verificar si la acción solicitada es eliminar un teléfono
        if ("eliminar".equals(accion)) {
            // Leer el ID del teléfono que se quiere eliminar y convertirlo a entero
            int idTelefono = Integer.parseInt(request.getParameter("idTelefono"));
            // Eliminar el teléfono de la base de datos usando su ID
            telefonoDAO.eliminar(idTelefono);
        } else {
            // Si la acción no es eliminar, entonces se quiere agregar un nuevo teléfono
            // Leer el número de teléfono ingresado en el formulario
            String numero = request.getParameter("telefono");
            // Verificar que el número no sea nulo ni esté vacío antes de guardarlo
            if (numero != null && !numero.isBlank()) {
                // Crear un nuevo objeto Telefono vacío para llenarlo con los datos
                Telefono t = new Telefono();
                // Asignar el número de teléfono al objeto, quitando espacios en blanco
                t.setTelefono(numero.trim());
                // Asignar el ID del cliente al que pertenece este teléfono
                t.setClienteId(idCliente);
                // Insertar el nuevo teléfono en la base de datos
                telefonoDAO.agregar(t);
            }
        }

        // Redirigir de vuelta a la lista de teléfonos del mismo cliente para ver los cambios
        response.sendRedirect(request.getContextPath() +
                "/TelefonoControlador?idCliente=" + idCliente);
    }
}

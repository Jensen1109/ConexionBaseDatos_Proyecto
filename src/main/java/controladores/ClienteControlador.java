// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase ClienteDAO para acceder a la base de datos de clientes
import dao.ClienteDAO;
// Importar la clase PermisosDAO para verificar permisos del usuario
import dao.PermisosDAO;
// Importar la clase TelefonoDAO para gestionar los teléfonos asociados a clientes
import dao.TelefonoDAO;
// Importar el modelo Cliente que representa la entidad cliente en la base de datos
import modelos.Cliente;
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
// Importar List para manejar listas de objetos (ej: lista de clientes)
import java.util.List;

/**
 * Controlador CRUD de clientes (tabla Cliente).
 * Solo administradores pueden gestionar clientes.
 * La acción "buscar" está disponible para cualquier usuario autenticado.
 */
// Mapear este servlet a la URL "/ClienteControlador" para que responda a esa ruta
@WebServlet("/ClienteControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class ClienteControlador extends HttpServlet {

    // Crear una instancia de ClienteDAO para realizar operaciones CRUD de clientes en la BD
    private final ClienteDAO  clienteDAO  = new ClienteDAO();
    // Crear una instancia de PermisosDAO para consultar los permisos del usuario según su rol
    private final PermisosDAO permisosDAO = new PermisosDAO();
    // Crear una instancia de TelefonoDAO para agregar/eliminar teléfonos de clientes
    private final TelefonoDAO telefonoDAO = new TelefonoDAO();

    /**
     * Método privado que verifica si el usuario actual es administrador.
     * Si no lo es, lo redirige a la página correspondiente.
     * Retorna true si tiene permiso, false si no.
     */
    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Configurar cabecera para evitar que el navegador guarde la página en caché
        response.setHeader("Cache-Control", "no-store");
        // Cabecera adicional de compatibilidad para navegadores antiguos, evita caché
        response.setHeader("Pragma", "no-cache");
        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión o si el usuario no ha iniciado sesión
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al login porque no hay sesión activa
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            // Retornar false indicando que NO tiene permiso
            return false;
        }
        // Obtener el objeto Usuario almacenado en la sesión (el usuario que inició sesión)
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        // Consultar en la BD si el rol del usuario tiene el permiso "GESTIONAR_CLIENTES"
        if (!permisosDAO.tienePermiso(u.getIdRol(), "GESTIONAR_CLIENTES")) {
            // Si no tiene permiso, redirigir a la página de productos (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            // Retornar false indicando que NO tiene permiso
            return false;
        }
        // Si pasó todas las validaciones, retornar true (sí tiene permiso de administrador)
        return true;
    }

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Puede listar clientes o realizar búsquedas AJAX.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar si la acción solicitada es "buscar" (búsqueda AJAX desde ventas)
        // Búsqueda AJAX: disponible para cualquier usuario autenticado (empleados y admins)
        if ("buscar".equals(request.getParameter("accion"))) {
            // Obtener la sesión actual sin crear una nueva
            HttpSession session = request.getSession(false);
            // Verificar que exista sesión y que haya un usuario logueado
            if (session == null || session.getAttribute("usuarioLogueado") == null) {
                // Si no hay sesión, enviar error 401 (no autorizado) como respuesta
                response.sendError(401);
                // Salir del método sin continuar
                return;
            }
            // Obtener el texto de búsqueda enviado por el parámetro "q" de la URL
            String q = request.getParameter("q");
            // Si no se envió ningún texto, usar cadena vacía para evitar errores null
            if (q == null) q = "";
            // Buscar clientes en la BD cuyo nombre, apellido o cédula coincidan con el texto
            List<Cliente> resultados = clienteDAO.buscarPorTexto(q.trim());

            // Establecer el tipo de respuesta como JSON con codificación UTF-8
            response.setContentType("application/json; charset=UTF-8");
            // Evitar que el navegador guarde en caché los resultados de búsqueda
            response.setHeader("Cache-Control", "no-store");
            // Crear un StringBuilder para construir manualmente el JSON de respuesta
            StringBuilder json = new StringBuilder("[");
            // Recorrer todos los clientes encontrados para convertirlos a formato JSON
            for (int i = 0; i < resultados.size(); i++) {
                // Obtener el cliente en la posición actual del bucle
                Cliente c = resultados.get(i);
                // Si no es el primer elemento, agregar una coma como separador JSON
                if (i > 0) json.append(",");
                // Construir el objeto JSON con los datos del cliente (id, nombre, apellido, cédula)
                json.append("{\"id\":").append(c.getIdCliente())
                    .append(",\"nombre\":\"").append(esc(c.getNombre())).append("\"")
                    .append(",\"apellido\":\"").append(esc(c.getApellido())).append("\"")
                    .append(",\"cedula\":\"").append(esc(c.getCedula())).append("\"}");
            }
            // Cerrar el arreglo JSON con el corchete de cierre
            json.append("]");
            // Escribir el JSON generado como respuesta HTTP al navegador
            response.getWriter().write(json.toString());
            // Salir del método porque ya se envió la respuesta AJAX
            return;
        }

        // Si no es búsqueda AJAX, verificar que el usuario sea administrador
        if (!verificarAdmin(request, response)) return;
        // Obtener la lista completa de clientes de la BD y pasarla como atributo a la vista
        request.setAttribute("clientes", clienteDAO.listar());
        // Reenviar la solicitud al archivo JSP que mostrará la tabla de clientes
        request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
    }

    /** Escapa caracteres especiales JSON básicos. */
    private String esc(String s) {
        // Si la cadena es nula, retornar cadena vacía para evitar errores
        if (s == null) return "";
        // Reemplazar caracteres que podrían romper la estructura JSON (barras, comillas, saltos de línea)
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Valida campos de cliente y retorna mensaje de error, o null si todo está bien. */
    private String validarCamposCliente(String nombre, String apellido, String cedula,
                                        String telefono, String email) {
        // Verificar que el nombre no sea nulo ni esté vacío (campo obligatorio)
        if (nombre == null || nombre.isBlank())   return "El nombre es obligatorio.";
        // Validar que el nombre solo contenga letras y espacios, con mínimo 2 caracteres
        if (!nombre.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El nombre solo puede contener letras (mínimo 2 caracteres).";

        // Verificar que el apellido no sea nulo ni esté vacío (campo obligatorio)
        if (apellido == null || apellido.isBlank()) return "El apellido es obligatorio.";
        // Validar que el apellido solo contenga letras y espacios, con mínimo 2 caracteres
        if (!apellido.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El apellido solo puede contener letras (mínimo 2 caracteres).";

        // Verificar que la cédula no sea nula ni esté vacía (campo obligatorio)
        if (cedula == null || cedula.isBlank())   return "La cédula es obligatoria.";
        // Validar que la cédula solo contenga dígitos numéricos, entre 8 y 15 dígitos
        if (!cedula.trim().matches("\\d{8,15}"))
            return "La cédula debe contener solo números (mínimo 8, máximo 15 dígitos).";

        // Validar el teléfono solo si fue proporcionado (es campo opcional)
        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("\\d{7,10}"))
            return "El teléfono debe contener solo números (mínimo 7, máximo 10 dígitos).";

        // Validar el email solo si fue proporcionado (es campo opcional)
        if (email != null && !email.isBlank() &&
            !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "El correo electrónico no es válido. Ejemplo: nombre@gmail.com";

        // Retornar null indica que no hubo errores, todos los campos son válidos
        return null;
    }

    /** Guarda o reemplaza el teléfono del cliente en la tabla Telefono. */
    private void sincronizarTelefono(int idCliente, String telefono, boolean esNuevo) {
        // Si el cliente NO es nuevo (ya existe), primero eliminar sus teléfonos anteriores
        if (!esNuevo) telefonoDAO.eliminarPorCliente(idCliente);
        // Si se proporcionó un número de teléfono válido, guardarlo en la BD
        if (telefono != null && !telefono.isBlank()) {
            // Crear un objeto Telefono con el número y el ID del cliente asociado
            modelos.Telefono t = new modelos.Telefono(0, telefono.trim(), idCliente);
            // Insertar el nuevo teléfono en la tabla Telefono de la base de datos
            telefonoDAO.agregar(t);
        }
    }

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Maneja las acciones: crear, actualizar y eliminar clientes.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario sea administrador antes de procesar cualquier acción
        if (!verificarAdmin(request, response)) return;
        // Establecer la codificación UTF-8 para leer correctamente caracteres especiales (ñ, tildes)
        request.setCharacterEncoding("UTF-8");

        // Obtener la acción que se quiere realizar (crear, actualizar o eliminar)
        String accion = request.getParameter("accion");

        // ===== ACCIÓN: CREAR un nuevo cliente =====
        if ("crear".equals(accion)) {
            // Leer el nombre del cliente desde el formulario enviado por el navegador
            String nombre   = request.getParameter("nombre");
            // Leer el apellido del cliente desde el formulario
            String apellido = request.getParameter("apellido");
            // Leer la cédula del cliente desde el formulario
            String cedula   = request.getParameter("cedula");
            // Leer el teléfono del cliente desde el formulario (campo opcional)
            String telefono = request.getParameter("telefono");
            // Leer el email del cliente desde el formulario (campo opcional)
            String email    = request.getParameter("email");

            // Validar todos los campos del cliente y obtener el mensaje de error (si lo hay)
            String errMsg = validarCamposCliente(nombre, apellido, cedula, telefono, email);
            // Si hubo un error de validación, mostrar el mensaje y no continuar
            if (errMsg != null) {
                // Cargar la lista de clientes para que la vista siga mostrando la tabla
                request.setAttribute("clientes", clienteDAO.listar());
                // Pasar el mensaje de error a la vista para que lo muestre al usuario
                request.setAttribute("error", errMsg);
                // Reenviar a la página de clientes mostrando el error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir del método sin crear el cliente
                return;
            }
            // Verificar si ya existe otro cliente con la misma cédula en la BD
            if (clienteDAO.cedulaExiste(cedula.trim())) {
                // Cargar la lista de clientes para la vista
                request.setAttribute("clientes", clienteDAO.listar());
                // Mostrar mensaje indicando que la cédula ya está registrada
                request.setAttribute("error", "Ya existe un cliente con esa cédula.");
                // Reenviar a la página de clientes con el mensaje de error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir del método sin crear el cliente duplicado
                return;
            }

            // Crear un nuevo objeto Cliente vacío para llenarlo con los datos del formulario
            Cliente c = new Cliente();
            // Asignar el nombre al cliente, quitando espacios en blanco al inicio y final
            c.setNombre(nombre.trim());
            // Asignar el apellido al cliente, quitando espacios innecesarios
            c.setApellido(apellido.trim());
            // Asignar la cédula al cliente, quitando espacios
            c.setCedula(cedula.trim());
            // Asignar el email al cliente (si es nulo, se deja como null)
            c.setEmail(email != null ? email.trim() : null);
            // Insertar el cliente en la BD y obtener el ID generado automáticamente
            int idNuevo = clienteDAO.crearYObtenerIdCliente(c);

            // Guardar el teléfono asociado al nuevo cliente en la tabla Telefono
            sincronizarTelefono(idNuevo, telefono, true);

        // ===== ACCIÓN: ACTUALIZAR un cliente existente =====
        } else if ("actualizar".equals(accion)) {
            // Leer el ID del cliente que se va a actualizar y convertirlo a número entero
            int    id       = Integer.parseInt(request.getParameter("idCliente"));
            // Proteger al cliente "Admin Tienda" de ser editado
            int idAdminTienda = clienteDAO.obtenerIdAdminTienda();
            if (idAdminTienda > 0 && id == idAdminTienda) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "El cliente 'Admin Tienda' no se puede modificar. Es un cliente del sistema.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            // Leer el nuevo nombre del cliente desde el formulario
            String nombre   = request.getParameter("nombre");
            // Leer el nuevo apellido del cliente desde el formulario
            String apellido = request.getParameter("apellido");
            // Leer la nueva cédula del cliente desde el formulario
            String cedula   = request.getParameter("cedula");
            // Leer el nuevo teléfono del cliente desde el formulario
            String telefono = request.getParameter("telefono");
            // Leer el nuevo email del cliente desde el formulario
            String email    = request.getParameter("email");

            // Validar todos los campos del cliente con las reglas definidas
            String errMsg = validarCamposCliente(nombre, apellido, cedula, telefono, email);
            // Si algún campo no pasó la validación, mostrar el error
            if (errMsg != null) {
                // Cargar la lista de clientes para que la tabla se siga mostrando
                request.setAttribute("clientes", clienteDAO.listar());
                // Pasar el mensaje de error al JSP
                request.setAttribute("error", errMsg);
                // Reenviar a la vista de clientes mostrando el error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir sin actualizar
                return;
            }
            // Verificar que la cédula no pertenezca a OTRO cliente (excluyendo al actual)
            if (clienteDAO.cedulaExisteExcluyendo(cedula.trim(), id)) {
                // Cargar la lista de clientes para la vista
                request.setAttribute("clientes", clienteDAO.listar());
                // Mostrar error de cédula duplicada
                request.setAttribute("error", "Ya existe otro cliente con esa cédula.");
                // Reenviar a la vista con el error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir sin actualizar
                return;
            }
            // Crear un objeto Cliente con los datos actualizados usando el constructor con parámetros
            Cliente c = new Cliente(id, nombre.trim(), apellido.trim(), cedula.trim());
            // Asignar el email actualizado al objeto cliente
            c.setEmail(email != null ? email.trim() : null);
            // Ejecutar la actualización del cliente en la base de datos
            clienteDAO.actualizar(c);

            // Eliminar teléfonos anteriores y guardar el nuevo teléfono del cliente
            sincronizarTelefono(id, telefono, false);

        // ===== ACCIÓN: ELIMINAR un cliente =====
        } else if ("eliminar".equals(accion)) {
            // Leer el ID del cliente que se quiere eliminar y convertirlo a entero
            int id = Integer.parseInt(request.getParameter("idCliente"));
            // Proteger al cliente "Admin Tienda" de ser eliminado
            int idAdminTienda = clienteDAO.obtenerIdAdminTienda();
            if (idAdminTienda > 0 && id == idAdminTienda) {
                request.setAttribute("clientes", clienteDAO.listar());
                request.setAttribute("error", "El cliente 'Admin Tienda' no se puede eliminar. Es un cliente del sistema.");
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                return;
            }
            // Verificar si el cliente tiene pedidos/ventas registradas (no se puede eliminar)
            if (clienteDAO.tienePedidos(id)) {
                // Cargar la lista de clientes para la vista
                request.setAttribute("clientes", clienteDAO.listar());
                // Mostrar error indicando que no se puede eliminar por tener ventas asociadas
                request.setAttribute("error", "No se puede eliminar este cliente porque tiene ventas registradas en el historial.");
                // Reenviar a la vista con el error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir sin eliminar
                return;
            }
            // Intentar eliminar el cliente de la BD; si falla, mostrar error
            if (!clienteDAO.eliminar(id)) {
                // Cargar la lista de clientes para la vista
                request.setAttribute("clientes", clienteDAO.listar());
                // Mostrar error genérico de eliminación fallida
                request.setAttribute("error", "No se pudo eliminar el cliente. Intenta de nuevo.");
                // Reenviar a la vista con el error
                request.getRequestDispatcher("/view/clientes.jsp").forward(request, response);
                // Salir del método
                return;
            }
        }

        // Redirigir al listado de clientes para que se actualice la vista después de la acción
        response.sendRedirect(request.getContextPath() + "/ClienteControlador");
    }
}

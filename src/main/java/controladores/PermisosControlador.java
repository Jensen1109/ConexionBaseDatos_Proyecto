// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase PermisosDAO para acceder a la base de datos de permisos y roles
import dao.PermisosDAO;
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
 * Controlador de gestión de permisos por rol (tablas Permisos, Rol y rol_permiso).
 * Implementa la interfaz de administración del sistema RBAC (Role-Based Access Control).
 * Permite al administrador ver todos los permisos, seleccionar un rol y asignar/quitar
 * permisos a ese rol mediante la tabla intermedia rol_permiso.
 * Solo los administradores (id_rol=1) pueden acceder a este módulo.
 */
// Mapear este servlet a la URL "/PermisosControlador" para que responda a esa ruta
@WebServlet("/PermisosControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class PermisosControlador extends HttpServlet {

    // Crear una instancia de PermisosDAO para gestionar permisos y roles en la BD
    private final PermisosDAO permisosDAO = new PermisosDAO();

    // ═══════════════════════════════════════════════════
    // GET: Listar permisos y roles, filtrar permisos por rol
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Lista todos los permisos del sistema y todos los roles disponibles.
     * Si se envía el parámetro ?idRol=X, también carga los permisos asignados a ese rol
     * para marcar los checkboxes correspondientes en la vista.
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

        // Obtener la lista completa de permisos del sistema y pasarla a la vista
        request.setAttribute("permisos", permisosDAO.listarTodos());
        // Obtener la lista completa de roles del sistema y pasarla a la vista
        request.setAttribute("roles",    permisosDAO.listarRoles());

        // Leer el parámetro "idRol" de la URL (ej: ?idRol=2) para filtrar permisos por rol
        String idRolParam = request.getParameter("idRol");
        // Si se envió un ID de rol válido, cargar los permisos asignados a ese rol
        if (idRolParam != null && !idRolParam.isBlank()) {
            // Convertir el ID del rol de texto a número entero
            int idRol = Integer.parseInt(idRolParam);
            // Pasar el ID del rol seleccionado a la vista para mantener la selección
            request.setAttribute("idRolSeleccionado", idRol);
            // Obtener los IDs de los permisos asignados a ese rol (para marcar checkboxes)
            request.setAttribute("permisosDelRol", permisosDAO.listarIdPermisosPorRol(idRol));
        }

        // Reenviar la solicitud al archivo JSP que mostrará la gestión de permisos
        request.getRequestDispatcher("/WEB-INF/view/permisos.jsp").forward(request, response);
    }

    // ═══════════════════════════════════════════════════
    // POST: Asignar o quitar permiso a un rol
    // ═══════════════════════════════════════════════════

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Recibe el ID del rol, el ID del permiso y la acción ("quitar" o asignar por defecto).
     * Inserta o elimina un registro en la tabla intermedia rol_permiso.
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

        // Leer el ID del rol al que se le va a asignar/quitar el permiso
        int idRol     = Integer.parseInt(request.getParameter("idRol"));
        // Leer el ID del permiso que se va a asignar o quitar
        int idPermiso = Integer.parseInt(request.getParameter("idPermiso"));
        // Leer la acción: "quitar" para eliminar el permiso, cualquier otro valor para asignarlo
        String accion = request.getParameter("accion");

        // ===== ACCIÓN: QUITAR un permiso del rol =====
        if ("quitar".equals(accion)) {
            // Eliminar el registro de la tabla rol_permiso (quitar permiso del rol)
            permisosDAO.quitarPermiso(idRol, idPermiso);

        // ===== ACCIÓN: ASIGNAR un permiso al rol =====
        } else {
            // Insertar un registro en la tabla rol_permiso (asignar permiso al rol)
            permisosDAO.asignarPermiso(idRol, idPermiso);
        }

        // Redirigir al listado de permisos manteniendo el rol seleccionado
        response.sendRedirect(request.getContextPath() +
                "/PermisosControlador?idRol=" + idRol);
    }
}

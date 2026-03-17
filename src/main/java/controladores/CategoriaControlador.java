// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase CategoriaDAO para acceder a la base de datos de categorías
import dao.CategoriaDAO;
// Importar el modelo Categoria que representa la entidad categoría en la BD
import modelos.Categoria;
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
 * Controlador CRUD de categorías (tabla Categoria).
 * Maneja: listar todas las categorías, crear nueva categoría y eliminar categoría.
 * Solo los administradores (id_rol=1) pueden acceder a este módulo.
 */
// Mapear este servlet a la URL "/CategoriaControlador" para que responda a esa ruta
@WebServlet("/CategoriaControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class CategoriaControlador extends HttpServlet {

    // Crear una instancia de CategoriaDAO para realizar operaciones CRUD de categorías en la BD
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // ═══════════════════════════════════════════════════
    // GET: Listar todas las categorías
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Lista todas las categorías registradas y las muestra en la vista.
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

        // Obtener la lista completa de categorías de la BD y pasarla como atributo a la vista
        request.setAttribute("categorias", categoriaDAO.listarTodas());
        // Reenviar la solicitud al archivo JSP que mostrará la tabla de categorías
        request.getRequestDispatcher("/WEB-INF/view/categorias.jsp").forward(request, response);
    }

    // ═══════════════════════════════════════════════════
    // POST: Crear o eliminar categoría
    // ═══════════════════════════════════════════════════

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Maneja las acciones: crear nueva categoría y eliminar categoría existente.
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

        // Obtener la acción que se quiere realizar ("eliminar", "crearAjax" o crear por defecto)
        String accion = request.getParameter("accion");

        // ===== ACCIÓN: CREAR CATEGORÍA VÍA AJAX (desde formulario de producto) =====
        // Esta acción se llama desde registroProducto.jsp y editarproducto.jsp
        // cuando el admin quiere crear una categoría al vuelo sin salir del formulario
        if ("crearAjax".equals(accion)) {
            // Establecer el tipo de respuesta como JSON con codificación UTF-8
            response.setContentType("application/json; charset=UTF-8");
            // Evitar que el navegador guarde en caché la respuesta AJAX
            response.setHeader("Cache-Control", "no-store");

            // Leer el nombre de la nueva categoría desde la petición AJAX
            String nombre = request.getParameter("nombre");
            // Validar que el nombre no sea nulo ni esté vacío
            if (nombre == null || nombre.isBlank()) {
                // Si no se envió nombre, devolver error en JSON
                response.getWriter().write("{\"ok\":false,\"msg\":\"El nombre es obligatorio.\"}");
                return;
            }

            // Crear un objeto Categoria con el nombre recibido
            Categoria c = new Categoria();
            c.setNombre(nombre.trim());
            // Insertar en la BD y obtener el ID auto-generado
            int nuevoId = categoriaDAO.crearYObtenerId(c);

            if (nuevoId > 0) {
                // Éxito: devolver JSON con el ID y nombre de la nueva categoría
                // El JavaScript del formulario usará estos datos para agregar la opción al select
                response.getWriter().write("{\"ok\":true,\"id\":" + nuevoId +
                        ",\"nombre\":\"" + nombre.trim().replace("\"", "\\\"") + "\"}");
            } else {
                // Error al insertar: devolver JSON con mensaje de error
                response.getWriter().write("{\"ok\":false,\"msg\":\"No se pudo crear la categoría. Puede que ya exista.\"}");
            }
            // Salir del método sin redirigir (es una respuesta AJAX, no una navegación)
            return;
        }

        // ===== ACCIÓN: ELIMINAR una categoría =====
        if ("eliminar".equals(accion)) {
            // Leer el ID de la categoría a eliminar y convertirlo a número entero
            int id = Integer.parseInt(request.getParameter("id"));
            // Ejecutar la eliminación de la categoría en la base de datos
            categoriaDAO.eliminar(id);

        // ===== ACCIÓN: CREAR una nueva categoría (formulario normal) =====
        } else {
            // Leer el nombre de la nueva categoría desde el formulario
            String nombre = request.getParameter("nombre");
            // Validar que el nombre no sea nulo ni esté vacío
            if (nombre != null && !nombre.isBlank()) {
                // Crear un nuevo objeto Categoria vacío
                Categoria c = new Categoria();
                // Asignar el nombre quitando espacios en blanco al inicio y final
                c.setNombre(nombre.trim());
                // Insertar la nueva categoría en la base de datos
                categoriaDAO.crear(c);
            }
        }

        // Redirigir al listado de categorías para que se actualice la vista
        response.sendRedirect(request.getContextPath() + "/CategoriaControlador");
    }
}

// Declaración del paquete al que pertenece esta clase (agrupación de controladores)
package controladores;

// Importar UsuarioDAO para realizar operaciones CRUD de usuarios en la base de datos
import dao.UsuarioDAO;
// Importar el modelo Usuario que representa un usuario del sistema (admin o empleado)
import modelos.Usuario;
// Importar ServletException para manejar errores internos del servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para registrar este servlet con una URL específica
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet, la clase base para crear controladores HTTP
import jakarta.servlet.http.HttpServlet;
// Importar HttpServletRequest para leer los datos enviados por el navegador
import jakarta.servlet.http.HttpServletRequest;
// Importar HttpServletResponse para enviar la respuesta al navegador
import jakarta.servlet.http.HttpServletResponse;
// Importar HttpSession para acceder a la sesión del usuario autenticado
import jakarta.servlet.http.HttpSession;
// Importar IOException para manejar errores de entrada/salida
import java.io.IOException;

/**
 * Controlador de gestión de usuarios del sistema (admins y empleados).
 * Solo los administradores pueden registrar, editar y eliminar usuarios.
 * RF01: solo admin puede crear nuevos usuarios.
 */
// Mapear este servlet a la URL "/UsuarioControlador" para que el servidor lo reconozca
@WebServlet("/UsuarioControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class UsuarioControlador extends HttpServlet {

    // Crear una instancia de UsuarioDAO para realizar operaciones de usuarios en la BD
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Método privado que verifica si el usuario actual es administrador (idRol=1).
     * Si no es admin o no está logueado, lo redirige y retorna false.
     */
    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Configurar cabecera para evitar que el navegador guarde la página en caché
        response.setHeader("Cache-Control", "no-store");
        // Cabecera adicional para navegadores antiguos, previene el uso de caché
        response.setHeader("Pragma", "no-cache");
        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        // Verificar si no hay sesión activa o si el usuario no ha iniciado sesión
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Redirigir al formulario de login porque no hay usuario autenticado
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            // Retornar false indicando que NO tiene acceso
            return false;
        }
        // Obtener el objeto Usuario almacenado en la sesión (el usuario logueado)
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        // Verificar si el rol del usuario NO es 1 (1 = administrador)
        if (u.getIdRol() != 1) {
            // Si no es admin, redirigir a la página de productos (acceso denegado)
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            // Retornar false indicando que NO es administrador
            return false;
        }
        // Si pasó todas las verificaciones, retornar true (sí es administrador)
        return true;
    }

    /** GET: lista todos los usuarios del sistema o muestra formulario de registro. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario sea administrador; si no lo es, salir del método
        if (!verificarAdmin(request, response)) return;

        // Leer el parámetro "accion" de la URL para determinar qué vista mostrar
        String accion = request.getParameter("accion");

        // Si la acción es "nuevo", mostrar el formulario para registrar un nuevo usuario
        if ("nuevo".equals(accion)) {
            // Formulario para registrar un nuevo usuario (admin o empleado)
            // Reenviar la solicitud al JSP del formulario de registro
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
        } else {
            // Si no hay acción específica, mostrar la lista completa de usuarios
            // Obtener todos los usuarios de la BD y pasarlos como atributo a la vista
            request.setAttribute("usuarios", usuarioDAO.listar());
            // Reenviar al JSP que muestra la tabla de gestión de usuarios
            request.getRequestDispatcher("/view/gestionUsuarios.jsp").forward(request, response);
        }
    }

    /** POST: crear, actualizar o eliminar un usuario. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario sea administrador antes de procesar cualquier acción
        if (!verificarAdmin(request, response)) return;
        // Establecer codificación UTF-8 para leer correctamente caracteres especiales (ñ, tildes)
        request.setCharacterEncoding("UTF-8");

        // Leer la acción que se quiere realizar (registrar, actualizar o eliminar)
        String accion = request.getParameter("accion");

        // ===== ACCIÓN: REGISTRAR un nuevo usuario =====
        if ("registrar".equals(accion)) {
            // Leer el nombre del nuevo usuario desde el formulario
            String nombre     = request.getParameter("nombre");
            // Leer el apellido del nuevo usuario desde el formulario
            String apellido   = request.getParameter("apellido");
            // Leer el email del nuevo usuario desde el formulario
            String email      = request.getParameter("email");
            // Leer la contraseña en texto plano (se encriptará con BCrypt en el DAO)
            String contrasena = request.getParameter("contrasena");
            // Leer la cédula del nuevo usuario desde el formulario
            String cedula     = request.getParameter("cedula");
            // Leer el rol seleccionado (1=admin, 2=empleado) como texto
            String rolStr     = request.getParameter("idRol");

            // Validación de campos obligatorios: verificar que ninguno esté vacío ni nulo
            if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank() ||
                email == null  || email.isBlank()  || contrasena == null || contrasena.isBlank() ||
                cedula == null || cedula.isBlank()  || rolStr == null || rolStr.isBlank()) {
                // Establecer mensaje de error para que se muestre en la vista
                request.setAttribute("error", "Por favor completa todos los campos.");
                // Reenviar al formulario de registro mostrando el error
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                // Salir del método sin registrar el usuario
                return;
            }

            // Verificar si ya existe otro usuario con el mismo email en la BD
            // Validar unicidad de email
            if (usuarioDAO.emailExiste(email)) {
                // Mostrar error indicando que el email ya está en uso
                request.setAttribute("error", "Este email ya está registrado.");
                // Reenviar al formulario de registro con el error
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                // Salir sin registrar
                return;
            }

            // Verificar si ya existe otro usuario con la misma cédula en la BD
            // Validar unicidad de cédula
            if (usuarioDAO.cedulaExiste(cedula)) {
                // Mostrar error indicando que la cédula ya está registrada
                request.setAttribute("error", "Esta cédula ya está registrada.");
                // Reenviar al formulario de registro con el error
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                // Salir sin registrar
                return;
            }

            // Crear un nuevo objeto Usuario vacío para llenarlo con los datos del formulario
            Usuario u = new Usuario();
            // Asignar el nombre al objeto usuario
            u.setNombre(nombre);
            // Asignar el apellido al objeto usuario
            u.setApellido(apellido);
            // Asignar el email al objeto usuario
            u.setEmail(email);
            // Asignar la cédula al objeto usuario
            u.setCedula(cedula);
            // Convertir el rol de texto a entero y asignarlo al usuario
            u.setIdRol(Integer.parseInt(rolStr));

            // Registrar el usuario en la BD; el DAO encripta la contraseña con BCrypt
            boolean ok = usuarioDAO.registrar(u, contrasena);
            // Si el registro falló (error en la BD u otro problema)
            if (!ok) {
                // Mostrar mensaje de error genérico
                request.setAttribute("error", "Error al registrar el usuario. Intenta de nuevo.");
                // Reenviar al formulario de registro con el error
                request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
                // Salir del método
                return;
            }

        // ===== ACCIÓN: ACTUALIZAR un usuario existente =====
        } else if ("actualizar".equals(accion)) {
            // Leer el ID del usuario a actualizar y convertirlo a número entero
            int    id       = Integer.parseInt(request.getParameter("idUsuario"));
            // Leer el nuevo nombre del usuario desde el formulario
            String nombre   = request.getParameter("nombre");
            // Leer el nuevo apellido del usuario desde el formulario
            String apellido = request.getParameter("apellido");
            // Leer el nuevo email del usuario desde el formulario
            String email    = request.getParameter("email");
            // Leer el nuevo rol del usuario y convertirlo a entero
            int    rol      = Integer.parseInt(request.getParameter("idRol"));

            // Crear un objeto Usuario con los datos actualizados
            Usuario u = new Usuario();
            // Asignar el ID del usuario que se va a actualizar
            u.setIdUsuario(id);
            // Asignar el nuevo nombre
            u.setNombre(nombre);
            // Asignar el nuevo apellido
            u.setApellido(apellido);
            // Asignar el nuevo email
            u.setEmail(email);
            // Asignar el nuevo rol (1=admin, 2=empleado)
            u.setIdRol(rol);
            // Ejecutar la actualización del usuario en la base de datos
            usuarioDAO.actualizar(u);

        // ===== ACCIÓN: ELIMINAR un usuario =====
        } else if ("eliminar".equals(accion)) {
            // Leer el ID del usuario que se quiere eliminar y convertirlo a entero
            int id = Integer.parseInt(request.getParameter("idUsuario"));
            // Obtener el usuario actualmente logueado desde la sesión
            // No permitir que el admin se elimine a sí mismo
            Usuario actual = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
            // Comparar si el ID a eliminar es el mismo que el del usuario logueado
            if (actual.getIdUsuario() == id) {
                // Cargar la lista de usuarios para que la tabla se siga mostrando
                request.setAttribute("usuarios", usuarioDAO.listar());
                // Mostrar error: un admin no puede eliminarse a sí mismo por seguridad
                request.setAttribute("error", "No puedes eliminar tu propio usuario.");
                // Reenviar a la vista de gestión de usuarios con el error
                request.getRequestDispatcher("/view/gestionUsuarios.jsp").forward(request, response);
                // Salir sin eliminar
                return;
            }
            // Si no es el mismo usuario, proceder a eliminarlo de la base de datos
            usuarioDAO.eliminar(id);
        }

        // Redirigir al listado de usuarios para actualizar la vista después de la acción
        response.sendRedirect(request.getContextPath() + "/UsuarioControlador");
    }
}

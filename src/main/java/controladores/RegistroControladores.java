package controladores;

// Importar el DAO que accede a la tabla Usuario en la BD
import dao.UsuarioDAO;
// Importar el modelo Usuario que representa un usuario del sistema
import modelos.Usuario;
// Importaciones de Jakarta Servlet para manejar peticiones HTTP
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;  // Para registrar la URL del servlet
import jakarta.servlet.http.HttpServlet;        // Clase base de todos los servlets
import jakarta.servlet.http.HttpServletRequest; // Objeto con los datos de la petición del navegador
import jakarta.servlet.http.HttpServletResponse;// Objeto para enviar la respuesta al navegador
import java.io.IOException;

/**
 * Controlador de registro de usuarios nuevos.
 * Permite crear cuentas con rol empleado (id_rol=2) desde el formulario público.
 * Valida campos obligatorios, email único y cédula única antes de registrar.
 * La contraseña se cifra con BCrypt en el DAO.
 */
@WebServlet("/RegistroControlador")
public class RegistroControladores extends HttpServlet {

    // Instancia del DAO para consultar y registrar usuarios en la BD
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * GET: Muestra el formulario de registro (registro.jsp).
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // forward() envía la petición al JSP para que se muestre el formulario
        request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
    }

    /**
     * POST: Procesa el formulario de registro cuando el usuario da click en "Registrarse".
     * 1. Lee los campos del formulario
     * 2. Valida que no estén vacíos
     * 3. Verifica que el email no esté registrado
     * 4. Verifica que la cédula no esté registrada
     * 5. Crea el usuario con rol empleado y contraseña cifrada
     * @param request  solicitud HTTP con los datos del formulario
     * @param response respuesta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leer los datos que el usuario escribió en cada campo del formulario
        String nombre     = request.getParameter("nombre");     // Campo "nombre"
        String apellido   = request.getParameter("apellido");   // Campo "apellido"
        String email      = request.getParameter("email");      // Campo "email"
        String contrasena = request.getParameter("contrasena"); // Campo "contrasena"
        String cedula     = request.getParameter("cedula");     // Campo "cedula"

        // Validación: verificar que NINGÚN campo esté vacío o nulo
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            email == null || email.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            cedula == null || cedula.isBlank()) {

            // Si algún campo está vacío, enviar error al JSP y volver al formulario
            request.setAttribute("error", "Por favor completa todos los campos.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return; // Salir, no procesar más
        }

        // Verificar si ya existe un usuario con ese email en la BD
        if (usuarioDAO.emailExiste(email)) {
            // El email ya está registrado → mostrar error
            request.setAttribute("error", "Este email ya está registrado.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return;
        }

        // Verificar si ya existe un usuario con esa cédula en la BD
        if (usuarioDAO.cedulaExiste(cedula)) {
            // La cédula ya está registrada → mostrar error
            request.setAttribute("error", "Esta cédula ya está registrada.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
            return;
        }

        // Crear el objeto Usuario con los datos del formulario
        Usuario u = new Usuario();
        u.setIdRol(2);          // Rol 2 = empleado (el registro público siempre crea empleados)
        u.setNombre(nombre);    // Guardar nombre
        u.setApellido(apellido);// Guardar apellido
        u.setEmail(email);      // Guardar email
        u.setCedula(cedula);    // Guardar cédula

        // Llamar al DAO para insertar el usuario en la BD
        // El DAO se encarga de cifrar la contraseña con BCrypt antes de guardarla
        boolean ok = usuarioDAO.registrar(u, contrasena);

        if (ok) {
            // Registro exitoso → redirigir al login para que inicie sesión
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
        } else {
            // Error al insertar en la BD → mostrar error
            request.setAttribute("error", "Error al registrar. Intenta de nuevo.");
            request.getRequestDispatcher("/view/registro.jsp").forward(request, response);
        }
    }
}

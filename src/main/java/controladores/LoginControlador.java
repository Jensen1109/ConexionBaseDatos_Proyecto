package controladores;

// Importar el DAO que accede a la tabla Usuario en la base de datos
import dao.UsuarioDAO;
// Importar el modelo Usuario que representa un usuario del sistema
import modelos.Usuario;
// Importaciones de Jakarta Servlet para manejar peticiones HTTP
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;  // Para registrar la URL del servlet
import jakarta.servlet.http.HttpServlet;        // Clase base de todos los servlets
import jakarta.servlet.http.HttpServletRequest; // Objeto con los datos de la petición
import jakarta.servlet.http.HttpServletResponse;// Objeto para enviar la respuesta
import jakarta.servlet.http.HttpSession;        // Sesión del usuario en el servidor
import java.io.IOException;

/**
 * Controlador de autenticación (Login).
 * Acepta login con email O cédula + contraseña.
 * Redirige según rol: Admin → ProductoControlador, Empleado → PedidoControlador?accion=nuevo
 *
 * @WebServlet("/LoginControlador") = este servlet atiende la URL /LoginControlador
 */
@WebServlet("/LoginControlador")
public class LoginControlador extends HttpServlet {

    // Crear una instancia del DAO para poder consultar usuarios en la BD
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Método GET: se ejecuta cuando el usuario ENTRA a la página de login (escribe la URL o lo redirigen).
     * Si ya tiene sesión activa, lo manda directo al dashboard sin mostrar el login.
     * @param request  solicitud HTTP con datos del navegador
     * @param response respuesta HTTP que se enviará al navegador
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Headers anti-caché: le dice al navegador que NO guarde esta página en memoria
        // Así, al darle "atrás" después de cerrar sesión, no ve la página anterior
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        // Verificar si ya hay una sesión activa (false = no crear una nueva si no existe)
        HttpSession session = request.getSession(false);
        // Si la sesión existe Y tiene un usuario guardado, ya está logueado
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            // Redirigir al dashboard según su rol (admin o empleado)
            redirigirPorRol(request, response,
                    (Usuario) session.getAttribute("usuarioLogueado"));
            return; // Salir del método, no mostrar el formulario de login
        }

        // Si NO está logueado, mostrar la página de login
        // forward() envía la petición al JSP sin cambiar la URL del navegador
        request.getRequestDispatcher("/view/login.jsp").forward(request, response);
    }

    /**
     * Método POST: se ejecuta cuando el usuario envía el formulario de login (click en "Iniciar sesión").
     * Recibe email/cédula y contraseña, los valida contra la BD y crea la sesión si son correctos.
     * @param request  solicitud HTTP con los parámetros "email" y "contrasena" del formulario
     * @param response respuesta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Headers anti-caché para la respuesta del POST también
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        // Leer los datos que el usuario escribió en el formulario de login
        String identificador = request.getParameter("email");      // Campo "email" (puede ser email o cédula)
        String contrasena    = request.getParameter("contrasena");  // Campo "contrasena"

        // Validar que ambos campos no estén vacíos
        if (identificador == null || identificador.isBlank() ||
            contrasena    == null || contrasena.isBlank()) {
            // Si están vacíos, enviar mensaje de error al JSP
            request.setAttribute("error", "Por favor completa todos los campos.");
            // Reenviar al login mostrando el error
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
            return; // Salir, no procesar más
        }

        // Detectar si el usuario escribió un email o una cédula
        // Si contiene @ es un email, si no es una cédula (solo números)
        Usuario usuario;
        if (identificador.trim().contains("@")) {
            // Buscar en BD por email y verificar contraseña con BCrypt
            usuario = usuarioDAO.login(identificador.trim(), contrasena);
        } else {
            // Buscar en BD por cédula y verificar contraseña con BCrypt
            usuario = usuarioDAO.loginPorCedula(identificador.trim(), contrasena);
        }

        if (usuario != null) {
            // LOGIN EXITOSO: las credenciales son correctas

            // Crear una sesión nueva para este usuario
            HttpSession session = request.getSession();
            // Guardar el objeto Usuario completo en la sesión (se usa en todo el sistema)
            session.setAttribute("usuarioLogueado", usuario);
            // Guardar el nombre por separado (para mostrarlo en el sidebar)
            session.setAttribute("nombreUsuario",   usuario.getNombre());
            // Guardar el rol por separado (para verificar permisos rápidamente)
            session.setAttribute("rolUsuario",      usuario.getIdRol());
            // La sesión expira después de 30 minutos de inactividad (30 * 60 = 1800 segundos)
            session.setMaxInactiveInterval(30 * 60);

            // Redirigir al dashboard según el rol del usuario
            redirigirPorRol(request, response, usuario);
        } else {
            // LOGIN FALLIDO: email/cédula no existe o contraseña incorrecta

            // Enviar mensaje de error al JSP para que lo muestre
            request.setAttribute("error", "Credenciales incorrectas. Verifica email/cédula y contraseña.");
            // Reenviar al formulario de login con el mensaje de error
            request.getRequestDispatcher("/view/login.jsp").forward(request, response);
        }
    }

    /**
     * Redirige al usuario al dashboard que le corresponde según su rol.
     * Admin (id_rol=1) va a ver productos, Empleado (id_rol=2) va a registrar ventas.
     * @param request  solicitud HTTP
     * @param response respuesta HTTP
     * @param usuario  usuario autenticado con su rol
     */
    private void redirigirPorRol(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Usuario usuario) throws IOException {
        if (usuario.getIdRol() == 1) {
            // Si es ADMIN (rol 1) → ir a la lista de productos
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
        } else {
            // Si es EMPLEADO (rol 2) → ir a registrar una venta nueva
            response.sendRedirect(request.getContextPath() + "/PedidoControlador?accion=nuevo");
        }
    }
}

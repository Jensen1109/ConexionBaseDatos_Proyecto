// Declaración del paquete al que pertenece esta clase (agrupación de controladores)
package controladores;

// Importar UsuarioDAO para acceder a operaciones de usuario en la base de datos
import dao.UsuarioDAO;
// Importar el modelo Usuario para trabajar con los datos del usuario logueado
import modelos.Usuario;
// Importar BCrypt para verificar y encriptar contraseñas de forma segura
import org.mindrot.jbcrypt.BCrypt;
// Importar ServletException para manejar errores internos del servlet
import jakarta.servlet.ServletException;
// Importar la anotación WebServlet para mapear este controlador a una URL
import jakarta.servlet.annotation.WebServlet;
// Importar HttpServlet, la clase base para crear controladores HTTP en Jakarta
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
 * Controlador de perfil propio.
 * Cualquier usuario autenticado puede ver y editar sus propios datos.
 */
// Mapear este servlet a la URL "/PerfilControlador" para que el servidor lo reconozca
@WebServlet("/PerfilControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class PerfilControlador extends HttpServlet {

    // Crear una instancia de UsuarioDAO para realizar operaciones de usuario en la BD
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Método privado que verifica si hay una sesión activa con un usuario logueado.
     * No verifica rol, porque cualquier usuario puede editar su propio perfil.
     * Retorna true si hay sesión, false si no.
     */
    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Configurar cabecera para evitar que el navegador guarde la página en caché
        res.setHeader("Cache-Control", "no-store");
        // Cabecera adicional para navegadores antiguos, previene el uso de caché
        res.setHeader("Pragma", "no-cache");
        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession s = req.getSession(false);
        // Verificar si no hay sesión o si no hay un usuario logueado en la sesión
        if (s == null || s.getAttribute("usuarioLogueado") == null) {
            // Redirigir al login porque no hay sesión activa
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            // Retornar false indicando que NO hay sesión válida
            return false;
        }
        // Si hay sesión con usuario logueado, retornar true
        return true;
    }

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Muestra la página del perfil del usuario con sus datos actuales.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Verificar que exista una sesión activa; si no, redirigir al login
        if (!verificarSesion(request, response)) return;
        // Reenviar la solicitud al JSP que muestra el formulario de perfil del usuario
        request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
    }

    /**
     * Método doPost: se ejecuta cuando el usuario envía el formulario de edición de perfil.
     * Actualiza nombre, apellido, email y opcionalmente la contraseña.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Verificar que exista una sesión activa antes de procesar el formulario
        if (!verificarSesion(request, response)) return;
        // Establecer codificación UTF-8 para leer correctamente caracteres especiales (ñ, tildes)
        request.setCharacterEncoding("UTF-8");

        // Obtener la sesión actual para acceder al usuario logueado
        HttpSession session  = request.getSession(false);
        // Obtener el objeto Usuario almacenado en la sesión (datos actuales del usuario)
        Usuario actual       = (Usuario) session.getAttribute("usuarioLogueado");
        // Guardar el ID del usuario logueado para usarlo en las consultas a la BD
        int     idUsuario    = actual.getIdUsuario();

        // Leer el nombre ingresado en el formulario de perfil
        String nombre    = request.getParameter("nombre");
        // Leer el apellido ingresado en el formulario de perfil
        String apellido  = request.getParameter("apellido");
        // Leer el email ingresado en el formulario de perfil
        String email     = request.getParameter("email");

        // Validación básica: verificar que nombre, apellido y email no estén vacíos
        // Validación básica
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            email == null || email.isBlank()) {
            // Establecer mensaje de error indicando que los campos son obligatorios
            request.setAttribute("error", "Nombre, apellido y email son obligatorios.");
            // Reenviar al formulario de perfil mostrando el error
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            // Salir del método sin actualizar nada
            return;
        }

        // Verificar que el email no esté en uso por OTRO usuario (excluyendo al usuario actual)
        // Email único (excluyendo el propio)
        if (usuarioDAO.emailExisteExcluyendo(email.trim(), idUsuario)) {
            // Mostrar error indicando que otro usuario ya usa ese email
            request.setAttribute("error", "Ese email ya está en uso por otro usuario.");
            // Reenviar al formulario de perfil con el error
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            // Salir sin actualizar
            return;
        }

        // Ejecutar la actualización de nombre, apellido y email en la base de datos
        // Actualizar datos básicos
        boolean ok = usuarioDAO.actualizarPerfil(idUsuario, nombre.trim(), apellido.trim(), email.trim());
        // Si la actualización falló (error en la BD u otro problema)
        if (!ok) {
            // Mostrar mensaje de error genérico
            request.setAttribute("error", "No se pudo actualizar el perfil. Intenta de nuevo.");
            // Reenviar al formulario de perfil con el error
            request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
            // Salir del método
            return;
        }

        // ===== CAMBIO DE CONTRASEÑA (sección opcional) =====
        // Leer la contraseña actual ingresada por el usuario (para verificar identidad)
        // Cambio de contraseña (opcional)
        String contrasenaActual = request.getParameter("contrasenaActual");
        // Leer la nueva contraseña que el usuario quiere establecer
        String contrasenaNueva  = request.getParameter("contrasenaNueva");
        // Leer la confirmación de la nueva contraseña (debe coincidir con la anterior)
        String confirmar        = request.getParameter("confirmar");

        // Solo procesar el cambio de contraseña si el usuario llenó el campo de contraseña actual
        if (contrasenaActual != null && !contrasenaActual.isBlank()) {
            // Obtener el hash de la contraseña actual almacenado en la BD
            String hashActual = usuarioDAO.obtenerHashContrasena(idUsuario);
            // Verificar que el hash existe y que la contraseña ingresada coincide con el hash
            if (hashActual == null || !BCrypt.checkpw(contrasenaActual, hashActual)) {
                // La contraseña actual no coincide; actualizar los datos en la sesión
                // porque el nombre/apellido/email ya se guardaron exitosamente
                // Actualizar sesión con datos ya guardados antes de mostrar el error
                actual.setNombre(nombre.trim());
                actual.setApellido(apellido.trim());
                actual.setEmail(email.trim());
                // Guardar el objeto actualizado en la sesión
                session.setAttribute("usuarioLogueado", actual);
                // Mostrar mensaje de éxito para los datos básicos que sí se actualizaron
                request.setAttribute("exito", "Datos actualizados.");
                // Mostrar mensaje de error para la contraseña incorrecta
                request.setAttribute("error", "La contraseña actual es incorrecta.");
                // Reenviar al formulario de perfil mostrando ambos mensajes
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                // Salir del método sin cambiar la contraseña
                return;
            }
            // Verificar que la nueva contraseña tenga al menos 6 caracteres de longitud
            if (contrasenaNueva == null || contrasenaNueva.length() < 6) {
                // Mostrar error si la contraseña nueva es demasiado corta
                request.setAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres.");
                // Reenviar al formulario de perfil con el error
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                // Salir sin cambiar la contraseña
                return;
            }
            // Verificar que la nueva contraseña coincida con la confirmación
            if (!contrasenaNueva.equals(confirmar)) {
                // Mostrar error si las contraseñas no coinciden
                request.setAttribute("error", "Las contraseñas nuevas no coinciden.");
                // Reenviar al formulario de perfil con el error
                request.getRequestDispatcher("/view/perfil.jsp").forward(request, response);
                // Salir sin cambiar la contraseña
                return;
            }
            // Generar un nuevo hash BCrypt con factor de costo 12 para encriptar la nueva contraseña
            String nuevoHash = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));
            // Actualizar la contraseña encriptada en la base de datos
            usuarioDAO.actualizarContrasena(idUsuario, nuevoHash);
        }

        // ===== ACTUALIZAR DATOS EN LA SESIÓN =====
        // Actualizar el nombre en el objeto de sesión para reflejar los cambios sin reloguear
        // Actualizar datos en sesión
        actual.setNombre(nombre.trim());
        // Actualizar el apellido en el objeto de sesión
        actual.setApellido(apellido.trim());
        // Actualizar el email en el objeto de sesión
        actual.setEmail(email.trim());
        // Guardar el objeto Usuario actualizado de vuelta en la sesión
        session.setAttribute("usuarioLogueado", actual);

        // Redirigir al perfil con parámetro "exito=1" para mostrar mensaje de confirmación
        response.sendRedirect(request.getContextPath() + "/PerfilControlador?exito=1");
    }
}

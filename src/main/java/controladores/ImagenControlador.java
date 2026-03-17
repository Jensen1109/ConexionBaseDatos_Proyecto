// Declaración del paquete al que pertenece esta clase (agrupación lógica de controladores)
package controladores;

// Importar la clase ImagenDAO para acceder a la base de datos de imágenes
import dao.ImagenDAO;
// Importar el modelo Imagen que representa la entidad imagen en la BD
import modelos.Imagen;
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
 * Controlador CRUD de imágenes de productos (tabla Imagen).
 * Maneja: listar imágenes de un producto, agregar nueva imagen por URL y eliminar imagen.
 * Solo los administradores (id_rol=1) pueden gestionar imágenes.
 * Se accede desde la vista de productos pasando el parámetro ?idProducto=X.
 */
// Mapear este servlet a la URL "/ImagenControlador" para que responda a esa ruta
@WebServlet("/ImagenControlador")
// Declarar la clase que hereda de HttpServlet para funcionar como controlador web
public class ImagenControlador extends HttpServlet {

    // Crear una instancia de ImagenDAO para realizar operaciones CRUD de imágenes en la BD
    private final ImagenDAO imagenDAO = new ImagenDAO();

    // ═══════════════════════════════════════════════════
    // GET: Listar imágenes de un producto específico
    // ═══════════════════════════════════════════════════

    /**
     * Método doGet: se ejecuta cuando el navegador hace una petición GET.
     * Recibe el ID de un producto y muestra todas las imágenes asociadas a ese producto.
     * Si no se envía un ID de producto, redirige al catálogo de productos.
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

        // Leer el parámetro "idProducto" de la URL (ej: ?idProducto=3)
        String idParam = request.getParameter("idProducto");
        // Si no se envió el ID del producto o está vacío, redirigir al catálogo de productos
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        // Convertir el ID del producto de texto a número entero
        int idProducto = Integer.parseInt(idParam);
        // Obtener la lista de imágenes del producto desde la BD y pasarla a la vista
        request.setAttribute("imagenes", imagenDAO.listarPorProducto(idProducto));
        // Pasar el ID del producto a la vista para mostrarlo como referencia
        request.setAttribute("idProducto", idProducto);
        // Reenviar la solicitud al archivo JSP que mostrará las imágenes del producto
        request.getRequestDispatcher("/WEB-INF/view/imagenes.jsp").forward(request, response);
    }

    // ═══════════════════════════════════════════════════
    // POST: Agregar o eliminar imagen de un producto
    // ═══════════════════════════════════════════════════

    /**
     * Método doPost: se ejecuta cuando el navegador envía un formulario (POST).
     * Maneja las acciones: agregar nueva imagen por URL y eliminar imagen existente.
     * Siempre recibe el idProducto para saber a qué producto pertenece la imagen.
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

        // Obtener la acción que se quiere realizar ("eliminar" o agregar por defecto)
        String accion = request.getParameter("accion");
        // Leer el ID del producto al que pertenece la imagen (siempre requerido)
        int idProducto = Integer.parseInt(request.getParameter("idProducto"));

        // ===== ACCIÓN: ELIMINAR una imagen =====
        if ("eliminar".equals(accion)) {
            // Leer el ID de la imagen a eliminar y convertirlo a número entero
            int idImagen = Integer.parseInt(request.getParameter("idImagen"));
            // Ejecutar la eliminación de la imagen en la base de datos
            imagenDAO.eliminar(idImagen);

        // ===== ACCIÓN: AGREGAR una nueva imagen por URL =====
        } else {
            // Leer la URL de la imagen desde el formulario
            String url = request.getParameter("url");
            // Validar que la URL no sea nula ni esté vacía
            if (url != null && !url.isBlank()) {
                // Crear un nuevo objeto Imagen vacío
                Imagen img = new Imagen();
                // Asignar el ID del producto al que pertenece la imagen
                img.setIdProducto(idProducto);
                // Asignar la URL de la imagen quitando espacios en blanco
                img.setUrl(url.trim());
                // Insertar la nueva imagen en la base de datos
                imagenDAO.guardar(img);
            }
        }

        // Redirigir al listado de imágenes del producto para que se actualice la vista
        response.sendRedirect(request.getContextPath() +
                "/ImagenControlador?idProducto=" + idProducto);
    }
}

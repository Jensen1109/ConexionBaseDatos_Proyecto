// Paquete donde vive esta clase: dentro de la carpeta config del proyecto Tienda_Barrio
package com.Tienda_Barrio.config;

// Importa IOException: excepción que se lanza cuando hay errores de entrada/salida (leer, escribir datos)
import java.io.IOException;
// Importa PrintWriter: permite escribir texto en la respuesta HTTP (lo que ve el navegador)
import java.io.PrintWriter;
// Importa Connection: representa una conexión abierta con la base de datos MySQL
import java.sql.Connection;
// Importa SQLException: excepción que se lanza cuando ocurre un error relacionado con la base de datos
import java.sql.SQLException;

// Estas importaciones son de Jakarta EE (la versión moderna de Java EE), reemplazaron a "javax.servlet"
// ServletException: excepción propia de los servlets cuando ocurre un error al procesar la petición
import jakarta.servlet.ServletException;
// @WebServlet: anotación que registra este servlet en el servidor y le asigna una URL
import jakarta.servlet.annotation.WebServlet;
// HttpServlet: clase base que deben extender todos los servlets que manejan peticiones HTTP
import jakarta.servlet.http.HttpServlet;
// HttpServletRequest: objeto que contiene toda la información de la petición que llega del navegador
import jakarta.servlet.http.HttpServletRequest;
// HttpServletResponse: objeto que permite enviar la respuesta de vuelta al navegador
import jakarta.servlet.http.HttpServletResponse;

// @WebServlet registra este servlet en el servidor Tomcat
// name="TextConexion" es el nombre interno del servlet
// urlPatterns={"/TextConexion"} es la URL que debe escribir el usuario para llegar aquí: http://localhost:8080/proyecto/TextConexion
@WebServlet(name = "TextConexion", urlPatterns = {"/TextConexion"})
// TextConexion extiende HttpServlet: hereda todos los métodos para manejar peticiones HTTP (GET, POST, etc.)
public class TextConexion extends HttpServlet {

    // Método central que procesa la petición, lo llaman tanto doGet como doPost
    // request: contiene los datos que llegaron del navegador
    // response: es el objeto donde escribimos lo que el navegador va a mostrar
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Le decimos al navegador que la respuesta es una página HTML con codificación UTF-8
        // UTF-8 permite mostrar caracteres especiales como tildes (á, é, í) y la ñ
        response.setContentType("text/html;charset=UTF-8");

        // try-with-resources: abre un PrintWriter para escribir HTML en la respuesta
        // Se cierra automáticamente al terminar el bloque, sin necesidad de llamar out.close()
        try (PrintWriter out = response.getWriter()) {

            // Escribimos la estructura básica de una página HTML
            out.println("<!DOCTYPE html>");         // Declara que es un documento HTML5
            out.println("<html>");                  // Abre la etiqueta raíz del HTML
            out.println("<head><title>Prueba de Conexión</title></head>"); // Título que aparece en la pestaña del navegador
            out.println("<body>");                  // Abre el cuerpo de la página (contenido visible)
            out.println("<h1>Estado de la base de datos desde el Servidor</h1>"); // Título principal de la página

            // Bloque donde intentamos conectarnos a la base de datos
            try {
                // Llamamos al método estático getConnection() de nuestra clase conexion.java
                // Esto intenta abrir una conexión real con MySQL
                Connection con = conexion.getConnection();

                // Verificamos que la conexión no sea null (null significaría que falló)
                if (con != null) {
                    // Si la conexión fue exitosa, mostramos un mensaje en VERDE en la página
                    out.println("<h2 style='color: green;'>¡ÉXITO! Conectado a MySQL con JDK 21.</h2>");
                    // Cerramos la conexión manualmente porque no usamos try-with-resources aquí
                    // Es importante cerrarla para liberar recursos del servidor
                    con.close();
                } else {
                    // Si getConnection() retornó null, mostramos error en ROJO
                    // Esto pasa cuando hay un error al conectar pero no lanzó excepción
                    out.println("<h2 style='color: red;'>ERROR: La conexión es nula. Revisa la URL en conexion.java.</h2>");
                }
            } catch (SQLException e) {
                // Si MySQL lanzó un error (contraseña incorrecta, BD no existe, servidor apagado, etc.)
                // Mostramos el mensaje del error en ROJO directamente en la página para diagnosticarlo
                out.println("<h2 style='color: red;'>Error de SQL: " + e.getMessage() + "</h2>");
            }

            // Agregamos un enlace para volver al inicio de la aplicación
            out.println("<br><a href='index.html'>Volver al inicio</a>");
            out.println("</body>"); // Cierra el cuerpo de la página
            out.println("</html>"); // Cierra el documento HTML
        }
    }

    // doGet se ejecuta cuando el usuario escribe la URL directamente en el navegador (petición GET)
    // Por ejemplo: http://localhost:8080/proyecto/TextConexion
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delega el trabajo al método processRequest para no repetir código
        processRequest(request, response);
    }

    // doPost se ejecuta cuando llega una petición POST (por ejemplo, desde un formulario HTML)
    // Aquí también delegamos a processRequest para reutilizar el mismo código
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delega el trabajo al método processRequest para no repetir código
        processRequest(request, response);
    }
}

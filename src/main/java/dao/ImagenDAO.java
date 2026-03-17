// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Imagen que representa la tabla Imagen en la BD
import modelos.Imagen;

// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para la tabla Imagen.
 * Gestiona el almacenamiento y recuperación de URLs de imágenes
 * asociadas a los productos de la tienda.
 */
// Clase que maneja todas las operaciones CRUD de la tabla Imagen en la BD
public class ImagenDAO {

    /**
     * Lista todas las imágenes asociadas a un producto.
     * @param idProducto identificador del producto
     * @return lista de imágenes del producto; lista vacía si no tiene
     */
    // Método que obtiene todas las imágenes de un producto específico
    public List<Imagen> listarPorProducto(int idProducto) {
        // Creamos una lista vacía para almacenar las imágenes encontradas
        List<Imagen> lista = new ArrayList<>();
        // Consulta SQL que selecciona las imágenes filtradas por id_producto
        String sql = "SELECT id_imagen, id_producto, url FROM Imagen WHERE id_producto = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos la consulta

            // Asignamos el ID del producto para filtrar sus imágenes
            ps.setInt(1, idProducto);
            // Ejecutamos la consulta y obtenemos los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos cada fila del resultado
                while (rs.next()) {
                    // Creamos un nuevo objeto Imagen con su constructor y lo agregamos a la lista
                    lista.add(new Imagen(
                            rs.getInt("id_imagen"),       // ID único de la imagen
                            rs.getInt("id_producto"),     // ID del producto al que pertenece
                            rs.getString("url")           // URL o nombre del archivo de la imagen
                    ));
                }
            }

        } catch (SQLException e) {
            // Si hay error, lo imprimimos en la consola del servidor
            System.err.println("Error al listar imágenes: " + e.getMessage());
        }
        // Retornamos la lista de imágenes (vacía si no tiene o hubo error)
        return lista;
    }

    /**
     * Guarda una imagen vinculada a un producto existente.
     * @param img objeto Imagen con id_producto y url
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    // Método para guardar una nueva imagen asociada a un producto
    public boolean guardar(Imagen img) {
        // Consulta INSERT que vincula una URL de imagen a un producto
        String sql = "INSERT INTO Imagen (id_producto, url) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el INSERT

            // Posición 1: ID del producto al que pertenece la imagen
            ps.setInt(1, img.getIdProducto());
            // Posición 2: URL o ruta del archivo de la imagen
            ps.setString(2, img.getUrl());
            // Ejecutamos el INSERT y verificamos si se insertó correctamente
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar imagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserta una imagen sin producto asociado (útil al crear producto con imagen simultáneamente).
     * @param url URL o nombre de archivo de la imagen
     * @return id_imagen generado por la base de datos, o 0 si ocurrió un error
     */
    // Método que inserta una imagen sin vincularla a un producto aún
    // Se usa cuando se crea un producto nuevo: primero se sube la imagen y luego se asocia al producto
    public int insertar(String url) {
        // INSERT que solo guarda la URL de la imagen (sin id_producto)
        String sql = "INSERT INTO Imagen (url) VALUES (?)";

        try (Connection con = conexion.getConnection();
             // RETURN_GENERATED_KEYS le dice a MySQL que nos devuelva el ID auto-generado de la imagen
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            // Asignamos la URL de la imagen al parámetro
            ps.setString(1, url);
            // Ejecutamos el INSERT
            ps.executeUpdate();
            // Obtenemos el ID auto-generado por MySQL para la imagen recién insertada
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // Si hay un ID generado, lo retornamos
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar imagen: " + e.getMessage());
        }
        // Retornamos 0 si algo falló (no se generó ID)
        return 0;
    }

    /**
     * Actualiza la URL de una imagen existente.
     * @param idImagen identificador de la imagen a actualizar
     * @param url nueva URL o nombre de archivo
     * @return true si se actualizó correctamente, false si ocurrió un error
     */
    // Método para cambiar la URL de una imagen ya existente (ej: cuando se reemplaza la foto)
    public boolean actualizarUrl(int idImagen, String url) {
        // Consulta UPDATE que modifica la URL de la imagen identificada por su id_imagen
        String sql = "UPDATE Imagen SET url = ? WHERE id_imagen = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el UPDATE

            // Posición 1: la nueva URL de la imagen
            ps.setString(1, url);
            // Posición 2: el ID de la imagen a actualizar (cláusula WHERE)
            ps.setInt(2, idImagen);
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar imagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una imagen por su identificador.
     * @param idImagen identificador de la imagen a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    // Método para eliminar una imagen de la BD por su ID
    public boolean eliminar(int idImagen) {
        // Consulta DELETE que elimina la imagen con el ID indicado
        String sql = "DELETE FROM Imagen WHERE id_imagen = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID de la imagen a eliminar
            ps.setInt(1, idImagen);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
            return false;
        }
    }
}

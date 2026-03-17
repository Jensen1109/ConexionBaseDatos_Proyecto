// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos proporciona la conexión a la BD MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Categoria que representa la tabla Categoria en la BD
import modelos.Categoria;

// Importamos las clases de JDBC necesarias para interactuar con la base de datos
import java.sql.Connection;        // Representa la conexión abierta con MySQL
import java.sql.PreparedStatement;  // Para ejecutar consultas SQL parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Contiene los resultados de una consulta SELECT
import java.sql.SQLException;       // Excepción que se lanza cuando ocurre un error de BD
import java.util.ArrayList;         // Implementación de lista dinámica para almacenar resultados
import java.util.List;              // Interfaz que define una lista ordenada de elementos

/**
 * DAO para la tabla Categoria.
 * Gestiona el CRUD de las categorías de productos de la tienda.
 */
// Clase pública que maneja todas las operaciones CRUD de la tabla Categoria en la BD
public class CategoriaDAO {

    /**
     * Lista todas las categorías ordenadas alfabéticamente.
     * @return lista de categorías; lista vacía si no hay ninguna
     */
    // Método que obtiene todas las categorías registradas en la BD, ordenadas por nombre
    public List<Categoria> listarTodas() {
        // Creamos una lista vacía donde guardaremos las categorías encontradas
        List<Categoria> lista = new ArrayList<>();
        // Consulta SQL que selecciona id y nombre de todas las categorías, ordenadas alfabéticamente
        String sql = "SELECT id_categoria, nombre FROM Categoria ORDER BY nombre";

        // try-with-resources: abre conexión, prepara y ejecuta la consulta; todo se cierra automáticamente al terminar
        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql);     // Preparamos la consulta SQL
             ResultSet rs = ps.executeQuery()) {                    // Ejecutamos y obtenemos los resultados

            // Recorremos cada fila del resultado con un bucle while
            while (rs.next()) {
                // Por cada fila, creamos un nuevo objeto Categoria con el constructor que recibe id y nombre
                // y lo agregamos a la lista
                lista.add(new Categoria(
                        rs.getInt("id_categoria"),     // Extraemos el ID de la categoría
                        rs.getString("nombre")         // Extraemos el nombre de la categoría
                ));
            }

        } catch (SQLException e) {
            // Si ocurre un error de BD, lo imprimimos en la consola del servidor para depuración
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        // Retornamos la lista de categorías (puede estar vacía si no hay categorías o si hubo error)
        return lista;
    }

    /**
     * Busca una categoría por su identificador.
     * @param id identificador de la categoría
     * @return objeto Categoria si se encontró, null en caso contrario
     */
    // Método que busca una categoría específica por su ID
    public Categoria buscarPorId(int id) {
        // Consulta SQL con parámetro "?" para buscar una categoría por su id_categoria
        String sql = "SELECT id_categoria, nombre FROM Categoria WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos la consulta

            // Reemplazamos el "?" con el ID de la categoría que buscamos
            ps.setInt(1, id);

            // Ejecutamos la consulta y obtenemos los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos un resultado, creamos y retornamos un objeto Categoria
                if (rs.next()) {
                    return new Categoria(rs.getInt("id_categoria"), rs.getString("nombre"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }
        // Retornamos null si la categoría no fue encontrada
        return null;
    }

    /**
     * Crea una nueva categoría y retorna su ID generado automáticamente.
     * Se usa desde el formulario de producto para crear categorías al vuelo vía AJAX.
     * @param c objeto Categoria con el nombre a registrar
     * @return el ID generado de la nueva categoría, o -1 si ocurrió un error
     */
    // Método que inserta una categoría y retorna el ID auto-generado por MySQL
    public int crearYObtenerId(Categoria c) {
        // Consulta INSERT con un parámetro para el nombre
        String sql = "INSERT INTO Categoria (nombre) VALUES (?)";

        // Usamos Statement.RETURN_GENERATED_KEYS para obtener el ID auto-generado
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            // Asignamos el nombre de la categoría al parámetro "?"
            ps.setString(1, c.getNombre());
            // Ejecutamos el INSERT
            ps.executeUpdate();

            // Obtenemos las claves generadas automáticamente (el id_categoria)
            try (ResultSet keys = ps.getGeneratedKeys()) {
                // Si hay una clave generada, la retornamos
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al crear categoría y obtener ID: " + e.getMessage());
        }
        // Retornamos -1 si hubo algún error
        return -1;
    }

    /**
     * Crea una nueva categoría en la base de datos.
     * @param c objeto Categoria con el nombre a registrar
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    // Método para insertar una nueva categoría en la BD
    public boolean crear(Categoria c) {
        // Consulta INSERT con un parámetro para el nombre de la categoría
        // El id_categoria se genera automáticamente (AUTO_INCREMENT en MySQL)
        String sql = "INSERT INTO Categoria (nombre) VALUES (?)";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el INSERT

            // Asignamos el nombre de la categoría al parámetro "?"
            ps.setString(1, c.getNombre());
            // executeUpdate() ejecuta el INSERT y retorna el número de filas afectadas
            // Retornamos true si se insertó al menos una fila (es decir, la inserción fue exitosa)
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el nombre de una categoría existente.
     * @param c objeto Categoria con el id y nombre actualizado
     * @return true si se actualizó correctamente, false si ocurrió un error
     */
    // Método para modificar el nombre de una categoría ya existente
    public boolean actualizar(Categoria c) {
        // Consulta UPDATE que cambia el nombre de la categoría identificada por su id_categoria
        String sql = "UPDATE Categoria SET nombre = ? WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el UPDATE

            // Posición 1: el nuevo nombre de la categoría
            ps.setString(1, c.getNombre());
            // Posición 2: el ID de la categoría a actualizar (cláusula WHERE)
            ps.setInt(2, c.getIdCategoria());
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una categoría por su identificador.
     * @param id identificador de la categoría a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    // Método para eliminar una categoría de la BD por su ID
    public boolean eliminar(int id) {
        // Consulta DELETE que elimina la categoría con el ID indicado
        String sql = "DELETE FROM Categoria WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID de la categoría a eliminar
            ps.setInt(1, id);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Puede fallar si hay productos asociados a esta categoría (violación de FK)
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }
}

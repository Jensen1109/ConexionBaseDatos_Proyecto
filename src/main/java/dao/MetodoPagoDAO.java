// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos proporciona la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo MetodoPago que representa la tabla MetodoPago en la BD
import modelos.MetodoPago;

// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para la tabla MetodoPago.
 * Gestiona los métodos de pago disponibles en la tienda
 * (Efectivo, Nequi, Tarjeta, etc.).
 */
// Clase que maneja las operaciones CRUD de la tabla MetodoPago en la BD
public class MetodoPagoDAO {

    /**
     * Lista todos los métodos de pago disponibles, ordenados alfabéticamente.
     * @return lista de métodos de pago; lista vacía si no hay ninguno
     */
    // Método que obtiene todos los métodos de pago registrados en la BD
    public List<MetodoPago> listarTodos() {
        // Creamos una lista vacía para almacenar los métodos de pago
        List<MetodoPago> lista = new ArrayList<>();
        // Consulta SQL que selecciona todos los métodos de pago ordenados por nombre
        String sql = "SELECT id_pago, nombre FROM MetodoPago ORDER BY nombre";

        // try-with-resources: abre conexión, prepara y ejecuta la consulta; se cierra automáticamente
        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql);     // Preparamos la consulta
             ResultSet rs = ps.executeQuery()) {                    // Ejecutamos y obtenemos resultados

            // Recorremos cada fila del resultado
            while (rs.next()) {
                // Creamos un nuevo MetodoPago con su ID y nombre, y lo agregamos a la lista
                lista.add(new MetodoPago(rs.getInt("id_pago"), rs.getString("nombre")));
            }

        } catch (SQLException e) {
            // Si hay error de BD, lo imprimimos en la consola del servidor
            System.err.println("Error al listar métodos de pago: " + e.getMessage());
        }
        // Retornamos la lista de métodos de pago
        return lista;
    }

    /**
     * Crea un nuevo método de pago.
     * @param mp objeto MetodoPago con el nombre a registrar
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    // Método para agregar un nuevo método de pago a la BD
    public boolean crear(MetodoPago mp) {
        // Consulta INSERT con un parámetro para el nombre del método de pago
        // El id_pago se genera automáticamente (AUTO_INCREMENT en MySQL)
        String sql = "INSERT INTO MetodoPago (nombre) VALUES (?)";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el INSERT

            // Asignamos el nombre del método de pago al parámetro "?"
            ps.setString(1, mp.getNombre());
            // Ejecutamos el INSERT y verificamos si se insertó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear método de pago: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un método de pago por su identificador.
     * @param idPago identificador del método de pago a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    // Método para eliminar un método de pago de la BD por su ID
    public boolean eliminar(int idPago) {
        // Consulta DELETE que elimina el método de pago con el ID indicado
        String sql = "DELETE FROM MetodoPago WHERE id_pago = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID del método de pago a eliminar
            ps.setInt(1, idPago);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Puede fallar si hay pedidos que usan este método de pago (violación de FK)
            System.err.println("Error al eliminar método de pago: " + e.getMessage());
            return false;
        }
    }
}

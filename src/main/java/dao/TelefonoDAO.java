// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Telefono que representa la tabla Telefono en la BD
import modelos.Telefono;

// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para la tabla Telefono.
 * Gestiona los números de teléfono adicionales de los clientes.
 * Un cliente puede tener múltiples teléfonos registrados.
 */
// Clase que maneja las operaciones CRUD de la tabla Telefono en la BD
// Permite que cada cliente tenga uno o más números de teléfono asociados
public class TelefonoDAO {

    /**
     * Lista todos los teléfonos registrados para un cliente.
     * @param idCliente identificador del cliente
     * @return lista de teléfonos del cliente; lista vacía si no tiene
     */
    // Método que obtiene todos los números de teléfono de un cliente específico
    public List<Telefono> listarPorCliente(int idCliente) {
        // Creamos una lista vacía para almacenar los teléfonos encontrados
        List<Telefono> lista = new ArrayList<>();
        // Consulta SQL que selecciona los teléfonos filtrados por cliente_id
        String sql = "SELECT id_telefono, telefono, cliente_id FROM Telefono WHERE cliente_id = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos la consulta

            // Asignamos el ID del cliente para filtrar sus teléfonos
            ps.setInt(1, idCliente);
            // Ejecutamos la consulta y obtenemos los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos cada fila del resultado
                while (rs.next()) {
                    // Creamos un nuevo objeto Telefono con sus datos y lo agregamos a la lista
                    lista.add(new Telefono(
                            rs.getInt("id_telefono"),     // ID único del teléfono
                            rs.getString("telefono"),     // Número de teléfono
                            rs.getInt("cliente_id")       // ID del cliente al que pertenece
                    ));
                }
            }

        } catch (SQLException e) {
            // Si hay error, lo imprimimos en la consola del servidor
            System.err.println("Error al listar teléfonos: " + e.getMessage());
        }
        // Retornamos la lista de teléfonos (vacía si no tiene o hubo error)
        return lista;
    }

    /**
     * Agrega un número de teléfono asociado a un cliente.
     * @param t objeto Telefono con el número y el cliente_id
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    // Método para agregar un nuevo número de teléfono a un cliente
    public boolean agregar(Telefono t) {
        // Consulta INSERT con dos parámetros: el número de teléfono y el ID del cliente
        String sql = "INSERT INTO Telefono (telefono, cliente_id) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el INSERT

            // Posición 1: el número de teléfono
            ps.setString(1, t.getTelefono());
            // Posición 2: el ID del cliente al que asociamos este teléfono
            ps.setInt(2, t.getClienteId());
            // Ejecutamos el INSERT y verificamos si se insertó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al agregar teléfono: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un teléfono por su identificador.
     * @param idTelefono identificador del teléfono a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    // Método para eliminar un número de teléfono específico por su ID
    public boolean eliminar(int idTelefono) {
        // Consulta DELETE que elimina el teléfono con el ID indicado
        String sql = "DELETE FROM Telefono WHERE id_telefono = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID del teléfono a eliminar
            ps.setInt(1, idTelefono);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar teléfono: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina todos los teléfonos de un cliente.
     * Útil al actualizar el teléfono principal de un cliente.
     * @param idCliente identificador del cliente
     * @return true si se eliminaron correctamente, false si ocurrió un error
     */
    // Método que elimina TODOS los teléfonos de un cliente de una sola vez
    // Se usa al actualizar los teléfonos: primero se borran todos y luego se insertan los nuevos
    // También se usa al eliminar un cliente para limpiar sus datos relacionados
    public boolean eliminarPorCliente(int idCliente) {
        // Consulta DELETE que elimina todos los teléfonos de un cliente específico
        String sql = "DELETE FROM Telefono WHERE cliente_id = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID del cliente cuyos teléfonos eliminamos
            ps.setInt(1, idCliente);
            // Ejecutamos el DELETE (puede ser 0 filas si el cliente no tenía teléfonos)
            ps.executeUpdate();
            // Retornamos true porque la operación fue exitosa (aunque no haya borrado nada)
            return true;

        } catch (SQLException e) {
            System.err.println("Error al eliminar teléfonos del cliente: " + e.getMessage());
            return false;
        }
    }
}

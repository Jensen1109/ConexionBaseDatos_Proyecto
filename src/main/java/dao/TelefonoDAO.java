package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Telefono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Telefono.
 * Gestiona los números de teléfono adicionales de los clientes.
 * Un cliente puede tener múltiples teléfonos registrados.
 */
public class TelefonoDAO {

    /**
     * Lista todos los teléfonos registrados para un cliente.
     * @param idCliente identificador del cliente
     * @return lista de teléfonos del cliente; lista vacía si no tiene
     */
    public List<Telefono> listarPorCliente(int idCliente) {
        List<Telefono> lista = new ArrayList<>();
        String sql = "SELECT id_telefono, telefono, cliente_id FROM Telefono WHERE cliente_id = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Telefono(
                            rs.getInt("id_telefono"),
                            rs.getString("telefono"),
                            rs.getInt("cliente_id")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar teléfonos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Agrega un número de teléfono asociado a un cliente.
     * @param t objeto Telefono con el número y el cliente_id
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    public boolean agregar(Telefono t) {
        String sql = "INSERT INTO Telefono (telefono, cliente_id) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTelefono());
            ps.setInt(2, t.getClienteId());
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
    public boolean eliminar(int idTelefono) {
        String sql = "DELETE FROM Telefono WHERE id_telefono = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTelefono);
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
    public boolean eliminarPorCliente(int idCliente) {
        String sql = "DELETE FROM Telefono WHERE cliente_id = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al eliminar teléfonos del cliente: " + e.getMessage());
            return false;
        }
    }
}

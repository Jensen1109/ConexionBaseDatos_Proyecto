package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.MetodoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla MetodoPago.
 * Gestiona los métodos de pago disponibles en la tienda
 * (Efectivo, Nequi, Tarjeta, etc.).
 */
public class MetodoPagoDAO {

    /**
     * Lista todos los métodos de pago disponibles, ordenados alfabéticamente.
     * @return lista de métodos de pago; lista vacía si no hay ninguno
     */
    public List<MetodoPago> listarTodos() {
        List<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT id_pago, nombre FROM MetodoPago ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new MetodoPago(rs.getInt("id_pago"), rs.getString("nombre")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar métodos de pago: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Crea un nuevo método de pago.
     * @param mp objeto MetodoPago con el nombre a registrar
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    public boolean crear(MetodoPago mp) {
        String sql = "INSERT INTO MetodoPago (nombre) VALUES (?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mp.getNombre());
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
    public boolean eliminar(int idPago) {
        String sql = "DELETE FROM MetodoPago WHERE id_pago = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPago);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar método de pago: " + e.getMessage());
            return false;
        }
    }
}

package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Telefono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelefonoDAO {

    // LISTAR TELÉFONOS DE UN USUARIO
    public List<Telefono> listarPorUsuario(int idUsuario) {
        List<Telefono> lista = new ArrayList<>();
        String sql = "SELECT id_telefono, telefono, usuario_id FROM Telefono WHERE usuario_id = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Telefono(
                            rs.getInt("id_telefono"),
                            rs.getString("telefono"),
                            rs.getInt("usuario_id")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar teléfonos: " + e.getMessage());
        }
        return lista;
    }

    // AGREGAR TELÉFONO
    public boolean agregar(Telefono t) {
        String sql = "INSERT INTO Telefono (telefono, usuario_id) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTelefono());
            ps.setInt(2, t.getUsuarioId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al agregar teléfono: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR
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
}

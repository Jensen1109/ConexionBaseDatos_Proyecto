package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // LOGIN
    public Usuario login(String email, String contrasenaIngresada) {
        String sql = "SELECT u.id_usuario, u.id_rol, u.email, u.contraseña_hash, u.nombre " +
                     "FROM Usuario u WHERE u.email = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contraseña_hash");
                    if (BCrypt.checkpw(contrasenaIngresada, hashGuardado)) {
                        Usuario u = new Usuario();
                        u.setIdUsuario(rs.getInt("id_usuario"));
                        u.setIdRol(rs.getInt("id_rol"));
                        u.setEmail(rs.getString("email"));
                        u.setNombre(rs.getString("nombre"));
                        return u;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return null;
    }

    // REGISTRAR
    public boolean registrar(Usuario u, String contrasenaNueva) {
        String sql = "INSERT INTO Usuario (id_rol, email, contraseña_hash, nombre, apellido, cedula) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        String hash = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, u.getIdRol());
            ps.setString(2, u.getEmail());
            ps.setString(3, hash);
            ps.setString(4, u.getNombre());
            ps.setString(5, u.getApellido());
            ps.setString(6, u.getCedula());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // VERIFICAR EMAIL
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE email = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }
}
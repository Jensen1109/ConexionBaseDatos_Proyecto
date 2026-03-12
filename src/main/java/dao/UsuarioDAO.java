package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    // LISTAR CLIENTES (id_rol = 2)
    public List<Usuario> listarClientes() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, id_rol, email, nombre, apellido, cedula " +
                     "FROM Usuario WHERE id_rol = 2 ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setEmail(rs.getString("email"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                u.setCedula(rs.getString("cedula"));
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    // ACTUALIZAR DATOS BÁSICOS DE UN CLIENTE
    public boolean actualizarCliente(int idUsuario, String nombre, String apellido, String email) {
        String sql = "UPDATE Usuario SET nombre=?, apellido=?, email=? WHERE id_usuario=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, email);
            ps.setInt(4, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR CLIENTE
    public boolean eliminarCliente(int idUsuario) {
        String sql = "DELETE FROM Usuario WHERE id_usuario = ? AND id_rol = 2";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
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
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

/**
 * DAO para la tabla Usuario.
 * Gestiona autenticación y CRUD de usuarios del sistema (admins y empleados).
 */
public class UsuarioDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Usuario
    // ─────────────────────────────────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setEmail(rs.getString("email"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setCedula(rs.getString("cedula"));
        return u;
    }

    /**
     * Autentica un usuario por email y contraseña.
     * @param email    email del usuario
     * @param contrasenaIngresada contraseña en texto plano
     * @return Usuario autenticado o null si las credenciales son incorrectas
     */
    public Usuario login(String email, String contrasenaIngresada) {
        String sql = "SELECT id_usuario, id_rol, email, contraseña_hash, nombre, apellido, cedula " +
                     "FROM Usuario WHERE email = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contraseña_hash");
                    if (BCrypt.checkpw(contrasenaIngresada, hashGuardado)) {
                        return mapear(rs);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en login por email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Autentica un usuario por cédula y contraseña.
     * @param cedula   cédula del usuario
     * @param contrasenaIngresada contraseña en texto plano
     * @return Usuario autenticado o null
     */
    public Usuario loginPorCedula(String cedula, String contrasenaIngresada) {
        String sql = "SELECT id_usuario, id_rol, email, contraseña_hash, nombre, apellido, cedula " +
                     "FROM Usuario WHERE cedula = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contraseña_hash");
                    if (BCrypt.checkpw(contrasenaIngresada, hashGuardado)) {
                        return mapear(rs);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en login por cédula: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra un nuevo usuario con contraseña cifrada con BCrypt.
     * @param u            objeto Usuario con datos básicos
     * @param contrasenaNueva contraseña en texto plano (se almacena como hash)
     * @return true si se insertó correctamente
     */
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

    /**
     * Lista todos los usuarios del sistema (admins + empleados).
     * @return lista de usuarios ordenada por nombre
     */
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.id_rol, u.email, u.nombre, u.apellido, u.cedula, " +
                     "r.nombre AS nombre_rol " +
                     "FROM Usuario u " +
                     "JOIN Rol r ON u.id_rol = r.id_rol " +
                     "ORDER BY u.nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = mapear(rs);
                // nombreRol se guarda temporalmente en apellido no: usamos campo extra
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza los datos de un usuario (sin cambiar contraseña).
     * @param u objeto Usuario con los nuevos datos
     * @return true si se actualizó
     */
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE Usuario SET nombre=?, apellido=?, email=?, id_rol=? WHERE id_usuario=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setInt(4, u.getIdRol());
            ps.setInt(5, u.getIdUsuario());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario del sistema.
     * @param idUsuario ID del usuario a eliminar
     * @return true si se eliminó
     */
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM Usuario WHERE id_usuario = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si ya existe un usuario con ese email.
     * @param email email a verificar
     * @return true si ya existe
     */
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE email = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe un usuario con esa cédula.
     * @param cedula cédula a verificar
     * @return true si ya existe
     */
    public boolean cedulaExiste(String cedula) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE cedula = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar cédula: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza solo el perfil propio (nombre, apellido, email) sin tocar rol ni contraseña.
     */
    public boolean actualizarPerfil(int idUsuario, String nombre, String apellido, String email) {
        String sql = "UPDATE Usuario SET nombre=?, apellido=?, email=? WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, email);
            ps.setInt(4, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia la contraseña de un usuario (recibe hash ya generado).
     */
    public boolean actualizarContrasena(int idUsuario, String nuevoHash) {
        String sql = "UPDATE Usuario SET contraseña_hash=? WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el email ya está en uso por OTRO usuario (para edición de perfil).
     */
    public boolean emailExisteExcluyendo(String email, int idUsuario) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE email=? AND id_usuario != ?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el hash de contraseña actual de un usuario (para verificar antes de cambiarla).
     */
    public String obtenerHashContrasena(int idUsuario) {
        String sql = "SELECT contraseña_hash FROM Usuario WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("contraseña_hash");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener hash: " + e.getMessage());
        }
        return null;
    }

    // ── Métodos legacy para compatibilidad con ClienteControlador antiguo ──

    /** @deprecated Usar ClienteDAO.listar() para la tabla Cliente */
    public List<Usuario> listarClientes() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, id_rol, email, nombre, apellido, cedula " +
                     "FROM Usuario WHERE id_rol = 2 ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }
}

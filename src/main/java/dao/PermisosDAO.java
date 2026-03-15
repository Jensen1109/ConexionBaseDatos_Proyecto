package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Permisos;
import modelos.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermisosDAO {

    // LISTAR TODOS LOS PERMISOS
    public List<Permisos> listarTodos() {
        List<Permisos> lista = new ArrayList<>();
        String sql = "SELECT id_permiso, nombre, descripcion FROM Permisos ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Permisos(
                        rs.getInt("id_permiso"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar permisos: " + e.getMessage());
        }
        return lista;
    }

    // LISTAR TODOS LOS ROLES
    public List<Rol> listarRoles() {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id_rol, nombre FROM Rol ORDER BY id_rol";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return lista;
    }

    // LISTAR PERMISOS DE UN ROL
    public List<Integer> listarIdPermisosPorRol(int idRol) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_permiso FROM rol_permiso WHERE id_rol = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("id_permiso"));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar permisos del rol: " + e.getMessage());
        }
        return ids;
    }

    // ASIGNAR PERMISO A ROL
    public boolean asignarPermiso(int idRol, int idPermiso) {
        String sql = "INSERT IGNORE INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setInt(2, idPermiso);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al asignar permiso: " + e.getMessage());
            return false;
        }
    }

    // VERIFICAR SI UN ROL TIENE UN PERMISO POR NOMBRE
    // Admin (idRol=1) siempre tiene todos los permisos.
    // Para otros roles se consulta la tabla rol_permiso.
    public boolean tienePermiso(int idRol, String nombrePermiso) {
        // Admin siempre tiene acceso completo — sin consultar la BD
        if (idRol == 1) return true;

        String sql = "SELECT 1 FROM rol_permiso rp " +
                     "JOIN Permisos p ON rp.id_permiso = p.id_permiso " +
                     "WHERE rp.id_rol = ? AND p.nombre = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setString(2, nombrePermiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }

            // Fallback para roles no-admin: si rol_permiso está vacía dar permisos básicos
            if (!permisosConfigurados(con)) {
                return nombrePermiso.equals("VER_PRODUCTOS")
                    || nombrePermiso.equals("VER_STOCK")
                    || nombrePermiso.equals("REGISTRAR_VENTA");
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar permiso: " + e.getMessage());
        }
        return false;
    }

    private boolean permisosConfigurados(Connection con) {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM rol_permiso LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // QUITAR PERMISO DE ROL
    public boolean quitarPermiso(int idRol, int idPermiso) {
        String sql = "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setInt(2, idPermiso);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al quitar permiso: " + e.getMessage());
            return false;
        }
    }
}

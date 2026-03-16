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

/**
 * DAO para las tablas Permisos, Rol y rol_permiso.
 * Gestiona la asignación y verificación de permisos por rol,
 * implementando el control de acceso basado en roles (RBAC) del sistema.
 */
public class PermisosDAO {

    /**
     * Lista todos los permisos registrados en el sistema, ordenados por nombre.
     * @return lista de permisos; lista vacía si no hay ninguno
     */
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

    /**
     * Lista todos los roles del sistema ordenados por id.
     * @return lista de roles; lista vacía si no hay ninguno
     */
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

    /**
     * Retorna los IDs de permisos asignados a un rol específico.
     * @param idRol identificador del rol
     * @return lista de IDs de permisos del rol; lista vacía si no tiene ninguno
     */
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

    /**
     * Asigna un permiso a un rol. Usa INSERT IGNORE para evitar duplicados.
     * @param idRol     identificador del rol
     * @param idPermiso identificador del permiso a asignar
     * @return true si se asignó correctamente, false si ocurrió un error
     */
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

    /**
     * Verifica si un rol tiene un permiso determinado.
     * El rol admin (id=1) siempre retorna true sin consultar la BD.
     * Para otros roles consulta la tabla rol_permiso.
     * @param idRol         identificador del rol a verificar
     * @param nombrePermiso nombre del permiso (ej: "REGISTRAR_VENTA")
     * @return true si el rol tiene el permiso, false en caso contrario
     */
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
                    || nombrePermiso.equals("REGISTRAR_VENTA")
                    || nombrePermiso.equals("VER_HISTORIAL")
                    || nombrePermiso.equals("VER_REPORTES");
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

    /**
     * Quita un permiso de un rol eliminando el registro de rol_permiso.
     * @param idRol     identificador del rol
     * @param idPermiso identificador del permiso a quitar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
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

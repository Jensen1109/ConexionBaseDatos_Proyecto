// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos los modelos que representan las tablas Permisos y Rol en la BD
import modelos.Permisos;
import modelos.Rol;

// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para las tablas Permisos, Rol y rol_permiso.
 * Gestiona la asignación y verificación de permisos por rol,
 * implementando el control de acceso basado en roles (RBAC) del sistema.
 */
// Clase que implementa el sistema de permisos RBAC (Role-Based Access Control)
// Gestiona qué acciones puede realizar cada rol en el sistema
public class PermisosDAO {

    /**
     * Lista todos los permisos registrados en el sistema, ordenados por nombre.
     * @return lista de permisos; lista vacía si no hay ninguno
     */
    // Método que obtiene todos los permisos disponibles en el sistema
    public List<Permisos> listarTodos() {
        // Creamos una lista vacía para almacenar los permisos
        List<Permisos> lista = new ArrayList<>();
        // Consulta SQL que trae todos los permisos ordenados alfabéticamente
        String sql = "SELECT id_permiso, nombre, descripcion FROM Permisos ORDER BY nombre";

        // try-with-resources: abre conexión, prepara y ejecuta la consulta; se cierra automáticamente
        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql);     // Preparamos la consulta
             ResultSet rs = ps.executeQuery()) {                    // Ejecutamos y obtenemos resultados

            // Recorremos cada fila del resultado
            while (rs.next()) {
                // Creamos un nuevo objeto Permisos con id, nombre y descripción
                lista.add(new Permisos(
                        rs.getInt("id_permiso"),           // ID único del permiso
                        rs.getString("nombre"),            // Nombre del permiso (ej: "REGISTRAR_VENTA")
                        rs.getString("descripcion")        // Descripción legible del permiso
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar permisos: " + e.getMessage());
        }
        // Retornamos la lista de permisos
        return lista;
    }

    /**
     * Lista todos los roles del sistema ordenados por id.
     * @return lista de roles; lista vacía si no hay ninguno
     */
    // Método que obtiene todos los roles del sistema (ej: admin, empleado)
    public List<Rol> listarRoles() {
        // Lista vacía para almacenar los roles
        List<Rol> lista = new ArrayList<>();
        // Consulta que trae todos los roles ordenados por su ID
        String sql = "SELECT id_rol, nombre FROM Rol ORDER BY id_rol";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql);     // Preparamos la consulta
             ResultSet rs = ps.executeQuery()) {                    // Ejecutamos

            // Recorremos cada rol encontrado
            while (rs.next()) {
                // Creamos un objeto Rol con su ID y nombre, y lo agregamos a la lista
                lista.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        // Retornamos la lista de roles
        return lista;
    }

    /**
     * Retorna los IDs de permisos asignados a un rol específico.
     * @param idRol identificador del rol
     * @return lista de IDs de permisos del rol; lista vacía si no tiene ninguno
     */
    // Método que obtiene los IDs de los permisos que tiene un rol específico
    // Se usa para marcar los checkboxes en la vista de configuración de permisos
    public List<Integer> listarIdPermisosPorRol(int idRol) {
        // Lista vacía para almacenar los IDs de permisos
        List<Integer> ids = new ArrayList<>();
        // Consulta que obtiene los permisos asignados a un rol desde la tabla intermedia rol_permiso
        String sql = "SELECT id_permiso FROM rol_permiso WHERE id_rol = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos la consulta

            // Asignamos el ID del rol cuyos permisos queremos consultar
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos cada permiso asignado y guardamos su ID en la lista
                while (rs.next()) ids.add(rs.getInt("id_permiso"));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar permisos del rol: " + e.getMessage());
        }
        // Retornamos la lista de IDs de permisos del rol
        return ids;
    }

    /**
     * Asigna un permiso a un rol. Usa INSERT IGNORE para evitar duplicados.
     * @param idRol     identificador del rol
     * @param idPermiso identificador del permiso a asignar
     * @return true si se asignó correctamente, false si ocurrió un error
     */
    // Método que asigna un permiso a un rol insertando un registro en la tabla rol_permiso
    public boolean asignarPermiso(int idRol, int idPermiso) {
        // INSERT IGNORE: si la combinación (id_rol, id_permiso) ya existe, MySQL ignora el INSERT
        // en lugar de lanzar un error de clave duplicada
        String sql = "INSERT IGNORE INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el INSERT

            // Posición 1: ID del rol al que asignamos el permiso
            ps.setInt(1, idRol);
            // Posición 2: ID del permiso que estamos asignando
            ps.setInt(2, idPermiso);
            // Ejecutamos el INSERT; retorna true si se insertó (o false si ya existía por IGNORE)
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
    // Método clave del sistema RBAC: verifica si un rol tiene acceso a una funcionalidad
    // Se llama desde los controladores antes de permitir una acción
    public boolean tienePermiso(int idRol, String nombrePermiso) {
        // Admin siempre tiene acceso completo — sin consultar la BD
        // Si el rol es admin (id=1), retornamos true inmediatamente sin ir a la BD
        if (idRol == 1) return true;

        // Consulta que busca si existe la relación entre el rol y el permiso
        // Usa JOIN entre rol_permiso y Permisos para buscar por nombre del permiso
        // "SELECT 1" es una convención: solo nos interesa si existe al menos una fila
        String sql = "SELECT 1 FROM rol_permiso rp " +
                     "JOIN Permisos p ON rp.id_permiso = p.id_permiso " +  // Unimos con la tabla Permisos
                     "WHERE rp.id_rol = ? AND p.nombre = ?";               // Filtramos por rol y nombre

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del rol
            ps.setInt(1, idRol);
            // Asignamos el nombre del permiso a verificar
            ps.setString(2, nombrePermiso);
            try (ResultSet rs = ps.executeQuery()) {
                // Si hay al menos un resultado, el rol tiene el permiso
                if (rs.next()) return true;
            }

            // Fallback para roles no-admin: si rol_permiso está vacía dar permisos básicos
            // Esto permite que el sistema funcione incluso sin haber configurado permisos aún
            if (!permisosConfigurados(con)) {
                // Si no hay permisos configurados, otorgamos permisos básicos por defecto
                return nombrePermiso.equals("VER_PRODUCTOS")
                    || nombrePermiso.equals("VER_STOCK")
                    || nombrePermiso.equals("REGISTRAR_VENTA")
                    || nombrePermiso.equals("VER_HISTORIAL")
                    || nombrePermiso.equals("VER_REPORTES");
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar permiso: " + e.getMessage());
        }
        // Si no se encontró el permiso para ese rol, retornamos false (sin acceso)
        return false;
    }

    // Método privado auxiliar que verifica si hay al menos un registro en la tabla rol_permiso
    // Si está vacía, significa que el admin aún no ha configurado los permisos
    private boolean permisosConfigurados(Connection con) {
        // Consulta simple que intenta traer una fila de rol_permiso
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM rol_permiso LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            // Si hay al menos un registro, los permisos están configurados
            return rs.next();
        } catch (SQLException e) {
            // Si hay error, asumimos que no están configurados
            return false;
        }
    }

    /**
     * Quita un permiso de un rol eliminando el registro de rol_permiso.
     * @param idRol     identificador del rol
     * @param idPermiso identificador del permiso a quitar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    // Método que revoca un permiso de un rol eliminando el registro de la tabla intermedia
    public boolean quitarPermiso(int idRol, int idPermiso) {
        // DELETE que elimina la relación entre el rol y el permiso
        String sql = "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";

        try (Connection con = conexion.getConnection();            // Abrimos conexión
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos el DELETE

            // Asignamos el ID del rol
            ps.setInt(1, idRol);
            // Asignamos el ID del permiso a quitar
            ps.setInt(2, idPermiso);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al quitar permiso: " + e.getMessage());
            return false;
        }
    }
}

package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Cliente.
 * Gestiona el CRUD de clientes de la tienda.
 */
public class ClienteDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Cliente
    // ─────────────────────────────────────────────
    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setCedula(rs.getString("cedula"));
        c.setTelefono(rs.getString("telefono"));
        return c;
    }

    /**
     * Lista todos los clientes ordenados por nombre.
     * @return lista de clientes
     */
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un cliente por su ID.
     * @param id id_cliente
     * @return Cliente encontrado o null
     */
    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM Cliente WHERE id_cliente = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea un nuevo cliente.
     * @param c objeto Cliente con los datos
     * @return true si se insertó correctamente
     */
    public boolean crear(Cliente c) {
        String sql = "INSERT INTO Cliente (nombre, apellido, cedula, telefono) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getTelefono());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea un cliente y retorna el ID generado (útil al registrar ventas con cliente nuevo).
     * @return id_cliente generado, o 0 si falló
     */
    public int crearYObtenerIdCliente(Cliente c) {
        String sql = "INSERT INTO Cliente (nombre, apellido, cedula, telefono) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param c objeto Cliente con los nuevos datos (debe tener idCliente válido)
     * @return true si se actualizó
     */
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE Cliente SET nombre=?, apellido=?, cedula=?, telefono=? WHERE id_cliente=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getTelefono());
            ps.setInt(5, c.getIdCliente());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un cliente por su ID.
     * @param id id_cliente a eliminar
     * @return true si se eliminó
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Cliente WHERE id_cliente = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca clientes cuyo nombre, apellido o cédula contengan el texto dado.
     * @param q texto a buscar
     * @return lista de hasta 10 resultados
     */
    public List<Cliente> buscarPorTexto(String q) {
        List<Cliente> lista = new ArrayList<>();
        String like = "%" + q + "%";
        String sql  = "SELECT * FROM Cliente WHERE nombre LIKE ? OR apellido LIKE ? OR cedula LIKE ? ORDER BY nombre LIMIT 10";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Verifica si ya existe un cliente con esa cédula.
     * @param cedula cédula a verificar
     * @return true si ya existe
     */
    public boolean cedulaExiste(String cedula) {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE cedula = ?";

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
     * Verifica si la cédula ya existe excluyendo un cliente específico
     * (útil al editar para no bloquear la misma cédula del cliente en cuestión).
     * @param cedula   cédula a verificar
     * @param idCliente cliente a excluir
     * @return true si ya existe en otro cliente
     */
    public boolean cedulaExisteExcluyendo(String cedula, int idCliente) {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE cedula = ? AND id_cliente != ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula);
            ps.setInt(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar cédula excluyendo: " + e.getMessage());
        }
        return false;
    }
}

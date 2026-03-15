package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Deuda;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Deuda.
 * Gestiona deudas generadas por ventas a crédito y registro de abonos.
 * Estados válidos: 'activa' | 'pagada'
 */
public class DeudaDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Deuda
    // ─────────────────────────────────────────────
    private Deuda mapear(ResultSet rs) throws SQLException {
        Deuda d = new Deuda();
        d.setIdDeuda(rs.getInt("id_deuda"));
        d.setIdPedido(rs.getInt("id_pedido"));
        d.setMontoPendiente(rs.getBigDecimal("monto_pendiente"));
        d.setEstado(rs.getString("estado"));
        d.setAbono(rs.getBigDecimal("abono"));
        Date fecha = rs.getDate("fecha_abono");
        if (fecha != null) d.setFechaAbono(fecha.toLocalDate());
        return d;
    }

    /**
     * Lista todas las deudas ordenadas por ID descendente.
     * @return lista de todas las deudas
     */
    public List<Deuda> listarTodas() {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT * FROM Deuda ORDER BY id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar deudas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista solo las deudas activas.
     * @return lista de deudas con estado 'activa'
     */
    public List<Deuda> listarActivas() {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT * FROM Deuda WHERE estado = 'activa' ORDER BY id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar deudas activas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista deudas activas con nombre del cliente (JOIN con Pedido → Cliente).
     * @return lista de deudas activas con nombreCliente poblado
     */
    public List<Deuda> listarActivasConCliente() {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT d.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente " +
                     "FROM Deuda d " +
                     "LEFT JOIN Pedido pe ON d.id_pedido = pe.id_pedido " +
                     "LEFT JOIN Cliente c ON pe.id_cliente = c.id_cliente " +
                     "WHERE d.estado = 'activa' " +
                     "ORDER BY d.id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Deuda d = mapear(rs);
                d.setNombreCliente(rs.getString("nombre_cliente"));
                lista.add(d);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar deudas con cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene una deuda por su ID.
     * @param idDeuda ID de la deuda
     * @return Deuda o null
     */
    public Deuda buscarPorId(int idDeuda) {
        String sql = "SELECT * FROM Deuda WHERE id_deuda = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDeuda);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar deuda: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra un abono a una deuda.
     * Si el abono cubre el total pendiente, el estado pasa a 'pagada'.
     * Rechaza si el abono supera el monto pendiente.
     * @param idDeuda    ID de la deuda
     * @param montoAbono monto a abonar
     * @return true si se registró, false si el abono supera el pendiente
     */
    public boolean abonar(int idDeuda, BigDecimal montoAbono) {
        // Verificar que el abono no supere el monto pendiente
        Deuda actual = buscarPorId(idDeuda);
        if (actual == null) return false;
        if (montoAbono.compareTo(actual.getMontoPendiente()) > 0) return false;

        String sql = "UPDATE Deuda SET " +
                     "abono = ?, " +
                     "fecha_abono = CURDATE(), " +
                     "monto_pendiente = monto_pendiente - ?, " +
                     "estado = CASE WHEN monto_pendiente <= 0 THEN 'pagada' ELSE 'activa' END " +
                     "WHERE id_deuda = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, montoAbono);
            ps.setBigDecimal(2, montoAbono);
            ps.setInt(3, idDeuda);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar abono: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra una nueva deuda vinculada a un pedido a crédito.
     * @param d objeto Deuda con id_pedido y monto_pendiente
     * @return true si se insertó
     */
    public boolean registrarDeuda(Deuda d) {
        String sql = "INSERT INTO Deuda (id_pedido, monto_pendiente, estado, abono) " +
                     "VALUES (?, ?, 'activa', 0)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getIdPedido());
            ps.setBigDecimal(2, d.getMontoPendiente());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar deuda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista las deudas de un cliente específico (via Pedido).
     * @param idCliente ID del cliente
     * @return lista de deudas del cliente
     */
    public List<Deuda> listarPorCliente(int idCliente) {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT d.* FROM Deuda d " +
                     "JOIN Pedido pe ON d.id_pedido = pe.id_pedido " +
                     "WHERE pe.id_cliente = ? " +
                     "ORDER BY d.id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar deudas por cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcula el total de deudas activas.
     * @return suma de monto_pendiente con estado 'activa'
     */
    public BigDecimal totalPendiente() {
        String sql = "SELECT COALESCE(SUM(monto_pendiente), 0) FROM Deuda WHERE estado = 'activa'";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular total deudas: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // ── Alias para compatibilidad con código existente ──

    /** @see #abonar(int, BigDecimal) */
    public boolean registrarAbono(int idDeuda, BigDecimal montoAbono) {
        return abonar(idDeuda, montoAbono);
    }

    /** @see #listarActivasConCliente() */
    public List<Deuda> listarPendientesConCliente() {
        return listarActivasConCliente();
    }
}

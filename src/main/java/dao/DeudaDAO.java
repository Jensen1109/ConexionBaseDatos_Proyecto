package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Deuda;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeudaDAO {

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

    // LISTAR TODAS LAS DEUDAS
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

    // LISTAR SOLO PENDIENTES
    public List<Deuda> listarPendientes() {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT * FROM Deuda WHERE estado = 'pendiente' ORDER BY id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar deudas pendientes: " + e.getMessage());
        }
        return lista;
    }

    // REGISTRAR ABONO
    public boolean registrarAbono(int idDeuda, java.math.BigDecimal montoAbono) {
        String sql = "UPDATE Deuda SET abono = ?, fecha_abono = CURDATE(), " +
                     "monto_pendiente = monto_pendiente - ?, " +
                     "estado = CASE WHEN monto_pendiente - ? <= 0 THEN 'pagado' ELSE 'pendiente' END " +
                     "WHERE id_deuda = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, montoAbono);
            ps.setBigDecimal(2, montoAbono);
            ps.setBigDecimal(3, montoAbono);
            ps.setInt(4, idDeuda);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar abono: " + e.getMessage());
            return false;
        }
    }

    // REGISTRAR DEUDA NUEVA (para ventas fiadas)
    public boolean registrarDeuda(Deuda d) {
        String sql = "INSERT INTO Deuda (id_pedido, monto_pendiente, estado, abono) VALUES (?, ?, 'pendiente', 0)";

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

    // LISTAR DEUDAS PENDIENTES CON NOMBRE DE CLIENTE (JOIN con Pedido + Usuario)
    public List<Deuda> listarPendientesConCliente() {
        List<Deuda> lista = new ArrayList<>();
        String sql = "SELECT d.*, CONCAT(u.nombre, ' ', u.apellido) AS nombre_cliente " +
                     "FROM Deuda d " +
                     "LEFT JOIN Pedido p  ON d.id_pedido  = p.id_pedido " +
                     "LEFT JOIN Usuario u ON p.id_cliente = u.id_usuario " +
                     "WHERE d.estado = 'pendiente' " +
                     "ORDER BY d.id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Deuda d2 = mapear(rs);
                d2.setNombreCliente(rs.getString("nombre_cliente"));
                lista.add(d2);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar deudas con cliente: " + e.getMessage());
        }
        return lista;
    }

    // TOTAL DEUDAS PENDIENTES
    public java.math.BigDecimal totalPendiente() {
        String sql = "SELECT COALESCE(SUM(monto_pendiente), 0) FROM Deuda WHERE estado = 'pendiente'";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular total deudas: " + e.getMessage());
        }
        return java.math.BigDecimal.ZERO;
    }
}

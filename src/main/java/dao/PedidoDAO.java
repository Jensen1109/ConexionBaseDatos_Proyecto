package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.DetallePedido;
import modelos.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Pedido
    // ─────────────────────────────────────────────
    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdPago(rs.getInt("id_pago"));
        Timestamp ts = rs.getTimestamp("fecha_venta");
        if (ts != null) p.setFechaVenta(ts.toLocalDateTime());
        p.setTotal(rs.getBigDecimal("total"));
        p.setEstado(rs.getString("estado"));
        return p;
    }

    // CREAR PEDIDO (con sus detalles en una sola transacción)
    public boolean crear(Pedido pedido, List<DetallePedido> detalles) {
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlStock   = "UPDATE Producto SET stock = stock - ? WHERE id_producto = ?";

        Connection con = null;
        try {
            con = conexion.getConnection();
            con.setAutoCommit(false);

            // 1. Insertar el pedido y obtener el ID generado
            int idPedidoNuevo;
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pedido.getIdCliente());
                ps.setInt(2, pedido.getIdUsuario());
                ps.setInt(3, pedido.getIdPago());
                ps.setBigDecimal(4, pedido.getTotal());
                ps.setString(5, pedido.getEstado());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se obtuvo el ID del pedido.");
                    idPedidoNuevo = keys.getInt(1);
                }
            }

            // 2. Insertar cada detalle y descontar stock
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock   = con.prepareStatement(sqlStock)) {

                for (DetallePedido d : detalles) {
                    psDetalle.setInt(1, idPedidoNuevo);
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setInt(3, d.getCantidadVendida());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.addBatch();

                    psStock.setInt(1, d.getCantidadVendida());
                    psStock.setInt(2, d.getIdProducto());
                    psStock.addBatch();
                }
                psDetalle.executeBatch();
                psStock.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al crear pedido: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) {
                    System.err.println("Error al cerrar conexión: " + ex.getMessage());
                }
            }
        }
    }

    // LISTAR PEDIDOS DE UN CLIENTE
    public List<Pedido> listarPorCliente(int idCliente) {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pedido WHERE id_cliente = ? ORDER BY fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos del cliente: " + e.getMessage());
        }
        return lista;
    }

    // LISTAR TODOS LOS PEDIDOS (admin)
    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pedido ORDER BY fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar todos los pedidos: " + e.getMessage());
        }
        return lista;
    }

    // LISTAR DETALLES DE UN PEDIDO
    public List<DetallePedido> listarDetalles(int idPedido) {
        List<DetallePedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pedido WHERE id_pedido = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdPedido(rs.getInt("id_pedido"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    lista.add(d);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar detalles: " + e.getMessage());
        }
        return lista;
    }

    // REGISTRAR PEDIDO Y RETORNAR ID GENERADO (-1 si falla)
    // Igual que crear() pero devuelve el id_pedido para poder enlazar una Deuda (ventas fiadas)
    public int registrar(Pedido pedido, List<DetallePedido> detalles) {
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlStock   = "UPDATE Producto SET stock = stock - ? WHERE id_producto = ?";

        Connection con = null;
        try {
            con = conexion.getConnection();
            con.setAutoCommit(false);

            int idNuevo;
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pedido.getIdCliente());
                ps.setInt(2, pedido.getIdUsuario());
                ps.setInt(3, pedido.getIdPago());
                ps.setBigDecimal(4, pedido.getTotal());
                ps.setString(5, pedido.getEstado());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se obtuvo el ID del pedido.");
                    idNuevo = keys.getInt(1);
                }
            }

            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                 PreparedStatement psS = con.prepareStatement(sqlStock)) {
                for (DetallePedido d : detalles) {
                    psD.setInt(1, idNuevo);
                    psD.setInt(2, d.getIdProducto());
                    psD.setInt(3, d.getCantidadVendida());
                    psD.setBigDecimal(4, d.getPrecioUnitario());
                    psD.addBatch();
                    psS.setInt(1, d.getCantidadVendida());
                    psS.setInt(2, d.getIdProducto());
                    psS.addBatch();
                }
                psD.executeBatch();
                psS.executeBatch();
            }

            con.commit();
            return idNuevo;

        } catch (SQLException e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
            if (con != null) try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return -1;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    // LISTAR TODOS CON NOMBRE DE CLIENTE (JOIN con Usuario)
    public List<Pedido> listarConCliente() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(u.nombre, ' ', u.apellido) AS nombre_cliente " +
                     "FROM Pedido p " +
                     "LEFT JOIN Usuario u ON p.id_cliente = u.id_usuario " +
                     "ORDER BY p.fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = mapear(rs);
                p.setNombreCliente(rs.getString("nombre_cliente"));
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos con cliente: " + e.getMessage());
        }
        return lista;
    }

    // OBTENER PEDIDO POR ID
    public Pedido obtenerPorId(int id) {
        String sql = "SELECT * FROM Pedido WHERE id_pedido = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener pedido por id: " + e.getMessage());
        }
        return null;
    }

    // CAMBIAR ESTADO (ej: "pendiente" → "entregado")
    public boolean cambiarEstado(int idPedido, String nuevoEstado) {
        String sql = "UPDATE Pedido SET estado = ? WHERE id_pedido = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del pedido: " + e.getMessage());
            return false;
        }
    }
}

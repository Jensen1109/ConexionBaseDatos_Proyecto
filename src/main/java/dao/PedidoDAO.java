package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.DetallePedido;
import modelos.Pedido;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Pedido y detalle_pedido.
 * Maneja creación de ventas con transacciones atómicas y actualización de stock.
 */
public class PedidoDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Pedido
    // ─────────────────────────────────────────────
    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setIdCliente(rs.getInt("id_cliente")); // puede ser 0 si NULL en BD
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdPago(rs.getInt("id_pago"));
        Timestamp ts = rs.getTimestamp("fecha_venta");
        if (ts != null) p.setFechaVenta(ts.toLocalDateTime());
        p.setTotal(rs.getBigDecimal("total"));
        p.setEstado(rs.getString("estado"));
        return p;
    }

    /**
     * Crea un pedido con sus detalles en una sola transacción atómica.
     * Descuenta el stock de cada producto. Rechaza si stock resultante quedaría negativo.
     * @param pedido  objeto Pedido con los datos de la venta
     * @param detalles lista de ítems de la venta
     * @return true si la transacción fue exitosa
     */
    public boolean crear(Pedido pedido, List<DetallePedido> detalles) {
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlStock   = "UPDATE Producto SET stock = stock - ? " +
                            "WHERE id_producto = ? AND stock >= ?";

        Connection con = null;
        try {
            con = conexion.getConnection();
            con.setAutoCommit(false);

            // 1. Insertar el pedido y obtener el ID generado
            int idPedidoNuevo;
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                if (pedido.getIdCliente() > 0) ps.setInt(1, pedido.getIdCliente());
                else                           ps.setNull(1, Types.INTEGER);
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

            // 2. Insertar detalles y descontar stock (verificando stock suficiente)
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
                    psStock.setInt(3, d.getCantidadVendida()); // stock >= cantidad requerida
                    psStock.addBatch();
                }
                psDetalle.executeBatch();
                int[] stockUpdates = psStock.executeBatch();

                // Verificar que todos los stocks se actualizaron (0 = stock insuficiente)
                for (int updated : stockUpdates) {
                    if (updated == 0) throw new SQLException("Stock insuficiente para uno o más productos.");
                }
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

    /**
     * Registra un pedido y retorna el ID generado.
     * Igual que crear() pero devuelve el id_pedido para enlazar con Deuda.
     * @param pedido  objeto Pedido con los datos de la venta
     * @param detalles lista de ítems de la venta
     * @return id_pedido generado, o -1 si falló
     */
    public int registrar(Pedido pedido, List<DetallePedido> detalles) {
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlStock   = "UPDATE Producto SET stock = stock - ? " +
                            "WHERE id_producto = ? AND stock >= ?";

        Connection con = null;
        try {
            con = conexion.getConnection();
            con.setAutoCommit(false);

            int idNuevo;
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                if (pedido.getIdCliente() > 0) ps.setInt(1, pedido.getIdCliente());
                else                           ps.setNull(1, Types.INTEGER);
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
                    psS.setInt(3, d.getCantidadVendida());
                    psS.addBatch();
                }
                psD.executeBatch();
                int[] stockUpdates = psS.executeBatch();

                for (int updated : stockUpdates) {
                    if (updated == 0) throw new SQLException("Stock insuficiente para uno o más productos.");
                }
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

    /**
     * Lista todos los pedidos con nombre del cliente (JOIN con tabla Cliente).
     * @return lista de pedidos ordenados por fecha descendente
     */
    public List<Pedido> listarConCliente() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente, " +
                     "mp.nombre AS nombre_pago " +
                     "FROM Pedido p " +
                     "LEFT JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                     "LEFT JOIN metodo_pago mp ON p.id_pago = mp.id_pago " +
                     "ORDER BY p.fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = mapear(rs);
                p.setNombreCliente(rs.getString("nombre_cliente"));
                p.setNombrePago(rs.getString("nombre_pago"));
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos con cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista pedidos en un rango de fechas (para filtro en historial).
     * @param inicio fecha inicio (inclusive)
     * @param fin    fecha fin (inclusive)
     * @return lista de pedidos en ese rango
     */
    public List<Pedido> listarPorFechas(LocalDate inicio, LocalDate fin) {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente, " +
                     "mp.nombre AS nombre_pago " +
                     "FROM Pedido p " +
                     "LEFT JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                     "LEFT JOIN metodo_pago mp ON p.id_pago = mp.id_pago " +
                     "WHERE DATE(p.fecha_venta) BETWEEN ? AND ? " +
                     "ORDER BY p.fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido p = mapear(rs);
                    p.setNombreCliente(rs.getString("nombre_cliente"));
                    p.setNombrePago(rs.getString("nombre_pago"));
                    lista.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos por fechas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista los pedidos de un cliente específico.
     * @param idCliente id del cliente
     * @return lista de pedidos del cliente
     */
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

    /**
     * Lista todos los pedidos (admin).
     * @return lista completa de pedidos
     */
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

    /**
     * Lista los detalles de un pedido específico.
     * @param idPedido ID del pedido
     * @return lista de detalles con nombre de producto
     */
    public List<DetallePedido> listarDetalles(int idPedido) {
        List<DetallePedido> lista = new ArrayList<>();
        String sql = "SELECT dp.*, p.nombre AS nombre_producto " +
                     "FROM detalle_pedido dp " +
                     "JOIN Producto p ON dp.id_producto = p.id_producto " +
                     "WHERE dp.id_pedido = ?";

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

    /**
     * Obtiene un pedido por su ID.
     * @param id id_pedido
     * @return Pedido o null
     */
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

    /**
     * Cambia el estado de un pedido.
     * @param idPedido    ID del pedido
     * @param nuevoEstado nuevo estado ('pagado' o 'credito')
     * @return true si se actualizó
     */
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

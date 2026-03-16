package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para reportes y estadísticas del sistema.
 * Provee consultas de resumen sobre ventas, stock, deudas y usuarios
 * para el módulo de reportes del panel administrativo.
 */
public class ReporteDAO {

    /**
     * Calcula el total de ventas del mes en curso.
     * @return suma total de ventas del mes actual; BigDecimal.ZERO si no hay ventas
     */
    public BigDecimal totalVentasMes() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM Pedido " +
                     "WHERE MONTH(fecha_venta) = MONTH(CURDATE()) " +
                     "AND YEAR(fecha_venta) = YEAR(CURDATE())";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular ventas del mes: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Retorna los productos cuyo stock actual es menor o igual al stock mínimo.
     * Ordenados de menor a mayor stock para priorizar los más críticos.
     * @return lista de productos con stock bajo o crítico
     */
    public List<Producto> productosStockBajo() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, id_categoria, id_imagen, nombre, precio, " +
                     "stock, stock_minimo, descripcion, fecha_vencimiento, unidad_medida " +
                     "FROM Producto WHERE stock <= stock_minimo ORDER BY stock ASC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setIdImagen(rs.getInt("id_imagen"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getBigDecimal("precio"));
                p.setStock(rs.getInt("stock"));
                p.setStockMinimo(rs.getInt("stock_minimo"));
                p.setDescripcion(rs.getString("descripcion"));
                java.sql.Date fecha = rs.getDate("fecha_vencimiento");
                if (fecha != null) p.setFechaVencimiento(fecha.toLocalDate());
                p.setUnidadMedida(rs.getString("unidad_medida"));
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcula la suma total de montos pendientes en deudas activas.
     * @return total de deudas pendientes; BigDecimal.ZERO si no hay deudas
     */
    public BigDecimal totalDeudasPendientes() {
        String sql = "SELECT COALESCE(SUM(monto_pendiente), 0) FROM Deuda WHERE estado = 'pendiente'";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular deudas pendientes: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Cuenta el número total de empleados registrados en el sistema (id_rol = 2).
     * @return cantidad de empleados registrados; 0 si no hay ninguno
     */
    public int contarClientes() {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE id_rol = 2";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error al contar clientes: " + e.getMessage());
        }
        return 0;
    }
}

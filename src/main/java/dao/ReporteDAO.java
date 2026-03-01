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

public class ReporteDAO {

    // TOTAL VENTAS DEL MES ACTUAL
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

    // PRODUCTOS CON STOCK BAJO O IGUAL AL MÍNIMO
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

    // TOTAL DEUDAS PENDIENTES
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

    // NÚMERO DE CLIENTES REGISTRADOS
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

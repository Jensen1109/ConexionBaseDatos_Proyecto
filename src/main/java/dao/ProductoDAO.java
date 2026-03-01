package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Producto
    // ─────────────────────────────────────────────
    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setIdImagen(rs.getInt("id_imagen"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setDescripcion(rs.getString("descripcion"));
        Date fecha = rs.getDate("fecha_vencimiento");
        if (fecha != null) p.setFechaVencimiento(fecha.toLocalDate());
        p.setUnidadMedida(rs.getString("unidad_medida"));
        return p;
    }

    // LISTAR TODOS
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM Producto ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    // BUSCAR POR ID
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM Producto WHERE id_producto = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    // BUSCAR POR CATEGORÍA
    public List<Producto> buscarPorCategoria(int idCategoria) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM Producto WHERE id_categoria = ? ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por categoría: " + e.getMessage());
        }
        return lista;
    }

    // CREAR
    public boolean crear(Producto p) {
        String sql = "INSERT INTO Producto (id_categoria, id_imagen, nombre, precio, stock, " +
                     "stock_minimo, descripcion, fecha_vencimiento, unidad_medida) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getIdCategoria());
            ps.setInt(2, p.getIdImagen());
            ps.setString(3, p.getNombre());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getStockMinimo());
            ps.setString(7, p.getDescripcion());
            ps.setDate(8, p.getFechaVencimiento() != null
                    ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setString(9, p.getUnidadMedida());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    // ACTUALIZAR
    public boolean actualizar(Producto p) {
        String sql = "UPDATE Producto SET id_categoria=?, nombre=?, precio=?, stock=?, " +
                     "stock_minimo=?, descripcion=?, fecha_vencimiento=?, unidad_medida=? " +
                     "WHERE id_producto=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getIdCategoria());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getDescripcion());
            ps.setDate(7, p.getFechaVencimiento() != null
                    ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setString(8, p.getUnidadMedida());
            ps.setInt(9, p.getIdProducto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Producto WHERE id_producto = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
}

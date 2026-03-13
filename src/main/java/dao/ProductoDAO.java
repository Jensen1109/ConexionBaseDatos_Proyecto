package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Categoria;
import modelos.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE =
        "SELECT p.*, i.url AS imagen_url " +
        "FROM Producto p " +
        "LEFT JOIN Imagen i ON p.id_imagen = i.id_imagen ";

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
        p.setImagenUrl(rs.getString("imagen_url")); // viene del JOIN con Imagen
        return p;
    }

    // LISTAR TODOS
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.nombre";

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
        String sql = SELECT_BASE + "WHERE p.id_producto = ?";

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
        String sql = SELECT_BASE + "WHERE p.id_categoria = ? ORDER BY p.nombre";

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
            if (p.getIdImagen() > 0) ps.setInt(2, p.getIdImagen());
            else                     ps.setNull(2, Types.INTEGER);
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
        String sql = "UPDATE Producto SET id_categoria=?, id_imagen=?, nombre=?, precio=?, stock=?, " +
                     "stock_minimo=?, descripcion=?, fecha_vencimiento=?, unidad_medida=? " +
                     "WHERE id_producto=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getIdCategoria());
            if (p.getIdImagen() > 0) ps.setInt(2, p.getIdImagen());
            else                     ps.setNull(2, Types.INTEGER);
            ps.setString(3, p.getNombre());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getStockMinimo());
            ps.setString(7, p.getDescripcion());
            ps.setDate(8, p.getFechaVencimiento() != null
                    ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setString(9, p.getUnidadMedida());
            ps.setInt(10, p.getIdProducto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // LISTAR CATEGORÍAS (para los formularios)
    public List<Categoria> listarCategorias() {
        return new CategoriaDAO().listarTodas();
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

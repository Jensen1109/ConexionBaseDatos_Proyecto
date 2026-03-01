package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Imagen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImagenDAO {

    // LISTAR IMÁGENES POR PRODUCTO
    public List<Imagen> listarPorProducto(int idProducto) {
        List<Imagen> lista = new ArrayList<>();
        String sql = "SELECT id_imagen, id_producto, url FROM Imagen WHERE id_producto = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Imagen(
                            rs.getInt("id_imagen"),
                            rs.getInt("id_producto"),
                            rs.getString("url")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar imágenes: " + e.getMessage());
        }
        return lista;
    }

    // GUARDAR URL DE IMAGEN
    public boolean guardar(Imagen img) {
        String sql = "INSERT INTO Imagen (id_producto, url) VALUES (?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, img.getIdProducto());
            ps.setString(2, img.getUrl());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar imagen: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int idImagen) {
        String sql = "DELETE FROM Imagen WHERE id_imagen = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idImagen);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
            return false;
        }
    }
}

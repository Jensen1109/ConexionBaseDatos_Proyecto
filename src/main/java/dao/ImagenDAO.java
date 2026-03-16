package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Imagen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Imagen.
 * Gestiona el almacenamiento y recuperación de URLs de imágenes
 * asociadas a los productos de la tienda.
 */
public class ImagenDAO {

    /**
     * Lista todas las imágenes asociadas a un producto.
     * @param idProducto identificador del producto
     * @return lista de imágenes del producto; lista vacía si no tiene
     */
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

    /**
     * Guarda una imagen vinculada a un producto existente.
     * @param img objeto Imagen con id_producto y url
     * @return true si se insertó correctamente, false si ocurrió un error
     */
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

    /**
     * Inserta una imagen sin producto asociado (útil al crear producto con imagen simultáneamente).
     * @param url URL o nombre de archivo de la imagen
     * @return id_imagen generado por la base de datos, o 0 si ocurrió un error
     */
    public int insertar(String url) {
        String sql = "INSERT INTO Imagen (url) VALUES (?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, url);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar imagen: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza la URL de una imagen existente.
     * @param idImagen identificador de la imagen a actualizar
     * @param url nueva URL o nombre de archivo
     * @return true si se actualizó correctamente, false si ocurrió un error
     */
    public boolean actualizarUrl(int idImagen, String url) {
        String sql = "UPDATE Imagen SET url = ? WHERE id_imagen = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, url);
            ps.setInt(2, idImagen);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar imagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una imagen por su identificador.
     * @param idImagen identificador de la imagen a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
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

package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla Categoria.
 * Gestiona el CRUD de las categorías de productos de la tienda.
 */
public class CategoriaDAO {

    /**
     * Lista todas las categorías ordenadas alfabéticamente.
     * @return lista de categorías; lista vacía si no hay ninguna
     */
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM Categoria ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca una categoría por su identificador.
     * @param id identificador de la categoría
     * @return objeto Categoria si se encontró, null en caso contrario
     */
    public Categoria buscarPorId(int id) {
        String sql = "SELECT id_categoria, nombre FROM Categoria WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id_categoria"), rs.getString("nombre"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea una nueva categoría en la base de datos.
     * @param c objeto Categoria con el nombre a registrar
     * @return true si se insertó correctamente, false si ocurrió un error
     */
    public boolean crear(Categoria c) {
        String sql = "INSERT INTO Categoria (nombre) VALUES (?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el nombre de una categoría existente.
     * @param c objeto Categoria con el id y nombre actualizado
     * @return true si se actualizó correctamente, false si ocurrió un error
     */
    public boolean actualizar(Categoria c) {
        String sql = "UPDATE Categoria SET nombre = ? WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getIdCategoria());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una categoría por su identificador.
     * @param id identificador de la categoría a eliminar
     * @return true si se eliminó correctamente, false si ocurrió un error
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Categoria WHERE id_categoria = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }
}

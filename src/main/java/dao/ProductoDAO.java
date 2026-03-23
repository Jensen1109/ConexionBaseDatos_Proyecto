// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a la base de datos MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos los modelos que representan tablas de la BD
import modelos.Categoria;
import modelos.Producto;

// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Representa la conexión abierta con MySQL
import java.sql.Date;              // Clase para manejar fechas en SQL (distinta de java.util.Date)
import java.sql.PreparedStatement;  // Para ejecutar consultas SQL parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Contiene los resultados de un SELECT
import java.sql.SQLException;       // Excepción para errores de base de datos
import java.sql.Types;              // Constantes que representan tipos de datos SQL (INTEGER, VARCHAR, etc.)
import java.util.ArrayList;         // Lista dinámica para almacenar productos
import java.util.List;              // Interfaz de lista

// Clase pública que maneja todas las operaciones de la tabla Producto en la BD
public class ProductoDAO {

    // Constante con la parte base del SELECT que se reutiliza en varias consultas
    // LEFT JOIN con Imagen trae la URL de la imagen del producto (puede ser NULL si no tiene imagen)
    // WHERE p.activo = true filtra solo productos activos (los eliminados tienen activo = false)
    private static final String SELECT_BASE =
        "SELECT p.*, i.url AS imagen_url " +   // p.* trae todas las columnas de Producto; i.url la URL de la imagen
        "FROM Producto p " +                     // "p" es un alias corto para la tabla Producto
        "LEFT JOIN Imagen i ON p.id_imagen = i.id_imagen " + // LEFT JOIN: trae el producto aunque no tenga imagen
        "WHERE p.activo = true ";  // Solo productos activos; los desactivados no aparecen en el sistema

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Producto
    // ─────────────────────────────────────────────
    // Método privado que convierte una fila del ResultSet en un objeto Producto de Java
    private Producto mapear(ResultSet rs) throws SQLException {
        // Creamos un objeto Producto vacío
        Producto p = new Producto();
        // Extraemos cada columna del ResultSet y la asignamos al objeto Producto
        p.setIdProducto(rs.getInt("id_producto"));       // ID único del producto
        p.setIdCategoria(rs.getInt("id_categoria"));     // ID de la categoría a la que pertenece
        p.setIdImagen(rs.getInt("id_imagen"));           // ID de la imagen asociada (puede ser 0 si no tiene)
        p.setNombre(rs.getString("nombre"));             // Nombre del producto
        p.setPrecio(rs.getBigDecimal("precio"));         // Precio del producto (BigDecimal para precisión monetaria)
        p.setStock(rs.getInt("stock"));                  // Cantidad disponible en inventario
        p.setStockMinimo(rs.getInt("stock_minimo"));     // Cantidad mínima antes de alertar reabastecimiento
        p.setDescripcion(rs.getString("descripcion"));   // Descripción detallada del producto
        // Obtenemos la fecha de vencimiento (puede ser NULL si el producto no vence)
        Date fecha = rs.getDate("fecha_vencimiento");
        // Solo asignamos la fecha si no es null; toLocalDate() convierte de java.sql.Date a LocalDate
        if (fecha != null) p.setFechaVencimiento(fecha.toLocalDate());
        // Unidad de medida del producto (ej: "kg", "litros", "unidad")
        p.setUnidadMedida(rs.getString("unidad_medida"));
        // URL de la imagen que viene del LEFT JOIN con la tabla Imagen
        p.setImagenUrl(rs.getString("imagen_url")); // viene del JOIN con Imagen
        // Retornamos el producto completo con todos sus datos
        return p;
    }

    // LISTAR TODOS
    // Método que obtiene todos los productos de la BD ordenados alfabéticamente
    public List<Producto> listarTodos() {
        // Creamos una lista vacía donde guardaremos los productos encontrados
        List<Producto> lista = new ArrayList<>();
        // Usamos la consulta base y le agregamos ORDER BY para ordenar por nombre
        String sql = SELECT_BASE + "ORDER BY p.nombre";

        // Abrimos conexión, preparamos y ejecutamos la consulta en una sola estructura try-with-resources
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos todas las filas del resultado y mapeamos cada una a un Producto
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            // Si hay error de BD, lo imprimimos en la consola del servidor
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        // Retornamos la lista de productos (vacía si no hay productos o si hubo error)
        return lista;
    }

    // BUSCAR POR ID
    // Método que busca un producto específico por su identificador único
    public Producto buscarPorId(int id) {
        // Usamos la consulta base con un filtro WHERE por id_producto
        String sql = SELECT_BASE + "AND p.id_producto = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del producto que queremos buscar al parámetro "?"
            ps.setInt(1, id);
            // Ejecutamos la consulta
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos un resultado, lo mapeamos y retornamos
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        // Retornamos null si no se encontró el producto
        return null;
    }

    // BUSCAR POR CATEGORÍA
    // Método que obtiene todos los productos que pertenecen a una categoría específica
    public List<Producto> buscarPorCategoria(int idCategoria) {
        // Lista vacía para almacenar los productos de esa categoría
        List<Producto> lista = new ArrayList<>();
        // Filtramos por id_categoria y ordenamos por nombre
        String sql = SELECT_BASE + "AND p.id_categoria = ? ORDER BY p.nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID de la categoría al parámetro "?"
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos todos los productos de esa categoría
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por categoría: " + e.getMessage());
        }
        // Retornamos la lista de productos filtrados
        return lista;
    }

    // CREAR
    // Método que inserta un nuevo producto en la base de datos
    public boolean crear(Producto p) {
        // Consulta INSERT con 9 parámetros para cada columna del producto
        String sql = "INSERT INTO Producto (id_categoria, id_imagen, nombre, precio, stock, " +
                     "stock_minimo, descripcion, fecha_vencimiento, unidad_medida) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Posición 1: ID de la categoría del producto
            ps.setInt(1, p.getIdCategoria());
            // Posición 2: ID de la imagen; si no tiene imagen (id=0), ponemos NULL en la BD
            if (p.getIdImagen() > 0) ps.setInt(2, p.getIdImagen());
            else                     ps.setNull(2, Types.INTEGER);  // setNull indica que el campo será NULL en la BD
            // Posición 3: Nombre del producto
            ps.setString(3, p.getNombre());
            // Posición 4: Precio del producto (BigDecimal para manejar decimales con precisión)
            ps.setBigDecimal(4, p.getPrecio());
            // Posición 5: Stock actual (cantidad en inventario)
            ps.setInt(5, p.getStock());
            // Posición 6: Stock mínimo (umbral para alertas de reabastecimiento)
            ps.setInt(6, p.getStockMinimo());
            // Posición 7: Descripción del producto
            ps.setString(7, p.getDescripcion());
            // Posición 8: Fecha de vencimiento; convertimos LocalDate a java.sql.Date, o null si no vence
            ps.setDate(8, p.getFechaVencimiento() != null
                    ? Date.valueOf(p.getFechaVencimiento()) : null);
            // Posición 9: Unidad de medida (kg, litros, unidad, etc.)
            ps.setString(9, p.getUnidadMedida());
            // Ejecutamos el INSERT; retorna true si se insertó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    // ACTUALIZAR
    // Método que modifica los datos de un producto existente en la BD
    public boolean actualizar(Producto p) {
        // Consulta UPDATE que modifica todos los campos del producto identificado por id_producto
        String sql = "UPDATE Producto SET id_categoria=?, id_imagen=?, nombre=?, precio=?, stock=?, " +
                     "stock_minimo=?, descripcion=?, fecha_vencimiento=?, unidad_medida=? " +
                     "WHERE id_producto=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos los nuevos valores a cada parámetro (misma lógica que en crear)
            ps.setInt(1, p.getIdCategoria());               // Nueva categoría
            // Si tiene imagen, asignamos el ID; si no, ponemos NULL
            if (p.getIdImagen() > 0) ps.setInt(2, p.getIdImagen());
            else                     ps.setNull(2, Types.INTEGER);
            ps.setString(3, p.getNombre());                 // Nuevo nombre
            ps.setBigDecimal(4, p.getPrecio());             // Nuevo precio
            ps.setInt(5, p.getStock());                     // Nuevo stock
            ps.setInt(6, p.getStockMinimo());               // Nuevo stock mínimo
            ps.setString(7, p.getDescripcion());            // Nueva descripción
            // Convertimos la fecha de vencimiento o ponemos null si no aplica
            ps.setDate(8, p.getFechaVencimiento() != null
                    ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setString(9, p.getUnidadMedida());           // Nueva unidad de medida
            ps.setInt(10, p.getIdProducto());               // ID del producto a actualizar (cláusula WHERE)
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // LISTAR CATEGORÍAS (para los formularios)
    // Método de conveniencia que delega en CategoriaDAO para obtener las categorías
    // Se usa en los formularios de crear/editar producto para llenar el combo de categorías
    public List<Categoria> listarCategorias() {
        // Creamos una instancia de CategoriaDAO y llamamos su método listarTodas()
        return new CategoriaDAO().listarTodas();
    }

    /**
     * Verifica si ya existe un producto con ese nombre (para evitar duplicados).
     * @param nombre nombre a verificar
     * @return true si ya existe
     */
    // Método que comprueba si un nombre de producto ya está registrado en la BD
    public boolean nombreExiste(String nombre) {
        // COUNT(*) cuenta cuántos productos tienen ese nombre exacto
        String sql = "SELECT COUNT(*) FROM Producto WHERE nombre = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el nombre a buscar; trim() elimina espacios al inicio y final
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                // Si el conteo es mayor a 0, el nombre ya existe
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar nombre: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si el nombre existe en otro producto (útil al editar).
     * @param nombre    nombre a verificar
     * @param idProducto producto a excluir
     * @return true si ya existe en otro producto
     */
    // Método que verifica si el nombre ya existe en OTRO producto (excluyendo el que se está editando)
    public boolean nombreExisteExcluyendo(String nombre, int idProducto) {
        // Busca productos con ese nombre pero que NO sean el producto actual
        String sql = "SELECT COUNT(*) FROM Producto WHERE nombre = ? AND id_producto != ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el nombre a verificar
            ps.setString(1, nombre.trim());
            // Excluimos el producto que estamos editando para no marcarlo como duplicado
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                // Si otro producto ya tiene ese nombre, retornamos true
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar nombre excluyendo: " + e.getMessage());
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // LISTAR INACTIVOS
    // ─────────────────────────────────────────────
    // Método que retorna todos los productos donde activo = false (los "eliminados")
    // Solo el administrador lo usa para mostrar la sección de productos desactivados en el catálogo
    public List<Producto> listarInactivos() {
        // Lista vacía donde guardaremos los productos desactivados encontrados
        List<Producto> lista = new ArrayList<>();

        // Consulta SQL igual a listarTodos() pero con WHERE p.activo = false en vez de true
        // LEFT JOIN con Imagen para traer la URL de la imagen aunque no tenga (para mostrar en la card)
        String sql = "SELECT p.*, i.url AS imagen_url " +
                     "FROM Producto p " +
                     "LEFT JOIN Imagen i ON p.id_imagen = i.id_imagen " +
                     // WHERE p.activo = false: solo trae los productos desactivados
                     "WHERE p.activo = false ORDER BY p.nombre"; // ORDER BY nombre: orden alfabético

        // try-with-resources: abre conexión, prepara y ejecuta el SELECT en un solo bloque
        // Todo se cierra automáticamente al terminar aunque haya un error
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado y la convertimos en un objeto Producto
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            // Si la consulta falla, imprimimos el error en la consola del servidor
            System.err.println("Error al listar inactivos: " + e.getMessage());
        }
        // Retornamos la lista de productos desactivados (puede estar vacía si no hay ninguno)
        return lista;
    }

    // ─────────────────────────────────────────────
    // ACTIVAR (restaurar producto desactivado)
    // ─────────────────────────────────────────────
    // Método que cambia activo = true para que el producto vuelva a aparecer en el catálogo
    // El admin lo usa cuando quiere restaurar un producto que había desactivado antes
    public boolean activar(int id) {
        // Consulta UPDATE que solo cambia el campo activo a true
        // No toca ningún otro campo del producto (precio, stock, etc. quedan igual)
        String sql = "UPDATE Producto SET activo = true WHERE id_producto = ?";

        // try-with-resources: abre conexión y prepara el UPDATE
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del producto que queremos restaurar al parámetro "?"
            ps.setInt(1, id);
            // Ejecutamos el UPDATE y retornamos true si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si hay error de BD, lo registramos y retornamos false
            System.err.println("Error al activar producto: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────
    // DESACTIVAR (borrado lógico)
    // ─────────────────────────────────────────────
    // En vez de hacer DELETE (que borraría el producto para siempre), hacemos un UPDATE
    // Cambiamos activo = false: el producto desaparece del catálogo pero SÍ sigue en la BD
    // Esto protege el historial de ventas: detalle_pedido sigue apuntando al producto sin error
    // Si se hubiera hecho DELETE, el historial perdería los registros de ese producto
    public boolean eliminar(int id) {
        // UPDATE simple: no necesitamos transacción porque solo tocamos una tabla
        // Antes se hacía DELETE en detalle_pedido + DELETE en Producto (dos operaciones)
        // Ahora con borrado lógico es solo un UPDATE en una tabla
        String sql = "UPDATE Producto SET activo = false WHERE id_producto = ?";

        // try-with-resources: abre conexión y prepara el UPDATE
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del producto que queremos desactivar al parámetro "?"
            ps.setInt(1, id);
            // Ejecutamos el UPDATE; retorna true si se modificó al menos una fila (el producto existía)
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si hay error de BD, lo registramos en la consola y retornamos false
            System.err.println("Error al desactivar producto: " + e.getMessage());
            return false;
        }
    }
}

// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Producto para usarlo en la lista de stock bajo
import modelos.Producto;

// Importamos BigDecimal para manejar valores monetarios con precisión (evita errores de redondeo con double)
import java.math.BigDecimal;
// Importamos las clases de JDBC necesarias para interactuar con la BD
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para reportes y estadísticas del sistema.
 * Provee consultas de resumen sobre ventas, stock, deudas y usuarios
 * para el módulo de reportes del panel administrativo.
 */
// Clase que genera los datos estadísticos y de resumen para el panel de reportes
// No tiene tabla propia; consulta varias tablas del sistema
public class ReporteDAO {

    /**
     * Calcula el total de ventas del mes en curso.
     * @return suma total de ventas del mes actual; BigDecimal.ZERO si no hay ventas
     */
    // Método que calcula la suma de todas las ventas realizadas en el mes actual
    public BigDecimal totalVentasMes() {
        // Consulta SQL que suma el campo "total" de todos los pedidos del mes actual
        // MONTH(CURDATE()) obtiene el número del mes actual (1-12)
        // YEAR(CURDATE()) obtiene el año actual para no mezclar con meses de otros años
        // COALESCE retorna 0 si no hay ventas (evita que el resultado sea NULL)
        String sql = "SELECT COALESCE(SUM(total), 0) FROM Pedido " +
                     "WHERE MONTH(fecha_venta) = MONTH(CURDATE()) " +
                     "AND YEAR(fecha_venta) = YEAR(CURDATE())";

        // Abrimos conexión, preparamos y ejecutamos la consulta
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Si hay resultado, retornamos la suma total de ventas del mes
            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular ventas del mes: " + e.getMessage());
        }
        // Si no hay ventas o hubo error, retornamos cero
        return BigDecimal.ZERO;
    }

    /**
     * Retorna los productos cuyo stock actual es menor o igual al stock mínimo.
     * Ordenados de menor a mayor stock para priorizar los más críticos.
     * @return lista de productos con stock bajo o crítico
     */
    // Método que obtiene los productos que necesitan reabastecimiento (stock bajo)
    // Útil para alertar al administrador sobre productos que se están agotando
    public List<Producto> productosStockBajo() {
        // Lista vacía para almacenar los productos con stock bajo
        List<Producto> lista = new ArrayList<>();
        // Consulta que selecciona productos donde el stock actual es menor o igual al stock mínimo
        // Ordenamos de menor a mayor stock para que los más urgentes aparezcan primero
        String sql = "SELECT id_producto, id_categoria, id_imagen, nombre, precio, " +
                     "stock, stock_minimo, descripcion, fecha_vencimiento, unidad_medida " +
                     "FROM Producto WHERE stock <= stock_minimo ORDER BY stock ASC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada producto con stock bajo
            while (rs.next()) {
                // Creamos un objeto Producto vacío y lo llenamos con los datos de la BD
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));       // ID del producto
                p.setIdCategoria(rs.getInt("id_categoria"));     // Categoría del producto
                p.setIdImagen(rs.getInt("id_imagen"));           // ID de su imagen
                p.setNombre(rs.getString("nombre"));             // Nombre del producto
                p.setPrecio(rs.getBigDecimal("precio"));         // Precio del producto
                p.setStock(rs.getInt("stock"));                  // Stock actual (bajo)
                p.setStockMinimo(rs.getInt("stock_minimo"));     // Stock mínimo configurado
                p.setDescripcion(rs.getString("descripcion"));   // Descripción del producto
                // Obtenemos la fecha de vencimiento (puede ser NULL)
                java.sql.Date fecha = rs.getDate("fecha_vencimiento");
                // Solo asignamos si no es null; convertimos de java.sql.Date a LocalDate
                if (fecha != null) p.setFechaVencimiento(fecha.toLocalDate());
                // Asignamos la unidad de medida
                p.setUnidadMedida(rs.getString("unidad_medida"));
                // Agregamos el producto a la lista de stock bajo
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        // Retornamos la lista de productos con stock bajo
        return lista;
    }

    /**
     * Calcula la suma total de montos pendientes en deudas activas.
     * @return total de deudas pendientes; BigDecimal.ZERO si no hay deudas
     */
    // Método que calcula cuánto dinero se debe en total por ventas a crédito
    public BigDecimal totalDeudasPendientes() {
        // SUM suma todos los montos pendientes de deudas con estado 'activa'
        // COALESCE retorna 0 si no hay deudas activas (evita NULL)
        // NOTA: las deudas se crean con estado 'activa' y pasan a 'pagada' cuando se saldan
        String sql = "SELECT COALESCE(SUM(monto_pendiente), 0) FROM Deuda WHERE estado = 'activa'";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Si hay resultado, retornamos la suma total de deudas pendientes
            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular deudas pendientes: " + e.getMessage());
        }
        // Si no hay deudas o hubo error, retornamos cero
        return BigDecimal.ZERO;
    }

    /**
     * Cuenta el número total de clientes registrados en la tabla Cliente.
     * @return cantidad de clientes registrados; 0 si no hay ninguno
     */
    // Método que cuenta cuántos clientes hay registrados en el sistema
    // Consulta la tabla Cliente (no Usuario), que es donde se registran los clientes de la tienda
    public int contarClientes() {
        // COUNT(*) cuenta todos los registros de la tabla Cliente
        String sql = "SELECT COUNT(*) FROM Cliente";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Si hay resultado, retornamos el conteo de empleados
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error al contar clientes: " + e.getMessage());
        }
        // Si hubo error, retornamos 0
        return 0;
    }
}

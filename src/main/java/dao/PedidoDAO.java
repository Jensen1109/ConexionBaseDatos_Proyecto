// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración para obtener la conexión a la BD MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos los modelos que representan las tablas Pedido y detalle_pedido
import modelos.DetallePedido;
import modelos.Pedido;

// Importamos las clases de JDBC necesarias para trabajar con la BD
import java.sql.Connection;         // Conexión abierta con la BD
import java.sql.Date;               // Clase para fechas SQL
import java.sql.PreparedStatement;   // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;           // Resultados de consultas SELECT
import java.sql.SQLException;        // Excepción para errores de BD
import java.sql.Statement;           // Constantes como RETURN_GENERATED_KEYS
import java.sql.Timestamp;           // Clase para fechas con hora (datetime en MySQL)
import java.sql.Types;               // Constantes de tipos SQL (INTEGER, VARCHAR, etc.)
import java.time.LocalDate;          // Clase moderna de Java para fechas sin hora
import java.util.ArrayList;          // Lista dinámica para almacenar resultados
import java.util.List;               // Interfaz de lista

/**
 * DAO para la tabla Pedido y detalle_pedido.
 * Maneja creación de ventas con transacciones atómicas y actualización de stock.
 */
// Clase que gestiona todas las operaciones de ventas (pedidos) en la base de datos
public class PedidoDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Pedido
    // ─────────────────────────────────────────────
    // Método privado que convierte una fila del ResultSet en un objeto Pedido de Java
    private Pedido mapear(ResultSet rs) throws SQLException {
        // Creamos un objeto Pedido vacío para llenarlo con datos de la BD
        Pedido p = new Pedido();
        // Extraemos el ID único del pedido
        p.setIdPedido(rs.getInt("id_pedido"));
        // Extraemos el ID del cliente; puede ser 0 si el campo es NULL en la BD (venta sin cliente registrado)
        p.setIdCliente(rs.getInt("id_cliente")); // puede ser 0 si NULL en BD
        // Extraemos el ID del usuario (vendedor) que registró la venta
        p.setIdUsuario(rs.getInt("id_usuario"));
        // Extraemos el ID del método de pago utilizado (efectivo, nequi, tarjeta, etc.)
        p.setIdPago(rs.getInt("id_pago"));
        // Obtenemos la fecha y hora de la venta como Timestamp (incluye fecha + hora)
        Timestamp ts = rs.getTimestamp("fecha_venta");
        // Convertimos el Timestamp a LocalDateTime solo si no es null
        if (ts != null) p.setFechaVenta(ts.toLocalDateTime());
        // Extraemos el total de la venta (BigDecimal para precisión monetaria)
        p.setTotal(rs.getBigDecimal("total"));
        // Extraemos el estado del pedido ("pagado" o "credito")
        p.setEstado(rs.getString("estado"));
        // Retornamos el objeto Pedido completo
        return p;
    }

    /**
     * Crea un pedido con sus detalles en una sola transacción atómica.
     * Descuenta el stock de cada producto. Rechaza si stock resultante quedaría negativo.
     * @param pedido  objeto Pedido con los datos de la venta
     * @param detalles lista de ítems de la venta
     * @return true si la transacción fue exitosa
     */
    // Método principal para crear una venta completa (pedido + detalles + descuento de stock)
    // Usa una transacción atómica: o se ejecuta TODO o no se ejecuta NADA
    public boolean crear(Pedido pedido, List<DetallePedido> detalles) {
        // SQL para insertar el pedido principal; NOW() pone la fecha y hora actual del servidor
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        // SQL para insertar cada línea de detalle (cada producto vendido en este pedido)
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        // SQL para descontar el stock del producto; la condición "stock >= ?" asegura que no quede negativo
        String sqlStock   = "UPDATE Producto SET stock = stock - ? " +
                            "WHERE id_producto = ? AND stock >= ?";

        // Variable para la conexión; la declaramos fuera del try porque la necesitamos en catch y finally
        Connection con = null;
        try {
            // Abrimos la conexión a la base de datos
            con = conexion.getConnection();
            // Desactivamos el autocommit para iniciar una transacción manual
            // Los cambios no se guardan hasta que llamemos con.commit()
            con.setAutoCommit(false);

            // 1. Insertar el pedido y obtener el ID generado
            // Variable para guardar el ID del pedido recién creado
            int idPedidoNuevo;
            // RETURN_GENERATED_KEYS le dice a MySQL que nos devuelva el ID auto-generado
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                // Si el pedido tiene un cliente asociado, asignamos su ID; si no, ponemos NULL
                if (pedido.getIdCliente() > 0) ps.setInt(1, pedido.getIdCliente());
                else                           ps.setNull(1, Types.INTEGER);
                // Asignamos el ID del vendedor que registra la venta
                ps.setInt(2, pedido.getIdUsuario());
                // Asignamos el ID del método de pago
                ps.setInt(3, pedido.getIdPago());
                // Asignamos el total de la venta
                ps.setBigDecimal(4, pedido.getTotal());
                // Asignamos el estado (ej: "pagado" o "credito")
                ps.setString(5, pedido.getEstado());
                // Ejecutamos el INSERT del pedido
                ps.executeUpdate();

                // Obtenemos el ID auto-generado por MySQL para el pedido recién insertado
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    // Si no se generó un ID, algo salió mal y lanzamos una excepción
                    if (!keys.next()) throw new SQLException("No se obtuvo el ID del pedido.");
                    // Guardamos el ID generado para usarlo en los detalles
                    idPedidoNuevo = keys.getInt(1);
                }
            }

            // 2. Insertar detalles y descontar stock (verificando stock suficiente)
            // Preparamos dos sentencias: una para los detalles y otra para actualizar el stock
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock   = con.prepareStatement(sqlStock)) {

                // Recorremos cada producto que se vendió en este pedido
                for (DetallePedido d : detalles) {
                    // Preparamos el INSERT del detalle con el ID del pedido recién creado
                    psDetalle.setInt(1, idPedidoNuevo);           // ID del pedido al que pertenece este detalle
                    psDetalle.setInt(2, d.getIdProducto());       // ID del producto vendido
                    psDetalle.setInt(3, d.getCantidadVendida());  // Cantidad vendida de ese producto
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario()); // Precio unitario al momento de la venta
                    // addBatch() acumula la operación para ejecutarla en lote (más eficiente)
                    psDetalle.addBatch();

                    // Preparamos el UPDATE para descontar el stock del producto
                    psStock.setInt(1, d.getCantidadVendida());    // Cantidad a descontar del stock
                    psStock.setInt(2, d.getIdProducto());         // ID del producto al que descontamos
                    psStock.setInt(3, d.getCantidadVendida());    // stock >= cantidad requerida (verificación)
                    // Acumulamos la operación de stock en el batch
                    psStock.addBatch();
                }
                // Ejecutamos todos los INSERTs de detalles de una sola vez (batch = lote)
                psDetalle.executeBatch();
                // Ejecutamos todos los UPDATEs de stock de una sola vez
                int[] stockUpdates = psStock.executeBatch();

                // Verificar que todos los stocks se actualizaron (0 = stock insuficiente)
                // Si algún UPDATE afectó 0 filas, significa que no había suficiente stock
                for (int updated : stockUpdates) {
                    if (updated == 0) throw new SQLException("Stock insuficiente para uno o más productos.");
                }
            }

            // Si todo salió bien, confirmamos la transacción (se guardan todos los cambios)
            con.commit();
            return true;

        } catch (SQLException e) {
            // Si hubo cualquier error, mostramos el mensaje en la consola
            System.err.println("Error al crear pedido: " + e.getMessage());
            // Hacemos rollback para deshacer TODOS los cambios de esta transacción
            // Así no quedan datos parciales (ej: pedido sin detalles, o stock descontado sin pedido)
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // El bloque finally siempre se ejecuta, haya error o no
            // Restauramos el autocommit y cerramos la conexión para liberar recursos
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
    // Método similar a crear() pero retorna el ID del pedido en vez de boolean
    // Se usa cuando necesitamos el ID para crear una Deuda asociada (venta a crédito)
    public int registrar(Pedido pedido, List<DetallePedido> detalles) {
        // SQL para insertar el pedido con la fecha actual del servidor MySQL
        String sqlPedido  = "INSERT INTO Pedido (id_cliente, id_usuario, id_pago, fecha_venta, total, estado) " +
                            "VALUES (?, ?, ?, NOW(), ?, ?)";
        // SQL para insertar cada detalle (producto) del pedido
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad_vendida, precio_unitario) " +
                            "VALUES (?, ?, ?, ?)";
        // SQL para descontar stock verificando que haya suficiente
        String sqlStock   = "UPDATE Producto SET stock = stock - ? " +
                            "WHERE id_producto = ? AND stock >= ?";

        // Variable de conexión fuera del try para poder usarla en catch/finally
        Connection con = null;
        try {
            // Abrimos la conexión
            con = conexion.getConnection();
            // Iniciamos transacción manual desactivando autocommit
            con.setAutoCommit(false);

            // Variable para guardar el ID del nuevo pedido
            int idNuevo;
            // Insertamos el pedido y solicitamos que MySQL nos devuelva el ID generado
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                // Asignamos cliente (o NULL si no hay cliente registrado)
                if (pedido.getIdCliente() > 0) ps.setInt(1, pedido.getIdCliente());
                else                           ps.setNull(1, Types.INTEGER);
                // Asignamos el vendedor, método de pago, total y estado
                ps.setInt(2, pedido.getIdUsuario());
                ps.setInt(3, pedido.getIdPago());
                ps.setBigDecimal(4, pedido.getTotal());
                ps.setString(5, pedido.getEstado());
                // Ejecutamos el INSERT
                ps.executeUpdate();

                // Obtenemos el ID auto-generado del pedido
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se obtuvo el ID del pedido.");
                    idNuevo = keys.getInt(1);
                }
            }

            // Insertamos los detalles y descontamos stock usando batch
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                 PreparedStatement psS = con.prepareStatement(sqlStock)) {

                // Recorremos cada producto vendido
                for (DetallePedido d : detalles) {
                    // Preparamos el INSERT del detalle
                    psD.setInt(1, idNuevo);                   // ID del pedido recién creado
                    psD.setInt(2, d.getIdProducto());         // ID del producto
                    psD.setInt(3, d.getCantidadVendida());    // Cantidad vendida
                    psD.setBigDecimal(4, d.getPrecioUnitario()); // Precio unitario
                    psD.addBatch();  // Acumulamos en el lote

                    // Preparamos el descuento de stock
                    psS.setInt(1, d.getCantidadVendida());    // Cantidad a restar
                    psS.setInt(2, d.getIdProducto());         // Producto al que restamos
                    psS.setInt(3, d.getCantidadVendida());    // Verificamos que haya suficiente stock
                    psS.addBatch();  // Acumulamos en el lote
                }
                // Ejecutamos todos los INSERTs de detalles en lote
                psD.executeBatch();
                // Ejecutamos todos los UPDATEs de stock en lote
                int[] stockUpdates = psS.executeBatch();

                // Verificamos que todos los descuentos de stock fueron exitosos
                for (int updated : stockUpdates) {
                    // Si algún UPDATE no afectó filas, no había stock suficiente
                    if (updated == 0) throw new SQLException("Stock insuficiente para uno o más productos.");
                }
            }

            // Confirmamos la transacción: todos los cambios se guardan
            con.commit();
            // Retornamos el ID del pedido creado para que el controlador lo use (ej: crear deuda)
            return idNuevo;

        } catch (SQLException e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
            // Deshacemos todos los cambios si hubo error
            if (con != null) try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            // Retornamos -1 para indicar que la operación falló
            return -1;
        } finally {
            // Restauramos autocommit y cerramos la conexión
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    /**
     * Lista todos los pedidos con nombre del cliente (JOIN con tabla Cliente).
     * @return lista de pedidos ordenados por fecha descendente
     */
    // Método que obtiene todos los pedidos con el nombre completo del cliente
    public List<Pedido> listarConCliente() {
        // Lista vacía para almacenar los pedidos encontrados
        List<Pedido> lista = new ArrayList<>();
        // Consulta con LEFT JOIN: une Pedido con Cliente para obtener el nombre del comprador
        // CONCAT une nombre y apellido en un solo campo "nombre_cliente"
        // LEFT JOIN asegura que aparezcan pedidos incluso si no tienen cliente registrado
        String sql = "SELECT p.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente " +
                     "FROM Pedido p " +
                     "LEFT JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                     "ORDER BY p.fecha_venta DESC";  // Ordenamos del más reciente al más antiguo

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado
            while (rs.next()) {
                // Mapeamos los datos básicos del pedido
                Pedido p = mapear(rs);
                // Asignamos el nombre del cliente que viene del JOIN
                p.setNombreCliente(rs.getString("nombre_cliente"));
                // Agregamos el pedido a la lista
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos con cliente: " + e.getMessage());
        }
        // Retornamos la lista de pedidos con nombre de cliente
        return lista;
    }

    /**
     * Lista pedidos en un rango de fechas (para filtro en historial).
     * @param inicio fecha inicio (inclusive)
     * @param fin    fecha fin (inclusive)
     * @return lista de pedidos en ese rango
     */
    // Método que filtra pedidos por un rango de fechas (útil para el historial de ventas)
    public List<Pedido> listarPorFechas(LocalDate inicio, LocalDate fin) {
        // Lista vacía para almacenar los pedidos del rango
        List<Pedido> lista = new ArrayList<>();
        // Consulta con filtro BETWEEN para el rango de fechas
        // DATE() extrae solo la fecha de fecha_venta (que es datetime con hora)
        String sql = "SELECT p.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente " +
                     "FROM Pedido p " +
                     "LEFT JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                     "WHERE DATE(p.fecha_venta) BETWEEN ? AND ? " +  // Filtro por rango de fechas
                     "ORDER BY p.fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos la fecha de inicio al primer parámetro; valueOf convierte LocalDate a java.sql.Date
            ps.setDate(1, Date.valueOf(inicio));
            // Asignamos la fecha de fin al segundo parámetro
            ps.setDate(2, Date.valueOf(fin));

            // Ejecutamos la consulta filtrada
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapeamos cada pedido y asignamos el nombre del cliente
                    Pedido p = mapear(rs);
                    p.setNombreCliente(rs.getString("nombre_cliente"));
                    lista.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar pedidos por fechas: " + e.getMessage());
        }
        // Retornamos los pedidos dentro del rango de fechas
        return lista;
    }

    /**
     * Lista los pedidos de un cliente específico.
     * @param idCliente id del cliente
     * @return lista de pedidos del cliente
     */
    // Método que obtiene todos los pedidos realizados por un cliente en particular
    public List<Pedido> listarPorCliente(int idCliente) {
        // Lista vacía para almacenar los pedidos del cliente
        List<Pedido> lista = new ArrayList<>();
        // Consulta simple filtrada por id_cliente, ordenada por fecha descendente
        String sql = "SELECT * FROM Pedido WHERE id_cliente = ? ORDER BY fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del cliente
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos y mapeamos cada pedido del cliente
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
    // Método que obtiene TODOS los pedidos del sistema (sin filtros, para vista de administrador)
    public List<Pedido> listarTodos() {
        // Lista vacía para todos los pedidos
        List<Pedido> lista = new ArrayList<>();
        // Consulta simple que trae todos los pedidos ordenados del más reciente al más antiguo
        String sql = "SELECT * FROM Pedido ORDER BY fecha_venta DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos y mapeamos cada fila
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
    // Método que obtiene los productos que conforman un pedido específico
    public List<DetallePedido> listarDetalles(int idPedido) {
        // Lista vacía para los detalles del pedido
        List<DetallePedido> lista = new ArrayList<>();
        // Consulta con JOIN que trae los detalles junto con el nombre del producto
        // "dp" es alias para detalle_pedido, "p" para Producto
        String sql = "SELECT dp.*, p.nombre AS nombre_producto " +
                     "FROM detalle_pedido dp " +
                     "JOIN Producto p ON dp.id_producto = p.id_producto " +  // JOIN para traer el nombre del producto
                     "WHERE dp.id_pedido = ?";  // Filtramos por el ID del pedido

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del pedido cuyos detalles queremos ver
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos cada detalle (cada producto vendido en ese pedido)
                while (rs.next()) {
                    // Creamos un objeto DetallePedido y llenamos sus campos
                    DetallePedido d = new DetallePedido();
                    d.setIdDetalle(rs.getInt("id_detalle"));          // ID único del detalle
                    d.setIdPedido(rs.getInt("id_pedido"));            // ID del pedido al que pertenece
                    d.setIdProducto(rs.getInt("id_producto"));        // ID del producto vendido
                    d.setCantidadVendida(rs.getInt("cantidad_vendida")); // Cantidad vendida
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario")); // Precio al momento de la venta
                    d.setNombreProducto(rs.getString("nombre_producto")); // Nombre del producto (viene del JOIN con Producto)
                    // Agregamos el detalle a la lista
                    lista.add(d);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar detalles: " + e.getMessage());
        }
        // Retornamos la lista de detalles del pedido
        return lista;
    }

    /**
     * Obtiene un pedido por su ID.
     * @param id id_pedido
     * @return Pedido o null
     */
    // Método que busca un pedido específico por su identificador
    public Pedido obtenerPorId(int id) {
        // Consulta simple que busca un pedido por su ID
        String sql = "SELECT * FROM Pedido WHERE id_pedido = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del pedido a buscar
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos el pedido, lo mapeamos y retornamos
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener pedido por id: " + e.getMessage());
        }
        // Retornamos null si no se encontró el pedido
        return null;
    }

    /**
     * Cambia el estado de un pedido.
     * @param idPedido    ID del pedido
     * @param nuevoEstado nuevo estado ('pagado' o 'credito')
     * @return true si se actualizó
     */
    // Método que actualiza el estado de un pedido (por ejemplo, de "credito" a "pagado")
    public boolean cambiarEstado(int idPedido, String nuevoEstado) {
        // Consulta UPDATE que modifica solo el campo estado
        String sql = "UPDATE Pedido SET estado = ? WHERE id_pedido = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el nuevo estado
            ps.setString(1, nuevoEstado);
            // Asignamos el ID del pedido a actualizar
            ps.setInt(2, idPedido);
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del pedido: " + e.getMessage());
            return false;
        }
    }
}

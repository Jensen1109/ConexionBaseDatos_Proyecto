// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración para obtener la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Deuda que representa la tabla Deuda en la BD
import modelos.Deuda;

// Importamos BigDecimal para manejar valores monetarios con precisión (evita errores de redondeo)
import java.math.BigDecimal;
// Importamos las clases de JDBC necesarias
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.Date;              // Clase para fechas SQL
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para la tabla Deuda.
 * Gestiona deudas generadas por ventas a crédito y registro de abonos.
 * Estados válidos: 'activa' | 'pagada'
 */
// Clase que gestiona todas las operaciones de deudas (ventas fiadas/a crédito) en la BD
public class DeudaDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Deuda
    // ─────────────────────────────────────────────
    // Método privado que convierte una fila del ResultSet en un objeto Deuda de Java
    private Deuda mapear(ResultSet rs) throws SQLException {
        // Creamos un objeto Deuda vacío para llenarlo con los datos de la BD
        Deuda d = new Deuda();
        // Extraemos el ID único de la deuda
        d.setIdDeuda(rs.getInt("id_deuda"));
        // Extraemos el ID del pedido que originó esta deuda (la venta a crédito)
        d.setIdPedido(rs.getInt("id_pedido"));
        // Extraemos el monto que aún se debe (BigDecimal para precisión monetaria)
        d.setMontoPendiente(rs.getBigDecimal("monto_pendiente"));
        // Extraemos el estado de la deuda: 'activa' (aún debe) o 'pagada' (ya pagó todo)
        d.setEstado(rs.getString("estado"));
        // Extraemos el último abono realizado
        d.setAbono(rs.getBigDecimal("abono"));
        // Obtenemos la fecha del último abono (puede ser NULL si no ha abonado aún)
        Date fecha = rs.getDate("fecha_abono");
        // Solo asignamos la fecha si no es null; toLocalDate() convierte de java.sql.Date a LocalDate
        if (fecha != null) d.setFechaAbono(fecha.toLocalDate());
        // Retornamos el objeto Deuda completo
        return d;
    }

    /**
     * Lista todas las deudas ordenadas por ID descendente.
     * @return lista de todas las deudas
     */
    // Método que obtiene todas las deudas registradas, ordenadas de la más reciente a la más antigua
    public List<Deuda> listarTodas() {
        // Creamos una lista vacía para almacenar las deudas
        List<Deuda> lista = new ArrayList<>();
        // Consulta que trae todas las deudas ordenadas por ID de mayor a menor (más recientes primero)
        String sql = "SELECT * FROM Deuda ORDER BY id_deuda DESC";

        // Abrimos conexión, preparamos y ejecutamos la consulta
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado y la mapeamos a un objeto Deuda
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar deudas: " + e.getMessage());
        }
        // Retornamos la lista de todas las deudas
        return lista;
    }

    /**
     * Lista solo las deudas activas.
     * @return lista de deudas con estado 'activa'
     */
    // Método que obtiene solo las deudas que aún están pendientes de pago
    public List<Deuda> listarActivas() {
        // Lista vacía para las deudas activas
        List<Deuda> lista = new ArrayList<>();
        // Filtramos solo las deudas con estado 'activa' (aún no se han pagado completamente)
        String sql = "SELECT * FROM Deuda WHERE estado = 'activa' ORDER BY id_deuda DESC";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos y mapeamos cada deuda activa
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar deudas activas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista deudas activas con nombre del cliente (JOIN con Pedido → Cliente).
     * @return lista de deudas activas con nombreCliente poblado
     */
    // Método que obtiene las deudas activas junto con el nombre del cliente que debe
    // Usa dos JOINs: Deuda → Pedido → Cliente para obtener el nombre
    public List<Deuda> listarActivasConCliente() {
        // Lista vacía para las deudas con información del cliente
        List<Deuda> lista = new ArrayList<>();
        // Consulta con doble LEFT JOIN para llegar del Deuda al nombre del Cliente
        // Deuda se conecta con Pedido por id_pedido, y Pedido se conecta con Cliente por id_cliente
        // CONCAT une nombre y apellido del cliente en un solo campo
        String sql = "SELECT d.*, " +
                     "CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente " +
                     "FROM Deuda d " +
                     "LEFT JOIN Pedido pe ON d.id_pedido = pe.id_pedido " +    // Unimos con Pedido
                     "LEFT JOIN Cliente c ON pe.id_cliente = c.id_cliente " +  // Unimos con Cliente
                     "WHERE d.estado = 'activa' " +                            // Solo deudas activas
                     "ORDER BY d.id_deuda DESC";                               // Más recientes primero

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Mapeamos los datos básicos de la deuda
                Deuda d = mapear(rs);
                // Asignamos el nombre del cliente que viene del doble JOIN
                d.setNombreCliente(rs.getString("nombre_cliente"));
                // Agregamos la deuda con nombre de cliente a la lista
                lista.add(d);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar deudas con cliente: " + e.getMessage());
        }
        // Retornamos las deudas activas con nombre del cliente
        return lista;
    }

    /**
     * Obtiene una deuda por su ID.
     * @param idDeuda ID de la deuda
     * @return Deuda o null
     */
    // Método que busca una deuda específica por su identificador
    public Deuda buscarPorId(int idDeuda) {
        // Consulta simple que busca por id_deuda
        String sql = "SELECT * FROM Deuda WHERE id_deuda = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID de la deuda a buscar
            ps.setInt(1, idDeuda);
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos la deuda, la mapeamos y retornamos
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar deuda: " + e.getMessage());
        }
        // Retornamos null si no se encontró la deuda
        return null;
    }

    /**
     * Registra un abono a una deuda.
     * Si el abono cubre el total pendiente, el estado pasa a 'pagada'.
     * Rechaza si el abono supera el monto pendiente.
     * @param idDeuda    ID de la deuda
     * @param montoAbono monto a abonar
     * @return true si se registró, false si el abono supera el pendiente
     */
    // Método para registrar un pago parcial (abono) o total a una deuda
    public boolean abonar(int idDeuda, BigDecimal montoAbono) {
        // Primero obtenemos la deuda actual para verificar el monto pendiente
        Deuda actual = buscarPorId(idDeuda);
        // Si no existe la deuda, retornamos false
        if (actual == null) return false;
        // Verificamos que el abono no sea mayor que lo que se debe
        // compareTo retorna > 0 si montoAbono es mayor que montoPendiente
        if (montoAbono.compareTo(actual.getMontoPendiente()) > 0) return false;

        // Consulta UPDATE que registra el abono, actualiza la fecha y resta del monto pendiente
        // CASE WHEN: si después del abono el pendiente llega a 0, cambia el estado a 'pagada'
        String sql = "UPDATE Deuda SET " +
                     "abono = ?, " +                                         // Guardamos el monto del abono
                     "fecha_abono = CURDATE(), " +                           // Registramos la fecha actual del abono
                     "monto_pendiente = monto_pendiente - ?, " +             // Restamos el abono del pendiente
                     "estado = CASE WHEN monto_pendiente <= 0 THEN 'pagada' ELSE 'activa' END " +  // Cambiamos estado si ya pagó todo
                     "WHERE id_deuda = ?";                                   // Identificamos la deuda

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el monto del abono para el campo "abono"
            ps.setBigDecimal(1, montoAbono);
            // Asignamos el monto del abono para restar del monto_pendiente
            ps.setBigDecimal(2, montoAbono);
            // Asignamos el ID de la deuda a abonar
            ps.setInt(3, idDeuda);
            // Ejecutamos el UPDATE y verificamos si se modificó la fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar abono: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra una nueva deuda vinculada a un pedido a crédito.
     * @param d objeto Deuda con id_pedido y monto_pendiente
     * @return true si se insertó
     */
    // Método para crear una nueva deuda cuando se hace una venta a crédito (fiado)
    public boolean registrarDeuda(Deuda d) {
        // INSERT que crea una deuda con estado 'activa' y abono inicial de 0
        String sql = "INSERT INTO Deuda (id_pedido, monto_pendiente, estado, abono) " +
                     "VALUES (?, ?, 'activa', 0)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del pedido que generó la deuda
            ps.setInt(1, d.getIdPedido());
            // Asignamos el monto total que se debe (inicialmente igual al total de la venta)
            ps.setBigDecimal(2, d.getMontoPendiente());
            // Ejecutamos el INSERT y verificamos si se insertó
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar deuda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista las deudas de un cliente específico (via Pedido).
     * @param idCliente ID del cliente
     * @return lista de deudas del cliente
     */
    // Método que obtiene todas las deudas de un cliente específico
    // Usa JOIN con Pedido porque la deuda no tiene id_cliente directamente, sino a través del pedido
    public List<Deuda> listarPorCliente(int idCliente) {
        // Lista vacía para las deudas del cliente
        List<Deuda> lista = new ArrayList<>();
        // JOIN entre Deuda y Pedido para filtrar por id_cliente
        String sql = "SELECT d.* FROM Deuda d " +
                     "JOIN Pedido pe ON d.id_pedido = pe.id_pedido " +  // Unimos deuda con pedido
                     "WHERE pe.id_cliente = ? " +                        // Filtramos por cliente
                     "ORDER BY d.id_deuda DESC";                         // Más recientes primero

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del cliente
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos y mapeamos cada deuda del cliente
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar deudas por cliente: " + e.getMessage());
        }
        // Retornamos las deudas del cliente
        return lista;
    }

    /**
     * Calcula el total de deudas activas.
     * @return suma de monto_pendiente con estado 'activa'
     */
    // Método que calcula la suma total de todas las deudas activas del sistema
    public BigDecimal totalPendiente() {
        // SUM suma todos los montos pendientes; COALESCE retorna 0 si no hay deudas (evita NULL)
        String sql = "SELECT COALESCE(SUM(monto_pendiente), 0) FROM Deuda WHERE estado = 'activa'";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Si hay resultado, retornamos la suma total de deudas activas
            if (rs.next()) return rs.getBigDecimal(1);

        } catch (SQLException e) {
            System.err.println("Error al calcular total deudas: " + e.getMessage());
        }
        // Si no hay deudas o hubo error, retornamos BigDecimal.ZERO (cero)
        return BigDecimal.ZERO;
    }

    // ── Alias para compatibilidad con código existente ──

    /** @see #abonar(int, BigDecimal) */
    // Método alias que redirige a abonar() - existe para mantener compatibilidad con código antiguo
    public boolean registrarAbono(int idDeuda, BigDecimal montoAbono) {
        // Simplemente llama al método abonar() con los mismos parámetros
        return abonar(idDeuda, montoAbono);
    }

    /** @see #listarActivasConCliente() */
    // Método alias que redirige a listarActivasConCliente() - existe para compatibilidad
    public List<Deuda> listarPendientesConCliente() {
        // Simplemente llama al método listarActivasConCliente()
        return listarActivasConCliente();
    }
}

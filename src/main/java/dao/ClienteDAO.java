// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object)
package dao;

// Importamos la clase de configuración que nos da la conexión a MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Cliente que representa la tabla Cliente en la BD
import modelos.Cliente;

// Importamos las clases de JDBC necesarias para interactuar con la base de datos
import java.sql.Connection;        // Conexión abierta con la BD
import java.sql.PreparedStatement;  // Consultas parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Resultados de consultas SELECT
import java.sql.SQLException;       // Excepción para errores de BD
import java.util.ArrayList;         // Lista dinámica para almacenar resultados
import java.util.List;              // Interfaz de lista

/**
 * DAO para la tabla Cliente.
 * Gestiona el CRUD de clientes de la tienda.
 * Los teléfonos del cliente se gestionan por separado en TelefonoDAO.
 */
// Clase pública que maneja todas las operaciones de la tabla Cliente en la BD
public class ClienteDAO {

    /**
     * Mapea una fila del ResultSet a un objeto Cliente.
     * @param rs ResultSet posicionado en la fila a mapear
     * @return objeto Cliente con los datos de la fila
     */
    // Método privado que convierte una fila del ResultSet en un objeto Cliente de Java
    private Cliente mapear(ResultSet rs) throws SQLException {
        // Creamos un objeto Cliente vacío para llenarlo con los datos de la BD
        Cliente c = new Cliente();
        // Extraemos el ID único del cliente
        c.setIdCliente(rs.getInt("id_cliente"));
        // Extraemos el nombre del cliente
        c.setNombre(rs.getString("nombre"));
        // Extraemos el apellido del cliente
        c.setApellido(rs.getString("apellido"));
        // Extraemos la cédula (documento de identidad) del cliente
        c.setCedula(rs.getString("cedula"));
        // Extraemos el correo electrónico del cliente
        c.setEmail(rs.getString("email"));
        // Retornamos el objeto Cliente completo con todos sus datos
        return c;
    }

    /**
     * Lista todos los clientes ordenados por nombre.
     * @return lista de clientes; lista vacía si no hay ninguno
     */
    // Método que obtiene todos los clientes registrados en la BD, ordenados alfabéticamente
    public List<Cliente> listar() {
        // Creamos una lista vacía donde guardaremos los clientes encontrados
        List<Cliente> lista = new ArrayList<>();
        // Consulta SQL que trae todos los campos de la tabla Cliente ordenados por nombre
        String sql = "SELECT * FROM Cliente ORDER BY nombre";

        // try-with-resources: abre conexión, prepara y ejecuta la consulta; todo se cierra automáticamente
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila del resultado y la mapeamos a un objeto Cliente
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            // Si hay error de BD, lo imprimimos en la consola del servidor para depuración
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        // Retornamos la lista (vacía si no hay clientes o si hubo error)
        return lista;
    }

    /**
     * Busca un cliente por su ID.
     * @param id id_cliente
     * @return Cliente encontrado o null
     */
    // Método que busca un cliente específico por su identificador único
    public Cliente buscarPorId(int id) {
        // Consulta SQL con parámetro "?" para buscar por id_cliente
        String sql = "SELECT * FROM Cliente WHERE id_cliente = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del cliente a buscar al parámetro "?"
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos un resultado, lo mapeamos y retornamos
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        // Retornamos null si el cliente no fue encontrado
        return null;
    }

    /**
     * Crea un nuevo cliente.
     * @param c objeto Cliente con los datos
     * @return true si se insertó correctamente
     */
    // Método para registrar un nuevo cliente en la base de datos
    public boolean crear(Cliente c) {
        // Consulta INSERT con 4 parámetros para nombre, apellido, cédula y email
        String sql = "INSERT INTO Cliente (nombre, apellido, cedula, email) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos cada valor del cliente a su posición en la consulta
            ps.setString(1, c.getNombre());   // Posición 1: nombre del cliente
            ps.setString(2, c.getApellido()); // Posición 2: apellido del cliente
            ps.setString(3, c.getCedula());   // Posición 3: cédula del cliente
            ps.setString(4, c.getEmail());    // Posición 4: email del cliente
            // executeUpdate() ejecuta el INSERT y retorna el número de filas insertadas
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea un cliente y retorna el ID generado (útil al registrar ventas con cliente nuevo).
     * @param c objeto Cliente con los datos
     * @return id_cliente generado, o 0 si falló
     */
    // Método que crea un cliente y devuelve su ID auto-generado por MySQL
    // Útil cuando necesitamos asociar el cliente recién creado a un pedido
    public int crearYObtenerIdCliente(Cliente c) {
        // Misma consulta INSERT que crear()
        String sql = "INSERT INTO Cliente (nombre, apellido, cedula, email) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConnection();
             // RETURN_GENERATED_KEYS le dice a MySQL que nos devuelva el ID auto-generado
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            // Asignamos los datos del cliente a los parámetros
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCedula());
            ps.setString(4, c.getEmail());
            // Ejecutamos el INSERT
            ps.executeUpdate();
            // Obtenemos el ID auto-generado del cliente recién insertado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // Si hay un ID generado, lo retornamos
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
        }
        // Retornamos 0 si algo falló (no se generó ID)
        return 0;
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param c objeto Cliente con los nuevos datos (debe tener idCliente válido)
     * @return true si se actualizó
     */
    // Método para modificar los datos de un cliente ya registrado
    public boolean actualizar(Cliente c) {
        // Consulta UPDATE que modifica nombre, apellido, cédula y email del cliente
        String sql = "UPDATE Cliente SET nombre=?, apellido=?, cedula=?, email=? WHERE id_cliente=?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos los nuevos valores del cliente
            ps.setString(1, c.getNombre());       // Nuevo nombre
            ps.setString(2, c.getApellido());     // Nuevo apellido
            ps.setString(3, c.getCedula());       // Nueva cédula
            ps.setString(4, c.getEmail());        // Nuevo email
            ps.setInt(5, c.getIdCliente());       // ID del cliente a actualizar (cláusula WHERE)
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un cliente tiene pedidos registrados.
     * @param id id_cliente
     * @return true si tiene al menos un pedido
     */
    // Método que comprueba si un cliente tiene pedidos asociados
    // Se usa antes de eliminar un cliente para no borrar datos relacionados
    public boolean tienePedidos(int id) {
        // COUNT(*) cuenta cuántos pedidos tiene ese cliente
        String sql = "SELECT COUNT(*) FROM Pedido WHERE id_cliente = ?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignamos el ID del cliente
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // Si el conteo es mayor a 0, el cliente tiene pedidos
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar pedidos del cliente: " + e.getMessage());
        }
        // Por defecto retornamos false (no tiene pedidos)
        return false;
    }

    /**
     * Elimina un cliente por su ID en una transacción atómica.
     * Solo elimina si el cliente no tiene pedidos registrados.
     * Borra sus teléfonos antes de eliminar el cliente.
     * @param id id_cliente a eliminar
     * @return true si se eliminó, false si tiene pedidos o falló
     */
    // Método que elimina un cliente y sus teléfonos asociados usando una transacción
    public boolean eliminar(int id) {
        // Primero verificamos si el cliente tiene pedidos; si los tiene, no lo eliminamos
        // porque borraríamos historial de ventas importante
        if (tienePedidos(id)) return false;

        // Variable de conexión fuera del try para poder usarla en catch/finally
        Connection con = null;
        try {
            // Abrimos la conexión a la BD
            con = conexion.getConnection();
            // Desactivamos autocommit para manejar la transacción manualmente
            // Así podemos hacer rollback si algo falla a mitad de proceso
            con.setAutoCommit(false);

            // PASO 1: Eliminamos los teléfonos del cliente primero
            // Esto es necesario porque la tabla Telefono tiene una FK (clave foránea) hacia Cliente
            try (PreparedStatement psTel = con.prepareStatement(
                    "DELETE FROM Telefono WHERE cliente_id = ?")) {
                psTel.setInt(1, id);  // ID del cliente cuyos teléfonos eliminamos
                psTel.executeUpdate(); // Ejecutamos la eliminación de teléfonos
            }

            // PASO 2: Ahora eliminamos el cliente de la tabla Cliente
            int filas;
            try (PreparedStatement psCli = con.prepareStatement(
                    "DELETE FROM Cliente WHERE id_cliente = ?")) {
                psCli.setInt(1, id);  // ID del cliente a eliminar
                filas = psCli.executeUpdate(); // Guardamos cuántas filas se eliminaron
            }

            // Si todo salió bien, confirmamos la transacción
            con.commit();
            // Retornamos true si se eliminó al menos un cliente
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            // Deshacemos los cambios si hubo algún error
            if (con != null) try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            // Restauramos autocommit y cerramos la conexión
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    /**
     * Busca clientes cuyo nombre, apellido o cédula contengan el texto dado.
     * @param q texto a buscar
     * @return lista de hasta 10 resultados
     */
    // Método de búsqueda flexible: busca clientes por nombre, apellido o cédula parcial
    public List<Cliente> buscarPorTexto(String q) {
        // Lista vacía para almacenar los resultados de la búsqueda
        List<Cliente> lista = new ArrayList<>();
        // Envolvemos el texto con "%" para buscar coincidencias parciales (LIKE '%texto%')
        String like = "%" + q + "%";
        // Consulta con OR que busca en nombre, apellido o cédula; LIMIT 10 evita resultados excesivos
        String sql  = "SELECT * FROM Cliente WHERE nombre LIKE ? OR apellido LIKE ? OR cedula LIKE ? ORDER BY nombre LIMIT 10";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el mismo patrón de búsqueda a los tres parámetros LIKE
            ps.setString(1, like);  // Busca en nombre
            ps.setString(2, like);  // Busca en apellido
            ps.setString(3, like);  // Busca en cédula
            try (ResultSet rs = ps.executeQuery()) {
                // Recorremos los resultados y mapeamos cada uno
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        // Retornamos los clientes que coinciden con la búsqueda (máximo 10)
        return lista;
    }

    /**
     * Verifica si ya existe un cliente con esa cédula.
     * @param cedula cédula a verificar
     * @return true si ya existe
     */
    // Método que comprueba si una cédula ya está registrada en la tabla Cliente
    public boolean cedulaExiste(String cedula) {
        // COUNT(*) cuenta cuántos clientes tienen esa cédula
        String sql = "SELECT COUNT(*) FROM Cliente WHERE cedula = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos la cédula a buscar
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                // Si el conteo es mayor a 0, la cédula ya existe
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar cédula: " + e.getMessage());
        }
        // Por defecto, asumimos que la cédula no existe
        return false;
    }

    /**
     * Verifica si la cédula ya existe excluyendo un cliente específico.
     * @param cedula    cédula a verificar
     * @param idCliente cliente a excluir
     * @return true si ya existe en otro cliente
     */
    // Método que verifica si la cédula pertenece a OTRO cliente (excluyendo al que se está editando)
    // Así no marca como duplicado si el cliente mantiene su propia cédula al editar
    /**
     * Obtiene el ID del cliente "Admin Tienda" (cédula 00000000).
     * Este cliente se usa para ventas anónimas (cuando el comprador no da sus datos).
     * @return id_cliente de Admin Tienda, o 0 si no existe
     */
    public int obtenerIdAdminTienda() {
        String sql = "SELECT id_cliente FROM Cliente WHERE cedula = '00000000'";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener Admin Tienda: " + e.getMessage());
        }
        return 0;
    }

    public boolean cedulaExisteExcluyendo(String cedula, int idCliente) {
        // Busca cédulas iguales pero que NO pertenezcan al cliente actual
        String sql = "SELECT COUNT(*) FROM Cliente WHERE cedula = ? AND id_cliente != ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos la cédula a verificar
            ps.setString(1, cedula);
            // Excluimos al cliente actual de la búsqueda
            ps.setInt(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                // Si otro cliente ya tiene esa cédula, retornamos true
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar cédula excluyendo: " + e.getMessage());
        }
        return false;
    }
}

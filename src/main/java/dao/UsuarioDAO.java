// Declaramos que esta clase pertenece al paquete "dao" (Data Access Object - Objeto de Acceso a Datos)
package dao;

// Importamos la clase que nos permite conectarnos a la base de datos MySQL
import com.Tienda_Barrio.config.conexion;
// Importamos el modelo Usuario que representa la tabla Usuario en la BD
import modelos.Usuario;
// Importamos BCrypt, una librería para cifrar y verificar contraseñas de forma segura
import org.mindrot.jbcrypt.BCrypt;

// Importamos las clases de JDBC necesarias para trabajar con la base de datos
import java.sql.Connection;        // Representa la conexión abierta con la BD
import java.sql.PreparedStatement;  // Permite ejecutar consultas SQL parametrizadas (previene SQL Injection)
import java.sql.ResultSet;          // Contiene los resultados de una consulta SELECT
import java.sql.SQLException;       // Excepción que se lanza cuando ocurre un error en la BD
import java.util.ArrayList;         // Implementación de lista dinámica para almacenar resultados
import java.util.List;              // Interfaz que define una lista ordenada de elementos

/**
 * DAO para la tabla Usuario.
 * Gestiona autenticación y CRUD de usuarios del sistema (admins y empleados).
 */
// Clase pública que contiene todos los métodos para interactuar con la tabla Usuario en la BD
public class UsuarioDAO {

    // ─────────────────────────────────────────────
    // Mapear ResultSet → Usuario
    // ─────────────────────────────────────────────
    // Método privado que convierte una fila del ResultSet en un objeto Usuario de Java
    // Es privado porque solo se usa internamente dentro de esta clase
    private Usuario mapear(ResultSet rs) throws SQLException {
        // Creamos un nuevo objeto Usuario vacío para llenarlo con los datos de la BD
        Usuario u = new Usuario();
        // Extraemos el campo "id_usuario" de la fila actual del ResultSet y lo asignamos al objeto
        u.setIdUsuario(rs.getInt("id_usuario"));
        // Extraemos el campo "id_rol" que indica si es admin (1) o empleado (2)
        u.setIdRol(rs.getInt("id_rol"));
        // Extraemos el correo electrónico del usuario
        u.setEmail(rs.getString("email"));
        // Extraemos el nombre del usuario
        u.setNombre(rs.getString("nombre"));
        // Extraemos el apellido del usuario
        u.setApellido(rs.getString("apellido"));
        // Extraemos la cédula (documento de identidad) del usuario
        u.setCedula(rs.getString("cedula"));
        // Retornamos el objeto Usuario ya completo con todos sus datos
        return u;
    }

    /**
     * Autentica un usuario por email y contraseña.
     * @param email    email del usuario
     * @param contrasenaIngresada contraseña en texto plano
     * @return Usuario autenticado o null si las credenciales son incorrectas
     */
    // Método público para iniciar sesión usando email y contraseña
    public Usuario login(String email, String contrasenaIngresada) {
        // Consulta SQL que busca un usuario por su email; el "?" es un parámetro que se reemplaza después
        // Esto previene ataques de SQL Injection porque el valor se escapa automáticamente
        String sql = "SELECT id_usuario, id_rol, email, contraseña_hash, nombre, apellido, cedula " +
                     "FROM Usuario WHERE email = ?";

        // try-with-resources: abre la conexión y prepara la consulta; se cierran automáticamente al terminar
        try (Connection con = conexion.getConnection();            // Abrimos conexión a la BD
             PreparedStatement ps = con.prepareStatement(sql)) {   // Preparamos la consulta SQL

            // Reemplazamos el primer "?" con el email recibido; trim() elimina espacios en blanco al inicio y final
            ps.setString(1, email.trim());
            // Ejecutamos la consulta SELECT y obtenemos los resultados en un ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                // rs.next() mueve el cursor a la primera fila; retorna true si encontró un usuario con ese email
                if (rs.next()) {
                    // Obtenemos el hash (contraseña cifrada) que está guardado en la BD
                    String hashGuardado = rs.getString("contraseña_hash");
                    // BCrypt.checkpw compara la contraseña ingresada en texto plano con el hash guardado
                    // Retorna true si coinciden (la contraseña es correcta)
                    if (BCrypt.checkpw(contrasenaIngresada, hashGuardado)) {
                        // Si la contraseña es correcta, mapeamos la fila a un objeto Usuario y lo retornamos
                        return mapear(rs);
                    }
                }
            }

        // Si ocurre un error de SQL, lo capturamos e imprimimos en la consola del servidor
        } catch (SQLException e) {
            System.err.println("Error en login por email: " + e.getMessage());
        }
        // Si no se encontró el email o la contraseña es incorrecta, retornamos null
        return null;
    }

    /**
     * Autentica un usuario por cédula y contraseña.
     * @param cedula   cédula del usuario
     * @param contrasenaIngresada contraseña en texto plano
     * @return Usuario autenticado o null
     */
    // Método alternativo de login que usa la cédula en lugar del email
    public Usuario loginPorCedula(String cedula, String contrasenaIngresada) {
        // Consulta SQL que busca un usuario por su cédula en vez de email
        String sql = "SELECT id_usuario, id_rol, email, contraseña_hash, nombre, apellido, cedula " +
                     "FROM Usuario WHERE cedula = ?";

        // Abrimos conexión y preparamos la consulta (se cierran automáticamente al salir del try)
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Reemplazamos el "?" con la cédula ingresada, eliminando espacios en blanco
            ps.setString(1, cedula.trim());
            // Ejecutamos la consulta y obtenemos el resultado
            try (ResultSet rs = ps.executeQuery()) {
                // Verificamos si se encontró un usuario con esa cédula
                if (rs.next()) {
                    // Obtenemos el hash de la contraseña almacenado en la BD
                    String hashGuardado = rs.getString("contraseña_hash");
                    // Comparamos la contraseña ingresada con el hash usando BCrypt
                    if (BCrypt.checkpw(contrasenaIngresada, hashGuardado)) {
                        // Si coincide, retornamos el usuario autenticado
                        return mapear(rs);
                    }
                }
            }

        } catch (SQLException e) {
            // Imprimimos el error en la consola para depuración
            System.err.println("Error en login por cédula: " + e.getMessage());
        }
        // Si las credenciales son incorrectas o hubo un error, retornamos null
        return null;
    }

    /**
     * Registra un nuevo usuario con contraseña cifrada con BCrypt.
     * @param u            objeto Usuario con datos básicos
     * @param contrasenaNueva contraseña en texto plano (se almacena como hash)
     * @return true si se insertó correctamente
     */
    // Método para registrar un nuevo usuario en la base de datos
    public boolean registrar(Usuario u, String contrasenaNueva) {
        // Consulta SQL INSERT para agregar un nuevo registro en la tabla Usuario
        // Los "?" son marcadores de posición que se reemplazan con valores seguros
        String sql = "INSERT INTO Usuario (id_rol, email, contraseña_hash, nombre, apellido, cedula) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        // Ciframos la contraseña con BCrypt antes de guardarla en la BD
        // gensalt(12) genera una "sal" aleatoria con factor de costo 12 (más alto = más seguro pero más lento)
        String hash = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));

        // Abrimos conexión y preparamos el INSERT
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos cada valor a su posición correspondiente en la consulta
            ps.setInt(1, u.getIdRol());        // Posición 1: rol del usuario (1=admin, 2=empleado)
            ps.setString(2, u.getEmail());     // Posición 2: correo electrónico
            ps.setString(3, hash);             // Posición 3: contraseña cifrada (nunca texto plano)
            ps.setString(4, u.getNombre());    // Posición 4: nombre del usuario
            ps.setString(5, u.getApellido());  // Posición 5: apellido del usuario
            ps.setString(6, u.getCedula());    // Posición 6: cédula del usuario
            // executeUpdate() ejecuta el INSERT y retorna el número de filas afectadas
            // Si es mayor que 0, significa que el registro se insertó exitosamente
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si hay un error (ej: email duplicado), lo imprimimos y retornamos false
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos los usuarios del sistema (admins + empleados).
     * @return lista de usuarios ordenada por nombre
     */
    // Método que obtiene todos los usuarios registrados junto con el nombre de su rol
    public List<Usuario> listar() {
        // Creamos una lista vacía donde almacenaremos todos los usuarios encontrados
        List<Usuario> lista = new ArrayList<>();
        // Consulta SQL con JOIN: une la tabla Usuario con la tabla Rol para obtener el nombre del rol
        // "u" es un alias para Usuario y "r" es un alias para Rol (hacen la consulta más corta)
        String sql = "SELECT u.id_usuario, u.id_rol, u.email, u.nombre, u.apellido, u.cedula, " +
                     "r.nombre AS nombre_rol " +
                     "FROM Usuario u " +
                     "JOIN Rol r ON u.id_rol = r.id_rol " +  // JOIN une las tablas por la columna id_rol
                     "ORDER BY u.nombre";  // Ordena los resultados alfabéticamente por nombre

        // Abrimos conexión, preparamos y ejecutamos la consulta en un solo try-with-resources
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos todas las filas del resultado con un bucle while
            while (rs.next()) {
                // Por cada fila, mapeamos los datos a un objeto Usuario
                Usuario u = mapear(rs);
                // nombreRol se guarda temporalmente en apellido no: usamos campo extra
                // Agregamos el usuario a la lista
                lista.add(u);
            }

        } catch (SQLException e) {
            // Si hay error, lo registramos en la consola del servidor
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        // Retornamos la lista (puede estar vacía si no hay usuarios o si hubo error)
        return lista;
    }

    /**
     * Actualiza los datos de un usuario (sin cambiar contraseña).
     * @param u objeto Usuario con los nuevos datos
     * @return true si se actualizó
     */
    // Método para actualizar los datos básicos de un usuario (nombre, apellido, email, rol)
    public boolean actualizar(Usuario u) {
        // Consulta SQL UPDATE que modifica los campos del usuario identificado por su id_usuario
        String sql = "UPDATE Usuario SET nombre=?, apellido=?, email=?, id_rol=? WHERE id_usuario=?";

        // Abrimos conexión y preparamos la consulta de actualización
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos los nuevos valores a cada parámetro de la consulta
            ps.setString(1, u.getNombre());      // Nuevo nombre
            ps.setString(2, u.getApellido());     // Nuevo apellido
            ps.setString(3, u.getEmail());        // Nuevo email
            ps.setInt(4, u.getIdRol());           // Nuevo rol
            ps.setInt(5, u.getIdUsuario());       // ID del usuario a actualizar (cláusula WHERE)
            // Ejecutamos el UPDATE y verificamos si se modificó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario del sistema.
     * @param idUsuario ID del usuario a eliminar
     * @return true si se eliminó
     */
    // Método para eliminar un usuario de la base de datos por su ID
    public boolean eliminar(int idUsuario) {
        // Consulta SQL DELETE que elimina el registro del usuario con el ID indicado
        String sql = "DELETE FROM Usuario WHERE id_usuario = ?";

        // Abrimos conexión y preparamos la consulta de eliminación
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el ID del usuario que queremos eliminar
            ps.setInt(1, idUsuario);
            // Ejecutamos el DELETE y verificamos si se eliminó al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si ya existe un usuario con ese email.
     * @param email email a verificar
     * @return true si ya existe
     */
    // Método que comprueba si un email ya está registrado en la BD (para evitar duplicados)
    public boolean emailExiste(String email) {
        // COUNT(*) cuenta cuántos registros tienen ese email; si es mayor a 0, ya existe
        String sql = "SELECT COUNT(*) FROM Usuario WHERE email = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos el email a buscar
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                // Si hay resultado, verificamos si el conteo es mayor a 0
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        // Si hubo error, asumimos que no existe (por seguridad, podría mejorarse)
        return false;
    }

    /**
     * Verifica si ya existe un usuario con esa cédula.
     * @param cedula cédula a verificar
     * @return true si ya existe
     */
    // Método que comprueba si una cédula ya está registrada (para evitar registros duplicados)
    public boolean cedulaExiste(String cedula) {
        // Contamos cuántos usuarios tienen esa cédula en la BD
        String sql = "SELECT COUNT(*) FROM Usuario WHERE cedula = ?";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos la cédula a buscar
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                // Si el conteo es mayor a 0, la cédula ya está registrada
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar cédula: " + e.getMessage());
        }
        // Por defecto retornamos false (cédula no encontrada)
        return false;
    }

    /**
     * Actualiza solo el perfil propio (nombre, apellido, email) sin tocar rol ni contraseña.
     */
    // Método que permite a un usuario actualizar sus propios datos de perfil sin modificar su rol
    public boolean actualizarPerfil(int idUsuario, String nombre, String apellido, String email) {
        // Solo actualiza nombre, apellido y email; no toca el rol ni la contraseña
        String sql = "UPDATE Usuario SET nombre=?, apellido=?, email=? WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignamos los nuevos valores del perfil
            ps.setString(1, nombre);     // Nuevo nombre del usuario
            ps.setString(2, apellido);   // Nuevo apellido del usuario
            ps.setString(3, email);      // Nuevo email del usuario
            ps.setInt(4, idUsuario);     // ID del usuario que actualiza su perfil
            // Ejecutamos y retornamos true si se actualizó correctamente
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia la contraseña de un usuario (recibe hash ya generado).
     */
    // Método para cambiar la contraseña de un usuario; recibe el hash ya generado
    public boolean actualizarContrasena(int idUsuario, String nuevoHash) {
        // Actualiza solo el campo contraseña_hash en la BD
        String sql = "UPDATE Usuario SET contraseña_hash=? WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignamos el nuevo hash de contraseña
            ps.setString(1, nuevoHash);
            // Asignamos el ID del usuario al que le cambiamos la contraseña
            ps.setInt(2, idUsuario);
            // Ejecutamos la actualización
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el email ya está en uso por OTRO usuario (para edición de perfil).
     */
    // Verifica si el email existe pero excluyendo al usuario actual (útil al editar perfil)
    // Así no marca como duplicado si el usuario mantiene su propio email
    public boolean emailExisteExcluyendo(String email, int idUsuario) {
        // Busca emails iguales pero que NO pertenezcan al usuario con ese ID
        String sql = "SELECT COUNT(*) FROM Usuario WHERE email=? AND id_usuario != ?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignamos el email a verificar
            ps.setString(1, email);
            // Excluimos al usuario actual de la búsqueda
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                // Si el conteo es mayor a 0, otro usuario ya tiene ese email
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el hash de contraseña actual de un usuario (para verificar antes de cambiarla).
     */
    // Obtiene el hash de la contraseña actual para poder verificarla antes de cambiarla
    public String obtenerHashContrasena(int idUsuario) {
        // Consulta que solo trae el campo contraseña_hash del usuario indicado
        String sql = "SELECT contraseña_hash FROM Usuario WHERE id_usuario=?";
        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Asignamos el ID del usuario
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos el usuario, retornamos su hash de contraseña
                if (rs.next()) return rs.getString("contraseña_hash");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener hash: " + e.getMessage());
        }
        // Retornamos null si no se encontró el usuario o hubo un error
        return null;
    }

    // ── Métodos legacy para compatibilidad con ClienteControlador antiguo ──

    /** @deprecated Usar ClienteDAO.listar() para la tabla Cliente */
    // Método antiguo que lista usuarios con rol de cliente (id_rol=2)
    // Está marcado como @deprecated porque ahora los clientes están en su propia tabla "Cliente"
    public List<Usuario> listarClientes() {
        // Creamos una lista vacía para almacenar los clientes encontrados
        List<Usuario> lista = new ArrayList<>();
        // Filtramos solo los usuarios con id_rol = 2 (clientes) ordenados por nombre
        String sql = "SELECT id_usuario, id_rol, email, nombre, apellido, cedula " +
                     "FROM Usuario WHERE id_rol = 2 ORDER BY nombre";

        try (Connection con = conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos cada fila y la mapeamos a un objeto Usuario, agregándola a la lista
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        // Retornamos la lista de clientes (puede estar vacía)
        return lista;
    }
}

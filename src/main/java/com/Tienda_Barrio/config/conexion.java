// Paquete donde vive esta clase (estructura de carpetas del proyecto)
package com.Tienda_Barrio.config;

// Importa Connection: representa una conexión abierta a la base de datos MySQL
import java.sql.Connection;
// Importa DriverManager: fábrica que crea conexiones a la BD usando URL + usuario + contraseña
import java.sql.DriverManager;
// Importa SQLException: excepción que se lanza cuando hay errores de base de datos
import java.sql.SQLException;

/**
 * Clase de configuración que gestiona la conexión a la base de datos MySQL.
 * Todos los DAOs llaman a conexion.getConnection() para obtener una conexión.
 */
public class conexion {

    // URL de conexión JDBC: protocolo jdbc:mysql, servidor localhost, puerto 3306, base de datos proyectoPersonal
    private static final String URL = "jdbc:mysql://localhost:3306/proyectoPersonal";
    // Usuario de MySQL con el que se conecta
    private static final String USER = "root";
    // Contraseña del usuario MySQL
    private static final String PASSWORD = "Jensen1234";

    /**
     * Crea y retorna una conexión a la base de datos MySQL.
     * Cualquier DAO puede llamar este método para comunicarse con la BD.
     * @return Connection activa o null si falla la conexión
     */
    public static Connection getConnection() {
        // Variable para almacenar la conexión (inicia en null)
        Connection con = null;
        try {
            // Carga el driver de MySQL en memoria para que Java sepa cómo comunicarse con MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Crea la conexión real usando la URL, usuario y contraseña definidos arriba
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            // Imprime en la consola del servidor que la conexión fue exitosa
            System.out.println("¡Conexión exitosa a la base de datos con JDK 21!");

        } catch (ClassNotFoundException e) {
            // Error: no encontró el archivo .jar del driver MySQL en las dependencias
            System.err.println("No se encontró el driver de MySQL: " + e.getMessage());
        } catch (SQLException e) {
            // Error: la BD no responde, contraseña incorrecta, BD no existe, etc.
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        // Retorna la conexión lista para usar (o null si hubo error)
        return con;
    }

    // Método main para probar la conexión directamente ejecutando esta clase
    public static void main(String[] args) {
        getConnection();
    }
}

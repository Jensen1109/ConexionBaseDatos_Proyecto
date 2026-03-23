// Paquete donde vive esta clase (estructura de carpetas del proyecto)
// config = carpeta de configuración, aquí van las clases que configuran el sistema
package com.Tienda_Barrio.config;

// Importa Connection: es la interfaz de JDBC que representa una conexión abierta a la base de datos
// Con este objeto se pueden crear consultas SQL y ejecutarlas en MySQL
import java.sql.Connection;

// Importa DriverManager: es la fábrica de conexiones de JDBC
// Su método getConnection() es el que realmente abre la conexión con MySQL usando la URL, usuario y contraseña
import java.sql.DriverManager;

// Importa SQLException: excepción que se lanza automáticamente cuando hay cualquier error relacionado con la BD
// Por ejemplo: contraseña incorrecta, base de datos no existe, servidor MySQL apagado, etc.
import java.sql.SQLException;

/**
 * Clase de configuración que gestiona la conexión a la base de datos MySQL.
 * Es el puente entre Java y MySQL: todos los DAOs llaman a conexion.getConnection()
 * para obtener una conexión antes de ejecutar cualquier consulta SQL.
 *
 * Patrón usado: Fábrica estática (Static Factory Method)
 * No se necesita crear un objeto de esta clase, se llama directamente: conexion.getConnection()
 */
public class conexion {

    // URL de conexión JDBC: indica al driver cómo y a dónde conectarse
    // Formato: jdbc:mysql://[servidor]:[puerto]/[nombre_base_de_datos]
    // localhost = el servidor MySQL corre en la misma máquina que el servidor web (Tomcat)
    // 3306 = puerto por defecto de MySQL
    // proyectoPersonal = nombre de la base de datos que creamos en MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/proyectoPersonal";

    // Usuario de MySQL con el que se inicia sesión en la base de datos
    // "root" es el usuario administrador por defecto de MySQL
    private static final String USER = "root";

    // Contraseña del usuario MySQL para autenticarse en la base de datos
    // En producción real esta contraseña debería estar en un archivo de configuración externo, no en el código
    private static final String PASSWORD = "Jensen1234";

    /**
     * Crea y retorna una conexión activa a la base de datos MySQL.
     *
     * Este método es "static" para que cualquier DAO pueda llamarlo sin crear un objeto:
     *     Connection con = conexion.getConnection();
     *
     * @return Connection lista para usar, o null si ocurrió un error al conectar
     */
    public static Connection getConnection() {

        // Declaramos la variable de conexión e iniciamos en null
        // Si algo falla antes de asignarla, retornamos null y el DAO lo maneja
        Connection con = null;

        try {
            // PASO 1: Cargar el driver de MySQL en memoria
            // Class.forName() busca y carga la clase del driver JDBC de MySQL
            // "com.mysql.cj.jdbc.Driver" es el nombre del driver incluido en el archivo mysql-connector-j.jar (dependencia en pom.xml)
            // Si el .jar no está en las dependencias, aquí lanza ClassNotFoundException
            Class.forName("com.mysql.cj.jdbc.Driver");

            // PASO 2: Abrir la conexión real con MySQL
            // DriverManager.getConnection() usa la URL, usuario y contraseña para conectarse
            // Si MySQL está apagado, la BD no existe o la contraseña es incorrecta, lanza SQLException
            con = DriverManager.getConnection(URL, USER, PASSWORD);

            // Si llegamos hasta aquí sin error, la conexión fue exitosa
            // Imprimimos un mensaje en la consola del servidor (Tomcat) para confirmarlo
            System.out.println("¡Conexión exitosa a la base de datos con JDK 21!");

        } catch (ClassNotFoundException e) {
            // Este error ocurre si el archivo mysql-connector-j.jar no está en las dependencias del pom.xml
            // Significa que Java no puede encontrar el driver necesario para hablar con MySQL
            System.err.println("No se encontró el driver de MySQL: " + e.getMessage());

        } catch (SQLException e) {
            // Este error ocurre cuando el driver sí existe pero no pudo conectarse a MySQL
            // Causas comunes: MySQL está apagado, URL incorrecta, contraseña incorrecta, BD no existe
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Retornamos la conexión lista para usar
        // Si hubo error, retorna null y el DAO que lo llamó debe manejar ese caso
        return con;
    }

    // Método main: sirve para probar la conexión ejecutando esta clase directamente
    // No se usa cuando corre en Tomcat, solo para pruebas rápidas desde el IDE
    public static void main(String[] args) {
        // Llama a getConnection() y muestra en consola si la conexión fue exitosa o no
        getConnection();
    }
}

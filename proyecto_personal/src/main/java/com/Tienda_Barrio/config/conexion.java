package com.Tienda_Barrio.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    // 1. IMPORTANTE: Agrega el nombre de tu base de datos al final de la URL
    // Si tu base de datos se llama 'tienda', ponla después del 3306/
    private static final String URL = "jdbc:mysql://localhost:3306/proyectoPersonal";
    private static final String USER = "root";
    private static final String PASSWORD = "#Aprendiz2024";

    public static Connection getConnection() {
        Connection con = null;
        try {
            // En versiones modernas de Java y el driver 'mysql-connector-j', 
            // el Class.forName ya no es estrictamente necesario, pero no estorba.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a la base de datos con JDK 21!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver de MySQL: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return con;
    }

    public static void main(String[] args) {
        getConnection();
    }
}
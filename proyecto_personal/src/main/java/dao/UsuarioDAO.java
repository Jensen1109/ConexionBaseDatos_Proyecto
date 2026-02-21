/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import com.Tienda_Barrio.config.conexion;
import modelos.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public boolean registrar(Usuario u, String contrasenaNueva) {
    String sql = "INSERT INTO Usuario (id_rol, email, contraseña_hash, nombre, apellido, cedula) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";

    String hash = BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(12));

    try (Connection con = conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, u.getIdRol());
        ps.setString(2, u.getEmail());
        ps.setString(3, hash);
        ps.setString(4, u.getNombre());
        ps.setString(5, u.getApellido());
        ps.setString(6, u.getCedula());

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Error al registrar usuario: " + e.getMessage());
        return false;
    }
}
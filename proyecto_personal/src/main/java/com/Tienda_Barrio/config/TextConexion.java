package com.Tienda_Barrio.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

// ESTOS SON LOS CAMBIOS CLAVE: de javax a jakarta
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "TextConexion", urlPatterns = {"/TextConexion"})
public class TextConexion extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Prueba de Conexión</title></head>");
            out.println("<body>");
            out.println("<h1>Estado de la base de datos desde el Servidor</h1>");

            try {
                // Llama a tu clase conexion
                Connection con = conexion.getConnection(); 
                if (con != null) {
                    out.println("<h2 style='color: green;'>¡ÉXITO! Conectado a MySQL con JDK 21.</h2>");
                    con.close();
                } else {
                    out.println("<h2 style='color: red;'>ERROR: La conexión es nula. Revisa la URL en conexion.java.</h2>");
                }
            } catch (SQLException e) {
                out.println("<h2 style='color: red;'>Error de SQL: " + e.getMessage() + "</h2>");
            }

            out.println("<br><a href='index.html'>Volver al inicio</a>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
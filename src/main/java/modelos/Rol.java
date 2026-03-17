// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "Rol" en la base de datos.
 * Los roles definen el tipo de usuario en el sistema y determinan sus permisos.
 * En este proyecto se manejan dos roles principales:
 *   - id_rol = 1 -> Administrador (acceso total al sistema)
 *   - id_rol = 2 -> Cliente (acceso limitado, solo puede ver productos y hacer pedidos)
 */
public class Rol {

    // ID unico del rol (columna id_rol, clave primaria, AUTO_INCREMENT)
    // Valores conocidos: 1 = admin, 2 = cliente
    private int idRol;

    // Nombre descriptivo del rol (ej: "Administrador", "Cliente")
    private String nombre;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Rol() {}

    // Constructor completo: permite crear un Rol con su ID y nombre de una sola vez
    public Rol(int idRol, String nombre) {
        // Asigna el ID del rol recibido como parametro
        this.idRol = idRol;
        // Asigna el nombre del rol
        this.nombre = nombre;
    }

    // Getter: devuelve el ID del rol
    public int getIdRol() { return idRol; }
    // Setter: permite asignar o modificar el ID del rol
    public void setIdRol(int idRol) { this.idRol = idRol; }

    // Getter: devuelve el nombre del rol (ej: "Administrador")
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre del rol
    public void setNombre(String nombre) { this.nombre = nombre; }
}

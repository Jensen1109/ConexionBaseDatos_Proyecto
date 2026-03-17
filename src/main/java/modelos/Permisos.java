// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "Permisos" en la base de datos.
 * Los permisos definen que acciones puede realizar un rol dentro del sistema.
 * Por ejemplo: "gestionar_productos", "ver_reportes", "registrar_ventas".
 * Se asocian a los roles para controlar el acceso a las funcionalidades.
 */
public class Permisos {

    // ID unico del permiso (columna id_permiso, clave primaria, AUTO_INCREMENT)
    private int idPermiso;

    // Nombre corto del permiso, sirve como identificador legible (ej: "gestionar_productos")
    private String nombre;

    // Descripcion detallada de lo que permite hacer este permiso (ej: "Permite agregar, editar y eliminar productos")
    private String descripcion;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Permisos() {}

    // Constructor completo: permite crear un Permiso con todos sus datos de una sola vez
    public Permisos(int idPermiso, String nombre, String descripcion) {
        // Asigna el ID del permiso
        this.idPermiso = idPermiso;
        // Asigna el nombre identificador del permiso
        this.nombre = nombre;
        // Asigna la descripcion detallada del permiso
        this.descripcion = descripcion;
    }

    // Getter: devuelve el ID del permiso
    public int getIdPermiso() { return idPermiso; }
    // Setter: permite asignar o modificar el ID del permiso
    public void setIdPermiso(int idPermiso) { this.idPermiso = idPermiso; }

    // Getter: devuelve el nombre del permiso
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre del permiso
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Getter: devuelve la descripcion del permiso
    public String getDescripcion() { return descripcion; }
    // Setter: permite actualizar la descripcion del permiso
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}

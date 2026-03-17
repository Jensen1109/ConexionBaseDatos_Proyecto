// Paquete "modelos": agrupa todas las clases que representan tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "Usuario" en la base de datos.
 * Cada instancia de esta clase equivale a una fila de dicha tabla.
 * Un usuario es alguien que puede iniciar sesion en el sistema (admin o empleado).
 */
public class Usuario {

    // ID unico del usuario en la BD (columna id_usuario, clave primaria, AUTO_INCREMENT)
    private int idUsuario;

    // Rol asignado al usuario: 1 = administrador, 2 = cliente (FK hacia la tabla Rol)
    private int idRol;

    // Correo electronico del usuario, se usa como credencial para iniciar sesion
    private String email;

    // Nombre de pila del usuario (columna "nombre" en la tabla Usuario)
    private String nombre;

    // Apellido del usuario (columna "apellido" en la tabla Usuario)
    private String apellido;

    // Numero de cedula del usuario, sirve como identificacion unica personal
    private String cedula;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Usuario() {}

    // Constructor completo: permite crear un Usuario con todos sus datos de una sola vez
    public Usuario(int idUsuario, int idRol, String email, String nombre, String apellido, String cedula) {
        // Asigna el ID del usuario recibido como parametro al atributo de la clase
        this.idUsuario = idUsuario;
        // Asigna el ID del rol (1=admin, 2=cliente) al atributo de la clase
        this.idRol = idRol;
        // Asigna el correo electronico al atributo de la clase
        this.email = email;
        // Asigna el nombre del usuario al atributo de la clase
        this.nombre = nombre;
        // Asigna el apellido del usuario al atributo de la clase
        this.apellido = apellido;
        // Asigna la cedula del usuario al atributo de la clase
        this.cedula = cedula;
    }

    // Getter: devuelve el ID del usuario para poder leerlo desde otras clases
    public int getIdUsuario() { return idUsuario; }
    // Setter: permite modificar el ID del usuario desde otras clases (ej: al leer de la BD)
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // Getter: devuelve el ID del rol asignado al usuario
    public int getIdRol() { return idRol; }
    // Setter: permite cambiar el rol del usuario (por ejemplo, de cliente a admin)
    public void setIdRol(int idRol) { this.idRol = idRol; }

    // Getter: devuelve el correo electronico del usuario
    public String getEmail() { return email; }
    // Setter: permite actualizar el correo electronico del usuario
    public void setEmail(String email) { this.email = email; }

    // Getter: devuelve el nombre de pila del usuario
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre del usuario
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Getter: devuelve el apellido del usuario
    public String getApellido() { return apellido; }
    // Setter: permite actualizar el apellido del usuario
    public void setApellido(String apellido) { this.apellido = apellido; }

    // Getter: devuelve la cedula del usuario
    public String getCedula() { return cedula; }
    // Setter: permite actualizar la cedula del usuario
    public void setCedula(String cedula) { this.cedula = cedula; }
}

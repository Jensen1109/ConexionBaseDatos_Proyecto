// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo para la tabla Cliente.
 * Representa a los clientes de la tienda (personas que compran a crédito).
 * Los teléfonos del cliente se gestionan en la tabla Telefono (relación 1:N).
 */
public class Cliente {

    // ID unico del cliente en la BD (columna id_cliente, clave primaria, AUTO_INCREMENT)
    private int    idCliente;

    // Nombre de pila del cliente (columna "nombre" en la tabla Cliente)
    private String nombre;

    // Apellido del cliente (columna "apellido" en la tabla Cliente)
    private String apellido;

    // Numero de cedula del cliente, sirve como identificacion unica personal
    private String cedula;

    // Correo electronico del cliente, opcional, para contacto o notificaciones
    private String email;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Cliente() {}

    /**
     * Constructor completo.
     * @param idCliente identificador único del cliente
     * @param nombre    nombre del cliente
     * @param apellido  apellido del cliente
     * @param cedula    número de cédula único
     */
    // Constructor con parametros: permite crear un Cliente con sus datos principales de una sola vez
    public Cliente(int idCliente, String nombre, String apellido, String cedula) {
        // Asigna el ID del cliente recibido como parametro
        this.idCliente = idCliente;
        // Asigna el nombre del cliente
        this.nombre    = nombre;
        // Asigna el apellido del cliente
        this.apellido  = apellido;
        // Asigna la cedula del cliente
        this.cedula    = cedula;
    }

    // Getter: devuelve el ID del cliente
    public int    getIdCliente()              { return idCliente; }
    // Setter: permite asignar o modificar el ID del cliente
    public void   setIdCliente(int idCliente) { this.idCliente = idCliente; }

    // Getter: devuelve el nombre del cliente
    public String getNombre()                 { return nombre; }
    // Setter: permite actualizar el nombre del cliente
    public void   setNombre(String nombre)    { this.nombre = nombre; }

    // Getter: devuelve el apellido del cliente
    public String getApellido()               { return apellido; }
    // Setter: permite actualizar el apellido del cliente
    public void   setApellido(String apellido){ this.apellido = apellido; }

    // Getter: devuelve la cedula del cliente
    public String getCedula()                 { return cedula; }
    // Setter: permite actualizar la cedula del cliente
    public void   setCedula(String cedula)    { this.cedula = cedula; }

    // Getter: devuelve el correo electronico del cliente
    public String getEmail()                  { return email; }
    // Setter: permite actualizar el correo electronico del cliente
    public void   setEmail(String email)      { this.email = email; }

    /**
     * Retorna el nombre completo del cliente (nombre + apellido).
     * @return nombre completo
     */
    // Metodo auxiliar: concatena nombre y apellido para mostrar el nombre completo en las vistas
    // Usa operador ternario para evitar que aparezca "null" si alguno de los campos es nulo
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}

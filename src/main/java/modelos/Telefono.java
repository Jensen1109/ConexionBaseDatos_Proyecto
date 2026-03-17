// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa un número de teléfono adicional de un cliente.
 * Un cliente puede tener múltiples teléfonos registrados en la tabla Telefono.
 * La relacion entre Cliente y Telefono es de uno a muchos (1:N).
 */
public class Telefono {

    // ID unico del telefono (columna id_telefono, clave primaria, AUTO_INCREMENT)
    private int    idTelefono;

    // Numero de telefono del cliente (ej: "3001234567", "6012345678")
    private String telefono;

    // ID del cliente al que pertenece este telefono (FK hacia tabla Cliente)
    private int    clienteId;

    /** Constructor vacío requerido por el DAO. */
    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Telefono() {}

    /**
     * Constructor completo.
     * @param idTelefono identificador único del teléfono
     * @param telefono   número de teléfono
     * @param clienteId  identificador del cliente al que pertenece
     */
    // Constructor completo: permite crear un Telefono con todos sus datos de una sola vez
    public Telefono(int idTelefono, String telefono, int clienteId) {
        // Asigna el ID del registro de telefono
        this.idTelefono = idTelefono;
        // Asigna el numero de telefono como cadena de texto
        this.telefono   = telefono;
        // Asigna el ID del cliente dueno de este numero de telefono
        this.clienteId  = clienteId;
    }

    // Getter: devuelve el ID del registro de telefono
    public int    getIdTelefono()              { return idTelefono; }
    // Setter: permite asignar o modificar el ID del telefono
    public void   setIdTelefono(int idTelefono){ this.idTelefono = idTelefono; }

    // Getter: devuelve el numero de telefono como String
    public String getTelefono()                { return telefono; }
    // Setter: permite actualizar el numero de telefono
    public void   setTelefono(String telefono) { this.telefono = telefono; }

    // Getter: devuelve el ID del cliente al que pertenece este telefono
    public int    getClienteId()               { return clienteId; }
    // Setter: permite cambiar el cliente asociado a este numero de telefono
    public void   setClienteId(int clienteId)  { this.clienteId = clienteId; }

}

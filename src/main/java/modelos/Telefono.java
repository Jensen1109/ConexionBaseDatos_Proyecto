package modelos;

/**
 * Modelo que representa un número de teléfono adicional de un cliente.
 * Un cliente puede tener múltiples teléfonos registrados en la tabla Telefono.
 */
public class Telefono {

    private int    idTelefono;
    private String telefono;
    private int    clienteId;

    /** Constructor vacío requerido por el DAO. */
    public Telefono() {}

    /**
     * Constructor completo.
     * @param idTelefono identificador único del teléfono
     * @param telefono   número de teléfono
     * @param clienteId  identificador del cliente al que pertenece
     */
    public Telefono(int idTelefono, String telefono, int clienteId) {
        this.idTelefono = idTelefono;
        this.telefono   = telefono;
        this.clienteId  = clienteId;
    }

    public int    getIdTelefono()              { return idTelefono; }
    public void   setIdTelefono(int idTelefono){ this.idTelefono = idTelefono; }

    public String getTelefono()                { return telefono; }
    public void   setTelefono(String telefono) { this.telefono = telefono; }

    public int    getClienteId()               { return clienteId; }
    public void   setClienteId(int clienteId)  { this.clienteId = clienteId; }

}

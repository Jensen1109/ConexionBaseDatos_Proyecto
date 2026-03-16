package modelos;

/**
 * Modelo para la tabla Cliente.
 * Representa a los clientes de la tienda (personas que compran a crédito).
 * Los teléfonos del cliente se gestionan en la tabla Telefono (relación 1:N).
 */
public class Cliente {

    private int    idCliente;
    private String nombre;
    private String apellido;
    private String cedula;
    private String email;

    public Cliente() {}

    /**
     * Constructor completo.
     * @param idCliente identificador único del cliente
     * @param nombre    nombre del cliente
     * @param apellido  apellido del cliente
     * @param cedula    número de cédula único
     */
    public Cliente(int idCliente, String nombre, String apellido, String cedula) {
        this.idCliente = idCliente;
        this.nombre    = nombre;
        this.apellido  = apellido;
        this.cedula    = cedula;
    }

    public int    getIdCliente()              { return idCliente; }
    public void   setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombre()                 { return nombre; }
    public void   setNombre(String nombre)    { this.nombre = nombre; }

    public String getApellido()               { return apellido; }
    public void   setApellido(String apellido){ this.apellido = apellido; }

    public String getCedula()                 { return cedula; }
    public void   setCedula(String cedula)    { this.cedula = cedula; }

    public String getEmail()                  { return email; }
    public void   setEmail(String email)      { this.email = email; }

    /**
     * Retorna el nombre completo del cliente (nombre + apellido).
     * @return nombre completo
     */
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}

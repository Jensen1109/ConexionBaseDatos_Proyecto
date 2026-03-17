// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "MetodoPago" en la base de datos.
 * Define los distintos metodos de pago disponibles en la tienda
 * (por ejemplo: "Efectivo", "Tarjeta", "Transferencia", "Fiado/Credito").
 * Cada pedido tiene un metodo de pago asociado.
 */
public class MetodoPago {

    // ID unico del metodo de pago (columna id_pago, clave primaria, AUTO_INCREMENT)
    private int idPago;

    // Nombre descriptivo del metodo de pago (ej: "Efectivo", "Tarjeta", "Transferencia")
    private String nombre;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public MetodoPago() {}

    // Constructor completo: permite crear un MetodoPago con su ID y nombre de una sola vez
    public MetodoPago(int idPago, String nombre) {
        // Asigna el ID del metodo de pago
        this.idPago = idPago;
        // Asigna el nombre del metodo de pago
        this.nombre = nombre;
    }

    // Getter: devuelve el ID del metodo de pago
    public int getIdPago() { return idPago; }
    // Setter: permite asignar o modificar el ID del metodo de pago
    public void setIdPago(int idPago) { this.idPago = idPago; }

    // Getter: devuelve el nombre del metodo de pago
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre del metodo de pago
    public void setNombre(String nombre) { this.nombre = nombre; }
}

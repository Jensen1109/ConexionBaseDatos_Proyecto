// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

// Importa BigDecimal para manejar montos de dinero con precision (evita errores de redondeo)
import java.math.BigDecimal;
// Importa LocalDate para manejar fechas sin hora (usado para la fecha del abono)
import java.time.LocalDate;

/**
 * Modelo que representa la tabla "Deuda" en la base de datos.
 * Una deuda se genera cuando un cliente compra a credito (fiado) y queda debiendo dinero.
 * Tambien puede ser una deuda manual sin pedido asociado.
 */
public class Deuda {

    // ID unico de la deuda (columna id_deuda, clave primaria, AUTO_INCREMENT)
    private int idDeuda;

    // ID del pedido que origino la deuda (FK hacia tabla Pedido); puede ser 0 si es deuda manual
    private int idPedido;

    // Monto que el cliente aun debe pagar; se usa BigDecimal para precision monetaria
    private BigDecimal montoPendiente;

    // Estado de la deuda: puede ser "pendiente" si aun debe, o "pagada" si ya la cancelo
    private String estado;

    // Monto del ultimo abono realizado por el cliente para ir pagando la deuda
    private BigDecimal abono;

    // Fecha en que se registro el ultimo abono realizado
    private LocalDate fechaAbono;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Deuda() {}

    // Constructor completo: permite crear una Deuda con todos sus datos de una sola vez
    public Deuda(int idDeuda, int idPedido, BigDecimal montoPendiente,
                String estado, BigDecimal abono, LocalDate fechaAbono) {
        // Asigna el ID de la deuda
        this.idDeuda = idDeuda;
        // Asigna el ID del pedido que genero esta deuda
        this.idPedido = idPedido;
        // Asigna el monto que queda pendiente por pagar
        this.montoPendiente = montoPendiente;
        // Asigna el estado de la deuda (pendiente o pagada)
        this.estado = estado;
        // Asigna el monto del abono realizado
        this.abono = abono;
        // Asigna la fecha en que se hizo el abono
        this.fechaAbono = fechaAbono;
    }

    // Getter: devuelve el ID de la deuda
    public int getIdDeuda() { return idDeuda; }
    // Setter: permite asignar o modificar el ID de la deuda
    public void setIdDeuda(int idDeuda) { this.idDeuda = idDeuda; }

    // Getter: devuelve el ID del pedido asociado a esta deuda
    public int getIdPedido() { return idPedido; }
    // Setter: permite asignar el pedido que origino la deuda
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    // Getter: devuelve el monto pendiente que el cliente aun debe
    public BigDecimal getMontoPendiente() { return montoPendiente; }
    // Setter: permite actualizar el monto pendiente (ej: cuando el cliente abona, se reduce)
    public void setMontoPendiente(BigDecimal montoPendiente) { this.montoPendiente = montoPendiente; }

    // Getter: devuelve el estado actual de la deuda ("pendiente" o "pagada")
    public String getEstado() { return estado; }
    // Setter: permite cambiar el estado de la deuda (ej: de "pendiente" a "pagada")
    public void setEstado(String estado) { this.estado = estado; }

    // Getter: devuelve el monto del abono realizado
    public BigDecimal getAbono() { return abono; }
    // Setter: permite registrar un nuevo monto de abono
    public void setAbono(BigDecimal abono) { this.abono = abono; }

    // Getter: devuelve la fecha en que se realizo el ultimo abono
    public LocalDate getFechaAbono() { return fechaAbono; }
    // Setter: permite asignar la fecha del abono
    public void setFechaAbono(LocalDate fechaAbono) { this.fechaAbono = fechaAbono; }

    // === Campo adicional: referencia directa al cliente (para deudas manuales sin pedido) ===

    // ID del cliente que tiene la deuda (FK hacia tabla Cliente); se usa cuando la deuda no viene de un pedido
    private int idCliente;
    // Getter: devuelve el ID del cliente que debe el dinero
    public int getIdCliente() { return idCliente; }
    // Setter: permite asignar el cliente responsable de la deuda
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    // === Campo calculado via JOIN (NO es columna de la tabla Deuda en la BD) ===

    // Nombre del cliente, se obtiene haciendo JOIN con la tabla Cliente al consultar deudas
    private String nombreCliente;
    // Getter: devuelve el nombre del cliente para mostrarlo en la vista sin hacer otra consulta
    public String getNombreCliente() { return nombreCliente; }
    // Setter: se usa en el DAO al mapear el resultado del JOIN con la tabla Cliente
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}

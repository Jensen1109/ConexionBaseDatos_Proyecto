// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

// Importa BigDecimal para manejar el total del pedido con precision monetaria
import java.math.BigDecimal;
// Importa LocalDateTime para almacenar la fecha y hora exacta de la venta
import java.time.LocalDateTime;

/**
 * Modelo que representa la tabla "Pedido" en la base de datos.
 * Cada instancia de esta clase equivale a una venta realizada en la tienda.
 * Un pedido tiene un cliente, un usuario que lo registro, un metodo de pago y un total.
 */
public class Pedido {

    // ID unico del pedido (columna id_pedido, clave primaria, AUTO_INCREMENT)
    private int idPedido;

    // ID del cliente que realizo la compra (FK hacia tabla Cliente)
    private int idCliente;

    // ID del usuario (empleado/admin) que registro la venta en el sistema (FK hacia tabla Usuario)
    private int idUsuario;

    // ID del metodo de pago utilizado: efectivo, tarjeta, etc. (FK hacia tabla MetodoPago)
    private int idPago;

    // Fecha y hora exacta en que se realizo la venta (columna fecha_venta en la BD)
    private LocalDateTime fechaVenta;

    // Monto total del pedido en dinero; se usa BigDecimal para precision con decimales
    private BigDecimal total;

    // Estado actual del pedido (ej: "completado", "pendiente", "cancelado")
    private String estado;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Pedido() {}

    // Constructor completo: permite crear un Pedido con todos sus datos de una sola vez
    public Pedido(int idPedido, int idCliente, int idUsuario, int idPago,
                LocalDateTime fechaVenta, BigDecimal total, String estado) {
        // Asigna el ID del pedido
        this.idPedido = idPedido;
        // Asigna el ID del cliente que compro
        this.idCliente = idCliente;
        // Asigna el ID del usuario que atendio la venta
        this.idUsuario = idUsuario;
        // Asigna el ID del metodo de pago usado
        this.idPago = idPago;
        // Asigna la fecha y hora de la venta
        this.fechaVenta = fechaVenta;
        // Asigna el monto total del pedido
        this.total = total;
        // Asigna el estado del pedido (completado, pendiente, etc.)
        this.estado = estado;
    }

    // Getter: devuelve el ID del pedido
    public int getIdPedido() { return idPedido; }
    // Setter: permite asignar o modificar el ID del pedido
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    // Getter: devuelve el ID del cliente asociado a este pedido
    public int getIdCliente() { return idCliente; }
    // Setter: permite asignar el cliente que realizo la compra
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    // Getter: devuelve el ID del usuario que registro la venta
    public int getIdUsuario() { return idUsuario; }
    // Setter: permite asignar el usuario que atendio la venta
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // Getter: devuelve el ID del metodo de pago utilizado
    public int getIdPago() { return idPago; }
    // Setter: permite cambiar el metodo de pago del pedido
    public void setIdPago(int idPago) { this.idPago = idPago; }

    // Getter: devuelve la fecha y hora en que se realizo la venta
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    // Setter: permite asignar o modificar la fecha de la venta
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    // Getter: devuelve el monto total del pedido
    public BigDecimal getTotal() { return total; }
    // Setter: permite asignar o modificar el total del pedido
    public void setTotal(BigDecimal total) { this.total = total; }

    // Getter: devuelve el estado actual del pedido
    public String getEstado() { return estado; }
    // Setter: permite cambiar el estado del pedido (ej: de "pendiente" a "completado")
    public void setEstado(String estado) { this.estado = estado; }

    // === Campos calculados via JOIN (NO son columnas de la tabla Pedido en la BD) ===

    // Nombre del cliente, se obtiene haciendo JOIN con la tabla Cliente al consultar pedidos
    private String nombreCliente;
    // Getter: devuelve el nombre del cliente para mostrarlo en la vista sin hacer otra consulta
    public String getNombreCliente() { return nombreCliente; }
    // Setter: se usa en el DAO al mapear el resultado del JOIN con la tabla Cliente
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    // Nombre del metodo de pago, se obtiene haciendo JOIN con la tabla MetodoPago
    private String nombrePago;
    // Getter: devuelve el nombre del metodo de pago (ej: "Efectivo", "Tarjeta")
    public String getNombrePago() { return nombrePago; }
    // Setter: se usa en el DAO al mapear el resultado del JOIN con la tabla MetodoPago
    public void setNombrePago(String nombrePago) { this.nombrePago = nombrePago; }
}

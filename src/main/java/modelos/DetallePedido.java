// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

// Importa BigDecimal para manejar el precio unitario con precision monetaria (evita errores de redondeo)
import java.math.BigDecimal;

/**
 * Modelo que representa la tabla "detalle_pedido" en la base de datos.
 * Cada fila de esta tabla corresponde a un producto especifico dentro de un pedido.
 * Por ejemplo, si un cliente compra 3 productos distintos, se crean 3 filas de DetallePedido.
 * Esto permite saber que productos se vendieron, en que cantidad y a que precio.
 */
public class DetallePedido {

    // ID unico del detalle (columna id_detalle, clave primaria, AUTO_INCREMENT)
    private int idDetalle;

    // ID del pedido al que pertenece este detalle (FK hacia tabla Pedido)
    private int idPedido;

    // ID del producto que se vendio en este detalle (FK hacia tabla Producto)
    private int idProducto;

    // Cantidad de unidades vendidas de este producto en este pedido
    private int cantidadVendida;

    // Precio por unidad al momento de la venta; se guarda aqui porque el precio puede cambiar despues
    private BigDecimal precioUnitario;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public DetallePedido() {}

    // Constructor completo: permite crear un DetallePedido con todos sus datos de una sola vez
    public DetallePedido(int idDetalle, int idPedido, int idProducto,
                        int cantidadVendida, BigDecimal precioUnitario) {
        // Asigna el ID del detalle
        this.idDetalle = idDetalle;
        // Asigna el ID del pedido al que pertenece
        this.idPedido = idPedido;
        // Asigna el ID del producto vendido
        this.idProducto = idProducto;
        // Asigna la cantidad de unidades vendidas
        this.cantidadVendida = cantidadVendida;
        // Asigna el precio unitario al momento de la venta
        this.precioUnitario = precioUnitario;
    }

    // Getter: devuelve el ID del detalle
    public int getIdDetalle() { return idDetalle; }
    // Setter: permite asignar o modificar el ID del detalle
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    // Getter: devuelve el ID del pedido al que pertenece este detalle
    public int getIdPedido() { return idPedido; }
    // Setter: permite asignar el pedido al que pertenece este detalle
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    // Getter: devuelve el ID del producto vendido en este detalle
    public int getIdProducto() { return idProducto; }
    // Setter: permite asignar el producto que se vendio
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    // Getter: devuelve la cantidad de unidades vendidas
    public int getCantidadVendida() { return cantidadVendida; }
    // Setter: permite modificar la cantidad vendida
    public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    // Getter: devuelve el precio unitario al momento de la venta
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    // Setter: permite asignar el precio unitario del producto en este detalle
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    // Nombre del producto obtenido con JOIN desde la tabla Producto (no es columna directa de detalle_pedido)
    // Se llena en el DAO cuando se consultan los detalles de un pedido para mostrarlos en la vista
    private String nombreProducto;
    // Getter: devuelve el nombre del producto para mostrarlo en el historial de ventas
    public String getNombreProducto() { return nombreProducto; }
    // Setter: permite asignar el nombre del producto tras el JOIN en el DAO
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
}

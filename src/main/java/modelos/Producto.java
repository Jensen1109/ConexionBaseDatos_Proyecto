// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

// Importa BigDecimal para manejar precios con precision decimal (evita errores de redondeo de double)
import java.math.BigDecimal;
// Importa LocalDate para manejar fechas sin hora (usado para la fecha de vencimiento)
import java.time.LocalDate;

/**
 * Modelo que representa la tabla "Producto" en la base de datos.
 * Cada instancia de esta clase equivale a un producto que se vende en la tienda.
 * Contiene informacion como nombre, precio, stock y fecha de vencimiento.
 */
public class Producto {

    // ID unico del producto (columna id_producto, clave primaria, AUTO_INCREMENT)
    private int idProducto;

    // ID de la categoria a la que pertenece el producto (FK hacia tabla Categoria, ej: "Bebidas", "Lacteos")
    private int idCategoria;

    // ID de la imagen asociada al producto (FK hacia tabla Imagen)
    private int idImagen;

    // Nombre comercial del producto (ej: "Leche Entera 1L", "Arroz Diana 500g")
    private String nombre;

    // Precio de venta del producto; se usa BigDecimal para evitar errores de redondeo con dinero
    private BigDecimal precio;

    // Cantidad actual en inventario (cuantas unidades hay disponibles para vender)
    private int stock;

    // Cantidad minima que debe haber en inventario antes de generar una alerta de reabastecimiento
    private int stockMinimo;

    // Descripcion detallada del producto (informacion adicional para el usuario)
    private String descripcion;

    // Fecha en que el producto vence; permite controlar productos perecederos
    private LocalDate fechaVencimiento;

    // Unidad de medida del producto (ej: "kg", "litros", "unidades")
    private String unidadMedida;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Producto() {}

    // Constructor completo: permite crear un Producto con todos sus datos de una sola vez
    public Producto(int idProducto, int idCategoria, int idImagen, String nombre,
                    BigDecimal precio, int stock, int stockMinimo, String descripcion,
                    LocalDate fechaVencimiento, String unidadMedida) {
        // Asigna el ID del producto recibido como parametro
        this.idProducto = idProducto;
        // Asigna la categoria a la que pertenece este producto
        this.idCategoria = idCategoria;
        // Asigna el ID de la imagen asociada al producto
        this.idImagen = idImagen;
        // Asigna el nombre comercial del producto
        this.nombre = nombre;
        // Asigna el precio de venta del producto
        this.precio = precio;
        // Asigna la cantidad actual disponible en inventario
        this.stock = stock;
        // Asigna el stock minimo para alertas de reabastecimiento
        this.stockMinimo = stockMinimo;
        // Asigna la descripcion detallada del producto
        this.descripcion = descripcion;
        // Asigna la fecha de vencimiento del producto
        this.fechaVencimiento = fechaVencimiento;
        // Asigna la unidad de medida (kg, litros, etc.)
        this.unidadMedida = unidadMedida;
    }

    // Getter: devuelve el ID del producto
    public int getIdProducto() { return idProducto; }
    // Setter: permite asignar o modificar el ID del producto
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    // Getter: devuelve el ID de la categoria del producto
    public int getIdCategoria() { return idCategoria; }
    // Setter: permite cambiar la categoria del producto
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    // Getter: devuelve el ID de la imagen asociada
    public int getIdImagen() { return idImagen; }
    // Setter: permite cambiar la imagen asociada al producto
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    // Getter: devuelve el nombre del producto
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre del producto
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Getter: devuelve el precio del producto como BigDecimal
    public BigDecimal getPrecio() { return precio; }
    // Setter: permite actualizar el precio del producto
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    // Getter: devuelve la cantidad actual en inventario
    public int getStock() { return stock; }
    // Setter: permite actualizar el stock (ej: al registrar una venta se reduce)
    public void setStock(int stock) { this.stock = stock; }

    // Getter: devuelve el stock minimo configurado para alertas
    public int getStockMinimo() { return stockMinimo; }
    // Setter: permite cambiar el umbral minimo de stock
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    // Getter: devuelve la descripcion del producto
    public String getDescripcion() { return descripcion; }
    // Setter: permite actualizar la descripcion del producto
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Getter: devuelve la fecha de vencimiento del producto
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    // Setter: permite actualizar la fecha de vencimiento
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    // Getter: devuelve la unidad de medida del producto
    public String getUnidadMedida() { return unidadMedida; }
    // Setter: permite cambiar la unidad de medida
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    // URL de la imagen del producto, campo auxiliar que se llena con un JOIN (no es columna directa de Producto)
    private String imagenUrl;
    // Getter: devuelve la URL de la imagen del producto para mostrarla en la vista
    public String getImagenUrl() { return imagenUrl; }
    // Setter: permite asignar la URL de la imagen tras consultar la tabla Imagen con JOIN
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}

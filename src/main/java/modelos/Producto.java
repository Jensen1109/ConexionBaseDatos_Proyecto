package modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Producto {
    private int idProducto;
    private int idCategoria;
    private int idImagen;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private String descripcion;
    private LocalDate fechaVencimiento;
    private String unidadMedida;

    public Producto() {}

    public Producto(int idProducto, int idCategoria, int idImagen, String nombre,
                    BigDecimal precio, int stock, int stockMinimo, String descripcion,
                    LocalDate fechaVencimiento, String unidadMedida) {
        this.idProducto = idProducto;
        this.idCategoria = idCategoria;
        this.idImagen = idImagen;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.descripcion = descripcion;
        this.fechaVencimiento = fechaVencimiento;
        this.unidadMedida = unidadMedida;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public int getIdImagen() { return idImagen; }
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
}
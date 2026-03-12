package modelos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {
    private int idPedido;
    private int idCliente;
    private int idUsuario;
    private int idPago;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private String estado;

    public Pedido() {}

    public Pedido(int idPedido, int idCliente, int idUsuario, int idPago,
                LocalDateTime fechaVenta, BigDecimal total, String estado) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.idPago = idPago;
        this.fechaVenta = fechaVenta;
        this.total = total;
        this.estado = estado;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Campo calculado via JOIN (no persiste en BD)
    private String nombreCliente;
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
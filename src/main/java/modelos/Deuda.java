package modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Deuda {
    private int idDeuda;
    private int idPedido;
    private BigDecimal montoPendiente;
    private String estado;
    private BigDecimal abono;
    private LocalDate fechaAbono;

    public Deuda() {}

    public Deuda(int idDeuda, int idPedido, BigDecimal montoPendiente,
                String estado, BigDecimal abono, LocalDate fechaAbono) {
        this.idDeuda = idDeuda;
        this.idPedido = idPedido;
        this.montoPendiente = montoPendiente;
        this.estado = estado;
        this.abono = abono;
        this.fechaAbono = fechaAbono;
    }

    public int getIdDeuda() { return idDeuda; }
    public void setIdDeuda(int idDeuda) { this.idDeuda = idDeuda; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public BigDecimal getMontoPendiente() { return montoPendiente; }
    public void setMontoPendiente(BigDecimal montoPendiente) { this.montoPendiente = montoPendiente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getAbono() { return abono; }
    public void setAbono(BigDecimal abono) { this.abono = abono; }

    public LocalDate getFechaAbono() { return fechaAbono; }
    public void setFechaAbono(LocalDate fechaAbono) { this.fechaAbono = fechaAbono; }

    // Campo calculado via JOIN (no persiste en BD)
    private String nombreCliente;
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
package modelos;

public class MetodoPago {
    private int idPago;
    private String nombre;

    public MetodoPago() {}

    public MetodoPago(int idPago, String nombre) {
        this.idPago = idPago;
        this.nombre = nombre;
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
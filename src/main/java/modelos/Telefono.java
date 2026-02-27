package modelos;

public class Telefono {
    private int idTelefono;
    private String telefono;
    private int usuarioId;

    public Telefono() {}

    public Telefono(int idTelefono, String telefono, int usuarioId) {
        this.idTelefono = idTelefono;
        this.telefono = telefono;
        this.usuarioId = usuarioId;
    }

    public int getIdTelefono() { return idTelefono; }
    public void setIdTelefono(int idTelefono) { this.idTelefono = idTelefono; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
}
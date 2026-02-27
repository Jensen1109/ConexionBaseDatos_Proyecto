package modelos;

public class Imagen {
    private int idImagen;
    private int idProducto;
    private String url;

    public Imagen() {}

    public Imagen(int idImagen, int idProducto, String url) {
        this.idImagen = idImagen;
        this.idProducto = idProducto;
        this.url = url;
    }

    public int getIdImagen() { return idImagen; }
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
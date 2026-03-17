// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "Imagen" en la base de datos.
 * Cada imagen esta asociada a un producto y almacena la URL o ruta del archivo de imagen.
 * Esto permite mostrar fotos de los productos en la interfaz web de la tienda.
 */
public class Imagen {

    // ID unico de la imagen (columna id_imagen, clave primaria, AUTO_INCREMENT)
    private int idImagen;

    // ID del producto al que pertenece esta imagen (FK hacia tabla Producto)
    private int idProducto;

    // URL o ruta del archivo de imagen (ej: "img/productos/leche.jpg")
    private String url;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Imagen() {}

    // Constructor completo: permite crear una Imagen con todos sus datos de una sola vez
    public Imagen(int idImagen, int idProducto, String url) {
        // Asigna el ID de la imagen
        this.idImagen = idImagen;
        // Asigna el ID del producto al que corresponde esta imagen
        this.idProducto = idProducto;
        // Asigna la URL o ruta donde se encuentra almacenada la imagen
        this.url = url;
    }

    // Getter: devuelve el ID de la imagen
    public int getIdImagen() { return idImagen; }
    // Setter: permite asignar o modificar el ID de la imagen
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    // Getter: devuelve el ID del producto asociado a esta imagen
    public int getIdProducto() { return idProducto; }
    // Setter: permite cambiar el producto al que esta asociada la imagen
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    // Getter: devuelve la URL o ruta del archivo de imagen
    public String getUrl() { return url; }
    // Setter: permite actualizar la URL o ruta de la imagen
    public void setUrl(String url) { this.url = url; }
}

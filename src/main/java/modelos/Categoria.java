// Paquete "modelos": agrupa las clases que representan las tablas de la base de datos
package modelos;

/**
 * Modelo que representa la tabla "Categoria" en la base de datos.
 * Las categorias sirven para clasificar los productos de la tienda
 * (por ejemplo: "Bebidas", "Lacteos", "Granos", "Aseo").
 * Cada producto pertenece a una sola categoria.
 */
public class Categoria {

    // ID unico de la categoria (columna id_categoria, clave primaria, AUTO_INCREMENT)
    private int idCategoria;

    // Nombre descriptivo de la categoria (ej: "Bebidas", "Lacteos", "Granos")
    private String nombre;

    // Constructor vacio: necesario para que el DAO pueda crear un objeto y llenarlo con setters
    public Categoria() {}

    // Constructor completo: permite crear una Categoria con su ID y nombre de una sola vez
    public Categoria(int idCategoria, String nombre) {
        // Asigna el ID de la categoria recibido como parametro
        this.idCategoria = idCategoria;
        // Asigna el nombre de la categoria
        this.nombre = nombre;
    }

    // Getter: devuelve el ID de la categoria
    public int getIdCategoria() { return idCategoria; }
    // Setter: permite asignar o modificar el ID de la categoria
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    // Getter: devuelve el nombre de la categoria
    public String getNombre() { return nombre; }
    // Setter: permite actualizar el nombre de la categoria
    public void setNombre(String nombre) { this.nombre = nombre; }
}

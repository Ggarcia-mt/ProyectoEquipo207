// Clase que representa un producto del menú.

public class Producto {
    private int id;
    private String nombre;
    private double precio;

    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }
    
    public Producto(String nombre, double precio) {
        this(-1, nombre, precio); // -1 indica que aún no tiene ID de BD
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setId(int id) {
        this.id = id;
    }
     // Sobrescribir toString para mostrar en listas o JComboBox
    @Override
    public String toString() {
        return nombre + " ($" + String.format("%.2f", precio) + ")";
    }
}
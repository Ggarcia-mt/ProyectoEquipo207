package proyectoequipo207;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que gestiona la conexión y las operaciones CRUD (Crear, Leer, Actualizar, Borrar)
 * con la base de datos SQLite. La base de datos se guarda en 'cafesoft.db'.
 */
public class DatabaseManager {

    // URL de conexión a la base de datos SQLite
    private static final String DB_URL = "jdbc:sqlite:cafesoft.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            // Cargar el driver JDBC de SQLite
            Class.forName("org.sqlite.JDBC");
            // Inicializar y conectar la base de datos
            initializeDB();
            System.out.println("DatabaseManager inicializado y conectado a SQLite.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver SQLite JDBC no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos.");
            e.printStackTrace();
        }
    }
    
    /**
     * Establece la conexión y crea las tablas si no existen.
     */
    private void initializeDB() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL);
        createTables();
        loadInitialData();
    }

    /**
     * Cierra la conexión de la base de datos.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión SQLite cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /**
     * Define y crea las tablas 'productos' y 'ventas' si no existen.
     */
    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // 1. Tabla de Productos
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                              "nombre TEXT NOT NULL UNIQUE," +
                              "precio REAL NOT NULL)";
        stmt.execute(sqlProductos);

        // 2. Tabla de Ventas (para el historial)
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas (" +
                           "timestamp INTEGER PRIMARY KEY," + // Usamos el tiempo UNIX como ID
                           "productoNombre TEXT NOT NULL," +
                           "cantidad INTEGER NOT NULL," +
                           "precioUnitario REAL NOT NULL," +
                           "subtotal REAL NOT NULL)";
        stmt.execute(sqlVentas);
        
        stmt.close();
    }
    
    /**
     * Inserta datos de prueba si la tabla de productos está vacía.
     */
    private void loadInitialData() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM productos";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Cargando datos iniciales...");
                String insertSql = "INSERT INTO productos (nombre, precio) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    
                    // Datos iniciales
                    String[][] data = {
                        {"Café Americano", "2500"},
                        {"Latte Caramel", "4200"},
                        {"Muffin de Vainilla", "2700"},
                        {"Sandwich de Pavo", "5500"},
                        {"Jugo de Naranja", "3500"}
                    };
                    
                    for (String[] item : data) {
                        pstmt.setString(1, item[0]);
                        pstmt.setDouble(2, Double.parseDouble(item[1]));
                        pstmt.addBatch(); // Agrega la instrucción al lote
                    }
                    pstmt.executeBatch(); // Ejecuta todas las inserciones
                }
            }
        }
    }

    // --- Métodos de Gestión de Productos (CRUD) ---

    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY nombre";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                productos.add(new Producto(id, nombre, precio));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Agrega un nuevo producto a la BD y actualiza el ID del objeto Producto.
     */
    public void agregarProducto(Producto nuevoProducto) {
        String sql = "INSERT INTO productos (nombre, precio) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nuevoProducto.getNombre());
            pstmt.setDouble(2, nuevoProducto.getPrecio());
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        nuevoProducto.setId(rs.getInt(1)); // Asigna el ID generado por la BD
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
        }
    }

    public boolean actualizarProducto(Producto productoActualizado) {
        String sql = "UPDATE productos SET nombre = ?, precio = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, productoActualizado.getNombre());
            pstmt.setDouble(2, productoActualizado.getPrecio());
            pstmt.setInt(3, productoActualizado.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // --- Métodos de Transacción y Reportes ---

    public boolean registrarVenta(String productoNombre, int cantidad, double precioUnitario) {
        String sql = "INSERT INTO ventas (timestamp, productoNombre, cantidad, precioUnitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            long timestamp = System.currentTimeMillis();
            double subtotal = cantidad * precioUnitario;
            
            pstmt.setLong(1, timestamp);
            pstmt.setString(2, productoNombre);
            pstmt.setInt(3, cantidad);
            pstmt.setDouble(4, precioUnitario);
            pstmt.setDouble(5, subtotal);
            
            pstmt.executeUpdate();
            return true; 
        } catch (SQLException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            return false;
        }
    }
    
    public List<VentaRecord> obtenerHistorialVentas() {
        List<VentaRecord> historial = new ArrayList<>();
        String sql = "SELECT timestamp, productoNombre, cantidad, precioUnitario, subtotal FROM ventas ORDER BY timestamp DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long timestamp = rs.getLong("timestamp");
                String productoNombre = rs.getString("productoNombre");
                int cantidad = rs.getInt("cantidad");
                double precioUnitario = rs.getDouble("precioUnitario");
                double subtotal = rs.getDouble("subtotal");
                
                historial.add(new VentaRecord(timestamp, productoNombre, cantidad, precioUnitario, subtotal));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial de ventas: " + e.getMessage());
        }
        return historial;
    }
}

/**
 * Clase auxiliar para el historial de ventas.
 * No necesita la anotación 'public' ya que es interna al paquete.
 */
class VentaRecord {
    private long timestamp;
    private String productoNombre;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public VentaRecord(long timestamp, String productoNombre, int cantidad, double precioUnitario, double subtotal) {
        this.timestamp = timestamp;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public long getTimestamp() { return timestamp; }
    public String getProductoNombre() { return productoNombre; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal() { return subtotal; }
}
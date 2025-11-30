package proyectoequipo207;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Clase que gestiona la conexión y las operaciones CRUD 
 * para la base de datos SQLite 'cafesoft.db'.
 * Incorpora validación de datos para asegurar la integridad.
 */
public class DatabaseManager {
    
    private static final String URL = "jdbc:sqlite:cafesoft.db";

    public DatabaseManager() {
        conectar();
        crearTablas(); 
        inicializarDatosDummy();
    }
    
    /**
     * Establece y retorna la conexión a la base de datos SQLite.
     * @return Objeto Connection o null si falla.
     */
    private Connection conectar() {
        Connection conn = null;
        try {
            // Cargar el driver SQLite
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            System.out.println("Conexión a la base de datos exitosa.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error FATAL: No se encontró el driver JDBC de SQLite. Asegúrate de que el JAR esté en el classpath.");
        } catch (SQLException e) {
            System.err.println("Error de conexión a la base de datos: " + e.getMessage());
        }
        return conn;
    }
    
    /**
     * Crea las tablas necesarias (productos y ventas). Intentará recrear las tablas 
     * si se detecta un esquema obsoleto (error SQL).
     */
    private void crearTablas() {
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT NOT NULL UNIQUE,"
                + "precio REAL NOT NULL)";
        
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "fecha TEXT NOT NULL,"
                + "nombre_producto TEXT NOT NULL,"
                + "cantidad INTEGER NOT NULL,"
                + "precio_unitario REAL NOT NULL)";
        
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            
            // Intento 1: Crear las tablas si no existen
            stmt.execute(sqlProductos);
            stmt.execute(sqlVentas);
            System.out.println("Tablas de la base de datos verificadas o creadas.");

        } catch (SQLException e) {
            System.err.println("Error al crear/verificar tablas. Intentando forzar la recreación: " + e.getMessage());
            
            // Si falla la creación o verificación (esquema obsoleto o faltante), forzamos DROP y CREATE
             try (Connection conn = conectar();
                  Statement stmt = conn.createStatement()) {
                 
                System.out.println("-> Forzando el borrado (DROP) de tablas antiguas...");
                stmt.execute("DROP TABLE IF EXISTS productos");
                stmt.execute("DROP TABLE IF EXISTS ventas");
                
                System.out.println("-> Recreando tablas con el esquema actualizado...");
                stmt.execute(sqlProductos);
                stmt.execute(sqlVentas);
                System.out.println("Tablas recreadas con éxito.");

            } catch (SQLException ex) {
                System.err.println("Error fatal al intentar recrear las tablas: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Inserta datos iniciales si las tablas están vacías.
     */
    private void inicializarDatosDummy() {
        // Verificar si existen productos para evitar duplicados si las tablas se recrean
        boolean productosExistentes = false;
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM productos")) {
            if (rs.next() && rs.getInt(1) > 0) {
                productosExistentes = true;
            }
        } catch (SQLException e) {
             System.err.println("Error al verificar existencia de productos: " + e.getMessage());
        }
        
        if (!productosExistentes) {
            System.out.println("Inicializando productos dummy...");
            agregarProducto(new Producto(0, "Espresso", 2.50));
            agregarProducto(new Producto(0, "Latte", 4.00));
            agregarProducto(new Producto(0, "Capuccino", 4.50));
            agregarProducto(new Producto(0, "Muffin de Chocolate", 3.00));
        }
        
        // Opcional: Agregar ventas dummy solo si no hay ninguna
        boolean ventasExistentes = false;
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ventas")) {
            if (rs.next() && rs.getInt(1) > 0) {
                ventasExistentes = true;
            }
        } catch (SQLException e) {
             System.err.println("Error al verificar existencia de ventas: " + e.getMessage());
        }
        
        if (!ventasExistentes) {
            System.out.println("Inicializando ventas dummy...");
            // Usamos la validación para registrar estas ventas
            registrarVenta("Espresso", 2, 2.50);
            registrarVenta("Latte", 1, 4.00);
        }
    }

    // --- MÉTODOS DE PRODUCTO (Inventario) ---

    /** Obtiene todos los productos del inventario desde la DB. */
    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY nombre";
        
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("precio")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return productos;
    }
    
    /** Agrega un nuevo producto a la DB con validación. */
    public void agregarProducto(Producto producto) {
        // --- VALIDACIÓN DE DATOS ---
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            System.err.println("Error de Validación: El nombre del producto no puede estar vacío.");
            return;
        }
        if (producto.getPrecio() <= 0) {
            System.err.println("Error de Validación: El precio del producto debe ser positivo.");
            return;
        }
        // ---------------------------
        
        String sql = "INSERT INTO productos(nombre, precio) VALUES(?, ?)";
        
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getNombre().trim());
            pstmt.setDouble(2, producto.getPrecio());
            pstmt.executeUpdate();
            System.out.println("Producto agregado: " + producto.getNombre());
            
        } catch (SQLException e) {
            // Manejar errores de nombre duplicado
            if (e.getErrorCode() == 19) { // SQLite constraint violation (Unique constraint)
                System.err.println("Error: El producto '" + producto.getNombre() + "' ya existe.");
            } else {
                System.err.println("Error al agregar producto: " + e.getMessage());
            }
        }
    }

    /** Actualiza un producto existente en la DB con validación. */
    public boolean actualizarProducto(Producto productoActualizado) {
        // --- VALIDACIÓN DE DATOS ---
        if (productoActualizado.getId() <= 0) {
            System.err.println("Error de Validación: ID de producto inválido para actualizar.");
            return false;
        }
        if (productoActualizado.getNombre() == null || productoActualizado.getNombre().trim().isEmpty()) {
            System.err.println("Error de Validación: El nombre del producto no puede estar vacío.");
            return false;
        }
        if (productoActualizado.getPrecio() <= 0) {
            System.err.println("Error de Validación: El precio del producto debe ser positivo.");
            return false;
        }
        // ---------------------------
        
        String sql = "UPDATE productos SET nombre = ?, precio = ? WHERE id = ?";
        
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productoActualizado.getNombre().trim());
            pstmt.setDouble(2, productoActualizado.getPrecio());
            pstmt.setInt(3, productoActualizado.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("Advertencia: No se encontró ningún producto con ID " + productoActualizado.getId() + " para actualizar.");
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un producto por ID de la DB con validación. */
    public boolean eliminarProducto(int id) {
        // --- VALIDACIÓN DE DATOS ---
        if (id <= 0) {
            System.err.println("Error de Validación: ID de producto inválido para eliminar.");
            return false;
        }
        // ---------------------------
        
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Advertencia: No se encontró ningún producto con ID " + id + " para eliminar.");
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
    
    // --- MÉTODOS DE VENTA (POS y Reportes) ---

    /**
     * Registra una línea de item de venta en la tabla 'ventas' con validación.
     */
    public void registrarVenta(String nombreProducto, int cantidad, double precioUnitario) {
        // --- VALIDACIÓN DE DATOS ---
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            System.err.println("Error de Validación: El nombre del producto para la venta no puede estar vacío.");
            return;
        }
        if (cantidad <= 0) {
            System.err.println("Error de Validación: La cantidad de venta debe ser mayor que cero.");
            return;
        }
        if (precioUnitario <= 0) {
            System.err.println("Error de Validación: El precio unitario de la venta debe ser positivo.");
            return;
        }
        // ---------------------------
        
        String sql = "INSERT INTO ventas(fecha, nombre_producto, cantidad, precio_unitario) VALUES(?, ?, ?, ?)";
        
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Usar el formato ISO para la fecha (TEXT en SQLite)
            String fechaSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            pstmt.setString(1, fechaSql);
            pstmt.setString(2, nombreProducto.trim());
            pstmt.setInt(3, cantidad);
            pstmt.setDouble(4, precioUnitario);
            pstmt.executeUpdate();
            
            System.out.println("Venta registrada en DB: " + nombreProducto + " x" + cantidad);

        } catch (SQLException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
        }
    }

    /**
     * Obtiene el historial completo de ventas.
     */
    public List<Venta> obtenerVentas() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT id, fecha, nombre_producto, cantidad, precio_unitario FROM ventas ORDER BY id DESC";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Convertir la fecha de String (SQLite) a Date (Java)
                Date fechaVenta = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("fecha"));
                
                ventas.add(new Venta(
                    rs.getInt("id"), 
                    fechaVenta,
                    rs.getString("nombre_producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario")
                ));
            }
        } catch (SQLException | ParseException e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
        }
        return ventas;
    }
}

/**
 * Clase modelo para el Producto (Inventario).
 */
class Producto {
    private int id;
    private String nombre;
    private double precio;

    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }

    // Setters
    public void setId(int id) { this.id = id; }
}

/**
 * Modelo de datos que representa una línea de item vendida.
 */
class Venta {
    private int idVenta; 
    private Date fechaVenta;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;

    public Venta(int idVenta, Date fechaVenta, String nombreProducto, int cantidad, double precioUnitario) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters
    public int getIdVenta() { return idVenta; }
    public Date getFechaVenta() { return fechaVenta; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    
    // Setters
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
}
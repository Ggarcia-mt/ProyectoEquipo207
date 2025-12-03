package proyectoequipo207;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DatabaseManager {
    
    private static final String URL = "jdbc:sqlite:cafesoft.db";

    public DatabaseManager() {
        conectar();
        crearTablas(); 
        inicializarDatosDummy();
    }
    
    // Establece y retorna la conexión a la base de datos SQLite.
    
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
    private void crearTablas() {
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            String sqlProductos = "CREATE TABLE IF NOT EXISTS productos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nombre TEXT NOT NULL UNIQUE,"
                    + "precio REAL NOT NULL)";
            
            // Verificación de nombres de columna: producto, precio_unitario, fecha_venta
            String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "producto TEXT NOT NULL,"
                    + "cantidad INTEGER NOT NULL,"
                    + "precio_unitario REAL NOT NULL,"
                    + "fecha_venta DATETIME DEFAULT CURRENT_TIMESTAMP)";

            // TABLA DE USUARIOS 
            String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios ("
                    + "username TEXT PRIMARY KEY,"
                    + "password TEXT NOT NULL,"
                    + "rol TEXT NOT NULL)";

            stmt.execute(sqlProductos);
            stmt.execute(sqlVentas);
            stmt.execute(sqlUsuarios); 
        } catch (SQLException e) {
            System.err.println("Error creando tablas: " + e.getMessage());
        }
    }

    private void inicializarDatosDummy() {
        // Inicialización de productos (dejamos tu lógica si ya existe)
        if (obtenerProductos().isEmpty()) {
            agregarProducto("Café Americano", 4500);
            agregarProducto("Cappuccino", 6500);
            agregarProducto("Latte", 6000);
            agregarProducto("Muffin de Arándanos", 4800);
            agregarProducto("Sandwich de Pollo", 8500);
            System.out.println("Datos de productos inicializados.");
        }
        
        // Inicialización de Usuarios por defecto si no existen
        try (Connection conn = conectar();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT count(*) FROM usuarios");
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                insertInitialUser(conn, "admin", "123", "ADMIN");
                insertInitialUser(conn, "vendedor", "456", "VENDEDOR");
                System.out.println("Usuarios por defecto creados (admin/123, vendedor/456).");
            }
        } catch (SQLException e) {
            System.err.println("Error verificando o creando usuarios iniciales: " + e.getMessage());
        }
    }
    
    private void insertInitialUser(Connection conn, String username, String password, String rol) throws SQLException {
        String sql = "INSERT OR IGNORE INTO usuarios(username, password, rol) VALUES(?, ?, ?)";
        // Reutilizamos la conexión 'conn' que está abierta en inicializarDatosDummy
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, rol);
            pstmt.executeUpdate();
        }
    }


    //  MÉTODOS DE USUARIOS 

    // Este método sigue abriendo una nueva conexión para el registro en tiempo de ejecución.
    public void registrarUsuario(String username, String password, String rol) {
        String sql = "INSERT OR IGNORE INTO usuarios(username, password, rol) VALUES(?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, rol);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error registrando usuario: " + e.getMessage());
        }
    }

    public Usuario autenticarUsuario(String username, String password) {
        String sql = "SELECT rol FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String rol = rs.getString("rol");
                return new Usuario(username, rol); 
            }
        } catch (SQLException e) {
            System.err.println("Error de autenticación: " + e.getMessage());
        }
        return null; // Retorna null si el login falla
    }

    // MÉTODOS DE PRODUCTOS 

    public boolean agregarProducto(String nombre, double precio) {
        String sql = "INSERT INTO productos(nombre, precio) VALUES(?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos";
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
            System.err.println("Error obteniendo productos: " + e.getMessage());
        }
        return productos;
    }

    public boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error eliminando producto: " + e.getMessage());
            return false;
        }
    }

    // MÉTODOS DE VENTAS 

    public void registrarVenta(String producto, int cantidad, double precioUnitario) {
        // Asegúrate que los nombres de las columnas coincidan con la definición de la tabla.
        String sql = "INSERT INTO ventas(producto, cantidad, precio_unitario) VALUES(?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto);
            pstmt.setInt(2, cantidad);
            pstmt.setDouble(3, precioUnitario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error registrando venta: " + e.getMessage());
        }
    }

    public List<Venta> obtenerVentas() {
        List<Venta> ventas = new ArrayList<>();
        // Asegúrate que los nombres de las columnas en el SELECT coincidan con la tabla.
        String sql = "SELECT id, fecha_venta, producto, cantidad, precio_unitario FROM ventas ORDER BY fecha_venta DESC";
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date fecha = new Date(); 
                try {
                     // El nombre de la columna en la BD es 'fecha_venta'
                     String dateStr = rs.getString("fecha_venta"); 
                     java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     fecha = sdf.parse(dateStr);
                } catch(Exception ex) {
                    System.err.println("Error al parsear fecha: " + ex.getMessage());
                    // Usamos la fecha actual como fallback si falla el parseo
                    fecha = new Date(); 
                }

                ventas.add(new Venta(
                    rs.getInt("id"),
                    fecha,
                    rs.getString("producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
        }
        return ventas;
    }
}
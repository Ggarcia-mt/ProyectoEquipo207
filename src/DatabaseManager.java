import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane; 


public class DatabaseManager {


    private static final String URL_DB = "jdbc:sqlite:cafeteria.db";

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL_DB);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error crítico al conectar a la base de datos: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }

    public void inicializarBaseDeDatos() {
        // SQL para crear la tabla de ventas
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas ("
                         + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                         + " producto_nombre TEXT NOT NULL,"
                         + " cantidad INTEGER NOT NULL,"
                         + " fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                         + ");";
        
        // SQL para crear la tabla de productos (para el menú)
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos ("
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + " nombre TEXT UNIQUE NOT NULL,"
                            + " precio REAL NOT NULL"
                            + ");";
                            
        // SQL para crear la tabla de usuarios
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios ("
                           + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                           + " usuario TEXT UNIQUE NOT NULL,"
                           + " password TEXT NOT NULL," 
                           + " rol TEXT NOT NULL"   
                           + ");";


        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlVentas);
            stmt.execute(sqlProductos);
            stmt.execute(sqlUsuarios); 
            System.out.println("Base de datos inicializada. Tablas 'ventas', 'productos' y 'usuarios' listas.");
            
            insertarUsuariosPorDefecto(conn);

        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error al inicializar la base de datos: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void insertarUsuariosPorDefecto(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM usuarios";
        String insertSql = "INSERT INTO usuarios(usuario, password, rol) VALUES (?, ?, ?)";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    
                    // Usuario Administrador
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123"); 
                    pstmt.setString(3, "ADMIN");
                    pstmt.addBatch();
                    
                    // Usuario Vendedor
                    pstmt.setString(1, "vendedor");
                    pstmt.setString(2, "venta123"); 
                    pstmt.setString(3, "VENDEDOR");
                    pstmt.addBatch();

                    pstmt.executeBatch();
                    System.out.println("Usuarios por defecto 'admin', 'vendedor' ");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuarios por defecto: " + e.getMessage());
        }
    }

    public Usuario autenticarUsuario(String usuario, String password) {
        String sql = "SELECT rol FROM usuarios WHERE usuario = ? AND password = ?";
        Usuario usuarioAutenticado = null;

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario);
            pstmt.setString(2, password); // NOTA: No es seguro usar contraseñas en texto plano
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol");
                    usuarioAutenticado = new Usuario(usuario, rol);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error de autenticación: " + e.getMessage());
        }
        return usuarioAutenticado;
    }

  
    public boolean registrarVenta(String productoNombre, int cantidad) {
        String sql = "INSERT INTO ventas(producto_nombre, cantidad) VALUES(?, ?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productoNombre);
            pstmt.setInt(2, cantidad);
            pstmt.executeUpdate();
            return true; 

        } catch (SQLException e) {
            System.err.println("Error al registrar la venta: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error al guardar la venta: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
            return false; 
        }
    }
    
    // --- MÉTODOS PARA GESTIÓN DE MENÚ ---

    public int insertarProducto(Producto producto) {
        String sql = "INSERT INTO productos(nombre, precio) VALUES(?, ?)";
        int idGenerado = -1;

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, producto.getNombre());
            pstmt.setDouble(2, producto.getPrecio());
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                 System.err.println("Error de duplicidad: " + producto.getNombre() + " ya existe.");
            } else {
                 System.err.println("Error al insertar producto: " + e.getMessage());
            }
        }
        return idGenerado;
    }

    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY nombre ASC";

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("precio")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error al cargar el menú: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
        return productos;
    }
    
 
    public boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error al eliminar producto: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
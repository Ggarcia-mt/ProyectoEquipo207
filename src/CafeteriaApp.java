import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class CafeteriaApp extends JFrame {
    
    private DatabaseManager dbManager;
    private Usuario usuarioActual;
    private JFrame loginFrame;

    private JComboBox<Producto> productoComboBox;
    private JSpinner cantidadSpinner;
    private JTable ordenTable;
    private DefaultTableModel ordenTableModel;
    private JLabel totalLabel;
    private Map<String, Producto> productosMap;

    public CafeteriaApp() {
        dbManager = new DatabaseManager();
        dbManager.inicializarBaseDeDatos();
        this.productosMap = new Hashtable<>();
        configurarLookAndFeel();
    }
    
    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al configurar Look and Feel: " + e.getMessage());
        }
    }
    public void mostrarLogin() {
        loginFrame = new JFrame("Login - Sistema de Cafetería");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 250);
        loginFrame.setLayout(new BorderLayout(10, 10));
        loginFrame.setLocationRelativeTo(null); // Centrar ventana
        
        // Panel principal con borde
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField userField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        
        panel.add(new JLabel("Usuario:"));
        panel.add(userField);
        panel.add(new JLabel("Contraseña:"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Ingresar");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Listener del botón de login
        ActionListener loginListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = userField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                autenticar(usuario, password);
            }
        };
        
        loginButton.addActionListener(loginListener);
        loginFrame.getRootPane().setDefaultButton(loginButton);
        panel.add(new JLabel()); 
        panel.add(loginButton);

        loginFrame.add(new JLabel("☕ INICIO DE SESIÓN ☕", SwingConstants.CENTER), BorderLayout.NORTH);
        loginFrame.add(panel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    private void autenticar(String usuario, String password) {
        Usuario usuarioAutenticado = dbManager.autenticarUsuario(usuario, password);
        
        if (usuarioAutenticado != null) {
            // Asignar el usuario actual y mostrar el POS
            this.usuarioActual = usuarioAutenticado;
            mostrarPOS();
            
            JOptionPane.showMessageDialog(loginFrame, 
                "¡Bienvenido, " + usuarioAutenticado.getNombreUsuario() + "!", 
                "Login Exitoso", 
                JOptionPane.INFORMATION_MESSAGE);
            
            loginFrame.dispose(); // Cerrar la ventana de login
            
        } else {
            JOptionPane.showMessageDialog(loginFrame, 
                "Credenciales incorrectas. Intente de nuevo.\n(Usuarios de prueba: admin/admin123 o vendedor/venta123)", 
                "Error de Login", 
                JOptionPane.ERROR_MESSAGE);
            // Limpiar la contraseña
            JPasswordField passwordField = (JPasswordField) ((JPanel)loginFrame.getContentPane().getComponent(1)).getComponent(3);
            passwordField.setText("");
        }
    }
    
    
    public void mostrarPOS() {
        setTitle("Punto de Venta (POS) - Usuario: " + usuarioActual.getNombreUsuario() + 
                 " (" + usuarioActual.getRol() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Espaciado principal
        setSize(800, 600);
        setLocationRelativeTo(null); 
        
        // 1. Panel Superior Controles y Botones de Navegación
        add(crearPanelSuperior(), BorderLayout.NORTH);

        // 2. Panel Central selección de Producto y Carrito
        add(crearPanelCentral(), BorderLayout.CENTER);

        // 3. Panel Inferior Total y Pago
        add(crearPanelInferior(), BorderLayout.SOUTH);

        // Inicializar datos y refrescar
        cargarProductos();
        setVisible(true);
    }

    // Carga los productos del menú desde la base de datos en el JComboBox.
     
    private void cargarProductos() {
        List<Producto> productos = dbManager.obtenerProductos();
        DefaultComboBoxModel<Producto> model = new DefaultComboBoxModel<>();
        
        // Limpiar el mapa antes de cargar nuevos productos
        productosMap.clear(); 
        
        for (Producto p : productos) {
            model.addElement(p);
            productosMap.put(p.getNombre(), p);
        }
        productoComboBox.setModel(model);
    }
    
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel title = new JLabel("🛒 TOMA DE ÓRDENES", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        // 1. Botón de Gestión de Menú (Solo para Admin)
        if (usuarioActual.esAdmin()) {
            JButton menuButton = new JButton("🔧 Gestionar Menú");
            menuButton.addActionListener(e -> {
                MenuManager menuManager = new MenuManager(this, dbManager);
                menuManager.setVisible(true);
                cargarProductos(); // Recarga productos por si hubo cambios
            });
            controls.add(menuButton);
        }
        
        // 2. Botón de Reporte de Ventas (Solo para Admin)
        if (usuarioActual.esAdmin()) {
            JButton reportButton = new JButton("📊 Ver Reportes");
            reportButton.addActionListener(e -> {
                SalesReporter reporter = new SalesReporter(this, dbManager);
                reporter.setVisible(true);
            });
            controls.add(reportButton);
        }
        
        // 3. Botón de Salir
        JButton logoutButton = new JButton("❌ Cerrar Sesión");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Desea cerrar la sesión?", "Confirmar Cierre", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Reiniciar la aplicación para mostrar el login
                dispose();
                new CafeteriaApp().mostrarLogin();
            }
        });
        controls.add(logoutButton);
        
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Panel de ingreso de producto
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        productoComboBox = new JComboBox<>();
        addPanel.add(new JLabel("Producto:"));
        addPanel.add(productoComboBox);
        
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        addPanel.add(new JLabel("Cantidad:"));
        addPanel.add(cantidadSpinner);
        
        JButton addButton = new JButton("➕ Añadir a Orden");
        addButton.addActionListener(e -> agregarProductoACarro());
        addPanel.add(addButton);
        
        panel.add(addPanel, BorderLayout.NORTH);

        // Tabla del carrito de compras
        String[] columnNames = {"Producto", "Precio Unitario", "Cantidad", "Subtotal"};
        
        // Modelo de tabla para manejar tipos de datos 
        ordenTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 3) return Double.class; 
                if (columnIndex == 2) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
               return column == 2;
            }
        };
        ordenTable = new JTable(ordenTableModel);
        
        ordenTableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 2) { 
                try {
                    int nuevaCantidad = (int) ordenTableModel.getValueAt(row, 2);
                    double precio = (double) ordenTableModel.getValueAt(row, 1);
                    double nuevoSubtotal = precio * nuevaCantidad;
                    
                    // Actualizar la celda del subtotal con el nuevo valor Double
                    ordenTableModel.setValueAt(nuevoSubtotal, row, 3);
                    calcularTotalOrden(); 
                } catch (Exception ex) {
                    System.err.println("Error de formato al actualizar la cantidad: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Error en la cantidad. Asegúrese de ingresar un número entero.", 
                        "Error de Cantidad", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ordenTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Panel del total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("TOTAL: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 28));
        totalLabel.setForeground(new Color(25, 135, 84)); // Verde Oscuro
        totalPanel.add(totalLabel);
        panel.add(totalPanel, BorderLayout.NORTH);

        // Panel de botones de acción
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        JButton clearButton = new JButton("🗑️ Limpiar Orden");
        clearButton.addActionListener(e -> limpiarOrden());
        
        JButton checkoutButton = new JButton("💳 Procesar Pago");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        checkoutButton.addActionListener(e -> procesarPago());
        
        actionPanel.add(clearButton);
        actionPanel.add(checkoutButton);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }


    //Añade el producto seleccionado y la cantidad a la tabla del carrito.
    private void agregarProductoACarro() {
        Producto productoSeleccionado = (Producto) productoComboBox.getSelectedItem();
        int cantidad = (int) cantidadSpinner.getValue();

        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un producto antes de añadir a la orden.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Buscar si el producto ya está en el carrito para sumar la cantidad
        boolean encontrado = false;
        for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
            String nombreEnTabla = (String) ordenTableModel.getValueAt(i, 0);
            if (nombreEnTabla.equals(productoSeleccionado.getNombre())) {
                int cantidadActual = (int) ordenTableModel.getValueAt(i, 2);
                int nuevaCantidad = cantidadActual + cantidad;
                double nuevoSubtotal = productoSeleccionado.getPrecio() * nuevaCantidad;
                
                ordenTableModel.setValueAt(nuevaCantidad, i, 2);
                ordenTableModel.setValueAt(nuevoSubtotal, i, 3); 
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            double subtotal = productoSeleccionado.getPrecio() * cantidad;
            ordenTableModel.addRow(new Object[]{
                productoSeleccionado.getNombre(), 
                productoSeleccionado.getPrecio(),
                cantidad,
                subtotal
            });
        }
        
        calcularTotalOrden();
    }
    
    private void calcularTotalOrden() {
        double total = 0.0;
        for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
            try {
                Double subtotal = (Double) ordenTableModel.getValueAt(i, 3);
                if (subtotal != null) {
                    total += subtotal;
                }
            } catch (ClassCastException e) {
                System.err.println("Error de formato (Casteo) al calcular el subtotal: " + e.getMessage());
            }
        }
        totalLabel.setText(String.format("TOTAL: $%.2f", total));
    }
    

    private void procesarPago() {
        if (ordenTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "La orden está vacía. Añada productos antes de pagar.", 
                "Error de Pago", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirmación de pago
        int confirm = JOptionPane.showConfirmDialog(this, 
            totalLabel.getText() + "\n¿Confirmar el pago de la orden?", 
            "Confirmar Pago", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            
            List<String> ventasFallidas = new ArrayList<>();
            for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
                String nombre = (String) ordenTableModel.getValueAt(i, 0);
                int cantidad = (int) ordenTableModel.getValueAt(i, 2);
                
                if (!dbManager.registrarVenta(nombre, cantidad)) {
                    ventasFallidas.add(nombre);
                }
            }
            
            //Mostrar resultado y limpiar
            if (ventasFallidas.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "¡Pago exitoso! La venta ha sido registrada. " + totalLabel.getText(), 
                    "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
                limpiarOrden();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Venta procesada con errores. Falló el registro de: " + String.join(", ", ventasFallidas),
                    "Error Parcial de Venta", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
   
    private void limpiarOrden() {
        ordenTableModel.setRowCount(0);
        calcularTotalOrden();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CafeteriaApp().mostrarLogin();
        });
    }
}
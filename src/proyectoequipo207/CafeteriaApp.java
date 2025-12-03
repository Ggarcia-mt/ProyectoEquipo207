package proyectoequipo207;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Locale;

public class CafeteriaApp extends JFrame {
    
    private DatabaseManager dbManager;
    private Usuario usuarioActual;
    private JFrame loginFrame;
    
    // Componentes del POS
    private JComboBox<Producto> productoComboBox;
    private JSpinner cantidadSpinner;
    private JTable ordenTable;
    private DefaultTableModel ordenTableModel;
    private JLabel totalLabel;
    private Map<String, Producto> productosMap;

    public CafeteriaApp() {
        // Inicializar el gestor de base de datos y asegurar que las tablas existan
        dbManager = new DatabaseManager();
        dbManager.inicializarBaseDeDatos();
        this.productosMap = new Hashtable<>();
        configurarLookAndFeel();
    }
    
    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al configurar Look and Feel: " + e.getMessage());
        }
    }
    
    private Image cargarIcon(String path) {
        try {
            return Toolkit.getDefaultToolkit().getImage(getClass().getResource(path));
        } catch (Exception e) {
            System.err.println("Error al cargar icono: " + path + ". Usando icono por defecto si existe.");
            return null;
        }
    }

    // Configura la ventana principal (Frame) de la aplicación POS.
    public void configurarVentana(Usuario usuario, JFrame loginFrame) {
        this.usuarioActual = usuario;
        this.loginFrame = loginFrame;

        setTitle("CAFESOFT - Punto de Venta (Usuario: " + usuario.getNombreUsuario() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 239, 230)); 

        Image icon = cargarIcon("/resources/icon.png");
        if (icon != null) {
            setIconImage(icon);
        }

        ordenTableModel = new DefaultTableModel(new String[]{"Producto", "Cant.", "Precio Unit.", "Subtotal"}, 0) {
            // Hacer las celdas no editables directamente
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordenTable = new JTable(ordenTableModel);
        ordenTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ordenTable.setRowHeight(25);
        cargarProductos();
        
        // Configuración de los paneles de la UI
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        // Contenedor principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350); // Divide el espacio

        add(splitPane, BorderLayout.CENTER);

        // Limpia al cerrar vuelve al login
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                loginFrame.setVisible(true); // O regresa al Dashboard
            }
        });
    }

    // Carga los productos desde la base de datos y actualiza el ComboBox
    private void cargarProductos() {
        List<Producto> productos = dbManager.obtenerTodosLosProductos();
        productoComboBox = new JComboBox<>();
        productosMap.clear();

        for (Producto p : productos) {
            productoComboBox.addItem(p);
            productosMap.put(p.getNombre(), p);
        }
        
        // Renderizar el nombre del producto en el ComboBox
        productoComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto) {
                    Producto p = (Producto) value;
                    setText(p.getNombre() + " ($" + String.format(Locale.US , "%.2f", p.getPrecio()) + ")");
                }
                return this;
            }
        });

        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    }

    // Crea el panel de la izquierda Selección de Producto
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 239, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de Controles (ComboBox y Botón)
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBackground(new Color(245, 239, 230));

        // Fila 1: Producto
        JPanel productRow = new JPanel(new BorderLayout(5, 5));
        productRow.add(new JLabel("Producto:"), BorderLayout.WEST);
        productRow.add(productoComboBox, BorderLayout.CENTER);
        
        // Fila 2: Cantidad y Añadir
        JPanel quantityRow = new JPanel(new BorderLayout(5, 5));
        quantityRow.add(new JLabel("Cantidad:"), BorderLayout.WEST);
        quantityRow.add(cantidadSpinner, BorderLayout.CENTER);
        
        JButton addButton = new JButton("Añadir a Orden");
        addButton.setBackground(new Color(175, 140, 107));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> agregarProducto());
        
        quantityRow.add(addButton, BorderLayout.EAST);
        
        controlPanel.add(productRow);
        controlPanel.add(quantityRow);

        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Aquí irían los botones de categoría si los hubiera

        return panel;
    }

    // Crea el panel de la derecha Carrito/Orden
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(230, 220, 210)); // Fondo ligeramente más oscuro
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del Carrito
        JLabel cartTitle = new JLabel("Orden Actual", SwingConstants.CENTER);
        cartTitle.setFont(new Font("Arial", Font.BOLD, 18));
        cartTitle.setForeground(new Color(74, 49, 39));
        panel.add(cartTitle, BorderLayout.NORTH);

        // Tabla de Orden
        JScrollPane scrollPane = new JScrollPane(ordenTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de Totales y Botones
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(new Color(230, 220, 210));

        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(new Color(74, 49, 39));
        bottomPanel.add(totalLabel, BorderLayout.NORTH);
        
        // Botones de Acción
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(new Color(230, 220, 210));
        
        JButton pagarButton = new JButton("PAGAR");
        pagarButton.setBackground(new Color(74, 49, 39));
        pagarButton.setForeground(Color.WHITE);
        pagarButton.setFont(new Font("Arial", Font.BOLD, 16));
        pagarButton.addActionListener(e -> intentarPago());
        
        JButton limpiarButton = new JButton("Limpiar Orden");
        limpiarButton.setBackground(new Color(175, 140, 107));
        limpiarButton.setForeground(Color.WHITE);
        limpiarButton.setFont(new Font("Arial", Font.BOLD, 16));
        limpiarButton.addActionListener(e -> limpiarOrden());
        
        buttonPanel.add(limpiarButton);
        buttonPanel.add(pagarButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // Agrega el producto seleccionado a la tabla de la orden.
    private void agregarProducto() {
        Producto productoSeleccionado = (Producto) productoComboBox.getSelectedItem();
        int cantidad = (Integer) cantidadSpinner.getValue();

        if (productoSeleccionado == null || cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto y una cantidad válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = productoSeleccionado.getNombre();
        double precioUnitario = productoSeleccionado.getPrecio();
        double subtotal = precioUnitario * cantidad;

        // 1. Verificar si el producto ya está en la orden para actualizar la cantidad
        boolean encontrado = false;
        for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
            if (ordenTableModel.getValueAt(i, 0).equals(nombre)) {
                // Producto encontrado, actualizar cantidad y subtotal
                int nuevaCantidad = (int) ordenTableModel.getValueAt(i, 1) + cantidad;
                double nuevoSubtotal = nuevaCantidad * precioUnitario;
                
                ordenTableModel.setValueAt(nuevaCantidad, i, 1);
                // Usar Locale.US para asegurar el formato con punto decimal para cálculos
                ordenTableModel.setValueAt(String.format(Locale.US, "$%.2f", nuevoSubtotal), i, 3);
                encontrado = true;
                break;
            }
        }

        // 2. Si no se encontró, añadir como nueva fila
        if (!encontrado) {
            ordenTableModel.addRow(new Object[]{
                nombre, 
                cantidad, 
                String.format(Locale.US, "$%.2f", precioUnitario), 
                String.format(Locale.US, "$%.2f", subtotal)
            });
        }
        
        calcularTotalOrden();
    }
    
    // Calcula el total de la orden sumando todos los subtotales.
    private void calcularTotalOrden() {
        double total = 0.0;
        for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
            // El subtotal está en la columna 3, pero necesitamos parsear el String 
            String subtotalStr = (String) ordenTableModel.getValueAt(i, 3);
            try {
                 // Eliminar el símbolo de moneda y parsear
                total += Double.parseDouble(subtotalStr.replace("$", "").replace(",", ""));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear subtotal: " + subtotalStr);
            }
        }
        // Actualizar la etiqueta del total
        totalLabel.setText("Total: $" + String.format(Locale.US, "%.2f", total));
    }
    
    // Inicia el proceso de pago.
    private void intentarPago() {
        if (ordenTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "La orden está vacía. Añade productos para continuar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extraer el total (eliminar el prefijo "Total: $")
        String totalStr = totalLabel.getText().replace("Total: $", "").replace(",", "");
        double total;
        try {
            total = Double.parseDouble(totalStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al calcular el total. Por favor, revisa la orden.", "Error Interno", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Simulación de pago y registro en la base de datos

        // Mapear la orden para facilitar el registro Producto, Cantidad
        Map<String, Integer> ordenParaDB = new HashMap<>();
        for (int i = 0; i < ordenTableModel.getRowCount(); i++) {
            String nombre = (String) ordenTableModel.getValueAt(i, 0);
            int cantidad = (int) ordenTableModel.getValueAt(i, 1);
            ordenParaDB.put(nombre, cantidad);
        }
        
        // Lista para guardar los nombres de los productos que fallaron
        List<String> ventasFallidas = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : ordenParaDB.entrySet()) {
            String nombre = entry.getKey();
            int cantidad = entry.getValue();
            Producto p = productosMap.get(nombre);
            
            // Intentar registrar en la BD
            if (p != null) {
                if (!dbManager.registrarVenta(p.getNombre(), cantidad, p.getPrecio(), usuarioActual)) { 
                    ventasFallidas.add(nombre);
                }
            }
        }
        
        // 2. Mostrar resultado y limpiar
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
    
    //Limpia la tabla del carrito y reinicia el total.
     
    private void limpiarOrden() {
        ordenTableModel.setRowCount(0);
        calcularTotalOrden();
    }

    public static void main(String[] args) {
        // Ejecutar la aplicación en el hilo de eventos de Swing
       SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            // Inicializa las dependencias
            DatabaseManager dbManager = new DatabaseManager();
            
            // Lanza la ventana del POS con el nuevo estilo CAFESOFT
            LoginFrame LoginFrame = new LoginFrame(dbManager);
            LoginFrame.setVisible(true);
            
           
        }
    });
    }
}
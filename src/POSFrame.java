import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class POSFrame extends JFrame {

    private DatabaseManager dbManager;
    
    // Componentes del "Carrito"
    private JTable carritoTable;
    private DefaultTableModel carritoModel;
    private JLabel totalLabel;
    private double totalVenta = 0.0;
    
    // Lista de Productos del Menú
    private List<Producto> menuProductos;
    private JPanel menuPanel;

    public POSFrame(DatabaseManager dbManager) {
        super("Punto de Venta (POS) - Cafetería");
        this.dbManager = dbManager;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana
        setSize(900, 650);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        // 1. Panel Principal del POS (Productos a la izquierda, Carrito a la derecha)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7); // 70% para el menú, 30% para el carrito

        // 2. Lado Izquierdo: Menú de Productos
        menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setBorder(BorderFactory.createTitledBorder("Selección de Productos"));
        
        splitPane.setLeftComponent(menuScrollPane);
        
        // 3. Lado Derecho: Carrito y Acciones
        JPanel carritoPanel = crearPanelCarrito();
        splitPane.setRightComponent(carritoPanel);

        add(splitPane, BorderLayout.CENTER);
        
        // Inicializar y cargar el menú
        cargarMenu();
    }

    private JPanel crearPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Detalle de la Venta (Carrito)"));
        String[] columnNames = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
        carritoModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; // El carrito no se edita directamente
            }
        };
        carritoTable = new JTable(carritoModel);
        
        JScrollPane scrollPane = new JScrollPane(carritoTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel para Total y Botones
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        // Etiqueta del Total
        totalLabel = new JLabel("TOTAL: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10)); // Margen
        footerPanel.add(totalLabel, BorderLayout.NORTH);
        
        // Botones de Acción
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton cobrarButton = new JButton("Cobrar Venta");
        cobrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarVenta();
            }
        });
        
        JButton limpiarButton = new JButton("Limpiar Carrito");
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCarrito();
            }
        });
        
        buttonPanel.add(limpiarButton);
        buttonPanel.add(cobrarButton);
        
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarMenu() {
        menuPanel.removeAll(); // Limpiar el panel antes de recargar
        
        menuProductos = dbManager.obtenerProductos();

        if (menuProductos.isEmpty()) {
            menuPanel.add(new JLabel("El menú está vacío. Añade productos desde 'Gestión de Menú'."));
        } else {
            for (Producto producto : menuProductos) {
                JButton productoButton = crearBotonProducto(producto);
                menuPanel.add(productoButton);
            }
        }
        
        // Actualizar la interfaz después de añadir componentes
        menuPanel.revalidate();
        menuPanel.repaint();
    }
    
    private JButton crearBotonProducto(Producto producto) {
        String buttonText = String.format("<html><center><b>%s</b><br>$%.2f</center></html>", 
                                          producto.getNombre(), producto.getPrecio());
        JButton button = new JButton(buttonText);
        
        button.setPreferredSize(new Dimension(150, 80)); // Tamaño fijo para la cuadrícula
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(new Color(230, 245, 255)); // Fondo azul claro
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2)); // Borde azul
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProductoACarrito(producto);
            }
        });
        return button;
    }
    
    private void agregarProductoACarrito(Producto producto) {
        
        int rowCount = carritoModel.getRowCount();
        boolean productoEncontrado = false;
        
        // 1. Buscar si el producto ya está en el carrito
        for (int i = 0; i < rowCount; i++) {
            String nombreCarrito = (String) carritoModel.getValueAt(i, 0);
            
            if (nombreCarrito.equals(producto.getNombre())) {
                // Producto encontrado: incrementar cantidad y actualizar subtotal
                int cantidadActual = (int) carritoModel.getValueAt(i, 1);
                int nuevaCantidad = cantidadActual + 1;
                double nuevoSubtotal = nuevaCantidad * producto.getPrecio();
                
                carritoModel.setValueAt(nuevaCantidad, i, 1); // Actualizar Cant.
                carritoModel.setValueAt(String.format("%.2f", nuevoSubtotal), i, 3); // Actualizar Subtotal
                productoEncontrado = true;
                break;
            }
        }
        
        // 2. Si no se encuentra, añadir como nueva fila
        if (!productoEncontrado) {
            double subtotal = producto.getPrecio();
            carritoModel.addRow(new Object[]{
                producto.getNombre(), 
                1, 
                String.format("%.2f", producto.getPrecio()), 
                String.format("%.2f", subtotal)
            });
        }
        
        actualizarTotalVenta();
    }

    private void actualizarTotalVenta() {
        totalVenta = 0.0;
        for (int i = 0; i < carritoModel.getRowCount(); i++) {
            String subtotalStr = (String) carritoModel.getValueAt(i, 3);
            try {
                totalVenta += Double.parseDouble(subtotalStr);
            } catch (NumberFormatException ex) {
                // Esto no debería pasar si el formato es correcto
                System.err.println("Error de formato al calcular el subtotal.");
            }
        }
        totalLabel.setText(String.format("TOTAL: $%.2f", totalVenta));
    }
    
    private void finalizarVenta() {
        if (totalVenta <= 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String mensaje = String.format("El total a cobrar es: $%.2f\n¿Desea confirmar el cobro?", totalVenta);
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            mensaje,
            "Confirmar Cobro",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            
            // Lógica de registro en BD
            boolean ventaExitosa = registrarDetalleVenta();
            
            if (ventaExitosa) {
                JOptionPane.showMessageDialog(
                    this, 
                    String.format("Venta finalizada con éxito. Total cobrado: $%.2f", totalVenta), 
                    "Venta Exitosa", 
                    JOptionPane.INFORMATION_MESSAGE
                );
                limpiarCarrito();
            } else {
                 JOptionPane.showMessageDialog(
                    this, 
                    "Error al registrar la venta en la base de datos.", 
                    "Error de Registro", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
 
    private boolean registrarDetalleVenta() {
        boolean exitoTotal = true;
        
        for (int i = 0; i < carritoModel.getRowCount(); i++) {
            String productoNombre = (String) carritoModel.getValueAt(i, 0);
            int cantidad = (int) carritoModel.getValueAt(i, 1);
            
            // Usamos el método existente en DatabaseManager
            boolean exito = dbManager.registrarVenta(productoNombre, cantidad);
            
            if (!exito) {
                exitoTotal = false; 
            }
        }
        
        return exitoTotal;
    }


    private void limpiarCarrito() {
        carritoModel.setRowCount(0);
        actualizarTotalVenta();
    }
}
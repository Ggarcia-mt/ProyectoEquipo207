import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer; 
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
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(900, 650);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        // 1. Panel Principal del POS 
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7); 

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

        // Configuración de la tabla del carrito
        String[] columnNames = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
        
        // Modelo de tablaxc 
        carritoModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; // El carrito no se edita directamente
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: return Integer.class; // Cantidad
                    case 2:
                    case 3: return Double.class; // Precio Unit. y Subtotal
                    default: return String.class;
                }
            }
        };
        carritoTable = new JTable(carritoModel);
        
        // Personalizar renderizado para que los Doubles se vean como moneda
        carritoTable.getColumnModel().getColumn(2).setCellRenderer(new CurrencyRenderer());
        carritoTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());
        
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
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5)); 
        
        JButton removerButton = new JButton("Remover Item");
        removerButton.addActionListener(e -> removerItemSeleccionado());
        
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
        
        buttonPanel.add(removerButton);
        buttonPanel.add(limpiarButton);
        buttonPanel.add(cobrarButton); // Añadir Cobrar al final
        
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    //Carga todos los productos del menú desde la BD y crea un botón por cada uno.
     
    public void cargarMenu() { 
        menuPanel.removeAll(); // Limpiar el panel antes de recargar
        
        menuProductos = dbManager.obtenerProductos();

        if (menuProductos.isEmpty()) {
            menuPanel.add(new JLabel("El menú está vacío. Añade productos desde 'Gestión de Menú'."));
        } else {
            // Usar GridLayout para una mejor presentación de botones
            menuPanel.setLayout(new GridLayout(0, 4, 10, 10)); 
            menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
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
        // HTML para formatear el texto del botón en varias líneas
        String buttonText = String.format("<html><center><b>%s</b><br>$%.2f</center></html>", 
                                          producto.getNombre(), producto.getPrecio());
        JButton button = new JButton(buttonText);
        
        button.setPreferredSize(new Dimension(150, 100)); // Ajuste el tamaño para el GridLayout
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(new Color(230, 245, 255)); // Fondo a
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2)); // Borde
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProductoACarrito(producto);
            }
        });
        return button;
    }
    
    // Agrega un producto al carrito o incrementa su cantidad.
     
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
                
                carritoModel.setValueAt(nuevaCantidad, i, 1);      
                carritoModel.setValueAt(nuevoSubtotal, i, 3);       
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
                producto.getPrecio(),             
                subtotal                          
            });
        }
        
        actualizarTotalVenta();
    }
    
    //Remueve el ítem seleccionado del carrito.
    
    private void removerItemSeleccionado() {
        int selectedRow = carritoTable.getSelectedRow();
        
        if (selectedRow >= 0) {
            carritoModel.removeRow(selectedRow);
            actualizarTotalVenta();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Seleccione una fila para remover el ítem.", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }


    // Toma el total sumando los subtotales de la tabla.
    private void actualizarTotalVenta() {
        totalVenta = 0.0;
        for (int i = 0; i < carritoModel.getRowCount(); i++) {
            // Leemos el valor como Double, gracias al cambio en el modelo
            Double subtotal = (Double) carritoModel.getValueAt(i, 3);
            if (subtotal != null) {
                totalVenta += subtotal;
            }
        }
        totalLabel.setText(String.format("TOTAL: $%.2f", totalVenta));
    }
    
    // Proceso de cobro de la venta.
     
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
                    "Error al registrar la venta en la base de datos. Verifique la consola para más detalles.", 
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
            
            double precioUnitario = (Double) carritoModel.getValueAt(i, 2);
            
            boolean exito = dbManager.registrarVenta(productoNombre, cantidad, precioUnitario);
            
            if (!exito) {
                exitoTotal = false; 
            }
        }
        
        return exitoTotal;
    }

    //Vacía el carrito y reinicia el total.
    private void limpiarCarrito() {
        carritoModel.setRowCount(0);
        actualizarTotalVenta();
    }
    

    private static class CurrencyRenderer extends DefaultTableCellRenderer { 
        // Usamos una región para obtener el formato de moneda (Colombia)
        private final java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof Double) {
                value = formatter.format((Double) value);
            }
            // Asegura que los valores de moneda estén alineados a la derecha
            setHorizontalAlignment(SwingConstants.RIGHT);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
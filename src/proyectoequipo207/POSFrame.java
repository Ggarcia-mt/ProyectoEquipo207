package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale; // Importar Locale para estandarizar el formato
import java.util.Map;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * JFrame para el Módulo de Punto de Venta (POS).
 * Muestra el menú de productos y gestiona la orden actual.
 */
public class POSFrame extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JPanel menuPanel;
    private Map<Producto, Integer> ordenActual; // Producto -> Cantidad
    
    // Constantes de Estilo
    private final Color COLOR_FONDO = new Color(245, 239, 230); // Beige claro
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39); // Café oscuro
    private final Color COLOR_ACCENT_PRODUCTO = new Color(209, 178, 140); // Tostado suave
    private final Color COLOR_HEADER = new Color(230, 220, 210); // Beige intermedio
    private final Color COLOR_EXITO = new Color(74, 49, 39); // Café oscuro para Cobrar
    private final Color COLOR_PELIGRO = new Color(244, 67, 54); // Rojo para Remover

    public POSFrame(DatabaseManager dbManager) {
        super("Punto de Venta (POS) - Cafetería");
        this.dbManager = dbManager;
        this.ordenActual = new HashMap<>();
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(0, 0)); // No gaps entre BorderLayout secciones
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        // 1. Header (Superior) - Similar a MenuManager
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Panel Principal (Dividido)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400); // Espacio para el menú
        splitPane.setDividerSize(10);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding alrededor
        splitPane.setBackground(COLOR_FONDO);

        // 2.1 Panel Izquierdo: Menú de Productos
        splitPane.setLeftComponent(createMenuPanel());
        
        // 2.2 Panel Derecho: Detalle de la Orden y Total
        splitPane.setRightComponent(createOrderPanel());

        add(splitPane, BorderLayout.CENTER);
        
        // Cargar y mostrar los productos al iniciar
        loadProductsFromDB();
    }
    
    /**
     * Creates the header panel with the logo and application title.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new LineBorder(COLOR_ACCENT_PRODUCTO, 1));
        
        // Left Side: Logo and App Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        leftPanel.setBackground(COLOR_HEADER);
        
        // Try to load the logo
        try {
            // Nota: Se asume que /proyectoequipo207/logo.png existe. 
            // Si no, se usará el fallback.
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/proyectoequipo207/logo.png"));
            Image image = logoIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(image));
            leftPanel.add(logoLabel);
        } catch (Exception e) {
            // Fallback en caso de que no se encuentre la imagen del logo
            System.err.println("No se pudo cargar el logo.png. Usando fallback. " + e.getMessage());
            JLabel fallback = new JLabel("☕");
            fallback.setFont(new Font("SansSerif", Font.PLAIN, 24));
            leftPanel.add(fallback);
        }
        
        JLabel appTitle = new JLabel("Punto de Venta (POS) - Cafetería");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        appTitle.setForeground(COLOR_PRIMARIO);
        leftPanel.add(appTitle);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    // --- 1. Panel de Menú de Productos (Izquierda) ---
    private JPanel createMenuPanel() {
        // Contenedor principal para el título y el scroll.
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding interno
        
        // Título estilizado
        JLabel title = new JLabel("MENÚ DE PRODUCTOS", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(COLOR_PRIMARIO);
        panel.add(title, BorderLayout.NORTH);

        menuPanel = new JPanel();
        // Usamos FlowLayout para que los botones fluyan como tarjetas (simulando 2 columnas)
        menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); 
        menuPanel.setBackground(COLOR_FONDO);

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave
        
        // Estilo del JScrollPane
        scrollPane.setBorder(new LineBorder(COLOR_ACCENT_PRODUCTO, 4, true)); // Borde redondeado grueso
        scrollPane.getViewport().setBackground(COLOR_FONDO);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private void loadProductsFromDB() {
        menuPanel.removeAll();
        List<Producto> productos = dbManager.obtenerProductos();

        if (productos.isEmpty()) {
            JLabel noData = new JLabel("No hay productos cargados.", SwingConstants.CENTER);
            noData.setFont(new Font("SansSerif", Font.ITALIC, 16));
            menuPanel.add(noData);
        } else {
            for (Producto p : productos) {
                menuPanel.add(createProductButton(p));
            }
        }
        
        // Refrescar la UI
        menuPanel.revalidate();
        menuPanel.repaint();
    }
    
    /**
     * Crea un botón estilizado (tile) para un producto.
     */
    private JButton createProductButton(Producto p) {
        // Usar Locale.US para asegurar que el precio se muestre con punto decimal, independientemente del sistema.
        String precioFormateado = String.format(Locale.US, "%.2f", p.getPrecio());
        
        String displayText = String.format("<html><center><b>%s</b><br><span style='font-size:1.2em;'>$%s</span></center></html>", 
                                          p.getNombre(), precioFormateado);
                                          
        JButton button = new JButton(displayText);
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        button.setBackground(Color.WHITE); // Fondo de tarjeta blanco
        button.setForeground(COLOR_PRIMARIO);
        // Borde que simula un "tile" o tarjeta
        button.setBorder(new LineBorder(COLOR_ACCENT_PRODUCTO.darker().brighter(), 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(170, 100)); // Tamaño fijo para el tile

        // Efecto hover (opcional pero recomendado en POS)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_ACCENT_PRODUCTO.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        // Acción al hacer click: añadir producto a la orden
        button.addActionListener(e -> addProductToOrder(p));
        return button;
    }

    // --- 2. Panel de Orden y Totales (Derecha) ---
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding interno
        
        // Título estilizado
        JLabel title = new JLabel("DETALLE DE LA ORDEN", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(COLOR_PRIMARIO);
        panel.add(title, BorderLayout.NORTH);

        // 2a. Tabla de Items de la Orden
        String[] columnNames = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; 
            }
        };
        JTable orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Estilo del encabezado de la tabla
        orderTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        orderTable.getTableHeader().setBackground(COLOR_ACCENT_PRODUCTO.brighter());
        orderTable.getTableHeader().setForeground(COLOR_PRIMARIO);
        orderTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(new LineBorder(COLOR_ACCENT_PRODUCTO, 4, true)); // Borde redondeado grueso
        
        panel.add(scrollPane, BorderLayout.CENTER);

        // 2b. Panel de Total y Acciones (Bottom)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(COLOR_FONDO);

        // Panel de Total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(COLOR_FONDO);
        totalLabel = new JLabel("TOTAL: $0.00");
        totalLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        totalLabel.setForeground(COLOR_PRIMARIO);
        totalPanel.add(totalLabel);
        bottomPanel.add(totalPanel, BorderLayout.NORTH);

        // Panel de Botones de Acción
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionPanel.setBackground(COLOR_FONDO);

        JButton removeItemButton = createActionButton("– REMOVER ITEM", COLOR_PELIGRO.darker(), e -> removeItemFromOrder(orderTable.getSelectedRow()));
        JButton clearCartButton = createActionButton("Ⅱ LIMPIAR CARITO", COLOR_ACCENT_PRODUCTO.darker(), e -> clearOrder());
        JButton checkoutButton = createActionButton("✔ COBRAR VENTA", COLOR_EXITO.darker(), e -> checkoutOrder());

        actionPanel.add(removeItemButton);
        actionPanel.add(clearCartButton);
        actionPanel.add(checkoutButton);
        
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JButton createActionButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        // Estilo de borde redondeado y sombra simple
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    // --- Lógica de la Orden ---
    
    /**
     * Agrega o incrementa la cantidad de un producto en la orden.
     */
    private void addProductToOrder(Producto p) {
        ordenActual.put(p, ordenActual.getOrDefault(p, 0) + 1);
        updateOrderTable();
    }
    
    /**
     * Remueve un item seleccionado de la orden.
     */
    private void removeItemFromOrder(int selectedRow) {
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un item para remover.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener el nombre del producto de la fila seleccionada
        String productName = (String) tableModel.getValueAt(selectedRow, 0);
        
        // Buscar el producto original en la ordenActual (debe tenerse cuidado con la igualdad de objetos)
        Producto productToRemove = ordenActual.keySet().stream()
            .filter(p -> p.getNombre().equals(productName))
            .findFirst()
            .orElse(null);

        if (productToRemove != null) {
            int currentQuantity = ordenActual.get(productToRemove);
            if (currentQuantity > 1) {
                ordenActual.put(productToRemove, currentQuantity - 1);
            } else {
                ordenActual.remove(productToRemove);
            }
            updateOrderTable();
        }
    }
    
    /**
     * Limpia completamente la orden.
     */
    private void clearOrder() {
        if (ordenActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito ya está vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Desea limpiar el carrito de compras?", 
            "Confirmar Limpieza", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            ordenActual.clear();
            updateOrderTable();
        }
    }

    /**
     * Actualiza la tabla con los contenidos de la ordenActual y recalcula el total.
     */
    private void updateOrderTable() {
        tableModel.setRowCount(0); // Limpiar tabla
        double grandTotal = 0.0;

        for (Map.Entry<Producto, Integer> entry : ordenActual.entrySet()) {
            Producto p = entry.getKey();
            int cantidad = entry.getValue();
            double subtotal = p.getPrecio() * cantidad;
            grandTotal += subtotal;

            // FIX: Usar Locale.US para estandarizar el punto decimal en la tabla, 
            // evitando que el sistema local (con coma) cause problemas de lectura futura.
            tableModel.addRow(new Object[]{
                p.getNombre(), 
                cantidad, 
                String.format(Locale.US, "%.2f", p.getPrecio()), 
                String.format(Locale.US, "%.2f", subtotal)
            });
        }

        // Mostrar el total usando Locale.US para que siempre use el punto decimal
        totalLabel.setText(String.format(Locale.US, "TOTAL: $%.2f", grandTotal));
    }
    
    /**
     * Proceso de cobro: registra la venta en la BD y limpia la orden.
     */
    private void checkoutOrder() {
        if (ordenActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío. Agregue productos antes de cobrar.", "Error de Venta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // FIX: Extraer el total y reemplazar la coma por un punto ANTES de parsear.
        // Esto maneja el caso donde el label aún podría contener una coma si el Locale.US fallara
        // o si se introduce manualmente una coma.
        String totalString = totalLabel.getText().replace("TOTAL: $", "");
        totalString = totalString.replace(',', '.'); // Asegurar que solo haya puntos
        
        double total;
        try {
            // Analizar el string con el punto decimal ya estandarizado
            total = Double.parseDouble(totalString);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al calcular el total. Por favor, revise los precios.", "Error Fatal de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simular el pago/cambio
        // Usamos Locale.US para mostrar el total en el prompt, asegurando el punto decimal
        String input = JOptionPane.showInputDialog(this, 
            String.format(Locale.US, "Total a pagar: $%.2f\nIngrese la cantidad recibida:", total), 
            "Procesar Pago", JOptionPane.QUESTION_MESSAGE);
        
        if (input == null || input.trim().isEmpty()) {
            return; // Cancelado
        }
        
        try {
            // FIX: También limpiar la entrada de pago si el usuario usa coma.
            String pagoString = input.trim().replace(',', '.');
            double pago = Double.parseDouble(pagoString);
            
            if (pago < total) {
                JOptionPane.showMessageDialog(this, "Pago insuficiente.", "Error de Pago", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double cambio = pago - total;
            
            // 1. Registrar cada item en la base de datos (simulada)
            for (Map.Entry<Producto, Integer> entry : ordenActual.entrySet()) {
                Producto p = entry.getKey();
                int cantidad = entry.getValue();
                dbManager.registrarVenta(p.getNombre(), cantidad, p.getPrecio());
            }

            // 2. Mostrar mensaje de éxito y cambio (Usando Locale.US para estandarizar el mensaje)
            JOptionPane.showMessageDialog(this, 
                String.format(Locale.US, "Venta Exitosa!\nTotal: $%.2f\nPago: $%.2f\nCambio: $%.2f", total, pago, cambio), 
                "Transacción Finalizada", JOptionPane.INFORMATION_MESSAGE);

            // 3. Limpiar la orden
            ordenActual.clear();
            updateOrderTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad de pago inválida. Asegúrese de usar números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }
}
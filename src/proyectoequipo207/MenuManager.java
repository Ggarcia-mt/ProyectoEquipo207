package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * JFrame para la Gestión de Inventario (Menú de Productos).
 * Permite al administrador Crear, Leer, Actualizar y Eliminar productos.
 */
public class MenuManager extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable productTable;

    // Constantes de Estilo
    private final Color COLOR_FONDO = new Color(245, 239, 230); // Beige claro
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39); // Café oscuro
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107); // Tostado suave
    private final Color COLOR_HEADER = new Color(230, 220, 210); // Beige intermedio
    private final Color COLOR_PELIGRO = new Color(244, 67, 54); // Rojo para Eliminar/Alerta
    
    public MenuManager(DatabaseManager dbManager) {
        super("CAFESOFT - Sistema de Gestión - Inventario");
        this.dbManager = dbManager;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        // 1. Header (Superior) - Estilo limpio con Logo
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Panel Central de Contenido
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(COLOR_FONDO);
        contentPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Título Principal
        JLabel titleLabel = new JLabel("Gestión de Inventario");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(COLOR_PRIMARIO);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Cuerpo: Tabla y Acciones
        JPanel bodyPanel = new JPanel(new BorderLayout(30, 0));
        bodyPanel.setBackground(COLOR_FONDO);

        // 2a. Tabla de Productos
        bodyPanel.add(createProductTable(), BorderLayout.CENTER);
        
        // 2b. Panel de Acciones (Derecha)
        bodyPanel.add(createActionPanel(), BorderLayout.EAST);
        
        contentPanel.add(bodyPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Cargar los datos iniciales
        loadProducts();
    }
    
    /**
     * Crea el panel de encabezado que contiene el logo y el título de la aplicación.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new LineBorder(COLOR_ACCENT_BOTON, 1));
        
        // Lado Izquierdo: Logo y Título de la Aplicación
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        leftPanel.setBackground(COLOR_HEADER);
        
        // Intentar cargar el logo
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/proyectoequipo207/logo.png"));
            Image image = logoIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(image));
            leftPanel.add(logoLabel);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo.png: " + e.getMessage());
            JLabel fallback = new JLabel("☕");
            fallback.setFont(new Font("SansSerif", Font.PLAIN, 24));
            leftPanel.add(fallback);
        }
        
        JLabel appTitle = new JLabel("CAFESOFT - Sistema de Gestión");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        appTitle.setForeground(COLOR_PRIMARIO);
        leftPanel.add(appTitle);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Lado Derecho: Buscador (simulado)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        rightPanel.setBackground(COLOR_HEADER);
        JTextField searchField = new JTextField("Buscar producto...", 20);
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(new LineBorder(COLOR_ACCENT_BOTON.brighter(), 1, true));
        rightPanel.add(searchField);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Crea la tabla de productos para el inventario.
     */
    private JScrollPane createProductTable() {
        String[] columnNames = {"ID", "Nombre", "Precio Venta"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; 
            }
        };
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        productTable.setRowHeight(25);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Alinear precios a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        productTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(new LineBorder(COLOR_ACCENT_BOTON, 1, true));
        return scrollPane;
    }

    /**
     * Crea el panel lateral con botones de acción (Añadir, Editar, Eliminar).
     */
    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        
        panel.add(Box.createVerticalStrut(50)); // Espacio inicial
        
        // Botones de acción
        JButton addButton = createActionButton("➕ Añadir Nuevo Producto", COLOR_ACCENT_BOTON.darker(), e -> addProduct());
        JButton editButton = createActionButton("✍ Editar Producto", COLOR_ACCENT_BOTON.darker(), e -> editProduct());
        // CORRECCIÓN: Se usa la constante de color definida correctamente (COLOR_PELIGRO)
        JButton deleteButton = createActionButton("🗑 Eliminar Producto", COLOR_PELIGRO, e -> deleteProduct());
        
        // Añadir botones
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(editButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(deleteButton);
        panel.add(Box.createVerticalGlue()); // Empuja los botones hacia arriba

        return panel;
    }
    
    private JButton createActionButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(bgColor.darker(), 1, true));
        button.addActionListener(listener);
        return button;
    }
    
    // --- Lógica de la Base de Datos ---

    private void loadProducts() {
        tableModel.setRowCount(0); // Limpiar tabla
        List<Producto> productos = dbManager.obtenerProductos();

        for (Producto p : productos) {
            tableModel.addRow(new Object[]{
                p.getId(), 
                p.getNombre(), 
                String.format("%.2f", p.getPrecio()) // Formateado para mostrar
            });
        }
    }
    
    private void addProduct() {
        // Campos de entrada
        JTextField nombreField = new JTextField();
        JTextField precioField = new JTextField();
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Nombre del Producto:"));
        inputPanel.add(nombreField);
        inputPanel.add(new JLabel("Precio de Venta:"));
        inputPanel.add(precioField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                 "Añadir Nuevo Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                double precio = Double.parseDouble(precioField.getText().trim());

                if (nombre.isEmpty() || precio <= 0) {
                    JOptionPane.showMessageDialog(this, "Nombre inválido o precio debe ser mayor que cero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Producto nuevoProducto = new Producto(0, nombre, precio); // ID 0 temporal
                dbManager.agregarProducto(nuevoProducto);
                loadProducts(); // Recargar la tabla para mostrar el nuevo producto con su ID real

                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        // Obtener el precio sin formato de la base de datos sería mejor, 
        // pero por simplicidad, lo parseamos del modelo de tabla.
        String currentPriceStr = (String) tableModel.getValueAt(selectedRow, 2); 
        
        // Campos de entrada pre-rellenados
        JTextField nombreField = new JTextField(currentName);
        JTextField precioField = new JTextField(currentPriceStr);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(new JLabel(String.valueOf(id))); // Mostrar ID pero no permitir editar
        inputPanel.add(new JLabel("Nombre del Producto:"));
        inputPanel.add(nombreField);
        inputPanel.add(new JLabel("Precio de Venta:"));
        inputPanel.add(precioField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                 "Editar Producto (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newName = nombreField.getText().trim();
                double newPrice = Double.parseDouble(precioField.getText().trim());

                if (newName.isEmpty() || newPrice <= 0) {
                    JOptionPane.showMessageDialog(this, "Nombre inválido o precio debe ser mayor que cero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Producto productoActualizado = new Producto(id, newName, newPrice);
                if (dbManager.actualizarProducto(productoActualizado)) {
                    loadProducts();
                    JOptionPane.showMessageDialog(this, "Producto actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar el producto '" + name + "' (ID: " + id + ")?", 
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.eliminarProducto(id)) {
                loadProducts();
                JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el producto de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
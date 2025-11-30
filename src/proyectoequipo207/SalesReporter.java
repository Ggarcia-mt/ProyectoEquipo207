package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * JFrame para el Módulo de Reportes de Ventas.
 * Muestra las ventas registradas en la base de datos.
 */
public class SalesReporter extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;

    // Constantes de Estilo (Consistentes con MenuManager y POSFrame)
    private final Color COLOR_FONDO = new Color(245, 239, 230); // Beige claro
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39); // Café oscuro
    private final Color COLOR_ACCENT = new Color(175, 140, 107); // Tostado suave
    private final Color COLOR_HEADER = new Color(230, 220, 210); // Beige intermedio
    private final Color COLOR_DETALLE_TOTAL = new Color(100, 65, 45); // Café más oscuro para Totales
    
    public SalesReporter(DatabaseManager dbManager) {
        super("CAFESOFT - Sistema de Gestión - Reportes de Ventas");
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
        JLabel titleLabel = new JLabel("Reportes y Estadísticas de Ventas");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(COLOR_PRIMARIO);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Cuerpo: Tabla de Reporte
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 20));
        bodyPanel.setBackground(COLOR_FONDO);

        // 2a. Tabla de Ventas
        JScrollPane scrollPane = createSalesTable();
        bodyPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 2b. Panel de Totales Resumidos
        bodyPanel.add(createSummaryPanel(), BorderLayout.SOUTH);
        
        contentPanel.add(bodyPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Cargar los datos iniciales
        loadSalesData();
    }
    
    /**
     * Creates the header panel with the logo and application title.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new LineBorder(COLOR_ACCENT, 1));
        
        // Left Side: Logo and App Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        leftPanel.setBackground(COLOR_HEADER);
        
        // Try to load the logo (assuming logo.png is accessible)
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
        
        return headerPanel;
    }
    
    /**
     * Creates the sales table component.
     */
    private JScrollPane createSalesTable() {
        String[] columnNames = {"ID Venta", "Fecha", "Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Asegurar que la columna de Cantidad y Precio sea de tipo numérico para renderizado
                if (columnIndex == 3) return Integer.class;
                if (columnIndex == 4 || columnIndex == 5) return Double.class;
                return String.class;
            }
        };
        JTable salesTable = new JTable(tableModel);
        salesTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        salesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        salesTable.setRowHeight(25);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Alinear columnas numéricas a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        salesTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cantidad
        salesTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio Unit
        salesTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Subtotal

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(new LineBorder(COLOR_ACCENT, 1, true));
        return scrollPane;
    }

    /**
     * Creates the summary panel showing total sales.
     */
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        summaryPanel.setBackground(COLOR_HEADER.brighter()); // Fondo contrastante para el resumen
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel totalLabel = new JLabel("Total de Ventas Registradas: Calculando...");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(COLOR_DETALLE_TOTAL);
        totalLabel.setName("totalSalesLabel"); // Para fácil referencia al actualizar
        
        summaryPanel.add(totalLabel);
        return summaryPanel;
    }
    
    // --- Lógica de la Base de Datos ---

    private void loadSalesData() {
        tableModel.setRowCount(0); // Limpiar tabla
        List<Venta> ventas = dbManager.obtenerVentas();
        double grandTotal = 0.0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Venta v : ventas) {
            double subtotal = v.getCantidad() * v.getPrecioUnitario();
            grandTotal += subtotal;
            
            tableModel.addRow(new Object[]{
                v.getIdVenta(), 
                dateFormat.format(v.getFechaVenta()), 
                v.getNombreProducto(), 
                v.getCantidad(),
                v.getPrecioUnitario(), // Usar Double directamente
                subtotal // Usar Double directamente
            });
        }
        
        // Actualizar el resumen
        updateSummary(ventas.size(), grandTotal);
    }
    
    private void updateSummary(int totalItems, double grandTotal) {
        JLabel totalLabel = (JLabel) findComponentByName(this.getContentPane(), "totalSalesLabel");
        if (totalLabel != null) {
             totalLabel.setText(String.format("Total de Ventas Registradas (%d items): $%.2f", totalItems, grandTotal));
        }
    }
    
    /**
     * Helper method to find a component by name recursively.
     */
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}

class Venta {
    private int idVenta;
    private Date fechaVenta;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;

    // Constructor
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
}
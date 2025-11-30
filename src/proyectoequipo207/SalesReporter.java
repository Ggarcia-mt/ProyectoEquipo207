package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * JFrame para el Módulo de Reportes de Ventas.
 * Muestra el historial completo de ventas en una tabla.
 */
public class SalesReporter extends JFrame {

    private DatabaseManager dbManager;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    
    // Constantes de Estilo
    private final Color COLOR_FONDO = new Color(245, 239, 230); // Beige claro
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39); // Café oscuro
    private final Color COLOR_ACCENT = new Color(209, 178, 140); // Tostado suave
    private final Color COLOR_HEADER = new Color(230, 220, 210); // Beige intermedio

    public SalesReporter(DatabaseManager dbManager) {
        super("Reporte de Ventas - Cafetería");
        this.dbManager = dbManager;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(10, 10)); 
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        
        // 1. Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Panel Central de la Tabla
        add(createSalesTablePanel(), BorderLayout.CENTER);

        // 3. Panel de Resumen (Bottom)
        add(createSummaryPanel(), BorderLayout.SOUTH);
        
        // Cargar datos al iniciar
        loadSalesData();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new LineBorder(COLOR_ACCENT, 1));
        
        JLabel title = new JLabel("HISTORIAL DE VENTAS Y ESTADÍSTICAS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(COLOR_PRIMARIO);
        headerPanel.add(title);
        
        return headerPanel;
    }
    
    private JPanel createSalesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(COLOR_FONDO);

        String[] columnNames = {"ID Venta", "Fecha", "Producto", "Cantidad", "Precio Unitario", "Total Item"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false; 
            }
        };
        
        salesTable = new JTable(tableModel);
        salesTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        salesTable.setRowHeight(25);
        
        // Estilo del encabezado
        salesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        salesTable.getTableHeader().setBackground(COLOR_ACCENT.brighter());
        salesTable.getTableHeader().setForeground(COLOR_PRIMARIO);
        
        // Renderizador para alinear números a la derecha
        TableCellRenderer rightRenderer = salesTable.getTableHeader().getDefaultRenderer();
        salesTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        salesTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(new LineBorder(COLOR_ACCENT, 4, true));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(0, 10, 10, 10));

        summaryLabel = new JLabel("Total de Ventas: $0.00 | Items Vendidos: 0");
        summaryLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        summaryLabel.setForeground(COLOR_PRIMARIO);
        panel.add(summaryLabel);
        
        return panel;
    }

    /**
     * Carga los datos de ventas desde el DatabaseManager y actualiza la tabla.
     */
    private void loadSalesData() {
        tableModel.setRowCount(0); // Limpiar tabla
        List<Venta> ventas = dbManager.obtenerVentas();
        double grandTotal = 0.0;
        int totalItems = 0;
        
        // Formato para la fecha
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        if (ventas != null) {
            for (Venta v : ventas) {
                double totalItem = v.getCantidad() * v.getPrecioUnitario();
                grandTotal += totalItem;
                totalItems += v.getCantidad();
                
                // Usamos Locale.US para estandarizar el formato numérico con punto decimal
                tableModel.addRow(new Object[]{
                    v.getIdVenta(),
                    dateFormat.format(v.getFechaVenta()),
                    v.getNombreProducto(),
                    v.getCantidad(),
                    String.format(Locale.US, "%.2f", v.getPrecioUnitario()),
                    String.format(Locale.US, "%.2f", totalItem)
                });
            }
        }
        
        // Actualizar el resumen
        summaryLabel.setText(String.format(Locale.US, 
            "Total de Ventas: $%.2f | Items Vendidos: %d", 
            grandTotal, 
            totalItems));
    }
}
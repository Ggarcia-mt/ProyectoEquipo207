package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
public class SalesReporter extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JLabel lblTotalVentas;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO")); // Formato de moneda

    // Colores basados en el Dashboard
    private final Color COLOR_FONDO = new Color(245, 239, 230);
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39);
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107);

    public SalesReporter(DatabaseManager dbManager) {
        super("CAFESOFT - Reportes de Ventas y Cierre");
        this.dbManager = dbManager;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        initComponents();
        loadSalesData();
    }

    private void initComponents() {
        // Título Superior
        JLabel titleLabel = new JLabel("Historial de Transacciones", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(COLOR_PRIMARIO);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        //Panel de Tabla (Centro) 
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Panel de Reporte y Totales (Sur) 
        JPanel reportPanel = createReportPanel();
        add(reportPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        // Definición de las columnas de la tabla de ventas
        String[] columnNames = {"ID Venta", "Fecha", "Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3) return Integer.class; // ID, Cantidad
                if (column == 4 || column == 5) return Double.class; // Precios, Subtotal
                return String.class;
            }
        };
        JTable salesTable = new JTable(tableModel);
        salesTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        salesTable.setRowHeight(25);
        salesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(COLOR_FONDO);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARIO.darker());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Subpanel para los totales a la izquierda
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        totalPanel.setOpaque(false);
        
        lblTotalVentas = new JLabel("Total de Ventas: " + currencyFormatter.format(0.00));
        lblTotalVentas.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTotalVentas.setForeground(Color.WHITE);
        totalPanel.add(lblTotalVentas);
        
        panel.add(totalPanel, BorderLayout.CENTER);

        // Botón de Cierre de Caja a la derecha
        JButton btnCierre = createStyledButton("Generar Reporte de Cierre");
        btnCierre.addActionListener(e -> generateClosingReport());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(btnCierre);
        
        panel.add(buttonWrapper, BorderLayout.EAST);
        
        return panel;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(COLOR_ACCENT_BOTON);
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    //  Lógica de Negocio 

    private void loadSalesData() {
        tableModel.setRowCount(0);
        List<Venta> ventas = dbManager.obtenerVentas();
        double totalGlobal = 0.0;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Ordenar ventas por ID de forma descendente 
        ventas.sort(Comparator.comparing(Venta::getIdVenta).reversed());

        for (Venta v : ventas) {
            double subtotal = v.getCantidad() * v.getPrecioUnitario();
            totalGlobal += subtotal;
            
            tableModel.addRow(new Object[]{
                v.getIdVenta(),
                dateFormat.format(v.getFechaVenta()),
                v.getNombreProducto(),
                v.getCantidad(),
                v.getPrecioUnitario(),
                subtotal
            });
        }
        
        lblTotalVentas.setText("Total de Ventas Global: " + currencyFormatter.format(totalGlobal));
    }
    
    // Aqui se genera un reporte de cierre de caja (simple) y lo muestra en un diálog.
     
    private void generateClosingReport() {
        List<Venta> ventas = dbManager.obtenerVentas();
        
        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay ventas registradas para generar un reporte.", 
                "Reporte Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 1. Cálculos de Totales y Productos Vendidos
        double totalRecaudado = 0.0;
        int totalItemsVendidos = 0;
        String productoMasVendido = "";
        int maxCantidad = 0;
        
        for (Venta v : ventas) {
            double subtotal = v.getCantidad() * v.getPrecioUnitario();
            totalRecaudado += subtotal;
            totalItemsVendidos += v.getCantidad();
            
            if (v.getCantidad() > maxCantidad) {
                maxCantidad = v.getCantidad();
                productoMasVendido = v.getNombreProducto();
            }
            
            // Lógica simple para simular el producto más vendido, solo se queda con el item de mayor cantidad en una sola transacción.
        }
        
        String inicioOperaciones = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(ventas.get(0).getFechaVenta());
        String fechaCierre = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
        
        // 2. Construcción del Mensaje del Reporte
        String reporte = "<html><body style='font-family:SansSerif; padding: 10px;'>"
                + "<h2 style='color:#4A3127;'>☕ Reporte de Cierre de Caja</h2>"
                + "<hr style='border: 1px solid #A88775;'>"
                + "<h3>Resumen Financiero</h3>"
                + "<table border='0' cellpadding='5' cellspacing='0' width='100%'>"
                + "<tr><td>Total de Transacciones:</td><td align='right'><b>" + ventas.size() + " líneas</b></td></tr>"
                + "<tr><td>Total de Ítems Vendidos:</td><td align='right'><b>" + totalItemsVendidos + " unidades</b></td></tr>"
                + "<tr><td>Producto con Mayor Venta Individual:</td><td align='right'><b>" + productoMasVendido + "</b></td></tr>"
                + "</table>"
                + "<h3>Total Recaudado</h3>"
                + "<div style='font-size: 36px; color: #15803d; text-align: center; background-color: #e0f2f1; padding: 15px; border-radius: 8px; margin-bottom: 20px;'>"
                + "<b>" + currencyFormatter.format(totalRecaudado) + "</b>"
                + "</div>"
                + "<h3>Periodo del Reporte</h3>"
                + "Inicio de Operaciones (Primer registro): <b>" + inicioOperaciones + "</b><br>"
                + "Fecha y Hora de Cierre: <b>" + fechaCierre + "</b>"
                + "<hr style='border: 1px solid #A88775; margin-top: 20px;'>"
                + "<p style='font-size: 12px; color: #666;'>Reporte generado por CAFESOFT</p>"
                + "</body></html>";
        
        // 3. Mostrar el reporte
        JOptionPane.showMessageDialog(this, 
            reporte, 
            "Reporte de Cierre de Caja", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
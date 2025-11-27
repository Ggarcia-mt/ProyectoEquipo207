import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SalesReporter extends JDialog {
    
    private DatabaseManager dbManager;
    private JTable ventasTable;
    private JLabel totalIngresosLabel; 

    public SalesReporter(Frame owner, DatabaseManager dbManager) {
        super(owner, "Registro de Ventas e Ingresos", true); // Modal
        this.dbManager = dbManager;
        
        // fuente 
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 14));
        
        setSize(800, 600); 
        setLayout(new BorderLayout(15, 15)); // Espaciado entre componentes
        setLocationRelativeTo(owner);
        
        // Etiqueta de Título
        JLabel titleLabel = new JLabel("💰 Historial Detallado de Transacciones 📊", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 1. Inicializar la tabla y cargar los datos
        inicializarTabla();
        
        // 2. Panel con la tabla (JScrollPane)
        JScrollPane scrollPane = new JScrollPane(ventasTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 0, 15), 
            BorderFactory.createLineBorder(Color.GRAY)
        ));
        add(scrollPane, BorderLayout.CENTER);
        
        // 3. Panel Inferior para Total y Botón de Cerrar
        JPanel footerPanel = crearPanelInferior();
        add(footerPanel, BorderLayout.SOUTH);

        // Actualizar el resumen financiero al cargar el diálogo
        actualizarResumenFinanciero(); 
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Etiqueta de Ingresos Totales
        totalIngresosLabel = new JLabel("Calculando Ingresos...", SwingConstants.RIGHT);
        totalIngresosLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalIngresosLabel.setForeground(new Color(0, 128, 0)); // Color verde para ingresos
        panel.add(totalIngresosLabel, BorderLayout.NORTH);
        
        // Botón para cerrar
        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(e -> dispose()); 
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(cerrarButton);
        panel.add(buttonWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private void inicializarTabla() {
        // La tabla carga el modelo de datos corregido, que usa precio_venta
        DefaultTableModel model = dbManager.obtenerRegistrosDetalladosDeVentas();
        ventasTable = new JTable(model);
        
        // Estilo de la tabla
        ventasTable.setFont(new Font("Arial", Font.PLAIN, 13));
        ventasTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        ventasTable.setRowHeight(25);
        ventasTable.setFillsViewportHeight(true);
        
        // Ajustar el ancho de las columnas (índices 0 a 5)
        if (ventasTable.getColumnModel().getColumnCount() > 5) {
            ventasTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID Venta
            ventasTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Producto
            ventasTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Cant.
            ventasTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Precio Unit.
            ventasTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Subtotal
            ventasTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Fecha/Hora
        }
    }
    
    private void actualizarResumenFinanciero() {
        double total = dbManager.obtenerIngresoTotal(); // Usa el método corregido
        totalIngresosLabel.setText(
            String.format("INGRESO TOTAL ACUMULADO: $%.2f", total)
        );
    }
}
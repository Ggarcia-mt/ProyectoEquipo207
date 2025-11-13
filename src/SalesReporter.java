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
        super(owner, "Registro de Ventas", true);
        this.dbManager = dbManager;
        
        setSize(800, 600);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);
        
        // Etiqueta de Título
        JLabel titleLabel = new JLabel("Historial Detallado de Transacciones", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        //  Inicializa la tabla y carga los datos
        inicializarTabla();
        
        //  Panel con la tabla (JScrollPane)
        JScrollPane scrollPane = new JScrollPane(ventasTable);
        add(scrollPane, BorderLayout.CENTER);
        
        //  Panel Inferior para Total y Botón de Cerrar
        JPanel footerPanel = crearPanelInferior();
        add(footerPanel, BorderLayout.SOUTH);

        // Empaqueta y hace visible
        pack();
        // Asegura de que el cálculo de totales se haga antes de mostrar
        actualizarResumenFinanciero(); 
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Etiqueta de Ingresos Totales
        totalIngresosLabel = new JLabel("Calculando Ingresos...", SwingConstants.RIGHT);
        totalIngresosLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(totalIngresosLabel, BorderLayout.NORTH);
        
        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); 
            }
        });
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(cerrarButton);
        panel.add(buttonWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private void inicializarTabla() {
        // Pedir al DatabaseManager el modelo de datos detallado
        DefaultTableModel model = dbManager.obtenerRegistrosDetalladosDeVentas();
        ventasTable = new JTable(model);
        
        // Configuración de la tabla
        ventasTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ventasTable.setRowHeight(22);
        ventasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ajustar el ancho de las columnas
        if (ventasTable.getColumnModel().getColumnCount() > 5) {
            ventasTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            ventasTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Producto
            ventasTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Cant.
            ventasTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Precio Unit.
            ventasTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Subtotal
            ventasTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Fecha/Hora
        }
    }
    
  
    private void actualizarResumenFinanciero() {
        double total = dbManager.obtenerIngresoTotal();
        totalIngresosLabel.setText(
            String.format("INGRESO TOTAL ACUMULADO: $%.2f", total)
        );
    }
}
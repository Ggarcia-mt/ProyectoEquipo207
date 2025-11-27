import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class MenuManager extends JDialog {
    
    private DatabaseManager dbManager;
    private JTable menuTable;
    private DefaultTableModel tableModel;

    public MenuManager(Frame owner, DatabaseManager dbManager) {
        super(owner, "Gestión de Menú", true); 
        this.dbManager = dbManager;
        
        setSize(700, 500);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);
        
        // Título
        JLabel titleLabel = new JLabel("Administración de Productos", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);
        
        // Inicializar la tabla y el modelo
        String[] columnNames = {"ID", "Nombre", "Precio"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class;
                return String.class;
            }
            // Hacer que todas las celdas sean no editables
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        menuTable = new JTable(tableModel);
        
        // Cargar datos iniciales y configurar la vista
        cargarProductos();
        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones y acciones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton addButton = new JButton("➕ Agregar Producto");
        addButton.addActionListener(e -> mostrarDialogoAgregar());
        
        JButton deleteButton = new JButton("➖ Eliminar Seleccionado");
        deleteButton.addActionListener(e -> eliminarProductoSeleccionado());
        
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Estilo
        menuTable.setFont(new Font("Arial", Font.PLAIN, 14));
        menuTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        menuTable.setRowHeight(25);
    }
    
    // Carga todos los productos desde la base de datos y actualiza la tabla.
     
    private void cargarProductos() {
        tableModel.setRowCount(0); // Limpiar tabla
        List<Producto> productos = dbManager.obtenerProductos();
        for (Producto p : productos) {
            tableModel.addRow(new Object[]{p.getId(), p.getNombre(), p.getPrecio()});
        }
    }
    private void mostrarDialogoAgregar() {
        JTextField nombreField = new JTextField(15);
        JTextField precioField = new JTextField(10);
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre del Producto:"));
        panel.add(nombreField);
        panel.add(new JLabel("Precio (ej. 5.99):"));
        panel.add(precioField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Agregar Nuevo Producto", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText().trim();
                double precio = Double.parseDouble(precioField.getText().trim());
                
                if (nombre.isEmpty() || precio <= 0) {
                    JOptionPane.showMessageDialog(this, "Por favor, ingrese un nombre y un precio válido.", 
                            "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Producto nuevoProducto = new Producto(nombre, precio);
                int newId = dbManager.insertarProducto(nuevoProducto);
                
                if (newId != -1) {
                    nuevoProducto.setId(newId);
                    cargarProductos(); // Recargar la tabla
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", 
                        "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Elimina el producto seleccionado de la tabla y de la base de datos.
     
    private void eliminarProductoSeleccionado() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", 
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener el ID de la primera columna
        int productoId = (int) tableModel.getValueAt(selectedRow, 0);
        String nombre = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar el producto '" + nombre + "'?", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.eliminarProducto(productoId)) {
                JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarProductos(); // Recargar la tabla
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el producto de la base de datos.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
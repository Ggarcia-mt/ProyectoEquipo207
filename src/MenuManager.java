import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MenuManager extends JDialog {
    
    private DatabaseManager dbManager;
    private JTable productosTable;
    private DefaultTableModel tableModel;

    private JTextField nombreField;
    private JTextField precioField;

    public MenuManager(Frame owner, DatabaseManager dbManager) {
        super(owner, "Gestión de Menú", true); // 'true' hace que sea modal
        this.dbManager = dbManager;
        
        setSize(600, 450);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);

        // 1. Inicializar componentes de la tabla
        inicializarTabla();
        add(new JScrollPane(productosTable), BorderLayout.CENTER);

        // 2. Inicializar formulario de adición
        add(crearPanelFormulario(), BorderLayout.NORTH);

        // 3. Inicializar panel de botones de acción
        add(crearPanelAcciones(), BorderLayout.SOUTH);

        // Cargar datos iniciales al abrir la ventana
        cargarProductos();
    }
    

    private void inicializarTabla() {
        String[] columnNames = {"ID", "Nombre", "Precio"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        productosTable = new JTable(tableModel);
        productosTable.getColumnModel().getColumn(0).setMinWidth(0);
        productosTable.getColumnModel().getColumn(0).setMaxWidth(0);
        productosTable.getColumnModel().getColumn(0).setWidth(0);
    }
    
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Añadir Nuevo Producto"));

        panel.add(new JLabel("Nombre del Producto:"));
        nombreField = new JTextField();
        panel.add(nombreField);

        panel.add(new JLabel("Precio ($):"));
        precioField = new JTextField();
        panel.add(precioField);

        return panel;
    }
    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton agregarButton = new JButton("Añadir Producto");
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProducto();
            }
        });
        JButton eliminarButton = new JButton("Eliminar Seleccionado"); 
        
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarProducto();
            }
        });

        panel.add(agregarButton);
        panel.add(eliminarButton);

        return panel;
    }
    private void cargarProductos() {
        // Limpiar tabla antes de cargar
        tableModel.setRowCount(0);
        
        List<Producto> productos = dbManager.obtenerProductos();

        if (productos != null) {
            for (Producto p : productos) {
                // Añadir fila: ID, Nombre, Precio
                tableModel.addRow(new Object[]{p.getId(), p.getNombre(), String.format("%.2f", p.getPrecio())});
            }
        }
    }
    private void agregarProducto() {
        String nombre = nombreField.getText().trim();
        String precioStr = precioField.getText().trim();
        
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            if (precio < 0) {
                 JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Error de Precio", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            Producto nuevoProducto = new Producto(nombre, precio);
            // Intentar insertar en la BD
            int idGenerado = dbManager.insertarProducto(nuevoProducto);

            if (idGenerado > 0) {
                // Éxito: Limpiar campos y recargar tabla
                nombreField.setText("");
                precioField.setText("");
                
                // Añadir directamente a la tabla para evitar recargar toda la BD
                tableModel.addRow(new Object[]{idGenerado, nuevoProducto.getNombre(), String.format("%.2f", nuevoProducto.getPrecio())});
                
                JOptionPane.showMessageDialog(this, "Producto añadido con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo añadir el producto. Podría ser un nombre duplicado.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void eliminarProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productoId = (int) tableModel.getValueAt(selectedRow, 0);
        String nombre = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que quieres eliminar el producto: " + nombre + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (dbManager.eliminarProducto(productoId)) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Producto eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al intentar eliminar el producto de la base de datos.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
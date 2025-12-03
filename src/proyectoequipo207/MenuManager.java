package proyectoequipo207;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class MenuManager extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable productTable;
    
    // Columna MODIFICADA: Ahora solo incluye ID, Nombre y Precio Venta.
    private final String[] NEW_COLUMN_NAMES = {"ID", "Nombre", "Precio Venta"};
    
    // Colores basados en el Dashboard (Alineación de Marca)
    private final Color COLOR_FONDO_CLARO = new Color(245, 239, 230); 
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39);       
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107); 
    
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public MenuManager(DatabaseManager dbManager) {
        super("CAFESOFT - Sistema de Gestión - Menú de Productos");
        this.dbManager = dbManager;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700); // Tamaño más grande para acomodar el nuevo diseño
        setLayout(new BorderLayout(0, 0)); 
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO_CLARO);

        initComponents();
        loadProducts();
    }

    private void initComponents() {
        // Panel Superior 
        //JPanel headerPanel = createHeaderPanel();
        //add(headerPanel, BorderLayout.NORTH);

        // Panel Central Contenido Principal: Título, Tabla y Barra de Acciones
        JPanel centralPanel = new JPanel(new BorderLayout(30, 0));
        centralPanel.setBackground(COLOR_FONDO_CLARO);
        centralPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 
        
        // Contenedor para el título y la tabla ocupa la mayor parte del espacio
        JPanel contentArea = new JPanel(new BorderLayout(0, 20));
        contentArea.setOpaque(false);
        
        // Título "Gestión de Inventario" y Barra de Búsqueda
        JPanel titleAndSearch = createTitleAndSearchPanel();
        contentArea.add(titleAndSearch, BorderLayout.NORTH);

        // Tabla de Productos
        JPanel tablePanel = createTablePanel();
        contentArea.add(tablePanel, BorderLayout.CENTER);

        centralPanel.add(contentArea, BorderLayout.CENTER);
        
        // Barra de Botones de Acción 
        JPanel actionButtonPanel = createActionButtonPanel();
        centralPanel.add(actionButtonPanel, BorderLayout.EAST);
        
        add(centralPanel, BorderLayout.CENTER);

        //  Footer 
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO_CLARO.darker()); 
        panel.setPreferredSize(new Dimension(1, 50)); 
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_ACCENT_BOTON));

        JLabel logo = new JLabel("CAFESOFT - Sistema de Gestión - Menú de Productos", SwingConstants.CENTER);
        logo.setFont(new Font("Serif", Font.BOLD, 20));
        logo.setForeground(COLOR_PRIMARIO);
        panel.add(logo, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(COLOR_PRIMARIO);
        panel.setPreferredSize(new Dimension(1, 30));
        
        JLabel footerLabel = new JLabel("Conectado a la base de datos");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(footerLabel);
        
        return panel;
    }
    
    private JPanel createTitleAndSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(50, 0));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("Gestión de Menú");
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(COLOR_PRIMARIO);
        panel.add(title, BorderLayout.WEST);
        
        // Barra de Búsqueda 
        JTextField searchField = new JTextField("Buscar producto...");
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setFont(new Font("SansSerif", Font.ITALIC, 14));
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACCENT_BOTON, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchField);
        panel.add(searchWrapper, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createTablePanel() {
        tableModel = new DefaultTableModel(NEW_COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class; 
                if (column == 2) return Double.class; 
                return String.class;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        productTable.setRowHeight(35);
        
        // 1. Estilo de encabezado de tabla
        productTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        productTable.getTableHeader().setBackground(COLOR_ACCENT_BOTON);
        productTable.getTableHeader().setForeground(Color.WHITE);
        productTable.getTableHeader().setPreferredSize(new Dimension(productTable.getTableHeader().getWidth(), 40));
        
        // 2. Comportamiento y Estilo
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setGridColor(COLOR_ACCENT_BOTON.brighter());
        productTable.setIntercellSpacing(new Dimension(1, 1)); // Espacio entre celdas
        
        // 3. Renderer para Precios
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        productTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer); 
        
        // 4. Listener para cargar los datos al seleccionar una fila (para Editar/Eliminar)
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editProduct(); // Doble click para editar
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARIO, 1, true));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(COLOR_FONDO_CLARO);
        return panel;
    }

    private JPanel createActionButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO_CLARO);
        panel.setPreferredSize(new Dimension(220, 1)); // Ancho fijo
        
        panel.add(Box.createVerticalStrut(50)); // Espacio superior

        // Botones con iconos
        JButton btnAdd = createIconStyledButton("Añadir Nuevo Producto", "➕", e -> addProduct());
        JButton btnUpdate = createIconStyledButton("Editar Producto", "✏️", e -> editProduct());
        JButton btnDelete = createIconStyledButton("Eliminar Producto", "🗑️", e -> deleteProduct());

        panel.add(btnAdd);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnUpdate);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnDelete);
        
        panel.add(Box.createVerticalGlue()); // Empuja los botones hacia arriba

        return panel;
    }
    
    private JButton createIconStyledButton(String text, String icon, ActionListener listener) {
        JButton button = new JButton("<html>" + icon + " &nbsp; " + text + "</html>");
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(COLOR_PRIMARIO); // Usamos el marrón oscuro para que resalte
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Para BoxLayout
        button.setMaximumSize(new Dimension(200, 50));
        button.addActionListener(listener);
        return button;
    }

    // Lógica de Negocio 

    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Producto> productos = dbManager.obtenerProductos();
        
        // Se elimina la lógica de categorías mock
        for (Producto p : productos) {
            
            // Columna MODIFICADA: Se elimina la categoría (índice 1)
            tableModel.addRow(new Object[]{
                p.getId(), 
                p.getNombre(), 
                p.getPrecio()
            });
        }
    }


    private void addProduct() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo producto:", "Añadir Producto", JOptionPane.QUESTION_MESSAGE);
        
        if (nombre == null || nombre.trim().isEmpty()) {
            if (nombre != null) {
                 JOptionPane.showMessageDialog(this, "El nombre del producto no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        
        String precioStr = JOptionPane.showInputDialog(this, "Ingrese el precio de " + nombre + " (" + currencyFormatter.getCurrency().getSymbol() + "):", "Añadir Producto", JOptionPane.QUESTION_MESSAGE);

        if (precioStr == null) {
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr.replace(',', '.'));
            
            if (precio <= 0) {
                 JOptionPane.showMessageDialog(this, "El precio debe ser un valor positivo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Producto nuevoProducto = new Producto(0, nombre, precio);
            dbManager.agregarProducto(nuevoProducto);
            loadProducts();
            JOptionPane.showMessageDialog(this, "Producto agregado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String currentName = (String) tableModel.getValueAt(selectedRow, 1); 
            double currentPrice = (double) tableModel.getValueAt(selectedRow, 2); 

            String newName = (String) JOptionPane.showInputDialog(this, "Nuevo nombre para el producto ID " + id + ":", "Editar Nombre", JOptionPane.QUESTION_MESSAGE, null, null, currentName);
            
            if (newName == null) return; 

            if (newName.trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            // Usamos un formato estándar con Locale.US punto decimal para el valor por defecto
            String defaultPriceFormat = String.format(Locale.US, "%.2f", currentPrice); 
            String newPriceStr = (String) JOptionPane.showInputDialog(this, 
                "<html>Ingrese el nuevo precio para <b>" + newName + "</b>:<br><i>(Use el punto '.' como separador decimal)</i></html>", 
                "Editar Precio", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                null, 
                defaultPriceFormat
            );
            
            if (newPriceStr == null) return; 
            
            // Sanitizamos el string para asegurar que solo tenga un punto decimal antes de parsear
            newPriceStr = newPriceStr.trim().replace(',', '.');
            
            double newPrice = Double.parseDouble(newPriceStr);

            if (newPrice <= 0) {
                 JOptionPane.showMessageDialog(this, "El precio debe ser un valor positivo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto productoActualizado = new Producto(id, newName, newPrice);
            if (dbManager.actualizarProducto(productoActualizado)) {
                loadProducts();
                JOptionPane.showMessageDialog(this, "Producto actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar. ¿El producto existe?", "Error de DB", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            // Error específico para el formato del número 
            JOptionPane.showMessageDialog(this, 
                "Error de Formato: Por favor, asegúrate de ingresar solo números y usar el punto '.' como separador decimal.", 
                "Error de Validación de Precio", JOptionPane.ERROR_MESSAGE);
        } catch (ClassCastException e) {
            // Error si los datos de la tabla no son del tipo esperado (
            JOptionPane.showMessageDialog(this, "Error interno al leer los datos de la tabla. Verifica el tipo de dato en la columna.", "Error Interno", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Imprimir el stack trace para depuración
        } catch (Exception e) {
            // Cualquier otro error inesperado 
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado durante la edición: " + e.getMessage(), "Error Desconocido", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Imprimir el stack trace
        }
    }

    // Maneja la eliminación de un producto.
     
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String nombre = (String) tableModel.getValueAt(selectedRow, 1); 

            int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿Estás seguro de que deseas eliminar el producto '" + nombre + "' (ID: " + id + ")?", 
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dbManager.eliminarProducto(id)) {
                    loadProducts();
                    JOptionPane.showMessageDialog(this, "Producto eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar el producto de la base de datos.", "Error de DB", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Error al leer datos de la tabla. Inténtalo de nuevo.", "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }
}
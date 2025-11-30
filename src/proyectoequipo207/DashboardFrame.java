package proyectoequipo207;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal (Dashboard) que actúa como menú de navegación.
 * Implementa Control de Acceso Basado en Roles (RBAC).
 */
public class DashboardFrame extends JFrame {

    private DatabaseManager dbManager;
    private String userRole;
    private final Color COLOR_FONDO = new Color(245, 239, 230);
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39);
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107); 

    public DashboardFrame(DatabaseManager dbManager, String userRole) {
        super("CAFESOFT - Sistema de Gestión");
        this.dbManager = dbManager;
        this.userRole = userRole; // Almacenar el rol del usuario
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        // --- Logo y Título ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_FONDO);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // Título principal
        JLabel titleLabel = new JLabel("CAFESOFT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48)); 
        titleLabel.setForeground(COLOR_PRIMARIO);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        // Subtítulo
        JLabel sloganLabel = new JLabel("EL SAZÓN DE LA U", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
        sloganLabel.setForeground(COLOR_ACCENT_BOTON.darker());
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(sloganLabel);

        add(headerPanel, BorderLayout.NORTH);

        // --- Panel de Botones de Navegación ---
        JPanel buttonGridPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        buttonGridPanel.setBackground(COLOR_FONDO);
        buttonGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        // Lista de botones a mostrar
        List<JButton> buttonsToShow = new ArrayList<>();
        
        // 1. Botón de Ventas (POS) - Visible para AMBOS roles
        buttonsToShow.add(createDashboardButton("VENTAS (POS)", "🛒", e -> openPOSFrame()));
        
        // 2. Botón de Inventario - Visible SOLO para ADMIN
        if ("ADMIN".equals(userRole)) {
            buttonsToShow.add(createDashboardButton("INVENTARIO", "📦", e -> openMenuManager()));
        }

        // 3. Botón de Reportes - Visible SOLO para ADMIN
        if ("ADMIN".equals(userRole)) {
            buttonsToShow.add(createDashboardButton("REPORTES", "📈", e -> openSalesReporter()));
        }

        // 4. Botón de Administración (Siempre visible para ADMIN, o simplemente como placeholder)
        if ("ADMIN".equals(userRole)) {
            buttonsToShow.add(createDashboardButton("ADMINISTRADOR", "⚙️", e -> JOptionPane.showMessageDialog(this, "Módulo de Administración no implementado.", "Info", JOptionPane.INFORMATION_MESSAGE)));
        } else {
             // Rellenar espacios si solo hay 1 botón (POS) para mantener el formato de grilla 2x2
             for (int i = 0; i < 3; i++) { 
                buttonsToShow.add(new JPanel()); // Panel vacío como separador
             }
        }
        
        // Agregar solo los botones permitidos a la grilla
        for (JButton button : buttonsToShow) {
            buttonGridPanel.add(button);
        }
        
        add(buttonGridPanel, BorderLayout.CENTER);

        // --- Footer (Información de Usuario) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(COLOR_PRIMARIO.darker());
        JLabel userLabel = new JLabel("Rol: " + userRole + " | Fecha: " + java.time.LocalDate.now());
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        footerPanel.add(userLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JButton createDashboardButton(String text, String icon, ActionListener listener) {
        JButton button = new JButton("<html><center>" + icon + "<br>" + text + "</center></html>");
        
        button.setPreferredSize(new Dimension(150, 150));
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setBackground(COLOR_ACCENT_BOTON);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_PRIMARIO, 2, true), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10) 
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }
    
    // Métodos para abrir ventanas (mismos que antes)
    private void openPOSFrame() {
        this.setVisible(false);
        POSFrame posFrame = new POSFrame(dbManager);
        posFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                setVisible(true);
            }
        });
        posFrame.setVisible(true);
    }

    private void openMenuManager() {
        this.setVisible(false);
        MenuManager menuManager = new MenuManager(dbManager);
        menuManager.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                setVisible(true);
            }
        });
        menuManager.setVisible(true);
    }

    private void openSalesReporter() {
        this.setVisible(false);
        SalesReporter salesReporter = new SalesReporter(dbManager);
        salesReporter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                setVisible(true);
            }
        });
        salesReporter.setVisible(true);
    }
}
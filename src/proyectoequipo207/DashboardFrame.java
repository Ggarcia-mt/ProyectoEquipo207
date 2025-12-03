package proyectoequipo207;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DashboardFrame extends JFrame {

    private DatabaseManager dbManager;
    private Usuario usuario; 
    
    // Constantes de Estilo
    private final Color COLOR_FONDO = new Color(245, 239, 230);        // Fondo
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39);        // Marrón oscuro Texto, Bordes, Header
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107);  // Marrón claro Botones/Cards
    private final Color COLOR_CARD_FONDO = Color.WHITE;               // Fondo de las tarjetas 
    private final Font FONT_TITULO_PRINCIPAL = new Font("Arial", Font.BOLD, 36);
    private final Font FONT_TARJETA_TITULO = new Font("Arial", Font.BOLD, 22);
    private final Font FONT_TARJETA_DESC = new Font("Arial", Font.PLAIN, 14);

    public DashboardFrame(DatabaseManager dbManager, Usuario usuario) {
        super("CAFESOFT - Dashboard");
        this.dbManager = dbManager;
        this.usuario = usuario; 
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); 
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        initComponents();
        
        validate();
    }
    
    private void initComponents() {
        // 1. Panel Superior Header con Logo, Bienvenida y Logout
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_PRIMARIO);
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Título del Sistema Logo de texto en el encabezado
        JLabel titleLabel = new JLabel("CAFESOFT");
        titleLabel.setFont(FONT_TITULO_PRINCIPAL); 
        titleLabel.setForeground(COLOR_FONDO);
        headerPanel.add(titleLabel, BorderLayout.WEST); 

        // Información de Usuario y Botón de Logout 
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userArea.setOpaque(false);

        JLabel welcomeLabel = new JLabel("<html><div style='text-align: right;'><b>" + usuario.getNombreUsuario() + "</b><br><span style='color: #A88775;'>Rol: " + usuario.getRol() + "</span></div></html>");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.WHITE);
        userArea.add(welcomeLabel);

        JButton logoutButton = new JButton("Cerrar Sesión");
        styleButton(logoutButton, COLOR_ACCENT_BOTON, COLOR_PRIMARIO);
        logoutButton.addActionListener(e -> {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame(dbManager);
            loginFrame.setVisible(true);
        });
        userArea.add(logoutButton);
        
        headerPanel.add(userArea, BorderLayout.EAST);
        
        //2. Panel Central Logo/Icono y Botones
       
        JPanel centerWrapper = new JPanel();
        // Usamos BoxLayout para apilar verticalmente y centrar
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS)); 
        centerWrapper.setBackground(COLOR_FONDO);
        centerWrapper.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        
        // Simulación más elaborada de un logo.png con Unicode y HTML
        JLabel visualLogo = new JLabel("<html><div style='text-align: center; color: " + toHtmlColor(COLOR_PRIMARIO) + ";'>" 
                                    + "<span style='font-size: 120px; font-weight: bold;'>&#9749;</span><br>" // Taza de café grande
                                    + "<span style='font-size: 50px; font-weight: bold;'>CAFESOFT</span>"
                                    + "</div></html>", SwingConstants.CENTER);
        visualLogo.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        // B. Contenedor de Botones
        JPanel buttonContainer = new JPanel(new GridLayout(1, 3, 30, 0)); 
        buttonContainer.setBackground(COLOR_FONDO);
        buttonContainer.setAlignmentX(Component.CENTER_ALIGNMENT); 
        buttonContainer.setMaximumSize(new Dimension(800, 100)); // Limitar ancho

        // Botón 1: POS siempre disponible
        JButton posButton = new JButton("PUNTO DE VENTA (POS)");
        styleButton(posButton, COLOR_ACCENT_BOTON, Color.WHITE);
        posButton.addActionListener(e -> openPOS());
        buttonContainer.add(posButton);
        
        // Botón 2: Gestión de Menú 
        JButton menuButton = new JButton("GESTIÓN DE PRODUCTOS");
        if (usuario.esAdmin()) {
            styleButton(menuButton, COLOR_ACCENT_BOTON, Color.WHITE);
            menuButton.addActionListener(e -> openMenuManager());
        } else {
            styleButton(menuButton, Color.LIGHT_GRAY, Color.DARK_GRAY);
            menuButton.setEnabled(false);
            menuButton.setText("GESTIÓN DE PRODUCTOS (Solo Admin)");
        }
        buttonContainer.add(menuButton);
        
        // Botón 3: Reportes de Ventas 
        JButton reportButton = new JButton("REPORTES Y CIERRES");
        if (usuario.esAdmin()) {
            styleButton(reportButton, COLOR_ACCENT_BOTON, Color.WHITE);
            reportButton.addActionListener(e -> openSalesReporter());
        } else {
            styleButton(reportButton, Color.LIGHT_GRAY, Color.DARK_GRAY);
            reportButton.setEnabled(false);
            reportButton.setText("REPORTES Y CIERRES (Solo Admin)");
        }
        buttonContainer.add(reportButton);

        // Añadir los componentes centrales: Logo, espacio, Botones
        centerWrapper.add(Box.createVerticalGlue()); // Espacio flexible arriba
        centerWrapper.add(visualLogo);
        centerWrapper.add(Box.createRigidArea(new Dimension(0, 50))); // Espacio fijo entre logo y botones
        centerWrapper.add(buttonContainer);
        centerWrapper.add(Box.createVerticalGlue()); // Espacio flexible abajo
        

        //3. Ensamblar Frame 
        add(headerPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
        
        // Pie de página
        JLabel footerLabel = new JLabel("© 2025 CAFESOFT | Desarrollado por Equipo 207", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        footerLabel.setForeground(COLOR_PRIMARIO.brighter());
        add(footerLabel, BorderLayout.SOUTH);
    }
    
    private String toHtmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    // Helper para estilizar botones simples 
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_TARJETA_TITULO.deriveFont(Font.BOLD, 16f));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(background.darker(), 1), 
            new EmptyBorder(15, 20, 15, 20) // Aumento de padding para que se vean más como botones principales
        ));
    }


    //Métodos de Apertura de Ventanas Sin cambios en la lógica
    
    private void openPOS() {
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
        if (!usuario.esAdmin()) {
            JOptionPane.showMessageDialog(this, "Acceso denegado. Se requiere rol de Administrador.", "Permisos", JOptionPane.WARNING_MESSAGE);
            return;
        }
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
        if (!usuario.esAdmin()) {
            JOptionPane.showMessageDialog(this, "Acceso denegado. Se requiere rol de Administrador.", "Permisos", JOptionPane.WARNING_MESSAGE);
            return;
        }
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
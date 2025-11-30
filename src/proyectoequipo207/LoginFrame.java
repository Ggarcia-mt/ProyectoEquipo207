package proyectoequipo207;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;

/**
 * Ventana de inicio de sesión que simula la autenticación y asigna un rol.
 */
public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private DatabaseManager dbManager;

    private final Color COLOR_FONDO = new Color(245, 239, 230);
    private final Color COLOR_PRIMARIO = new Color(74, 49, 39);
    private final Color COLOR_ACCENT_BOTON = new Color(175, 140, 107); 

    public LoginFrame(DatabaseManager dbManager) {
        super("CAFESOFT - Inicio de Sesión");
        this.dbManager = dbManager;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        // --- Panel Principal y Logo ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Título y Slogan
        JLabel titleLabel = new JLabel("CAFESOFT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40)); 
        titleLabel.setForeground(COLOR_PRIMARIO);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        JLabel sloganLabel = new JLabel("Sistema de Punto de Venta", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        sloganLabel.setForeground(COLOR_ACCENT_BOTON.darker());
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(sloganLabel);
        
        mainPanel.add(Box.createVerticalStrut(30)); 

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(COLOR_FONDO);
        
        // Campo Usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userField = new JTextField(15);
        userField.setText("admin"); // Valor por defecto para prueba
        
        formPanel.add(userLabel);
        formPanel.add(userField);

        // Campo Contraseña
        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        passField = new JPasswordField(15);
        passField.setText("123"); // Valor por defecto para prueba
        
        formPanel.add(passLabel);
        formPanel.add(passField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(30)); 

        // Botón de Login
        JButton loginButton = new JButton("INICIAR SESIÓN");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setBackground(COLOR_PRIMARIO);
        loginButton.setForeground(Color.WHITE);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBorder(new LineBorder(COLOR_ACCENT_BOTON, 2, true));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
        
        // Permite presionar ENTER para iniciar sesión
        passField.addActionListener(e -> attemptLogin());

        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalGlue()); // Empuja todo hacia arriba
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void attemptLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());
        String role = null;

        // Simulación de autenticación y asignación de rol
        if ("admin".equals(username) && "123".equals(password)) {
            role = "ADMIN";
        } else if ("vendedor".equals(username) && "456".equals(password)) {
            role = "VENDEDOR";
        }

        if (role != null) {
            JOptionPane.showMessageDialog(this, "Bienvenido, " + username + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            // Abrir Dashboard y pasar el rol
            DashboardFrame dashboard = new DashboardFrame(dbManager, role);
            dashboard.setVisible(true);
            this.dispose(); // Cierra la ventana de Login
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas. Inténtalo de nuevo.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }
}
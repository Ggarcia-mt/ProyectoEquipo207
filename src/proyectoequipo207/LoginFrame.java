package proyectoequipo207;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;
 
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

        // Panel Principal contenedor, centrado
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Título y Slogan
        JLabel titleLabel = new JLabel("CAFESOFT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36)); 
        titleLabel.setForeground(COLOR_PRIMARIO);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel sloganLabel = new JLabel("Bienvenido", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sloganLabel.setForeground(COLOR_ACCENT_BOTON.darker());
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sloganLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0)); 

        mainPanel.add(titleLabel);
        mainPanel.add(sloganLabel);
        
        // Panel para los Campos de Texto con GridBagLayout para la alineación 
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_FONDO);
        formPanel.setMaximumSize(new Dimension(300, 150)); // Controla el ancho del formulario
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 1. Etiqueta Usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setForeground(COLOR_PRIMARIO);
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.3; 
        formPanel.add(userLabel, gbc);
        
        // 2. Campo de Texto Usuario
        userField = new JTextField();
        userField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1; 
        gbc.gridy = 0; 
        gbc.weightx = 0.7; 
        formPanel.add(userField, gbc);
        userField.setText("admin");
        
        // 3. Etiqueta Contraseña
        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setForeground(COLOR_PRIMARIO);
        gbc.gridx = 0; 
        gbc.gridy = 1; 
        gbc.weightx = 0.3;
        formPanel.add(passLabel, gbc);
        
        // 4. Campo de Texto Contraseña
        passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1; 
        gbc.gridy = 1; 
        gbc.weightx = 0.7;
        formPanel.add(passField, gbc);
        passField.setText("123");

        // Agregar el panel de formulario centrado
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30))); 

        // Botón Login
        JButton loginButton = new JButton("INGRESAR");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(COLOR_PRIMARIO);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
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
        mainPanel.add(Box.createVerticalGlue()); 
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void attemptLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese usuario y contraseña.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Usamos el DatabaseManager para la autenticación
        Usuario usuarioAutenticado = dbManager.autenticarUsuario(username, password);

        if (usuarioAutenticado != null) {
            JOptionPane.showMessageDialog(this, "Bienvenido, " + usuarioAutenticado.getNombreUsuario() + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Abrir Dashboard
            DashboardFrame dashboard = new DashboardFrame(dbManager, usuarioAutenticado);
            dashboard.setVisible(true);
            
            this.dispose(); // Cierra la ventana de Login
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas. Inténtalo de nuevo.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            passField.setText(""); // Limpia contraseña
        }
    }
}
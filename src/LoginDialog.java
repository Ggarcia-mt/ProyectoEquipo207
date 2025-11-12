import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginDialog extends JDialog {

    private DatabaseManager dbManager;
    private Usuario usuarioAutenticado = null;
    
    private JTextField usuarioField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginDialog(JFrame owner, DatabaseManager dbManager) {
        super(owner, "Iniciar Sesión", true); // Modal: true
        this.dbManager = dbManager;
        
        setSize(400, 200);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // No permitir cerrar sin iniciar sesión
        
        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Usuario:"));
        usuarioField = new JTextField(15);
        formPanel.add(usuarioField);

        formPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField);

        add(formPanel, BorderLayout.CENTER);
        
        // --- Panel de Botón ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intentarLogin();
            }
        });
        
        // Añadir Listener para Enter en campos de texto
        ActionListener enterListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intentarLogin();
            }
        };
        usuarioField.addActionListener(enterListener);
        passwordField.addActionListener(enterListener);
        
        buttonPanel.add(loginButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Sugerencia de usuarios por defecto
        usuarioField.setText("vendedor");
        passwordField.setText("venta123");
        
        // --- Mejora de visibilidad ---
        this.setAlwaysOnTop(true);
        
        pack(); // Ajustar el tamaño al contenido
    }
    
    /**
     * Intenta autenticar al usuario usando el DatabaseManager.
     */
    private void intentarLogin() {
        String usuario = usuarioField.getText().trim();
        // Obtener la contraseña como String (es un array de chars, se convierte a String)
        String password = new String(passwordField.getPassword()); 

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce usuario y contraseña.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            return;
        }

        usuarioAutenticado = dbManager.autenticarUsuario(usuario, password);

        if (usuarioAutenticado != null) {
            // Éxito: cerrar el diálogo
            this.dispose(); 
        } else {
            // Fallo: Limpiar contraseña y mostrar mensaje
            passwordField.setText("");
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Login", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
   ADMIN (Gestión de Menú): admin / admin123
   VENDEDOR (Solo POS): vendedor / venta123
     */
    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
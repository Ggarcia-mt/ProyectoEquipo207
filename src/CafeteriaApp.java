import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Esta es la clase principal de la aplicación de gestión de la cafetería.
 * Utiliza Java Swing para la interfaz de usuario.
 */
public class CafeteriaApp {

    // Componentes principales de la UI
    private JFrame ventanaPrincipal;
    private JTextArea areaDeTextoLog;
    private JButton botonRegistrarVenta;
    private JButton botonGestionMenu;
    private JButton botonPuntoVenta;
    
    private JPanel panelPrincipal;

    // Instancia del gestor de base de datos
    private DatabaseManager dbManager;
    
    // --- Nuevo: Usuario Autenticado ---
    private Usuario usuarioActual; 

    /**
     * Constructor. Aquí se inicializa la interfaz gráfica.
     */
    public CafeteriaApp() {
        // --- Configuración de la Ventana Principal (JFrame) ---
        ventanaPrincipal = new JFrame("Gestión de Cafetería");
        ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaPrincipal.setSize(700, 500); // Aumentamos el tamaño
        ventanaPrincipal.setLocationRelativeTo(null); // Centrar la ventana

        // --- Inicialización de BD (Se mantiene la lógica) ---
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de SQLite.");
             JOptionPane.showMessageDialog(
                null, 
                "Error crítico: Driver JDBC de SQLite no encontrado.\n"
                + "Asegúrate de que el archivo .jar esté en el Classpath.", 
                "Error de Driver", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1); 
        }
        
        dbManager = new DatabaseManager();
        dbManager.inicializarBaseDeDatos();
        
        // **La UI se construye DESPUÉS del login exitoso**
    }
    
    /**
     * Construye y muestra la interfaz principal después de una autenticación exitosa.
     */
    private void construirUI() {
        
        // --- Mostrar nombre de usuario y rol en la ventana ---
        String titulo = String.format("Gestión de Cafetería - Usuario: %s (%s)", 
                                      usuarioActual.getNombreUsuario(), usuarioActual.getRol());
        ventanaPrincipal.setTitle(titulo);
        
        // --- Creación del Panel Principal ---
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout(10, 10)); // Layout con espaciado
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen
        
        // --- Panel de Botones Superiores ---
        JPanel panelBotonesSuperiores = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        // --- Botón 1: Punto de Venta (POS) --- 
        botonPuntoVenta = new JButton("Abrir Punto de Venta (POS)");
        botonPuntoVenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirPuntoDeVenta();
            }
        });
        panelBotonesSuperiores.add(botonPuntoVenta);
        
        // --- Botón 2: Gestión de Menú ---
        botonGestionMenu = new JButton("Gestión de Menú (Productos)");
        botonGestionMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirGestorDeMenu();
            }
        });
        
        // Control de acceso: solo ADMIN puede acceder a la gestión de menú
        if (usuarioActual.esAdmin()) {
            panelBotonesSuperiores.add(botonGestionMenu);
        } else {
            // El botón no se añade al panel para el rol de VENDEDOR
            botonGestionMenu.setEnabled(false); 
        }
        
        // --- Botón 3: Registrar Venta (Demo) ---
        botonRegistrarVenta = new JButton("Registrar Venta Rápida (Demo)");
        botonRegistrarVenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarVentaConDialogos();
            }
        });
        panelBotonesSuperiores.add(botonRegistrarVenta);
        
        panelPrincipal.add(panelBotonesSuperiores, BorderLayout.NORTH); // Colocamos los botones arriba

        // --- Área de Texto para mostrar un log ---
        areaDeTextoLog = new JTextArea();
        areaDeTextoLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaDeTextoLog); // Agregarle scroll
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Añadir el panel principal a la ventana
        ventanaPrincipal.add(panelPrincipal);

        areaDeTextoLog.append("Sistema listo. Base de datos conectada.\n");
        areaDeTextoLog.append("¡Bienvenido, " + usuarioActual.getNombreUsuario() + "!\n");
        
        // Mostrar la ventana principal
        ventanaPrincipal.setVisible(true);
    }

    /**
     * Inicia el proceso de login. Si es exitoso, construye la UI.
     */
    public void iniciar() {
        // Crear un JDialog de Login usando un Frame temporal como dueño
        LoginDialog loginDialog = new LoginDialog(ventanaPrincipal, dbManager);
        
        // Mostrar el diálogo de login y esperar a que se cierre
        loginDialog.setVisible(true); // <--- Esta llamada bloquea la ejecución hasta el login

        // Una vez que el diálogo se cierra (después de un login exitoso o fallo)
        usuarioActual = loginDialog.getUsuarioAutenticado();

        if (usuarioActual != null) {
            // Login exitoso: construir la interfaz principal
            construirUI();
        } else {
            // Login fallido o ventana cerrada, salir de la aplicación
            JOptionPane.showMessageDialog(null, 
                "Debe iniciar sesión para usar la aplicación. Saliendo...", 
                "Acceso Denegado", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    /**
     * Abre la ventana de gestión de menú.
     */
    private void abrirGestorDeMenu() {
        // Se pasa la ventana principal (JFrame) como 'owner' y el gestor de BD.
        MenuManager menuDialog = new MenuManager(ventanaPrincipal, dbManager);
        menuDialog.setVisible(true);
    }
    
    /**
     * Abre la ventana del Punto de Venta (POS).
     */
    private void abrirPuntoDeVenta() {
        POSFrame posFrame = new POSFrame(dbManager);
        posFrame.setVisible(true);
    }

    /**
     * Este método demuestra el uso de varios tipos de JOptionPane
     * para simular el registro de una venta. 
     */
    private void registrarVentaConDialogos() {
        
        // 1. Pedir un dato (Input Dialog)
        String producto = JOptionPane.showInputDialog(
                ventanaPrincipal, 
                "¿Qué producto desea registrar?", 
                "Registrar Venta", 
                JOptionPane.QUESTION_MESSAGE 
        );

        if (producto == null || producto.trim().isEmpty()) {
            areaDeTextoLog.append("Venta rápida cancelada.\n");
            return; 
        }

        // --- Modificado: Pedir cantidad ---
        int cantidad = 1; 
        try {
            String cantidadStr = JOptionPane.showInputDialog(
                ventanaPrincipal,
                "¿Qué cantidad de \"" + producto + "\"?",
                "Registrar Venta",
                JOptionPane.QUESTION_MESSAGE
            );
            if (cantidadStr == null) { 
                 areaDeTextoLog.append("Venta rápida cancelada.\n");
                 return;
            }
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                ventanaPrincipal, 
                "Cantidad no válida. Se asumirá 1.", 
                "Error de formato", 
                JOptionPane.WARNING_MESSAGE);
            cantidad = 1;
        }


        // 2. Pedir confirmación (Confirm Dialog)
        int confirmacion = JOptionPane.showConfirmDialog(
                ventanaPrincipal,
                "¿Seguro que desea registrar: " + cantidad + " de \"" + producto + "\"?",
                "Confirmar Venta",
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            
            boolean exito = dbManager.registrarVenta(producto, cantidad);

            if (exito) {
                // 3. Mostrar un mensaje (Message Dialog)
                JOptionPane.showMessageDialog(
                        ventanaPrincipal,
                        "Venta de " + cantidad + " de '" + producto + "' registrada con éxito.",
                        "Venta Exitosa",
                        JOptionPane.INFORMATION_MESSAGE 
                );

                areaDeTextoLog.append("VENTA REGISTRADA: " + cantidad + "x " + producto + "\n");
            } else {
                areaDeTextoLog.append("FALLO AL REGISTRAR VENTA: " + producto + "\n");
            }


        } else {
            areaDeTextoLog.append("Venta rápida cancelada por el usuario: " + producto + "\n");
        }
    }

    /**
     * Método Main.
     * Es el punto de entrada de la aplicación.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CafeteriaApp app = new CafeteriaApp();
                app.iniciar(); // Inicia el proceso de login
            }
        });
    }
}
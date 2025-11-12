
public class Usuario {
    private String nombreUsuario;
    private String rol; // Ej: "ADMIN", "VENDEDOR"

    public Usuario(String nombreUsuario, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    // Getters
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    /**
     * Comprueba si el usuario tiene rol de Administrador.
     */
    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol);
    }
}
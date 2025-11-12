
public class Usuario {
    private String nombreUsuario;
    private String rol;

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

    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol);
    }
}
package proyectoequipo207;

import java.util.Date;

public class Venta {
    private int idVenta; 
    private Date fechaVenta;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;

    public Venta(int idVenta, Date fechaVenta, String nombreProducto, int cantidad, double precioUnitario) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters
    public int getIdVenta() { return idVenta; }
    public Date getFechaVenta() { return fechaVenta; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    //se pusiero setters por si acaso 
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public void setFechaVenta(Date fechaVenta) { this.fechaVenta = fechaVenta; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
}
package cr.ac.una.model;

/**
 * Sección del parqueo destinada a un único tipo de vehículo,
 * con una cantidad máxima de espacios (capacidad).
 */
public class Zona {

    private String codigo;
    private String nombre;
    private TipoVehiculo tipoVehiculo;
    private int capacidad;

    public Zona() {
    }

    public Zona(String codigo, String nombre, TipoVehiculo tipoVehiculo, int capacidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoVehiculo = tipoVehiculo;
        this.capacidad = capacidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-26s | %-11s | capacidad: %d",
                codigo, nombre, tipoVehiculo == null ? "-" : tipoVehiculo.getDescripcion(), capacidad);
    }
}

package cr.ac.una.model;

/**
 * Vehículo autorizado para usar el parqueo institucional.
 * La placa es su identificador único.
 */
public class Vehiculo {

    private String placa;
    private TipoVehiculo tipo;
    private String marca;
    private String color;
    private String propietario;
    private CategoriaVehiculo categoria;

    public Vehiculo() {
    }

    public Vehiculo(String placa, TipoVehiculo tipo, String marca, String color, String propietario,  CategoriaVehiculo categoria) {
        this.placa = placa;
        this.tipo = tipo;
        this.marca = marca;
        this.color = color;
        this.propietario = propietario;
        this.categoria = categoria;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }
    
    public CategoriaVehiculo getCategoria() {
        return categoria;
}

    public void setCategoria(CategoriaVehiculo categoria) {
        this.categoria = categoria;
}

    @Override
    public String toString() {
        return String.format(
            "%-10s | %-11s | %-12s | %-10s | %-12s | %s",
            placa,
            tipo == null ? "-" : tipo.getDescripcion(),
            marca,
            color,
            categoria == null ? "-" : categoria.name(),
            propietario
         );
    }
}

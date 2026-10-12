package cr.ac.una.model;

/**
 * Tipos de vehículo que admite el parqueo. Cada zona acepta un único tipo
 * (RN-03), por eso tanto Vehiculo como Zona usan esta enumeración.
 */
public enum TipoVehiculo {
    AUTOMOVIL("Automóvil"),
    MOTOCICLETA("Motocicleta");

    private final String descripcion;

    TipoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

package cr.ac.una.model;

/**
 * Resultado de la consulta de ocupación: una zona junto con la cantidad de
 * vehículos que están dentro de ella en este momento.
 */
public class OcupacionZona {

    private Zona zona;
    private int ocupados;

    public OcupacionZona() {
    }

    public OcupacionZona(Zona zona, int ocupados) {
        this.zona = zona;
        this.ocupados = ocupados;
    }

    public int getLibres() {
        return Math.max(0, zona.getCapacidad() - ocupados);
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public int getOcupados() {
        return ocupados;
    }

    public void setOcupados(int ocupados) {
        this.ocupados = ocupados;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-26s | %-11s | ocupados: %3d | libres: %3d | capacidad: %3d",
                zona.getCodigo(), zona.getNombre(), zona.getTipoVehiculo().getDescripcion(),
                ocupados, getLibres(), zona.getCapacidad());
    }
}

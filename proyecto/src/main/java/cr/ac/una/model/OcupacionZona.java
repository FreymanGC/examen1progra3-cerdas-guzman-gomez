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
    
        /**
    * Espacios disponibles para vehículos PARTICULARES y VISITANTES.
    * Los últimos dos espacios se reservan para OFICIALES.
    */
    public int getLibresGenerales() {
        return Math.max(0, zona.getCapacidad() - 2 - ocupados);
    }

    /**
    * Espacios libres del bloque reservado para vehículos oficiales.
    */
    public int getReservadosDisponibles() {
        return Math.min(2, getLibres());
    }

    /**
    * Espacios disponibles para vehículos OFICIALES.
    * Pueden utilizar toda la capacidad libre de la zona.
    */
    public int getLibresParaOficiales() {
        return getLibres();
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
        return String.format(
            "%-6s | %-26s | %-11s | "
            + "ocupados: %3d | capacidad: %3d | "
            + "libres: %3d | generales: %3d | "
            + "reservados: %3d | disponibles oficiales: %3d",
            zona.getCodigo(),
            zona.getNombre(),
            zona.getTipoVehiculo().getDescripcion(),
            ocupados,
            zona.getCapacidad(),
            getLibres(),
            getLibresGenerales(),
            getReservadosDisponibles(),
            getLibresParaOficiales()
            );
        }
}

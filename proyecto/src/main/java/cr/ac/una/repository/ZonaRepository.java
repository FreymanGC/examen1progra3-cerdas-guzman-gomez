package cr.ac.una.repository;

import cr.ac.una.model.OcupacionZona;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Zona;
import java.util.List;
import java.util.Optional;

/** Operaciones de persistencia de las zonas del parqueo. */
public interface ZonaRepository {

    void insertar(Zona zona);

    Optional<Zona> buscarPorCodigo(String codigo);

    List<Zona> listar();

    List<Zona> listarPorTipo(TipoVehiculo tipo);

    void actualizar(Zona zona);

    /** Cada zona con la cantidad de vehículos que tiene dentro (JOIN + COUNT). */
    List<OcupacionZona> listarOcupacion();
}

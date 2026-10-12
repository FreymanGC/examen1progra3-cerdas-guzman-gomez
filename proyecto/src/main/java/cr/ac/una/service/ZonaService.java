package cr.ac.una.service;

import cr.ac.una.model.OcupacionZona;
import cr.ac.una.model.Zona;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.ZonaRepository;
import java.util.List;

/**
 * Validaciones y reglas sobre las zonas del parqueo.
 * Etapa 1: solo estructura (atributos, constructor y firmas).
 */
public class ZonaService {

    private final ZonaRepository zonaRepository;
    private final IngresoRepository ingresoRepository;

    public ZonaService(ZonaRepository zonaRepository, IngresoRepository ingresoRepository) {
        this.zonaRepository = zonaRepository;
        this.ingresoRepository = ingresoRepository;
    }

    public Zona registrar(Zona zona) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Zona buscarPorCodigo(String codigo) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public List<Zona> listar() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    /** RN-06: la capacidad no puede ser menor que los vehículos que ya están dentro. */
    public Zona actualizarCapacidad(String codigo, int nuevaCapacidad) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    /** Espacios ocupados y libres por zona. */
    public List<OcupacionZona> consultarOcupacion() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

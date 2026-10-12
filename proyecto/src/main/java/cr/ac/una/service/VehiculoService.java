package cr.ac.una.service;

import cr.ac.una.model.Vehiculo;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.VehiculoRepository;
import java.util.List;

/**
 * Validaciones y reglas sobre los vehículos autorizados.
 * Depende de las interfaces de repository (no de las clases JDBC).
 * Etapa 1: solo estructura (atributos, constructor y firmas).
 */
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final IngresoRepository ingresoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, IngresoRepository ingresoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.ingresoRepository = ingresoRepository;
    }

    /** Valida los datos y que la placa no esté registrada. */
    public Vehiculo registrar(Vehiculo vehiculo) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Vehiculo buscarPorPlaca(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public List<Vehiculo> listar() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    /** RN-05: no se puede cambiar el tipo de un vehículo que está dentro. */
    public Vehiculo actualizar(Vehiculo cambios) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

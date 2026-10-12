package cr.ac.una.service;

import cr.ac.una.model.Ingreso;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.VehiculoRepository;
import cr.ac.una.repository.ZonaRepository;
import java.util.List;

/**
 * Operación de negocio principal del caso: registrar el ingreso de un
 * vehículo (RN-01, RN-02, RN-03) y registrar su salida (RN-04).
 * Etapa 1: solo estructura (atributos, constructor y firmas).
 */
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ZonaRepository zonaRepository;

    public IngresoService(IngresoRepository ingresoRepository,
                          VehiculoRepository vehiculoRepository,
                          ZonaRepository zonaRepository) {
        this.ingresoRepository = ingresoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.zonaRepository = zonaRepository;
    }

    /**
     * 1. Verificar que el vehículo esté registrado.
     * 2. RN-02: verificar que no tenga un ingreso abierto.
     * 3. RN-03: seleccionar una zona del tipo del vehículo.
     * 4. RN-01: verificar que la zona tenga espacio.
     * 5. Insertar el ingreso con la fecha y hora actual.
     */
    public Ingreso registrarIngreso(String placa, String codigoZona) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    /** RN-04: solo se registra la salida de un vehículo que esté dentro. */
    public Ingreso registrarSalida(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public List<Ingreso> listarVehiculosDentro() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public List<Ingreso> historialPorPlaca(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

package cr.ac.una.service;

import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ParqueoLlenoException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.VehiculoYaIngresadoException;
import cr.ac.una.model.Ingreso;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.model.Zona;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.VehiculoRepository;
import cr.ac.una.repository.ZonaRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Operación de negocio principal del caso: registrar el ingreso de un
 * vehículo (RN-01, RN-02, RN-03) y registrar su salida (RN-04).
 */
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ZonaRepository zonaRepository;
    /** Reloj inyectable: permite fijar la hora en las pruebas (Etapa 3). */
    private final Clock reloj;

    public IngresoService(IngresoRepository ingresoRepository,
                          VehiculoRepository vehiculoRepository,
                          ZonaRepository zonaRepository) {
        this(ingresoRepository, vehiculoRepository, zonaRepository, Clock.systemDefaultZone());
    }

    public IngresoService(IngresoRepository ingresoRepository,
                          VehiculoRepository vehiculoRepository,
                          ZonaRepository zonaRepository,
                          Clock reloj) {
        this.ingresoRepository = ingresoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.zonaRepository = zonaRepository;
        this.reloj = reloj;
    }

    /**
     * Registra el ingreso de un vehículo.
     *
     * @param placa      placa del vehículo
     * @param codigoZona zona elegida por el oficial; si viene vacía, el sistema
     *                   asigna la primera zona del tipo del vehículo con espacio
     */
    public Ingreso registrarIngreso(String placa, String codigoZona) {
        // 1. Validar datos y verificar que el vehículo esté registrado.
        String p = VehiculoService.normalizarPlaca(placa);
        Vehiculo vehiculo = vehiculoRepository.buscarPorPlaca(p)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "El vehículo " + p + " no está registrado como vehículo autorizado."));

        // 2. RN-02: el vehículo no puede tener un ingreso abierto.
        if (ingresoRepository.buscarAbiertoPorPlaca(p).isPresent()) {
            throw new VehiculoYaIngresadoException(p);
        }

        // 3 y 4. Seleccionar zona del mismo tipo (RN-03) con espacio libre (RN-01).
        Zona zona = (codigoZona == null || codigoZona.isBlank())
                ? asignarZonaAutomatica(vehiculo)
                : validarZonaElegida(vehiculo, codigoZona);

        // 5. Insertar el ingreso con la fecha y hora actual.
        Ingreso ingreso = new Ingreso(vehiculo, zona, ahora());
        ingresoRepository.insertar(ingreso);
        return ingreso;
    }

    /**
     * Registra la salida de un vehículo.
     * RN-04: solo puede salir un vehículo que esté dentro.
     * Devuelve el ingreso cerrado para poder mostrar el tiempo de permanencia.
     */
    public Ingreso registrarSalida(String placa) {
        String p = VehiculoService.normalizarPlaca(placa);
        if (vehiculoRepository.buscarPorPlaca(p).isEmpty()) {
            throw new EntidadNoEncontradaException("El vehículo " + p + " no está registrado.");
        }
        Ingreso ingreso = ingresoRepository.buscarAbiertoPorPlaca(p)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se puede registrar la salida: el vehículo " + p + " no está dentro del parqueo."));
        LocalDateTime salida = ahora();
        if (salida.isBefore(ingreso.getFechaHoraEntrada())) {
            throw new ReglaNegocioException("La hora de salida (" + salida + ") no puede ser anterior a la de entrada ("
                    + ingreso.getFechaHoraEntrada() + "). Revise la zona horaria del equipo y del servidor MySQL.");
        }
        ingresoRepository.registrarSalida(ingreso.getId(), salida);
        ingreso.setFechaHoraSalida(salida);
        return ingreso;
    }

    /** Vehículos que se encuentran dentro del parqueo en este momento. */
    public List<Ingreso> listarVehiculosDentro() {
        return ingresoRepository.listarAbiertos();
    }

    /** Historial de ingresos y salidas de un vehículo. */
    public List<Ingreso> historialPorPlaca(String placa) {
        String p = VehiculoService.normalizarPlaca(placa);
        if (vehiculoRepository.buscarPorPlaca(p).isEmpty()) {
            throw new EntidadNoEncontradaException("No existe un vehículo con la placa " + p + ".");
        }
        return ingresoRepository.listarPorPlaca(p);
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(reloj).truncatedTo(ChronoUnit.SECONDS);
    }

    // ------------------------------------------------------------------

    private Zona validarZonaElegida(Vehiculo vehiculo, String codigoZona) {
        String c = ZonaService.normalizarCodigo(codigoZona);
        Zona zona = zonaRepository.buscarPorCodigo(c)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe una zona con el código " + c + "."));
        // RN-03
        if (zona.getTipoVehiculo() != vehiculo.getTipo()) {
            throw new ReglaNegocioException("La zona " + c + " es para "
                    + zona.getTipoVehiculo().getDescripcion().toLowerCase() + "s; el vehículo "
                    + vehiculo.getPlaca() + " es " + vehiculo.getTipo().getDescripcion().toLowerCase() + ".");
        }
        // RN-01
        int ocupados = ingresoRepository.contarAbiertosPorZona(c);
        if (ocupados >= zona.getCapacidad()) {
            throw new ParqueoLlenoException("La zona " + c + " está llena (" + ocupados + "/"
                    + zona.getCapacidad() + " espacios ocupados).");
        }
        return zona;
    }

    private Zona asignarZonaAutomatica(Vehiculo vehiculo) {
        List<Zona> zonas = zonaRepository.listarPorTipo(vehiculo.getTipo()); // RN-03
        if (zonas.isEmpty()) {
            throw new ReglaNegocioException("No hay zonas registradas para "
                    + vehiculo.getTipo().getDescripcion().toLowerCase() + "s.");
        }
        for (Zona zona : zonas) {
            if (ingresoRepository.contarAbiertosPorZona(zona.getCodigo()) < zona.getCapacidad()) { // RN-01
                return zona;
            }
        }
        throw new ParqueoLlenoException("No hay espacios libres para "
                + vehiculo.getTipo().getDescripcion().toLowerCase() + "s: todas las zonas están llenas.");
    }
}

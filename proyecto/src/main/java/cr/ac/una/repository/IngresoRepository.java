package cr.ac.una.repository;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.model.Zona;

import cr.ac.una.model.Ingreso;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Operaciones de persistencia de los ingresos (estadías) de vehículos. */
public interface IngresoRepository {

    /** Inserta el ingreso y le asigna el id generado por la base de datos. */
    void insertar(Ingreso ingreso);

    Optional<Ingreso> buscarPorId(int id);

    /** Ingreso sin salida del vehículo indicado, si existe. */
    Optional<Ingreso> buscarAbiertoPorPlaca(String placa);

    /** Cantidad de vehículos que están dentro de la zona en este momento. */
    int contarAbiertosPorZona(String codigoZona);

    /** Vehículos que están dentro del parqueo (JOIN vehiculo + zona). */
    List<Ingreso> listarAbiertos();

    /** Historial de ingresos de un vehículo, del más reciente al más antiguo. */
    List<Ingreso> listarPorPlaca(String placa);

    void registrarSalida(int idIngreso, LocalDateTime fechaHoraSalida);
    
    /**
 * Registra un ingreso de forma transaccional, verificando nuevamente
 * la disponibilidad de la zona bajo bloqueo.
 */
    Ingreso insertarConReserva(Ingreso ingreso);
}

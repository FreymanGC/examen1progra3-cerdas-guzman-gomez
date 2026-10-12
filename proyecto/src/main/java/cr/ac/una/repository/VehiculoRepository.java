package cr.ac.una.repository;

import cr.ac.una.model.Vehiculo;
import java.util.List;
import java.util.Optional;

/** Operaciones de persistencia de los vehículos autorizados. */
public interface VehiculoRepository {

    void insertar(Vehiculo vehiculo);

    Optional<Vehiculo> buscarPorPlaca(String placa);

    List<Vehiculo> listar();

    void actualizar(Vehiculo vehiculo);
}

package cr.ac.una.controller;

import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.service.VehiculoService;
import java.util.List;

/** Recibe las solicitudes de la consola sobre vehículos y las delega al Service. */
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    public Respuesta<Vehiculo> registrar(String placa, TipoVehiculo tipo, String marca,
                                         String color, String propietario) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<Vehiculo> buscar(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<List<Vehiculo>> listar() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<Vehiculo> actualizar(String placa, TipoVehiculo tipo, String marca,
                                          String color, String propietario) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

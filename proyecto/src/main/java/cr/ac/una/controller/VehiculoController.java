package cr.ac.una.controller;

import cr.ac.una.model.CategoriaVehiculo;
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
                                         String color, String propietario, CategoriaVehiculo categoria) {
        return ManejadorErrores.ejecutar("Vehículo registrado correctamente.",
                () -> vehiculoService.registrar(new Vehiculo(placa, tipo, marca, color, propietario,categoria)));
    }

    public Respuesta<Vehiculo> buscar(String placa) {
        return ManejadorErrores.ejecutar("Vehículo encontrado.", () -> vehiculoService.buscarPorPlaca(placa));
    }

    public Respuesta<List<Vehiculo>> listar() {
        return ManejadorErrores.ejecutar("Vehículos autorizados:", vehiculoService::listar);
    }

    public Respuesta<Vehiculo> actualizar(String placa, TipoVehiculo tipo, String marca,
                                          String color, String propietario, CategoriaVehiculo categoria) {
        return ManejadorErrores.ejecutar("Vehículo actualizado correctamente.",
                () -> vehiculoService.actualizar(new Vehiculo(placa, tipo, marca, color, propietario, categoria)));
    }
}

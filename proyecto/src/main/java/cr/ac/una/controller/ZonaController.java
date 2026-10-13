package cr.ac.una.controller;

import cr.ac.una.model.OcupacionZona;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Zona;
import cr.ac.una.service.ZonaService;
import java.util.List;

/** Recibe las solicitudes de la consola sobre zonas y las delega al Service. */
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    public Respuesta<Zona> registrar(String codigo, String nombre, TipoVehiculo tipo, int capacidad) {
        return ManejadorErrores.ejecutar("Zona registrada correctamente.",
                () -> zonaService.registrar(new Zona(codigo, nombre, tipo, capacidad)));
    }

    public Respuesta<Zona> buscar(String codigo) {
        return ManejadorErrores.ejecutar("Zona encontrada.", () -> zonaService.buscarPorCodigo(codigo));
    }

    public Respuesta<List<Zona>> listar() {
        return ManejadorErrores.ejecutar("Zonas del parqueo:", zonaService::listar);
    }

    public Respuesta<Zona> actualizarCapacidad(String codigo, int nuevaCapacidad) {
        return ManejadorErrores.ejecutar("Capacidad actualizada correctamente.",
                () -> zonaService.actualizarCapacidad(codigo, nuevaCapacidad));
    }

    public Respuesta<List<OcupacionZona>> consultarOcupacion() {
        return ManejadorErrores.ejecutar("Espacios por zona:", zonaService::consultarOcupacion);
    }
}

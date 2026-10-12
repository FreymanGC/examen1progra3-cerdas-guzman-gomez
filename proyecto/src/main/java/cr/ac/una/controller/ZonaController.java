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
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<Zona> buscar(String codigo) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<List<Zona>> listar() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<Zona> actualizarCapacidad(String codigo, int nuevaCapacidad) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<List<OcupacionZona>> consultarOcupacion() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

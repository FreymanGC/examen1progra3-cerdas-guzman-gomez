package cr.ac.una.config;

import cr.ac.una.controller.IngresoController;
import cr.ac.una.controller.VehiculoController;
import cr.ac.una.controller.ZonaController;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.IngresoRepositoryJdbc;
import cr.ac.una.repository.VehiculoRepository;
import cr.ac.una.repository.VehiculoRepositoryJdbc;
import cr.ac.una.repository.ZonaRepository;
import cr.ac.una.repository.ZonaRepositoryJdbc;
import cr.ac.una.service.IngresoService;
import cr.ac.una.service.VehiculoService;
import cr.ac.una.service.ZonaService;

/**
 * Arma el sistema: crea las implementaciones JDBC y las inyecta (como
 * interfaces) en los Services, y los Services en los Controllers.
 * Es el único lugar que conoce las clases concretas *RepositoryJdbc;
 * así App no accede directamente a la capa repository.
 */
public final class Contenedor {

    private final VehiculoController vehiculoController;
    private final ZonaController zonaController;
    private final IngresoController ingresoController;

    public Contenedor() {
        VehiculoRepository vehiculoRepository = new VehiculoRepositoryJdbc();
        ZonaRepository zonaRepository = new ZonaRepositoryJdbc();
        IngresoRepository ingresoRepository = new IngresoRepositoryJdbc();

        VehiculoService vehiculoService = new VehiculoService(vehiculoRepository, ingresoRepository);
        ZonaService zonaService = new ZonaService(zonaRepository, ingresoRepository);
        IngresoService ingresoService = new IngresoService(ingresoRepository, vehiculoRepository, zonaRepository);

        this.vehiculoController = new VehiculoController(vehiculoService);
        this.zonaController = new ZonaController(zonaService);
        this.ingresoController = new IngresoController(ingresoService);
    }

    public VehiculoController getVehiculoController() {
        return vehiculoController;
    }

    public ZonaController getZonaController() {
        return zonaController;
    }

    public IngresoController getIngresoController() {
        return ingresoController;
    }
}

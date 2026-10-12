package cr.ac.una.controller;

import cr.ac.una.model.Ingreso;
import cr.ac.una.service.IngresoService;
import java.util.List;

/** Recibe las solicitudes de ingreso/salida y las consultas de estadías. */
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    public Respuesta<Ingreso> registrarIngreso(String placa, String codigoZona) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<Ingreso> registrarSalida(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<List<Ingreso>> listarVehiculosDentro() {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }

    public Respuesta<List<Ingreso>> historial(String placa) {
        throw new UnsupportedOperationException("Pendiente: se implementa en la Etapa 2");
    }
}

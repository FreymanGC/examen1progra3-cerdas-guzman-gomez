package cr.ac.una.controller;

import cr.ac.una.model.Ingreso;
import cr.ac.una.service.IngresoService;
import java.time.LocalDateTime;
import java.util.List;

/** Recibe las solicitudes de ingreso/salida y las consultas de estadías. */
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    public Respuesta<Ingreso> registrarIngreso(String placa, String codigoZona) {
        return ManejadorErrores.ejecutar("Ingreso registrado correctamente.",
                () -> ingresoService.registrarIngreso(placa, codigoZona));
    }

    public Respuesta<Ingreso> registrarSalida(String placa) {
        return ManejadorErrores.ejecutar("Salida registrada correctamente.",
                () -> ingresoService.registrarSalida(placa));
    }

    public Respuesta<List<Ingreso>> listarVehiculosDentro() {
        return ManejadorErrores.ejecutar("Vehículos dentro del parqueo:", ingresoService::listarVehiculosDentro);
    }

    public Respuesta<List<Ingreso>> historial(String placa) {
        return ManejadorErrores.ejecutar("Historial de ingresos:", () -> ingresoService.historialPorPlaca(placa));
    }

    /** Hora actual según el Service (para calcular permanencias en pantalla). */
    public LocalDateTime ahora() {
        return ingresoService.ahora();
    }
}

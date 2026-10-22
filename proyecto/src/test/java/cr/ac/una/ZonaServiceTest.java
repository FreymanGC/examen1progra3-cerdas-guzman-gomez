package cr.ac.una;

import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Zona;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.ZonaRepository;
import cr.ac.una.service.ZonaService;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ZonaServiceTest {

    private static final class Escenario {
        int llamadasActualizacion = 0;
        String codigoRecibido;
        int capacidadRecibida;
        boolean rechazarActualizacion = false;

        ZonaService servicio() {

            ZonaRepository zonas = (ZonaRepository)
                    Proxy.newProxyInstance(
                            ZonaRepository.class.getClassLoader(),
                            new Class<?>[]{ZonaRepository.class},
                            (proxy, metodo, args) -> {

                                if (metodo.getName().equals(
                                        "actualizarCapacidadSegura")) {

                                    llamadasActualizacion++;

                                    codigoRecibido = (String) args[0];
                                    capacidadRecibida = (Integer) args[1];

                                    if (rechazarActualizacion) {
                                        throw new ReglaNegocioException(
                                                "La capacidad no puede ser "
                                                + "menor que la ocupación.");
                                    }

                                    return new Zona(
                                            codigoRecibido,
                                            "Zona A",
                                            TipoVehiculo.AUTOMOVIL,
                                            capacidadRecibida);
                                }

                                throw new UnsupportedOperationException(
                                        metodo.getName());
                            });

            IngresoRepository ingresos = (IngresoRepository)
                    Proxy.newProxyInstance(
                            IngresoRepository.class.getClassLoader(),
                            new Class<?>[]{IngresoRepository.class},
                            (proxy, metodo, args) -> {
                                throw new UnsupportedOperationException(
                                        metodo.getName());
                            });

            return new ZonaService(zonas, ingresos);
        }
    }

    @Test
    void rechazaCapacidadCero() {
        Escenario e = new Escenario();

        assertThrows(ValidacionException.class,
                () -> e.servicio().actualizarCapacidad("A", 0));

        assertEquals(0, e.llamadasActualizacion);
    }

    @Test
    void rechazaCapacidadSuperiorAlMaximo() {
        Escenario e = new Escenario();

        assertThrows(ValidacionException.class,
                () -> e.servicio().actualizarCapacidad("A", 501));

        assertEquals(0, e.llamadasActualizacion);
    }

    @Test
    void actualizaCapacidadValidaYNormalizaCodigo() {
        Escenario e = new Escenario();

        Zona resultado = e.servicio()
                .actualizarCapacidad(" a ", 10);

        assertEquals(1, e.llamadasActualizacion);
        assertEquals("A", e.codigoRecibido);
        assertEquals(10, e.capacidadRecibida);
        assertEquals(10, resultado.getCapacidad());
    }

    @Test
    void propagaRechazoPorOcupacion() {
        Escenario e = new Escenario();
        e.rechazarActualizacion = true;

        assertThrows(ReglaNegocioException.class,
                () -> e.servicio().actualizarCapacidad("A", 2));

        assertEquals(1, e.llamadasActualizacion);
    }
}

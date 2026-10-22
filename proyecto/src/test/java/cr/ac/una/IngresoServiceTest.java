package cr.ac.una;

import cr.ac.una.exception.EspaciosReservadosException;
import cr.ac.una.exception.ParqueoLlenoException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.VehiculoYaIngresadoException;
import cr.ac.una.model.CategoriaVehiculo;
import cr.ac.una.model.Ingreso;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.model.Zona;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.VehiculoRepository;
import cr.ac.una.repository.ZonaRepository;
import cr.ac.una.service.IngresoService;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class IngresoServiceTest {

    private static final Clock RELOJ = Clock.fixed(
            Instant.parse("2026-10-21T14:00:00Z"),
            ZoneId.of("America/Costa_Rica"));

    private static final class Escenario {
        Vehiculo vehiculo;
        Zona zona;
        int ocupados;
        boolean ingresoAbierto;
        Ingreso ingresoInsertado;

        IngresoService servicio() {
            VehiculoRepository vehiculos = (VehiculoRepository)
                    Proxy.newProxyInstance(
                            VehiculoRepository.class.getClassLoader(),
                            new Class<?>[]{VehiculoRepository.class},
                            (proxy, metodo, args) -> {
                                if (metodo.getName().equals("buscarPorPlaca")) {
                                    return Optional.of(vehiculo);
                                }
                                throw new UnsupportedOperationException(
                                        metodo.getName());
                            });

            ZonaRepository zonas = (ZonaRepository)
                    Proxy.newProxyInstance(
                            ZonaRepository.class.getClassLoader(),
                            new Class<?>[]{ZonaRepository.class},
                            (proxy, metodo, args) -> {
                                return switch (metodo.getName()) {
                                    case "buscarPorCodigo" -> Optional.of(zona);
                                    case "listarPorTipo" -> List.of(zona);
                                    default -> throw new UnsupportedOperationException(
                                            metodo.getName());
                                };
                            });

            IngresoRepository ingresos = (IngresoRepository)
                    Proxy.newProxyInstance(
                            IngresoRepository.class.getClassLoader(),
                            new Class<?>[]{IngresoRepository.class},
                            (proxy, metodo, args) -> {
                                return switch (metodo.getName()) {
                                    case "buscarAbiertoPorPlaca" ->
                                        ingresoAbierto
                                                ? Optional.of(new Ingreso(
                                                        vehiculo, zona,
                                                        RELOJ.instant()
                                                                .atZone(RELOJ.getZone())
                                                                .toLocalDateTime()))
                                                : Optional.empty();

                                    case "contarAbiertosPorZona" -> ocupados;

                                    case "insertarConReserva" -> {
                                        ingresoInsertado = (Ingreso) args[0];
                                        yield ingresoInsertado;
                                    }

                                    default -> throw new UnsupportedOperationException(
                                            metodo.getName());
                                };
                            });

            return new IngresoService(
                    ingresos, vehiculos, zonas, RELOJ);
        }
    }

    private Escenario escenario(CategoriaVehiculo categoria,
                               int capacidad,
                               int ocupados) {
        Escenario e = new Escenario();

        e.vehiculo = new Vehiculo(
                "TEST001",
                TipoVehiculo.AUTOMOVIL,
                "Toyota",
                "Blanco",
                "Estudiante",
                categoria);

        e.zona = new Zona(
                "A",
                "Zona A",
                TipoVehiculo.AUTOMOVIL,
                capacidad);

        e.ocupados = ocupados;
        return e;
    }

    @Test
    void particularIngresaCuandoHayEspaciosGenerales() {
        Escenario e = escenario(
                CategoriaVehiculo.PARTICULAR, 5, 2);

        Ingreso resultado = e.servicio()
                .registrarIngreso("TEST001", "A");

        assertNotNull(resultado);
        assertSame(resultado, e.ingresoInsertado);
        assertEquals("TEST001",
                resultado.getVehiculo().getPlaca());
    }

    @Test
    void particularNoPuedeUsarEspaciosReservados() {
        Escenario e = escenario(
                CategoriaVehiculo.PARTICULAR, 5, 3);

        assertThrows(EspaciosReservadosException.class,
                () -> e.servicio().registrarIngreso("TEST001", "A"));

        assertNull(e.ingresoInsertado);
    }

    @Test
    void visitanteNoPuedeUsarEspaciosReservados() {
        Escenario e = escenario(
                CategoriaVehiculo.VISITANTE, 5, 3);

        assertThrows(EspaciosReservadosException.class,
                () -> e.servicio().registrarIngreso("TEST001", "A"));

        assertNull(e.ingresoInsertado);
    }

    @Test
    void oficialPuedeUsarEspaciosReservados() {
        Escenario e = escenario(
                CategoriaVehiculo.OFICIAL, 5, 4);

        Ingreso resultado = e.servicio()
                .registrarIngreso("TEST001", "A");

        assertNotNull(resultado);
        assertSame(resultado, e.ingresoInsertado);
    }

    @Test
    void oficialNoPuedeSuperarCapacidadTotal() {
        Escenario e = escenario(
                CategoriaVehiculo.OFICIAL, 5, 5);

        assertThrows(ParqueoLlenoException.class,
                () -> e.servicio().registrarIngreso("TEST001", "A"));

        assertNull(e.ingresoInsertado);
    }

    @Test
    void vehiculoNoPuedeIngresarDosVeces() {
        Escenario e = escenario(
                CategoriaVehiculo.OFICIAL, 5, 1);
        e.ingresoAbierto = true;

        assertThrows(VehiculoYaIngresadoException.class,
                () -> e.servicio().registrarIngreso("TEST001", "A"));

        assertNull(e.ingresoInsertado);
    }

    @Test
    void rechazaZonaDeTipoIncompatible() {
        Escenario e = escenario(
                CategoriaVehiculo.OFICIAL, 5, 0);

        e.zona = new Zona(
                "M",
                "Motocicletas",
                TipoVehiculo.MOTOCICLETA,
                5);

        assertThrows(ReglaNegocioException.class,
                () -> e.servicio().registrarIngreso("TEST001", "M"));

        assertNull(e.ingresoInsertado);
    }

    @Test
    void asignacionAutomaticaRespetaEspaciosReservados() {
        Escenario e = escenario(
                CategoriaVehiculo.PARTICULAR, 5, 3);

        assertThrows(EspaciosReservadosException.class,
                () -> e.servicio().registrarIngreso("TEST001", ""));

        assertNull(e.ingresoInsertado);
    }
}


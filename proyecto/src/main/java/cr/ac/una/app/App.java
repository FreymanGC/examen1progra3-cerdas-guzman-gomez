package cr.ac.una.app;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.config.Contenedor;
import cr.ac.una.controller.IngresoController;
import cr.ac.una.controller.Respuesta;
import cr.ac.una.controller.VehiculoController;
import cr.ac.una.controller.ZonaController;
import cr.ac.una.model.Ingreso;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.CategoriaVehiculo;
import cr.ac.una.model.Vehiculo;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Punto de entrada de la aplicación (interfaz de consola) del sistema de
 * administración de parqueos.
 *
 * Solo muestra menús, lee datos y presenta resultados: no contiene SQL ni
 * reglas de negocio. Todo se delega en los Controllers.
 *
 * Flujo:  App (consola) → Controller → Service → Repository → MySQL
 */
public class App {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final VehiculoController vehiculos;
    private final ZonaController zonas;
    private final IngresoController ingresos;

    public App(Contenedor contenedor) {
        this.vehiculos = contenedor.getVehiculoController();
        this.zonas = contenedor.getZonaController();
        this.ingresos = contenedor.getIngresoController();
    }

    public static void main(String[] args) {
        new App(new Contenedor()).ejecutar();
    }

    private void ejecutar() {
        int opcion;
        do {
            Consola.titulo("Parqueo Institucional - Caseta de ingreso");
            System.out.println("1. Probar conexión con la base de datos");
            System.out.println("2. Vehículos autorizados");
            System.out.println("3. Zonas del parqueo");
            System.out.println("4. Registrar INGRESO de vehículo");
            System.out.println("5. Registrar SALIDA de vehículo");
            System.out.println("6. Vehículos dentro del parqueo");
            System.out.println("7. Espacios libres por zona");
            System.out.println("8. Historial de ingresos de un vehículo");
            System.out.println("0. Salir");
            opcion = Consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> probarConexion();
                    case 2 -> menuVehiculos();
                    case 3 -> menuZonas();
                    case 4 -> registrarIngreso();
                    case 5 -> registrarSalida();
                    case 6 -> mostrarLista(ingresos.listarVehiculosDentro(), this::formatearDentro);
                    case 7 -> mostrarLista(zonas.consultarOcupacion(), Object::toString);
                    case 8 -> mostrarLista(ingresos.historial(Consola.leerTexto("Placa: ")), this::formatearHistorial);
                    case 0 -> System.out.println("Fin del programa.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                // Última barrera: ningún error inesperado debe cerrar la aplicación.
                System.out.println("Ocurrió un error inesperado: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    // ------------------------------------------------------------------
    // Submenús
    // ------------------------------------------------------------------

    private void menuVehiculos() {
        Consola.titulo("Vehículos autorizados");
        System.out.println("1. Registrar vehículo");
        System.out.println("2. Consultar vehículo por placa");
        System.out.println("3. Listar vehículos");
        System.out.println("4. Actualizar vehículo");
        System.out.println("0. Volver");
        switch (Consola.leerEntero("Opción: ")) {
            case 1 -> {
                String placa = Consola.leerTexto("Placa: ");
                TipoVehiculo tipo = leerTipo();
                String marca = Consola.leerTexto("Marca: ");
                String color = Consola.leerTexto("Color: ");
                String propietario = Consola.leerTexto("Propietario: ");
                CategoriaVehiculo categoria = leerCategoria();
                mostrar(vehiculos.registrar(placa, tipo, marca, color, propietario, categoria));
            }
            case 2 -> mostrar(vehiculos.buscar(Consola.leerTexto("Placa: ")));
            case 3 -> mostrarLista(vehiculos.listar(), Object::toString);
            case 4 -> actualizarVehiculo();
            default -> { }
        }
    }

    private void actualizarVehiculo() {
        Respuesta<Vehiculo> actual = vehiculos.buscar(Consola.leerTexto("Placa del vehículo a actualizar: "));
        if (!actual.exito()) {
            System.out.println(actual.mensaje());
            return;
        }
        Vehiculo v = actual.dato();
        System.out.println("Datos actuales: " + v);
        System.out.println("(Deje el campo vacío para conservar el valor actual)");
        TipoVehiculo tipo = leerTipoOpcional(v.getTipo());
        String marca = valorOActual(Consola.leerTexto("Marca [" + v.getMarca() + "]: "), v.getMarca());
        String color = valorOActual(Consola.leerTexto("Color [" + v.getColor() + "]: "), v.getColor());
        String propietario = valorOActual(
                Consola.leerTexto("Propietario [" + v.getPropietario() + "]: "), v.getPropietario());
        CategoriaVehiculo categoria = leerCategoriaOpcional(v.getCategoria());
        mostrar(vehiculos.actualizar(v.getPlaca(), tipo, marca, color, propietario, categoria));
    }

    private void menuZonas() {
        Consola.titulo("Zonas del parqueo");
        System.out.println("1. Registrar zona");
        System.out.println("2. Consultar zona por código");
        System.out.println("3. Listar zonas");
        System.out.println("4. Actualizar capacidad de una zona");
        System.out.println("0. Volver");
        switch (Consola.leerEntero("Opción: ")) {
            case 1 -> {
                String codigo = Consola.leerTexto("Código: ");
                String nombre = Consola.leerTexto("Nombre: ");
                TipoVehiculo tipo = leerTipo();
                int capacidad = Consola.leerEntero("Capacidad: ");
                mostrar(zonas.registrar(codigo, nombre, tipo, capacidad));
            }
            case 2 -> mostrar(zonas.buscar(Consola.leerTexto("Código: ")));
            case 3 -> mostrarLista(zonas.listar(), Object::toString);
            case 4 -> {
                String codigo = Consola.leerTexto("Código de la zona: ");
                int capacidad = Consola.leerEntero("Nueva capacidad: ");
                mostrar(zonas.actualizarCapacidad(codigo, capacidad));
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------
    // Operación de negocio principal
    // ------------------------------------------------------------------

    private void registrarIngreso() {
        Consola.titulo("Registrar ingreso");
        String placa = Consola.leerTexto("Placa: ");
        String zona = Consola.leerTexto("Código de zona (Enter = asignar automáticamente): ");
        Respuesta<Ingreso> r = ingresos.registrarIngreso(placa, zona);
        System.out.println(r.mensaje());
        if (r.exito()) {
            Ingreso i = r.dato();
            System.out.println("  Ingreso #" + i.getId() + " | " + i.getVehiculo().getPlaca()
                    + " -> zona " + i.getZona().getCodigo() + " (" + i.getZona().getNombre() + ")"
                    + " | entrada: " + i.getFechaHoraEntrada().format(FORMATO_FECHA));
        }
    }

    private void registrarSalida() {
        Consola.titulo("Registrar salida");
        Respuesta<Ingreso> r = ingresos.registrarSalida(Consola.leerTexto("Placa: "));
        System.out.println(r.mensaje());
        if (r.exito()) {
            Ingreso i = r.dato();
            System.out.println("  " + i.getVehiculo().getPlaca() + " salió de la zona " + i.getZona().getCodigo()
                    + " | entrada: " + i.getFechaHoraEntrada().format(FORMATO_FECHA)
                    + " | salida: " + i.getFechaHoraSalida().format(FORMATO_FECHA)
                    + " | permanencia: " + formatearDuracion(i.calcularPermanencia(i.getFechaHoraSalida())));
        }
    }

    // ------------------------------------------------------------------
    // Utilidades de presentación
    // ------------------------------------------------------------------

    private void probarConexion() {
        try (Connection con = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("Conexión exitosa: " + meta.getDatabaseProductName()
                    + " " + meta.getDatabaseProductVersion()
                    + " — base de datos: " + con.getCatalog());
        } catch (SQLException e) {
            System.out.println("No fue posible conectar: " + e.getMessage());
        }
    }

    private static void mostrar(Respuesta<?> r) {
        System.out.println(r.mensaje());
        if (r.exito() && r.dato() != null) {
            System.out.println("  " + r.dato());
        }
    }

    private static <T> void mostrarLista(Respuesta<List<T>> r, java.util.function.Function<T, String> formato) {
        System.out.println(r.mensaje());
        if (!r.exito()) {
            return;
        }
        if (r.dato().isEmpty()) {
            System.out.println("  (sin registros)");
        }
        r.dato().forEach(x -> System.out.println("  " + formato.apply(x)));
    }

    private String formatearDentro(Ingreso i) {
        Vehiculo v = i.getVehiculo();
        return String.format("%-10s | %-11s | %-10s | zona %-5s | desde %s | %s",
                v.getPlaca(), v.getTipo().getDescripcion(), v.getMarca(), i.getZona().getCodigo(),
                i.getFechaHoraEntrada().format(FORMATO_FECHA),
                formatearDuracion(i.calcularPermanencia(ingresos.ahora())));
    }

    private String formatearHistorial(Ingreso i) {
        String salida = i.estaAbierto() ? "(dentro)        " : i.getFechaHoraSalida().format(FORMATO_FECHA);
        return String.format("#%-4d | zona %-5s | entrada %s | salida %s | %s",
                i.getId(), i.getZona().getCodigo(), i.getFechaHoraEntrada().format(FORMATO_FECHA), salida,
                formatearDuracion(i.calcularPermanencia(ingresos.ahora())));
    }

    private static String formatearDuracion(Duration d) {
        long minutos = Math.max(0, d.toMinutes());
        return minutos / 60 + " h " + minutos % 60 + " min";
    }

    private static TipoVehiculo leerTipo() {
        while (true) {
            int op = Consola.leerEntero("Tipo (1 = Automóvil, 2 = Motocicleta): ");
            if (op == 1) {
                return TipoVehiculo.AUTOMOVIL;
            }
            if (op == 2) {
                return TipoVehiculo.MOTOCICLETA;
            }
            System.out.println("  Opción no válida.");
        }
    }

    private static TipoVehiculo leerTipoOpcional(TipoVehiculo actual) {
        String t = Consola.leerTexto("Tipo (1 = Automóvil, 2 = Motocicleta) [" + actual.getDescripcion() + "]: ");
        return switch (t) {
            case "1" -> TipoVehiculo.AUTOMOVIL;
            case "2" -> TipoVehiculo.MOTOCICLETA;
            default -> actual;
        };
    }

    private static String valorOActual(String nuevo, String actual) {
        return nuevo.isBlank() ? actual : nuevo;
    }
    
    private static CategoriaVehiculo leerCategoria() {
    while (true) {
        System.out.println("Categoría del vehículo:");
        System.out.println("1. PARTICULAR");
        System.out.println("2. OFICIAL");
        System.out.println("3. VISITANTE");

        String opcion = Consola.leerTexto("Seleccione una opción: ");

        switch (opcion) {
            case "1":
                return CategoriaVehiculo.PARTICULAR;
            case "2":
                return CategoriaVehiculo.OFICIAL;
            case "3":
                return CategoriaVehiculo.VISITANTE;
            default:
                System.out.println("Categoría inválida. Intente nuevamente.");
            }
        }
    }
    
    private static CategoriaVehiculo leerCategoriaOpcional(
        CategoriaVehiculo actual) {

        while (true) {
            System.out.println("Categoría actual: " + actual);
            System.out.println("1. PARTICULAR");
            System.out.println("2. OFICIAL");
            System.out.println("3. VISITANTE");

            String opcion = Consola.leerTexto(
                "Nueva categoría (Enter para conservar): ").trim();

            switch (opcion) {
                case "":
                    return actual;
                case "1":
                    return CategoriaVehiculo.PARTICULAR;
                case "2":
                    return CategoriaVehiculo.OFICIAL;
                case "3":
                    return CategoriaVehiculo.VISITANTE;
                default:
                    System.out.println(
                        "Categoría inválida. Intente nuevamente.");
            }
        }
    }
}


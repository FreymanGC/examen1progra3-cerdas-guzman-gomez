# Etapa 2 — Implementación funcional y persistencia

## Cambios al diseño

El diseño de la Etapa 1 se mantuvo; el UML y el modelo relacional siguen vigentes. Se agregaron o ajustaron estos elementos durante la implementación:

| Cambio | Motivo |
|---|---|
| Implementaciones `VehiculoRepositoryJdbc`, `ZonaRepositoryJdbc` e `IngresoRepositoryJdbc`. | Persistencia real con JDBC sobre MySQL. |
| Clase `config/Contenedor`. | Es el único lugar que crea las clases JDBC concretas y las inyecta como interfaces en los servicios. Así `App` nunca accede a la capa `repository`. |
| Clase `controller/ManejadorErrores` (paquete privado). | Centraliza la conversión de excepciones en `Respuesta` con un mensaje claro, para no repetir el mismo `try/catch` en cada método de los tres controladores. |
| `IngresoService` recibe opcionalmente un `java.time.Clock`. | Permite fijar la hora en las pruebas automatizadas de la Etapa 3 sin depender del reloj real. |
| `IngresoService.registrarSalida` verifica que la salida no sea anterior a la entrada. | Defecto encontrado al probar con un servidor MySQL en otra zona horaria: el `CHECK` de la tabla rechazaba la salida con un error técnico; ahora el usuario recibe un mensaje claro. |
| `schema.sql` empieza con `SET NAMES utf8mb4`. | Al cargarlo desde algunos clientes, las tildes de los datos de prueba se guardaban mal ("AutomÃ³vil"). |
| Datos de prueba en `schema.sql`. | La zona M queda llena (2/2) para demostrar RN-01 de inmediato con la motocicleta `MOT-500600`. |

## Configuración y ejecución

```text
1. Crear la base de datos:   mysql -u root -p < database/schema.sql
   (o abrir database/schema.sql en MySQL Workbench y ejecutarlo completo)
2. Configurar la conexión:   copiar proyecto/src/main/resources/db.properties.example
                             como db.properties y escribir el usuario y la clave de MySQL
3. Ejecutar:                 cd proyecto
                             mvn clean compile exec:java
   (o en NetBeans: abrir la carpeta proyecto/ y ejecutar cr.ac.una.app.App)
```

Requisitos: JDK 21, Maven 3.9, MySQL 8.0.16 o superior (para que se apliquen los `CHECK`).

## Operaciones implementadas

| Operación | Tipo | Controller | Service | Repository |
|---|---|---|---|---|
| Registrar vehículo | registro | `VehiculoController.registrar` | `VehiculoService.registrar` | `VehiculoRepository.insertar` |
| Consultar vehículo por placa | consulta | `VehiculoController.buscar` | `VehiculoService.buscarPorPlaca` | `VehiculoRepository.buscarPorPlaca` |
| Listar vehículos | consulta | `VehiculoController.listar` | `VehiculoService.listar` | `VehiculoRepository.listar` |
| Actualizar vehículo | actualización | `VehiculoController.actualizar` | `VehiculoService.actualizar` | `VehiculoRepository.actualizar` |
| Registrar zona | registro | `ZonaController.registrar` | `ZonaService.registrar` | `ZonaRepository.insertar` |
| Consultar / listar zonas | consulta | `ZonaController.buscar` / `listar` | `ZonaService.buscarPorCodigo` / `listar` | `ZonaRepository.buscarPorCodigo` / `listar` |
| Actualizar capacidad de zona | actualización | `ZonaController.actualizarCapacidad` | `ZonaService.actualizarCapacidad` | `ZonaRepository.actualizar` |
| Espacios libres por zona | consulta (LEFT JOIN + COUNT) | `ZonaController.consultarOcupacion` | `ZonaService.consultarOcupacion` | `ZonaRepository.listarOcupacion` |
| **Registrar ingreso** | **negocio** | `IngresoController.registrarIngreso` | `IngresoService.registrarIngreso` | `IngresoRepository.buscarAbiertoPorPlaca`, `contarAbiertosPorZona`, `insertar` |
| Registrar salida y tiempo de permanencia | negocio / actualización | `IngresoController.registrarSalida` | `IngresoService.registrarSalida` | `IngresoRepository.buscarAbiertoPorPlaca`, `registrarSalida` |
| Vehículos dentro del parqueo | consulta (JOIN) | `IngresoController.listarVehiculosDentro` | `IngresoService.listarVehiculosDentro` | `IngresoRepository.listarAbiertos` |
| Historial de un vehículo | consulta con filtro | `IngresoController.historial` | `IngresoService.historialPorPlaca` | `IngresoRepository.listarPorPlaca` |

## Reglas de negocio implementadas

| ID | Regla | Clase / método del Service | Excepción que se lanza |
|---|---|---|---|
| RN-01 | No superar la capacidad de la zona | `IngresoService.validarZonaElegida` / `asignarZonaAutomatica` (llamados desde `registrarIngreso`) | `ParqueoLlenoException` |
| RN-02 | Un vehículo no puede ingresar si ya está dentro | `IngresoService.registrarIngreso` | `VehiculoYaIngresadoException` |
| RN-03 | Solo zonas del mismo tipo que el vehículo | `IngresoService.validarZonaElegida` / `asignarZonaAutomatica` | `ReglaNegocioException` |
| RN-04 | Solo sale un vehículo que está dentro | `IngresoService.registrarSalida` | `ReglaNegocioException` |
| RN-05 | No cambiar el tipo de un vehículo que está dentro | `VehiculoService.actualizar` | `ReglaNegocioException` |
| RN-06 | La capacidad no puede ser menor que los vehículos dentro | `ZonaService.actualizarCapacidad` | `ReglaNegocioException` |
| RN-07 | Solo ingresan vehículos autorizados (registrados) | `IngresoService.registrarIngreso` | `EntidadNoEncontradaException` |

Además, RN-02 se refuerza en la base de datos con el índice `UNIQUE` sobre la columna calculada `placa_dentro`, y RN-01/RN-03 se apoyan en los `CHECK` y llaves foráneas de `schema.sql`.

## JDBC

Fragmento de un método del repositorio con `PreparedStatement` y `try-with-resources` (`IngresoRepositoryJdbc`):

```java
@Override
public int contarAbiertosPorZona(String codigoZona) {
    String sql = "SELECT COUNT(*) FROM ingreso WHERE zona_codigo = ? AND fecha_hora_salida IS NULL";
    try (Connection con = ConexionBD.obtenerConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, codigoZona);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    } catch (SQLException e) {
        throw new DatosException("Error al contar los vehículos de la zona " + codigoZona, e);
    }
}
```

Consulta con JOIN y COUNT (`ZonaRepositoryJdbc.listarOcupacion`):

```sql
SELECT z.codigo, z.nombre, z.tipo_vehiculo, z.capacidad,
       COUNT(i.id) AS ocupados
FROM zona z
LEFT JOIN ingreso i
       ON i.zona_codigo = z.codigo AND i.fecha_hora_salida IS NULL
GROUP BY z.codigo, z.nombre, z.tipo_vehiculo, z.capacidad
ORDER BY z.codigo
```

¿Alguna operación usa transacción? ¿Cuál y por qué?

No fue necesaria. La guía pide transacción cuando una operación modifica **más de una tabla**. En este diseño ninguna lo hace: registrar un ingreso solo inserta una fila en `ingreso`, y registrar una salida solo actualiza esa fila. Esto se logró a propósito al no guardar un "estado" en `vehiculo` ni un contador en `zona`; ambos datos se calculan a partir de los ingresos abiertos. Si en la Etapa 3 un cambio de requerimiento obliga a modificar dos tablas en una misma operación, se usará `setAutoCommit(false)` con `commit` / `rollback` como indica la sección 14 de la guía.

## Service

Fragmento de la operación de negocio principal (`IngresoService.registrarIngreso`):

```java
public Ingreso registrarIngreso(String placa, String codigoZona) {
    // 1. Validar datos y verificar que el vehículo esté registrado.
    String p = VehiculoService.normalizarPlaca(placa);
    Vehiculo vehiculo = vehiculoRepository.buscarPorPlaca(p)
            .orElseThrow(() -> new EntidadNoEncontradaException(
                    "El vehículo " + p + " no está registrado como vehículo autorizado."));

    // 2. RN-02: el vehículo no puede tener un ingreso abierto.
    if (ingresoRepository.buscarAbiertoPorPlaca(p).isPresent()) {
        throw new VehiculoYaIngresadoException(p);
    }

    // 3 y 4. Seleccionar zona del mismo tipo (RN-03) con espacio libre (RN-01).
    Zona zona = (codigoZona == null || codigoZona.isBlank())
            ? asignarZonaAutomatica(vehiculo)
            : validarZonaElegida(vehiculo, codigoZona);

    // 5. Insertar el ingreso con la fecha y hora actual.
    Ingreso ingreso = new Ingreso(vehiculo, zona, ahora());
    ingresoRepository.insertar(ingreso);
    return ingreso;
}
```

```java
// RN-01 dentro de validarZonaElegida
int ocupados = ingresoRepository.contarAbiertosPorZona(c);
if (ocupados >= zona.getCapacidad()) {
    throw new ParqueoLlenoException("La zona " + c + " está llena (" + ocupados + "/"
            + zona.getCapacidad() + " espacios ocupados).");
}
```

## Excepciones

```java
/** RN-01: la zona (o todas las zonas del tipo) ya alcanzó su capacidad máxima. */
public class ParqueoLlenoException extends ReglaNegocioException {

    public ParqueoLlenoException(String mensaje) {
        super(mensaje);
    }
}
```

Dónde se captura (`controller/ManejadorErrores`, usado por los tres controladores):

```java
static <T> Respuesta<T> ejecutar(String mensajeExito, Supplier<T> operacion) {
    try {
        return Respuesta.ok(mensajeExito, operacion.get());
    } catch (ValidacionException e) {
        return Respuesta.error("Dato inválido: " + e.getMessage());
    } catch (ReglaNegocioException e) {          // incluye ParqueoLlenoException
        return Respuesta.error("Operación rechazada: " + e.getMessage());
    } catch (EntidadNoEncontradaException e) {
        return Respuesta.error("No encontrado: " + e.getMessage());
    } catch (DatosException e) {
        return Respuesta.error("Error de base de datos: " + e.getMessage() + ...);
    }
}
```

Las `SQLException` se envuelven en `DatosException` dentro de cada repositorio, y `App` tiene un `catch (RuntimeException)` final para que ningún error inesperado cierre la aplicación.

## Evidencias de ejecución

Salida completa de una sesión de prueba: [`evidencia_consola.txt`](evidencia_consola.txt). Capturas: `docs/etapa_2/img/`.

**Registro de cada entidad principal**

```text
[2 → 1] Placa: FGH987 | Tipo: 1 | Marca: Mazda | Color: Plateado | Propietario: Pedro Vargas Solís
Vehículo registrado correctamente.
  FGH987     | Automóvil   | Mazda        | Plateado   | Pedro Vargas Solís

[3 → 1] Código: C | Nombre: Zona C - Visitantes | Tipo: 1 | Capacidad: 4
Zona registrada correctamente.
  C      | Zona C - Visitantes        | Automóvil   | capacidad: 4
```

**Consulta y listado**

```text
[2 → 2] Placa: fgh987
Vehículo encontrado.
  FGH987     | Automóvil   | Mazda        | Plateado   | Pedro Vargas Solís

[3 → 3]
Zonas del parqueo:
  A      | Zona A - Automóviles norte | Automóvil   | capacidad: 5
  B      | Zona B - Automóviles sur   | Automóvil   | capacidad: 3
  C      | Zona C - Visitantes        | Automóvil   | capacidad: 4
  M      | Zona M - Motocicletas      | Motocicleta | capacidad: 2
```

**Actualización**

```text
[2 → 4] Placa: FGH987 | Color: Negro (los demás campos en blanco = se conservan)
Vehículo actualizado correctamente.
  FGH987     | Automóvil   | Mazda        | Negro      | Pedro Vargas Solís

[3 → 4] Zona: B | Nueva capacidad: 4
Capacidad actualizada correctamente.
  B      | Zona B - Automóviles sur   | Automóvil   | capacidad: 4
```

**Consulta con JOIN / COUNT (espacios libres por zona)**

```text
[7]
Espacios por zona:
  A      | Zona A - Automóviles norte | Automóvil   | ocupados:   2 | libres:   3 | capacidad:   5
  B      | Zona B - Automóviles sur   | Automóvil   | ocupados:   0 | libres:   4 | capacidad:   4
  C      | Zona C - Visitantes        | Automóvil   | ocupados:   0 | libres:   4 | capacidad:   4
  M      | Zona M - Motocicletas      | Motocicleta | ocupados:   2 | libres:   0 | capacidad:   2
```

**Operación de negocio exitosa**

```text
[4] Placa: fgh987 | Zona: (Enter = automática)
Ingreso registrado correctamente.
  Ingreso #8 | FGH987 -> zona A (Zona A - Automóviles norte) | entrada: 12/10/2026 11:00

[5] Placa: BCD123
Salida registrada correctamente.
  BCD123 salió de la zona A | entrada: 12/10/2026 08:00 | salida: 12/10/2026 11:00 | permanencia: 3 h 0 min
```

**Operación de negocio rechazada por una regla**

```text
[4] Placa: FGH987                      → Operación rechazada: El vehículo FGH987 ya se encuentra dentro del parqueo.       (RN-02)
[4] Placa: MOT-500600                  → Operación rechazada: No hay espacios libres para motocicletas: todas las zonas están llenas. (RN-01)
[4] Placa: CLM789 | Zona: M            → Operación rechazada: La zona M es para motocicletas; el vehículo CLM789 es automóvil. (RN-03)
[4] Placa: ZZZ999                      → No encontrado: El vehículo ZZZ999 no está registrado como vehículo autorizado.   (RN-07)
[5] Placa: DFG321                      → Operación rechazada: No se puede registrar la salida: el vehículo DFG321 no está dentro del parqueo. (RN-04)
[3 → 4] Zona: M | Nueva capacidad: 1   → Operación rechazada: La zona M tiene 2 vehículos dentro; la capacidad no puede ser menor que eso. (RN-06)
```

**Validaciones (la aplicación no se cierra)**

```text
[2 → 1] Placa: FGH987 (repetida)       → Dato inválido: Ya existe un vehículo registrado con la placa FGH987.
[2 → 1] Placa: !!                      → Dato inválido: La placa '!!' no es válida: use de 3 a 10 letras, números o guion.
[Opción del menú] abc                  →   Valor no válido. Digite un número entero.
```

## Distribución del trabajo

| Integrante | Aportes principales |
|---|---|
| Sadrak Cerdas Cedeño | `VehiculoRepositoryJdbc`, `VehiculoService` y `VehiculoController` (registro, consulta, listado y actualización de vehículos). |
| Freyman Guzmán Cortes | `ZonaRepositoryJdbc`, `ZonaService`, `ZonaController`, script SQL definitivo con datos de prueba. |
| Andrey Gómez Alfaro | `IngresoRepositoryJdbc`, `IngresoService` (operación de negocio principal), `IngresoController`, `Contenedor`, menú de `App` y este documento. |

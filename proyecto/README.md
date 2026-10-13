# Sistema de Administración de Parqueos

## Curso

EIF206 – Programación III · Universidad Nacional de Costa Rica

## Integrantes

| Nombre | Usuario de GitHub |
|---|---|
| Sadrak Cerdas Cedeño | USUARIO_GITHUB |
| Freyman Guzmán Cortes | USUARIO_GITHUB |
| Andrey Gómez Alfaro | USUARIO_GITHUB |

## Caso asignado

Caso 6 — Administración de parqueos

## Descripción del sistema

Aplicación de consola para el oficial de seguridad de la caseta de un parqueo institucional con zonas para automóviles y motocicletas. Registra los vehículos autorizados y las zonas, controla cada ingreso y salida y muestra en todo momento qué vehículos están dentro y cuántos espacios quedan libres por zona. Antes de aceptar un ingreso verifica que el vehículo esté autorizado, que no esté ya dentro, que la zona sea de su tipo y que la zona no esté llena. Los datos se guardan en MySQL mediante JDBC.

## Funcionalidades implementadas

- [x] Registrar, consultar, listar y actualizar vehículos autorizados.
- [x] Registrar, consultar, listar zonas y actualizar su capacidad.
- [x] Registrar ingreso de vehículo (operación de negocio principal), con zona elegida o asignada automáticamente.
- [x] Registrar salida y calcular el tiempo de permanencia.
- [x] Consultar los vehículos que están dentro del parqueo (JOIN).
- [x] Consultar espacios ocupados y libres por zona (LEFT JOIN + COUNT).
- [x] Consultar el historial de ingresos de un vehículo.
- [ ] Cambio de requerimiento de la Etapa 3.

## Reglas de negocio implementadas

| ID | Regla | Dónde se valida |
|---|---|---|
| RN-01 | No se puede superar la capacidad máxima de una zona | `IngresoService.registrarIngreso` → `ParqueoLlenoException` |
| RN-02 | Un vehículo no puede ingresar si ya está dentro | `IngresoService.registrarIngreso` → `VehiculoYaIngresadoException` (reforzada con `UNIQUE` en la BD) |
| RN-03 | Un vehículo solo ocupa zonas de su mismo tipo | `IngresoService.registrarIngreso` → `ReglaNegocioException` |
| RN-04 | Solo sale un vehículo que está dentro | `IngresoService.registrarSalida` → `ReglaNegocioException` |
| RN-05 | No se cambia el tipo de un vehículo que está dentro | `VehiculoService.actualizar` → `ReglaNegocioException` |
| RN-06 | La capacidad no baja de los vehículos que hay dentro | `ZonaService.actualizarCapacidad` → `ReglaNegocioException` |
| RN-07 | Solo ingresan vehículos autorizados | `IngresoService.registrarIngreso` → `EntidadNoEncontradaException` |

## Arquitectura

```text
App (consola) → Controller → Service → Repository (interface) → RepositoryJdbc → MySQL
```

- **app**: `App` muestra los menús y lee datos con `Consola`; no tiene SQL ni reglas.
- **controller**: recibe la solicitud, llama al servicio y devuelve una `Respuesta` con un mensaje claro (las excepciones se traducen en `ManejadorErrores`).
- **service**: valida los datos y aplica las reglas RN-01 a RN-07; depende solo de las interfaces de repositorio, recibidas por constructor.
- **repository**: interfaces y sus implementaciones JDBC con `PreparedStatement`, `try-with-resources` y método `mapear`.
- **model**: entidades `Vehiculo`, `Zona`, `Ingreso`, `OcupacionZona` y la enumeración `TipoVehiculo`.
- **exception**: `ValidacionException`, `ReglaNegocioException`, `ParqueoLlenoException`, `VehiculoYaIngresadoException`, `EntidadNoEncontradaException`, `DatosException`.
- **config**: `ConexionBD` (conexión) y `Contenedor` (crea las clases JDBC y las inyecta en los servicios).

## Estructura del proyecto

```text
proyecto/
├── pom.xml
└── src
    ├── main/java/cr/ac/una
    │   ├── app/          App, Consola
    │   ├── config/       ConexionBD, Contenedor
    │   ├── model/        Vehiculo, Zona, Ingreso, OcupacionZona, TipoVehiculo
    │   ├── repository/   *Repository (interfaces) + *RepositoryJdbc
    │   ├── service/      VehiculoService, ZonaService, IngresoService
    │   ├── controller/   VehiculoController, ZonaController, IngresoController, Respuesta
    │   └── exception/    excepciones personalizadas
    └── test/java/cr/ac/una/
```

## Configuración de la base de datos

```bash
mysql -u root -p < database/schema.sql
cd proyecto/src/main/resources
cp db.properties.example db.properties   # y edite usuario/contraseña
```

El script crea la base `examen_progra3` desde cero con datos de prueba. La zona **M** queda llena (2/2) para demostrar la regla de capacidad con la motocicleta `MOT-500600`.

## Pasos para ejecutar

```bash
git clone https://github.com/USUARIO/examen1progra3-cerdas-guzman-gomez.git
cd examen1progra3-cerdas-guzman-gomez/proyecto
mvn clean compile exec:java
```

En NetBeans: *File → Open Project* → carpeta `proyecto/` → ejecutar `cr.ac.una.app.App`.

## Ejecutar las pruebas

```bash
cd proyecto
mvn test
```

## Video de demostración

(Se agrega en la Etapa 3.)

## Tecnologías utilizadas

- Java 21
- Maven 3.9
- MySQL 8.0
- MySQL Connector/J 9.1.0
- JUnit 5.11.3

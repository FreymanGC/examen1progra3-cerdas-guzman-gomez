# Sistema de Administración de Parqueos

## Curso

EIF206 – Programación III · Universidad Nacional de Costa Rica

## Integrantes

| Nombre | Usuario de GitHub |
|---|---|
| Sadrak Cerdas Cedeño | **SadrakCe** |
| Freyman Guzmán Cortes | **FreymanGC** |
| Andrey Gómez Alfaro | **gomez1408** |

## Caso asignado

**Caso 6 — Administración de parqueos institucionales.**

## Descripción del sistema

Aplicación de consola para administrar un parqueo institucional con zonas destinadas a automóviles y motocicletas. Permite registrar vehículos autorizados y zonas, controlar ingresos y salidas, consultar vehículos estacionados, revisar historiales y conocer la ocupación. La información se almacena en MySQL mediante JDBC.

Como cambio de requerimiento de la etapa 3, cada vehículo tiene una categoría (`PARTICULAR`, `OFICIAL` o `VISITANTE`). Los **últimos dos espacios de cada zona** se reservan para vehículos `OFICIAL`. Los vehículos particulares y visitantes no pueden utilizar esos espacios, incluso si están físicamente desocupados.

## Funcionalidades implementadas

- [x] Registrar, consultar, listar y actualizar vehículos autorizados, incluyendo su categoría.
- [x] Registrar, consultar y listar zonas; actualizar su capacidad con validación de ocupación.
- [x] Registrar ingresos en una zona elegida o asignada automáticamente.
- [x] Aplicar la reserva de dos espacios por zona para vehículos oficiales.
- [x] Rechazar ingresos duplicados, zonas incompatibles o sin espacios disponibles para la categoría.
- [x] Registrar salidas y calcular el tiempo de permanencia.
- [x] Consultar vehículos actualmente dentro del parqueo mediante JOIN.
- [x] Consultar ocupación y espacios generales y reservados disponibles por zona.
- [x] Consultar el historial de ingresos de un vehículo.
- [x] Impedir cambiar el tipo o la categoría de un vehículo mientras tiene un ingreso abierto.

## Reglas de negocio

| ID | Regla | Implementación principal |
|---|---|---|
| RN-01 | No superar la capacidad física de una zona. | `IngresoService` / `ParqueoLlenoException` |
| RN-02 | Un vehículo no puede tener dos ingresos abiertos. | `IngresoService` / `VehiculoYaIngresadoException`; comprobación adicional en JDBC |
| RN-03 | Un vehículo solo puede ingresar a zonas de su tipo. | `IngresoService` / `ReglaNegocioException` |
| RN-04 | Solo se registra salida de un vehículo con ingreso abierto. | `IngresoService` / `ReglaNegocioException` |
| RN-05 | No se cambia el tipo ni la categoría de un vehículo estacionado. | `VehiculoService` / `ReglaNegocioException` |
| RN-06 | La capacidad nueva de una zona no puede ser menor que su ocupación actual. | `ZonaService` y `ZonaRepositoryJdbc.actualizarCapacidadSegura()` |
| RN-07 | Solo pueden ingresar vehículos registrados y autorizados. | `IngresoService` / `EntidadNoEncontradaException` |
| RN-08 | Los últimos dos espacios de cada zona se reservan para vehículos oficiales; particulares y visitantes no pueden ocuparlos. | `IngresoService` y `IngresoRepositoryJdbc.insertarConReserva()` / `EspaciosReservadosException` |

En zonas de capacidad inferior a dos, el límite de espacios generales se considera cero. La categoría `OFICIAL` puede utilizar espacios generales y reservados hasta alcanzar la capacidad total.

## Arquitectura

```text
App (consola) → Controller → Service → Repository (interfaz) → RepositoryJdbc → MySQL
```

- **app:** `App` presenta el menú y `Consola` lee los datos.
- **controller:** coordina las solicitudes y devuelve respuestas al menú.
- **service:** valida entradas y aplica las reglas de negocio mediante interfaces de repositorio.
- **repository:** contiene las interfaces y las implementaciones JDBC, con `PreparedStatement` y `try-with-resources`.
- **model:** `Vehiculo`, `Zona`, `Ingreso`, `OcupacionZona`, `TipoVehiculo` y `CategoriaVehiculo`.
- **exception:** incluye `ValidacionException`, `ReglaNegocioException`, `ParqueoLlenoException`, `EspaciosReservadosException`, `VehiculoYaIngresadoException`, `EntidadNoEncontradaException` y `DatosException`.
- **config:** `ConexionBD` y `Contenedor` configuran conexiones y dependencias.

El registro de ingresos utiliza `IngresoRepositoryJdbc.insertarConReserva()`, que verifica las restricciones dentro de una transacción JDBC. La actualización de capacidad utiliza `ZonaRepositoryJdbc.actualizarCapacidadSegura()` para comprobar la ocupación y actualizar la zona en una misma transacción. Estas medidas reducen riesgos de inconsistencias; no se han documentado pruebas automatizadas de concurrencia simultánea.

## Estructura del proyecto

```text
proyecto/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/cr/ac/una/
    │   │   ├── app/
    │   │   ├── config/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   └── exception/
    │   └── resources/
    │       └── db.properties.example
    └── test/java/cr/ac/una/
        ├── IngresoServiceTest.java
        └── ZonaServiceTest.java
```

Los documentos de entrega se encuentran en `docs/etapa_1/`, `docs/etapa_2/` y `docs/etapa_3/`. El script principal de la base de datos se encuentra en `database/schema.sql`.

## Configuración de la base de datos

**Requisitos:** Java 21 o superior, Maven, MySQL 8.0 o superior y un cliente MySQL.

Desde la raíz del repositorio, ejecutar el script de creación:

```bash
mysql -u root -p < database/schema.sql
```

El script debe crear la base `examen_progra3`, sus tablas y datos de prueba. **Antes de la entrega, comprobar que los datos de ejemplo respeten la reserva de dos espacios por zona**, especialmente la zona M si conserva dos motocicletas particulares en una zona de capacidad 2.

Crear el archivo local de configuración a partir de la plantilla `proyecto/src/main/resources/db.properties.example`, indicando la URL JDBC, el usuario y la contraseña de MySQL conforme a las claves de la plantilla. **No publicar `db.properties` ni contraseñas.**

## Ejecutar el sistema

Desde la raíz del repositorio:

```bash
cd proyecto
mvn clean compile exec:java
```

En NetBeans, abrir la carpeta `proyecto/` como proyecto Maven y ejecutar `cr.ac.una.app.App`.

**Repositorio GitHub:** [AGREGAR URL REAL DEL REPOSITORIO]

## Pruebas

Desde `proyecto/`:

```bash
mvn test
```

Resultados comprobados de JUnit 5:

| Clase | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| `IngresoServiceTest` | 8 | 0 | 0 |
| `ZonaServiceTest` | 4 | 0 | 0 |
| **Total** | **12** | **0** | **0** |

Las pruebas automatizadas verifican reglas de servicio con repositorios simulados. También se realizaron comprobaciones manuales de operaciones con MySQL. Los casos se documentan en `docs/etapa_3/pruebas.md`.

## Video de demostración

**[AGREGAR ENLACE ACCESIBLE AL VIDEO — máximo 5 minutos]**

El video debe mostrar la creación de la base de datos, la ejecución del sistema, un ingreso permitido y otro rechazado, una regla en `Service`, un método JDBC con `PreparedStatement` y `try-with-resources`, y el funcionamiento de los espacios reservados. Confirmar con el docente las condiciones de participación para un equipo de tres estudiantes.

## Tecnologías utilizadas

- Java 21
- Maven 3.9 (versión indicada en la documentación inicial; verificar la versión instalada)
- MySQL 8.0 (compatible con la versión utilizada)
- MySQL Connector/J 9.1.0
- JUnit Jupiter 5.11.3
- Maven Surefire Plugin 3.5.2

# Etapa 3 — Integración, pruebas y mejora

## Integración

La aplicación de consola del caso **Parqueo Institucional** integra el registro, consulta y actualización de vehículos autorizados; la administración de zonas y su capacidad; el registro de entradas y salidas; y la consulta de ocupación e historial. La persistencia se realiza en MySQL mediante repositorios JDBC. Se verificaron manualmente operaciones de registro, actualización, ingreso, rechazo por reglas de negocio, salida y consulta de disponibilidad. **Antes de entregar, cotejar las denominaciones y opciones exactas con el menú final de `App.java`.**

| Opción o sección del menú | Funcionalidad | Estado |
|---|---|---|
| Vehículos | Registrar vehículos y actualizar sus datos y categoría | ✅ Probado manualmente |
| Zonas | Consultar zonas y modificar su capacidad | ✅ Probado manualmente |
| Ingresos | Registrar entrada en zona elegida o mediante asignación automática | ✅ Probado manualmente |
| Salidas | Registrar salida de un vehículo estacionado | ✅ Probado manualmente |
| Ocupación | Consultar espacios ocupados, generales y reservados disponibles | ✅ Probado manualmente |
| Consultas e historial | Consultar vehículos dentro e historial por placa | ⚠️ Confirmar todas las opciones del menú y documentar evidencia |

## Cambio de requerimiento

**Cambio solicitado por el docente:** Espacios reservados para vehículos oficiales. Se incorporan las categorías `PARTICULAR`, `OFICIAL` y `VISITANTE`. En cada zona se reservan los últimos dos espacios para vehículos oficiales: los no oficiales solamente ingresan cuando la ocupación es menor que `max(0, capacidad - 2)`; los oficiales pueden ingresar hasta alcanzar la capacidad total. Ningún vehículo puede superar la capacidad de la zona.

**Clases y archivos modificados o agregados:**

| Clase o archivo | Capa | Cambio realizado |
|---|---|---|
| `CategoriaVehiculo` | Modelo | Enum con las tres categorías |
| `Vehiculo` | Modelo | Atributo de categoría, acceso y representación |
| `VehiculoRepositoryJdbc` | Persistencia | Guardado y lectura de categoría |
| `IngresoRepositoryJdbc` | Persistencia | Lectura de categoría en JOIN y registro transaccional con bloqueo |
| `IngresoRepository` | Persistencia | Contrato `insertarConReserva` |
| `IngresoService` | Servicio | Validación de reservas y asignación de zona; delegación de inserción transaccional |
| `EspaciosReservadosException` | Excepciones | Rechazo específico por reservas |
| `OcupacionZona` | Modelo | Cálculo de espacios generales y reservados disponibles |
| `ZonaRepository` y `ZonaRepositoryJdbc` | Persistencia | Actualización segura de capacidad con transacción |
| `ZonaService` | Servicio | Delegación de actualización de capacidad al repositorio transaccional |
| `VehiculoService` | Servicio | Validación de categoría y restricción de cambios mientras el vehículo está estacionado |
| `App` | Interfaz de consola | Selección de categoría al registrar y actualizar; lectura unificada por `Consola` |
| `schema.sql` y `schema_phpmyadmin.sql` | Base de datos | Columna `categoria` y restricción de valores admitidos |
| `IngresoServiceTest` y `ZonaServiceTest` | Pruebas | 12 pruebas unitarias con JUnit 5 |

**Análisis:** La separación en modelo, servicio, repositorio y consola facilitó añadir la categoría y modificar las reglas de ingreso sin rehacer toda la aplicación. Fue necesario actualizar los mapeos JDBC, ambos esquemas SQL, las entradas de consola y las validaciones de ocupación. La mejora de concurrencia requirió nuevas operaciones transaccionales en los repositorios para mantener la consulta de ocupación y la escritura bajo una misma conexión.

## Decisiones de diseño relevantes

- **Enum para la categoría:** evita valores arbitrarios en Java y permite reglas explícitas por categoría.
- **Validaciones en servicio:** el servicio decide compatibilidad y límites de ocupación; el repositorio vuelve a validar bajo bloqueo para proteger la operación concurrente.
- **Transacciones JDBC:** se utiliza `SELECT ... FOR UPDATE`, `commit` y `rollback` al registrar ingresos y cambiar capacidades.
- **Reloj inyectable:** `IngresoService` admite `Clock`, lo que facilita pruebas reproducibles.
- **Interfaces de repositorio:** permiten ejecutar pruebas unitarias con implementaciones simuladas sin acceder a MySQL.
- **Restricción al actualizar vehículos estacionados:** se impide cambiar tipo o categoría mientras existe un ingreso abierto.

## Pruebas y mejoras

- Pruebas: [`pruebas.md`](pruebas.md)
- Mejoras y defectos corregidos: [`mejoras.md`](mejoras.md)

## UML final

El diseño cambió al incorporar `CategoriaVehiculo`, `EspaciosReservadosException` y operaciones transaccionales en las interfaces y repositorios. **Pendiente de verificación:** actualizar y comprobar el archivo `docs/etapa_1/diagramas/uml_final.png` para reflejar estos cambios; no se ha confirmado su existencia ni contenido.

## Aspectos pendientes

- Confirmar las opciones y nombres exactos del menú y adjuntar capturas de las pruebas manuales.
- Realizar una prueba de integración de concurrencia real con MySQL; las pruebas unitarias no verifican los bloqueos JDBC.
- Revisar operaciones concurrentes de salida y cambios de categoría frente a los bloqueos del registro de ingresos.
- Verificar el UML final y que el README describa el cambio implementado.
- Revisar datos de ejemplo de los scripts SQL para que no contradigan la regla de reservas.
- Confirmar que `db.properties`, contraseñas y otros secretos queden fuera del repositorio y del ZIP de entrega.
- Incorporar referencias reales a commits y la distribución del trabajo de los integrantes.

## Distribución del trabajo

| Integrante | Aportes principales |
|---|---|
| Integrante 1 — Sadrak Cerdas Cedeño  | Desarrollo del módulo de vehículos: modelo Vehiculo, categorías PARTICULAR, OFICIAL y VISITANTE, repositorio JDBC, servicio, controlador y validaciones para registrar, consultar y actualizar vehículos. Integración de estas funcionalidades con el menú de consola. |
| Integrante 2 — Freyman Guzmán Cortes | Desarrollo del módulo de zonas: registro y consulta de zonas, validación de capacidades, consulta de ocupación y disponibilidad de espacios generales y reservados. Implementación de la actualización transaccional de capacidad y pruebas relacionadas con ZonaService. |
| Integrante 3 — Andrey Gómez Alfaro | Desarrollo del módulo de ingresos y salidas: validación de vehículos autorizados, asignación de zonas, control de ingresos duplicados y aplicación de las reglas de espacios reservados para vehículos oficiales. Implementación del registro transaccional de ingresos y pruebas relacionadas con IngresoService. |

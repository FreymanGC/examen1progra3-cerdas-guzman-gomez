# Pruebas documentadas

Se documentan **12 pruebas automatizadas JUnit 5**, con más de cuatro casos exitosos y cuatro casos de rechazo/error. Se incluyen la operación principal de ingreso aceptada y rechazada. Adicionalmente, se realizaron comprobaciones manuales sobre MySQL.

## Tipo de pruebas

- [x] Manuales sobre MySQL (realizadas; falta adjuntar capturas o transcripciones al repositorio)
- [x] Automatizadas con JUnit 5 (`mvn test`) — clases: `IngresoServiceTest` y `ZonaServiceTest`

## Casos de prueba automatizados

| ID | Tipo | Descripción | Datos de entrada | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| P-01 | Éxito | Ingreso particular con espacios generales | PARTICULAR; capacidad 5; ocupación 2 | Aceptar ingreso | Ingreso devuelto por el servicio | ✅ |
| P-02 | Error | Particular intenta usar reserva | PARTICULAR; capacidad 5; ocupación 3 | `EspaciosReservadosException` | Excepción esperada | ✅ |
| P-03 | Error | Visitante intenta usar reserva | VISITANTE; capacidad 5; ocupación 3 | `EspaciosReservadosException` | Excepción esperada | ✅ |
| P-04 | Éxito | Oficial ocupa espacio reservado | OFICIAL; capacidad 5; ocupación 4 | Aceptar ingreso | Ingreso devuelto por el servicio | ✅ |
| P-05 | Error | Oficial intenta superar capacidad | OFICIAL; capacidad 5; ocupación 5 | `ParqueoLlenoException` | Excepción esperada | ✅ |
| P-06 | Error | Vehículo ya estacionado intenta reingresar | OFICIAL; ingreso abierto | `VehiculoYaIngresadoException` | Excepción esperada | ✅ |
| P-07 | Error | Zona incompatible con tipo de vehículo | Automóvil en zona de motocicletas | `ReglaNegocioException` | Excepción esperada | ✅ |
| P-08 | Error | Asignación automática sin espacios generales | PARTICULAR; capacidad 5; ocupación 3 | `EspaciosReservadosException` | Excepción esperada | ✅ |
| P-09 | Error | Capacidad igual a cero | Zona A; capacidad 0 | `ValidacionException` | Excepción esperada | ✅ |
| P-10 | Error | Capacidad mayor al máximo | Zona A; capacidad 501 | `ValidacionException` | Excepción esperada | ✅ |
| P-11 | Éxito | Capacidad válida y código normalizado | Código ` a `; capacidad 10 | Código A; capacidad 10 | Valores verificados | ✅ |
| P-12 | Error | Rechazo del repositorio por ocupación | Zona A; capacidad 2; repositorio simulado rechaza | Propagar `ReglaNegocioException` | Excepción esperada | ✅ |

> Los casos P-01 a P-08 comprueban las validaciones de `IngresoService` con repositorios simulados; P-09 a P-12 comprueban `ZonaService`. No son pruebas de integración JDBC.

## Comprobaciones manuales adicionales

- Registro de vehículos y persistencia de categoría en MySQL.
- Rechazo de particulares cuando solo quedan espacios reservados.
- Aceptación de oficiales hasta completar la capacidad de una zona.
- Rechazo al intentar ingresar en una zona completamente llena.
- Asignación automática a una zona con espacio general disponible.
- Rechazo de ingresos duplicados de una misma placa.
- Registro de salida y posterior validación de disponibilidad.
- Rechazo de reducción de capacidad por debajo de la ocupación; aceptación de cambios válidos.
- Conservación de categoría al actualizar y rechazo de cambios de tipo/categoría con ingreso abierto.

## Evidencias

Salida de pruebas automatizadas compartida después de ejecutar JUnit 5:

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s -- in cr.ac.una.IngresoServiceTest
Running cr.ac.una.ZonaServiceTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in cr.ac.una.ZonaServiceTest
```

**Evidencia pendiente:** incorporar capturas o transcripciones de las pruebas manuales en `docs/etapa_3/img/`. No se adjuntan capturas no proporcionadas.

## Resumen

| Total automatizadas | Exitosas | Fallidas |
|---:|---:|---:|
| 12 | 12 | 0 |

**Alcance pendiente:** ejecutar pruebas de integración con MySQL para verificar `commit`, `rollback`, bloqueos y concurrencia real; las pruebas anteriores no demuestran estas propiedades.

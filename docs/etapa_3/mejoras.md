# Informe de mejoras

## Mejora de calidad implementada

**Tipo de mejora:** consistencia de datos, separación de responsabilidades y robustez transaccional.

**Problema identificado:** La validación de ocupación y el registro de ingreso se realizaban en operaciones JDBC separadas. Si dos ingresos competían por el último espacio, ambos podían aprobar la comprobación antes de insertar. La actualización de capacidad presentaba un riesgo equivalente al consultar ocupación y actualizar mediante conexiones diferentes.

**Antes (fragmento de `IngresoService`):**

```java
Ingreso ingreso = new Ingreso(vehiculo, zona, ahora());
ingresoRepository.insertar(ingreso);
return ingreso;
```

**Después (fragmento de `IngresoService`):**

```java
Ingreso ingreso = new Ingreso(vehiculo, zona, ahora());
return ingresoRepository.insertarConReserva(ingreso);
```

El nuevo método del repositorio utiliza una transacción JDBC, bloquea las filas pertinentes con `SELECT ... FOR UPDATE`, comprueba nuevamente las reglas y ejecuta `INSERT` y `commit` en la misma conexión, o `rollback` si falla. Se aplicó un enfoque equivalente a la actualización de capacidad mediante `actualizarCapacidadSegura`.

**Beneficio obtenido:** Las validaciones críticas se ejecutan junto con la escritura dentro de una transacción. Esto reduce las condiciones de carrera entre ingresos y modificaciones de capacidad, sin trasladar toda la lógica de presentación al repositorio. La eficacia ante simultaneidad real está pendiente de prueba de integración.

**Commit:** PENDIENTE — añadir hash o enlace del commit real de la mejora.

## Defectos encontrados y corregidos

| ID | Defecto | Cómo se detectó (prueba o revisión) | Corrección | Commit |
|---|---|---|---|---|
| D-01 | El esquema SQL no contemplaba la categoría | Revisión de `schema.sql` y `schema_phpmyadmin.sql` | Columna `categoria` y restricción de valores permitidos | Pendiente |
| D-02 | Categoría nula no validada | Revisión de `VehiculoService` | Validación obligatoria de categoría | Pendiente |
| D-03 | Dos lectores `Scanner` sobre `System.in` | Revisión de consola | Reutilización de `Consola.leerTexto` | Pendiente |
| D-04 | Actualización obligaba a elegir categoría de nuevo | Prueba manual de actualización | `leerCategoriaOpcional` conserva el valor con Enter | Pendiente |
| D-05 | Permitía cambiar categoría con ingreso abierto | Prueba manual de vehículo estacionado | Restricción de cambio de tipo o categoría mientras está dentro | Pendiente |
| D-06 | Riesgo de carrera al validar e insertar ingresos | Revisión de conexiones JDBC | `insertarConReserva` con bloqueo y transacción | Pendiente |
| D-07 | Riesgo de carrera al reducir capacidad | Revisión de `ZonaService` y repositorio | `actualizarCapacidadSegura` con bloqueo y transacción | Pendiente |
| D-08 | Solo había pruebas JUnit de ejemplo | Revisión de `src/test/java` | 12 pruebas de servicio en `IngresoServiceTest` y `ZonaServiceTest` | Pendiente |

## Verificaciones pendientes

- Ejecutar una prueba de integración concurrente con dos conexiones reales a MySQL.
- Verificar que la actualización de categoría y la salida mantengan consistencia frente a ingresos simultáneos.
- Adjuntar evidencia manual y completar los hashes de los commits.

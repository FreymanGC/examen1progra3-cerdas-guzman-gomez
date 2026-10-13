package cr.ac.una.controller;

import cr.ac.una.exception.DatosException;
import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.ValidacionException;
import java.util.function.Supplier;

/**
 * Ejecuta una operación del Service y convierte cualquier excepción en una
 * Respuesta con un mensaje claro. Así una entrada inválida o un error de base
 * de datos nunca cierra la aplicación.
 */
final class ManejadorErrores {

    private ManejadorErrores() {
    }

    static <T> Respuesta<T> ejecutar(String mensajeExito, Supplier<T> operacion) {
        try {
            return Respuesta.ok(mensajeExito, operacion.get());
        } catch (ValidacionException e) {
            return Respuesta.error("Dato inválido: " + e.getMessage());
        } catch (ReglaNegocioException e) {
            return Respuesta.error("Operación rechazada: " + e.getMessage());
        } catch (EntidadNoEncontradaException e) {
            return Respuesta.error("No encontrado: " + e.getMessage());
        } catch (DatosException e) {
            Throwable causa = e.getCause();
            return Respuesta.error("Error de base de datos: " + e.getMessage()
                    + (causa != null ? " (" + causa.getMessage() + ")" : ""));
        }
    }
}

package cr.ac.una.exception;

/**
 * Datos de entrada inválidos: campos vacíos, formatos incorrectos,
 * valores fuera de rango o registros duplicados.
 */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}

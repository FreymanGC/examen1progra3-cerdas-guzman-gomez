package cr.ac.una.exception;

/**
 * Se viola una regla de negocio del parqueo (RN-xx).
 * Es la clase base de las excepciones de negocio más específicas.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

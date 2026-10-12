package cr.ac.una.exception;

/**
 * Envuelve una SQLException ocurrida en la capa repository, para que las
 * capas superiores no dependan de java.sql.
 */
public class DatosException extends RuntimeException {

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

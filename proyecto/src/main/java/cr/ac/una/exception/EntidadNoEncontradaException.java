package cr.ac.una.exception;

/** Se buscó un vehículo, zona o ingreso que no existe en la base de datos. */
public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}

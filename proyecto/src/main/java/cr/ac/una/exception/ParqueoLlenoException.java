package cr.ac.una.exception;

/** RN-01: la zona (o todas las zonas del tipo) ya alcanzó su capacidad máxima. */
public class ParqueoLlenoException extends ReglaNegocioException {

    public ParqueoLlenoException(String mensaje) {
        super(mensaje);
    }
}

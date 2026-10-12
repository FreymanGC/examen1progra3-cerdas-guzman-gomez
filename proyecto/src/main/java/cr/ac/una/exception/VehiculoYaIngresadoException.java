package cr.ac.una.exception;

/** RN-02: el vehículo ya tiene un ingreso abierto (ya está dentro del parqueo). */
public class VehiculoYaIngresadoException extends ReglaNegocioException {

    public VehiculoYaIngresadoException(String placa) {
        super("El vehículo " + placa + " ya se encuentra dentro del parqueo.");
    }
}

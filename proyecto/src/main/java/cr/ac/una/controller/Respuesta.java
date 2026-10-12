package cr.ac.una.controller;

/**
 * Resultado que un Controller entrega a la consola: si la operación tuvo
 * éxito, un mensaje para el usuario y, opcionalmente, un dato.
 */
public record Respuesta<T>(boolean exito, String mensaje, T dato) {

    public static <T> Respuesta<T> ok(String mensaje, T dato) {
        return new Respuesta<>(true, mensaje, dato);
    }

    public static <T> Respuesta<T> error(String mensaje) {
        return new Respuesta<>(false, mensaje, null);
    }
}

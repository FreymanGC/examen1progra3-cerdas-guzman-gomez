package cr.ac.una.service;

import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.OcupacionZona;
import cr.ac.una.model.Zona;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.ZonaRepository;
import java.util.List;

/** Validaciones y reglas sobre las zonas del parqueo. */
public class ZonaService {

    private static final String FORMATO_CODIGO = "[A-Z0-9-]{1,10}";
    public static final int CAPACIDAD_MAXIMA = 500;

    private final ZonaRepository zonaRepository;
    private final IngresoRepository ingresoRepository;

    public ZonaService(ZonaRepository zonaRepository, IngresoRepository ingresoRepository) {
        this.zonaRepository = zonaRepository;
        this.ingresoRepository = ingresoRepository;
    }

    public Zona registrar(Zona zona) {
        if (zona == null) {
            throw new ValidacionException("Debe indicar los datos de la zona.");
        }
        zona.setCodigo(normalizarCodigo(zona.getCodigo()));
        if (zona.getNombre() == null || zona.getNombre().isBlank()) {
            throw new ValidacionException("El nombre de la zona es obligatorio.");
        }
        zona.setNombre(zona.getNombre().trim());
        if (zona.getNombre().length() > 60) {
            throw new ValidacionException("El nombre de la zona admite como máximo 60 caracteres.");
        }
        if (zona.getTipoVehiculo() == null) {
            throw new ValidacionException("Debe indicar el tipo de vehículo de la zona.");
        }
        validarCapacidad(zona.getCapacidad());
        if (zonaRepository.buscarPorCodigo(zona.getCodigo()).isPresent()) {
            throw new ValidacionException("Ya existe una zona con el código " + zona.getCodigo() + ".");
        }
        zonaRepository.insertar(zona);
        return zona;
    }

    public Zona buscarPorCodigo(String codigo) {
        String c = normalizarCodigo(codigo);
        return zonaRepository.buscarPorCodigo(c)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe una zona con el código " + c + "."));
    }

    public List<Zona> listar() {
        return zonaRepository.listar();
    }

    /**
     * RN-06: la nueva capacidad no puede ser menor que la cantidad de
     * vehículos que ya están dentro de la zona.
     */
    public Zona actualizarCapacidad(
        String codigo, int nuevaCapacidad) {

    validarCapacidad(nuevaCapacidad);

    String c = normalizarCodigo(codigo);

    return zonaRepository.actualizarCapacidadSegura(
            c, nuevaCapacidad);
}

    /** Espacios ocupados y libres por zona. */
    public List<OcupacionZona> consultarOcupacion() {
        return zonaRepository.listarOcupacion();
    }

    // ------------------------------------------------------------------

    static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ValidacionException("El código de la zona es obligatorio.");
        }
        String c = codigo.trim().toUpperCase();
        if (!c.matches(FORMATO_CODIGO)) {
            throw new ValidacionException("El código de zona '" + codigo.trim()
                    + "' no es válido: use de 1 a 10 letras, números o guion.");
        }
        return c;
    }

    private static void validarCapacidad(int capacidad) {
        if (capacidad <= 0 || capacidad > CAPACIDAD_MAXIMA) {
            throw new ValidacionException("La capacidad debe estar entre 1 y " + CAPACIDAD_MAXIMA + ".");
        }
    }
}

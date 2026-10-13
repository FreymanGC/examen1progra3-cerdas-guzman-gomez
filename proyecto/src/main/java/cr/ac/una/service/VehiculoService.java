package cr.ac.una.service;

import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.repository.IngresoRepository;
import cr.ac.una.repository.VehiculoRepository;
import java.util.List;

/**
 * Validaciones y reglas sobre los vehículos autorizados.
 * Depende de las interfaces de repository (no de las clases JDBC).
 */
public class VehiculoService {

    /** Placas de 3 a 10 caracteres: letras, números y guion (ej.: BCD123, MOT-123456). */
    private static final String FORMATO_PLACA = "[A-Z0-9-]{3,10}";

    private final VehiculoRepository vehiculoRepository;
    private final IngresoRepository ingresoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, IngresoRepository ingresoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.ingresoRepository = ingresoRepository;
    }

    public Vehiculo registrar(Vehiculo vehiculo) {
        validar(vehiculo);
        if (vehiculoRepository.buscarPorPlaca(vehiculo.getPlaca()).isPresent()) {
            throw new ValidacionException("Ya existe un vehículo registrado con la placa " + vehiculo.getPlaca() + ".");
        }
        vehiculoRepository.insertar(vehiculo);
        return vehiculo;
    }

    public Vehiculo buscarPorPlaca(String placa) {
        String p = normalizarPlaca(placa);
        return vehiculoRepository.buscarPorPlaca(p)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe un vehículo con la placa " + p + "."));
    }

    public List<Vehiculo> listar() {
        return vehiculoRepository.listar();
    }

    /**
     * Actualiza marca, color, propietario y tipo. La placa no se modifica
     * porque es la llave primaria.
     * RN-05: no se puede cambiar el tipo de un vehículo que está dentro,
     * porque quedaría en una zona de otro tipo (violaría RN-03).
     */
    public Vehiculo actualizar(Vehiculo cambios) {
        validar(cambios);
        Vehiculo actual = buscarPorPlaca(cambios.getPlaca());
        if (actual.getTipo() != cambios.getTipo()
                && ingresoRepository.buscarAbiertoPorPlaca(actual.getPlaca()).isPresent()) {
            throw new ReglaNegocioException("No se puede cambiar el tipo del vehículo " + actual.getPlaca()
                    + " mientras está dentro del parqueo.");
        }
        vehiculoRepository.actualizar(cambios);
        return cambios;
    }

    // ------------------------------------------------------------------

    private void validar(Vehiculo v) {
        if (v == null) {
            throw new ValidacionException("Debe indicar los datos del vehículo.");
        }
        v.setPlaca(normalizarPlaca(v.getPlaca()));
        if (v.getTipo() == null) {
            throw new ValidacionException("Debe indicar el tipo de vehículo.");
        }
        v.setMarca(requerido(v.getMarca(), "marca", 40));
        v.setColor(requerido(v.getColor(), "color", 30));
        v.setPropietario(requerido(v.getPropietario(), "propietario", 80));
    }

    /** Pasa la placa a mayúsculas, quita espacios y valida su formato. */
    static String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new ValidacionException("La placa es obligatoria.");
        }
        String p = placa.trim().toUpperCase().replace(" ", "");
        if (!p.matches(FORMATO_PLACA)) {
            throw new ValidacionException("La placa '" + placa.trim()
                    + "' no es válida: use de 3 a 10 letras, números o guion.");
        }
        return p;
    }

    private static String requerido(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        String v = valor.trim();
        if (v.length() > maximo) {
            throw new ValidacionException("El campo " + campo + " admite como máximo " + maximo + " caracteres.");
        }
        return v;
    }
}

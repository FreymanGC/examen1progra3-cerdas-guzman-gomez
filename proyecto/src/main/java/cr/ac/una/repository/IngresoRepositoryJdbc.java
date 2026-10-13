package cr.ac.una.repository;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Ingreso;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Vehiculo;
import cr.ac.una.model.Zona;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de IngresoRepository. Las consultas hacen JOIN con
 * vehiculo y zona para devolver objetos Ingreso completos.
 */
public class IngresoRepositoryJdbc implements IngresoRepository {

    /** SELECT base con JOIN; las consultas le agregan su WHERE / ORDER BY. */
    private static final String SELECT_BASE = """
            SELECT i.id, i.fecha_hora_entrada, i.fecha_hora_salida,
                   v.placa, v.tipo, v.marca, v.color, v.propietario,
                   z.codigo, z.nombre, z.tipo_vehiculo, z.capacidad
            FROM ingreso i
            JOIN vehiculo v ON v.placa = i.vehiculo_placa
            JOIN zona z ON z.codigo = i.zona_codigo
            """;

    @Override
    public void insertar(Ingreso ingreso) {
        String sql = "INSERT INTO ingreso (vehiculo_placa, zona_codigo, fecha_hora_entrada) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ingreso.getVehiculo().getPlaca());
            ps.setString(2, ingreso.getZona().getCodigo());
            ps.setTimestamp(3, Timestamp.valueOf(ingreso.getFechaHoraEntrada()));
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    ingreso.setId(llaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("Error al registrar el ingreso del vehículo "
                    + ingreso.getVehiculo().getPlaca(), e);
        }
    }

    @Override
    public Optional<Ingreso> buscarPorId(int id) {
        return buscarUno(SELECT_BASE + "WHERE i.id = ?", ps -> ps.setInt(1, id),
                "Error al buscar el ingreso " + id);
    }

    @Override
    public Optional<Ingreso> buscarAbiertoPorPlaca(String placa) {
        return buscarUno(SELECT_BASE + "WHERE v.placa = ? AND i.fecha_hora_salida IS NULL",
                ps -> ps.setString(1, placa),
                "Error al buscar el ingreso abierto de " + placa);
    }

    @Override
    public int contarAbiertosPorZona(String codigoZona) {
        String sql = "SELECT COUNT(*) FROM ingreso WHERE zona_codigo = ? AND fecha_hora_salida IS NULL";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoZona);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatosException("Error al contar los vehículos de la zona " + codigoZona, e);
        }
    }

    @Override
    public List<Ingreso> listarAbiertos() {
        return buscarVarios(SELECT_BASE + "WHERE i.fecha_hora_salida IS NULL ORDER BY z.codigo, i.fecha_hora_entrada",
                ps -> { }, "Error al listar los vehículos dentro del parqueo");
    }

    @Override
    public List<Ingreso> listarPorPlaca(String placa) {
        return buscarVarios(SELECT_BASE + "WHERE v.placa = ? ORDER BY i.fecha_hora_entrada DESC",
                ps -> ps.setString(1, placa), "Error al consultar el historial de " + placa);
    }

    @Override
    public void registrarSalida(int idIngreso, LocalDateTime fechaHoraSalida) {
        String sql = "UPDATE ingreso SET fecha_hora_salida = ? WHERE id = ? AND fecha_hora_salida IS NULL";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaHoraSalida));
            ps.setInt(2, idIngreso);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosException("Error al registrar la salida del ingreso " + idIngreso, e);
        }
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares (evitan repetir el bloque try-with-resources)
    // ------------------------------------------------------------------

    /** Asigna los parámetros (?) de un PreparedStatement. */
    @FunctionalInterface
    private interface Parametros {
        void asignar(PreparedStatement ps) throws SQLException;
    }

    private Optional<Ingreso> buscarUno(String sql, Parametros parametros, String mensajeError) {
        List<Ingreso> lista = buscarVarios(sql, parametros, mensajeError);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    private List<Ingreso> buscarVarios(String sql, Parametros parametros, String mensajeError) {
        List<Ingreso> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException(mensajeError, e);
        }
    }

    private Ingreso mapear(ResultSet rs) throws SQLException {
        Vehiculo vehiculo = new Vehiculo(
                rs.getString("placa"),
                TipoVehiculo.valueOf(rs.getString("tipo")),
                rs.getString("marca"),
                rs.getString("color"),
                rs.getString("propietario"));
        Zona zona = new Zona(
                rs.getString("codigo"),
                rs.getString("nombre"),
                TipoVehiculo.valueOf(rs.getString("tipo_vehiculo")),
                rs.getInt("capacidad"));
        Timestamp salida = rs.getTimestamp("fecha_hora_salida");
        return new Ingreso(
                rs.getInt("id"),
                vehiculo,
                zona,
                rs.getTimestamp("fecha_hora_entrada").toLocalDateTime(),
                salida == null ? null : salida.toLocalDateTime());
    }
}

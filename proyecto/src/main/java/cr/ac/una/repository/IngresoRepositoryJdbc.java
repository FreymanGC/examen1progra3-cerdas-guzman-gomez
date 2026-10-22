package cr.ac.una.repository;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.exception.EspaciosReservadosException;
import cr.ac.una.exception.ParqueoLlenoException;
import cr.ac.una.exception.VehiculoYaIngresadoException;
import cr.ac.una.exception.ReglaNegocioException;
import cr.ac.una.model.Ingreso;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.CategoriaVehiculo;
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
                   v.placa, v.tipo, v.marca, v.color, v.propietario,v.categoria,
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
                rs.getString("propietario"),
                CategoriaVehiculo.valueOf(rs.getString("categoria")));
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
    
    @Override
    public Ingreso insertarConReserva(Ingreso ingreso) {
        String placa = ingreso.getVehiculo().getPlaca();
        String codigoZona = ingreso.getZona().getCodigo();

        String bloquearVehiculo =
            "SELECT placa FROM vehiculo WHERE placa = ? FOR UPDATE";

        String bloquearZona =
            "SELECT tipo_vehiculo, capacidad FROM zona "
            + "WHERE codigo = ? FOR UPDATE";

        String consultarDuplicado =
            "SELECT COUNT(*) FROM ingreso "
            + "WHERE vehiculo_placa = ? AND fecha_hora_salida IS NULL";

        String consultarOcupacion =
        "SELECT id FROM ingreso "
        + "WHERE zona_codigo = ? AND fecha_hora_salida IS NULL "
        + "FOR UPDATE";

        String insertarIngreso =
            "INSERT INTO ingreso "
            + "(vehiculo_placa, zona_codigo, fecha_hora_entrada) "
            + "VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);

            try {
            // Bloquear el vehículo para evitar ingresos simultáneos
            // con la misma placa.
                try (PreparedStatement ps =
                    con.prepareStatement(bloquearVehiculo)) {
                    ps.setString(1, placa);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ReglaNegocioException(
                                "El vehículo " + placa + " no existe.");
                    }
                }
            }

            int capacidad;
            TipoVehiculo tipoZona;

            // Bloquear la zona durante toda la operación.
            try (PreparedStatement ps =
                    con.prepareStatement(bloquearZona)) {
                ps.setString(1, codigoZona);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ReglaNegocioException(
                                "La zona " + codigoZona + " no existe.");
                    }

                    capacidad = rs.getInt("capacidad");
                    tipoZona = TipoVehiculo.valueOf(
                            rs.getString("tipo_vehiculo"));
                }
            }

            // RN-03: verificar compatibilidad.
            if (tipoZona != ingreso.getVehiculo().getTipo()) {
                throw new ReglaNegocioException(
                        "El tipo de vehículo no es compatible "
                        + "con la zona " + codigoZona + ".");
            }

            // RN-02: impedir ingresos duplicados.
            try (PreparedStatement ps =
                    con.prepareStatement(consultarDuplicado)) {
                ps.setString(1, placa);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        throw new VehiculoYaIngresadoException(placa);
                    }
                }
            }

            int ocupados = 0;

            try (PreparedStatement ps =
            con.prepareStatement(consultarOcupacion)) {

            ps.setString(1, codigoZona);

            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ocupados++;
            }
        }
}

            // RN-01: capacidad máxima.
            if (ocupados >= capacidad) {
                throw new ParqueoLlenoException(
                        "La zona " + codigoZona + " está llena ("
                        + ocupados + "/" + capacidad + ").");
            }

            // RN-N1, RN-N2 y RN-N3: espacios reservados.
            boolean oficial =
                    ingreso.getVehiculo().getCategoria()
                    == CategoriaVehiculo.OFICIAL;

            int limite = oficial
                    ? capacidad
                    : Math.max(0, capacidad - 2);

            if (ocupados >= limite) {
                throw new EspaciosReservadosException(
                        "Los últimos dos espacios de la zona "
                        + codigoZona
                        + " están reservados para vehículos oficiales.");
            }

            try (PreparedStatement ps = con.prepareStatement(
                    insertarIngreso, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, placa);
                ps.setString(2, codigoZona);
                ps.setTimestamp(3, Timestamp.valueOf(
                        ingreso.getFechaHoraEntrada()));

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ingreso.setId(rs.getInt(1));
                    }
                }
            }

            con.commit();
            return ingreso;

        } catch (SQLException | RuntimeException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }

    } catch (SQLException e) {
        throw new DatosException(
                "Error al registrar el ingreso transaccional "
                + "del vehículo " + placa, e);
        }
    }
}

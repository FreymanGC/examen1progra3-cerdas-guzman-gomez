package cr.ac.una.repository;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.OcupacionZona;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Zona;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC de ZonaRepository sobre la tabla zona. */
public class ZonaRepositoryJdbc implements ZonaRepository {

    private static final String COLUMNAS = "codigo, nombre, tipo_vehiculo, capacidad";

    @Override
    public void insertar(Zona z) {
        String sql = "INSERT INTO zona (" + COLUMNAS + ") VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, z.getCodigo());
            ps.setString(2, z.getNombre());
            ps.setString(3, z.getTipoVehiculo().name());
            ps.setInt(4, z.getCapacidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosException("Error al registrar la zona " + z.getCodigo(), e);
        }
    }

    @Override
    public Optional<Zona> buscarPorCodigo(String codigo) {
        String sql = "SELECT " + COLUMNAS + " FROM zona WHERE codigo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosException("Error al buscar la zona " + codigo, e);
        }
    }

    @Override
    public List<Zona> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM zona ORDER BY codigo";
        List<Zona> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("Error al listar las zonas", e);
        }
    }

    @Override
    public List<Zona> listarPorTipo(TipoVehiculo tipo) {
        String sql = "SELECT " + COLUMNAS + " FROM zona WHERE tipo_vehiculo = ? ORDER BY codigo";
        List<Zona> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("Error al listar las zonas de tipo " + tipo, e);
        }
    }

    @Override
    public void actualizar(Zona z) {
        String sql = "UPDATE zona SET nombre = ?, capacidad = ? WHERE codigo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, z.getNombre());
            ps.setInt(2, z.getCapacidad());
            ps.setString(3, z.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosException("Error al actualizar la zona " + z.getCodigo(), e);
        }
    }

    @Override
    public List<OcupacionZona> listarOcupacion() {
        // LEFT JOIN para que también aparezcan las zonas vacías (COUNT = 0).
        String sql = """
                SELECT z.codigo, z.nombre, z.tipo_vehiculo, z.capacidad,
                       COUNT(i.id) AS ocupados
                FROM zona z
                LEFT JOIN ingreso i
                       ON i.zona_codigo = z.codigo AND i.fecha_hora_salida IS NULL
                GROUP BY z.codigo, z.nombre, z.tipo_vehiculo, z.capacidad
                ORDER BY z.codigo
                """;
        List<OcupacionZona> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new OcupacionZona(mapear(rs), rs.getInt("ocupados")));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("Error al consultar la ocupación de las zonas", e);
        }
    }

    private Zona mapear(ResultSet rs) throws SQLException {
        return new Zona(
                rs.getString("codigo"),
                rs.getString("nombre"),
                TipoVehiculo.valueOf(rs.getString("tipo_vehiculo")),
                rs.getInt("capacidad"));
    }
}

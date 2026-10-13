package cr.ac.una.repository;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.TipoVehiculo;
import cr.ac.una.model.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC de VehiculoRepository sobre la tabla vehiculo. */
public class VehiculoRepositoryJdbc implements VehiculoRepository {

    private static final String COLUMNAS = "placa, tipo, marca, color, propietario";

    @Override
    public void insertar(Vehiculo v) {
        String sql = "INSERT INTO vehiculo (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getTipo().name());
            ps.setString(3, v.getMarca());
            ps.setString(4, v.getColor());
            ps.setString(5, v.getPropietario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosException("Error al registrar el vehículo " + v.getPlaca(), e);
        }
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculo WHERE placa = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosException("Error al buscar el vehículo " + placa, e);
        }
    }

    @Override
    public List<Vehiculo> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculo ORDER BY placa";
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException("Error al listar los vehículos", e);
        }
    }

    @Override
    public void actualizar(Vehiculo v) {
        String sql = "UPDATE vehiculo SET tipo = ?, marca = ?, color = ?, propietario = ? WHERE placa = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getTipo().name());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getColor());
            ps.setString(4, v.getPropietario());
            ps.setString(5, v.getPlaca());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosException("Error al actualizar el vehículo " + v.getPlaca(), e);
        }
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        return new Vehiculo(
                rs.getString("placa"),
                TipoVehiculo.valueOf(rs.getString("tipo")),
                rs.getString("marca"),
                rs.getString("color"),
                rs.getString("propietario"));
    }
}

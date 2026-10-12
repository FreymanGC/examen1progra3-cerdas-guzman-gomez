package cr.ac.una.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Estadía de un vehículo dentro del parqueo. Mientras fechaHoraSalida sea
 * null, el vehículo se considera "dentro" (ingreso abierto).
 */
public class Ingreso {

    private int id;
    private Vehiculo vehiculo;
    private Zona zona;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;

    public Ingreso() {
    }

    /** Constructor para un ingreso nuevo (todavía sin id ni salida). */
    public Ingreso(Vehiculo vehiculo, Zona zona, LocalDateTime fechaHoraEntrada) {
        this(0, vehiculo, zona, fechaHoraEntrada, null);
    }

    public Ingreso(int id, Vehiculo vehiculo, Zona zona,
                   LocalDateTime fechaHoraEntrada, LocalDateTime fechaHoraSalida) {
        this.id = id;
        this.vehiculo = vehiculo;
        this.zona = zona;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
    }

    /** true si el vehículo todavía no ha salido. */
    public boolean estaAbierto() {
        return fechaHoraSalida == null;
    }

    /**
     * Tiempo de permanencia. Si el ingreso sigue abierto, se calcula
     * hasta el momento indicado.
     */
    public Duration calcularPermanencia(LocalDateTime hasta) {
        LocalDateTime fin = fechaHoraSalida != null ? fechaHoraSalida : hasta;
        return Duration.between(fechaHoraEntrada, fin);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    @Override
    public String toString() {
        return "Ingreso #" + id
                + " | placa: " + (vehiculo == null ? "-" : vehiculo.getPlaca())
                + " | zona: " + (zona == null ? "-" : zona.getCodigo())
                + " | entrada: " + fechaHoraEntrada
                + " | salida: " + (fechaHoraSalida == null ? "(dentro)" : fechaHoraSalida);
    }
}

package com.inventario.conexion;

import java.time.LocalDateTime;

/**
 * Resultado de una prueba de conexión, listo para mostrarse en la interfaz
 * y para guardarse en el historial CSV.
 */
public record ResultadoConexion(Tipo tipo, String titulo, String detalle, LocalDateTime fecha) {

    /** Clasificación del resultado; cada tipo se muestra con un color distinto. */
    public enum Tipo {
        EXITO,
        ERROR_CONEXION,
        ERROR_DRIVER,
        DATOS_INCOMPLETOS
    }

    public static ResultadoConexion exito(String detalle) {
        return new ResultadoConexion(Tipo.EXITO, "Conexión exitosa", detalle, LocalDateTime.now());
    }

    public static ResultadoConexion falloConexion(String detalle) {
        return new ResultadoConexion(Tipo.ERROR_CONEXION, "Fallo de conexión", detalle, LocalDateTime.now());
    }

    public static ResultadoConexion falloDriver(String detalle) {
        return new ResultadoConexion(Tipo.ERROR_DRIVER, "Fallo del driver JDBC", detalle, LocalDateTime.now());
    }

    public static ResultadoConexion datosIncompletos(String detalle) {
        return new ResultadoConexion(Tipo.DATOS_INCOMPLETOS, "Datos incompletos", detalle, LocalDateTime.now());
    }

    public boolean esExitoso() {
        return tipo == Tipo.EXITO;
    }
}

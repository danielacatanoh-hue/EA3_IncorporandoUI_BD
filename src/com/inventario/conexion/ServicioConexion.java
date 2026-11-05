package com.inventario.conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Lógica de conexión a la base de datos (API JDBC), separada de la interfaz gráfica.
 * No conoce ningún componente Swing: recibe una configuración y devuelve un texto
 * con la información de la base de datos, o lanza la excepción correspondiente.
 */
public class ServicioConexion {

    private static final int TIEMPO_ESPERA_SEGUNDOS = 5;

    /** Driver JDBC que corresponde a cada prefijo de URL. */
    private static final Map<String, String> DRIVERS_POR_PREFIJO = Map.of(
            "jdbc:sqlite:", "org.sqlite.JDBC",
            "jdbc:mysql:", "com.mysql.cj.jdbc.Driver",
            "jdbc:mariadb:", "org.mariadb.jdbc.Driver",
            "jdbc:postgresql:", "org.postgresql.Driver",
            "jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:oracle:", "oracle.jdbc.OracleDriver");

    /**
     * Intenta abrir una conexión con los datos recibidos.
     *
     * @return descripción de la base de datos a la que se conectó
     * @throws ClassNotFoundException si el driver JDBC no está en el classpath
     * @throws SQLException si la URL, el usuario o la contraseña no permiten conectarse
     */
    public String probarConexion(ConfiguracionConexion configuracion)
            throws ClassNotFoundException, SQLException {

        cargarDriver(configuracion.url());
        DriverManager.setLoginTimeout(TIEMPO_ESPERA_SEGUNDOS);

        // try-with-resources: la conexión se cierra sola al salir del bloque,
        // tanto si todo sale bien como si se lanza una excepción.
        try (Connection conexion = abrirConexion(configuracion)) {
            if (!conexion.isValid(TIEMPO_ESPERA_SEGUNDOS)) {
                throw new SQLException("La base de datos no respondió a la validación de la conexión.");
            }
            return describir(conexion.getMetaData());
        }
    }

    /** Carga explícitamente el driver según el prefijo de la URL. */
    private void cargarDriver(String url) throws ClassNotFoundException {
        String driver = DRIVERS_POR_PREFIJO.entrySet().stream()
                .filter(entrada -> url.startsWith(entrada.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (driver != null) {
            Class.forName(driver);
        }
        // Si el prefijo no es conocido, DriverManager buscará un driver registrado
        // y lanzará SQLException ("No suitable driver") si no encuentra ninguno.
    }

    private Connection abrirConexion(ConfiguracionConexion configuracion) throws SQLException {
        if (configuracion.tieneCredenciales()) {
            return DriverManager.getConnection(configuracion.url(),
                    configuracion.usuario(), configuracion.contrasena());
        }
        return DriverManager.getConnection(configuracion.url());
    }

    private String describir(DatabaseMetaData metadatos) throws SQLException {
        return "Motor: " + metadatos.getDatabaseProductName() + " " + metadatos.getDatabaseProductVersion()
                + "\nDriver: " + metadatos.getDriverName() + " " + metadatos.getDriverVersion()
                + "\nURL: " + metadatos.getURL();
    }
}

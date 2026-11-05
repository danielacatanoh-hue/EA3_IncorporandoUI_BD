package com.inventario.conexion;

/**
 * Datos de conexión ingresados por el usuario en la interfaz.
 * Es un record: sus valores no cambian después de crearse.
 */
public record ConfiguracionConexion(String url, String usuario, String contrasena) {

    public ConfiguracionConexion {
        url = url == null ? "" : url.trim();
        usuario = usuario == null ? "" : usuario.trim();
        contrasena = contrasena == null ? "" : contrasena;
    }

    /** Indica si se ingresaron credenciales (en SQLite normalmente quedan vacías). */
    public boolean tieneCredenciales() {
        return !usuario.isEmpty() || !contrasena.isEmpty();
    }
}

package com.inventario.conexion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

/**
 * Guarda cada prueba de conexión en un archivo CSV (historial_conexiones.csv).
 * Nunca se guarda la contraseña.
 */
public class HistorialCsv {

    private static final String ENCABEZADO = "fecha;url;usuario;resultado;detalle";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path archivo;

    public HistorialCsv(Path archivo) {
        this.archivo = archivo;
    }

    public void registrar(ConfiguracionConexion configuracion, ResultadoConexion resultado) throws IOException {
        if (Files.notExists(archivo)) {
            // BOM UTF-8 para que Excel muestre bien las tildes
            Files.writeString(archivo, "﻿" + ENCABEZADO + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        String linea = String.join(";",
                resultado.fecha().format(FORMATO_FECHA),
                limpiar(configuracion.url()),
                limpiar(configuracion.usuario()),
                resultado.titulo(),
                limpiar(resultado.detalle()));
        Files.writeString(archivo, linea + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
    }

    public Path getArchivo() {
        return archivo;
    }

    /** Evita que saltos de línea o punto y coma rompan las columnas del CSV. */
    private String limpiar(String texto) {
        String sinSaltos = texto.replace("\r", " ").replace("\n", " | ").replace(";", ",");
        return "\"" + sinSaltos.replace("\"", "'") + "\"";
    }
}

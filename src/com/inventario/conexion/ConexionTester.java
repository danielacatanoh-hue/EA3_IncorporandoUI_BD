package com.inventario.conexion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Ventana principal (JFrame) que permite ingresar la configuración de una base de datos
 * y validar la conexión. La interfaz solo recoge los datos y muestra el resultado;
 * la conexión JDBC la realiza {@link ServicioConexion}.
 */
public final class ConexionTester extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String URL_EJEMPLO = "jdbc:sqlite:inventario.db";
    private static final Color COLOR_EXITO = new Color(0x1B7F3B);
    private static final Color COLOR_ERROR = new Color(0xC62828);
    private static final Color COLOR_AVISO = new Color(0xB26A00);
    private static final Color COLOR_NEUTRO = new Color(0x555555);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final transient ServicioConexion servicioConexion;
    private final transient HistorialCsv historial;

    private final JTextField campoUrl = new JTextField(URL_EJEMPLO, 30);
    private final JTextField campoUsuario = new JTextField(30);
    private final JPasswordField campoContrasena = new JPasswordField(30);
    private final JButton botonProbar = new JButton("Probar conexión");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JTextArea areaResultado = new JTextArea(10, 50);

    public ConexionTester(ServicioConexion servicioConexion, HistorialCsv historial) {
        super("TecnoInventario - Probador de conexión JDBC");
        this.servicioConexion = servicioConexion;
        this.historial = historial;
        construirInterfaz();
        registrarEventos();
    }

    // ---------------------------------------------------------------------
    // Construcción de la interfaz gráfica
    // ---------------------------------------------------------------------

    private void construirInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.add(crearEncabezado(), BorderLayout.NORTH);
        panelSuperior.add(crearFormulario(), BorderLayout.CENTER);
        panelSuperior.add(crearPanelBotones(), BorderLayout.SOUTH);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(crearPanelResultado(), BorderLayout.CENTER);

        setContentPane(panelPrincipal);
        getRootPane().setDefaultButton(botonProbar);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Configuración de la base de datos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel ayuda = new JLabel("Ejemplo de URL: " + URL_EJEMPLO
                + "  (en SQLite deje vacíos el usuario y la contraseña)");
        ayuda.setForeground(COLOR_NEUTRO);

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 4));
        encabezado.add(titulo);
        encabezado.add(ayuda);
        return encabezado;
    }

    private JPanel crearFormulario() {
        // Columna de etiquetas (WEST) y columna de campos (CENTER), cada una con GridLayout,
        // para que las etiquetas ocupen solo su ancho y los campos el resto de la ventana.
        JPanel etiquetas = new JPanel(new GridLayout(3, 1, 0, 8));
        etiquetas.add(crearEtiqueta("URL de conexión:", campoUrl));
        etiquetas.add(crearEtiqueta("Usuario:", campoUsuario));
        etiquetas.add(crearEtiqueta("Contraseña:", campoContrasena));

        JPanel campos = new JPanel(new GridLayout(3, 1, 0, 8));
        campos.add(campoUrl);
        campos.add(campoUsuario);
        campos.add(campoContrasena);

        JPanel formulario = new JPanel(new BorderLayout(12, 0));
        formulario.add(etiquetas, BorderLayout.WEST);
        formulario.add(campos, BorderLayout.CENTER);
        return formulario;
    }

    /** Crea una etiqueta asociada a su campo (al hacer clic en ella, el foco va al campo). */
    private JLabel crearEtiqueta(String texto, JTextField campo) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setLabelFor(campo);
        return etiqueta;
    }

    private JPanel crearPanelBotones() {
        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botonProbar.setFont(botonProbar.getFont().deriveFont(Font.BOLD));
        botones.add(botonLimpiar);
        botones.add(botonProbar);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(botones, BorderLayout.EAST);
        return contenedor;
    }

    private JScrollPane crearPanelResultado() {
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaResultado.setMargin(new java.awt.Insets(8, 8, 8, 8));
        mostrarMensaje("Ingrese los datos y presione \"Probar conexión\".", COLOR_NEUTRO);

        JScrollPane desplazamiento = new JScrollPane(areaResultado);
        desplazamiento.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));
        return desplazamiento;
    }

    // ---------------------------------------------------------------------
    // Eventos
    // ---------------------------------------------------------------------

    private void registrarEventos() {
        botonProbar.addActionListener(evento -> probarConexion());
        botonLimpiar.addActionListener(evento -> limpiarFormulario());
    }

    /** Acción del botón "Probar conexión". */
    private void probarConexion() {
        ConfiguracionConexion configuracion = leerConfiguracion();
        if (configuracion.url().isEmpty()) {
            mostrarResultado(configuracion, ResultadoConexion.datosIncompletos(
                    "Debe escribir la URL de conexión, por ejemplo " + URL_EJEMPLO));
            return;
        }

        botonProbar.setEnabled(false);
        mostrarMensaje("Probando conexión con " + configuracion.url() + " ...", COLOR_NEUTRO);

        // La conexión se ejecuta en segundo plano para que la ventana no se congele
        // mientras espera la respuesta del servidor.
        new SwingWorker<ResultadoConexion, Void>() {
            @Override
            protected ResultadoConexion doInBackground() {
                try {
                    String informacion = servicioConexion.probarConexion(configuracion);
                    return ResultadoConexion.exito(informacion);
                } catch (ClassNotFoundException e) {
                    return ResultadoConexion.falloDriver(
                            "No se encontró la clase del driver: " + e.getMessage()
                            + "\nAgregue el archivo .jar del driver a la carpeta lib y al classpath.");
                } catch (SQLException e) {
                    return ResultadoConexion.falloConexion(
                            "Mensaje técnico: " + e.getMessage()
                            + "\nSQLState: " + e.getSQLState() + "   Código: " + e.getErrorCode());
                }
            }

            @Override
            protected void done() {
                try {
                    mostrarResultado(configuracion, get());
                } catch (InterruptedException | ExecutionException e) {
                    mostrarResultado(configuracion, ResultadoConexion.falloConexion(
                            "Error inesperado: " + e.getMessage()));
                } finally {
                    botonProbar.setEnabled(true);
                }
            }
        }.execute();
    }

    private ConfiguracionConexion leerConfiguracion() {
        char[] contrasena = campoContrasena.getPassword();
        try {
            return new ConfiguracionConexion(campoUrl.getText(), campoUsuario.getText(), new String(contrasena));
        } finally {
            Arrays.fill(contrasena, '\0'); // no dejar la contraseña en memoria
        }
    }

    private void limpiarFormulario() {
        campoUrl.setText(URL_EJEMPLO);
        campoUsuario.setText("");
        campoContrasena.setText("");
        mostrarMensaje("Ingrese los datos y presione \"Probar conexión\".", COLOR_NEUTRO);
        campoUrl.requestFocusInWindow();
    }

    // ---------------------------------------------------------------------
    // Presentación del resultado
    // ---------------------------------------------------------------------

    private void mostrarResultado(ConfiguracionConexion configuracion, ResultadoConexion resultado) {
        String texto = "[" + resultado.fecha().format(FORMATO_HORA) + "] "
                + etiquetaPara(resultado.tipo()) + " - " + resultado.titulo().toUpperCase()
                + "\n\n" + resultado.detalle();
        try {
            historial.registrar(configuracion, resultado);
            texto += "\n\nPrueba registrada en " + historial.getArchivo().getFileName();
        } catch (IOException e) {
            texto += "\n\n(No se pudo guardar el historial: " + e.getMessage() + ")";
        }
        mostrarMensaje(texto, colorPara(resultado.tipo()));
    }

    private String etiquetaPara(ResultadoConexion.Tipo tipo) {
        return switch (tipo) {
            case EXITO -> "OK";
            case ERROR_CONEXION, ERROR_DRIVER -> "ERROR";
            case DATOS_INCOMPLETOS -> "AVISO";
        };
    }

    private Color colorPara(ResultadoConexion.Tipo tipo) {
        return switch (tipo) {
            case EXITO -> COLOR_EXITO;
            case ERROR_CONEXION, ERROR_DRIVER -> COLOR_ERROR;
            case DATOS_INCOMPLETOS -> COLOR_AVISO;
        };
    }

    private void mostrarMensaje(String texto, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(texto);
        areaResultado.setCaretPosition(0);
    }

    // ---------------------------------------------------------------------
    // Punto de entrada
    // ---------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si no se puede aplicar el estilo del sistema, se usa el de Java.
            }
            ConexionTester ventana = new ConexionTester(
                    new ServicioConexion(), new HistorialCsv(Path.of("historial_conexiones.csv")));
            ventana.setVisible(true);
        });
    }
}

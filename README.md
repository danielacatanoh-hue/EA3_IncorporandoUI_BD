# EA3. Incorporando UI y BD — Probador de conexión JDBC

Aplicación de escritorio en **Java Swing** que permite ingresar la configuración de una base de datos (URL, usuario y contraseña), validar la conexión con **JDBC** y mostrar el resultado: en **verde** si la conexión es exitosa y en **rojo** si falla, junto con el mensaje técnico del error.

**Asignatura:** Programación Orientada a Objetos II · **Docente:** Leonardo Montes Gutiérrez · **Institución Universitaria Digital de Antioquia** · 2026

## Requisitos

- JDK 17 o superior.
- Sin frameworks externos. Solo se incluye el driver JDBC de SQLite (`lib/sqlite-jdbc-3.46.1.3.jar`), que es necesario para conectarse a archivos `.db`.

## Ejecutar

**Windows:** doble clic en `ejecutar.bat`, o desde la carpeta del proyecto:

```bat
javac -encoding UTF-8 -d out src\com\inventario\conexion\*.java
java -cp "out;lib\sqlite-jdbc-3.46.1.3.jar" com.inventario.conexion.ConexionTester
```

**macOS / Linux:** `./ejecutar.sh`, o:

```bash
javac -encoding UTF-8 -d out src/com/inventario/conexion/*.java
java -cp "out:lib/sqlite-jdbc-3.46.1.3.jar" com.inventario.conexion.ConexionTester
```

## Pruebas sugeridas

| URL | Usuario / contraseña | Resultado esperado |
|-----|----------------------|--------------------|
| `jdbc:sqlite:inventario.db` | vacíos | Verde: conexión exitosa |
| `jdbc:sqlite:Z:/carpeta_inexistente/inventario.db` | vacíos | Rojo: `SQLException` (fallo de conexión) |
| `jdbc:mysql://localhost:3306/inventario` | `root` / cualquiera | Rojo: `ClassNotFoundException` (falta el driver de MySQL) |
| `jdbc:h2:mem:inventario` | cualquiera | Rojo: `SQLException` (*No suitable driver*) |
| *(vacía)* | — | Naranja: aviso de datos incompletos |

Cada prueba queda registrada en `historial_conexiones.csv` (la contraseña nunca se guarda).

## Estructura

```
EA3_IncorporandoUI_BD/
├── src/com/inventario/conexion/
│   ├── ConexionTester.java         # JFrame: interfaz, ActionListener y presentación del resultado
│   ├── ServicioConexion.java       # JDBC: Class.forName, DriverManager.getConnection, try-with-resources
│   ├── ConfiguracionConexion.java  # record con URL, usuario y contraseña
│   ├── ResultadoConexion.java      # record con el tipo de resultado y el mensaje
│   └── HistorialCsv.java           # registro de pruebas en CSV
├── lib/sqlite-jdbc-3.46.1.3.jar    # driver JDBC de SQLite (Xerial)
├── evidencias/                     # capturas de ejecución e historial CSV de ejemplo
├── documentacion/                  # diagrama de clases
├── informe/                        # informe del grupo (PDF)
├── codigo_fuente.csv               # código fuente en formato CSV (archivo; línea; código)
├── ejecutar.bat / ejecutar.sh
└── README.md
```

## Integrantes

| Integrante | Repositorio |
|------------|-------------|
| Daniela Cataño Hernández | [_pendiente_](https://github.com/danielacatanoh-hue/EA3_IncorporandoUI_BD/blob/main/README.md) |
| Anderson Julián Valencia Estrada | _pendiente_ |

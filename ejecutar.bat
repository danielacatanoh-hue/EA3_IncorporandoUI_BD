@echo off
rem Compila y ejecuta la aplicacion (Windows). Requiere JDK 17 o superior.
chcp 65001 > nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out src\com\inventario\conexion\*.java
if errorlevel 1 (
  echo Error al compilar. Verifique que tiene instalado el JDK.
  pause
  exit /b 1
)
java --enable-native-access=ALL-UNNAMED -cp "out;lib\sqlite-jdbc-3.46.1.3.jar" com.inventario.conexion.ConexionTester

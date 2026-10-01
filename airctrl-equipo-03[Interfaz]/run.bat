@echo off
rem AIRCTRL - compila y abre la interfaz grafica (Windows).
rem Uso: run.bat  |  run.bat --csv  |  run.bat --auto
setlocal
cd /d "%~dp0"

set DRIVER_VERSION=42.7.4
set DRIVER=lib\postgresql-%DRIVER_VERSION%.jar
set DRIVER_URL=https://repo1.maven.org/maven2/org/postgresql/postgresql/%DRIVER_VERSION%/postgresql-%DRIVER_VERSION%.jar

if not exist lib mkdir lib
if not exist "%DRIVER%" (
  echo Descargando el driver JDBC de PostgreSQL ^(una sola vez^)...
  curl -fsSL -o "%DRIVER%" "%DRIVER_URL%"
  if errorlevel 1 (
    del /q "%DRIVER%" 2>nul
    echo No se pudo descargar el driver. Descargalo de:
    echo   %DRIVER_URL%
    echo y guardalo en la carpeta lib\. Mientras tanto puedes usar: run.bat --csv
  )
)

echo Compilando...
if exist bin rmdir /s /q bin
mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if errorlevel 1 (
  del sources.txt
  echo Error de compilacion.
  pause
  exit /b 1
)
del sources.txt

echo Abriendo AIRCTRL...
java -cp "bin;lib\*" airctrl.gui.AirCtrlApp %*
endlocal

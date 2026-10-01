# Integracion del primer avance

## Componentes incluidos

- Repositorio base y archivos de datos.
- Analisis del problema y alcance del sistema.
- Entidades principales del dominio aeroportuario.
- Lista simplemente enlazada.
- Cola enlazada.
- Pila enlazada.
- Cola de prioridades.
- Carga y validacion de cinco archivos CSV.
- Servicio de consultas, prioridades, historial y asignacion de puertas.
- Interfaz de consola con menu interactivo.
- Documentacion del diseno y las decisiones tecnicas.
- Pruebas de casos normales y casos limite.

## Flujo completo

El programa carga los archivos de la carpeta `data`, crea las entidades, valida sus relaciones y registra los vuelos pendientes de acuerdo con su prioridad. El operador puede consultar informacion, revisar pendientes, procesar la siguiente operacion y consultar el historial reciente.

## Ejecucion del programa

Desde la raiz del repositorio:

```text
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin Main
```

## Ejecucion de las pruebas

Desde la raiz del repositorio:

```text
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src tests -name "*.java")
java -cp bin tests.AllTests
```

El resultado esperado es:

```text
PASS: Queue order
PASS: Stack order
PASS: Priority order
PASS: Same priority order
PASS: Empty queue
PASS: Invalid priority
PASS: CSV loading
Passed tests: 7
```


# AIRCTRL - Centro de Operaciones Aeroportuarias

AIRCTRL es una aplicacion de consola en Java para apoyar la operacion interna de un aeropuerto. El sistema carga los archivos CSV proporcionados, valida sus relaciones y permite administrar vuelos, aeronaves, puertas, equipajes, incidentes y conexiones internas.

No es un sistema de reservaciones ni de pasajeros. Su objetivo es conservar el orden operativo, atender primero las situaciones urgentes y facilitar consultas sobre los datos del aeropuerto.

## Funcionalidades

- Carga y validacion de cinco archivos CSV.
- Registro y consulta de vuelos, aeronaves, equipajes e incidentes.
- Busqueda de vuelos por identificador, aerolinea y estado.
- Procesamiento de operaciones por prioridad y orden de llegada.
- Historial de operaciones procesadas.
- Asignacion de puertas con validacion de disponibilidad.
- Reportes de vuelos, puertas, equipajes e incidentes.
- Grafo de conexiones internas construido desde `connections.csv`.
- Recorrido BFS desde cualquier ubicacion valida.
- Calculo de ruta minima con el algoritmo de Dijkstra.
- Suite automatizada con casos normales y casos limite.

## Interfaz grafica (Swing + PostgreSQL local)

Ademas del menu de consola, el proyecto incluye una interfaz de escritorio en `src/airctrl/gui/`. Usa las mismas estructuras de datos y el mismo `AirCtrlSystem`: la interfaz solo presenta y valida, las reglas de negocio siguen en la capa de servicio. Todo corre en tu computadora; la unica conexion es a un PostgreSQL local.

### Secciones

- **Panel de control:** metricas del momento, la siguiente operacion de la cola con el motivo de su prioridad y el boton "Atender ahora".
- **Operaciones:** la cola completa por prioridad (5 a 1, FIFO dentro de cada nivel) y el historial como pila. Permite atender una o varias operaciones.
- **Vuelos:** busqueda exacta por la tabla hash, filtros por aerolinea y estado, registro de vuelos y asignacion de puertas.
- **Puertas:** mapa por terminal. Solo las puertas disponibles se pueden asignar; tambien se liberan, se mandan a mantenimiento o se cierran.
- **Aeronaves, Equipaje e Incidentes:** consulta y registro. El equipaje avanza solo por transiciones validas (Documentado, Cargado, En traslado, Entregado, Extraviado).
- **Rutas internas:** mapa del grafo de conexiones, ruta minima con Dijkstra y recorrido BFS paso a paso.
- **Reportes:** graficas y resumen, con exportacion a CSV.
- **Advertencias de datos:** registros de los CSV que se omitieron o tienen referencias incompletas.

Atajos: `Ctrl+1` a `Ctrl+9` (`Cmd` en macOS) para cambiar de seccion y `F5` para recargar.

### Requisitos

- Java 17 o superior (JDK, para compilar).
- PostgreSQL local (opcional). Sin base de datos la interfaz funciona en modo CSV, pero no guarda cambios.
- Driver JDBC de PostgreSQL. Los scripts lo descargan solos a `lib/` la primera vez.

### Preparar PostgreSQL

Solo necesitas un servidor corriendo y un usuario con permiso para crear bases. Al conectarse, AIRCTRL crea la base `airctrl` si no existe, crea las tablas y las llena con los CSV de `data/` la primera vez. El esquema de referencia esta en `sql/schema.sql` para revisarlo en DataGrip o psql.

Si no tienes PostgreSQL instalado puedes usar Docker:

```bash
docker compose up -d   # usuario postgres, contrasena airctrl
```

### Ejecutar

```bash
./run.sh           # macOS / Linux
run.bat            # Windows
```

Se abre la ventana de conexion (servidor, puerto, base, usuario y contrasena). Tambien puedes:

```bash
./run.sh --csv     # sin base de datos, solo lectura de los CSV
./run.sh --auto    # conecta directo con la configuracion guardada
```

O manualmente, con el driver en `lib/`:

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp "bin:lib/*" airctrl.gui.AirCtrlApp          # en Windows: -cp "bin;lib\*"
java -cp "bin:lib/*" Main --gui                     # equivalente desde Main
```

La conexion se guarda en `config/database.properties` (ignorado por git). La contrasena solo se guarda si marcas "Recordar contrasena"; tambien se puede dar con la variable de entorno `AIRCTRL_DB_PASSWORD`.

### Persistencia

Cada accion (atender una operacion, asignar o liberar puertas, registrar vuelos, equipaje o incidentes, cambiar estados) se valida primero en `AirCtrlSystem`, despues se guarda en PostgreSQL dentro de una transaccion y solo entonces se aplica en memoria. Al reabrir la aplicacion la cola, el historial y los estados se reconstruyen desde la base. El boton "Restablecer desde CSV" de la barra lateral vuelve a cargar los datos originales.

## Requisitos

- Java 17 o superior.
- Terminal ubicada en la raiz del repositorio.

No se utilizan dependencias externas.

## Compilacion

En macOS o Linux:

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```

## Ejecucion

Menu interactivo:

```bash
java -cp bin Main
```

Demostracion automatica:

```bash
java -cp bin Main --demo
```

## Pruebas

Compilar el codigo y las pruebas:

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src tests -name "*.java")
```

Ejecutar la suite:

```bash
java -cp bin tests.AllTests
```

La ejecucion correcta termina con:

```text
TOTAL: 34 | PASSED: 34 | FAILED: 0
```

## Datos utilizados

| Archivo | Registros recibidos | Uso |
|---|---:|---|
| `flights.csv` | 50 | Vuelos, estados y prioridades |
| `aircraft.csv` | 35 | Aeronaves y capacidades |
| `gates.csv` | 45 | Puertas y disponibilidad |
| `baggage.csv` | 140 | Equipajes asociados a vuelos |
| `connections.csv` | 35 | Conexiones internas del aeropuerto |

El sistema detecta dos aeronaves con capacidad cero, seis conexiones sin distancia y cuatro vuelos que referencian esas aeronaves invalidas. Los problemas se reportan como advertencias sin detener el programa.

## Estructura

```text
airctrl-equipo-03/
├── README.md
├── data/
├── docs/
│   ├── avance/
│   └── final/
├── src/
│   ├── Main.java
│   └── airctrl/
│       ├── data/
│       ├── graph/
│       ├── model/
│       ├── service/
│       ├── structure/
│       └── ui/
└── tests/
    └── AllTests.java
```

## Estructuras de datos

- Lista simplemente enlazada para conservar vuelos.
- Cola enlazada para mantener el orden FIFO.
- Pila enlazada para mostrar primero la operacion procesada mas reciente.
- Cola de prioridad para atender primero la mayor urgencia.
- Tabla hash propia para localizar vuelos por identificador.
- Mapas para indices y conteos.
- `TreeMap` para ordenar alfabeticamente los reportes.
- Grafo ponderado para representar conexiones internas.
- BFS para recorrer componentes conectados.
- Dijkstra para encontrar rutas de menor distancia.

## Documentacion final

- [Analisis del problema](docs/final/analisis-problema.md)
- [Analisis de datos](docs/final/analisis-datos.md)
- [Diseno y UML](docs/final/diseno-uml.md)
- [Decisiones tecnicas](docs/final/decisiones-tecnicas.md)
- [Pruebas](docs/final/pruebas.md)
- [Guia de pruebas manuales](docs/final/guia-pruebas-manuales.md)
- [Autoevaluacion](docs/final/autoevaluacion.md)

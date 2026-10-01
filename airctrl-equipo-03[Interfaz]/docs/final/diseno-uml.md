# Diseno completo y UML

## Arquitectura

- `model`: entidades del dominio y resultados de rutas.
- `structure`: estructuras implementadas durante el curso.
- `data`: lectura, validacion y almacenamiento de CSV.
- `graph`: representacion y algoritmos del mapa interno.
- `service`: reglas de negocio y coordinacion de estructuras.
- `ui`: menu de consola y presentacion de resultados.
- `Main.java`: punto de entrada y manejo de errores de carga.
- `tests`: suite automatizada independiente.

## Diagrama de clases

```mermaid
classDiagram
    class Main
    class ConsoleInterface {
        -AirCtrlSystem system
        +runDemo()
        +runInteractive()
    }
    class CsvRepository {
        -Path dataDirectory
        +load() DataStore
    }
    class DataStore {
        -SinglyLinkedList~Flight~ flights
        -Map aircraft
        -Map gates
        -Map baggage
        -Map incidents
        -List connections
        -List warnings
    }
    class AirCtrlSystem {
        -DataStore store
        -PriorityOperationQueue pending
        -LinkedStack history
        -HashTable flightIndex
        -AirportGraph airportGraph
        +findFlight(id) Flight
        +registerFlight(flight)
        +assignGate(flightId, gateId)
        +processNext() Operation
        +findShortestRoute(source, target) Route
        +traverseConnections(source) List
        +reports() Map
    }
    class AirportGraph {
        -Map adjacency
        +shortestRoute(source, target) Optional~Route~
        +breadthFirst(source) List
    }
    class Flight
    class Aircraft
    class Gate
    class Baggage
    class Connection
    class Incident
    class Operation
    class Route
    class SinglyLinkedList
    class LinkedQueue
    class LinkedStack
    class PriorityOperationQueue
    class HashTable

    Main --> CsvRepository
    Main --> AirCtrlSystem
    Main --> ConsoleInterface
    ConsoleInterface --> AirCtrlSystem
    CsvRepository --> DataStore
    DataStore o-- Flight
    DataStore o-- Aircraft
    DataStore o-- Gate
    DataStore o-- Baggage
    DataStore o-- Connection
    DataStore o-- Incident
    AirCtrlSystem --> DataStore
    AirCtrlSystem --> AirportGraph
    AirCtrlSystem --> PriorityOperationQueue
    AirCtrlSystem --> LinkedStack
    AirCtrlSystem --> HashTable
    AirportGraph --> Connection
    AirportGraph --> Route
    PriorityOperationQueue o-- LinkedQueue
    Operation --> Flight
    Operation --> Incident
```

## Flujo de datos

```text
CSV -> CsvRepository -> DataStore -> AirCtrlSystem -> ConsoleInterface
                                  -> AirportGraph
                                  -> Indices y estructuras operativas
```

## Separacion de responsabilidades

La interfaz no lee archivos ni modifica colecciones directamente. El repositorio no muestra informacion al usuario. Las estructuras son genericas y no dependen del dominio. El servicio concentra las validaciones operativas y el grafo concentra sus algoritmos.

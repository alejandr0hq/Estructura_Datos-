# Datos de trabajo

Los archivos están en la carpeta [`data/`](../data/) en formato CSV, UTF-8, primera fila = encabezados.

## 📄 flights.csv — Itinerario de vuelos

```
flight_id,airline,origin,destination,aircraft_id,gate,status,priority
AM101,AirMex,MEX,TIJ,AC001,G12,BOARDING,2
AM205,AirMex,MEX,CUN,AC014,G08,DELAYED,3
VB410,Viva,MEX,MTY,AC022,G03,BOARDING,2
XA901,CargoAir,MEX,GDL,AC031,G20,EMERGENCY,5
```

| Campo | Notas |
|---|---|
| `flight_id` | Único, código de vuelo (AM101, VB410, ...) |
| `airline` | Aerolínea |
| `origin` / `destination` | Códigos IATA de aeropuerto |
| `aircraft_id` | Referencia a `aircraft.csv` |
| `gate` | Puerta asignada (referencia a `gates.csv`) |
| `status` | SCHEDULED, BOARDING, DEPARTED, DELAYED, EMERGENCY, LANDED |
| `priority` | 1–5 (ver reglas-negocio) |

El dataset mezcla prioridades bajas con emergencias reales: la operación no llega ordenada.

## 📄 aircraft.csv — Flota de aeronaves

```
aircraft_id,model,capacity,status
AC001,Boeing 737-800,186,ACTIVE
```

| Campo | Notas |
|---|---|
| `aircraft_id` | Único, formato `ACnnn` |
| `model` | Modelo de la aeronave |
| `capacity` | Pasajeros máximo |
| `status` | ACTIVE, MAINTENANCE, RETIRED |

## 📄 gates.csv — Puertas del aeropuerto

```
gate_id,terminal,status,current_flight
G01,T1,AVAILABLE,
G12,T1,OCCUPIED,AM101
```

| Campo | Notas |
|---|---|
| `gate_id` | Único, formato `Gnn` |
| `terminal` | Terminal (T1, T2, T3) |
| `status` | AVAILABLE, OCCUPIED, MAINTENANCE, CLOSED |
| `current_flight` | Vuelo que la ocupa (**vacío si está libre**) |

Hay puertas en **todos los estados**: el sistema debe respetar las reglas de asignación en cada caso.

## 📄 baggage.csv — Equipaje

```
bag_id,flight_id,passenger_code,status
BAG0001,AM101,PAX0001,CHECKED
```

| Campo | Notas |
|---|---|
| `bag_id` | Único, formato `BAGnnnn` |
| `flight_id` | Vuelo al que pertenece |
| `passenger_code` | Código anónimo de pasajero |
| `status` | CHECKED, LOADED, IN_TRANSIT, DELIVERED, MISSING |

El dataset incluye equipajes `DELIVERED` (que ya no deben aparecer como pendientes) y `MISSING` (que sí requieren seguimiento).

## 📄 connections.csv — Conexiones internas del aeropuerto

```
source_location,target_location,distance_m
G12,BELT03,180
```

| Campo | Notas |
|---|---|
| `source_location` | Punto de origen (puerta, banda, zona) |
| `target_location` | Punto destino |
| `distance_m` | Distancia en metros |

Representa el mapa interno del aeropuerto: puertas, bandas de equipaje, zonas de rampa y áreas operativas conectadas entre sí. Será especialmente relevante en fases avanzadas para analizar rutas internas.

## Relaciones entre archivos

```
flights.csv ──(aircraft_id)──► aircraft.csv
flights.csv ──(gate)─────────► gates.csv ──(current_flight)──► flights.csv
baggage.csv ──(flight_id)────► flights.csv
connections.csv ──► ubicaciones internas (puertas, bandas, zonas)
```

## ¿Qué deben demostrar con estos datos?

Que comprenden las cinco entidades, sus relaciones cruzadas (¡las puertas apuntan de vuelta a los vuelos!), y dónde están escondidos los casos límite: emergencias, puertas ocupadas y equipajes ya entregados.

# Reglas de negocio

## Reglas de prioridad

| Prioridad | Nivel | Ejemplos |
|---|---|---|
| 1 | Normal | Operación rutinaria |
| 2 | Operación importante | Abordaje en curso, preparación de vuelo |
| 3 | Retraso | Vuelo con retraso que afecta conexiones |
| 4 | Incidente grave | Problema de equipaje masivo, falla de aeronave |
| 5 | Emergencia | Emergencia médica, seguridad, pista cerrada |

**Regla:** un vuelo con prioridad 5 debe recibir atención antes que uno con prioridad 1. En general, mayor número = mayor urgencia.

### Desempate

Cuando dos situaciones comparten prioridad, el equipo define y documenta un criterio (por ejemplo, la más antigua primero). La decisión debe ser consistente y estar justificada.

## Reglas de puertas

Estados posibles de una puerta:

```
AVAILABLE
OCCUPIED
MAINTENANCE
CLOSED
```

**Regla:** **no puede asignarse un vuelo** a una puerta en estado:

- `OCCUPIED` — ya hay un vuelo usándola (`current_flight` lo registra)
- `MAINTENANCE` — fuera de servicio programado
- `CLOSED` — cerrada por operación o emergencia

Solo una puerta `AVAILABLE` acepta asignación. Al asignar, la puerta pasa a `OCCUPIED` y su campo `current_flight` se actualiza; al liberar, vuelve a `AVAILABLE`.

## Reglas de equipaje

Estados posibles de un equipaje:

```
CHECKED → LOADED → IN_TRANSIT → DELIVERED
                     │
                     └──► MISSING
```

| Estado | Significado |
|---|---|
| `CHECKED` | Registrado en mostrador |
| `LOADED` | Cargado a la aeronave |
| `IN_TRANSIT` | En traslado interno / conexión |
| `DELIVERED` | Entregado al pasajero |
| `MISSING` | Extraviado |

**Regla:** un equipaje `DELIVERED` **no debe volver a aparecer como pendiente**: su ciclo terminó. Lo mismo aplica para los procesados correctamente. El sistema mantiene pendientes únicamente los equipajes cuyo flujo no ha concluido.

## Consistencia referencial

- Todo vuelo debería referir a una aeronave existente; si no existe, reportarlo sin caer.
- Todo equipaje pertenece a un vuelo; un equipaje huérfano es un incidente de datos, no un crash.
- Una puerta ocupada siempre registra qué vuelo la ocupa.

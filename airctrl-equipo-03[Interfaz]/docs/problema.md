# Problema

El aeropuerto actualmente recibe información de múltiples áreas: torre de control, rampa, equipaje, seguridad y mantenimiento. Todo llega mezclado al centro de operaciones.

El operador necesita responder rápidamente:

| Pregunta | ¿Qué implica? |
|---|---|
| ¿Qué vuelo necesita atención? | Ordenar lo pendiente por urgencia real, no por llegada |
| ¿Qué puerta está asignada? | Estado de puertas consistente con los vuelos |
| ¿Qué aeronave está involucrada? | Relación vuelo → aeronave siempre consultable |
| ¿Qué operaciones están pendientes? | Separar claramente pendiente de ya procesado |
| ¿Qué conexiones existen? | Rutas internas entre puertas, bandas y zonas |
| ¿Qué ocurre si una pista queda cerrada? | Anticipar el efecto sobre la operación completa |

## Las tres tensiones del problema

### 1. Urgencia vs. orden de llegada

Los eventos llegan en cualquier orden. Un cambio de puerta rutinario (prioridad 2) puede llegar cinco minutos antes que una emergencia médica (prioridad 5). Si el sistema procesa en orden de llegada, la emergencia espera detrás del volumen rutinario — inaceptable en un aeropuerto.

### 2. Recursos compartidos

Las puertas son finitas y compartidas: si `G12` está ocupada por `AM101`, ningún otro vuelo puede usarla hasta que se libere. El estado de las puertas debe ser **siempre** consistente con el estado de los vuelos: una puerta ocupada sabe quién la ocupa; una puerta libre no aparece asignada a nadie.

### 3. Flujos que terminan... y flujos que no

Un equipaje entregado (`DELIVERED`) terminó su ciclo: no debe seguir apareciendo como pendiente. Pero uno extraviado (`MISSING`) exige seguimiento activo. El sistema debe distinguir qué está "en proceso", qué "terminó" y qué "se rompió" para cada tipo de recurso.

## Lo que el sistema debe permitir

Registrar y consultar vuelos, aeronaves, puertas y equipaje; asignar recursos respetando sus reglas; registrar incidencias; procesar la operación pendiente más urgente en cada momento; conservar el historial completo; analizar conexiones internas y generar reportes operativos.

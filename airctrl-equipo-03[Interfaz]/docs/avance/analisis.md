# Analisis del problema

AIRCTRL apoya al centro de operaciones de un aeropuerto. El sistema conserva informacion de vuelos, aeronaves, puertas, equipaje y conexiones para que los operadores puedan consultar el estado actual y atender eventos pendientes.

El problema principal consiste en procesar varios eventos sin perder su prioridad ni el orden en que llegaron. Un evento de prioridad alta debe atenderse antes que uno rutinario. Cuando dos eventos tienen la misma prioridad se conserva el orden de llegada.

El primer flujo completo carga los cinco archivos CSV, relaciona sus registros, crea operaciones para los vuelos pendientes, muestra la siguiente operacion y permite procesarla. La operacion procesada pasa al historial reciente.

Los usuarios principales son los operadores del centro de control. Necesitan consultar vuelos y equipajes, revisar operaciones pendientes, procesar la siguiente operacion y asignar puertas disponibles.

El sistema no administra reservaciones, boletos ni datos personales de pasajeros.

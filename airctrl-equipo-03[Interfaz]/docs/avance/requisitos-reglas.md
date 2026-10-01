# Requisitos y reglas implementadas

## Requisitos funcionales

- Cargar vuelos, aeronaves, puertas, equipajes y conexiones desde archivos CSV.
- Mostrar un resumen de los datos cargados.
- Consultar un vuelo mediante su identificador.
- Consultar un equipaje mediante su identificador.
- Mostrar las operaciones pendientes en orden de atencion.
- Procesar la siguiente operacion pendiente.
- Consultar el historial de operaciones procesadas.
- Asignar una puerta disponible a un vuelo.
- Informar las inconsistencias encontradas durante la carga de datos.

## Reglas de negocio


- Las prioridades validas se encuentran entre 1 y 5.
- La prioridad 5 representa el nivel de atencion mas alto.
- Una operacion de mayor prioridad se procesa antes que una de menor prioridad.
- Las operaciones con la misma prioridad conservan su orden de llegada.
- Un vuelo finalizado o cancelado no se agrega a las operaciones pendientes.
- No se puede registrar dos veces el mismo vuelo.
- La aeronave asignada a un vuelo debe existir.
- La puerta asignada a un vuelo debe existir y estar disponible.
- Al cambiar la puerta de un vuelo se libera la puerta anterior cuando corresponde.
- Una busqueda inexistente informa el problema sin cerrar el programa.

## Casos limite considerados

- Cola de operaciones vacia.
- Identificador inexistente o escrito con espacios.
- Prioridad fuera del intervalo permitido.
- Vuelo duplicado.
- Puerta inexistente, ocupada o fuera de servicio.
- Referencias invalidas entre archivos CSV.
- Campos numericos vacios o invalidos.

## Pruebas implementadas

Las pruebas se encuentran en `tests/AllTests.java` y comprueban:

- Orden de salida de la cola.
- Orden de salida de la pila.
- Atencion de la prioridad mayor.
- Orden de llegada dentro de una misma prioridad.
- Comportamiento de una cola vacia.
- Rechazo de una prioridad invalida.
- Carga de los cinco archivos CSV.

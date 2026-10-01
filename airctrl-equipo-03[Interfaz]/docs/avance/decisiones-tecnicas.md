# Decisiones tecnicas

## Lista simplemente enlazada

Los vuelos se conservan en una lista simplemente enlazada porque el conjunto puede crecer durante la operacion. La insercion al final es constante gracias a las referencias al primer y ultimo nodo. El recorrido y la busqueda secuencial son lineales.

## Cola enlazada

Cada nivel de prioridad usa una cola enlazada. La primera operacion que entra es la primera que sale dentro de su nivel. Insertar, consultar el frente y retirar el frente son operaciones constantes.

## Cola de prioridades

Las prioridades validas van de 1 a 5. La estructura mantiene una cola por nivel y revisa primero el nivel 5. Esto permite atender antes los eventos urgentes y conservar el orden de llegada cuando la prioridad es igual.

## Pila enlazada

El historial reciente usa una pila porque la ultima operacion procesada debe aparecer primero. Agregar, consultar y retirar el elemento superior son operaciones constantes.

## Mapas por identificador

Las aeronaves, puertas y equipajes se almacenan en mapas para localizarlos mediante su identificador sin recorrer todos los registros. El servicio tambien crea un indice de vuelos para las consultas frecuentes.

## Separacion de responsabilidades

La lectura de archivos se concentra en `data`, las reglas se aplican en `service` y la comunicacion con el usuario se mantiene en `ui`. Las estructuras no dependen de la interfaz ni de los archivos CSV.

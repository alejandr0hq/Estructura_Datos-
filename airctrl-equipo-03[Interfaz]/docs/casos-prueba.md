# Casos de prueba iniciales

Estos casos deben funcionar antes de la primera entrega. Cada caso indica acción, resultado esperado y qué valida.

---

## Caso 1 — Vuelo normal

**Acción:** registrar:

```
AM101
priority = 2
```

**Resultado esperado:** el vuelo se agrega correctamente al sistema y queda visible como operación pendiente de prioridad normal.

**Valida:** registro de vuelos + entrada a la operación diaria.

---

## Caso 2 — Emergencia

**Acción:** registrar:

```
XA901
priority = 5
```

**Resultado esperado:** XA901 tiene **prioridad superior** y aparece antes que AM101 en el orden de atención, aunque hubiera llegado después.

**Valida:** prioridad sobre orden de llegada.

---

## Caso 3 — Puerta ocupada

**Acción:** intentar asignar un vuelo a una puerta:

```
G12 = OCCUPIED
```

**Resultado esperado:** la operación **se rechaza** con mensaje claro: *"La puerta G12 no está disponible (OCCUPIED)"*. El estado no cambia.

**Valida:** reglas de asignación de puertas.

---

## Caso 4 — Equipaje inexistente

**Acción:** buscar:

```
BAG999
```

**Resultado esperado:** mensaje claro tipo *"Equipaje no encontrado"*. El sistema **no** debe lanzar un error inesperado.

**Valida:** manejo controlado de búsquedas sin resultados.

---

## Caso 5 — Vuelo inexistente

**Acción:** consultar:

```
ZZ999
```

**Resultado esperado:** el sistema muestra que no existe ese vuelo, con un mensaje claro y sin comportamiento indefinido.

**Valida:** manejo controlado de referencias rotas.

---

## Casos límite adicionales

Recuerda los casos límite listados en [`requisitos.md`](requisitos.md): cola vacía, vuelos duplicados, equipajes huérfanos, aeronaves inexistentes y desempates por prioridad. Cada uno debe convertirse en una prueba con resultado esperado.

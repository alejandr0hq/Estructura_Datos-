package airctrl.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PriorityOperationQueue<T> implements Iterable<T> {
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 5;

    private final List<LinkedQueue<T>> queues = new ArrayList<>(MAX_PRIORITY + 1);
    private int size;

    public PriorityOperationQueue() {
        for (int index = 0; index <= MAX_PRIORITY; index++) {
            queues.add(new LinkedQueue<>());
        }
    }

    public void enqueue(T value, int priority) {
        validatePriority(priority);
        queues.get(priority).enqueue(value);
        size++;
    }

    public T dequeue() {
        int priority = highestAvailablePriority();
        T value = queues.get(priority).dequeue();
        size--;
        return value;
    }

    public T peek() {
        return queues.get(highestAvailablePriority()).peek();
    }

    public int countAt(int priority) {
        validatePriority(priority);
        return queues.get(priority).size();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        List<T> ordered = new ArrayList<>(size);
        for (int priority = MAX_PRIORITY; priority >= MIN_PRIORITY; priority--) {
            for (T value : queues.get(priority)) {
                ordered.add(value);
            }
        }
        return ordered.iterator();
    }

    private int highestAvailablePriority() {
        for (int priority = MAX_PRIORITY; priority >= MIN_PRIORITY; priority--) {
            if (!queues.get(priority).isEmpty()) {
                return priority;
            }
        }
        throw new EmptyStructureException("No hay operaciones pendientes");
    }

    private static void validatePriority(int priority) {
        if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 y 5");
        }
    }
}

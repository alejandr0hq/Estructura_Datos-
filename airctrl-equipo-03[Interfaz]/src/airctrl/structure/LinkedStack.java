package airctrl.structure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class LinkedStack<T> implements Iterable<T> {
    private Node<T> top;
    private int size;

    public void push(T value) {
        top = new Node<>(value, top);
        size++;
    }

    public T pop() {
        if (top == null) {
            throw new EmptyStructureException("No hay elementos en la pila");
        }
        T value = top.value;
        top = top.next;
        size--;
        return value;
    }

    public T peek() {
        if (top == null) {
            throw new EmptyStructureException("No hay elementos en la pila");
        }
        return top.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = top;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }
}

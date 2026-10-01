package airctrl.structure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class LinkedQueue<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public T dequeue() {
        if (head == null) {
            throw new EmptyStructureException("No hay elementos en la cola");
        }
        T value = head.value;
        head = head.next;
        size--;
        if (head == null) {
            tail = null;
        }
        return value;
    }

    public T peek() {
        if (head == null) {
            throw new EmptyStructureException("No hay elementos en la cola");
        }
        return head.value;
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
            private Node<T> current = head;

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

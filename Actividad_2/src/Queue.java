

public class Queue {
    private Node front;
    private Node rear;
    private int count;

    public Queue() {
        front = null;
        rear = null;
        count = 0;
    }

    public void enqueue(String data) {
        Node newNode = new Node(data);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }

        count++;
    }

    public String dequeue() {
        if (isEmpty()) {
            return null;

        }

        String data = front.data;
        front = front.next;
        count--;

        if (front == null) {
            rear = null;
        }

        return data;
    }

    public String peek() {
        return isEmpty() ? null : front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return count;
    }

    public void display() {
        if (isEmpty()) {
            System.out.println("No hay tareas pendientes.");
            return;
        }

        System.out.println("FRONT");
        System.out.println("  |");
        System.out.println("  v");

        Node current = front;

        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }

        System.out.println("  ^");
        System.out.println("  |");
        System.out.println("REAR");

    }

}
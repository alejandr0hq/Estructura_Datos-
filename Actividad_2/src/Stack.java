

public class Stack {
    private Node top;
    private int count;

    public Stack() {
        top = null;
        count = 0;
    }

    public void push(String data) {
        Node newNode = new Node(data);
        newNode.next = top;
        top = newNode;
        count++;
    }

    public String pop() {
        if (isEmpty()) {
            return null;
        }

        String data = top.data;
        top = top.next;
        count--;

        return data;
    }

    public String peek() {
        return isEmpty() ? null : top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return count;
    }

    public void display() {
        if (isEmpty()) {
            System.out.println("El historial de acciones esta vacio.");
            return;
        }

        System.out.println("TOP");
        System.out.println(" |");
        System.out.println(" v");

        Node current = top;

        while (current != null) {
            System.out.println(current.data);
            current = current.next;

        }

    }

    
}
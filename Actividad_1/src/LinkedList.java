public class LinkedList {
    private Node head;
    private int size;

    public LinkedList() {
        head = null;
        size = 0;
    }

    public void insertAtBeginning(Book book) {
        Node newNode = new Node(book);
        newNode.setNext(head);
        head = newNode;
        size++;
    }

    public void insertAtEnd(Book book) {
        if (head == null) {
            insertAtBeginning(book);
            return;
        }

        Node current = head;

        while (current.getNext() != null) {
            current = current.getNext();
        }

        current.setNext(new Node(book));
        size++;
    }

    public boolean insertAtPosition(Book book, int position) {
        if (position < 0 || position > size) {
            return false;
        }

        if (position == 0) {
            insertAtBeginning(book);
            return true;
        }

        if (position == size) {
            insertAtEnd(book);
            return true;
        }

        Node current = head;

        for (int i = 0; i < position - 1; i++) {
            current = current.getNext();
        }

        Node newNode = new Node(book);
        newNode.setNext(current.getNext());
        current.setNext(newNode);
        size++;

        return true;
    }

    public void display() {
        if (head == null) {
            System.out.println("La biblioteca está vacía.");
            return;
        }

        Node current = head;
        int position = 0;

        while (current != null) {
            System.out.println(position + " -> " + current.getData());
            current = current.getNext();
            position++;
        }
    }

    public Book searchByCode(String code) {
        Node current = head;

        while (current != null) {
            if (current.getData().getCode().equalsIgnoreCase(code)) {
                return current.getData();
            }

            current = current.getNext();
        }

        return null;
    }

    public Book getByPosition(int position) {
        if (position < 0 || position >= size) {
            return null;
        }

        Node current = head;

        for (int i = 0; i < position; i++) {
            current = current.getNext();
        }

        return current.getData();
    }

    public boolean deleteByCode(String code) {
        if (head == null) {
            return false;
        }

        if (head.getData().getCode().equalsIgnoreCase(code)) {
            head = head.getNext();
            size--;
            return true;
        }

        Node current = head;

        while (current.getNext() != null) {
            if (current.getNext().getData().getCode()
                    .equalsIgnoreCase(code)) {

                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    public boolean deleteByPosition(int position) {
        if (position < 0 || position >= size) {
            return false;
        }

        if (position == 0) {
            head = head.getNext();
            size--;
            return true;
        }

        Node current = head;

        for (int i = 0; i < position - 1; i++) {
            current = current.getNext();
        }

        current.setNext(current.getNext().getNext());
        size--;

        return true;
    }

    public int getSize() {
        return size;
    }
}
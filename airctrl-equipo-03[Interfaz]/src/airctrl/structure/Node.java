package airctrl.structure;

final class Node<T> {
    final T value;
    Node<T> next;

    Node(T value) {
        this.value = value;
    }

    Node(T value, Node<T> next) {
        this.value = value;
        this.next = next;
    }
}

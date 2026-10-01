package airctrl.structure;

import java.util.Objects;

public final class HashTable<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final double MAX_LOAD_FACTOR = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    public HashTable() {
        buckets = createBuckets(INITIAL_CAPACITY);
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "La llave no puede ser nula");
        int index = indexFor(key, buckets.length);
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }
        buckets[index] = new Entry<>(key, value, buckets[index]);
        size++;
        if ((double) size / buckets.length > MAX_LOAD_FACTOR) {
            resize();
        }
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        Entry<K, V> current = buckets[indexFor(key, buckets.length)];
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void resize() {
        Entry<K, V>[] previous = buckets;
        buckets = createBuckets(previous.length * 2);
        for (Entry<K, V> entry : previous) {
            Entry<K, V> current = entry;
            while (current != null) {
                Entry<K, V> next = current.next;
                int index = indexFor(current.key, buckets.length);
                current.next = buckets[index];
                buckets[index] = current;
                current = next;
            }
        }
    }

    private static int indexFor(Object key, int capacity) {
        int hash = key.hashCode();
        hash ^= hash >>> 16;
        return hash & (capacity - 1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <K, V> Entry<K, V>[] createBuckets(int capacity) {
        return (Entry<K, V>[]) new Entry[capacity];
    }

    private static final class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        private Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}

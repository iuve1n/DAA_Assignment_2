public class LinkedList {
    private Node head;
    private Node tail;
    private int size;
    private long accessCount;
    private long comparisonCount;

    public LinkedList() {
    }

    public LinkedList(int[] values) {
        for (int value : values) {
            add(value);
        }

        resetMetrics();
    }

    public void add(int value) {
        Node newNode = new Node(value);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }

        size++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);

        if (index == size) {
            add(value);
            return;
        }

        Node newNode = new Node(value);

        if (index == 0) {
            newNode.next = head;
            head = newNode;
        } else {
            Node previous = nodeAt(index - 1);
            newNode.next = previous.next;
            previous.next = newNode;
        }

        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        Node removedNode;

        if (index == 0) {
            removedNode = head;
            head = head.next;
            accessCount++;

            if (size == 1) {
                tail = null;
            }
        } else {
            Node previous = nodeAt(index - 1);
            removedNode = previous.next;
            accessCount++;
            previous.next = removedNode.next;

            if (removedNode == tail) {
                tail = previous;
            }
        }

        size--;
        return removedNode.value;
    }

    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int value) {
        Node current = head;

        while (current != null) {
            accessCount++;
            comparisonCount++;

            if (current.value == value) {
                return true;
            }

            current = current.next;
        }

        return false;
    }

    public int size() {
        return size;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public void resetMetrics() {
        accessCount = 0;
        comparisonCount = 0;
    }

    private Node nodeAt(int index) {
        Node current = head;

        for (int i = 0; i < index; i++) {
            accessCount++;
            current = current.next;
        }

        accessCount++;
        return current;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private static class Node {
        private final int value;
        private Node next;

        private Node(int value) {
            this.value = value;
        }
    }
}

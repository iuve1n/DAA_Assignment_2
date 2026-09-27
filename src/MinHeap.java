import java.util.NoSuchElementException;

public class MinHeap {
    private static final int DEFAULT_CAPACITY = 10;

    private int[] heap;
    private int size;
    private long comparisonCount;

    public MinHeap() {
        heap = new int[DEFAULT_CAPACITY];
    }

    public MinHeap(int[] values) {
        heap = new int[Math.max(DEFAULT_CAPACITY, values.length)];

        for (int value : values) {
            insert(value);
        }

        resetMetrics();
    }

    public void insert(int value) {
        ensureCapacity();
        heap[size] = value;
        int currentIndex = size;
        size++;

        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;
            comparisonCount++;

            if (heap[parentIndex] <= heap[currentIndex]) {
                break;
            }

            swap(parentIndex, currentIndex);
            currentIndex = parentIndex;
        }
    }

    public int peekMin() {
        checkNotEmpty();
        return heap[0];
    }

    public int extractMin() {
        checkNotEmpty();
        int minimum = heap[0];
        size--;

        if (size > 0) {
            heap[0] = heap[size];
            siftDown();
        }

        return minimum;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public void resetMetrics() {
        comparisonCount = 0;
    }

    public boolean isValidHeap() {
        for (int i = 0; i < size; i++) {
            int leftChild = i * 2 + 1;
            int rightChild = i * 2 + 2;

            if (leftChild < size && heap[i] > heap[leftChild]) {
                return false;
            }

            if (rightChild < size && heap[i] > heap[rightChild]) {
                return false;
            }
        }

        return true;
    }

    private void siftDown() {
        int currentIndex = 0;

        while (true) {
            int leftChild = currentIndex * 2 + 1;

            if (leftChild >= size) {
                return;
            }

            int rightChild = leftChild + 1;
            int smallerChild = leftChild;

            if (rightChild < size) {
                comparisonCount++;

                if (heap[rightChild] < heap[leftChild]) {
                    smallerChild = rightChild;
                }
            }

            comparisonCount++;

            if (heap[currentIndex] <= heap[smallerChild]) {
                return;
            }

            swap(currentIndex, smallerChild);
            currentIndex = smallerChild;
        }
    }

    private void ensureCapacity() {
        if (size < heap.length) {
            return;
        }

        int[] largerHeap = new int[heap.length * 2];

        for (int i = 0; i < size; i++) {
            largerHeap[i] = heap[i];
        }

        heap = largerHeap;
    }

    private void swap(int firstIndex, int secondIndex) {
        int temporary = heap[firstIndex];
        heap[firstIndex] = heap[secondIndex];
        heap[secondIndex] = temporary;
    }

    private void checkNotEmpty() {
        if (size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }
    }
}

public class DynamicArray {
    private static final int DEFAULT_CAPACITY = 10;

    private int[] arr;
    private int size;
    private long accessCount;
    private long comparisonCount;
    private long movementCount;

    public DynamicArray() {
        arr = new int[DEFAULT_CAPACITY];
    }

    public DynamicArray(int[] values) {
        int capacity = Math.max(DEFAULT_CAPACITY, values.length);
        arr = new int[capacity];

        for (int i = 0; i < values.length; i++) {
            arr[i] = values[i];
        }

        size = values.length;
    }

    public void add(int value) {
        ensureCapacity();
        arr[size] = value;
        size++;
        movementCount++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);
        ensureCapacity();

        for (int i = size; i > index; i--) {
            arr[i] = arr[i - 1];
            movementCount++;
        }

        arr[index] = value;
        size++;
        movementCount++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removedValue = arr[index];
        accessCount++;

        for (int i = index; i < size - 1; i++) {
            arr[i] = arr[i + 1];
            movementCount++;
        }

        size--;
        return removedValue;
    }

    public int get(int index) {
        checkElementIndex(index);
        accessCount++;
        return arr[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            comparisonCount++;

            if (arr[i] == value) {
                return true;
            }
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

    public long getMovementCount() {
        return movementCount;
    }

    public void resetMetrics() {
        accessCount = 0;
        comparisonCount = 0;
        movementCount = 0;
    }

    private void ensureCapacity() {
        if (size < arr.length) {
            return;
        }

        int newCapacity = arr.length == 0 ? DEFAULT_CAPACITY : arr.length * 2;
        int[] newArray = new int[newCapacity];

        for (int i = 0; i < size; i++) {
            newArray[i] = arr[i];
            movementCount++;
        }

        arr = newArray;
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
}

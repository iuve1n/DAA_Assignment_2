import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public class Tests {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        run("DynamicArray basic cases", Tests::testDynamicArrayBasicCases);
        run("DynamicArray randomized and large cases", Tests::testDynamicArrayRandomized);
        run("LinkedList basic cases", Tests::testLinkedListBasicCases);
        run("LinkedList randomized and large cases", Tests::testLinkedListRandomized);
        run("MinHeap basic cases", Tests::testMinHeapBasicCases);
        run("MinHeap reference and large cases", Tests::testMinHeapAgainstPriorityQueue);

        System.out.println("Passed: " + passed + ", failed: " + failed);

        if (failed > 0) {
            throw new AssertionError("Some tests failed");
        }
    }

    private static void testDynamicArrayBasicCases() {
        DynamicArray array = new DynamicArray();
        check(array.size() == 0, "new array should be empty");
        check(!array.contains(5), "empty array should not contain values");
        expectException(IndexOutOfBoundsException.class, () -> array.get(0));
        expectException(IndexOutOfBoundsException.class, () -> array.remove(0));
        expectException(IndexOutOfBoundsException.class, () -> array.add(1, 5));

        array.add(8);
        check(array.get(0) == 8, "one element was not stored");
        check(array.remove(0) == 8, "one element was not removed");
        check(array.size() == 0, "array should be empty after removal");

        DynamicArray values = new DynamicArray(new int[] { 2, 2, 4 });
        values.add(0, 1);
        values.add(2, 3);
        values.add(values.size(), 5);
        check(values.get(0) == 1, "insertion at the beginning failed");
        check(values.get(2) == 3, "insertion in the middle failed");
        check(values.get(values.size() - 1) == 5, "insertion at the end failed");
        check(values.contains(2), "duplicate value was not found");
        check(values.remove(1) == 2, "duplicate removal failed");
        expectException(IndexOutOfBoundsException.class, () -> values.get(-1));
        expectException(IndexOutOfBoundsException.class, () -> values.get(values.size()));

        values.resetMetrics();
        values.get(0);
        values.contains(100);
        check(values.getAccessCount() == 1, "get should count one access");
        check(values.getComparisonCount() == values.size(), "search comparisons are wrong");
        values.add(0, 9);
        check(values.getMovementCount() > 0, "insertion should count movements");
    }

    private static void testDynamicArrayRandomized() {
        DynamicArray array = new DynamicArray();
        ArrayList<Integer> reference = new ArrayList<>();
        Random random = new Random(42);

        for (int step = 0; step < 3000; step++) {
            int operation = random.nextInt(4);
            int value = random.nextInt(100);

            if (reference.isEmpty() || operation == 0) {
                array.add(value);
                reference.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(reference.size() + 1);
                array.add(index, value);
                reference.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(reference.size());
                check(array.remove(index) == reference.remove(index), "random removal differs");
            } else {
                int index = random.nextInt(reference.size());
                check(array.get(index) == reference.get(index), "random access differs");
            }

            check(array.size() == reference.size(), "randomized size differs");
        }

        for (int i = 0; i < reference.size(); i++) {
            check(array.get(i) == reference.get(i), "randomized contents differ");
        }

        DynamicArray large = new DynamicArray();
        for (int i = 0; i < 100000; i++) {
            large.add(i);
        }
        check(large.size() == 100000, "large array has the wrong size");
        check(large.get(0) == 0, "large array first value is wrong");
        check(large.get(50000) == 50000, "large array middle value is wrong");
        check(large.get(99999) == 99999, "large array last value is wrong");
    }

    private static void testLinkedListBasicCases() {
        LinkedList list = new LinkedList();
        check(list.size() == 0, "new list should be empty");
        check(!list.contains(5), "empty list should not contain values");
        expectException(IndexOutOfBoundsException.class, () -> list.get(0));
        expectException(IndexOutOfBoundsException.class, () -> list.remove(0));
        expectException(IndexOutOfBoundsException.class, () -> list.add(1, 5));

        list.add(8);
        check(list.get(0) == 8, "one node was not stored");
        check(list.remove(0) == 8, "one node was not removed");
        list.add(9);
        check(list.get(0) == 9, "list did not recover after becoming empty");

        LinkedList values = new LinkedList(new int[] { 2, 2, 4 });
        values.add(0, 1);
        values.add(2, 3);
        values.add(values.size(), 5);
        check(values.get(0) == 1, "insertion at the beginning failed");
        check(values.get(2) == 3, "insertion in the middle failed");
        check(values.get(values.size() - 1) == 5, "insertion at the end failed");
        check(values.contains(2), "duplicate value was not found");
        check(values.remove(1) == 2, "duplicate removal failed");
        expectException(IndexOutOfBoundsException.class, () -> values.get(-1));
        expectException(IndexOutOfBoundsException.class, () -> values.get(values.size()));

        values.resetMetrics();
        values.get(values.size() - 1);
        check(values.getAccessCount() == values.size(), "get should count traversed nodes");
        values.resetMetrics();
        values.contains(100);
        check(values.getAccessCount() == values.size(), "search accesses are wrong");
        check(values.getComparisonCount() == values.size(), "search comparisons are wrong");
    }

    private static void testLinkedListRandomized() {
        LinkedList list = new LinkedList();
        java.util.LinkedList<Integer> reference = new java.util.LinkedList<>();
        Random random = new Random(42);

        for (int step = 0; step < 3000; step++) {
            int operation = random.nextInt(4);
            int value = random.nextInt(100);

            if (reference.isEmpty() || operation == 0) {
                list.add(value);
                reference.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(reference.size() + 1);
                list.add(index, value);
                reference.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(reference.size());
                check(list.remove(index) == reference.remove(index), "random removal differs");
            } else {
                int index = random.nextInt(reference.size());
                check(list.get(index) == reference.get(index), "random access differs");
            }

            check(list.size() == reference.size(), "randomized size differs");
        }

        for (int i = 0; i < reference.size(); i++) {
            check(list.get(i) == reference.get(i), "randomized contents differ");
        }

        LinkedList large = new LinkedList();
        for (int i = 0; i < 100000; i++) {
            large.add(i);
        }
        check(large.size() == 100000, "large list has the wrong size");
        check(large.get(0) == 0, "large list first value is wrong");
        check(large.get(50000) == 50000, "large list middle value is wrong");
        check(large.get(99999) == 99999, "large list last value is wrong");
    }

    private static void testMinHeapBasicCases() {
        MinHeap heap = new MinHeap();
        check(heap.isEmpty(), "new heap should be empty");
        expectException(NoSuchElementException.class, heap::peekMin);
        expectException(NoSuchElementException.class, heap::extractMin);

        int[] values = { 5, 1, 8, 1, -3, 7, 2 };
        for (int value : values) {
            heap.insert(value);
            check(heap.isValidHeap(), "heap property failed after insertion");
        }

        check(heap.peekMin() == -3, "peekMin returned the wrong value");
        int[] sorted = { -3, 1, 1, 2, 5, 7, 8 };
        for (int value : sorted) {
            check(heap.extractMin() == value, "heap extraction order is wrong");
            check(heap.isValidHeap(), "heap property failed after extraction");
        }
        check(heap.isEmpty(), "heap should be empty after all extractions");

        MinHeap oneValue = new MinHeap();
        oneValue.insert(10);
        check(oneValue.peekMin() == 10, "single value peek failed");
        check(oneValue.extractMin() == 10, "single value extraction failed");

        MinHeap metrics = new MinHeap(new int[] { 4, 3, 2 });
        check(metrics.getComparisonCount() == 0, "constructor metrics should be reset");
        metrics.insert(1);
        check(metrics.getComparisonCount() > 0, "insert should count comparisons");
        metrics.resetMetrics();
        check(metrics.getComparisonCount() == 0, "comparison counter should reset");
    }

    private static void testMinHeapAgainstPriorityQueue() {
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> reference = new PriorityQueue<>();
        Random random = new Random(42);

        for (int i = 0; i < 100000; i++) {
            int value = random.nextInt();
            heap.insert(value);
            reference.add(value);
        }

        check(heap.size() == reference.size(), "large heap has the wrong size");
        check(heap.isValidHeap(), "large heap does not satisfy the heap property");

        int previous = Integer.MIN_VALUE;
        while (!reference.isEmpty()) {
            int actual = heap.extractMin();
            int expected = reference.remove();
            check(actual == expected, "heap result differs from PriorityQueue");
            check(actual >= previous, "heap output is not non-decreasing");
            previous = actual;
        }

        check(heap.isEmpty(), "large heap should be empty after extraction");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (RuntimeException | AssertionError error) {
            failed++;
            System.out.println("FAIL: " + name + " - " + error.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectException(Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expectedType.isInstance(error)) {
                return;
            }

            throw new AssertionError("wrong exception type: " + error.getClass().getSimpleName());
        }

        throw new AssertionError("expected " + expectedType.getSimpleName());
    }
}

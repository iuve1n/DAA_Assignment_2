import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    private static final int[] SIZES = { 100, 1000, 10000, 100000 };
    private static final int REPETITIONS = 5;
    private static final int RANDOM_SEED = 42;
    private static final int RANDOM_ACCESS_OPERATIONS = 10000;
    private static final int SEARCH_OPERATIONS = 1000;
    private static final int UPDATE_OPERATIONS = 1000;
    private static volatile long sink;

    public static void main(String[] args) throws IOException {
        Path tablesDirectory = Path.of("results", "tables");
        Files.createDirectories(tablesDirectory);

        runRandomAccessWorkload(tablesDirectory.resolve("random_access.csv"));
        runSearchWorkload(tablesDirectory.resolve("search.csv"));
        runInsertionRemovalWorkload(tablesDirectory.resolve("insertion_removal.csv"));
        runPriorityProcessingWorkload(tablesDirectory.resolve("priority_processing.csv"));

        System.out.println("Benchmark tables saved in results/tables");
    }

    private static void runRandomAccessWorkload(Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            writer.println("structure,n,average_time_ns,element_accesses,theoretical_complexity");

            for (int n : SIZES) {
                int[] values = createValues(n);
                int[] indices = createIndices(n, RANDOM_ACCESS_OPERATIONS);
                long[] dynamicTimes = new long[REPETITIONS];
                long[] dynamicAccesses = new long[REPETITIONS];
                long[] linkedTimes = new long[REPETITIONS];
                long[] linkedAccesses = new long[REPETITIONS];

                for (int repetition = 0; repetition < REPETITIONS; repetition++) {
                    DynamicArray dynamicArray = new DynamicArray(values);
                    dynamicArray.resetMetrics();
                    long checksum = 0;
                    long start = System.nanoTime();
                    for (int index : indices) {
                        checksum += dynamicArray.get(index);
                    }
                    dynamicTimes[repetition] = System.nanoTime() - start;
                    dynamicAccesses[repetition] = dynamicArray.getAccessCount();
                    sink ^= checksum;

                    LinkedList linkedList = new LinkedList(values);
                    linkedList.resetMetrics();
                    checksum = 0;
                    start = System.nanoTime();
                    for (int index : indices) {
                        checksum += linkedList.get(index);
                    }
                    linkedTimes[repetition] = System.nanoTime() - start;
                    linkedAccesses[repetition] = linkedList.getAccessCount();
                    sink ^= checksum;
                }

                writeResult(writer, "Dynamic Array", n, average(dynamicTimes),
                        average(dynamicAccesses), "Theta(m)");
                writeResult(writer, "Linked List", n, average(linkedTimes),
                        average(linkedAccesses), "Theta(m*n)");
                System.out.println("Random access n=" + n + " complete");
            }
        }
    }

    private static void runSearchWorkload(Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            writer.println("structure,n,average_time_ns,element_comparisons,theoretical_complexity");

            for (int n : SIZES) {
                int[] values = createValues(n);
                int[] searchValues = createSearchValues(n, SEARCH_OPERATIONS);
                long[] dynamicTimes = new long[REPETITIONS];
                long[] dynamicComparisons = new long[REPETITIONS];
                long[] linkedTimes = new long[REPETITIONS];
                long[] linkedComparisons = new long[REPETITIONS];

                for (int repetition = 0; repetition < REPETITIONS; repetition++) {
                    DynamicArray dynamicArray = new DynamicArray(values);
                    dynamicArray.resetMetrics();
                    long matches = 0;
                    long start = System.nanoTime();
                    for (int value : searchValues) {
                        if (dynamicArray.contains(value)) {
                            matches++;
                        }
                    }
                    dynamicTimes[repetition] = System.nanoTime() - start;
                    dynamicComparisons[repetition] = dynamicArray.getComparisonCount();
                    sink ^= matches;

                    LinkedList linkedList = new LinkedList(values);
                    linkedList.resetMetrics();
                    matches = 0;
                    start = System.nanoTime();
                    for (int value : searchValues) {
                        if (linkedList.contains(value)) {
                            matches++;
                        }
                    }
                    linkedTimes[repetition] = System.nanoTime() - start;
                    linkedComparisons[repetition] = linkedList.getComparisonCount();
                    sink ^= matches;
                }

                writeResult(writer, "Dynamic Array", n, average(dynamicTimes),
                        average(dynamicComparisons), "O(m*n)");
                writeResult(writer, "Linked List", n, average(linkedTimes),
                        average(linkedComparisons), "O(m*n)");
                System.out.println("Search n=" + n + " complete");
            }
        }
    }

    private static void runInsertionRemovalWorkload(Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            writer.println("structure,operation,position,n,average_time_ns,metric_type,metric_count,theoretical_complexity");

            for (int n : SIZES) {
                int[] values = createValues(n);
                int[] updateValues = createValues(UPDATE_OPERATIONS);
                benchmarkDynamicUpdates(writer, values, updateValues, n, 0, "beginning");
                benchmarkDynamicUpdates(writer, values, updateValues, n, n / 2, "middle");
                benchmarkLinkedUpdates(writer, values, updateValues, n, 0, "beginning");
                benchmarkLinkedUpdates(writer, values, updateValues, n, n / 2, "middle");
                System.out.println("Insertion and removal n=" + n + " complete");
            }
        }
    }

    private static void benchmarkDynamicUpdates(PrintWriter writer, int[] values,
            int[] updateValues, int n, int index, String position) {
        long[] insertionTimes = new long[REPETITIONS];
        long[] insertionMovements = new long[REPETITIONS];
        long[] removalTimes = new long[REPETITIONS];
        long[] removalMovements = new long[REPETITIONS];

        for (int repetition = 0; repetition < REPETITIONS; repetition++) {
            DynamicArray insertionArray = new DynamicArray(values);
            insertionArray.resetMetrics();
            long start = System.nanoTime();
            for (int value : updateValues) {
                insertionArray.add(index, value);
            }
            insertionTimes[repetition] = System.nanoTime() - start;
            insertionMovements[repetition] = insertionArray.getMovementCount();
            sink ^= insertionArray.size();

            DynamicArray removalArray = new DynamicArray(values);
            for (int value : updateValues) {
                removalArray.add(index, value);
            }
            removalArray.resetMetrics();
            long checksum = 0;
            start = System.nanoTime();
            for (int operation = 0; operation < UPDATE_OPERATIONS; operation++) {
                checksum += removalArray.remove(index);
            }
            removalTimes[repetition] = System.nanoTime() - start;
            removalMovements[repetition] = removalArray.getMovementCount();
            sink ^= checksum;
        }

        String complexity = "Theta(m*n)";
        writeUpdateResult(writer, "Dynamic Array", "insert", position, n,
                average(insertionTimes), "movements", average(insertionMovements), complexity);
        writeUpdateResult(writer, "Dynamic Array", "remove", position, n,
                average(removalTimes), "movements", average(removalMovements), complexity);
    }

    private static void benchmarkLinkedUpdates(PrintWriter writer, int[] values,
            int[] updateValues, int n, int index, String position) {
        long[] insertionTimes = new long[REPETITIONS];
        long[] insertionAccesses = new long[REPETITIONS];
        long[] removalTimes = new long[REPETITIONS];
        long[] removalAccesses = new long[REPETITIONS];

        for (int repetition = 0; repetition < REPETITIONS; repetition++) {
            LinkedList insertionList = new LinkedList(values);
            insertionList.resetMetrics();
            long start = System.nanoTime();
            for (int value : updateValues) {
                insertionList.add(index, value);
            }
            insertionTimes[repetition] = System.nanoTime() - start;
            insertionAccesses[repetition] = insertionList.getAccessCount();
            sink ^= insertionList.size();

            LinkedList removalList = new LinkedList(values);
            for (int value : updateValues) {
                removalList.add(index, value);
            }
            removalList.resetMetrics();
            long checksum = 0;
            start = System.nanoTime();
            for (int operation = 0; operation < UPDATE_OPERATIONS; operation++) {
                checksum += removalList.remove(index);
            }
            removalTimes[repetition] = System.nanoTime() - start;
            removalAccesses[repetition] = removalList.getAccessCount();
            sink ^= checksum;
        }

        String complexity = position.equals("beginning") ? "Theta(m)" : "Theta(m*n)";
        writeUpdateResult(writer, "Linked List", "insert", position, n,
                average(insertionTimes), "accesses", average(insertionAccesses), complexity);
        writeUpdateResult(writer, "Linked List", "remove", position, n,
                average(removalTimes), "accesses", average(removalAccesses), complexity);
    }

    private static void runPriorityProcessingWorkload(Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            writer.println("operation,n,average_time_ns,element_comparisons,theoretical_complexity");

            for (int n : SIZES) {
                int[] values = createHeapValues(n);
                long[] insertionTimes = new long[REPETITIONS];
                long[] insertionComparisons = new long[REPETITIONS];
                long[] extractionTimes = new long[REPETITIONS];
                long[] extractionComparisons = new long[REPETITIONS];

                for (int repetition = 0; repetition < REPETITIONS; repetition++) {
                    MinHeap insertionHeap = new MinHeap();
                    insertionHeap.resetMetrics();
                    long start = System.nanoTime();
                    for (int value : values) {
                        insertionHeap.insert(value);
                    }
                    insertionTimes[repetition] = System.nanoTime() - start;
                    insertionComparisons[repetition] = insertionHeap.getComparisonCount();
                    checkHeap(insertionHeap);
                    sink ^= insertionHeap.peekMin();

                    MinHeap extractionHeap = new MinHeap(values);
                    extractionHeap.resetMetrics();
                    int[] extractedValues = new int[n];
                    start = System.nanoTime();
                    for (int i = 0; i < n; i++) {
                        extractedValues[i] = extractionHeap.extractMin();
                    }
                    extractionTimes[repetition] = System.nanoTime() - start;
                    extractionComparisons[repetition] = extractionHeap.getComparisonCount();
                    checkSorted(extractedValues);
                    sink ^= extractedValues[n - 1];
                }

                writeHeapResult(writer, "insert", n, average(insertionTimes),
                        average(insertionComparisons), "O(n*log(n))");
                writeHeapResult(writer, "extractMin", n, average(extractionTimes),
                        average(extractionComparisons), "Theta(n*log(n))");
                System.out.println("Priority processing n=" + n + " complete");
            }
        }
    }

    private static int[] createValues(int size) {
        Random random = new Random(RANDOM_SEED);
        int[] values = new int[size];
        int bound = Math.max(2, size * 2);

        for (int i = 0; i < size; i++) {
            values[i] = random.nextInt(bound);
        }

        return values;
    }

    private static int[] createHeapValues(int size) {
        Random random = new Random(RANDOM_SEED);
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            values[i] = random.nextInt();
        }

        return values;
    }

    private static int[] createIndices(int size, int count) {
        Random random = new Random(RANDOM_SEED);
        int[] indices = new int[count];

        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(size);
        }

        return indices;
    }

    private static int[] createSearchValues(int size, int count) {
        Random random = new Random(RANDOM_SEED + 1);
        int[] values = new int[count];
        int bound = Math.max(2, size * 2);

        for (int i = 0; i < count; i++) {
            values[i] = random.nextInt(bound);
        }

        return values;
    }

    private static double average(long[] values) {
        long total = 0;

        for (long value : values) {
            total += value;
        }

        return (double) total / values.length;
    }

    private static void writeResult(PrintWriter writer, String structure, int n,
            double time, double metric, String complexity) {
        writer.printf(Locale.US, "%s,%d,%.2f,%.0f,%s%n",
                structure, n, time, metric, complexity);
    }

    private static void writeUpdateResult(PrintWriter writer, String structure,
            String operation, String position, int n, double time,
            String metricType, double metric, String complexity) {
        writer.printf(Locale.US, "%s,%s,%s,%d,%.2f,%s,%.0f,%s%n",
                structure, operation, position, n, time, metricType, metric, complexity);
    }

    private static void writeHeapResult(PrintWriter writer, String operation, int n,
            double time, double comparisons, String complexity) {
        writer.printf(Locale.US, "%s,%d,%.2f,%.0f,%s%n",
                operation, n, time, comparisons, complexity);
    }

    private static void checkHeap(MinHeap heap) {
        if (!heap.isValidHeap()) {
            throw new IllegalStateException("Heap property was not maintained");
        }
    }

    private static void checkSorted(int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[i - 1]) {
                throw new IllegalStateException("Heap output is not sorted");
            }
        }
    }
}

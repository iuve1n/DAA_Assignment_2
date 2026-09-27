# Assignment 2: algorithmic analysis

## 1. Overview

This project implements a dynamic array, a singly linked list, and a min-heap without using Java collection classes for their internal storage. Each structure records the accesses, comparisons, or movements needed by the benchmark workloads. The project compares theoretical complexity with timings collected from fixed workloads at several input sizes.

The source files also include a correctness test suite and a benchmark runner. Java's standard collections are used only in the tests as reference implementations.

## 2. Complexity analysis

In the tables, `n` is the number of stored elements. Ω gives a lower bound, O gives an upper bound, and Θ gives a tight bound. Average cases assume typical valid indices and search values. Space refers to temporary auxiliary space used by one operation.

### Dynamic Array

| Operation | Best case | Average case | Worst case | Auxiliary space |
| --- | --- | --- | --- | --- |
| `add(x)` | Ω(1) | Θ(1) amortized | O(n) | O(n) during resize |
| `add(index, x)` | Ω(1) | Θ(n) | O(n) | O(n) during resize |
| `remove(index)` | Ω(1) | Θ(n) | O(n) | Θ(1) |
| `get(index)` | Ω(1) | Θ(1) | O(1) | Θ(1) |
| `contains(x)` | Ω(1) | Θ(n) | O(n) | Θ(1) |

`get` uses the index as an array offset, so its running time does not grow with `n`. Appending is usually constant time. When the internal array is full, `add(x)` allocates a larger array and copies all existing values, which makes that individual call linear. Doubling the capacity spreads this copying cost across many appends and gives an amortized Θ(1) average.

Indexed insertion shifts every value from the insertion point to the right. Removal shifts the remaining values to the left. An operation near the end may move few or no values, while an operation near the beginning moves close to `n` values. Search checks values in order and may stop at the first element, but a missing value requires `n` comparisons.

### Linked List

| Operation | Best case | Average case | Worst case | Auxiliary space |
| --- | --- | --- | --- | --- |
| `add(x)` | Ω(1) | Θ(1) | O(1) | Θ(1) |
| `add(index, x)` | Ω(1) | Θ(n) | O(n) | Θ(1) |
| `remove(index)` | Ω(1) | Θ(n) | O(n) | Θ(1) |
| `get(index)` | Ω(1) | Θ(n) | O(n) | Θ(1) |
| `contains(x)` | Ω(1) | Θ(n) | O(n) | Θ(1) |

The list keeps both `head` and `tail` references, so `add(x)` appends a node in constant time. Insertion at index 0 is also constant because only the head link changes. An insertion in the middle must first traverse to the previous node. The same traversal cost applies to indexed removal and access. Search follows one link at a time until it finds the value or reaches the end.

The list can insert or remove at the beginning without shifting stored values. The dynamic array has faster indexed access because its elements occupy consecutive positions. These operations can have different practical times even when they share the same O(n) upper bound because array copying and node traversal use memory differently.

### Min-Heap

| Operation | Best case | Average case | Worst case | Auxiliary space |
| --- | --- | --- | --- | --- |
| `insert(x)` | Ω(1) | Θ(log n) | O(n) when resized | O(n) during resize |
| `peekMin()` | Ω(1) | Θ(1) | O(1) | Θ(1) |
| `extractMin()` | Ω(1) | Θ(log n) | O(log n) | Θ(1) |

The minimum is always stored at index 0, so `peekMin()` is constant time. Insertion adds a value at the end and may move it toward the root. Extraction replaces the root with the last value and may move that value toward a leaf. A binary heap has height Θ(log n), which bounds both loops. An insertion without resizing is O(log n). A resize copies `n` values, so one insertion can take O(n) time and O(n) temporary space.

## 3. Correctness

### Loop invariant for `DynamicArray.add(index, value)`

Let `oldSize` be the number of elements before insertion. The shifting loop starts with `i = oldSize` and moves toward `index`.

Loop invariant: before an iteration with the current value of `i`, every original element from position `i` through `oldSize - 1` is already stored one position to the right. The original elements from `index` through `i - 1` are still in their old positions. Elements before `index` have not changed.

Initialization. Before the first iteration, `i` equals `oldSize`. The range from `oldSize` through `oldSize - 1` is empty, so there are no shifted elements to check. All existing elements are still in their original positions. `ensureCapacity()` has already made position `oldSize` available and preserves the order of existing values if a resize occurs.

Maintenance. The loop assigns `arr[i] = arr[i - 1]`. This moves the original element at `i - 1` one position to the right. After `i` is decreased, the shifted range begins at the new value of `i`, and all positions before it remain unchanged. The invariant therefore holds for the next iteration.

Termination. The loop stops when `i == index`. At that point, every original element from `index` through `oldSize - 1` has moved to the position one place to its right. Position `index` is free, while every element before it is unchanged.

The method writes the new value at `index` and increases `size`. The resulting array contains the old prefix, the inserted value, and the shifted suffix in their required order. This proves that indexed insertion is correct.

### Loop invariant for `MinHeap.insert(value)`

The method places the new value at the next free position and calls the upward loop with `currentIndex` at that position.

Loop invariant: before each iteration, the min-heap property holds for every parent-child edge except possibly the edge between `currentIndex` and its parent. All subtrees below `currentIndex` are valid min-heaps, and the array still contains exactly the old heap elements plus the inserted value.

Initialization. The old array already satisfies the min-heap property. The new value is added as a leaf, so it has no children. Adding a leaf cannot change any existing parent-child relationship. The only possible violation is between the new leaf and its parent, which matches the invariant.

Maintenance. If the parent is less than or equal to the current value, the possible violation is gone and the loop stops. Otherwise, the method swaps the current value with its parent. The smaller inserted value moves upward. The former parent moves down to a position where it remains no greater than the existing children, since those relationships came from the valid heap before the swap. The only edge that may now violate the property is between the inserted value at its new position and its new parent. The invariant holds again.

Termination. The loop ends when the inserted value reaches the root or its parent is less than or equal to it. The invariant says that every other edge is already valid, and the stopping condition makes the last possible edge valid as well.

No values are lost during the swaps, and every parent is less than or equal to its children when the loop ends. The array therefore represents a valid min-heap containing the inserted value.

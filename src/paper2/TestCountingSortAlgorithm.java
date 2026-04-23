package paper2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountingSortAlgorithmTest {

    private CountingSortAlgorithm sorter;

    @BeforeEach
    void setUp() {
        sorter = new CountingSortAlgorithm();
    }

    private int[] sorted(int[] arr) {
        int[] copy = arr.clone();
        java.util.Arrays.sort(copy);
        return copy;
    }

    @Test
    void testGetMaxNormalArray() {
        int[] arr = {3, 1, 7, 2, 9, 4};
        assertEquals(9, CountingSortAlgorithm.getMax(arr, arr.length));
    }

    @Test
    void testGetMaxSingleElement() {
        int[] arr = {42};
        assertEquals(42, CountingSortAlgorithm.getMax(arr, 1));
    }

    @Test
    void testGetMaxAllSame() {
        int[] arr = {5, 5, 5, 5};
        assertEquals(5, CountingSortAlgorithm.getMax(arr, arr.length));
    }

    @Test
    void testGetMinNormalArray() {
        int[] arr = {3, 1, 7, 2, 9, 4};
        assertEquals(1, CountingSortAlgorithm.getMin(arr, arr.length));
    }

    @Test
    void testGetMinSingleElement() {
        int[] arr = {42};
        assertEquals(42, CountingSortAlgorithm.getMin(arr, 1));
    }

    @Test
    void testGetMinAllSame() {
        int[] arr = {7, 7, 7};
        assertEquals(7, CountingSortAlgorithm.getMin(arr, arr.length));
    }

    @Test
    void testPartition() {
        int[] arr = {5, 3, 8, 1, 9, 2, 7};
        int p = CountingSortAlgorithm.partition(arr, 0, arr.length - 1);
        int pivotVal = arr[p];
        for (int i = 0; i <= p; i++) {
            assertTrue(arr[i] <= pivotVal, "Element at index " + i + " (" + arr[i] + ") should be <= pivot " + pivotVal);
        }
        for (int i = p + 1; i < arr.length; i++) {
            assertTrue(arr[i] >= pivotVal, "Element at index " + i + " (" + arr[i] + ") should be >= pivot " + pivotVal);
        }
    }

    @Test
    void testPartitionTwoElements() {
        int[] arr = {9, 1};
        int p = CountingSortAlgorithm.partition(arr, 0, 1);
        assertTrue(arr[0] <= arr[1], "After partitioning a 2-element array, arr[0] should be <= arr[1]");
        assertTrue(p >= 0 && p <= 1);
    }

    @Test
    void testPartitionAllSame() {
        int[] arr = {4, 4, 4, 4, 4};
        int p = CountingSortAlgorithm.partition(arr, 0, arr.length - 1);
        assertTrue(p >= 0 && p < arr.length);
    }

    @Test
    void testCountingSortNormalArray() {
        int[] arr = {5, 3, 8, 1, 9, 2, 7};
        int[] expected = sorted(arr);
        sorter.countingSort(arr, arr.length);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testCountingSortSorted() {
        int[] arr = {1, 2, 3, 4, 5};
        sorter.countingSort(arr, arr.length);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, arr);
    }

    @Test
    void testCountingSortReverseSorted() {
        int[] arr = {9, 7, 5, 3, 1};
        sorter.countingSort(arr, arr.length);
        assertArrayEquals(new int[]{1, 3, 5, 7, 9}, arr);
    }

    @Test
    void testCountingSortDuplicates() {
        int[] arr = {4, 2, 4, 1, 4, 2};
        int[] expected = sorted(arr);
        sorter.countingSort(arr, arr.length);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testQuicksortModifiedWithCountingSort() {
        int[] arr = {5, 3, 8, 1, 9, 2, 7};
        int[] expected = sorted(arr);
        int n = arr.length;
        int min = CountingSortAlgorithm.getMin(arr, n);
        int max = CountingSortAlgorithm.getMax(arr, n);
        sorter.quicksortModified(arr, 0, n - 1, max, min);
        sorter.countingSort(arr, n);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testCountingSortByPartitions() {
        int[] arr = {5, 3, 8, 1, 9, 2, 7};
        int[] expected = sorted(arr);
        int n = arr.length;
        int min = CountingSortAlgorithm.getMin(arr, n);
        int max = CountingSortAlgorithm.getMax(arr, n);
        sorter.quicksortModified(arr, 0, n - 1, max, min);
        sorter.countingSortByPartitions(arr, n);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testQuicksortClassic() {
        int[] arr = {5, 3, 8, 1, 9, 2, 7};
        int[] expected = sorted(arr);
        sorter.quicksortClassic(arr, 0, arr.length - 1);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testHybridAlgorithm() {
        int[] arr = {9, 1};
        int n = arr.length;
        int min = CountingSortAlgorithm.getMin(arr, n);
        int max = CountingSortAlgorithm.getMax(arr, n);
        sorter.quicksortModified(arr, 0, n - 1, max, min);
        sorter.countingSortByPartitions(arr, n);
        assertArrayEquals(new int[]{1, 9}, arr);
    }
}
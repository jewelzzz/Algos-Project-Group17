package paper2;

import java.util.Arrays;
import java.util.Random;

/*
 * Table 1: Random vs Sorted input on classic counting sort
 * Table 2: With vs without preprocessing (r >> n case)
 * Table 3: Classic QuickSort vs QuickSort + Insertion vs Proposed Hybrid (n = r)
 */
public class Benchmark {

    //each test is run 5 times and then report the average, the paper does not specify the number of trials run
    static final int TRIALS = 5;

    /*
     * random seed for reproducible benchmarks, the paper does not specify a particular seed, but uses "random integers
     * drawn independently at random" for their experiments (Table 1, page 2). we use a fixed seed, the randomly picked number 21, to ensure
     * that running the benchmark multiple times produces identical results, as well as fair comparison since all three tables
     * use the same data across different algorithms.
     *
     * this generates uniformly distributed random integers in the range [0, r-1] as
     * the paper describes.
     *
     * paper's approach (Section 2.1): "n input integers were drawn independently
     * at random between [0, (n-1)]"
     */
    static final Random range = new Random(21);

    public static void main(String[] args)
    {
        runTable1();
        System.out.println();

        runTable2();
        System.out.println();

        runTable3();
        System.out.println();
    }

    /*
     * Table 1 from paper (page 2): Random vs Sorted Input on Classic Counting Sort
     * Tests classic counting sort on random vs sorted inputs
     * Expected: sorted should be ~2-3x faster than random due to data locality
     */
    private static void runTable1() {
        System.out.println("━".repeat(80));
        System.out.println("Table 1: Random vs Sorted Input on Classic Counting Sort");
        System.out.println("━".repeat(80));
        System.out.println("Expected: Sorted input should be 2-3x faster due to cache locality\n");
        System.out.printf("%-15s %-15s %-20s %-20s %-15s%n",
                "n", "r", "Random (ms)", "Sorted (ms)", "Speed Increase");
        System.out.println("-".repeat(80));

        int[] table1Sizes = {1000000, 2000000, 3000000};

        for (int n : table1Sizes)
        {
            double randomTime = 0, sortedTime = 0;

            for (int t = 0; t < TRIALS; t++)
            {
                //random input
                int[] arr = randomArray(n, n);
                long start = System.nanoTime();
                new CountingSortAlgorithm().countingSort(arr, n);
                randomTime += (System.nanoTime() - start) / 1000000.0;

                //sorted input
                arr = sortedArray(n);
                start = System.nanoTime();
                new CountingSortAlgorithm().countingSort(arr, n);
                sortedTime += (System.nanoTime() - start) / 1000000.0;
            }

            double avgRandom = randomTime / TRIALS;
            double avgSorted = sortedTime / TRIALS;
            double speed = avgRandom / avgSorted;

            System.out.printf("%-15d %-15d %-20.3f %-20.3f %.2fx%n", n, n, avgRandom, avgSorted, speed);
        }

        System.out.println("-".repeat(80));
    }


    /*
     * Table 2 from paper (page 4): With vs Without Preprocessing (r >> n case)
     * Tests hybrid algorithm when range >> number of elements
     * Expected: hybrid (preprocessing + counting) should beat plain counting sort
     */
    private static void runTable2() {
        System.out.println("━".repeat(80));
        System.out.println("TABLE 2: With vs Without Preprocessing (r >> n)");
        System.out.println("━".repeat(80));
        System.out.println("Expected: Preprocessing + Counting Sort < Plain Counting Sort\n");
        System.out.printf("%-10s %-15s %-20s %-20s %-20s %-15s%n",
                "n", "r", "Preprocess (ms)", "Count (ms)", "Total Hybrid (ms)", "Plain (ms)");
        System.out.println("-".repeat(80));

        int[] table2Sizes = {1000, 2000, 3000};
        int r = 1000000;

        for (int n : table2Sizes)
        {
            double preprocessTime = 0, countSortTime = 0, plainTime = 0;

            for (int t = 0; t < TRIALS; t++)
            {
                int[] arr = randomArray(n, r);

                // with preprocessing (hybrid approach)
                int[] arrCopy = Arrays.copyOf(arr, n);
                CountingSortAlgorithm sorter = new CountingSortAlgorithm();
                int max = CountingSortAlgorithm.getMax(arrCopy, n);
                int min = CountingSortAlgorithm.getMin(arrCopy, n);

                long start = System.nanoTime();
                sorter.quicksortModified(arrCopy, 0, n - 1, max, min);
                preprocessTime += (System.nanoTime() - start) / 1000000.0;

                start = System.nanoTime();
                sorter.countingSortByPartitions(arrCopy, n); // <-- only change on this line
                countSortTime += (System.nanoTime() - start) / 1000000.0;

                // without preprocessing (plain counting sort)
                int[] arrPlain = Arrays.copyOf(arr, n);
                start = System.nanoTime();
                new CountingSortAlgorithm().countingSort(arrPlain, n);
                plainTime += (System.nanoTime() - start) / 1000000.0;
            }

            double avgPreprocess = preprocessTime / TRIALS;
            double avgCount = countSortTime / TRIALS;
            double avgHybrid = avgPreprocess + avgCount;
            double avgPlain = plainTime / TRIALS;

            System.out.printf("%-10d %-15d %-20.3f %-20.3f %-20.3f %-15.3f%n", n, r, avgPreprocess, avgCount, avgHybrid, avgPlain);
        }

        System.out.println("-".repeat(80));
    }


    /*
     * Table 3 from paper (page 4): QuickSort vs QuickSort+Insertion vs Proposed Hybrid (n = r)
     * Compares three algorithms when n = r (ideal case for counting sort)
     * Expected: Proposed hybrid should outperform both classic quicksort and quicksort + insertion hybrid
     */
    private static void runTable3() {
        System.out.println("━".repeat(80));
        System.out.println("Table 3: QuickSort vs QuickSort + Insertion vs Proposed Hybrid (n = r)");
        System.out.println("━".repeat(80));
        System.out.println("Expected: Proposed Hybrid < QuickSort + Insertion < Classic QuickSort\n");
        System.out.printf("%-15s %-20s %-25s %-20s%n",
                "n = r", "QuickSort (ms)", "QSort + Insertion (ms)", "Proposed Hybrid (ms)");
        System.out.println("-".repeat(80));

        int[] table3Sizes = {1000000, 2000000};

        for (int n : table3Sizes)
        {
            double quickSortTime = 0, hybridInsertionTime = 0, proposedTime = 0;

            for (int t = 0; t < TRIALS; t++)
            {
                int[] arr = randomArray(n, n);

                //classic quicksort
                int[] arr1 = Arrays.copyOf(arr, n);
                long start = System.nanoTime();
                new CountingSortAlgorithm().quicksortClassic(arr1, 0, n - 1);
                quickSortTime += (System.nanoTime() - start) / 1000000.0;

                //quicksort with insertion sort
                int[] arr2 = Arrays.copyOf(arr, n);
                start = System.nanoTime();
                new CountingSortAlgorithm().quicksortWithInsertion(arr2, 0, n - 1);
                hybridInsertionTime += (System.nanoTime() - start) / 1000000.0;

                //proposed hybrid (modified quicksort + counting sort)
                int[] arr3 = Arrays.copyOf(arr, n);
                CountingSortAlgorithm sorter = new CountingSortAlgorithm();
                int max = CountingSortAlgorithm.getMax(arr3, n);
                int min = CountingSortAlgorithm.getMin(arr3, n);
                start = System.nanoTime();
                sorter.quicksortModified(arr3, 0, n - 1, max, min);
                sorter.countingSortByPartitions(arr3, n);
                proposedTime += (System.nanoTime() - start) / 1000000.0;
            }

            double avgQuick = quickSortTime / TRIALS;
            double avgHybridIns = hybridInsertionTime / TRIALS;
            double avgProposed = proposedTime / TRIALS;

            System.out.printf("%-15d %-20.2f %-25.2f %-20.2f%n", n, avgQuick, avgHybridIns, avgProposed);
        }

        System.out.println("-".repeat(80));
    }

    //generate random array of n elements with values in range [0, r-1]
    static int[] randomArray(int n, int r)
    {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++)
        {
            arr[i] = range.nextInt(r);
        }
        return arr;
    }

    //generate sorted array {0, 1, 2, ..., n-1}
    static int[] sortedArray(int n)
    {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++)
        {
            arr[i] = i;
        }
        return arr;
    }
}
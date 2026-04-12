package paper2;

/*
 * CountingSortAlgorithm implements a hybrid sorting algorithm described in Paper 2
 * 
 * There are two phases to the proposed algorithm:
 * 	1. A modified quicksort for preprocessing that partially sorts the array into cache-friendly partitions to prevent cache misses,
 * 		stopping early once each partition is small enough to fit inside CPU cache
 *  2. A standard counting sort applied to the now partially sorted array
 *
 * The following code also contains:
 * - A classic quicksort implementation for comparison to the proposed algorithm (Table 3 in the paper)
 * - A quicksort with insertion sort hybrid for comparison to the proposed algorithm (Table 3 in the paper)
 * 
 */

public class CountingSortAlgorithm {

	/*
	 * Cache threshold constant: as specified in the paper 
	 * Partitioning stops when the combined size of the value range and index 
	 * 	fits within C, meaning subarray is small enough to reside in CPU cache
	 */
	static final int C = 1000;

    // Threshold for insertion sort in hybrid algorithm
    private static final int INSERTION_THRESHOLD = 10;

	/*
	 * 1. Modified QuickSort that partitions without fully sorting (Algorithm 2 from paper)
	 * 
	 * Stops recursing early unlike standard quicksort after a sub-array is small enough to fit inside cache
	 * Result: 
	 * 		- All values in section[i] are less than all values in section[i+1]
	 * 		- within each section, elements may still be unsorted
	 * 
	 */
	void quicksort_modified(int arr[], int low, int high,int maxValue, int minValue) {

        if (maxValue == minValue){
            return;
        }
		//only continue partitioning if:
		//	- the sub-array has  more than one element (low<high)
		//	- the combined value range+index range still exceeds the cache
		//once both conditions are met -> the partition is small enough to fit in cache so we move on to next
		while ((low < high) &&(maxValue- minValue + high - low > C)) {
			
			int pivot = partition(arr, low, high); //partition around pivot - returns pivot index
			int midValue = arr[pivot]; //value at pivot position after partitioning
			
			quicksort_modified(arr, low,pivot - 1, midValue, minValue); //recursively partition left half
			quicksort_modified(arr, pivot + 1, high, maxValue, midValue); //recursively partition the right half 
			
			return; //return after handling both halves
		}
	}

    void countingsort_by_partitions(int[] arr, int n) {
        int i = 0;
        while (i < n) {
            // Find the extent of this partition using the same C threshold
            int partMin = arr[i];
            int partMax = arr[i];
            int j = i + 1;

            while (j < n) {
                int newMin = Math.min(partMin, arr[j]);
                int newMax = Math.max(partMax, arr[j]);
                // Stop expanding if adding this element would exceed cache threshold
                if ((newMax - newMin + (j - i)) > C) break;
                partMin = newMin;
                partMax = newMax;
                j++;
            }

            countingsort_ranged_segment(arr, i, j - 1, partMin, partMax);
            i = j;
        }
    }

    private void countingsort_ranged_segment(int[] arr, int low, int high, int min, int max) {
        if (max < min){
            return;
        }

        int range = max - min + 1;

        if (range <= 0){
            return;
        }
        int[] count = new int[range];

        for (int i = low; i <= high; i++) {
            count[arr[i] - min]++;
        }

        int idx = low;
        for (int v = 0; v < range; v++) {
            while (count[v]-- > 0){
                arr[idx++] = v + min;
            }
        }
    }
	
	
	/*
	 * Partitions the sub-array around a pivot using a median of three pivot selection strategy
	 * 
	 * Median of three: looks at arr[low], arr[mid] and arr[high], sorts those three, 
	 * 	and uses middle value as the pivot. 
	 * 	This avoids the worst case behaviour (O(n^2)) of normal quicksort on already or nearly sorted input
	 * 
	 * After partitioning:
	 * 		- All elements to left of returned index are <= pivot
	 * 		- All elements to right are >= pivot
	 * 
	 */
	static int partition(int[] arr, int low, int high) {
		
		int mid = low + (high - low) / 2; //safer midpoint calculation (avoids integer overflow)
		
		//Median of three: 
		
		if(arr[low] > arr[mid]) {
			int temp = arr[low];
			arr[low] = arr[mid];
			arr[mid] = temp;
		}
		
		if(arr[low] > arr[high]) {
			int temp = arr[low];
			arr[low] = arr[high];
			arr[high] = temp;
		}
		
		if(arr[mid] > arr[high]) {
			int temp = arr[mid];
			arr[mid] = arr[high];
			arr[high] = temp;
		}
		
		//after these swaps, arr[mid] holds median value (pivot)
		
		int pivot = arr[mid]; //mid value is pivot
		int i = low - 1; //left pointer
		int j = high + 1; //right pointer
		
		while(true) {
			
			do {i++; } while (arr[i] < pivot); //keep moving pointer until value >= pivot is found
			do {j--; } while (arr[j] > pivot); //keep moving pointer until value <= pivot is found
			
			if(i >= j) {
				return j; //pointers have met (crossed) 
			}
			
			//swap out of place elements so smaller goes left and larger goes right 
			int temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}		
	}

    void countingsort_ranged(int arr[], int n){
        int max = getMax(arr, n);
        int min = getMin(arr, n);
        int range = max - min + 1;

        int[] output = new int[n];
        int[] count = new int[range]; // much smaller when values are clustered

        for (int i = 0; i < n; i++)
            count[arr[i] - min]++;

        for (int i = 1; i < range; i++)
            count[i] += count[i - 1];

        for (int i = n - 1; i >= 0; i--) {
            output[count[arr[i] - min] - 1] = arr[i];
            count[arr[i] - min]--;
        }

        for (int i = 0; i < n; i++)
            arr[i] = output[i];
    }
	
	
	/*
	 * 2. Counting sort applied to the pre-partitioned array (Algorithm 1 from paper)
	 * 
	 * Counting sort is non-comparison, stable sort with O(n+r) time complexity
	 * 	where r is value range. 
	 * Method:
	 * 		1. Counting how many times value appears
	 * 		2. Computing prefix sums to determine each value's final position
	 * 		3. Placing each element in its correct position in an output array
	 * 
	 * Because the array has already been partitioned into cache-sized sections by 
	 * 	quicksort_modified, this steps benefits from improved cache locality
	 * 
	 */
	void countingsort(int arr[], int n) {
		
		int[] output= new int[n+1]; //output buffer (1-indexed due to prefix sum logic)
		
		int r = getMax(arr, n); //find max value to size count array
		int[] count = new int[r+1]; //count[i] will hold frequency of value i
		
		//initialise all counts to zero 
		for(int i = 0; i<=r; i++) {
			count[i] = 0;
		}
		
		//count occurrences of each value
		for(int i = 0; i < n; i++) {
			count[arr[i]]++;
		}
		
		//prefix sum-count[i] now holds position of last occurrence of value i
		for(int i = 1; i<=r; i++) {
			count[i] += count[i-1];
		}
		
		//build output array by placing each element at its sorted position 
		//iterate in reverse to maintain stability (equal elements preserve original order)
		for(int i = n-1; i>=0; i--) {
			output[count[arr[i]] - 1] = arr[i]; //place element at computed position
			count[arr[i]] -= 1; //decrement so next equal value goes one spot earlier
		}
		
		//copy sorted output back into original array
		for(int i = 0; i<n; i++) {
			arr[i] = output[i]; 
		}
	}


    /*
     * Classic quicksort algorithm - no early termination
     */
    public void quicksort_classic(int arr[], int low, int high) {
        if (low < high) {
            int pivot = partition(arr, low, high);
            quicksort_classic(arr, low, pivot - 1);
            quicksort_classic(arr, pivot + 1, high);
        }
    }

    // ============================================================
    // QUICKSORT WITH INSERTION SORT (for Table 3 comparison)
    // ============================================================

    /*
     * Hybrid quicksort that switches to insertion sort for small partitions
     */
    public void quicksort_with_insertion(int arr[], int low, int high) {
        if (low < high) {
            // Use insertion sort for small partitions
            if (high - low < INSERTION_THRESHOLD) {
                insertionSort(arr, low, high);
            } else {
                int pivot = partition(arr, low, high);
                quicksort_with_insertion(arr, low, pivot - 1);
                quicksort_with_insertion(arr, pivot + 1, high);
            }
        }
    }

    /*
     * Insertion sort for small partitions
     */
    private void insertionSort(int arr[], int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j = i - 1;

            while (j >= low && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }


	/*
	 * Returns maximum value in arr[0..n-1]
	 * Used by countingsort to determine range of count array 
	 */
	static int getMax(int[] arr, int n) {
		int max = arr[0];
		
		for(int i=1; i<n; i++) {
			if(arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/*
	 * Returns minimum value in arr[0..n-1]
	 * Used in main to pass initial minValue bound into quicksort_modified
	 */
	static int getMin(int[] arr, int n) {
		int min = arr[0];
		
		for(int i=1; i<n; i++) {
			if(arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	
	public static void main(String[] args) {
	    CountingSortAlgorithm sorter = new CountingSortAlgorithm();
	    int[] arr = {5, 3, 8, 1, 9, 2, 7};
	    int n = arr.length;

	    int min = getMin(arr, n);
	    int max = getMax(arr, n);
	    
	    for (int x : arr) System.out.print(x + " ");
	    
	    System.out.println();

	   sorter.quicksort_modified(arr, 0, n - 1, max, min);

	    System.out.println();

	    sorter.countingsort(arr, n);

	    for (int x : arr) System.out.print(x + " ");
	}
}


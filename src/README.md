# Cache-Aware Hybrid Sorting Algorithm
 
A Java implementation of a hybrid sorting algorithm that combines a modified QuickSort with Counting Sort to improve cache efficiency. This project reproduces the results from Paper 2, comparing the proposed algorithm against other sorting algorithms. 
 
---
 
## Overview
 
The proposed algorithm works in two phases:
 
1. **Modified QuickSort (preprocessing)** — Partially sorts the array into cache-friendly partitions, stopping early once each partition is small enough to fit in CPU cache. Unlike standard QuickSort, it does not fully sort within partitions.
2. **Counting Sort** — Applied to the pre-partitioned array. Because data is already arranged into cache-sized sections, this step benefits from improved cache locality.
 
---
 
## Project Structure
 
```
paper2/
├── CountingSortAlgorithm.java   # Core algorithm implementations
└── Benchmark.java               # Benchmarking suite (Tables 1–3)
```
 
### `CountingSortAlgorithm.java`
 
Contains:
- `quicksort_modified` — Modified QuickSort with early termination (Algorithm 2)
- `countingsort` — Standard Counting Sort (Algorithm 1)
- `countingsort_by_partitions` — Counting Sort applied per cache-sized partition
- `quicksort_classic` — Classic QuickSort (Table 3 baseline)
- `quicksort_with_insertion` — QuickSort + Insertion Sort hybrid (Table 3 baseline)
- Helper methods: `partition`, `getMax`, `getMin`
 
### `Benchmark.java`
 
Reproduces three tables from the paper:
 
**Table  :  Description**  

 Table 1 : Random vs sorted input on classic Counting Sort 

 Table 2 : With vs without preprocessing when `r >> n`

 Table 3 : Classic QuickSort vs QuickSort+Insertion vs Proposed Hybrid when `n = r` 
 
 
---
 
## Prerequisites
 
- Java 8 or higher
- No external dependencies
 
---
 
## Running the Code
 
**Compile:**
```bash
javac paper2/CountingSortAlgorithm.java paper2/Benchmark.java
```
 
**Run the main algorithm demo:**
```bash
java paper2.CountingSortAlgorithm
```
 
**Run the full benchmark suite:**
```bash
java paper2.Benchmark
```
 
---
 
## Key Parameters
 
| Constant ................| Value | Description .............................................................................................................................|

|---------------------------|--------|--------------------------------------------------------------------------------------------------------------------------|

| `C` .............................| `1000` | Cache threshold — partitioning stops when value range + index range fits within this bound |

| `INSERTION_THRESHOLD` | `10`... | Partition size below which the hybrid falls back to Insertion Sort .............................................|

| `TRIALS` .....................| `5`.... | Number of benchmark trials averaged per test case ................................................................|
 
---
 
## Algorithm Details
 
### Cache Threshold (`C`)

The paper sets the Cache Threshold value to be 1000.
 
The modified QuickSort continues partitioning only while:
 
```
(maxValue - minValue) + (high - low) > C
```
 
Once a sub-array satisfies this condition, it is considered small enough to reside in CPU cache and is left for Counting Sort to handle.
 
### Pivot Selection
 
Both QuickSort variants use a **median-of-three** pivot strategy — comparing `arr[low]`, `arr[mid]`, and `arr[high]` and using the median as the pivot. This avoids the O(n²) worst-case behaviour of naive QuickSort on already-sorted input.
 
---
 
## Expected Benchmark Results
 
- **Table 1:** Sorted input should be ~2–3× faster than random input on classic Counting Sort due to cache locality.
- **Table 2:** The hybrid (preprocessing + Counting Sort) should outperform plain Counting Sort when `r >> n`.
- **Table 3:** Proposed Hybrid < QuickSort + Insertion < Classic QuickSort when `n = r`.
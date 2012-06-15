/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * This class contains additional methods for manipulating arrays (such as
 * sorting and searching). For all quick sort methods the base code was taken
 * from jdk6 {@link Arrays} class.
 *
 * @see Arrays
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ArraysUtils {

    private ArraysUtils() {
    }
    public static final Comparator<Object> HASH_COMPARATOR = new Comparator<Object>() {

        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    };

    /**
     * This is the same method to {@link Arrays#binarySearch(int[], int) }. The
     * differs is in the returned value. If key not found, this method returns
     * the position of the first element, witch is closest to key (i.e. if
     * Arrays.binarySearch returns {@code -low-1}, this method returns {@code low}).
     *
     * @param a   the array to be searched
     * @param key the value to be searched for
     *
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt><i>insertion point</i></tt>. The <i>insertion
     *         point</i> is defined as the point at which the key would be
     *         inserted into the array: the index of the first element greater
     *         than the key, or <tt>a.length</tt> if all elements in the array
     *         are less than the specified key.
     */
    public static int binarySearch1(int[] a, int key) {
        return binarySearch1(a, 0, a.length, key);
    }

    /**
     * This is the same method to {@link Arrays#binarySearch(int[], int, int, int)
     * }. The differs is in the returned value. If key not found, this method
     * returns the position of the first element, witch is closest to key (i.e.
     * if Arrays.binarySearch returns {@code -low-1}, this method returns {@code low}).
     *
     * @param a         the array to be searched
     * @param key       the value to be searched for
     * @param fromIndex the index of the first element (inclusive) to be
     *                  searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     *
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt><i>insertion point</i></tt>. The <i>insertion
     *         point</i> is defined as the point at which the key would be
     *         inserted into the array: the index of the first element greater
     *         than the key, or <tt>toIndex</tt> if all elements in the array are
     *         less than the specified key.
     */
    public static int binarySearch1(int[] a, int fromIndex, int toIndex,
                                    int key) {
        Arrays.binarySearch(a, key);
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low;  // key not found.
    }

    /**
     * Returns xor of objects hashes spreaded by
     *  {@link HashFunctions#JenkinWang32shift(int) }
     *
     * @param objects array
     *
     * @return commutative hash
     */
    public static int commutativeHashCode(final Object[] objects) {
        if (objects == null)
            return 0;
        int hash = 0;
        for (Object o : objects)
            hash ^= (o == null ? 0 : o.hashCode());
        return HashFunctions.JenkinWang32shift(hash);
    }

    /**
     * Returns xor of objects hashes spreaded by
     *  {@link HashFunctions#JenkinWang32shift(int) }
     *
     * @param objects array
     *
     * @return commutative hash
     */
    public static int commutativeHashCode(final Object[] objects, final int from, final int to) {
        rangeCheck(objects.length, from, to);
        if (objects == null)
            return 0;
        int hash = 0;
        for (int i = from; i < to; ++i)
            hash ^= (objects[i] == null ? 0 : objects[i].hashCode());
        return HashFunctions.JenkinWang32shift(hash);
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion
     * sort algorithm and simultaneously permutes the {@code coSort} ints array
     * in the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input).
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     */
    public static void insertionSort(int[] target, int[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion
     * sort algorithm and simultaneously permutes the {@code coSort} ints array
     * in the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input). The range to be sorted extends from index <tt>fromIndex</tt>,
     * inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static void insertionSort(int[] target, int fromIndex, int toIndex, int[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);

        int i, key, j, keyC;
        for (i = fromIndex + 1; i < toIndex; i++) {
            key = target[i];
            keyC = coSort[i];
            for (j = i; j > fromIndex && target[j - 1] > key; j--) {
                target[j] = target[j - 1];
                coSort[j] = coSort[j - 1];
            }
            target[j] = key;
            coSort[j] = keyC;
        }
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion
     * sort algorithm and simultaneously permutes the {@code coSort} longs array
     * in the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input).
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     */
    public static void insertionSort(int[] target, long[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion
     * sort algorithm and simultaneously permutes the {@code coSort} ints array
     * in the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input). The range to be sorted extends from index <tt>fromIndex</tt>,
     * inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static void insertionSort(int[] target, int fromIndex, int toIndex, long[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);

        int i, key, j;
        long keyC;
        for (i = fromIndex + 1; i < toIndex; i++) {
            key = target[i];
            keyC = coSort[i];
            for (j = i; j > fromIndex && target[j - 1] > key; j--) {
                target[j] = target[j - 1];
                coSort[j] = coSort[j - 1];
            }
            target[j] = key;
            coSort[j] = keyC;
        }
    }

    /**
     * Sorts the specified target array of objects into ascending order,
     * according to the natural ordering of its elements using insertion sort
     * algorithm and simultaneously permutes the {@code coSort} objects array in
     * the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input).
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     */
    public static <T extends Comparable<T>> void insertionSort(T[] target, Object[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified target array of objects into ascending order,
     * according to the natural ordering of its elements using insertion sort
     * algorithm and simultaneously permutes the {@code coSort} objects array in
     * the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted
     * input). The range to be sorted extends from index <tt>fromIndex</tt>,
     * inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
     *
     * <p> This sort is the best choice for small arrays with elements number <
     * 100.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static <T extends Comparable<T>> void insertionSort(T[] target, int fromIndex, int toIndex, Object[] coSort) {
        int i, j;
        T key;
        Object keyC;
        for (i = fromIndex + 1; i < toIndex; i++) {
            key = target[i];
            keyC = coSort[i];
            for (j = i; j > fromIndex && target[j - 1].compareTo(key) > 0; j--) {
                target[j] = target[j - 1];
                coSort[j] = coSort[j - 1];
            }
            target[j] = key;
            coSort[j] = keyC;
        }
    }

    /**
     * Sorts the specified array of ints into ascending order using TimSort
     * algorithm and simultaneously permutes the {@code coSort} ints array in
     * the same way then specified target array.
     *
     * <p> NOTE: using of this method is very good for large arrays with more
     * then 100 elements, in other case using of insertion sort is highly
     * recommended.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not
     * be reordered as a result of the sort.
     *
     * <p> The code was taken from {@link Arrays#sort(java.lang.Object[]) } and
     * adapted for integers. For more information look there.
     *
     * @see Arrays#sort(java.lang.Object[])
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws ClassCastException       if the array contains elements that are
     *                                  not <i>mutually comparable</i> (for
     *                                  example, strings and integers)
     * @throws IllegalArgumentException (optional) if the natural ordering of
     *                                  the array elements is found to violate
     *                                  the {@link Comparable} contract
     */
    public static void timSort(int target[], int[] coSort) {
        IntTimSort.sort(target, coSort);
    }

    public static void stableSort(int target[], int[] cosort) {
        //TODO test 100
        if (target.length > 100)
            ArraysUtils.timSort(target, cosort);
        else
            ArraysUtils.insertionSort(target, cosort);
    }

    /**
     * Sorts the specified target array of ints into ascending numerical order
     * and simultaneously permutes the {@code coSort} ints array in the same way
     * then specified target array.
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: remember this is unstable sort algorithm, so additional
     * combinatorics of the {@code coSort} array can be perfomed. Use this
     * method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * <p><b>NOTE:</b> The method throws {@code IllegalArgumentException} if
     * {@code target == coSort}, because in this case no sorting will be
     * perfomed.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static void quickSort(int[] target, int[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into
     * ascending numerical order and simultaneously permutes the {@code coSort}
     * ints array in the same way then specified target array. The range to be
     * sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.)<p>
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: remember this is unstable sort algorithm, so additional
     * combinatorics of the {@code coSort} array can be perfomed. Use this
     * method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * <p><b>NOTE:</b> The method throws {@code IllegalArgumentException} if
     * {@code target == coSort}, because in this case no sorting will be
     * perfomed.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     * @throws IllegalArgumentException       if target == coSort (as
     *                                        references).
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, int[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, int[]) },
     * but without range checking and toIndex -> length (see params). Throws
     * {@code IllegalArgumentException} if {@code target == coSort}, because in
     * this case no sorting will be perfomed .
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static void quickSort1(int target[], int fromIndex, int length, int[] coSort) {
        if (target == coSort)
            throw new IllegalArgumentException();
        quickSort2(target, fromIndex, length, coSort);
    }

    private static void quickSort2(int target[], int fromIndex, int length, int[] coSort) {
        // Insertion quickSort on smallest arrays
        if (length < 7) {
            for (int i = fromIndex; i < length + fromIndex; i++)
                for (int j = i; j > fromIndex && target[j - 1] > target[j]; j--)
                    swap(target, j, j - 1, coSort);
            return;
        }

        // Choose a partition element, v
        int m = fromIndex + (length >> 1);       // Small arrays, middle element
        if (length > 7) {
            int l = fromIndex;
            int n = fromIndex + length - 1;
            if (length > 40) {        // Big arrays, pseudomedian of 9
                int s = length / 8;
                l = med3(target, l, l + s, l + 2 * s);
                m = med3(target, m - s, m, m + s);
                n = med3(target, n - 2 * s, n - s, n);
            }
            m = med3(target, l, m, n); // Mid-size, med of 3
        }
        int v = target[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = fromIndex, b = a, c = fromIndex + length - 1, d = c;
        while (true) {
            while (b <= c && target[b] <= v) {
                if (target[b] == v)
                    swap(target, a++, b, coSort);
                b++;
            }
            while (c >= b && target[c] >= v) {
                if (target[c] == v)
                    swap(target, c, d--, coSort);
                c--;
            }
            if (b > c)
                break;
            swap(target, b++, c--, coSort);
        }

        // Swap partition elements back to middle
        int s, n = fromIndex + length;
        s = Math.min(a - fromIndex, b - a);
        vecswap(target, fromIndex, b - s, s, coSort);
        s = Math.min(d - c, n - d - 1);
        vecswap(target, b, n - s, s, coSort);

        // Recursively quickSort non-partition-elements
        if ((s = b - a) > 1)
            quickSort2(target, fromIndex, s, coSort);
        if ((s = d - c) > 1)
            quickSort2(target, n - s, s, coSort);

    }

    private static void swap(int x[], int a, int b, int[] coSort) {
        swap(x, a, b);
        swap(coSort, a, b);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void vecswap(int x[], int a, int b, int n, int[] coSort) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, coSort);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(int x[], int a, int b, int c) {
        return (x[a] < x[b]
                ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
                : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified target array of ints into ascending numerical order
     * and simultaneously permutes the {@code coSort} longs array in the same
     * way then specified target array.
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     *
     */
    public static void quickSort(int[] target, long[] coSort) {
        quickSort1(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into
     * ascending numerical order and simultaneously permutes the {@code coSort}
     * longs array in the same way then specified target array. The range to be
     * sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.)<p>
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be performed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, long[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, long[])  )
     * }, but without range checking.
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be performed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     */
    public static void quickSort1(int target[], int fromIndex, int length, long[] coSort) {
        // Insertion quickSort on smallest arrays
        if (length < 7) {
            for (int i = fromIndex; i < length + fromIndex; i++)
                for (int j = i; j > fromIndex && target[j - 1] > target[j]; j--)
                    swap(target, j, j - 1, coSort);
            return;
        }

        // Choose a partition element, v
        int m = fromIndex + (length >> 1);       // Small arrays, middle element
        if (length > 7) {
            int l = fromIndex;
            int n = fromIndex + length - 1;
            if (length > 40) {        // Big arrays, pseudomedian of 9
                int s = length / 8;
                l = med3(target, l, l + s, l + 2 * s);
                m = med3(target, m - s, m, m + s);
                n = med3(target, n - 2 * s, n - s, n);
            }
            m = med3(target, l, m, n); // Mid-size, med of 3
        }
        int v = target[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = fromIndex, b = a, c = fromIndex + length - 1, d = c;
        while (true) {
            while (b <= c && target[b] <= v) {
                if (target[b] == v)
                    swap(target, a++, b, coSort);
                b++;
            }
            while (c >= b && target[c] >= v) {
                if (target[c] == v)
                    swap(target, c, d--, coSort);
                c--;
            }
            if (b > c)
                break;
            swap(target, b++, c--, coSort);
        }

        // Swap partition elements back to middle
        int s, n = fromIndex + length;
        s = Math.min(a - fromIndex, b - a);
        vecswap(target, fromIndex, b - s, s, coSort);
        s = Math.min(d - c, n - d - 1);
        vecswap(target, b, n - s, s, coSort);

        // Recursively quickSort non-partition-elements
        if ((s = b - a) > 1)
            quickSort1(target, fromIndex, s, coSort);
        if ((s = d - c) > 1)
            quickSort1(target, n - s, s, coSort);

    }

    private static void swap(int x[], int a, int b, long[] coSort) {
        swap(x, a, b);
        swap(coSort, a, b);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(long x[], int a, int b) {
        long t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void vecswap(int x[], int a, int b, int n, long[] coSort) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, coSort);
    }

    /**
     * Sorts the specified target array of objects into ascending order,
     * according to the natural ordering of its elements and simultaneously
     * permutes the {@code coSort} objects array in the same way then specified
     * target array.
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static <T extends Comparable<T>> void quickSort(T[] target, Object[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified target array of objects into ascending order,
     * according to the natural ordering of its elements and simultaneously
     * permutes the {@code coSort} objects array in the same way then specified
     * target array. The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     * @throws IllegalArgumentException       if target == coSort (as
     *                                        references).
     */
    public static <T extends Comparable<T>> void quickSort(T[] target, int fromIndex, int toIndex, Object[] coSort) {
        if (target == coSort)
            throw new IllegalArgumentException();
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(T[], int, int, java.lang.Object[])
     * }, but without range checking.
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static <T extends Comparable<T>> void quickSort1(T[] target, int fromIndex, int length, Object[] coSort) {
        // Insertion quickSort on smallest arrays
        if (length < 7) {
            for (int i = fromIndex; i < length + fromIndex; i++)
                for (int j = i; j > fromIndex && target[j - 1].compareTo(target[j]) > 0; j--)
                    swap(target, j, j - 1, coSort);
            return;
        }

        // Choose a partition element, v
        int m = fromIndex + (length >> 1);       // Small arrays, middle element
        if (length > 7) {
            int l = fromIndex;
            int n = fromIndex + length - 1;
            if (length > 40) {        // Big arrays, pseudomedian of 9
                int s = length / 8;
                l = med3(target, l, l + s, l + 2 * s);
                m = med3(target, m - s, m, m + s);
                n = med3(target, n - 2 * s, n - s, n);
            }
            m = med3(target, l, m, n); // Mid-size, med of 3
        }
        T v = target[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = fromIndex, b = a, c = fromIndex + length - 1, d = c;
        while (true) {
            while (b <= c && target[b].compareTo(v) <= 0) {
                if (target[b] == v)
                    swap(target, a++, b, coSort);
                b++;
            }
            while (c >= b && target[c].compareTo(v) >= 0) {
                if (target[c] == v)
                    swap(target, c, d--, coSort);
                c--;
            }
            if (b > c)
                break;
            swap(target, b++, c--, coSort);
        }

        // Swap partition elements back to middle
        int s, n = fromIndex + length;
        s = Math.min(a - fromIndex, b - a);
        vecswap(target, fromIndex, b - s, s, coSort);
        s = Math.min(d - c, n - d - 1);
        vecswap(target, b, n - s, s, coSort);

        // Recursively quickSort non-partition-elements
        if ((s = b - a) > 1)
            quickSort1(target, fromIndex, s, coSort);
        if ((s = d - c) > 1)
            quickSort1(target, n - s, s, coSort);

    }

    private static void swap(Object[] x, int a, int b, Object[] coSort) {
        swap(x, a, b);
        swap(coSort, a, b);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static <T extends Comparable<T>> int med3(T[] x, int a, int b, int c) {
        return (x[a].compareTo(x[b]) < 0
                ? (x[b].compareTo(x[c]) < 0 ? b : x[a].compareTo(x[c]) < 0 ? c : a)
                : (x[b].compareTo(x[c]) > 0 ? b : x[a].compareTo(x[c]) > 0 ? c : a));
    }

    private static void vecswap(Object[] x, int a, int b, int n, Object[] coSort) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, coSort);
    }

    /**
     * Sorts the specified target array of ints into ascending numerical order
     * and simultaneously permutes the {@code coSort} Objects array in the same
     * way then specified target array.
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     */
    public static void quickSort(int[] target, Object[] coSort) {
        quickSort1(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into
     * ascending numerical order and simultaneously permutes the {@code coSort}
     * Objects array in the same way then specified target array. The range to
     * be sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.)<p>
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, Object[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, Objects[])  )
     * }, but without range checking.
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     */
    public static void quickSort1(int target[], int fromIndex, int length, Object[] coSort) {
        // Insertion quickSort on smallest arrays
        if (length < 7) {
            for (int i = fromIndex; i < length + fromIndex; i++)
                for (int j = i; j > fromIndex && target[j - 1] > target[j]; j--)
                    swap(target, j, j - 1, coSort);
            return;
        }

        // Choose a partition element, v
        int m = fromIndex + (length >> 1);       // Small arrays, middle element
        if (length > 7) {
            int l = fromIndex;
            int n = fromIndex + length - 1;
            if (length > 40) {        // Big arrays, pseudomedian of 9
                int s = length / 8;
                l = med3(target, l, l + s, l + 2 * s);
                m = med3(target, m - s, m, m + s);
                n = med3(target, n - 2 * s, n - s, n);
            }
            m = med3(target, l, m, n); // Mid-size, med of 3
        }
        int v = target[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = fromIndex, b = a, c = fromIndex + length - 1, d = c;
        while (true) {
            while (b <= c && target[b] <= v) {
                if (target[b] == v)
                    swap(target, a++, b, coSort);
                b++;
            }
            while (c >= b && target[c] >= v) {
                if (target[c] == v)
                    swap(target, c, d--, coSort);
                c--;
            }
            if (b > c)
                break;
            swap(target, b++, c--, coSort);
        }

        // Swap partition elements back to middle
        int s, n = fromIndex + length;
        s = Math.min(a - fromIndex, b - a);
        vecswap(target, fromIndex, b - s, s, coSort);
        s = Math.min(d - c, n - d - 1);
        vecswap(target, b, n - s, s, coSort);

        // Recursively quickSort non-partition-elements
        if ((s = b - a) > 1)
            quickSort1(target, fromIndex, s, coSort);
        if ((s = d - c) > 1)
            quickSort1(target, n - s, s, coSort);

    }

    private static void swap(int x[], int a, int b, Object[] coSort) {
        swap(x, a, b);
        swap(coSort, a, b);
    }

    private static void vecswap(int x[], int a, int b, int n, Object[] coSort) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, coSort);
    }

    public static int[] quickSortP(short[] target) {
        int[] permutation = new int[target.length];
        for (int i = 1; i < target.length; ++i)
            permutation[i] = i;
        quickSort(target, 0, target.length, permutation);
        return permutation;
    }

    /**
     * Sorts the specified target array of shorts into ascending numerical order
     * and simultaneously permutes the {@code coSort} ints array in the same way
     * then specified target array.
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: remember this is unstable sort algorithm, so additional
     * combinatorics of the {@code coSort} array can be perfomed. Use this
     * method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * <p><b>NOTE:</b> The method throws {@code IllegalArgumentException} if
     * {@code target == coSort}, because in this case no sorting will be
     * perfomed.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the
     *               specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException if coSort length less then target
     *                                  length.
     */
    public static void quickSort(short[] target, int[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into
     * ascending numerical order and simultaneously permutes the {@code coSort}
     * ints array in the same way then specified target array. The range to be
     * sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.)<p>
     *
     * The code was taken from the jdk6 Arrays class.
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * <p><b>NOTE: remember this is unstable sort algorithm, so additional
     * combinatorics of the {@code coSort} array can be perfomed. Use this
     * method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * <p><b>NOTE:</b> The method throws {@code IllegalArgumentException} if
     * {@code target == coSort}, because in this case no sorting will be
     * perfomed.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *                                        <tt>toIndex &gt; target.length</tt>
     *                                        or <tt>toIndex &gt;
     *                                        coSort.length</tt>
     */
    public static void quickSort(short[] target, int fromIndex, int toIndex, int[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, int[]) },
     * but without range checking and toIndex -> length (see params). Throws
     * {@code IllegalArgumentException} if {@code target == coSort}, because in
     * this case no sorting will be perfomed .
     *
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics
     * of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then
     *                  the specified target array, during sorting procedure
     *
     */
    public static void quickSort1(short target[], int fromIndex, int length, int[] coSort) {
        quickSort2(target, fromIndex, length, coSort);
    }

    private static void quickSort2(short target[], int fromIndex, int length, int[] coSort) {
        // Insertion quickSort on smallest arrays
        if (length < 7) {
            for (int i = fromIndex; i < length + fromIndex; i++)
                for (int j = i; j > fromIndex && target[j - 1] > target[j]; j--)
                    swap(target, j, j - 1, coSort);
            return;
        }

        // Choose a partition element, v
        int m = fromIndex + (length >> 1);       // Small arrays, middle element
        if (length > 7) {
            int l = fromIndex;
            int n = fromIndex + length - 1;
            if (length > 40) {        // Big arrays, pseudomedian of 9
                int s = length / 8;
                l = med3(target, l, l + s, l + 2 * s);
                m = med3(target, m - s, m, m + s);
                n = med3(target, n - 2 * s, n - s, n);
            }
            m = med3(target, l, m, n); // Mid-size, med of 3
        }
        int v = target[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = fromIndex, b = a, c = fromIndex + length - 1, d = c;
        while (true) {
            while (b <= c && target[b] <= v) {
                if (target[b] == v)
                    swap(target, a++, b, coSort);
                b++;
            }
            while (c >= b && target[c] >= v) {
                if (target[c] == v)
                    swap(target, c, d--, coSort);
                c--;
            }
            if (b > c)
                break;
            swap(target, b++, c--, coSort);
        }

        // Swap partition elements back to middle
        int s, n = fromIndex + length;
        s = Math.min(a - fromIndex, b - a);
        vecswap(target, fromIndex, b - s, s, coSort);
        s = Math.min(d - c, n - d - 1);
        vecswap(target, b, n - s, s, coSort);

        // Recursively quickSort non-partition-elements
        if ((s = b - a) > 1)
            quickSort2(target, fromIndex, s, coSort);
        if ((s = d - c) > 1)
            quickSort2(target, n - s, s, coSort);

    }

    private static void swap(short x[], int a, int b, int[] coSort) {
        swap(x, a, b);
        swap(coSort, a, b);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(short x[], int a, int b) {
        short t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void vecswap(short x[], int a, int b, int n, int[] coSort) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, coSort);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(short x[], int a, int b, int c) {
        return (x[a] < x[b]
                ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
                : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an appropriate
     * exception if they aren't.
     */
    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }
}

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

import cc.redberry.core.math.MathUtils;
import cc.redberry.core.tensor.Tensor;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * This class contains additional methods for manipulating arrays (such as sorting and searching). For all quick sort
 * methods the base code was taken from jdk6 {@link Arrays} class.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Arrays
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
     * This method is similar to {@link #bijection(Comparable[], Comparable[])}  }, but uses specified {@code
     * comparator}.
     *
     * @param from       from array
     * @param to         to array
     * @param comparator comparator
     * @return a bijective mapping from {@code from}-array to {@code to}-array and {@code null} if no mapping exist
     */
    public static <T> int[] bijection(T[] from, T[] to, Comparator<? super T> comparator) {
        //TODO refactor with sorting !!!!
        if (from.length != to.length)
            return null;
        int length = from.length;
        int[] bijection = new int[length];
        Arrays.fill(bijection, -1);
        int i, j;
        OUT:
        for (i = 0; i < length; ++i) {
            for (j = 0; j < length; ++j)
                if (bijection[j] == -1 && comparator.compare(from[i], to[j]) == 0) {
                    bijection[j] = i;
                    continue OUT;
                }
            return null;
        }
        return bijection;
    }

    /**
     * Creates a bijective mapping between two arrays and returns the resulting bijection as array. Method returns null,
     * if no mapping found. <p/>
     * <p>Example: <blockquote><pre>
     *      Integer from[] = {1,2,1,4};
     *      Integer to[] = {2,4,1,1};
     *      int[] bijection = bijection(from,to);
     * </pre></blockquote>
     * <p/> <p> The resulting bijection will be {@code [2, 0, 3, 1]}
     *
     * @param from from array
     * @param to   to array
     * @return a bijective mapping from {@code from}-array to {@code to}-array and {@code null} if no mapping exist
     */
    public static <T extends Comparable<? super T>> int[] bijection(T[] from, T[] to) {
        //TODO refactor with sorting !!!!
        if (from.length != to.length)
            return null;
        int length = from.length;
        int[] bijection = new int[length];
        Arrays.fill(bijection, -1);
        int i, j;
        OUT:
        for (i = 0; i < length; ++i) {
            for (j = 0; j < length; ++j)
                if (bijection[j] == -1 && from[i].compareTo(to[j]) == 0) {
                    bijection[j] = i;
                    continue OUT;
                }
            return null;
        }
        return bijection;
    }

    public static Tensor[] addAll(Tensor[] array1, Tensor... array2) {
        Tensor[] r = new Tensor[array1.length + array2.length];
        System.arraycopy(array1, 0, r, 0, array1.length);
        System.arraycopy(array2, 0, r, array1.length, array2.length);
        return r;
    }

    public static int[] addAll(int[] array1, int... array2) {
        int[] r = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, r, 0, array1.length);
        System.arraycopy(array2, 0, r, array1.length, array2.length);
        return r;
    }

    public static int[] addAll(int[]... arrays) {
        if (arrays.length == 0)
            return new int[0];
        int i, length = 0;
        for (i = 0; i < arrays.length; ++i)
            length += arrays[i].length;
        if (length == 0)
            return new int[0];
        int[] r = new int[length];
        int pointer = 0;
        for (i = 0; i < arrays.length; ++i) {
            System.arraycopy(arrays[i], 0, r, pointer, arrays[i].length);
            pointer += arrays[i].length;
        }
        return r;
    }


    public static Tensor[] remove(Tensor[] array, int i) {
        Tensor[] r = new Tensor[array.length - 1];
        System.arraycopy(array, 0, r, 0, i);
        if (i < array.length - 1)
            System.arraycopy(array, i + 1, r, i, array.length - i - 1);
        return r;
    }

    public static <T> T[] remove(T[] array, int i) {
        @SuppressWarnings("unchecked")
        T[] r = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, r, 0, i);
        if (i < array.length - 1)
            System.arraycopy(array, i + 1, r, i, array.length - i - 1);
        return r;
    }

    /**
     * This code is taken from Apache Commons Lang ArrayUtils. <p/> <p>Adds all the elements of the given arrays into a
     * new array.</p> <p>The new array contains all of the element of {@code array1} followed by all of the elements
     * {@code array2}. When an array is returned, it is always a new array.</p> <p/>
     * <pre>
     * ArrayUtils.addAll(null, null)     = null
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * ArrayUtils.addAll([null], [null]) = [null, null]
     * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param array1 the first array whose elements are added to the new array, may be {@code null}
     * @param array2 the second array whose elements are added to the new array, may be {@code null}
     * @return The new array, {@code null} if both arrays are {@code null}. The type of the new array is the type of the
     *         first array, unless the first array is null, in which case the type is the same as the second array.
     * @throws IllegalArgumentException if the array types are incompatible
     * @since 2.1
     */
    @SafeVarargs
    public static <T> T[] addAll(T[] array1, T... array2) {
        if (array1 == null)
            return array2.clone();
        else if (array2 == null)
            return array1.clone();
        final Class<?> type1 = array1.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
                T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because: - it would
             * be a wasted check most of the time - safer, in case check turns
             * out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2))
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                        + type1.getName(), ase);
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    public static <T> T[] remove(T[] array, int[] positions) {
        if (array == null)
            throw new NullPointerException();
        int[] p = MathUtils.getSortedDistinct(positions);

        int size = p.length, pointer = 0, s = array.length;
        for (; pointer < size; ++pointer)
            if (p[pointer] >= s)
                throw new ArrayIndexOutOfBoundsException();

        final Class<?> type = array.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
                T[] r = (T[]) Array.newInstance(type, array.length - p.length);

        pointer = 0;
        int i = -1;
        for (int j = 0; j < s; ++j) {
            if (pointer < size - 1 && j > p[pointer])
                ++pointer;
            if (j == p[pointer]) continue;
            else r[++i] = array[j];
        }
        return r;
    }

    public static <T> T[] select(T[] array, int[] positions) {
        if (array == null)
            throw new NullPointerException();
        int[] p = MathUtils.getSortedDistinct(positions);
        final Class<?> type = array.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
                T[] r = (T[]) Array.newInstance(type, p.length);
        int i = -1;
        for (int j : p)
            r[++i] = array[j];
        return r;
    }

    public static int[] toArray(Set<Integer> set) {
        int i = -1;
        int[] a = new int[set.size()];
        for (Integer ii : set)
            a[++i] = ii;
        return a;
    }

    public static int binarySearch(IntArrayList list, int key) {
        return Arrays.binarySearch(list.data, 0, list.size, key);
    }

    /**
     * This is the same method to {@link Arrays#binarySearch(int[], int) }. The differs is in the returned value. If key
     * not found, this method returns the position of the first element, witch is closest to key (i.e. if
     * Arrays.binarySearch returns {@code -low-1}, this method returns {@code low}).
     *
     * @param a   the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array; otherwise, <tt><i>insertion point</i></tt>. The
     *         <i>insertion point</i> is defined as the point at which the key would be inserted into the array: the
     *         index of the first element greater than the key, or <tt>a.length</tt> if all elements in the array are
     *         less than the specified key.
     */
    public static int binarySearch1(int[] a, int key) {
        return binarySearch1(a, 0, a.length, key);
    }

    /**
     * This is the same method to {@link Arrays#binarySearch(int[], int, int, int) }. The differs is in the returned
     * value. If key not found, this method returns the position of the first element, witch is closest to key (i.e. if
     * Arrays.binarySearch returns {@code -low-1}, this method returns {@code low}).
     *
     * @param a         the array to be searched
     * @param key       the value to be searched for
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @return index of the search key, if it is contained in the array; otherwise, <tt><i>insertion point</i></tt>. The
     *         <i>insertion point</i> is defined as the point at which the key would be inserted into the array: the
     *         index of the first element greater than the key, or <tt>toIndex</tt> if all elements in the array are
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
     * Returns xor of objects hashes spreaded by {@link HashFunctions#JenkinWang32shift(int) }
     *
     * @param objects array
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
     * Returns xor of objects hashes spreaded by {@link HashFunctions#JenkinWang32shift(int) }
     *
     * @param objects array
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
     * Sorts the specified array of ints into ascending order using insertion sort algorithm and simultaneously permutes
     * the {@code coSort} ints array in the same way then specified target array. This sort guarantee O(n^2) performance
     * in the worst case and O(n) in the best case (nearly sorted input). <p/> <p> This sort is the best choice for
     * small arrays with elements number < 100. <p/> <p>This sort is guaranteed to be <i>stable</i>: equal elements will
     * not be reordered as a result of the sort; <i>adaptive</i>: performance adapts to the initial order of elements
     * and <i>in-place</i>: requires constant amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static void insertionSort(int[] target, int[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion sort algorithm and simultaneously permutes
     * the {@code coSort} ints array in the same way then specified target array. This sort guarantee O(n^2) performance
     * in the worst case and O(n) in the best case (nearly sorted input). The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.) <p/> <p> This sort is the best choice for small arrays with elements number < 100. <p/>
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the sort;
     * <i>adaptive</i>: performance adapts to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
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
     * Sorts the specified array of ints into ascending order using insertion sort algorithm and simultaneously permutes
     * the {@code coSort} longs array in the same way then specified target array. This sort guarantee O(n^2)
     * performance in the worst case and O(n) in the best case (nearly sorted input). <p/> <p> This sort is the best
     * choice for small arrays with elements number < 100. <p/> <p>This sort is guaranteed to be <i>stable</i>: equal
     * elements will not be reordered as a result of the sort; <i>adaptive</i>: performance adapts to the initial order
     * of elements and <i>in-place</i>: requires constant amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static void insertionSort(int[] target, long[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified array of ints into ascending order using insertion sort algorithm and simultaneously permutes
     * the {@code coSort} ints array in the same way then specified target array. This sort guarantee O(n^2) performance
     * in the worst case and O(n) in the best case (nearly sorted input). The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range
     * to be sorted is empty.) <p/> <p> This sort is the best choice for small arrays with elements number < 100. <p/>
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the sort;
     * <i>adaptive</i>: performance adapts to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
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
     * Sorts the specified target array of objects into ascending order, according to the natural ordering of its
     * elements using insertion sort algorithm and simultaneously permutes the {@code coSort} objects array in the same
     * way then specified target array. This sort guarantee O(n^2) performance in the worst case and O(n) in the best
     * case (nearly sorted input). <p/> <p> This sort is the best choice for small arrays with elements number < 100.
     * <p/> <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the
     * sort; <i>adaptive</i>: performance adapts to the initial order of elements and <i>in-place</i>: requires constant
     * amount of additional space.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static <T extends Comparable<T>> void insertionSort(T[] target, Object[] coSort) {
        insertionSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified target array of objects into ascending order, according to the natural ordering of its
     * elements using insertion sort algorithm and simultaneously permutes the {@code coSort} objects array in the same
     * way then specified target array. This sort guarantee O(n^2) performance in the worst case and O(n) in the best
     * case (nearly sorted input). The range to be sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.) <p/> <p> This
     * sort is the best choice for small arrays with elements number < 100. <p/> <p>This sort is guaranteed to be
     * <i>stable</i>: equal elements will not be reordered as a result of the sort; <i>adaptive</i>: performance adapts
     * to the initial order of elements and <i>in-place</i>: requires constant amount of additional space.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
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
     * Sorts the specified array of ints into ascending order using TimSort algorithm and simultaneously permutes the
     * {@code coSort} ints array in the same way then specified target array. <p/> <p> NOTE: using of this method is
     * very good for large arrays with more then 100 elements, in other case using of insertion sort is highly
     * recommended. <p/> <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a
     * result of the sort. <p/> <p> The code was taken from {@link Arrays#sort(java.lang.Object[]) } and adapted for
     * integers. For more information look there.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> (for example,
     *                            strings and integers)
     * @see Arrays#sort(java.lang.Object[])
     */
    public static void timSort(int target[], int[] coSort) {
        IntTimSort.sort(target, coSort);
    }

    /**
     * Sorts the specified array of ints into ascending order using stable sort algorithm and simultaneously permutes
     * the {@code coSort} ints array in the same way then specified target array. If length of specified array is less
     * than 100 - insertion sort algorithm performed, otherwise - TimSort. <p/> <p>This sort is guaranteed to be
     * <i>stable</i>: equal elements will not be reordered as a result of the sort.
     *
     * @param target the array to be sorted
     * @param cosort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws ClassCastException       if the array contains elements that are not <i>mutually comparable</i> (for
     *                                  example, strings and integers)
     * @throws IllegalArgumentException if coSort length less then target length.
     * @throws IllegalArgumentException if target == coSort (as references).
     * @see #insertionSort(int[], int[])
     * @see #timSort(int[], int[])
     */
    public static void stableSort(int target[], int[] cosort) {
        if (target.length > 100)
            ArraysUtils.timSort(target, cosort);
        else
            ArraysUtils.insertionSort(target, cosort);
    }

    /**
     * Sorts the specified target array of ints into ascending numerical order and simultaneously permutes the {@code
     * coSort} ints array in the same way then specified target array. <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p><b>NOTE: remember this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b> <p/> <p><b>NOTE:</b> The method throws {@code
     * IllegalArgumentException} if {@code target == coSort}, because in this case no sorting will be perfomed.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static void quickSort(int[] target, int[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into ascending numerical order and simultaneously
     * permutes the {@code coSort} ints array in the same way then specified target array. The range to be sorted
     * extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p> <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p><b>NOTE: remember this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b> <p/> <p><b>NOTE:</b> The method throws {@code
     * IllegalArgumentException} if {@code target == coSort}, because in this case no sorting will be perfomed.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
     * @throws IllegalArgumentException       if target == coSort (as references).
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, int[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, int[]) }, but without range checking and toIndex ->
     * length (see params). Throws {@code IllegalArgumentException} if {@code target == coSort}, because in this case no
     * sorting will be perfomed . <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the
     * {@code coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use
     * stable sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static void quickSort1(int target[], int fromIndex, int length, int[] coSort) {
        if (target == coSort)
            throw new IllegalArgumentException("Target reference == coSort reference.");
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
     * Sorts the specified target array of ints into ascending numerical order and simultaneously permutes the {@code
     * coSort} longs array in the same way then specified target array. <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static void quickSort(int[] target, long[] coSort) {
        quickSort1(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into ascending numerical order and simultaneously
     * permutes the {@code coSort} longs array in the same way then specified target array. The range to be sorted
     * extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p> <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be performed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, long[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, long[])  ) }, but without range checking. <p/>
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code coSort} array can be
     * performed. Use this method only if you are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
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
     * Sorts the specified target array of objects into ascending order, according to the natural ordering of its
     * elements and simultaneously permutes the {@code coSort} objects array in the same way then specified target
     * array. <p/> The code was taken from the jdk6 Arrays class. <p/> The sorting algorithm is a tuned quicksort,
     * adapted from Jon L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice and
     * Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data
     * sets that cause other quicksorts to degrade to quadratic performance. <p/> <p><b>NOTE: this is unstable sort
     * algorithm, so additional combinatorics of the {@code coSort} array can be perfomed. Use this method only if you
     * are sure, in what you are doing. If not - use stable sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws IllegalArgumentException if coSort length less then target length.
     * @throws IllegalArgumentException if target == coSort (as references).
     */
    public static <T extends Comparable<T>> void quickSort(T[] target, Object[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified target array of objects into ascending order, according to the natural ordering of its
     * elements and simultaneously permutes the {@code coSort} objects array in the same way then specified target
     * array. The range to be sorted extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>,
     * exclusive. (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p> <p/> The code was taken from the
     * jdk6 Arrays class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas
     * McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993). This algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to
     * quadratic performance. <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the
     * {@code coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use
     * stable sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
     * @throws IllegalArgumentException       if target == coSort (as references).
     */
    public static <T extends Comparable<T>> void quickSort(T[] target, int fromIndex, int toIndex, Object[] coSort) {
        if (target == coSort)
            throw new IllegalArgumentException();
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(T[], int, int, java.lang.Object[]) }, but without range checking.
     * <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code coSort} array can be
     * perfomed. Use this method only if you are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
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
     * Sorts the specified target array of ints into ascending numerical order and simultaneously permutes the {@code
     * coSort} Objects array in the same way then specified target array. <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static void quickSort(int[] target, Object[] coSort) {
        quickSort1(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into ascending numerical order and simultaneously
     * permutes the {@code coSort} Objects array in the same way then specified target array. The range to be sorted
     * extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p> <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
     */
    public static void quickSort(int[] target, int fromIndex, int toIndex, Object[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, Object[])  ) }, but without range checking. <p/>
     * <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the {@code coSort} array can be
     * perfomed. Use this method only if you are sure, in what you are doing. If not - use stable sort methods like an
     * insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
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
     * Sorts the specified target array of shorts into ascending numerical order and simultaneously permutes the {@code
     * coSort} ints array in the same way then specified target array. <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p><b>NOTE: remember this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b> <p/> <p><b>NOTE:</b> The method throws {@code
     * IllegalArgumentException} if {@code target == coSort}, because in this case no sorting will be perfomed.
     *
     * @param target the array to be sorted
     * @param coSort the array, which will be permuted in the same way, then the specified target array, during sorting
     *               procedure
     * @throws IllegalArgumentException if coSort length less then target length.
     */
    public static void quickSort(short[] target, int[] coSort) {
        quickSort(target, 0, target.length, coSort);
    }

    /**
     * Sorts the specified range of the specified target array of ints into ascending numerical order and simultaneously
     * permutes the {@code coSort} ints array in the same way then specified target array. The range to be sorted
     * extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p> <p/> The code was taken from the jdk6 Arrays
     * class. <p/> The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
     * algorithm offers n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic
     * performance. <p/> <p><b>NOTE: remember this is unstable sort algorithm, so additional combinatorics of the {@code
     * coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use stable
     * sort methods like an insertion sort or Tim sort.</b> <p/> <p><b>NOTE:</b> The method throws {@code
     * IllegalArgumentException} if {@code target == coSort}, because in this case no sorting will be perfomed.
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; target.length</tt> or
     *                                        <tt>toIndex &gt; coSort.length</tt>
     */
    public static void quickSort(short[] target, int fromIndex, int toIndex, int[] coSort) {
        rangeCheck(target.length, fromIndex, toIndex);
        rangeCheck(coSort.length, fromIndex, toIndex);
        quickSort1(target, fromIndex, toIndex - fromIndex, coSort);
    }

    /**
     * This method is the same as {@link #quickSort(int[], int, int, int[]) }, but without range checking and toIndex ->
     * length (see params). Throws {@code IllegalArgumentException} if {@code target == coSort}, because in this case no
     * sorting will be perfomed . <p/> <p><b>NOTE: this is unstable sort algorithm, so additional combinatorics of the
     * {@code coSort} array can be perfomed. Use this method only if you are sure, in what you are doing. If not - use
     * stable sort methods like an insertion sort or Tim sort.</b>
     *
     * @param target    the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param length    the length of the sorting subarray.
     * @param coSort    the array, which will be permuted in the same way, then the specified target array, during
     *                  sorting procedure
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
     * Check that fromIndex and toIndex are in range, and throw an appropriate exception if they aren't.
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

    public static <T> String toString(T[] a, ToStringConverter<T> format) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(format.toString(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public static String toString(int[] a, ToStringConverter<Integer> format) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(format.toString(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
}

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
package cc.redberry.core.combinatorics;

import java.util.Arrays;
import java.util.Iterator;

/**
 * This class represents iterator over all possible combinatorics of specified
 * dimension. Permutation receiving by {@code next()} represents integer array -
 * permutation in one-line notation. Number of all combinatorics with dimension D
 * is D!.
 *
 * <p>Example <blockquote><pre>
 *      IntPermutationsGenerator ig = new IntPermutationsGenerator(3);
 *      while (ig.hasNext())
 *          System.out.println(Arrays.toString(ig.next()))
 * </pre></blockquote> <p>The result will be <blockquote><pre>
 *      [0, 1, 2]
 *      [0, 2, 1]
 *      [1, 0, 2]
 *      [1, 2, 0]
 *      [2, 0, 1]
 *      [2, 1, 0]
 * </pre></blockquote>
 *
 * <p>This class provides opportunities to iterate both in straight and forward
 * direction.
 *
 * <p><b>NOTE:</b> Caring about performance, this class does not create a
 * <i>new</i> instance of integer array on each iteration, but permutes elements
 * in private array and returns result. So, on each iteration it returns
 * reference on the same array. The bellow code will illustrates this note <blockquote><pre>
 *      IntPermutationsGenerator ig = new IntPermutationsGenerator(3);
 *      List<int[]> list = new ArrayList<>();
 *      while (ig.hasNext())
 *          list.add(ig.next());
 * </pre></blockquote> <p>{@code List} will contains equal arrays [2,1,0]! If
 * you want store combinatorics you must copy data on each iteration, using
 * {@link Arrays#copyOf(int[], int) }.
 *
 * <p><b>NOTE:</b> if dimension is rather big (on typical machines 13),
 * iterating can get a lot of time, due to huge number of combinations.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IntPermutationsGenerator implements IntCombinatoricGenerator {
    final int[] permutation;
    private boolean onFirst = true;
    private final int size;

    /**
     * Construct iterator over combinatorics with specified dimension. Start
     * permutation is identity permutation.
     *
     * @param dimension dimension of combinatorics
     */
    public IntPermutationsGenerator(int dimension) {
        checkSize(dimension);
        permutation = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            permutation[i] = i;
        this.size = dimension;
    }

    /**
     * Construct iterator over combinatorics with specified permutation at start.
     * In this way, iterator will not iterate over all possible combinatorics,
     * but only from start permutation up to last permutation, witch is
     * [size-1,size-2,....1,0]. NOTE: parameter {@code permutation} is not
     * coping in constructor, so it will changing during iteration, as it was
     * says in class documentation.
     *
     * @param permutation start permutation of iterator.
     * @throws IllegalArgumentException if permutation is inconsistent with
     * <i>single-line</i> notation
     */
    public IntPermutationsGenerator(int[] permutation) {
        this.permutation = permutation;
        this.size = permutation.length;
        for (int i = 0; i < size - 1; ++i) {
            if (permutation[i] >= size || permutation[i] < 0)
                throw new IllegalArgumentException("Wrong permutation input: image of " + i + " element"
                        + " greater then dimension");
            for (int j = i + 1; j < size; ++j)
                if (permutation[i] == permutation[j])
                    throw new IllegalArgumentException("Wrong permutation input: to elemets have the same image");
        }
        checkSize(size);
    }

    private void checkSize(int size) {
        if (size >= 11)
            System.out.println("Initializing PermutationsGenerator with size = " + size + ". Iteration may take awhile.");
    }

    /**
     * Returns {@code true} if the iteration has more elements, iterating in
     * straight order. (In other words, returns {@code true} if {@link #next}
     * would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return !isLast() || onFirst;
    }

    /**
     * Returns {@code true} if the iteration has more elements, iterating in
     * back order. (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    public boolean hasPrevious() {
        return !isFirst();
    }

    private boolean isLast() {
        for (int i = 0; i < size; i++)
            if (permutation[i] != size - 1 - i)
                return false;
        return true;
    }

    private boolean isFirst() {
        for (int i = 0; i < size; i++)
            if (permutation[i] != i)
                return false;
        return true;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    @Override
    public int[] next() {
        if (onFirst) {
            onFirst = false;
            return permutation;
        }
        final int end = size - 1;
        int p = end, low, high, med, s;
        while ((p > 0) && (permutation[p] < permutation[p - 1]))
            p--;
        if (p > 0) //if p==0 then it's the last one
        {
            s = permutation[p - 1];
            if (permutation[end] > s)
                low = end;
            else {
                high = end;
                low = p;
                while (high > low + 1) {
                    med = (high + low) >> 1;
                    if (permutation[med] < s)
                        high = med;
                    else
                        low = med;
                }
            }
            permutation[p - 1] = permutation[low];
            permutation[low] = s;
        }
        high = end;
        while (high > p) {
            med = permutation[high];
            permutation[high] = permutation[p];
            permutation[p] = med;
            p++;
            high--;
        }
        return permutation;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration
     */
    public int[] previous() {
        int Nm1 = size - 1;
        int p = Nm1, low, high, s, m;
        while ((p > 0) && (permutation[p] > permutation[p - 1]))
            p--;
        if (p > 0) {
            s = permutation[p - 1];
            if (permutation[Nm1] < s)
                low = Nm1;
            else {
                high = Nm1;
                low = p;
                while (high > low + 1) {
                    m = (high + low) >> 1;
                    if (permutation[m] > s)
                        high = m;
                    else
                        low = m;
                }
            }
            permutation[p - 1] = permutation[low];
            permutation[low] = s;
        }
        high = Nm1;
        while (high > p) {
            m = permutation[high];
            permutation[high] = permutation[p];
            permutation[p] = m;
            p++;
            high--;
        }
        return permutation;
    }

    /**
     * Resets iterator.
     */
    @Override
    public void reset() {
        onFirst = true;
        for (int i = 0; i < size; ++i)
            permutation[i] = i;
    }

    /**
     * Throws new UnsupportedOperationException("Not supported yet.").
     *
     * @throws UnsupportedOperationException("Not supported yet.");
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * Returns current permutation
//     * @return current permutation
//     */
//    public int[] getPermutation() {
//        return permutation;
//    }
//
    /**
     * Returns dimension of generating combinatorics.
     *
     * @return dimension of generating combinatorics
     */
    public int getDimension() {
        return size;
    }

    @Override
    public Iterator<int[]> iterator() {
        return this;
    }

    @Override
    public int[] getReference() {
        return permutation;
    }
}
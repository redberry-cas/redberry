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

import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PriorityPermutationGenerator {
    private final IntPermutationsGenerator generator;
    private final List<PermutationPriorityTuple> tuples = new ArrayList<>();
    private final Set<PermutationPriorityTuple> set = new HashSet<>();
    private int[] last = null;
    private int lastTuplePointer = 0;

    public PriorityPermutationGenerator(int dimension) {
        generator = new IntPermutationsGenerator(dimension);
    }

    public PriorityPermutationGenerator(int[] initialPermutation) {
        generator = new IntPermutationsGenerator(initialPermutation);
    }

    public int[] next() {
        if (lastTuplePointer == tuples.size()) {
            if (!generator.hasNext())
                return null;
            int[] next;
            do {
                if (!generator.hasNext())
                    return null;
                next = generator.next();
            } while (set.contains(new PermutationPriorityTuple(next)));
            last = next;
            return next;
        }
        return tuples.get(lastTuplePointer++).permutation;
    }

    public void nice() {
        if (last == null) {
            int index = lastTuplePointer - 1;
            int nPriority = ++tuples.get(index).priority;
            int position = index;
            while (--position >= 0 && tuples.get(position).priority < nPriority);
            ++position;
            swap(position, index);
            return;
        }
        PermutationPriorityTuple tuple = new PermutationPriorityTuple(last.clone());
        set.add(tuple);
        tuples.add(tuple);
        ++lastTuplePointer;
    }

    public void reset() {
        generator.reset();
        lastTuplePointer = 0;
        last = null;
    }

    private void swap(int i, int j) {
        PermutationPriorityTuple permutationPriorityTuple = tuples.get(i);
        tuples.set(i, tuples.get(j));
        tuples.set(j, permutationPriorityTuple);
    }

    private static class PermutationPriorityTuple {
        final int[] permutation;
        int priority;

        PermutationPriorityTuple(int[] permutation) {
            this.permutation = permutation;
            this.priority = 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final PermutationPriorityTuple other = (PermutationPriorityTuple) obj;
            if (!Arrays.equals(this.permutation, other.permutation))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Arrays.hashCode(this.permutation);
            return hash;
        }

        @Override
        public String toString() {
            return Arrays.toString(permutation) + " : " + priority;
        }
    }
}

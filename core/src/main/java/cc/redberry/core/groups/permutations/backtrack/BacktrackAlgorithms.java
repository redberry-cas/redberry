/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.groups.permutations.backtrack;

import cc.redberry.core.groups.permutations.BSGSCandidateElement;
import cc.redberry.core.groups.permutations.Permutation;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class BacktrackAlgorithms {
    private BacktrackAlgorithms() {
    }

    public static Iterator<Permutation> groupElementsIterator(ArrayList<BSGSCandidateElement> BSGS) {
        return null;
    }

    private static class IntComparator {
        final int[] base;

        private IntComparator(int[] base) {
            this.base = base;
        }

        private int indexOf(int a) {
            for (int i = 0; i < base.length; ++i)
                if (a == base[i])
                    return i;
            return -1;
        }

        public int compare(int a, int b) {
            if (a == b)
                return 0;
            return Integer.compare(indexOf(a), indexOf(b));
        }
    }

}

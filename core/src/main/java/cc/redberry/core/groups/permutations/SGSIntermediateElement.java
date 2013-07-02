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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SGSIntermediateElement extends SGSElement {
    /**
     * Orbit of basePoint
     */
    final IntArrayList orbitList;

    SGSIntermediateElement(int basePoint, ArrayList<Permutation> stabilizerGenerators, int length) {
        super(basePoint, stabilizerGenerators, new int[length]);
        this.orbitList = new IntArrayList();
        this.orbitList.add(basePoint);
        reCalculateSchreierVector();
    }

    void reCalculateSchreierVector() {
        orbitList.removeAfter(1);
        Arrays.fill(schreierVector, -2);
        schreierVector[basePoint] = -1;
        int image, stabilizerIndex;
        for (int i = 0; i < orbitList.size(); ++i) {
            for (stabilizerIndex = 0; stabilizerIndex < stabilizerGenerators.size(); ++stabilizerIndex) {
                image = stabilizerGenerators.get(stabilizerIndex).newIndexOf(orbitList.get(i));
                if (schreierVector[image] == -2) {
                    orbitList.add(image);
                    schreierVector[image] = stabilizerIndex;
                }
            }
        }
    }

    void trimToSize() {
        ((ArrayList) stabilizerGenerators).trimToSize();
    }
}

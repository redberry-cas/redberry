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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SGSElement {
    /**
     * Point from the base.
     */
    final int basePoint;
    /**
     * List of generators, that stabilises all point before and including basePoint
     */
    final List<Permutation> stabilizerGenerators;
    /**
     * Schreier vector
     */
    final int[] schreierVector;
    /**
     * Orbit
     */
    final int[] orbit;

    SGSElement(int basePoint, List<Permutation> stabilizerGenerators, int[] schreierVector) {
        this.basePoint = basePoint;
        this.stabilizerGenerators = stabilizerGenerators;
        this.schreierVector = schreierVector;
        this.orbit = null;
    }

    SGSElement(SGSIntermediateElement sgsIntermediateElement) {
        this.schreierVector = sgsIntermediateElement.schreierVector;
        this.basePoint = sgsIntermediateElement.basePoint;
        this.orbit = sgsIntermediateElement.orbitList.toArray();
        this.stabilizerGenerators = Collections.unmodifiableList(sgsIntermediateElement.stabilizerGenerators);
    }

    public int getOrbitSize() {
        return orbit.length;
    }

    public int getOrbitPoint(int i) {
        return orbit[i];
    }

    public int getBasePoint() {
        return basePoint;
    }

    public List<Permutation> getStabilizerGenerators() {
        return stabilizerGenerators;
    }

    boolean belongsToOrbit(int point) {
        return schreierVector[point] != -2;
    }

    public Permutation getTransversalOf(int point) {
        final Permutation transversal = getInverseTransversalOf(point).inverse();

        assert transversal.newIndexOf(basePoint) == point;

        return transversal;
    }

    public Permutation getInverseTransversalOf(int point) {
        Permutation transversal = Combinatorics.getIdentity(schreierVector.length);

        while (schreierVector[transversal.newIndexOf(point)] != -1)
            transversal =
                    transversal.compositionWithInverse(
                            stabilizerGenerators.get(schreierVector[transversal.newIndexOf(point)]));

        return transversal;
    }

    public ArrayList<Permutation> getBasePointStabilizerGenerators() {
        ArrayList<Permutation> bStabilizers = new ArrayList<>();
        for (Permutation gen : stabilizerGenerators)
            if (gen.newIndexOf(basePoint) == basePoint)
                bStabilizers.add(gen);
        return bStabilizers;
    }
}

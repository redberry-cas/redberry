/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
import java.util.Collections;
import java.util.List;

/**
 * A mutable version of {@link BSGSElement}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.BSGSElement
 * @since 1.1.6
 */
public final class BSGSCandidateElement extends BSGSElement {
    /**
     * Basic raw constructor.
     *
     * @param basePoint            base point
     * @param stabilizerGenerators stabilizers
     */
    BSGSCandidateElement(int basePoint, List<Permutation> stabilizerGenerators) {
        this(basePoint, stabilizerGenerators, calculateSVCapacity(stabilizerGenerators));
        internalDegree = SchreierVector.length();
    }

    /**
     * Calculates minimal capacity needed to store Schreier vector
     *
     * @param stabilizerGenerators stabilizers
     * @return minimal capacity needed to store Schreier vector
     */
    private static int calculateSVCapacity(List<Permutation> stabilizerGenerators) {
        int capacity = -1;
        for (Permutation p : stabilizerGenerators)
            capacity = Math.max(capacity, p.internalDegree());
        return capacity;
    }

    /**
     * Basic raw constructor.
     *
     * @param basePoint              base point
     * @param stabilizerGenerators   stabilizers
     * @param SchreierVectorCapacity initial capacity of Schreier vector
     */
    BSGSCandidateElement(int basePoint, List<Permutation> stabilizerGenerators, int SchreierVectorCapacity) {
        super(basePoint, stabilizerGenerators, new SchreierVector(SchreierVectorCapacity), new IntArrayList());
        assert stabilizerGenerators instanceof ArrayList;
        //creating list of orbit points
        orbitList.add(basePoint);
        recalculateOrbitAndSchreierVector();
    }

    private BSGSCandidateElement(int basePoint, List<Permutation> stabilizerGenerators,
                                 SchreierVector schreierVector, IntArrayList orbitList) {
        super(basePoint, stabilizerGenerators, schreierVector, orbitList);
    }

    public void addStabilizer(Permutation stabilizer) {
        internalDegree = Math.max(internalDegree, stabilizer.internalDegree());
        stabilizerGenerators.add(stabilizer);
        recalculateOrbitAndSchreierVector();
    }

    /**
     * Calculates Schreier vector according to the algorithm ORBITSV in Sec. 4.1.1 of  <b>[Holt05]</b>
     */
    void recalculateOrbitAndSchreierVector() {
        //clear orbit list
        orbitList.removeAfter(1);
        //fill Schreier vector with some dummmy values
        SchreierVector.reset();
        //base point
        SchreierVector.set(basePoint, -1);

        int imageOfPoint;
        //main loop over all points in orbit
        for (int orbitIndex = 0; orbitIndex < orbitList.size(); ++orbitIndex) {
            //loop over all generators of a group
            for (int stabilizerIndex = 0, size = stabilizerGenerators.size();
                 stabilizerIndex < size; ++stabilizerIndex) {
                //image of point under permutation
                imageOfPoint = stabilizerGenerators.get(stabilizerIndex).newIndexOf(orbitList.get(orbitIndex));
                //testing whether current permutation maps orbit point into orbit or not
                if (SchreierVector.get(imageOfPoint) == -2) {
                    //adding new point to orbit
                    orbitList.add(imageOfPoint);
                    //filling Schreier vector
                    SchreierVector.set(imageOfPoint, stabilizerIndex);
                }
            }
        }
    }

    /**
     * Returns a subset of {@code stabilizerGenerators} that fix this base point.
     *
     * @return a subset of {@code stabilizerGenerators} that fix this base point
     */
    public List<Permutation> getStabilizersOfThisBasePoint() {
        ArrayList<Permutation> basePointStabilizers = new ArrayList<>();
        for (Permutation previousPointsStabilizer : stabilizerGenerators)
            if (previousPointsStabilizer.newIndexOf(basePoint) == basePoint)
                basePointStabilizers.add(previousPointsStabilizer);
        return basePointStabilizers;
    }

    /**
     * Returns an immutable representation of this _BSGS element
     *
     * @return immutable representation of this _BSGS element
     */
    @Override
    public BSGSElement asBSGSElement() {
        return new BSGSElement(basePoint, Collections.unmodifiableList(stabilizerGenerators), SchreierVector, orbitList.clone());
    }

    @Override
    public BSGSCandidateElement asBSGSCandidateElement() {
        return this;
    }

    @Override
    public BSGSCandidateElement clone() {
        return new BSGSCandidateElement(basePoint,
                new ArrayList<>(stabilizerGenerators),
                SchreierVector.clone(), orbitList.clone());
    }
}

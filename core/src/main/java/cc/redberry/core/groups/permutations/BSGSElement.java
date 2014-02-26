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
import java.util.List;

/**
 * This class holds <i>i-th</i> base point (&beta;<sub>i</sub>), generators of
 * G<sup>(i)</sup> = G<sub>&beta;<sub>1</sub>&beta;<sub>2</sub>...&beta;<sub>i-1</sub></sub> -
 * stabilizer of all base points from <i>0-th</i> to <i>(i-1)-th</i> as described in Sec. 4.4.1 in <b>[Holt05]</b>.
 * <p/>
 * Additionally it provides access to &Delta;<sup>(i)</sup> - the orbit of &beta;<sub>i</sub> under
 * G<sup>(i)</sup>, right transversals of H<sup>(i)</sup><sub>&beta;<sub>i</sub></sub> - stabilizer of
 * &beta;<sub>i</sub> in G<sup>(i)</sup>. This access is based on Schreier vector as described in Sec.
 * 4.1.1 of <b>[Holt05]</b>.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public class BSGSElement {
    /**
     * <i>i-th</i> base point (&beta;<sub>i</sub>)
     */
    public final int basePoint;
    /**
     * Generators of G<sup>(i)</sup> = G<sub>&beta;<sub>1</sub>&beta;<sub>2</sub>...&beta;<sub>i-1</sub></sub> -
     * stabilizer of all base points from <i>0-th</i> to <i>(i-1)-th</i> as described in Sec. 4.4.1 in <b>[Holt05]</b>
     */
    final List<Permutation> stabilizerGenerators;
    /**
     * Schreier vector that encodes information about stabilizer of &beta;<sub>i</sub> in G<sup>(i)</sup>.
     */
    final SchreierVector SchreierVector;
    /**
     * List of orbit points.
     */
    final IntArrayList orbitList;
    /**
     * Maximum internal degrees of stabilizers
     */
    int internalDegree;


    /**
     * Basic raw constructor.
     *
     * @param basePoint
     * @param stabilizerGenerators
     * @param schreierVector
     */
    BSGSElement(int basePoint, List<Permutation> stabilizerGenerators, SchreierVector schreierVector, IntArrayList orbitList) {
        this.basePoint = basePoint;
        this.stabilizerGenerators = stabilizerGenerators;
        this.SchreierVector = schreierVector;
        this.orbitList = orbitList;
        this.internalDegree = Permutations.internalDegree(stabilizerGenerators);
    }

    /**
     * Returns a reference to the list of stabilizers.
     *
     * @return reference to the list of stabilizers
     */
    public List<Permutation> getStabilizerGeneratorsReference() {
        return stabilizerGenerators;
    }

    /**
     * Calculates the transversal of specified point (u<sub>&beta;</sub>), i.e. the element
     * u<sub>&beta;</sub> such that &beta;<sub>i</sub><sup>u<sub>&beta;</sub></sup> =  &beta;.
     *
     * @param point point
     * @return element that maps this base point to the specified point.
     * @see #getInverseTransversalOf(int)
     */
    public Permutation getTransversalOf(int point) {
        Permutation transversal = getInverseTransversalOf(point).inverse();
        assert transversal.newIndexOf(basePoint) == point;
        return transversal;
    }

    /**
     * Calculates the inverse transversal corresponding to the specified point (u<sub>&beta;</sub>), i.e. the element
     * u<sub>&beta;</sub><sup>(-1)</sup> such that &beta;<sub>i</sub><sup>u<sub>&beta;</sub></sup> =  &beta;.
     *
     * @param point point
     * @return inverse of the element that maps this base point to the specified point.
     */
    public Permutation getInverseTransversalOf(int point) {
        if (SchreierVector.get(point) == -2)
            throw new IllegalArgumentException("Specified point does not belong to orbit of this base element.");
        Permutation temp = Permutations.createIdentityPermutation(SchreierVector.length());
        while (SchreierVector.get(temp.newIndexOf(point)) != -1)
            temp = temp.compositionWithInverse(
                    stabilizerGenerators.get(SchreierVector.get(temp.newIndexOf(point))));
        return temp;
    }

    /**
     * Returns an immutable representation of this BSGS element
     *
     * @return immutable representation of this BSGS element
     */
    public BSGSElement asBSGSElement() {
        return this;
    }

    /**
     * Returns true if specified point belongs to the orbit of this &beta;<sub>i</sub>.
     *
     * @param point
     * @return true if specified point belongs to the orbit of this &beta;<sub>i</sub>.
     */
    public boolean belongsToOrbit(int point) {
        return SchreierVector.get(point) != -2;
    }

    /**
     * Returns a mutable copy of this BSGS element.
     *
     * @return a mutable copy of this BSGS element
     */
    public BSGSCandidateElement asBSGSCandidateElement() {
        return new BSGSCandidateElement(basePoint,
                new ArrayList<>(stabilizerGenerators), SchreierVector.length());
    }

    /**
     * Returns size of orbit.
     *
     * @return size of orbit
     */
    public int orbitSize() {
        return orbitList.size();
    }

    /**
     * Returns <i>i-th</i> point in orbit.
     *
     * @param i position in orbit
     * @return <i>i-th</i> point in orbit
     */
    public int getOrbitPoint(int i) {
        return orbitList.get(i);
    }

//    /**
//     * Returns a degree of permutations. More specifically, it returns
//     * {@code stabilizerGenerators.get(0).length();}
//     *
//     * @return degree of permutations
//     */
//    public int degree() {
//        return stabilizerGenerators.get(0).degree();
//    }

    public int internalDegree() {
        return internalDegree;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(basePoint).append(", ").append(stabilizerGenerators).append("]").toString();
    }
}
